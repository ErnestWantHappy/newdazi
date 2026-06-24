const app = document.querySelector('#app').__vue_app__;
const instance = app._instance;
// The login view is inside a router-view, so we need to find the current component
const loginInstance = instance.subTree.component?.subTree.component?.exposed;
// Try the setupState directly
let found = false;
function findLoginVM(comp) {
  if (!comp) return null;
  if (comp.setupState && typeof comp.setupState.handleLogin === 'function') {
    return comp;
  }
  if (comp.setupState && comp.setupState.loginForm) {
    return comp;
  }
  // try children
  if (comp.subTree) {
    let result = findLoginVM(comp.subTree.component);
    if (result) return result;
    if (comp.subTree.children) {
      for (const child of comp.subTree.children) {
        if (child.component) {
          result = findLoginVM(child.component);
          if (result) return result;
        }
      }
    }
  }
  return null;
}
const loginVM = findLoginVM(instance);
if (loginVM) {
  loginVM.setupState.loginForm.username = 'admin';
  loginVM.setupState.loginForm.password = 'admin123';
  loginVM.setupState.handleLogin();
  'login triggered via Vue internals';
} else {
  'login component not found';
}