import { createRequire } from 'node:module'
import { pathToFileURL } from 'node:url'
import path from 'node:path'
import fs from 'node:fs/promises'
import assert from 'node:assert/strict'

// 真实路由容器和题库页面配模拟接口，避免修改业务数据。
const root = path.resolve('RuoYi-Vue3')
const require = createRequire(path.join(root, 'package.json'))
const load = async name => (await import(pathToFileURL(require.resolve(name)).href)).default
const { createServer } = await import(pathToFileURL(require.resolve('vite')).href)
const vue = await load('@vitejs/plugin-vue')
const autoImportModule = await load('unplugin-auto-import/vite')
const autoImport = autoImportModule.default || autoImportModule
const { chromium } = require('playwright')
const output = path.resolve('output/playwright/question-navigation-20261020')
await fs.mkdir(output, { recursive: true })
const modules = {
  'nav-request': `export default async ({url})=>{
    if(url.endsWith('/dashboard-data'))return {data:[]};
    if(url.endsWith('/advance-policy'))return {data:{autoAdvanceEnabled:false}};
    return {code:200,rows:[],total:0,data:{hasTask:false}};
  }`,
  'nav-user': `export default ()=>({roles:['teacher'],id:1})`,
  'nav-tags': `import {reactive} from 'vue';
    const store=reactive({cachedViews:[],iframeViews:[],addIframeView(){}});
    export default ()=>store;`,
  'nav-entry': `
    import {createApp,ref} from 'vue';
    import {createRouter,createMemoryHistory} from 'vue-router';
    import ElementPlus from 'element-plus';import 'element-plus/dist/index.css';
    import * as icons from '@element-plus/icons-vue';
    import AppMain from '/src/layout/components/AppMain.vue';
    import tags from 'nav-tags';
    import '/src/assets/styles/transition.scss';
    const router=createRouter({history:createMemoryHistory(),routes:[
      {path:'/teacher-dashboard/index',component:()=>import('/src/views/business/teacher/index.vue'),name:'TeacherDashboard'},
      {path:'/question',component:()=>import('/src/views/business/question/index.vue'),name:'Question'}
    ]});
    window.router=router;window.tags=tags();window.warnings=[];
    router.beforeEach(()=>document.documentElement.classList.add('route-pending'));
    router.afterEach(()=>document.documentElement.classList.remove('route-pending'));
    await router.push('/teacher-dashboard/index');await router.isReady();
    const app=createApp({components:{AppMain},template:'<nav><router-link to="/teacher-dashboard/index">教师首页</router-link> <router-link to="/question">题库管理</router-link></nav><AppMain/>'});
    app.use(router).use(ElementPlus);
    for(const [name,icon] of Object.entries(icons))app.component(name,icon);
    for(const name of ['pagination','right-toolbar','dict-tag','editor','file-upload','image-upload'])app.component(name,{template:'<div/>'});
    app.directive('hasPermi',{});
    app.config.globalProperties.useDict=(...names)=>Object.fromEntries(names.map(n=>[n,ref([])]));
    app.config.warnHandler=message=>window.warnings.push(message);
    app.mount('#app');
  `
}
const server = await createServer({
  configFile:false,root,logLevel:'error',
  plugins:[{
    name:'question-navigation-harness',enforce:'pre',
    resolveId(id){if(modules[id])return '\0'+id},
    load(id){return modules[id.slice(1)]},
    transform(code,id){
      if(id.replaceAll('\\','/').endsWith('/business/teacher/index.vue')){
        return code.replace(/import ResearchNotificationBar[^\n]+/,'const ResearchNotificationBar={template:"<div/>"}')
      }
    },
    configureServer(s){s.middlewares.use('/navigation-test',async (_req,res)=>{
      res.setHeader('Content-Type','text/html');
      res.end(await s.transformIndexHtml('/navigation-test','<html><meta charset="utf-8"><div id="app"></div><script type="module" src="/@id/nav-entry"></script></html>'));
    })}
  },vue(),autoImport({imports:['vue','vue-router','pinia'],dts:false})],
  resolve:{extensions:['.mjs','.js','.ts','.jsx','.tsx','.json','.vue'],alias:[{find:/^vue$/,replacement:require.resolve('vue/dist/vue.esm-bundler.js')},
    {find:'@/utils/request',replacement:'nav-request'},
    {find:'@/store/modules/user',replacement:'nav-user'},
    {find:'@/store/modules/tagsView',replacement:'nav-tags'},
    {find:'@',replacement:path.join(root,'src')}]},
  cacheDir:path.join(output,'.vite-cache'),
  optimizeDeps:{entries:[],include:['vue','vue-router','element-plus','@element-plus/icons-vue','xlsx']},
  server:{host:'127.0.0.1',port:18131,strictPort:true}
})
const checks=[], errors=[]
let browser
try{
  await server.listen();browser=await chromium.launch({headless:true});
  for(const width of [1440,390]){
    for(const cached of [false,true]){
      const page=await browser.newPage({viewport:{width,height:950}});
      page.setDefaultTimeout(15000);
      page.on('pageerror',e=>{errors.push(e.message);console.error(e.message)});
      page.on('console',m=>{if(m.type()==='error')console.error(m.text())});
      await page.goto('http://127.0.0.1:18131/navigation-test');
      await page.getByText('课程设置',{exact:true}).waitFor();
      if(cached)await page.evaluate(()=>window.tags.cachedViews=['TeacherDashboard','Question']);
      for(let i=0;i<3;i++){
        await page.getByRole('link',{name:'题库管理',exact:true}).click();
        await page.getByText('全部题目',{exact:true}).waitFor();
        if(i===0){
          await page.getByRole('button',{name:'新建数据分析实验题',exact:true}).click();
          const editor=page.getByRole('dialog',{name:'数据分析实验题',exact:true});
          await editor.waitFor();
          await editor.getByRole('button',{name:'取消',exact:true}).click();
          await editor.waitFor({state:'hidden'});
        }
        await page.getByRole('link',{name:'教师首页',exact:true}).click();
        await page.getByText('课程设置',{exact:true}).waitFor({timeout:5000});
      }
      await page.screenshot({path:path.join(output,`${width}-${cached?'cached':'uncached'}.png`)});
      const transitionWarnings=await page.evaluate(()=>window.warnings.filter(w=>/non-element root|inside <Transition>/.test(w)));
      assert.deepEqual(transitionWarnings,[]);
      checks.push({width,cached,roundTrips:3,dataLabDialog:true});await page.close();
    }
  }
  assert.deepEqual(errors,[]);
  await fs.writeFile(path.join(output,'result.json'),JSON.stringify({checks,errors},null,2));
  console.log(JSON.stringify({checks,errors}));
}finally{await browser?.close();await server.close()}
