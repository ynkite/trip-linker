/* page_community.html 에서 옮김 (CSP: 인라인 스크립트를 쓰지 않음) */
    /* ─── 커뮤니티 UI 동적 데이터 주입 헬퍼 함수 ─── */
    function initCommunityPageData() {
      const tagContainer = document.getElementById("popular-tags-list");


      // 2. AI 취향 추천 렌더링
      const aiTitle = document.getElementById("ai-reco-title");
      const aiContainer = document.getElementById("ai-reco-list");
      if (typeof MOCK_AI_RECOMMENDATIONS !== 'undefined') {
        if (aiTitle) aiTitle.textContent = MOCK_AI_RECOMMENDATIONS.title;
        if (aiContainer && MOCK_AI_RECOMMENDATIONS.items) {
          aiContainer.innerHTML = MOCK_AI_RECOMMENDATIONS.items.map(item => `
        <div class="ai-reco-card" data-onclick="go(${item.url})">
          <div style="font-size:11px;color:var(--sage-d);font-weight:700;margin-bottom:2px">${item.category}</div>
          <div style="font-size:13px;font-weight:700;color:var(--text);margin-bottom:6px">${item.title}</div>
          <div style="font-size:11px;color:var(--text3)">취향 일치 ${item.matchCount}개</div>
        </div>
      `).join('');
        }
      }

      // 3. 카테고리 탭 게시글 목록 주입
      const posts = typeof MOCK_COMMUNITY_POSTS !== 'undefined' ? MOCK_COMMUNITY_POSTS : null;
      if (posts) {
        const tabsData = [
          { id: 'tab-route', key: 'route', label: '여행 경로', color: 'var(--warm)', catClass: 'cat-route', catStyle: '', borderClass: 'comm-tab-route' },
          { id: 'tab-stay', key: 'stay', label: '숙소', color: 'var(--sage)', catClass: 'cat-stay', catStyle: '', borderClass: 'comm-tab-stay' },
          { id: 'tab-food', key: 'food', label: '맛집', color: 'var(--coral)', catClass: 'cat-food', catStyle: '', borderClass: 'comm-tab-food' },
          { id: 'tab-tour', key: 'tour', label: '관광지', color: 'var(--sky)', catClass: 'cat-tour', catStyle: '', borderClass: 'comm-tab-tour' },
          { id: 'tab-cafe', key: 'cafe', label: '카페', color: '#A78BFA', catClass: '', catStyle: 'background:#F3F0FF;color:#7C3AED', borderClass: 'comm-tab-cafe' }
        ];

        tabsData.forEach(tab => {
          const container = document.getElementById(tab.id);
          const items = posts[tab.key] || [];
          if (container) {
            if (items.length > 0) {
              container.innerHTML = items.map(p => `
            <div class="post-card comm-post-item ${tab.borderClass}" data-onclick="go(${p.url})" data-tags="${p.tags}" data-likes="${p.likes}"
              data-scrap="${p.scrap}" data-date="${p.dateVal}" data-author="${p.author}">
              <div style="display:flex;align-items:center;justify-content:space-between;margin-bottom:5px">
                <span class="post-cat ${tab.catClass}" style="${tab.catStyle}">${tab.label}</span>
                <span style="font-size:10px;color:var(--text3)">${p.author} · ${p.dateStr}</span>
              </div>
              <div class="post-ttl">${p.title}</div>
              <div style="display:flex;gap:4px;margin-top:4px;flex-wrap:wrap">
                ${p.tagList.map(tag => `<span style="font-size:10px;color:var(--sage-d);background:var(--sage-pale);padding:2px 6px;border-radius:10px">#${tag}</span>`).join('')}
              </div>
              <div class="post-foot">
                <div class="post-stats">
                  <span class="post-stat">❤️ ${p.likes}</span>
                  <span class="post-stat">🔖 ${p.scrap}</span>
                  <span class="post-stat">👁 ${p.views}</span>
                </div>
              </div>
            </div>
          `).join('');
            } else {
              container.innerHTML = `<div style="padding:40px;text-align:center;color:var(--text3);font-size:13px;">등록된 게시글이 없습니다.</div>`;
            }
          }
        });
      }

      // 4. 시스템 알림 데이터 주입
      const notis = typeof MOCK_NOTIFICATIONS !== 'undefined' ? MOCK_NOTIFICATIONS : null;
      const notiListContainer = document.getElementById('notifList');
      if (notiListContainer && notis) {
        notiListContainer.innerHTML = notis.map(n => `
      <div style="padding:14px;border-bottom:1px solid var(--border2);font-size:12px;cursor:pointer" data-onclick="toast('알림 이동')">
        <div style="color:var(--text2);margin-bottom:6px;line-height:1.4">${n.text}</div>
        <div style="font-size:10px;color:var(--text3)">${n.time}</div>
      </div>
    `).join('') + `<div style="padding:12px;text-align:center;font-size:12px;color:var(--text3);cursor:pointer;background:var(--surface);border-radius:0 0 var(--r) var(--r)" data-onclick="toast('모든 알림 보기')">모든 알림 보기</div>`;
      }

      // 5. 장소리뷰 & 후기상세 기본 데이터 매핑
      const mc = typeof MOCK_MODAL_CONTENTS !== 'undefined' ? MOCK_MODAL_CONTENTS : null;
      if (mc) {
        // 장소 팝업 데이터 바인딩
        const mpPlace = document.getElementById('mpPlace'), mpType = document.getElementById('mpType'), mpReviews = document.getElementById('mpReviews'), mpLinks = document.getElementById('mpLinks');
        if (mpPlace) mpPlace.textContent = mc.mapPlace.title;
        if (mpType) mpType.textContent = mc.mapPlace.type;
        if (mpReviews) mpReviews.innerHTML = mc.mapPlace.reviews.map(r => `<div class="p-rev-item" style="margin-bottom:10px;padding-bottom:10px;border-bottom:1px dashed var(--border2)"><div style="display:flex;justify-content:space-between;font-size:12px;margin-bottom:4px"><span style="font-weight:700">${r.author}</span><span>${r.rating}</span></div><div style="font-size:12px;color:var(--text2)">${r.text}</div></div>`).join('');
        if (mpLinks) mpLinks.innerHTML = mc.mapPlace.links.map(l => `<button class="btn-o" style="flex:1;padding:8px;font-size:12px" data-onclick="toast('${l}로 이동')">${l}</button>`).join('');

        // 리뷰상세 팝업 데이터 바인딩
        const rdPlace = document.getElementById('rdPlace'), rdType = document.getElementById('rdType'), rdMeta = document.getElementById('rdMeta'), rdBody = document.getElementById('rdBody'), rdComments = document.getElementById('rdComments');
        if (rdPlace) rdPlace.textContent = mc.reviewDetail.title;
        if (rdType) rdType.textContent = mc.reviewDetail.type;
        if (rdMeta) rdMeta.textContent = mc.reviewDetail.meta;
        if (rdBody) rdBody.textContent = mc.reviewDetail.body;
        if (rdComments) rdComments.innerHTML = mc.reviewDetail.comments.map(c => `<div style="font-size:12px;margin-bottom:6px;padding:6px;background:var(--surface);border-radius:4px"><strong style="color:var(--text)">${c.author}:</strong> <span style="color:var(--text2)">${c.text}</span></div>`).join('');
      }

      // 6. 후기 작성 '플랜 연동' 셀렉트 박스 바인딩
      const planSelect = document.getElementById('writePlanSelect');
      const planOptions = typeof MOCK_PLAN_OPTIONS !== 'undefined' ? MOCK_PLAN_OPTIONS : null;
      if (planSelect && planOptions) {
        planSelect.innerHTML = '<option value="">여행 플랜을 선택하세요...</option>' +
                planOptions.map(opt => `<option value="${opt.value}">${opt.label}</option>`).join('');
      }
    }

    // ⭕ 인텔리제이 에러 방지 및 안전한 화면 초기화 코드 (실행 순서 완전 해결!)
    document.addEventListener("DOMContentLoaded", async () => {
      try {
        // 1. [가장 중요] 화면에 그릴 뼈대와 데이터를 가장 먼저 주입합니다.
        initCommunityPageData();

        // 2. 검색 함수만 세팅 (setCommTab/sortPosts 는 app_community.js + v2 의 정식 버전을 사용)
        //    ※ 예전 인라인 정의는 서버 정렬을 무시하고 장소 탭 정렬이 안 되는 문제가 있어 제거함
        window.doSearch = function() {
          var type = document.getElementById('searchType').value;
          var q = (document.getElementById('searchInp').value||'').trim().toLowerCase();
          if (!q) { toast('검색어를 입력해주세요'); return; }
          var found = 0;
          document.querySelectorAll('.comm-post-item').forEach(function(item) {
            var title  = (item.querySelector('.post-ttl')  ? item.querySelector('.post-ttl').textContent  : '').toLowerCase();
            var tags   = (item.getAttribute('data-tags')   || '').toLowerCase();
            var author = (item.getAttribute('data-author') || '').toLowerCase();
            var match  = (type==='title') ? title.includes(q) :
                    (type==='content') ? (title.includes(q)||tags.includes(q)) :
                            (type==='author')  ? author.includes(q) : tags.includes(q);
            item.style.display = match ? '' : 'none';
            if (match) found++;
          });
          toast('"'+q+'" 검색 결과: '+found+'건');
        };

        function _hookGo() {
          if (typeof window.go !== 'function') { setTimeout(_hookGo, 50); return; }
          var _orig = window.go;
          window.go = function(id, addToHistory) {
            _orig(id, addToHistory);
          };
        }
        _hookGo();



        // 4. [마지막 단계] 화면 렌더링이 완벽히 끝났으므로 안심하고 메인 자바스크립트 초기화를 실행!
        if (typeof initCommunityPage === 'function') {
          await initCommunityPage();
        }

      } catch (error) {
        console.error("커뮤니티 페이지 초기화 중 오류 발생:", error);
      }
    });
  
