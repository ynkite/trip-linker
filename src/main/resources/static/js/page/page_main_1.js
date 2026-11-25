/* page_main.html 에서 옮김 (CSP: 인라인 스크립트를 쓰지 않음) */
    /* ============================================================
     * ✅ [서비스 로직] — 이 블록은 그대로 유지하세요.
     * MOCK 데이터가 없으면 자동으로 api.get() 호출로 전환됩니다.
     * ============================================================ */
    const SECTION_META = {
      top3:      { rankColor: 'var(--warm)' },
      food:      { rankColor: 'var(--sage)' },
      stay:      { rankColor: 'var(--coral)' },
      tour:      { rankColor: 'var(--sky)' },
      cafe:      { rankColor: '#7C3AED' },
    };

    // ── 렌더링 헬퍼 (이슈 6a,6b 수정: 토글 스크랩 + 카테고리 매핑 수정) ──
    function _popCard(item, index, rankColor) {
      var rank = String(index + 1).padStart(2, '0');
      var rating = item.avgRating != null ? Number(item.avgRating).toFixed(1)
              : (item.rating != null ? Number(item.rating).toFixed(1) : '0.0');
      var ratingNum = parseFloat(rating);
      var stars = item.stars ||
              ('★'.repeat(Math.round(ratingNum)) + '☆'.repeat(5 - Math.round(ratingNum)));
      var reviewCnt = item.reviewCount != null ? item.reviewCount
              : (item.saveCount != null ? item.saveCount : 0);
      var tags = Array.isArray(item.tags) ? item.tags
              : (item.tags ? String(item.tags).split(',').filter(Boolean) : []);
      var placeId = item.placeId || item.id || '';

      /* API category: FOOD/STAY/TOUR/CAFE -> 소문자 통일 */
      var catLower = (item.category || '').toLowerCase();

      /* 카드 클릭: 해당 장소 후기로 이동 */
      var onClickAction = item.isMock
              ? 'go(' + item.url + ')'
              : 'goToPlaceReviews(' + placeId + ', \'' + _esc(item.name) + '\', \'' + catLower + '\', ' + ratingNum + ', ' + reviewCnt + ')';

      /* 스크랩 버튼 토글 */
      var scrapBtnId = 'pop-scrap-' + placeId + '-' + index;
      var onClickScrap = item.isMock
              ? '_applyMainScrapStyle(this,true);toast(\'🔖 스크랩했습니다.\')'
              : 'toggleMainPlaceScrap(event,' + placeId + ',\'' + catLower + '\',\'' + scrapBtnId + '\')';

      /* 이미 스크랩한 장소면 버튼을 켜진(초록 채움) 상태로 초기 렌더링 → 새로고침/재진입 시에도 유지 */
      var _alreadyScrapped = !item.isMock && placeId !== '' &&
              window._scrappedPlaceIds && window._scrappedPlaceIds.has(String(placeId));
      var scrapBtnCls   = 'pop-scrap-btn' + (_alreadyScrapped ? ' scrapped' : '');
      var scrapBtnStyle = _alreadyScrapped
              ? ' style="background:#46B29E;border-color:#46B29E;color:#fff"'
              : '';

      var tagHtml = tags.length
              ? '<div class="pop-tags">' + tags.map(function(t){ return '<span class="pop-tag">' + _esc(t) + '</span>'; }).join('') + '</div>'
              : '';

      return '<div class="pop-card" data-onclick="' + onClickAction + '">' +
              '<div class="pop-rank" style="background:' + rankColor + '">' + rank + '</div>' +
              '<div class="pop-icon-row"><span class="pop-icon">' + (item.icon || '📍') + '</span>' +
              '<div>' +
              '<div class="pop-name">' + _esc(item.name) + '</div>' +
              '<div class="pop-meta">' + _esc(item.address || item.roadAddress || '') + '</div>' +
              '</div>' +
              '</div>' +
              '<div class="pop-rating">' +
              '<span style="color:#F5A623">' + stars + '</span>' +
              '<span style="font-size:12px;font-weight:700;margin-left:3px">' + rating + '</span>' +
              '</div>' +
              tagHtml +
              '<div class="pop-foot">' +
              '<span class="pop-stats">' +
              '<span class="pop-saves">📌 ' + reviewCnt + '번 담김</span>' +
              '<span class="pop-scrap-n">★ ' + (item.scrapCount || 0) + '</span>' +
              '</span>' +
              '<button id="' + scrapBtnId + '" class="' + scrapBtnCls + '"' + scrapBtnStyle + ' ' +
              'data-onclick="event.stopPropagation(); ' + onClickScrap + '">★ 스크랩</button>' +
              '</div>' +
              '</div>';
    }

    function _esc(str) {
      return (str || '').replace(/&/g,'&amp;').replace(/</g,'&lt;').replace(/>/g,'&gt;');
    }

    // ── 메인페이지 인기 장소 카드 클릭 → 커뮤니티 해당 장소 후기로 직접 이동 (이슈 5) ──
    function goToPlaceReviews(placeId, name, category, avgRating, reviewCount) {
      /* API 반환: FOOD/STAY/TOUR/CAFE(대문자) 또는 food/stay/tour/cafe(소문자) 모두 처리 */
      const catLower = (category || '').toLowerCase();
      const catMap = {
        food: 'food', restaurant: 'food',
        stay: 'stay', accommodation: 'stay',
        tour: 'tour', attraction: 'tour',
        cafe: 'cafe'
      };
      const type = catMap[catLower] || 'tour';

      /* 메인 → 장소 후기 직행 플래그 (go()의 목록 자동 로드가 덮어쓰지 않게) */
      window._pendingPlaceReview = { placeId: placeId, name: name, type: type };

      /* 1) 커뮤니티 화면으로 즉시 전환 */
      go('community');

      /* 2) 목록 로드(setCommTab)를 거치지 않고, 탭 표시만 직접 전환 → 중간 화면 안 보임 */
      const showTab = function () {
        document.querySelectorAll('#commTabs .comm-tab').forEach(function (b) {
          const oc = b.getAttribute('data-onclick') || '';
          b.classList.toggle('on', oc.indexOf("'" + type + "'") !== -1);
        });
        ['route', 'stay', 'food', 'tour', 'cafe'].forEach(function (c) {
          const el = document.getElementById('tab-' + c);
          if (el) el.style.display = (c === type) ? 'block' : 'none';
        });
        if (typeof _commState !== 'undefined') { _commState.currentTab = type; _commState.currentPage = 0; }
        window._communityWriteCategory = ({ food: 'FOOD', stay: 'STAY', tour: 'TOUR', cafe: 'CAFE' })[type] || 'TOUR';
      };

      /* 3) 해당 장소 후기 패널을 그림 */
      const openReviews = function () {
        showTab();
        if (typeof window._openPlaceReviews === 'function') {
          window._openPlaceReviews(placeId, name, type,
                  (avgRating != null ? avgRating : null),
                  (reviewCount != null ? reviewCount : null));
        }
      };

      /* 즉시 1회 + go() 내부의 100ms 목록 로직 이후 1회 더 그려서 확실히 후기 화면 유지 */
      openReviews();
      setTimeout(openReviews, 150);
      setTimeout(function () { window._pendingPlaceReview = null; }, 700);
    }

    /* ── 메인페이지 장소 스크랩 토글 헬퍼 (이슈 6a) ── */
    /* 메인페이지 로그인 여부 — Token 기반(안정적) */
    /* 내가 스크랩한 placeId 집합 — 새로고침/재진입 시에도 버튼 상태 유지용 */
    window._scrappedPlaceIds = window._scrappedPlaceIds || new Set();
    /* 내가 스크랩한 경로 게시글 postId 집합 */
    window._scrappedPostIds = window._scrappedPostIds || new Set();
    async function _loadMyScrappedPlaceIds() {
      window._scrappedPlaceIds = new Set();
      window._scrappedPostIds = new Set();
      if (!_mainIsLoggedIn() || typeof api === 'undefined') return;
      try {
        const res = await api.get('/api/scraps');
        const list = (res && res.success !== false) ? (res.data || []) : [];
        list.forEach(function (s) {
          if (s && s.placeId != null) window._scrappedPlaceIds.add(String(s.placeId));
        });
      } catch (e) {}
      try {
        const resP = await api.get('/api/posts/scrapped');
        const listP = (resP && resP.success !== false) ? (resP.data || []) : [];
        listP.forEach(function (p) {
          if (p && p.postId != null) window._scrappedPostIds.add(String(p.postId));
        });
      } catch (e) {}
    }

    function _mainIsLoggedIn() {
      try {
        if (typeof Token !== 'undefined' && Token.getAccess && Token.getAccess()) return true;
      } catch (e) {}
      return !!(localStorage.getItem('accessToken') || localStorage.getItem('access_token'));
    }

    /* 스크랩 버튼 스타일 — 커뮤니티 상세 페이지와 동일(솔리드 그린 채움, 라벨은 '★ 스크랩' 유지) */
    var _MAIN_SCRAP_ACTIVE = '#46B29E';
    function _applyMainScrapStyle(btn, scrapped) {
      if (!btn) return;
      btn.textContent = '★ 스크랩';
      if (scrapped) {
        btn.classList.add('scrapped');
        btn.style.background  = _MAIN_SCRAP_ACTIVE;
        btn.style.borderColor = _MAIN_SCRAP_ACTIVE;
        btn.style.color       = '#fff';
      } else {
        btn.classList.remove('scrapped');
        btn.style.background  = '';
        btn.style.borderColor = '';
        btn.style.color       = '';
      }
    }

    /* 스크랩 카운트(★ 숫자) 실시간 갱신 — 버튼이 속한 카드 내부의 카운트 엘리먼트를 +1/-1 */
    function _updateScrapCount(btn, scrapped, selector) {
      if (!btn) return;
      const card = btn.closest('.pop-card') || btn.closest('.top3-card') || btn.parentElement;
      if (!card) return;
      const el = card.querySelector(selector);
      if (!el) return;
      const cur = parseInt((el.textContent.match(/\d+/) || [0])[0], 10) || 0;
      const next = Math.max(0, scrapped ? cur + 1 : cur - 1);
      el.textContent = '★ ' + next;
    }

    async function toggleMainPlaceScrap(e, placeId, category, btnId) {
      e.stopPropagation();
      if (!_mainIsLoggedIn()) { if (typeof toast === 'function') toast('로그인이 필요합니다'); return; }
      /* 외부 함수(doCommPlaceScrap)에 의존하지 않고 직접 토글 API 호출 → 로드 순서와 무관하게 동작 */
      const _catKey = (category || '').toLowerCase();
      const _catMap = { stay: 'STAY', food: 'FOOD', tour: 'TOUR', cafe: 'CAFE',
        accommodation: 'STAY', restaurant: 'FOOD' };
      const dbCat = _catMap[_catKey] || 'TOUR';
      let res;
      try {
        res = await api.post('/api/places/' + placeId + '/scraps', { category: dbCat });
      } catch (err) {
        if (typeof toast === 'function') toast('⚠️ 스크랩 처리에 실패했습니다.');
        return;
      }
      if (!res || res.success === false) { if (typeof toast === 'function') toast(res && res.message ? res.message : '⚠️ 스크랩 처리에 실패했습니다.'); return; }
      const scrapped = res.data === true;
      const btn = document.getElementById(btnId);
      if (btn) {
        _applyMainScrapStyle(btn, scrapped);
        _updateScrapCount(btn, scrapped, '.pop-scrap-n');
      }
      /* 집합 갱신 → 같은 장소가 다른 곳에서도 일관되게 표시 */
      if (window._scrappedPlaceIds) {
        if (scrapped) window._scrappedPlaceIds.add(String(placeId));
        else          window._scrappedPlaceIds.delete(String(placeId));
      }
      if (typeof toast === 'function') toast(scrapped ? '🔖 스크랩했습니다.' : '🔖 스크랩을 취소했습니다.');
    }

    /* ── 메인페이지 인기 경로 스크랩 토글 (이슈 3) ── */
    async function toggleRouteScrap(e, postId, btn) {
      e.stopPropagation();
      if (!_mainIsLoggedIn()) { if (typeof toast === 'function') toast('로그인이 필요합니다'); return; }
      const res = await api.post('/api/posts/' + postId + '/scraps?category=ROUTE', {});
      const isOk = res && res.success !== false;
      if (!isOk) { if (typeof toast === 'function') toast('⚠️ 스크랩 처리에 실패했습니다.'); return; }
      const scrapped = res.data === true;
      if (btn) {
        _applyMainScrapStyle(btn, scrapped);
        _updateScrapCount(btn, scrapped, '.card-scrap-n');
      }
      /* 집합 갱신 → 새로고침 없이도 상태 일관 유지 */
      if (window._scrappedPostIds) {
        if (scrapped) window._scrappedPostIds.add(String(postId));
        else          window._scrappedPostIds.delete(String(postId));
      }
      if (typeof toast === 'function') toast(scrapped ? '🔖 스크랩했습니다.' : '🔖 스크랩을 취소했습니다.');
    }

    // ── 메인 페이지 초기화 (하드코딩 Fallback 자동 지원) ──
    async function initMainPage() {
      // 2. 큐레이션 데이터 로드 (MOCK 배열이 있으면 쓰고, 삭제되었으면 api 통신)
      let curations = typeof MOCK_CURATIONS !== 'undefined' ? MOCK_CURATIONS : null;
      if (!curations && typeof api !== 'undefined') {
        const res = await api.get('/api/curations');
        curations = res.success && Array.isArray(res.data) ? res.data : [];
      }
      window._curationData = curations || [];

      const panelEl = document.querySelector('.curation-panel');
      if (panelEl && curations && curations.length) {
        const existingArrow = panelEl.querySelector('.slide-arr-wrap'), existingDots = panelEl.querySelector('.slide-dots');
        panelEl.querySelectorAll('.slide').forEach(s => s.remove());

        const frag = document.createDocumentFragment();
        const gradients = ['linear-gradient(135deg,#1E7A69,#4DB8A4)', 'linear-gradient(135deg,#8c4a10,#d4854a)', 'linear-gradient(135deg,#6a2880,#b070b0)'];

        curations.forEach((s, i) => {
          // extra_notes 파싱
          let en = {};
          try { en = JSON.parse(s.extra_notes || s.extraNotes || '{}'); } catch(e) {}

          // cardColor → 상단 그라데이션 대신 단색 배경으로 사용
          const cardColor = en.cardColor || null;
          const grad = cardColor
                  ? cardColor
                  : (s.gradient || gradients[i % gradients.length]);

          // tags → 칩 형태로 렌더
          const tags = en.tags || (s.theme ? s.theme.split(',') : []);
          const chips = tags.length
                  ? `<div class="cur-tags">${tags.map(t => _esc(String(t).trim())).join(' · ')}</div>`
                  : '';

          // 숙박일수 · 인원 · 이동수단 → subTitle
          const pref = en.preferences || {};
          const days = en.days || [];
          const nightCount = days.length > 1 ? `${days.length - 1}박${days.length}일` : (days.length === 1 ? '당일치기' : '');
          const companion = pref.companion || '';
          const transport = pref.transport || '';
          const subTitleParts = [nightCount, companion, transport ? transport + ' 추천' : ''].filter(Boolean);
          const subTitle = s.isMock ? s.subTitle : (subTitleParts.join(' · ') || _esc(s.subTitle || s.meta || ''));

          // 관광지 spots 목록
          const attractions = en.adminRecommendedAttractions || [];
          const spots = s.isMock ? s.spots
                  : (attractions.length ? attractions.slice(0,3).map(a=>_esc(a)).join(' · ') : _esc(s.destination || ''));

          // ── 카드 본문을 채울 추가 요약 정보 (숙소·맛집·예산) ──
          const accs = en.adminRecommendedAccommodations || [];
          const rests = en.adminRecommendedRestaurants || [];
          const allPlaces = (Array.isArray(days) ? days : []).reduce(
                  (acc, d) => acc.concat(Array.isArray(d.places) ? d.places : []), []);
          const totalBudget = allPlaces.reduce((sum, p) => sum + (Number(p.amount) || 0), 0);

          const infoRows = [];
          if (accs.length) {
            const stayTxt = accs.length === 1 ? _esc(accs[0]) : `${_esc(accs[0])} 외 ${accs.length - 1}곳`;
            infoRows.push({ icon: '🏨', label: '숙소', value: stayTxt });
          }
          if (rests.length) {
            const foodTxt = rests.length === 1 ? _esc(rests[0]) : `${_esc(rests[0])} 외 ${rests.length - 1}곳`;
            infoRows.push({ icon: '🍽️', label: '맛집', value: foodTxt });
          }
          if (totalBudget > 0) {
            infoRows.push({ icon: '💰', label: '예상 경비', value: '₩' + totalBudget.toLocaleString() + '~' });
          }
          const infoHtml = (!s.isMock && infoRows.length)
                  ? `<div style="margin-top:16px;display:flex;flex-direction:column;gap:9px">` +
                  infoRows.map(r => `
                      <div style="display:flex;align-items:center;gap:9px;font-size:12.5px">
                        <span style="width:26px;height:26px;flex-shrink:0;display:flex;align-items:center;justify-content:center;background:var(--sage-pale);border-radius:8px;font-size:13px" aria-hidden="true">${r.icon}</span>
                        <span style="color:var(--text3);font-weight:600;flex-shrink:0;width:60px;white-space:nowrap">${r.label}</span>
                        <span style="color:var(--text);font-weight:600;overflow:hidden;text-overflow:ellipsis;white-space:nowrap;min-width:0">${r.value}</span>
                      </div>`).join('') +
                  `</div>`
                  : '';

          // ⭐ 배지: starBadges 배열 우선 → 없으면 "⭐ 추천 큐레이션" 기본값
          const _starBadges = en.starBadges && en.starBadges.length ? en.starBadges : null;
          const badges = s.badgesHtml || (
                  _starBadges
                          ? `<span class="cur-badge">${_starBadges.map(t => _esc(t)).join(' · ')}</span>`
                          : `<span class="cur-badge">추천 큐레이션</span>`
          );
          const title = s.isMock ? s.title : _esc(s.title);
          const budget = s.budgetHtml || '';

          const onClickPrev = s.isMock ? `openPreview(${s.curationId})` : `openPreview('${s.curationId}')`;
          const onClickPlan = s.isMock ? `startPlanFromCard(${s.planData})` : `startPlanFromCuration(${s.curationId})`;

          const div = document.createElement('div');
          div.className = 'slide' + (i === 0 ? ' on' : ''); div.id = 'sl-' + i;
          div.innerHTML = `
        <div style="height:10px;background:${grad};flex-shrink:0"></div>
        <div style="flex:1;display:flex;flex-direction:column;padding:22px 26px 24px;min-height:0">
          <div style="display:flex;gap:5px;flex-wrap:wrap;margin-bottom:12px">${badges}</div>
          <div style="font-size:23px;font-weight:900;color:var(--text);line-height:1.2;margin-bottom:6px;word-break:keep-all">${title}</div>
          <div style="font-size:12.5px;color:var(--text3);margin-bottom:14px">${subTitle}</div>
          ${chips}
          <div style="display:flex;gap:6px;flex-wrap:wrap;margin-top:2px">
            <span style="font-size:12px;color:var(--text2);background:var(--cream);padding:5px 10px;border-radius:8px;line-height:1.4">${spots}</span>
            ${budget}
          </div>
          ${infoHtml}
          <div style="display:flex;gap:8px;margin-top:auto;padding-top:14px">
            <button type="button" class="btn-slide-prev" data-onclick="${onClickPrev}">미리보기</button>
            <button type="button" class="btn-slide-plan" data-onclick="${onClickPlan}">계획하기</button>
          </div>
        </div>`;
          frag.appendChild(div);
        });

        if (existingArrow) panelEl.insertBefore(frag, existingArrow); else panelEl.appendChild(frag);
        if (existingDots) existingDots.innerHTML = curations.map((_, i) => `<div class="dot${i===0?' on':''}" data-onclick="goSlide(${i})"></div>`).join('');

        // 🎯 [여기부터 신규 추가!] 4초마다 자연스럽게 다음 슬라이드로 넘어가는 자동 롤링 기능
        if (window._slideAutoTimer) clearInterval(window._slideAutoTimer);
        window._slideAutoTimer = setInterval(() => {
          // 현재 켜져있는('on' 클래스) 슬라이드가 몇 번째인지 찾기
          const slides = document.querySelectorAll('.curation-panel .slide');
          if (slides.length <= 1) return; // 1개 이하면 넘길 필요 없음

          let currentIdx = Array.from(slides).findIndex(s => s.classList.contains('on'));
          if (currentIdx === -1) currentIdx = 0;

          // 다음 인덱스 계산 (마지막이면 다시 0으로 돌아감)
          let nextIdx = (currentIdx + 1) % slides.length;

          // 화면 전환 함수 호출 (미리 구현된 goSlide 연동)
          if (typeof goSlide === 'function') {
            goSlide(nextIdx);
          }
        }, 4000); // 4000ms = 4초
      }

      // 3. 커뮤니티 인기 경로 TOP 3 로드 (MOCK 배열이 있으면 쓰고, 삭제되었으면 api 통신)
      // ★ 스크랩 상태 유지를 위해 카드 렌더링 전에 내 스크랩 목록을 먼저 로드
      await _loadMyScrappedPlaceIds();
      let top3Routes = typeof MOCK_TOP3 !== 'undefined' ? MOCK_TOP3 : null;
      if (!top3Routes && typeof api !== 'undefined') {
        // 관리자에게는 목록 API 가 숨김 글도 주므로(관리용) 메인 랭킹에서는 한 번 더 걸러 3개를 채운다
        const res = await api.get('/api/posts?sort=scrap&size=6&category=ROUTE');
        top3Routes = res.success ? (res.data.content || res.data || []) : [];
        top3Routes = top3Routes.filter(p => !p.hidden).slice(0, 3);
      }

      const top3Grid = document.querySelector('.top3-grid');
      if (top3Grid && top3Routes) {
        top3Grid.innerHTML = top3Routes.length ? top3Routes.map((r, i) => {
          const rank = String(i + 1).padStart(2, '0');
          const onClickCard = r.isMock ? `go(${r.url})` : `openPostDetail(${r.postId})`;
          const rawTitle = r.isMock ? r.title : _esc(r.title || '');
          const title = rawTitle.replace(' — ', '<br>— ').replace(' - ', '<br>- ');

          const sub = r.isMock ? r.sub : `${_esc(r.writerName || r.author?.name || '익명')} · 조회 ${r.views ?? r.viewCount ?? 0}`;

          const onClickPreview = r.isMock ? `openPreview(${r.pvbKey})` : `openPostDetail(${r.postId})`;
          const pvbLabel = r.isMock ? r.pvbLabel : '후기 미리보기';
          const pvbText = r.isMock ? r.pvbText : _esc((r.content || '').replace(/[\r\n]+/g, ' ').trim());
          /* 스크랩 토글: mock은 인라인, API는 toggleRouteScrap 사용 */
          const onClickScrap = r.isMock
                  ? `_applyMainScrapStyle(this,true);toast('🔖 스크랩했습니다.')`
                  : `toggleRouteScrap(event, ${r.postId}, this)`;

          /* 이미 스크랩한 경로면 버튼을 켜진(초록 채움) 상태로 초기 렌더링 */
          const _routeScrapped = !r.isMock && r.postId != null &&
                  window._scrappedPostIds && window._scrappedPostIds.has(String(r.postId));
          const scrapBtnCls   = 'btn-scrap' + (_routeScrapped ? ' scrapped' : '');
          const scrapBtnStyle = _routeScrapped
                  ? ' style="background:#46B29E;border-color:#46B29E;color:#fff"'
                  : '';

          return `
        <div class="top3-card" data-onclick="${onClickCard}">
          <div class="card-num" style="background:${SECTION_META.top3.rankColor}">${rank}</div>
          <div class="card-ttl">${title.replace(' — ', '<br>— ')}</div>
          <div class="card-sub">${sub}</div>
          <div class="preview-box" data-onclick="event.stopPropagation(); ${onClickPreview}">
            <div class="pvb-label">${pvbLabel}</div>
            <div class="pvb-text">${pvbText}</div>
          </div>
          <div class="card-foot">
            <div class="card-stats">
              <span class="card-stat">❤️ ${r.likes || r.likeCount || 0}</span>
              <span class="card-stat">👁 ${r.views || r.viewCount || 0}</span>
              <span class="card-scrap-n">★ ${r.scraps || r.scrapCount || 0}</span>
            </div>
            <button type="button" class="${scrapBtnCls}"${scrapBtnStyle} data-onclick="event.stopPropagation(); ${onClickScrap}">★ 스크랩</button>
          </div>
        </div>`;
        }).join('') : '<div class="no-data">인기 경로 데이터가 없습니다.</div>';
      }

      // 4. 인기 명소 TOP 3 로드 (MOCK 배열이 있으면 쓰고, 삭제되었으면 api 통신)
      const categories = [
        { id: 'popular-food-grid', code: 'FOOD', color: SECTION_META.food.rankColor },
        { id: 'popular-stay-grid', code: 'STAY', color: SECTION_META.stay.rankColor },
        { id: 'popular-tour-grid', code: 'TOUR', color: SECTION_META.tour.rankColor },
        { id: 'popular-cafe-grid', code: 'CAFE', color: SECTION_META.cafe.rankColor }
      ];

      for (const cat of categories) {
        const gridEl = document.getElementById(cat.id);
        if (!gridEl) continue;

        let places = null;
        if (typeof MOCK_FOOD !== 'undefined') {
          if (cat.code === 'FOOD') places = MOCK_FOOD;
          if (cat.code === 'STAY') places = MOCK_STAY;
          if (cat.code === 'TOUR') places = MOCK_TOUR;
          if (cat.code === 'CAFE') places = MOCK_CAFE;
        }

        if (!places && typeof api !== 'undefined') {
          const resPlace = await api.get(`/api/places/popular?category=${cat.code}&size=3`);
          places = resPlace.success ? (resPlace.data || []) : [];
        }

        if (places) {
          gridEl.innerHTML = places.length
                  ? places.map((p, idx) => _popCard(p, idx, cat.color)).join('')
                  : '<div class="no-data" style="grid-column:1/-1;text-align:center;padding:20px;color:var(--text3)">인기 명소가 없습니다.</div>';
        }

      }
    }



    /* ════════════════════════════════════════════════════════════
     * 모바일 캐러셀 (인기 경로/맛집/숙소/관광지/카페)
     *   - 큐레이션과 동일: 좌우 화살표(겹침) + 하단 점
     *   - 동작: 스와이프(scroll-snap) + 화살표·점 클릭 시 해당 카드로 스크롤
     *   - 모바일(≤768px)에서만 활성, 카드가 렌더된 뒤 자동 구성
     * ════════════════════════════════════════════════════════════ */
    var _mobCarouselMQ = window.matchMedia('(max-width: 1024px)');

    function _buildOneCarousel(grid) {
      if (!grid) return;
      var cards = grid.children;
      // .no-data 한 장만 있는 경우는 캐러셀 불필요
      var onlyNoData = (cards.length === 1 && cards[0].className && cards[0].className.indexOf('no-data') !== -1);

      // 이미 구성된 경우: 카드 개수가 바뀌었으면 점/화살표를 다시 만든다(탭 전환·비동기 재렌더 대응)
      if (grid.dataset.carousel === '1') {
        var existingWrap = grid.parentNode;
        if (existingWrap && existingWrap.classList.contains('mob-carousel-wrap')) {
          var existingDots = existingWrap.querySelector('.mob-carousel-dots');
          var dotCount = existingDots ? existingDots.children.length : -1;
          if (!onlyNoData && cards.length > 0 && dotCount !== cards.length) {
            // 개수가 달라졌으니 래퍼를 해제하고 처음부터 다시 구성
            grid.dataset.carousel = '';
            var arrs = existingWrap.querySelectorAll('.mob-carousel-arr');
            arrs.forEach(function (a) { a.remove(); });
            if (existingDots) existingDots.remove();
            existingWrap.parentNode.insertBefore(grid, existingWrap);
            existingWrap.remove();
          } else {
            return; // 동일하면 그대로 둠
          }
        } else {
          return;
        }
      }

      if (!cards || cards.length === 0) return;
      if (onlyNoData) return;

      grid.dataset.carousel = '1';

      // 래퍼로 감싸기
      var wrap = document.createElement('div');
      wrap.className = 'mob-carousel-wrap';
      grid.parentNode.insertBefore(wrap, grid);
      wrap.appendChild(grid);

      // 화살표
      var prev = document.createElement('button');
      prev.className = 'mob-carousel-arr prev';
      prev.type = 'button';
      prev.innerHTML = '&#8249;';
      var next = document.createElement('button');
      next.className = 'mob-carousel-arr next';
      next.type = 'button';
      next.innerHTML = '&#8250;';
      wrap.appendChild(prev);
      wrap.appendChild(next);

      // 점
      var dotsWrap = document.createElement('div');
      dotsWrap.className = 'mob-carousel-dots';
      var n = cards.length;
      for (var i = 0; i < n; i++) {
        var d = document.createElement('div');
        d.className = 'dot' + (i === 0 ? ' on' : '');
        (function (idx) {
          d.addEventListener('click', function () { _scrollToCard(grid, idx); });
        })(i);
        dotsWrap.appendChild(d);
      }
      wrap.appendChild(dotsWrap);

      function _currentIndex() {
        // 가장 가까운 카드 인덱스 계산 (카드 중심과 뷰포트 중심 비교)
        var center = grid.scrollLeft + grid.clientWidth / 2;
        var best = 0, bestDist = Infinity;
        for (var k = 0; k < grid.children.length; k++) {
          var c = grid.children[k];
          var cc = c.offsetLeft + c.offsetWidth / 2;
          var dist = Math.abs(cc - center);
          if (dist < bestDist) { bestDist = dist; best = k; }
        }
        return best;
      }

      function _sync() {
        var idx = _currentIndex();
        var dots = dotsWrap.children;
        for (var k = 0; k < dots.length; k++) dots[k].classList.toggle('on', k === idx);
        prev.disabled = (idx === 0);
        next.disabled = (idx === grid.children.length - 1);
      }

      // 스크롤 시 점/화살표 상태 동기화 (rAF 스로틀)
      var ticking = false;
      grid.addEventListener('scroll', function () {
        if (ticking) return;
        ticking = true;
        requestAnimationFrame(function () { _sync(); ticking = false; });
      });

      prev.addEventListener('click', function () { _scrollToCard(grid, _currentIndex() - 1); });
      next.addEventListener('click', function () { _scrollToCard(grid, _currentIndex() + 1); });

      _sync();
    }

    // 🧹 데스크톱(>768px)로 커지면 캐러셀 래퍼/점/화살표를 모두 해제해 원래 그리드로 복귀
    function _teardownOneCarousel(grid) {
      if (!grid) return;
      var wrap = grid.parentNode;
      if (!wrap || !wrap.classList || !wrap.classList.contains('mob-carousel-wrap')) {
        grid.dataset.carousel = '';
        return;
      }
      var arrs = wrap.querySelectorAll('.mob-carousel-arr');
      arrs.forEach(function (a) { a.remove(); });
      var dots = wrap.querySelector('.mob-carousel-dots');
      if (dots) dots.remove();
      // 그리드를 래퍼 밖(원래 위치)으로 되돌리고 래퍼 제거
      wrap.parentNode.insertBefore(grid, wrap);
      wrap.remove();
      grid.dataset.carousel = '';
      grid.scrollLeft = 0;
    }

    function _teardownCarousels() {
      var grids = document.querySelectorAll('.top3-grid, .popular-grid');
      grids.forEach(function (g) { _teardownOneCarousel(g); });
    }

    function _scrollToCard(grid, idx) {
      if (idx < 0 || idx >= grid.children.length) return;
      var card = grid.children[idx];
      // 카드를 트랙 가운데로 스크롤
      var target = card.offsetLeft - (grid.clientWidth - card.offsetWidth) / 2;
      grid.scrollTo({ left: target, behavior: 'smooth' });
    }

    function initMobCarousels() {
      if (!_mobCarouselMQ.matches) return;  // 모바일에서만
      var grids = document.querySelectorAll('.top3-grid, .popular-grid');
      grids.forEach(function (g) { _buildOneCarousel(g); });
    }

    // 카드가 비동기로 채워지므로, 각 그리드에 자식이 들어오면 자동 구성
    function _watchCarouselGrids() {
      if (!_mobCarouselMQ.matches) return;
      var grids = document.querySelectorAll('.top3-grid, .popular-grid');
      grids.forEach(function (g) {
        if (g.dataset.carouselWatch === '1') return;
        g.dataset.carouselWatch = '1';
        var mo = new MutationObserver(function () {
          if (_mobCarouselMQ.matches && g.children.length > 0) { _buildOneCarousel(g); }
        });
        mo.observe(g, { childList: true });
        if (g.children.length > 0) _buildOneCarousel(g);
      });
    }

    document.addEventListener('DOMContentLoaded', function () {
      // 카드 렌더 타이밍을 모두 커버: 감시 시작 + 약간의 지연 후 일괄 시도
      _watchCarouselGrids();
      setTimeout(initMobCarousels, 400);
      setTimeout(initMobCarousels, 1200);
      setTimeout(initMobCarousels, 2500);
    });

    // 📱 화면 크기가 모바일/태블릿(≤768px)로 들어올 때도 캐러셀을 구성한다.
    //    (데스크톱에서 열었다가 창을 줄이거나, 기기 회전 시 화살표·점이 안 나오던 문제 방지)
    function _onMobMQChange() {
      if (_mobCarouselMQ.matches) {
        // 모바일/태블릿: 캐러셀 구성 (이미 있으면 카드수 변화에 맞춰 재구성됨)
        _watchCarouselGrids();
        initMobCarousels();
        setTimeout(initMobCarousels, 300);
      } else {
        // 데스크톱: 캐러셀 잔재(점·화살표·래퍼) 완전 제거
        _teardownCarousels();
      }
    }
    if (_mobCarouselMQ.addEventListener) {
      _mobCarouselMQ.addEventListener('change', _onMobMQChange);
    } else if (_mobCarouselMQ.addListener) {
      _mobCarouselMQ.addListener(_onMobMQChange);
    }
    window.addEventListener('resize', function () {
      clearTimeout(window._mobCarouselResizeT);
      window._mobCarouselResizeT = setTimeout(_onMobMQChange, 200);
    });

    document.addEventListener('DOMContentLoaded', initMainPage);


  
