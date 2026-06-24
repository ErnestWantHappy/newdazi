const app = document.querySelector('#app').__vue_app__;
const vm = app._instance.proxy;
vm.loginForm.username = 'admin';
vm.loginForm.password = 'admin123';
vm.handleLogin();
'login triggered'