/* page_auth.html 에서 옮김 (CSP: 인라인 스크립트를 쓰지 않음) */
  // prShowStep 오버라이드 → 스텝 인디케이터 동기화
  window.prShowStep = function(n) {
    document.querySelectorAll('.pw-reset-step').forEach(el => el.classList.remove('active'));
    document.getElementById('pw-reset-step' + n).classList.add('active');
    [1,2,3].forEach(i => {
      const dot  = document.getElementById('pr-dot'  + i);
      const line = document.getElementById('pr-line' + i);
      dot.classList.toggle('active', i === n);
      dot.classList.toggle('done',   i < n);
      if (line) line.classList.toggle('done', i < n);
    });
  };
