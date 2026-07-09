(function() {
  try {
    // Find the VForm3 designer component
    var els = document.querySelectorAll('[class*="v-form"]');
    var found = [];
    for (var i = 0; i < els.length; i++) {
      found.push(els[i].className);
    }
    return JSON.stringify(found);
  } catch(e) {
    return e.message;
  }
})()