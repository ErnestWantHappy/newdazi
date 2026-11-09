// 仅验收本目录静态原型，不访问平台或数据库。
import { chromium } from '../../RuoYi-Vue3/node_modules/playwright/index.mjs';
import { fileURLToPath, pathToFileURL } from 'node:url';
import path from 'node:path';
import fs from 'node:fs/promises';
import assert from 'node:assert/strict';

const root = path.dirname(fileURLToPath(import.meta.url));
const out = path.join(root,'screenshots');
await fs.mkdir(out,{recursive:true});
const pages = ['01-editor','02-bank','03-import','04-student','05-designer'];
const browser = await chromium.launch({headless:true});
const results = [];
const errors = [];
const check = (name, value) => { assert.ok(value,name); results.push(name); };
const p = await browser.newPage({viewport:{width:1440,height:1080},deviceScaleFactor:1});
p.on('pageerror',e=>errors.push(e.message));
const goto = async name => {await p.goto(pathToFileURL(path.join(root,name+'.html')).href);await p.waitForLoadState('load');await p.evaluate(()=>document.fonts.ready);};
try {
 for(const name of pages){
  await goto(name);
  check(name+' 无图片加载失败',await p.locator('img').evaluateAll(ns=>ns.every(n=>n.complete&&n.naturalWidth>0)));
  check(name+' 桌面无水平溢出',await p.evaluate(()=>document.documentElement.scrollWidth<=innerWidth));
  await p.screenshot({path:path.join(out,name+'.png'),fullPage:true});
 }
 await goto('01-editor');
 await p.locator('[data-answer="B"]').click();check('答案选择切换',await p.locator('[data-answer="B"]').getAttribute('aria-pressed')==='true');
 await p.locator('[data-zoom]').first().click();check('编辑页放大打开',await p.locator('.lightbox').count()===1);await p.keyboard.press('Escape');check('Esc关闭预览',await p.locator('.lightbox').count()===0);
 await p.locator('input[type=file]').setInputFiles(path.join(out,'04-student.png'));check('点击上传本机预览',await p.locator('#stem-gallery .upload-tile').count()===2);
 await p.locator('#stem-gallery [data-remove-tile]').last().click();check('删除只移除当前缩略图',await p.locator('#stem-gallery .upload-tile').count()===1);
 const pngBytes = [...await fs.readFile(path.join(out,'04-student.png'))];
 await p.evaluate(bytes=>{const d=new DataTransfer();d.items.add(new File([new Uint8Array(bytes)],'demo.png',{type:'image/png'}));document.getElementById('stem').dispatchEvent(new ClipboardEvent('paste',{clipboardData:d,bubbles:true,cancelable:true}));},pngBytes);
 check('Ctrl+V图片事件接入本机预览',await p.locator('#stem-gallery .upload-tile').count()===2);
 await p.locator('#stem-gallery .upload-tile').last().locator('img').evaluate(n=>n.decode());
 check('粘贴图片实际解码可见',await p.locator('#stem-gallery .upload-tile').last().locator('img').evaluate(n=>n.naturalWidth>0));
 await p.evaluate(bytes=>{const d=new DataTransfer();d.items.add(new File([new Uint8Array(bytes)],'drag.png',{type:'image/png'}));document.querySelector('[data-upload-zone="A"]').dispatchEvent(new DragEvent('drop',{dataTransfer:d,bubbles:true,cancelable:true}));},pngBytes);
 check('选项拖拽图片预览',await p.locator('[data-upload-zone="A"] .tiny-photo').count()===1);
 await p.locator('[data-action=save-editor]').click();await p.waitForTimeout(450);check('自动另存提示',await p.locator('.toast').innerText().then(t=>t.includes('已保留历史版本')));
 await p.screenshot({path:path.join(out,'01-editor-saved.png'),fullPage:true});
 await p.locator('.editor-footer a[href*="saved"]').click();check('自动另存结果可找到',await p.locator('[data-question-card="7"]').isVisible());check('另存后原题仍保留',await p.locator('[data-question-card="1"]').count()===1);
 await goto('02-bank');await p.locator('[data-preview="2"]').click();check('题库完整图片选项预览',await p.locator('#preview-content img').count()===4);
 await p.locator('#bank-search').fill('密码');await p.locator('[data-action=search]').click();check('示例题库筛选',await p.locator('[data-question-card]:visible').count()===1);
 await goto('03-import');check('异常未修正前不可确认导入',await p.locator('#confirm-import').isDisabled());
 await p.locator('[data-import-preview="12"]').click();check('导入在线图片修正可见',await p.locator('#fix-panel').isVisible());
 await p.screenshot({path:path.join(out,'03-import-fix.png'),fullPage:true});
 await p.locator('#fix-target').selectOption('C');await p.locator('[data-action=fix-import]').click();check('修正后仍缺必需内容不假通过',await p.locator('#confirm-import').isDisabled());await p.locator('#fix-target').selectOption('D');
 await p.locator('[data-action=fix-import]').click();check('修正后全部可导入',await p.locator('#valid-count').innerText()==='50');check('修正后确认可用',await p.locator('#confirm-import').isEnabled());
 await p.locator('#confirm-import').click();check('导入成功返回入口',await p.locator('.import-success a').isVisible());
 await goto('04-student');await p.locator('[data-q="1"][data-select-answer="C"]').click();await p.locator('[data-q="2"][data-select-answer="A"] [data-zoom]').click();
 check('放大图片不误选答案',await p.locator('#answered-count').innerText()==='1');await p.keyboard.press('Escape');
 await p.locator('[data-q="2"][data-select-answer="A"]').click();await p.screenshot({path:path.join(out,'04-student-selected.png'),fullPage:true});await p.locator('[data-action=submit-student]').click();check('A—D示例评分',await p.locator('#student-status').innerText().then(t=>t.includes('20 / 20')));
 check('已提交锁定',await p.locator('[data-action=submit-student]').isDisabled());
 await goto('05-designer');await p.locator('[data-add-q="2"]').click();check('添加题目',await p.locator('#selected-count').innerText()==='4 题');
 await p.locator('[data-move="2:up"]').click();check('题目排序',await p.locator('#selected-rows tr').nth(2).getAttribute('data-selected-row')==='2');
 await p.locator('[data-score="2"]').fill('15');check('单题分值联动',await p.locator('#total-score').innerText()==='45');
 await p.locator('[data-move="2:up"]').click();check('排序后保留已修改分值',await p.locator('[data-score="2"]').inputValue()==='15'&&await p.locator('#total-score').innerText()==='45');
 for(const width of [390,320]){
  await p.setViewportSize({width,height:844});
  for(const name of pages){await goto(name);check(name+` ${width}px无水平溢出`,await p.evaluate(()=>document.documentElement.scrollWidth<=innerWidth));if(width===390)await p.screenshot({path:path.join(out,name+'-390.png'),fullPage:true});}
 }
 await p.setViewportSize({width:1440,height:1080});await goto('index');check('总览5张设计截图可见',await p.locator('.gallery-card img').evaluateAll(ns=>ns.length===5&&ns.every(n=>n.complete&&n.naturalWidth>0)));await p.screenshot({path:path.join(out,'00-overview.png'),fullPage:true});
 check('页面无JavaScript异常',errors.length===0);
 await fs.writeFile(path.join(root,'prototype-validation.json'),JSON.stringify({date:'2026-11-06',scope:'静态原型；非真实业务验收',passed:results.length,checks:results,errors},null,2),'utf8');
 console.log(JSON.stringify({passed:results.length,errors,screenshots:out}));
}finally{await browser.close();}
