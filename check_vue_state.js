(() => {
  const app = document.querySelector('#app').__vue_app__;
  const root = app._instance;
  function findSetupState(comp, depth = 0) {
    if (depth > 10) return null;
    if (comp.setupState && comp.setupState.loginForm) {
      return comp.setupState;
    }
    if (comp.subTree) {
      const child = comp.subTree.component;
      if (child) {
        const result = findSetupState(child, depth + 1);
        if (result) return result;
      }
    }
    return null;
  }
  const state = findSetupState(root);
  if (state) {
    return JSON.stringify({
      username: state.loginForm.username,
      password: state.loginForm.password,
      code: state.loginForm.code
    });
  }
  return 'not found';
})()