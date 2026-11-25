/* page_auth.html 에서 옮김 (CSP: 인라인 스크립트를 쓰지 않음) */
  // 테스트 로그인 단추. 비밀번호 없이 role 만 보내고, 계정은 서버가 정한다.
  // 토큰을 받은 뒤는 일반 로그인(tryLogin)과 같은 _initSession 을 탄다.
  async function tryDemoLogin(role) {
    const w = document.getElementById('login-warn');
    const res = await api.post('/api/auth/demo', { role: role });
    if (!res || !res.success || !res.data) {
      if (w) {
        w.innerHTML = '⚠️ ' + ((res && res.message) || '테스트 로그인을 할 수 없습니다.');
        w.style.display = 'flex';
      }
      return;
    }
    if (w) w.style.display = 'none';
    await _initSession(res.data.accessToken, res.data.refreshToken);
    toast((_currentUser ? _currentUser.name : '테스트 계정') + '님, 환영합니다! 🎉');
    go(role === 'admin' ? 'admin' : 'main');
  }

  document.addEventListener('DOMContentLoaded', function () {
    fetch('/api/auth/demo', { method: 'GET' })
      .then(function (r) {
        if (!r.ok) return;
        const row = document.getElementById('demo-login-row');
        if (row) row.style.display = 'flex';
      })
      .catch(function () { /* 꺼져 있거나 서버 오류면 단추를 숨긴 채로 둔다 */ });
  });
