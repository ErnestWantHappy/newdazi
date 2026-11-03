import { createRequire } from 'node:module'
import { pathToFileURL } from 'node:url'
import path from 'node:path'
import fs from 'node:fs/promises'
import assert from 'node:assert/strict'

// 用真实上传区模板和提交函数配模拟接口，避免改动学生作品与成绩。
const root = path.resolve('RuoYi-Vue3')
const require = createRequire(path.join(root, 'package.json'))
const { createServer } = await import(pathToFileURL(require.resolve('vite')).href)
const vue = (await import(pathToFileURL(require.resolve('@vitejs/plugin-vue')).href)).default
const { chromium } = require('playwright')
const source = await fs.readFile(path.join(root, 'src/views/student/index.vue'), 'utf8')
const start = source.indexOf('<div class="upload-section">')
const template = source.slice(start, source.indexOf('</el-card>', start))
const logic = source.slice(source.indexOf('function getPracticalUploadLimit('), source.indexOf('function getPracticalAttachments('))
const sync = source.slice(source.indexOf('function syncPracticalSubmission('), source.indexOf('function clearPracticalPolling('))
const styles = [...source.matchAll(/<style[^>]*>([\s\S]*?)<\/style>/g)].map(match=>match[1]).join('\n')
const output = path.resolve('output/playwright/practical-resubmit-20261020')
await fs.mkdir(output, { recursive: true })
const component = `<template><div class="practical-card"><strong>文件作业 · 当前分数：{{ practicalScores[3] ?? '待批改' }}</strong>${template}</div></template>
<script setup>
import {ref} from 'vue';import {ElMessage,ElMessageBox} from 'element-plus';
const q={questionId:3,practicalAllowedExtensions:'pdf'},lessonId=ref(2),practicalScores=ref({3:68});
const practicalArtifacts=ref({3:{versionId:5,attachments:[{resourcePath:'/old.pdf',originalFileName:'原作业.pdf',previewStatus:'success'}]}});
const practicalUploads=ref({3:'/old.pdf'}), submittedAnswers=ref({3:{answer:'/old.pdf',score:68,taskState:'GRADED'}});
const practicalPreviewPaths=ref({}),practicalPreviewStatuses=ref({}),practicalDrafts=ref({}),practicalFileLists=ref({});
const submittingPracticalQuestionId=ref(null),uploadingQuestionId=ref(null),practicalUploadStates=ref({});
const practicalAutoCommitTimers=new Map(),practicalFileNameLimit=100;
let practicalUploadEpoch=1;const uploadUrl='/upload',uploadHeaders={};
function setPracticalUploader(){};function getPracticalAllowedExtensions(){return ['pdf']};function getPracticalAccept(){return '.pdf'};
function markQuestionWorking(){};function getFileName(p){return p};function formatPracticalFileSize(){return '1 KB'};
function getPracticalAttachments(id){return practicalArtifacts.value[id]?.attachments||[]};
function getPracticalDisplayName(){return practicalArtifacts.value[3]?.attachments[0]?.originalFileName};
function getPracticalPreviewClass(){return ''};function getPracticalPreviewLabel(){return '可预览'};
function canPreviewPractical(){return true};function previewWork(){};function downloadSubmittedWork(){};function deleteWork(){};
function schedulePracticalPreviewPolling(){};
async function refreshPracticalSubmission(){return submittedAnswers.value[3]};
window.calls=0;window.failSubmit=false;window.slowSubmit=false;
window.setScore=n=>practicalScores.value[3]=n;
window.state=()=>({score:practicalScores.value[3],version:practicalArtifacts.value[3]?.versionId});
async function submitPracticalArtifact(){window.calls++;if(window.slowSubmit)await new Promise(r=>setTimeout(r,700));if(window.failSubmit)throw new Error('模拟提交失败');return {data:{versionId:7,attachments:[{resourcePath:'/new.pdf',originalFileName:'新作业.pdf',previewStatus:'success'}]}}};
${sync}
${logic}
</script><style scoped lang="scss">${styles}</style>`
const server = await createServer({configFile:false,root,logLevel:'error',plugins:[{
  name:'practical-resubmit-harness',
  resolveId(id){if(id==='resubmit-component.vue')return path.join(root,'__resubmit-component.vue');if(id==='resubmit-entry')return '\0'+id},
  load(id){if(id.replaceAll('\\','/')===path.join(root,'__resubmit-component.vue').replaceAll('\\','/'))return component;if(id==='\0resubmit-entry')return `import{createApp}from'vue';import ElementPlus from'element-plus';import'element-plus/dist/index.css';import*as icons from'@element-plus/icons-vue';import App from'resubmit-component.vue';const app=createApp(App).use(ElementPlus);for(const[n,c]of Object.entries(icons))app.component(n,c);app.mount('#app')`},
  configureServer(s){
    s.middlewares.use('/upload',(req,res)=>{req.resume();req.on('end',()=>{res.setHeader('Content-Type','application/json');res.end(JSON.stringify({code:200,uploadToken:'ticket',fileName:'/new.pdf',newFileName:'新作业.pdf',fileKind:'PDF'}))})});
    s.middlewares.use('/resubmit-test',async(_req,res)=>{res.setHeader('Content-Type','text/html');res.end(await s.transformIndexHtml('/resubmit-test','<html><meta charset="utf-8"><meta name="viewport" content="width=device-width,initial-scale=1"><div id="app"></div><script type="module" src="/@id/resubmit-entry"></script></html>'))})
  }
},vue()],resolve:{alias:{vue:require.resolve('vue/dist/vue.esm-bundler.js')}},optimizeDeps:{entries:[],include:['vue','element-plus','@element-plus/icons-vue']},cacheDir:path.join(output,'.vite-cache'),server:{host:'127.0.0.1',port:18132,strictPort:true}})
let browser
const checks=[],errors=[]
try {
  await server.listen();browser=await chromium.launch({headless:true})
  for(const width of [1440,390]){
    const page=await browser.newPage({viewport:{width,height:950}})
    page.on('pageerror',e=>errors.push(e.message))
    await page.goto('http://127.0.0.1:18132/resubmit-test')
    await page.getByRole('button',{name:'重新提交作业',exact:true}).waitFor()
    assert.equal(await page.getByRole('button',{name:'删除',exact:true}).count(),0)
    await page.locator('input[type=file]').setInputFiles({name:'新作业.pdf',mimeType:'application/pdf',buffer:Buffer.from('%PDF-1.4 test')})
    await page.getByText('新作品提交成功后，原分数将清除', {exact:false}).waitFor()
    assert.deepEqual(await page.evaluate(()=>window.state()),{score:68,version:5})
    await page.getByRole('button',{name:'保留原作业',exact:true}).click()
    assert.equal(await page.evaluate(()=>window.calls),0)
    await page.getByRole('button',{name:'重试提交',exact:true}).click()
    await page.evaluate(()=>{window.failSubmit=true})
    await page.getByRole('button',{name:'提交新作品',exact:true}).click()
    await page.getByText('作品提交失败，文件已保留，请稍后重试').waitFor()
    assert.deepEqual(await page.evaluate(()=>window.state()),{score:68,version:5})
    await page.evaluate(()=>{window.failSubmit=false;window.slowSubmit=true})
    await page.getByRole('button',{name:'重试提交',exact:true}).click()
    await page.getByRole('button',{name:'提交新作品',exact:true}).click()
    await page.waitForFunction(()=>window.calls===2)
    assert.deepEqual(await page.evaluate(()=>window.state()),{score:68,version:5})
    await page.waitForFunction(()=>window.state().version===7)
    assert.equal(await page.evaluate(()=>window.state().score),null)
    await page.getByRole('button',{name:'重新提交作业',exact:true}).isEnabled()
    await page.waitForFunction(()=>!document.querySelector('.el-message'))
    await page.screenshot({path:path.join(output,`${width}-success.png`),fullPage:true})
    await page.reload();await page.waitForFunction(()=>!!window.setScore)
    await page.evaluate(()=>window.setScore(0))
    await page.locator('input[type=file]').setInputFiles({name:'重做.pdf',mimeType:'application/pdf',buffer:Buffer.from('%PDF-1.4 test')})
    await page.getByRole('button',{name:'提交新作品',exact:true}).waitFor()
    assert.equal(await page.evaluate(()=>window.state().score),0)
    await page.getByRole('button',{name:'提交新作品',exact:true}).click()
    await page.waitForFunction(()=>window.state().version===7)
    checks.push({width,cancel:true,failed:true,slow:true,success:true,zero:true})
    await page.close()
  }
  assert.deepEqual(errors,[])
  await fs.writeFile(path.join(output,'report.json'),JSON.stringify({checks,errors},null,2))
  console.log(JSON.stringify({checks,errors}))
} finally {await browser?.close();await server.close()}
