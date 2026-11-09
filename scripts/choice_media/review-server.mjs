import http from 'node:http'
import { readFile } from 'node:fs/promises'
import path from 'node:path'
import { fileURLToPath } from 'node:url'
const root=path.resolve(path.dirname(fileURLToPath(import.meta.url)),'../../RuoYi-Vue3/dist')
const types={'.html':'text/html; charset=utf-8','.js':'text/javascript','.css':'text/css','.svg':'image/svg+xml','.png':'image/png','.jpg':'image/jpeg','.woff2':'font/woff2','.json':'application/json'}
http.createServer(async(req,res)=>{
  if(/^\/(prod|dev)-api(?:\/|$)/.test(req.url)){
    const target=http.request({host:'127.0.0.1',port:18080,path:req.url.replace(/^\/(prod|dev)-api/,''),method:req.method,headers:{...req.headers,host:'localhost:18080'}},up=>{res.writeHead(up.statusCode,up.headers);up.pipe(res)})
    target.on('error',()=>{res.writeHead(502);res.end('验收接口未就绪')});req.pipe(target);return
  }
  try{
    let name=decodeURIComponent(req.url.split('?')[0]);let file=path.resolve(root,'.'+name)
    if(!file.startsWith(root+path.sep)){file=path.join(root,'index.html')}
    let bytes;try{bytes=await readFile(file)}catch{file=path.join(root,'index.html');bytes=await readFile(file)}
    res.writeHead(200,{'Content-Type':types[path.extname(file)]||'application/octet-stream','Cache-Control':'no-store'});res.end(bytes)
  }catch{res.writeHead(500);res.end('读取失败')}
}).listen(4178,'127.0.0.1',()=>console.log('本机生产构建验收入口 http://127.0.0.1:4178'))
