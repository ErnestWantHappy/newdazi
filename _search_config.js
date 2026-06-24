const fs = require('fs');
const c = fs.readFileSync('E:/Project/newdazi/RuoYi-Vue3/node_modules/vform3-builds/dist/designer.umd.js', 'utf8');

// Search for bannedWidgets context
let idx = c.indexOf('bannedWidgets');
if (idx > -1) {
  console.log('--- bannedWidgets context ---');
  console.log(c.substring(Math.max(0, idx - 200), idx + 400));
  console.log('--- END ---');
}

// Search getBannedWidgets
idx = c.indexOf('getBannedWidgets');
if (idx > -1) {
  console.log('\n--- getBannedWidgets context ---');
  console.log(c.substring(Math.max(0, idx - 300), idx + 200));
}

// Search for provide/inject of banned
idx = c.indexOf('provide("getBannedWidgets"');
if (idx === -1) idx = c.indexOf("provide('getBannedWidgets'");
if (idx === -1) idx = c.indexOf('provide(getBannedWidgets');
if (idx > -1) {
  console.log('\n--- provide bannedWidgets ---');
  console.log(c.substring(Math.max(0, idx - 100), idx + 300));
}

// Search for bannedWidgets in designerConfig
idx = c.indexOf('bannedWidgets');
let count = 0;
let pos = 0;
while ((pos = c.indexOf('bannedWidgets', pos + 1)) > -1 && count < 5) {
  console.log(`\n--- bannedWidgets[${++count}] at ${pos} ---`);
  console.log(c.substring(Math.max(0, pos - 80), pos + 200));
}

// Also search for 'ban' 
idx = c.indexOf('isBanned');
if (idx > -1) {
  console.log('\n--- isBanned context ---');
  console.log(c.substring(Math.max(0, idx - 50), idx + 150));
}