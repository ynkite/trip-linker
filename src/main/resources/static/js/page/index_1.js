/* index.html 에서 옮김 (CSP: 인라인 스크립트를 쓰지 않음) */
  /* [반응형] 모바일 nav: 메뉴/버튼 클릭 시 자동 닫힘 */
  (function(){
    var navEl = document.querySelector('nav');
    if(!navEl) return;
    navEl.addEventListener('click', function(e){
      var t = e.target.closest('.nav-link, .nav-btns button, .nav-btns a');
      if(t && t.id !== 'navHamburger'){
        navEl.classList.remove('nav-open');
      }
    });
  }());
