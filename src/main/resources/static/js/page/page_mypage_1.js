/* page_mypage.html 에서 옮김 (CSP: 인라인 스크립트를 쓰지 않음) */
    /* ============================================================
     * 🚨 [임시 하드코딩 데이터]
     * 나중에 백엔드 API 및 실데이터 통신이 연결되면 이 블록을 통째로 지워주세요!
     * 지우는 순간, 아래의 실제 서비스/API 연동 로직으로 자동 전환됩니다.
     * ============================================================ */

    // 1. 프로필 정보
    const MOCK_MYPAGE_PROFILE = {
      avatar: "김",
      name: "김여행",
      email: "travel***@gmail.com"
    };

    // 2. 내 여행 기록 데이터
    const MOCK_MYPAGE_TRIPS = [
      { action: "go('map')", icon: "🗺️", title: "제주도 3박4일 힐링 여행", meta: "2025.06.14~06.17 · 2인 · 자차 · 비공개", budget: "₩404,000" },
      { action: "go('map')", icon: "🏙️", title: "부산 2박3일 가성비 투어", meta: "2025.04.22~04.24 · 4인 · 대중교통 · 공개", budget: "₩280,000" }
    ];

    // 3. 작성한 후기 데이터 → API 연동으로 대체
    // 4. 좋아요한 후기 데이터 → API 연동으로 대체

    // 5. 스크랩한 숙소 데이터
    const MOCK_MYPAGE_SCRAP_STAY = [
      { action: "go('place-hyeopjae')", icon: "🏖️", iconBg: "#E8F5F2", name: "협재 오션뷰 풀빌라", meta: "제주도 · 펜션", ratingColor: "#F5A623", ratingStar: "★★★★★", ratingNum: "4.9", tags: ["오션뷰", "풀빌라", "커플"], stats1: "📌 3,241번 담김", stats2: "★ 스크랩 412" },
      { action: "go('place-reviews')", icon: "🌊", iconBg: "#E8F5F2", name: "안목해변 오션뷰 펜션", meta: "강릉 · 펜션", ratingColor: "#F5A623", ratingStar: "★★★★★", ratingNum: "4.8", tags: ["해변", "힐링"], stats1: "📌 2,718번 담김", stats2: "★ 스크랩 287" }
    ];

    // 6. 스크랩한 맛집 데이터
    const MOCK_MYPAGE_SCRAP_FOOD = [
      { action: "go('place-teraroasa')", icon: "☕", iconBg: "#FEF3F2", name: "테라로사 강릉본점", meta: "강릉 · 로스터리 카페", ratingColor: "#F5A623", ratingStar: "★★★★★", ratingNum: "4.9", tags: ["커피", "힐링"], stats1: "📌 2,941번 담김", stats2: "★ 스크랩 318" },
      { action: "go('place-reviews')", icon: "🍖", iconBg: "#FEF3F2", name: "흑돼지 거리 맛집A", meta: "제주도 · 고깃집", ratingColor: "#F5A623", ratingStar: "★★★★☆", ratingNum: "4.7", tags: ["흑돼지", "제주"], stats1: "📌 2,512번 담김", stats2: "★ 스크랩 241" }
    ];

    // 7. 스크랩한 관광지 데이터
    const MOCK_MYPAGE_SCRAP_TOUR = [
      { action: "go('place-t-seongsan')", icon: "🌅", iconBg: "#EEF6FF", name: "성산일출봉", meta: "제주도 · 관광지", ratingColor: "#F5A623", ratingStar: "★★★★★", ratingNum: "4.9", tags: ["제주", "일출", "트레킹"], stats1: "📌 4,102번 담김", stats2: "★ 스크랩 521" },
      { action: "go('place-t-udo')", icon: "🏝️", iconBg: "#EEF6FF", name: "우도", meta: "제주도 · 관광지", ratingColor: "#F5A623", ratingStar: "★★★★★", ratingNum: "4.7", tags: ["제주", "섬", "힐링"], stats1: "📌 3,560번 담김", stats2: "★ 스크랩 448" }
    ];

    // 8. 스크랩한 카페 데이터
    const MOCK_MYPAGE_SCRAP_CAFE = [
      { action: "go('place-c-handam')", icon: "☕", iconBg: "#F5F0FF", name: "한담해안산책로 카페", meta: "제주도 · 카페", ratingColor: "#F5A623", ratingStar: "★★★★★", ratingNum: "4.7", tags: ["제주", "오션뷰", "힐링"], stats1: "📌 2,341번 담김", stats2: "★ 스크랩 312" },
      { action: "go('place-c-arario')", icon: "☕", iconBg: "#F5F0FF", name: "제주 로스터리 카페", meta: "제주도 · 카페", ratingColor: "#F5A623", ratingStar: "★★★★★", ratingNum: "4.8", tags: ["제주", "커피", "로스터리"], stats1: "📌 1,980번 담김", stats2: "★ 스크랩 267" }
    ];

    // 8. 가계부 데이터
    const MOCK_MYPAGE_LEDGER = [
      { id: "my-ledger-jeju", action: "selectMyLedger(this,'jeju')", class: "ts-card on", border: "var(--sage)", bg: "var(--sage-pale)", icon: "🗺️", title: "제주도 3박4일 힐링 여행", meta: "2025.06.14~06.17 · 제주 기준", budget: "₩404,000", budgetColor: "var(--sage)", mb: "10px" },
      { id: "my-ledger-busan", action: "selectMyLedger(this,'busan')", class: "ts-card", border: "var(--border)", bg: "var(--surface)", icon: "🏙️", title: "부산 2박3일 가성비 투어", meta: "2025.04.22~04.24 · 부산 기준", budget: "₩280,000", budgetColor: "var(--text2)", mb: "18px" }
    ];

    // 9. 설정 및 안내 문구 텍스트
    const MOCK_MYPAGE_TEXTS = {
      infoPwWarn: "🔒 보안을 위해 현재 비밀번호를 먼저 확인합니다.",
      withdrawWarn: "⚠️ 탈퇴 즉시 username·email 마스킹 / status → DELETED<br>REFRESH_TOKENS 즉시 삭제 · 작성 후기는 '탈퇴한 사용자'로 표시"
    };
    /* ============================================================ */


    /* ─── 마이페이지 데이터 렌더링 헬퍼 ─── */
    function initMyPageData() {
      // 1. 프로필 정보 렌더링
      let profile = typeof MOCK_MYPAGE_PROFILE !== 'undefined' ? MOCK_MYPAGE_PROFILE : null;
      if (profile) {
        const avatar = document.getElementById("myAvatar");
        const name = document.getElementById("myName");
        const email = document.getElementById("myEmail");
        if (avatar) avatar.textContent = profile.avatar;
        if (name) name.textContent = profile.name;
        if (email) email.textContent = profile.email;
      }

      // 2. 내 여행 기록 렌더링
      let trips = typeof MOCK_MYPAGE_TRIPS !== 'undefined' ? MOCK_MYPAGE_TRIPS : [];
      const tripsContainer = document.getElementById("my-trips-list");
      if (tripsContainer && trips.length > 0) {
        tripsContainer.innerHTML = trips.map(t => `
      <div class="trip-card" data-onclick="${t.action}">
        <div class="trip-thumb">${t.icon}</div>
        <div class="trip-info">
          <div class="trip-ttl">${t.title}</div>
          <div class="trip-meta">${t.meta}</div>
        </div>
        <div class="trip-budget">${t.budget}</div>
      </div>
    `).join('');
      }

      // 2-2. 🤝 초대받은 일정 렌더링 (내 여행 기록과 똑같은 디자인 적용)
      let invited = typeof MOCK_MYPAGE_INVITED !== 'undefined' ? MOCK_MYPAGE_INVITED : [];
      const invitedContainer = document.getElementById("my-invited-list");
      if (invitedContainer && invited.length > 0) {
        invitedContainer.innerHTML = invited.map(t => {
          // 백엔드에서 저장해 준 초대 url이 있으면 사용하고, 없으면 MOCK 액션 사용
          const targetAction = t.inviteUrl ? `_goToUrl('${t.inviteUrl}')` : t.action;
          const metaInfo = t.meta || "공유받은 편집자 일정";

          return `
        <div class="ts-card" data-onclick="${targetAction}" style="display:flex;align-items:center;gap:14px;padding:14px 16px;border-radius:var(--r);background:var(--surface);border:1px solid var(--border2);cursor:pointer;margin-bottom:12px;transition:all .2s;" data-onmouseover="this.style.borderColor='var(--sage-l)'; this.style.boxShadow='var(--sh)';" data-onmouseout="this.style.borderColor='var(--border2)'; this.style.boxShadow='none';">
          <div style="width:42px;height:42px;border-radius:10px;background:var(--sage);display:flex;align-items:center;justify-content:center;font-size:20px;color:#fff;flex-shrink:0;">🤝</div>
          <div style="flex:1">
            <div style="font-weight:800;font-size:14px;color:var(--text);margin-bottom:2px;">${t.title}</div>
            <div style="font-size:11px;color:var(--text3);">${metaInfo}</div>
          </div>
          <div style="text-align:right; line-height:1.4; min-width:80px; flex-shrink:0;">
              <span style="display:block; font-size:10px; color:var(--text3); font-weight:700; margin-bottom:2px;">링크 상태</span>
              <span style="color:var(--sage-d); font-weight:800; font-size:12px;">🔗 연결됨</span>
          </div>
        </div>`;
        }).join('');
      }

      // 3. 작성한 후기 렌더링 → app_main.js의 _renderMyReviews()가 처리
      // 4. 좋아요한 후기 렌더링 → app_main.js의 updateMyPageUI()가 처리

      // 5~8. 스크랩한 장소 렌더링 → app_community_v2.js의 loadMyScrap()이 처리
      // 9. 가계부 목록 렌더링
      let ledgers = typeof MOCK_MYPAGE_LEDGER !== 'undefined' ? MOCK_MYPAGE_LEDGER : [];
      const ledgerContainer = document.getElementById("my-ledger-list");
      if (ledgerContainer && ledgers.length > 0) {
        ledgerContainer.innerHTML = ledgers.map(l => `
      <div class="${l.class}" id="${l.id}" data-onclick="${l.action}"
        style="display:flex;align-items:center;gap:14px;padding:14px 16px;border-radius:var(--r);border:2px solid ${l.border};background:${l.bg};cursor:pointer;margin-bottom:${l.mb}">
        <div style="width:42px;height:42px;border-radius:10px;background:var(--sage);display:flex;align-items:center;justify-content:center;font-size:20px">${l.icon}</div>
        <div style="flex:1">
          <div style="font-weight:700;font-size:14px">${l.title}</div>
          <div style="font-size:11px;color:var(--text3);margin-top:2px">${l.meta}</div>
        </div>
        <div style="font-size:15px;font-weight:800;color:${l.budgetColor}">${l.budget}</div>
      </div>
    `).join('');
      }

      // 10. 설정 텍스트 렌더링
      let texts = typeof MOCK_MYPAGE_TEXTS !== 'undefined' ? MOCK_MYPAGE_TEXTS : null;
      if (texts) {
        const infoPwWarn = document.getElementById("info-pw-warn-txt");
        const withdrawWarn = document.getElementById("withdraw-warn-txt");
        if (infoPwWarn) infoPwWarn.innerHTML = texts.infoPwWarn;
        if (withdrawWarn) withdrawWarn.innerHTML = texts.withdrawWarn;
      }
    }

    document.addEventListener('DOMContentLoaded', initMyPageData);
    function toggleMyMenu() {
      const collapse = document.getElementById('myMenuCollapse');
      const toggle = document.getElementById('myMenuToggle');
      const isCollapsed = collapse.classList.toggle('collapsed');
      toggle.textContent = isCollapsed ? '메뉴 펼치기 ▼' : '메뉴 접기 ▲';
    }
  
