const fs = require('fs');
const c = fs.readFileSync('E:/Project/newdazi/RuoYi-Vue3/node_modules/vform3-builds/dist/designer.umd.js', 'utf8');
const types = new Set();
const re = /type:"([a-z][a-z0-9-]*)"/g;
let m;
while ((m = re.exec(c)) !== null) types.add(m[1]);
const sorted = [...types].sort();
console.log(JSON.stringify(sorted, null, 2));