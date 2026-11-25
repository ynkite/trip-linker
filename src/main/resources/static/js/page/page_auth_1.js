/* page_auth.html 에서 옮김 (CSP: 인라인 스크립트를 쓰지 않음) */
  /* ============================================================
   * [page_auth.html] 하드코딩 데이터 → 변수 분리
   * ============================================================ */

  // ── API 엔드포인트 ──
  const AUTH_API = {
    checkUsername:  '/api/auth/check-username',
    checkEmail:     '/api/auth/check-email',
    sendEmail:      '/api/auth/send-email',
    verifyEmail:    '/api/auth/verify-email',
    signup:         '/api/auth/signup',
    pwResetRequest: '/api/auth/password/reset-request',
    pwResetVerify:  '/api/auth/verify-email',
    pwReset:        '/api/auth/password/reset',
  };

  // ── 페이지 텍스트 ──
  const AUTH_TEXT = {
    signupTitle:   '회원가입',
    signupSub:     'TripLinker와 함께 완성형 여행을 시작하세요',
    loginTitle:    '로그인',
    loginSub:      'TripLinker에 오신 것을 환영합니다',
    kakaoTitle:    '카카오 계정으로 가입',
    kakaoSub:      '카카오 계정이 연결되었습니다',
    kakaoNotice:   '🟡 카카오로 연결되었습니다. 아이디·이메일·비밀번호는 카카오 계정으로 대체됩니다.',
  };

  // ── 거주 지역 도/시 목록 ──
  const REGION_PROVINCES = [
    '서울','경기','인천','강원','충북','충남','대전','세종',
    '전북','전남','광주','경북','경남','대구','울산','부산','제주',
  ];

  // ── 성별 옵션 ──
  const GENDER_OPTIONS = [
    { val: 'M', label: '남성' },
    { val: 'F', label: '여성' },
  ];

  // ── MBTI 차원 정의 ──
  const MBTI_DIMS = [
    { key: 'ei', label: '에너지', pairs: [{ val:'E', label:'E (외향)' }, { val:'I', label:'I (내향)' }], default: 'E' },
    { key: 'sn', label: '인식',   pairs: [{ val:'S', label:'S (감각)' }, { val:'N', label:'N (직관)' }], default: 'S' },
    { key: 'tf', label: '판단',   pairs: [{ val:'T', label:'T (사고)' }, { val:'F', label:'F (감정)' }], default: 'T' },
    { key: 'jp', label: '생활',   pairs: [{ val:'J', label:'J (계획)' }, { val:'P', label:'P (즉흥)' }], default: 'P' },
  ];

  // ── 비밀번호 정규식 ──
  const PW_PATTERN = /^(?=.*[0-9])(?=.*[a-zA-Z])(?=.*\W)(?=\S+$).{8,16}$/;
  const PW_PATTERN_MSG = '비밀번호는 8~16자 영문 대 소문자, 숫자, 특수문자를 포함해야 합니다.';

  // ── 유효성 검사 메시지 ──
  const AUTH_VALID_MSG = {
    idEmpty:       '아이디를 먼저 입력해주세요.',
    idDuplicate:   '❌ 이미 사용 중인 아이디입니다.',
    idOk:          '✅ 사용 가능한 아이디입니다!',
    emailEmpty:    '이메일을 입력해주세요.',
    emailInvalid:  '올바른 이메일 형식이 아닙니다. (@가 포함된 이메일을 입력해주세요.)',
    emailDuplicate:'❌ 이미 가입된 이메일입니다. 다른 이메일을 사용해주세요.',
    emailSending:  '메일을 발송 중입니다. 잠시만 기다려주세요...',
    emailSent:     '✅ 인증 메일이 발송되었습니다! 메일함을 확인해주세요.',
    verifyOk:      '✅ 이메일 인증이 완료되었습니다!',
    verifyFail:    '❌ 인증번호가 틀렸거나 만료되었습니다.',
    nameEmpty:     '이름을 입력해주세요.',
    emailEmptyForm:'이메일을 입력해주세요.',
    emailNotVerify:'⚠️ 이메일 인증을 먼저 완료해주세요!',
    pwEmpty:       '비밀번호를 입력해주세요.',
    birthEmpty:    '생년월일을 선택해주세요.',
    regionEmpty:   '거주 지역(도/시)을 선택해주세요.',
    genderEmpty:   '성별을 선택해주세요.',
    mbtiIncomplete:'MBTI 4가지 항목을 모두 선택해주세요.',
    signupSuccess: '🎉 TripLinker 회원가입 성공!',
    signupFail:    '가입 실패: 입력 조건을 다시 확인해주세요.',
  };

  // ── 지역별 시/군/구 목록 ──
  const CITY_DATA = {
    '서울': ['강남구','강동구','강북구','강서구','관악구','광진구','구로구','금천구','노원구','도봉구','동대문구','동작구','마포구','서대문구','서초구','성동구','성북구','송파구','양천구','영등포구','용산구','은평구','종로구','중구','중랑구'],
    '경기': ['수원시','성남시','고양시','용인시','부천시','안산시','안양시','남양주시','화성시','평택시','의정부시','시흥시','파주시','광명시','김포시','군포시','광주시','이천시','양주시','오산시','구리시','안성시','포천시','의왕시','하남시','여주시','양평군','동두천시','과천시','가평군','연천군'],
    '인천': ['계양구','미추홀구','남동구','동구','부평구','서구','연수구','중구','강화군','옹진군'],
    '부산': ['강서구','금정구','기장군','남구','동구','동래구','부산진구','북구','사상구','사하구','서구','수영구','연제구','영도구','중구','해운대구'],
    '제주': ['제주시','서귀포시'],
  };

  // page_auth.html 상단 스크립트 내 기존 함수를 아래와 같이 변경
  function initAuthPage() {
    // 변경: querySelector -> querySelectorAll 로 카카오 드롭다운까지 한 번에 선택
    const provSelectors = document.querySelectorAll('select.loc-big[data-onchange*="updateCity"]');

    provSelectors.forEach(provSel => {
      if (provSel) {
        provSel.innerHTML = `<option value="">도/시 선택</option>` +
                REGION_PROVINCES.map(p => `<option>${p}</option>`).join('');
      }
    });

    // MBTI 선택기 (기존 로직 유지)
    const mbtiSel = document.getElementById('mbti-selector');
    if (mbtiSel) {
      mbtiSel.innerHTML = MBTI_DIMS.map(d => `
      <div style="display:flex;gap:6px;align-items:center">
        <span style="font-size:11px;color:var(--text3);width:52px;font-weight:600">${d.label}</span>
        ${d.pairs.map(p => `
          <button class="chip chip-sm${p.val === d.default ? ' on' : ''}"
            data-onclick="selectMbti(this,'${d.key}','${p.val}')"
            data-pos="${MBTI_DIMS.findIndex(x => x.key === d.key)}"
            data-val="${p.val}">${p.label}</button>
        `).join('')}
      </div>
    `).join('');
    }
  }
  // // MBTI 선택기 삭제예정
  // const mbtiSel = document.getElementById('mbti-selector');
  // if (mbtiSel) {
  //   mbtiSel.innerHTML = MBTI_DIMS.map(d => `
  //     <div style="display:flex;gap:6px;align-items:center">
  //       <span style="font-size:11px;color:var(--text3);width:52px;font-weight:600">${d.label}</span>
  //       ${d.pairs.map(p => `
  //         <button class="chip chip-sm${p.val === d.default ? ' on' : ''}"
  //           data-onclick="selectMbti(this,'${d.key}','${p.val}')"
  //           data-pos="${MBTI_DIMS.findIndex(x => x.key === d.key)}"
  //           data-val="${p.val}">${p.label}</button>
  //       `).join('')}
  //     </div>
  //   `).join('');
  // }
  // }

  // ── 지역 select 업데이트 ──
  // page_auth.html 상단 스크립트 내 기존 함수를 아래와 같이 변경
  // 두 번째 인자로 하위 시군구 select의 ID를 받도록 확장 (기본값은 일반 폼의 'city-select')
  function updateCity(provinceSelect, targetId = 'city-select') {
    const citySelect = document.getElementById(targetId);
    if (!citySelect) return;

    const cities = CITY_DATA[provinceSelect.value] || [];
    citySelect.innerHTML = '<option value="">시/군/구 선택</option>' +
            cities.map(c => `<option value="${c}">${c}</option>`).join('');
  }

  // ── 아이디 중복확인 ──
  function checkDuplicateId() {
    const username = document.getElementById('username').value;
    if (!username) { alert(AUTH_VALID_MSG.idEmpty); return; }
    fetch(`${AUTH_API.checkUsername}?username=${username}`)
            .then(res => res.text())
            .then(isDuplicate => {
              alert(isDuplicate === 'true' ? AUTH_VALID_MSG.idDuplicate : AUTH_VALID_MSG.idOk);
            });
  }

  // ── 이메일 인증 발송 ──
  function sendAuthCode(btnElement) {
    const email = document.getElementById('email').value.trim();
    if (!email) { alert(AUTH_VALID_MSG.emailEmpty); return; }
    if (!/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(email)) { alert(AUTH_VALID_MSG.emailInvalid); return; }

    // 1. HTML에서 넘겨준 버튼(this)이 있으면 쓰고, 없으면 쿼리로 찾습니다.
    const btn = btnElement || document.querySelector('button[data-onclick*="sendAuthCode"]');

    fetch(`${AUTH_API.checkEmail}?email=${email}`)
            .then(res => res.text())
            .then(isDuplicate => {
              if (isDuplicate === 'true') {
                alert(AUTH_VALID_MSG.emailDuplicate);
              } else {
                // 2. 알림창 대신 버튼의 글자, 색상, 커서를 바꿉니다.
                if (btn) {
                  btn.innerText = "발송 중...";
                  btn.disabled = true;
                  btn.style.background = "var(--text3)"; // TripLinker 테마 회록색
                  btn.style.cursor = "wait";             // 로딩 커서
                }

                fetch(`${AUTH_API.sendEmail}?email=${encodeURIComponent(email)}`, { method: 'POST' })
                        .then(async (res) => {
                          // 서버가 거절(발송 횟수 초과 429 등)하면 그 안내를 보여 준다
                          if (!res.ok) {
                            let msg = "⚠️ 메일 발송에 실패했습니다. 다시 시도해주세요.";
                            try { msg = (await res.json()).message || msg; } catch (e) {}
                            alert(msg);
                            return;
                          }
                          alert(AUTH_VALID_MSG.emailSent);
                        })
                        .catch(() => alert("⚠️ 메일 발송에 실패했습니다. 다시 시도해주세요."))
                        .finally(() => {
                          // 3. 발송 완료 후(성공/실패 무관) 버튼을 원래대로 복구합니다.
                          if (btn) {
                            btn.innerText = "인증 받기";
                            btn.disabled = false;
                            btn.style.background = ""; // 원래 CSS 색상(var(--sage))으로 복구
                            btn.style.cursor = "pointer";
                          }
                        });
              }
            });
  }

  // ── 이메일 인증 확인 ──
  let isEmailVerified = false;
  function verifyAuthCode() {
    const email = document.getElementById('email').value;
    const code  = document.getElementById('authCode').value;
    fetch(`${AUTH_API.verifyEmail}?email=${email}&code=${code}`, { method: 'POST' })
            .then(response => {
              if (response.ok) {
                alert(AUTH_VALID_MSG.verifyOk);
                isEmailVerified = true;
                document.getElementById('email').readOnly = true;
              } else {
                alert(AUTH_VALID_MSG.verifyFail);
                isEmailVerified = false;
              }
            });
  }

  // ── 회원가입 제출 ──
  // ── 회원가입 제출 ──
  function doSignup() {
    const username        = document.getElementById('username').value.trim();
    const password        = document.getElementById('password').value;
    const passwordConfirm = document.getElementById('passwordConfirm').value;
    const email           = document.getElementById('email').value.trim();
    const name            = document.getElementById('name').value.trim();
    const birthDate       = document.getElementById('birthDate').value;
    const province        = document.querySelectorAll('.loc-big')[0].value;
    const city            = document.getElementById('city-select').value;
    const genderBtn       = document.querySelector('button[data-onclick*="gender"].on');
    const selectedGender  = genderBtn ? genderBtn.getAttribute('data-val') : '';
    const selectedMbti    = document.getElementById('mbti-result').innerText;

    // ── 유효성 검사 ──
    if (!username)                          { toast(AUTH_VALID_MSG.idEmpty.replace('먼저 ', '')); return; }
    if (!name)                              { toast(AUTH_VALID_MSG.nameEmpty); return; }
    if (!email)                             { toast(AUTH_VALID_MSG.emailEmptyForm); return; }
    if (!isEmailVerified)                   { toast(AUTH_VALID_MSG.emailNotVerify); return; }
    if (!password)                          { toast(AUTH_VALID_MSG.pwEmpty); return; }
    if (!PW_PATTERN.test(password))         { toast(PW_PATTERN_MSG); return; }
    if (password !== passwordConfirm)       { toast('비밀번호 재입력이 일치하지 않습니다.'); return; }
    if (!birthDate)                         { toast(AUTH_VALID_MSG.birthEmpty); return; }
    if (!province || province === '도/시 선택') { toast(AUTH_VALID_MSG.regionEmpty); return; }
    if (!selectedGender)                    { toast(AUTH_VALID_MSG.genderEmpty); return; }
    if (selectedMbti.length !== 4)          { toast(AUTH_VALID_MSG.mbtiIncomplete); return; }

    // ── 요청 데이터 구성 ──
    const region = province + (city && city !== '시/군/구 선택' ? ' ' + city : '');
    const requestData = { username, password, email, name, region, birthDate, gender: selectedGender, mbti: selectedMbti };

    // ── API 호출 ──
    fetch(AUTH_API.signup, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(requestData),
    }).then(async response => {
      if (response.ok) {
        document.getElementById('signupSuccessModal').style.display = 'flex';
      } else {
        const errData = await response.json().catch(() => null);
        const msg = errData?.errors?.[0]?.defaultMessage
                || AUTH_VALID_MSG.signupFail.split(': ')[1];
        toast('⚠️ 가입 실패: ' + msg);
      }
    });
  }

  // ── 비밀번호 재설정 모달 ──
  let _pwResetEmail = '';
  let _pwResetCode  = '';

  function openPwResetModal() {
    _pwResetEmail = '';
    _pwResetCode  = '';
    // 단계 초기화
    document.querySelectorAll('.pw-reset-step').forEach(el => el.classList.remove('active'));
    document.getElementById('pw-reset-step1').classList.add('active');
    // 입력 초기화
    ['pr-email','pr-code','pr-new','pr-confirm'].forEach(id => {
      const el = document.getElementById(id);
      if (el) el.value = '';
    });
    document.getElementById('pr-email-msg').textContent = '';
    document.getElementById('pr-code-msg').textContent  = '';
    document.getElementById('pr-pw-msg').textContent    = '';
    document.getElementById('pw-reset-modal').style.display = 'flex';
  }

  function closePwResetModal() {
    document.getElementById('pw-reset-modal').style.display = 'none';
  }

  function closePwChangeRecommendModal() {
    document.getElementById('pw-change-recommend-modal').style.display = 'none';
  }

  function snoozeAndClosePwChangeModal() {
    closePwChangeRecommendModal();
    if (typeof apiCall === 'function') {
      apiCall('/api/auth/pw-noti-later', { method: 'POST' });
    }
  }

  function goChangePw() {
    closePwChangeRecommendModal();
    if (typeof go === 'function') go('mypage');
    setTimeout(() => {
      if (typeof showMySection === 'function') showMySection('info', null);
    }, 300);
  }

  function prShowStep(n) {
    document.querySelectorAll('.pw-reset-step').forEach(el => el.classList.remove('active'));
    document.getElementById('pw-reset-step' + n).classList.add('active');
  }

  function prSetMsg(id, msg, ok) {
    const el = document.getElementById(id);
    el.textContent  = msg;
    el.style.color  = ok ? 'var(--sage-d, #4a7c59)' : 'var(--coral, #e05c5c)';
  }

  // 1단계: 이메일로 인증번호 발송
  function prSendCode() {
    const email = document.getElementById('pr-email').value.trim();
    if (!email) { prSetMsg('pr-email-msg', '이메일을 입력해주세요.', false); return; }
    prSetMsg('pr-email-msg', '발송 중입니다...', true);
    fetch(AUTH_API.pwResetRequest, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ email })
    }).then(async res => {
      if (res.ok) {
        _pwResetEmail = email;
        prSetMsg('pr-email-msg', '✅ 인증번호가 이메일로 발송되었습니다.', true);
        setTimeout(() => prShowStep(2), 800);
      } else {
        const msg = await res.text().catch(() => '');
        prSetMsg('pr-email-msg', '❌ ' + (msg || '등록되지 않은 이메일입니다.'), false);
      }
    }).catch(() => prSetMsg('pr-email-msg', '❌ 네트워크 오류가 발생했습니다.', false));
  }

  // 2단계: 인증번호 확인
  function prVerifyCode() {
    const code = document.getElementById('pr-code').value.trim();
    if (!code) { prSetMsg('pr-code-msg', '인증번호를 입력해주세요.', false); return; }
    prSetMsg('pr-code-msg', '확인 중...', true);
    fetch(`${AUTH_API.pwResetVerify}?email=${encodeURIComponent(_pwResetEmail)}&code=${code}`, {
      method: 'POST'
    }).then(res => {
      if (res.ok) {
        _pwResetCode = code;
        prSetMsg('pr-code-msg', '✅ 인증되었습니다.', true);
        setTimeout(() => prShowStep(3), 800);
      } else {
        prSetMsg('pr-code-msg', '❌ 인증번호가 틀렸거나 만료되었습니다.', false);
      }
    }).catch(() => prSetMsg('pr-code-msg', '❌ 네트워크 오류가 발생했습니다.', false));
  }

  // 3단계: 새 비밀번호 변경
  function prChangePassword() {
    const newPw     = document.getElementById('pr-new').value;
    const confirmPw = document.getElementById('pr-confirm').value;
    if (!newPw) { prSetMsg('pr-pw-msg', '비밀번호를 입력해주세요.', false); return; }
    if (!PW_PATTERN.test(newPw)) { prSetMsg('pr-pw-msg', PW_PATTERN_MSG, false); return; }
    if (newPw !== confirmPw)  { prSetMsg('pr-pw-msg', '비밀번호가 일치하지 않습니다.', false); return; }
    prSetMsg('pr-pw-msg', '변경 중...', true);
    fetch(AUTH_API.pwReset, {
      method: 'PATCH',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ email: _pwResetEmail, code: _pwResetCode, newPassword: newPw })
    }).then(async res => {
      if (res.ok) {
        prSetMsg('pr-pw-msg', '🎉 비밀번호가 변경되었습니다!', true);
        setTimeout(() => { closePwResetModal(); }, 1500);
      } else {
        const msg = await res.text().catch(() => '');
        prSetMsg('pr-pw-msg', '❌ ' + (msg || '변경에 실패했습니다. 다시 시도해주세요.'), false);
      }
    }).catch(() => prSetMsg('pr-pw-msg', '❌ 네트워크 오류가 발생했습니다.', false));
  }

  document.addEventListener('DOMContentLoaded', initAuthPage);
