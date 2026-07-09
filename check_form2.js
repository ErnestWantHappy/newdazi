(function() {
  try {
    var card = document.querySelector('.designer-card');
    if (!card) return 'no card';
    return card.innerHTML.substring(0, 500);
  } catch(e) {
    return e.message;
  }
})()