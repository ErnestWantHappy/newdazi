import { createServer } from 'vite'
import { chromium } from 'playwright'
import assert from 'node:assert/strict'
import fs from 'node:fs/promises'
const server=await createServer({server:{host:'127.0.0.1',port:5197,strictPort:true,open:false},plugins:[{
  name:'lab-runtime-check',
  configureServer(server){server.middlewares.use('/__lab_runtime',(_,res)=>{res.setHeader('Content-Type','text/html');res.end('<html><body><script type="module">import {createPyodideRunner} from "/src/utils/pyodideRunner.js";window.runner=createPyodideRunner();</script></body></html>')})}
}]})
let browser
try {
  await server.listen();browser=await chromium.launch({headless:true})
  const page=await browser.newPage();await page.goto('http://127.0.0.1:5197/__lab_runtime')
  await page.waitForFunction(()=>!!window.runner)
  const result=await page.evaluate(async()=>{
    await window.runner.initPyodide()
    return window.runner.runCode(`import pandas as pd\nimport matplotlib.pyplot as plt\ndf=pd.DataFrame({'成绩':[80,90,100]})\ndf.to_excel('高中运动会成绩统计.xlsx',index=False)\ndf=pd.read_excel('高中运动会成绩统计.xlsx')\nprint(int(df['成绩'].sum()))\nplt.plot(df['成绩'])\nplt.title('高中运动会成绩')\nplt.show()`)
  })
  assert.equal(result.error,null);assert.match(result.output,/270/);assert.ok(result.chartBase64?.startsWith('data:image/png;base64,'))
  assert.doesNotMatch(result.output,/Glyph .* missing/)
  await fs.writeFile('../output/datalab-runtime-chart.png',Buffer.from(result.chartBase64.split(',')[1],'base64'))
  const timeout=await page.evaluate(async()=>{try{await window.runner.runCode('while True:\n    pass');return false}catch(e){return e.message.includes('超时')}})
  assert.equal(timeout,true)
  const recovered=await page.evaluate(()=>window.runner.runCode('print(42)'))
  assert.equal(recovered.output,'42')
  await fs.writeFile('../output/datalab-runtime-result.json',JSON.stringify({excel:true,png:true,timeout,recovery:true,output:result.output},null,2))
  console.log('真实 Pyodide：Excel读写、PNG绘图、30秒死循环终止及恢复通过')
} finally {await browser?.close();await server.close()}
