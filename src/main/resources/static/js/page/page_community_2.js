/* page_community.html 에서 옮김 (CSP: 인라인 스크립트를 쓰지 않음) */
  function renderReviewDetailPage(pageId) {
    return;

    const container = document.getElementById('page-' + pageId);
    if (!container) return;

    const starsHtml = '⭐'.repeat(data.rating || 4);
    const placesHtml = data.places ? `
      <div class="bcard" style="margin-bottom:18px;padding:16px">
        <div style="font-size:13px;font-weight:700;margin-bottom:10px">📍 방문한 장소</div>
        ${data.places.map(p => `
          <div style="display:flex;justify-content:space-between;align-items:center;padding:8px 0;border-bottom:1px solid var(--border2)">
            <div>
              <div style="font-size:13px;font-weight:700">${p.name}</div>
              <div style="font-size:11px;color:var(--text3)">${p.type}</div>
            </div>
            <div style="font-size:12px;color:var(--warm)">⭐ ${p.rating}</div>
          </div>
        `).join('')}
      </div>
    ` : (data.info ? `
      <div class="bcard" style="margin-bottom:18px;padding:16px">
        <div style="font-size:13px;font-weight:700;margin-bottom:10px">🏨 숙소 정보</div>
        ${Object.entries(data.info).map(([k,v]) => `
          <div style="display:flex;justify-content:space-between;font-size:12px;padding:4px 0;border-bottom:1px dashed var(--border2)">
            <span style="color:var(--text3)">${k === 'price' ? '가격' : k === 'location' ? '위치' : '편의시설'}</span>
            <span style="font-weight:600">${v}</span>
          </div>
        `).join('')}
      </div>
    ` : '');

    container.querySelector('.review-wrap').innerHTML = `
      <button class="review-back" data-onclick="go('community')">← 커뮤니티</button>
      <div style="margin-bottom:10px">
        <span class="post-cat cat-route" style="font-size:11px">${data.cat}</span>
      </div>
      <h1 class="review-h1">${data.title}</h1>
      <div class="review-meta-row" style="margin-bottom:12px">
        <span style="font-size:12px;color:var(--text3)">
          ${data.author} · ${data.date} · ❤️ ${data.likes} · 🔖 ${data.scraps} · 👁 ${data.views}
        </span>
      </div>
      ${data.rating ? `<div style="margin-bottom:14px;font-size:14px">${starsHtml} <span style="font-size:12px;color:var(--text3)">(${data.rating}.0)</span></div>` : ''}
      <div style="display:flex;gap:4px;flex-wrap:wrap;margin-bottom:18px">
        ${(data.tags || []).map(t => `<span style="font-size:11px;color:var(--sage-d);background:var(--sage-pale);padding:3px 8px;border-radius:10px">#${t}</span>`).join('')}
      </div>
      ${placesHtml}
      <div class="review-body">${data.body}</div>
      <div class="comment-box" style="margin-top:24px">
        <div style="font-size:13px;font-weight:700;margin-bottom:12px">댓글</div>
        <div style="padding:20px;text-align:center;color:var(--text3);font-size:12px;background:var(--cream2);border-radius:8px">
          댓글 기능은 백엔드 연동 후 활성화됩니다.
        </div>
        <div style="display:flex;gap:6px;margin-top:10px">
          <input class="form-input" style="flex:1;padding:8px;font-size:12px" placeholder="댓글을 입력하세요...">
          <button class="btn-f" style="padding:8px 14px;font-size:12px" data-onclick="toast('로그인 후 이용 가능합니다.')">등록</button>
        </div>
      </div>
    `;
  }

  /**
   * 장소 상세 페이지 렌더러 (place-t-*, place-c-*)
   */
  function renderPlaceDetailPage(pageId) {
    // const allPlaces = { ...MOCK_TOUR_PLACES, ...MOCK_CAFE_PLACES };
    // const data = allPlaces[pageId];
    // if (!data) return;
    return; // 👇 API 연동까지 기본 HTML 유지

    const container = document.getElementById('page-' + pageId);
    if (!container) return;

    container.querySelector('.place-review-wrap').innerHTML = `
      <button class="review-back" data-onclick="go('community')">← 커뮤니티</button>
      <div style="display:flex;align-items:center;gap:14px;margin-bottom:18px">
        <div style="font-size:36px">📍</div>
        <div>
          <h2 style="font-size:22px;font-weight:800;margin-bottom:4px">${data.name}</h2>
          <div style="font-size:12px;color:var(--text3)">${data.region} · ${data.type}</div>
          <div style="font-size:13px;margin-top:4px">⭐ <strong>${data.rating}</strong> <span style="color:var(--text3)">(리뷰 ${data.reviews.toLocaleString()}개)</span></div>
        </div>
      </div>
      <div style="display:flex;gap:4px;flex-wrap:wrap;margin-bottom:18px">
        ${(data.tags || []).map(t => `<span style="font-size:11px;color:var(--sage-d);background:var(--sage-pale);padding:3px 8px;border-radius:10px">#${t}</span>`).join('')}
      </div>
      <div class="bcard" style="margin-bottom:18px;padding:18px">
        <p style="font-size:13px;color:var(--text2);line-height:1.7;margin-bottom:14px">${data.desc}</p>
        <div style="border-top:1px dashed var(--border2);padding-top:12px">
          <div style="font-size:12px;font-weight:700;margin-bottom:8px;color:var(--text)">📋 방문 정보</div>
          ${Object.entries(data.info).map(([k,v]) => `
            <div style="display:flex;justify-content:space-between;font-size:12px;padding:5px 0;border-bottom:1px dashed var(--border2)">
              <span style="color:var(--text3)">${k === 'hours' ? '운영시간' : k === 'price' ? '입장료/가격' : '팁'}</span>
              <span style="font-weight:500;text-align:right;max-width:60%">${v}</span>
            </div>
          `).join('')}
        </div>
      </div>
      <div style="padding:20px;text-align:center;color:var(--text3);font-size:12px;background:var(--cream2);border-radius:8px">
        상세 리뷰 목록은 백엔드 연동 후 표시됩니다.
      </div>
    `;
  }

  // go() 함수가 review/place 페이지로 이동할 때 데이터를 주입하기 위한 훅
  // app_main.js의 go() 함수 실행 후 처리
  document.addEventListener('DOMContentLoaded', function() {
    // 페이지 전환 감지를 위해 go() 래핑
    const _origGo = window.go;
    if (_origGo) {
      window.go = function(id, addToHistory) {
        _origGo(id, addToHistory);
        // 후기 상세 페이지
        // (예시 데이터 MOCK_* 가 지워진 뒤에도 이 감시가 남아 화면을 옮길 때마다 ReferenceError 가 나던 것을 막음)
        const _has = (name) => typeof window[name] !== 'undefined' && window[name] && window[name][id];
        if (_has('MOCK_ROUTE_REVIEWS') || _has('MOCK_STAY_REVIEWS') || _has('MOCK_FOOD_REVIEWS')) {
          setTimeout(() => renderReviewDetailPage(id), 10);
        }
        // 장소 상세 페이지
        if (_has('MOCK_TOUR_PLACES') || _has('MOCK_CAFE_PLACES')) {
          setTimeout(() => renderPlaceDetailPage(id), 10);
        }
      };
    }
  });
