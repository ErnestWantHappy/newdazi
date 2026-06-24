fetch('/dev-api/login', {
  method: 'POST',
  headers: { 'Content-Type': 'application/json' },
  body: JSON.stringify({
    username: 'admin',
    password: 'admin123',
    code: '',
    uuid: ''
  })
}).then(r => r.json()).then(d => JSON.stringify(d)).catch(e => e.message)