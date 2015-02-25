function test(){
  document.body.style.background ="#00FF00"; //turns to green the background color
}

function defer(method) {
    if (window.jQuery)
        method();
    else
        setTimeout(function() { defer(method) }, 50);
}

defer(function() {
    test();
});