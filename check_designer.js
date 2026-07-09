(function() {
  try {
    // Find the designer component
    var card = document.querySelector('.designer-card');
    if (!card) return 'no designer-card';
    
    // Find the v-form-designer element
    var all = card.querySelectorAll('*');
    var designerEl = null;
    for (var i = 0; i < all.length; i++) {
      if (all[i].__vue_app__ && all[i].__vue_app__._context) {
        var apps = all[i].__vue_app__._context.apps;
        for (var j = 0; j < apps.length; j++) {
          var inst = apps[j]._instance;
          if (inst && inst.setupState && inst.setupState.designerRef) {
            var ref = inst.setupState.designerRef;
            if (ref && ref.value) {
              var d = ref.value;
              return 'designer found: ' + (d.designer ? 'has designer' : 'no designer') + ' getFormJson: ' + (typeof d.getFormJson);
            }
          }
        }
      }
    }
    return 'no designer found';
  } catch(e) {
    return 'error: ' + e.message;
  }
})()