(function() {
  try {
    var v = document.querySelector('.designer-card');
    if (!v) return 'no designer';
    var designers = v.querySelectorAll('.v-form-designer');
    if (!designers.length) return 'no v-form-designer';
    var designer = designers[0];
    var vm = designer.__vue_app__;
    if (!vm) return 'no vue app';
    var apps = vm._context.apps;
    if (!apps) return 'no apps';
    for (var i = 0; i < apps.length; i++) {
      var inst = apps[i]._instance;
      if (inst && inst.designerRef) {
        try {
          var json = inst.designerRef.getFormJson();
          if (json && json.widgetList) {
            return JSON.stringify(json.widgetList).substring(0, 2000);
          }
        } catch(e) {}
      }
    }
    return 'not found';
  } catch(e) {
    return e.message;
  }
})()