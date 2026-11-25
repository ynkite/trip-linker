/* page_place.html 에서 옮김 (CSP: 인라인 스크립트를 쓰지 않음) */
    /* ============================================================
     * page_place.html — API 연동 스크립트
     *
     * 공통 유틸(apiCall, api.get/post/patch, Token, toast)은
     * app_main.js에 정의되어 있습니다.
     * ============================================================ */

    /* ────────────────────────────────────────────────
     * 1. 후기 상세   GET /api/posts/{postId}
     *    window._currentPostId 를 세팅한 뒤 go('review') 호출
     * ──────────────────────────────────────────────── */
    async function loadReviewDetail(postId) {
        window._currentPostId = postId;

        const res = await apiCall(`/api/posts/${postId}`);
        if (!res.success) { toast('후기를 불러올 수 없습니다.'); return; }
        const d = res.data;

        // 카테고리 / 태그
        _setText('pr-cat', d.category || '');
        _setText('pr-tag', d.tag || '');

        // 제목
        _setText('pr-title', d.title || '');

        // 작성자
        const avEl = document.getElementById('pr-author-av');
        if (avEl) avEl.textContent = (d.authorName || '?')[0];
        // 관리자 배지 포함 작성자명 렌더링
        const _adminBadgeHtml = (role) =>
            role === 'ADMIN'
                ? ' <span style="font-size:10px;font-weight:800;background:#0d9488;color:#fff;border-radius:4px;padding:2px 9px;letter-spacing:.4px;vertical-align:middle">관리자</span>'
                : '';
        const _authorEl = document.getElementById('pr-author-name');
        if (_authorEl) {
            _authorEl.innerHTML = escapeHtml(d.authorName || '') + _adminBadgeHtml(d.writerRole);
        }
        _setHTML('pr-meta',
            `<span>${d.createdAt||''}</span>
     <span>👁 ${d.viewCount||0}</span>
     <span>❤️ ${d.likeCount||0}</span>`);

        // 수정 버튼 — 내 글일 때만
        const editBtn = document.getElementById('btn-review-edit');
        if (editBtn) editBtn.style.display = d.isMine ? '' : 'none';

        // 연결 플랜 배지
        if (d.connectedTrip) {
            _setHTML('pr-plan-badge', `
      <div style="font-size:12px;font-weight:700;color:var(--sage-d)">📋 연결된 플랜</div>
      <div style="font-size:13px;color:var(--text2);flex:1">${d.connectedTrip.summary||''}</div>
      <button class="btn-f" style="padding:6px 12px;font-size:11px"
              data-onclick="openPreview('${d.connectedTrip.tripId}')">미리보기</button>`);
        }

        // 방문 장소별 별점 & 한줄평   GET /api/posts/{postId}/place-reviews
        // (한 장소에 여러 리뷰가 있어도 대표 1건만 표시, 클릭 시 해당 장소의 전체 리뷰 탭으로 이동)
        const placeListEl = document.getElementById('pr-place-list');
        try {
            const prRes = await apiCall(`/api/posts/${postId}/place-reviews`);
            const allPlaceReviews = (prRes.success && Array.isArray(prRes.data)) ? prRes.data : [];

            // 게시글 작성자 본인이 직접 쓴 리뷰만 표시 (다른 사용자 리뷰 완전 제외)
            // d.userId = 현재 게시글 작성자 ID (PostDetailResponseDto.userId)
            const postAuthorId = d.userId;
            window._currentPostDetail = d;  // 다른 컴포넌트에서도 사용 가능하도록 저장

            const authorOnlyReviews = allPlaceReviews.filter(
                p => postAuthorId && Number(p.writerId) === Number(postAuthorId)
            );

            // 같은 장소에 여러 번 방문한 경우 최신 1건만 (placeId 기준 중복 제거)
            const seenPlaceIds = new Set();
            const repReviews = [];
            authorOnlyReviews.forEach(p => {
                if (!seenPlaceIds.has(p.placeId)) {
                    seenPlaceIds.add(p.placeId);
                    repReviews.push(p);
                }
            });

            if (repReviews.length && placeListEl) {
                placeListEl.style.display = '';
                _setHTML('pr-place-list',
                    `<div class="bcard-ttl">📍 방문 장소별 별점 &amp; 한줄평</div>` +
                    repReviews.map(p => `
        <div class="place-review-card" style="cursor:pointer"
             data-onclick="goToPlaceReviews(${p.placeId})">
          <div class="prc-icon" style="background:var(--sage-pale)">
            ${_placeCatIcon(p.category)}
          </div>
          <div style="flex:1">
            <div class="prc-name">${p.placeName||''}</div>
            <div class="prc-type">${_placeCatLabel(p.category)}</div>
          </div>
          <div style="display:flex;align-items:center">
            <span class="prc-stars">${p.starsHtml||''}</span>
            <span class="prc-rating">${p.rating||''}</span>
          </div>
          <div class="prc-comment">"${p.comment||''}"</div>
        </div>`).join(''));
            } else if (placeListEl) {
                placeListEl.style.display = 'none';
            }
        } catch (e) {
            console.error('[review] 장소별 리뷰 로드 실패', e);
            if (placeListEl) placeListEl.style.display = 'none';
        }

        // 본문
        _setHTML('pr-body', d.content || '');

        // CTA
        _setText('pr-cta-sub',
            `${d.destination||''} 여행을 내 일정으로 만들어보세요.`);

        // 댓글
        await loadComments(postId);
    }

    /* ────────────────────────────────────────────────
     * 2. 댓글 목록   GET /api/posts/{postId}/comments
     * ──────────────────────────────────────────────── */
    async function loadComments(postId) {
        const area = document.getElementById('pr-comments');
        if (!area) return;

        const res = await apiCall(`/api/posts/${postId}/comments`);
        const list = (res.success && Array.isArray(res.data)) ? res.data : [];

        area.innerHTML = `
    <div class="comment-ttl">
      💬 댓글 <span style="color:var(--sage)">${list.length}</span>
    </div>
    <div style="display:flex;gap:8px;margin-bottom:14px">
      <div class="comm-av" style="flex-shrink:0">나</div>
      <div style="flex:1">
        <input class="search-inp" type="text" id="commentInput"
               placeholder="댓글을 입력하세요..."
               data-onkeydown="if(event.key==='Enter')doCommentWrite()"
               style="margin-bottom:6px;width:100%">
        <button class="btn-f" style="padding:6px 13px;font-size:11px"
                data-onclick="doCommentWrite()">등록</button>
      </div>
    </div>` +
            (() => {
                const _myId = window._currentUser ? (window._currentUser.id || window._currentUser.userId) : null;
                return list.filter(c => {
                    // 숨김 댓글: 관리자 또는 댓글 작성자 본인만 볼 수 있음
                    if (c.status === 'HIDDEN') {
                        const _adm = (window._checkIsAdmin ? window._checkIsAdmin() : false) || (window._isAdmin === true) || !!(window._currentUser && window._currentUser.role === 'ADMIN');
                        return _adm || (_myId && Number(c.userId) === Number(_myId));
                    }
                    return c.status !== 'DELETED';
                }).map(c => {
                    const isHidden = c.status === 'HIDDEN';
                    if (isHidden) return `
      <div style="display:flex;gap:8px;margin-bottom:11px">
        <div class="comm-av" style="opacity:0.5;background:#FFEBEE;color:#E53935">${(c.writerName||c.authorName||'?')[0]}</div>
        <div class="comm-bubble" style="background:#FFF3F3;border-color:#FFCDD2;flex:1">
          <div class="comm-hd">
            <span class="comm-author" style="color:#9E9E9E">${c.writerName||c.authorName||''}${c.writerRole==='ADMIN' ? ' <span style="font-size:10px;font-weight:800;background:#0d9488;color:#fff;border-radius:4px;padding:2px 9px;letter-spacing:.4px;vertical-align:middle">관리자</span>' : ''}</span>
            <span class="comm-date">${c.createdAt||''}</span>
            <span style="font-size:10px;color:#E53935;background:#FFEBEE;border:1px solid #FFCDD2;border-radius:4px;padding:1px 6px">🚫 숨김 처리된 댓글</span>
          </div>
          <div class="comm-text" style="color:#9E9E9E">${c.content||''}</div>
        </div>
      </div>`;
                    return `
      <div style="display:flex;gap:8px;margin-bottom:11px">
        <div class="comm-av">${(c.writerName||c.authorName||'?')[0]}</div>
        <div class="comm-bubble">
          <div class="comm-hd">
            <span class="comm-author">${c.writerName||c.authorName||''}${c.writerRole==='ADMIN' ? ' <span style="font-size:10px;font-weight:800;background:#0d9488;color:#fff;border-radius:4px;padding:2px 9px;letter-spacing:.4px;vertical-align:middle">관리자</span>' : ''}</span>
            <span class="comm-date">${c.createdAt||''}</span>
          </div>
          <div class="comm-text">${c.content||''}</div>
        </div>
      </div>`;
                }).join('');
            })();
    }

    /* ────────────────────────────────────────────────
     * 3. 댓글 작성   POST /api/posts/{postId}/comments
     * ──────────────────────────────────────────────── */
    async function doCommentWrite() {
        const postId = window._currentPostId;
        const input  = document.getElementById('commentInput');
        if (!input?.value.trim()) { toast('댓글을 입력해주세요.'); return; }
        if (!Token.getAccess())   { toast('로그인이 필요합니다.'); go('login'); return; }

        const res = await api.post(`/api/posts/${postId}/comments`,
            { content: input.value.trim() });
        if (res.success) {
            input.value = '';
            toast('댓글이 등록되었습니다.');
            await loadComments(postId);
        } else {
            toast(res.message || '댓글 등록에 실패했습니다.');
        }
    }

    /* ────────────────────────────────────────────────
     * 4. 좋아요   POST /api/posts/{postId}/likes
     * ──────────────────────────────────────────────── */
    async function doReviewLike() {
        const postId = window._currentPostId;
        if (!Token.getAccess()) { toast('로그인이 필요합니다.'); go('login'); return; }
        const res = await api.post(`/api/posts/${postId}/likes`, {});
        toast(res.success ? '❤️ 좋아요!' : (res.message || '좋아요 처리 실패'));
    }

    /* ────────────────────────────────────────────────
     * 5. 스크랩   POST /api/posts/{postId}/scraps
     * ──────────────────────────────────────────────── */
    async function doReviewScrap() {
        const postId = window._currentPostId;
        if (!Token.getAccess()) { toast('로그인이 필요합니다.'); go('login'); return; }
        const res = await api.post(`/api/posts/${postId}/scraps`, {});
        toast(res.success ? '★ 스크랩 완료!' : (res.message || '스크랩 처리 실패'));
    }

    /* ────────────────────────────────────────────────
     * 6. 신고   POST /api/posts/{postId}/reports
     * ──────────────────────────────────────────────── */
    async function doReviewReport() {
        if (!confirm('이 게시글을 신고하시겠습니까?')) return;
        const postId = window._currentPostId;
        if (!Token.getAccess()) { toast('로그인이 필요합니다.'); go('login'); return; }
        const res = await api.post(`/api/posts/${postId}/reports`,
            { reason: '부적절한 게시글' });
        toast(res.success ? '신고가 접수되었습니다.' : (res.message || '신고 처리 실패'));
    }

    /* ────────────────────────────────────────────────
     * 7. 후기 수정 페이지 진입
     * ──────────────────────────────────────────────── */
    async function goEditReview() {
        go('edit-review');

        const post = window._currentPostDetail;
        if (!post) { toast('후기 정보를 찾을 수 없습니다.'); return; }

        // 제목
        const titleEl = document.getElementById('editReviewTitle');
        if (titleEl) titleEl.value = post.title || '';

        // 본문 (raw content)
        const bodyEl = document.getElementById('editBlogEditor');
        if (bodyEl) bodyEl.innerHTML = post.content || '';

        // 공개/비공개 버튼 상태
        document.querySelectorAll('#page-edit-review .vis-chip').forEach(btn => {
            const wantPublic = !btn.textContent.includes('비공개');
            btn.classList.toggle('on', wantPublic === post.isPublic);
        });

        // 장소 리뷰 로드
        const postId    = window._currentPostId;
        const container = document.getElementById('edit-places-container');
        if (!container || !postId) return;

        container.innerHTML = '';

        try {
            const res     = await api.get(`/api/posts/${postId}/place-reviews`);
            const reviews = (res && Array.isArray(res.data)) ? res.data
                : Array.isArray(res) ? res : [];

            if (!reviews.length) return;

            container.innerHTML = `
                    <label class="form-label" style="display:block;margin-bottom:10px">
                        📍 방문 장소별 별점 &amp; 한줄평
                    </label>
                    <div id="edit-place-list">
                        ${reviews.map(r => `
                        <div class="edit-plr-row" data-review-id="${r.id}">
                            <div class="edit-plr-name">
                                <span class="edit-plr-cat">${r.category || ''}</span>
                                ${esc(r.placeName || '')}
                            </div>
                            <div class="star-sel" data-rating="${r.rating || 0}">
                                ${[1,2,3,4,5].map(n =>
                `<button class="star-btn${n <= (r.rating||0) ? ' lit' : ''}"
                                             data-onclick="setStars(this,${n})">★</button>`
            ).join('')}
                            </div>
                            <input class="one-line edit-plr-comment"
                                   value="${esc(r.comment || '')}"
                                   placeholder="한줄평 (선택)" maxlength="200">
                        </div>`).join('')}
                    </div>`;
        } catch (e) {
            console.error('[edit] 장소 리뷰 로드 실패', e);
        }
    }

    /* ────────────────────────────────────────────────
     * 8. 후기 수정 저장   PATCH /api/posts/{postId}
     * ──────────────────────────────────────────────── */
    async function doEditReviewSave() {
        const postId  = window._currentPostId;
        const title   = document.getElementById('editReviewTitle')?.value.trim();
        const content = document.getElementById('editBlogEditor')?.innerHTML.trim();
        const activeChip = document.querySelector('#page-edit-review .vis-chip.on');
        const isPublic   = activeChip ? !activeChip.textContent.includes('비공개') : true;

        if (!title)   { toast('제목을 입력해주세요.'); return; }
        if (!content) { toast('내용을 입력해주세요.'); return; }

        // 1. 게시글 수정
        const res = await api.patch(`/api/posts/${postId}`, { title, content, isPublic });
        if (!res || res.success === false) {
            toast(res?.message || '수정에 실패했습니다.');
            return;
        }

        // 2. 장소 리뷰 수정
        const rows = document.querySelectorAll('#edit-place-list .edit-plr-row');
        if (rows.length) {
            const reviews = Array.from(rows)
                .map(row => ({
                    placeReviewId: Number(row.dataset.reviewId),
                    rating: Number(row.querySelector('.star-sel')?.dataset.rating || 0),
                    comment: (row.querySelector('.edit-plr-comment')?.value || '').trim() || null
                }))
                .filter(r => r.placeReviewId && r.rating >= 1);

            if (reviews.length) {
                await api.patch(`/api/posts/${postId}/place-reviews`, { reviews });
            }
        }

        toast('✅ 수정이 완료되었습니다.');
        go('review');
        loadReviewDetail(postId);
    }

    /* ────────────────────────────────────────────────
     * 9. 에디터 이미지 삽입
     * ──────────────────────────────────────────────── */
    function insertEditImage(input) {
        const file = input.files[0];
        if (!file) return;
        const reader = new FileReader();
        reader.onload = e => {
            const img = document.createElement('img');
            img.src = e.target.result;
            img.style.cssText = 'max-width:100%;border-radius:8px;margin:8px 0';
            const editor = document.getElementById('editBlogEditor');
            const sel = window.getSelection();
            if (sel.rangeCount && editor.contains(sel.anchorNode)) {
                const r = sel.getRangeAt(0); r.deleteContents(); r.insertNode(img);
            } else { editor.appendChild(img); }
        };
        reader.readAsDataURL(file);
        input.value = '';
    }

    /* ────────────────────────────────────────────────
     * 10. 장소 리뷰 목록   GET /api/places/{placeId}/reviews
     *     window._currentPlaceId 세팅 후 go('place-reviews') 호출
     * ──────────────────────────────────────────────── */
    function goToPlaceReviews(placeId, placeName) {
        go('place-reviews');
        loadPlaceReviews(placeId, placeName);
    }

    function _placeCatIcon(category) {
        return { accommodation: '🏨', restaurant: '🍽️', cafe: '☕', attraction: '📍' }
            [(category || '').toLowerCase()] || '📍';
    }

    function _placeCatLabel(category) {
        return { accommodation: '숙소', restaurant: '맛집', cafe: '카페', attraction: '관광지' }
            [(category || '').toLowerCase()] || '';
    }

    async function loadPlaceReviews(placeId, hintPlaceName) {
        window._currentPlaceId = placeId;

        const headerEl = document.getElementById('place-reviews-header');
        // 미리 알고 있는 이름이 있으면 즉시 헤더 표시, 없으면 로딩 표시
        if (headerEl) headerEl.innerHTML = hintPlaceName
            ? `<div class="prc-icon" style="background:var(--sage-pale);font-size:22px">📍</div>
               <div style="flex:1"><div style="font-size:18px;font-weight:800">${escapeHtml ? escapeHtml(hintPlaceName) : hintPlaceName}</div></div>`
            : `<div class="prc-icon" style="background:var(--sage-pale);font-size:22px">📍</div>
               <div><div style="font-size:12px;color:var(--text3)">불러오는 중...</div></div>`;

        const res = await api.get(`/api/places/${placeId}/reviews`);
        const reviews = (res.success && Array.isArray(res.data)) ? res.data : [];

        // 사용자별 중복 제거: writerId 기준 최신 1건만 유지
        const _seenUsers = new Map();
        reviews.forEach(r => {
            const uid = r.writerId;  // PlaceReviewResponseDto 필드명은 writerId
            if (!_seenUsers.has(uid) ||
                new Date(r.createdAt || 0) > new Date(_seenUsers.get(uid).createdAt || 0)) {
                _seenUsers.set(uid, r);
            }
        });
        const deduplicatedReviews = Array.from(_seenUsers.values());

        // 정렬용으로 메모리에 캐시 (백엔드 재호출 없이 클라이언트에서 정렬)
        window._currentPlaceReviews = deduplicatedReviews;

        const placeName = hintPlaceName || reviews[0]?.placeName || '장소';
        const category  = reviews[0]?.category  || 'tour';
        window._currentPlaceCategory = category;

        const avg = reviews.length
            ? reviews.reduce((sum, r) => sum + (r.rating || 0), 0) / reviews.length
            : 0;

        if (headerEl) headerEl.innerHTML = `
    <div class="prc-icon" style="background:var(--sage-pale);font-size:22px">
      ${_placeCatIcon(category)}
    </div>
    <div style="flex:1">
      <div style="font-size:18px;font-weight:800">${placeName}</div>
      <div style="font-size:12px;color:var(--text3);margin-top:2px">
        ${_placeCatLabel(category)} &nbsp;·&nbsp; 리뷰 ${reviews.length}개
      </div>
    </div>
    <button class="btn-o" style="padding:6px 14px;font-size:12px"
            data-onclick="doPlaceScrap()">★ 스크랩</button>`;

        // 통계
        _setHTML('place-stats-grid', `
    <div class="prev-stat-card">
      <div class="psc-val">${avg ? avg.toFixed(1) : '—'}</div>
      <div class="psc-label">★★★★★ 평균 평점</div>
    </div>
    <div class="prev-stat-card">
      <div class="psc-val">${reviews.length}</div>
      <div class="psc-label">총 리뷰 수</div>
    </div>
    <div class="prev-stat-card">
      <div class="psc-val">—</div>
      <div class="psc-label">스크랩 수</div>
    </div>`);

        _setHTML('place-tags-box', '');

        renderPlaceReviewList(_sortPlaceReviews(deduplicatedReviews, 'latest'));
    }

    function loadPlaceReviewsSorted(sort) {
        const reviews = window._currentPlaceReviews || [];
        renderPlaceReviewList(_sortPlaceReviews(reviews, sort));
    }

    function _sortPlaceReviews(reviews, sort) {
        const copy = reviews.slice();
        if (sort === 'rating_desc')      copy.sort((a, b) => (b.rating || 0) - (a.rating || 0));
        else if (sort === 'rating_asc')  copy.sort((a, b) => (a.rating || 0) - (b.rating || 0));
        else /* latest */               copy.sort((a, b) => new Date(b.createdAt || 0) - new Date(a.createdAt || 0));
        return copy;
    }

    function renderPlaceReviewList(reviews) {
        const listEl = document.getElementById('place-review-list');
        if (!listEl) return;
        if (!reviews.length) {
            listEl.innerHTML = `<div style="text-align:center;padding:40px;color:var(--text3)">
      아직 등록된 리뷰가 없습니다.</div>`;
            return;
        }
        listEl.innerHTML = reviews.map(r => `
    <div style="background:var(--surface);border:1px solid var(--border2);
                border-radius:var(--r);padding:16px;${r.postId ? 'cursor:pointer' : ''}"
         ${r.postId ? `onclick="openPostDetail(${r.postId})"` : ''}>
      <div style="display:flex;gap:8px;margin-bottom:8px">
        <div class="comm-av">${(r.writerName||'?')[0]}</div>
        <div>
          <div style="font-weight:700;font-size:13px">${r.writerName||''}${r.writerRole==='ADMIN'?' <span style="font-size:10px;font-weight:800;background:#0d9488;color:#fff;border-radius:4px;padding:2px 9px;letter-spacing:.4px;vertical-align:middle">관리자</span>':''}</div>
          <div style="font-size:11px;color:var(--text3)">${(r.createdAt||'').toString().substring(0,10)}${r.postTitle ? ' · '+r.postTitle : ''}</div>
        </div>
        <div style="margin-left:auto;color:var(--warm);font-weight:700">
          ${r.starsHtml||''} ${r.rating||''}
        </div>
      </div>
      <div style="font-size:13px;color:var(--text2);line-height:1.6">
        ${r.comment||''}
      </div>
    </div>`).join('');
    }

    /* ────────────────────────────────────────────────
     * 11. 장소 스크랩 (커뮤니티 스크랩 패턴 활용)
     * ──────────────────────────────────────────────── */
    async function doPlaceScrap() {
        if (!window._commUtil?.requireLogin()) {
            toast('로그인이 필요합니다.'); go('login'); return;
        }
        const placeId = window._currentPlaceId;
        if (!placeId) { toast('장소 정보를 찾을 수 없습니다.'); return; }
        const category = window._currentPlaceCategory || 'tour';
        const res = await api.post(`/api/places/${placeId}/scraps`, { category });
        const scrapped = res?.data === true;
        toast(res?.success === false
            ? (res?.message || '스크랩 처리에 실패했습니다.')
            : (scrapped ? '★ 스크랩했습니다!' : '스크랩을 취소했습니다.'));
    }

    /* ────────────────────────────────────────────────
     * 12. 미리보기 모달 스크랩
     * ──────────────────────────────────────────────── */
    async function doPreviewScrap() {
        const postId = window._currentPostId;
        if (postId) {
            const res = await api.post(`/api/posts/${postId}/scraps`, {});
            toast(res.success ? '🔖 스크랩 완료!' : (res.message || '스크랩 처리 실패'));
        } else {
            toast('🔖 스크랩 완료!');
        }
        closePrev();
    }

    /* ────────────────────────────────────────────────
     * 13. 플랜 확정   PATCH /api/trips/{tripId}
     * ──────────────────────────────────────────────── */
    async function doConfirmPlan() {
        const tripId   = window._currentTripId;
        const isPublic = !!document.querySelector('#confirmModal .vis-chip.on')
            ?.textContent.includes('공개');
        if (tripId) {
            await api.patch(`/api/trips/${tripId}`, { isPublic });
        }
        document.getElementById('confirmModal').classList.remove('open');
        go('map');
        toast('✅ 플랜이 확정되었습니다!');
        document.getElementById('confirmDoneModal').classList.add('open');
    }

    /* ────────────────────────────────────────────────
     * 14. 공유 모달 — 링크 생성 + 멤버 목록
     *     POST /api/trips/{tripId}/share
     *     GET  /api/trips/{tripId}/members
     * ──────────────────────────────────────────────── */
    async function loadShareModal() {
        const tripId = window._currentTripId || sessionStorage.getItem('plannerDraftId');
        if (!tripId) {
            document.getElementById('share-member-list').innerHTML = '<div style="font-size:13px;color:var(--text3);">저장된 플랜이 없습니다.</div>';
            return;
        }

        // 💡 읽기 전용 링크 세팅 (/plan/view 경로)
        const linkEl = document.getElementById('share-link-url');
        if (linkEl) linkEl.value = `${window.location.origin}/plan/view?id=${tripId}`;

        // 멤버 목록 렌더링
        const membersRes = await api.get(`/api/trips/${tripId}/members`);
        const listEl = document.getElementById('share-member-list');

        if (listEl) {
            if (membersRes.success && Array.isArray(membersRes.data) && membersRes.data.length > 0) {
                listEl.innerHTML = membersRes.data.map(m => `
                        <div style="display:flex; align-items:center; justify-content:space-between;">
                          <div style="display:flex; align-items:center; gap:12px;">
                            <div style="width:36px; height:36px; border-radius:50%; background:${m.role === 'OWNER' ? 'var(--sage)' : '#E5E7EB'}; color:${m.role === 'OWNER' ? '#fff' : '#333'}; display:flex; align-items:center; justify-content:center; font-weight:800; font-size:14px;">${(m.name||'?')[0]}</div>
                            <div style="font-weight:700; font-size:14px; color:#111;">${m.name||''} ${m.role === 'OWNER' ? '<span style="color:#2563EB; font-weight:800;">(소유자)</span>' : ''}</div>
                          </div>
                          <div style="font-size:12px; color:var(--text3);">${m.role==='OWNER'?'소유자':'편집자'}</div>
                        </div>`).join('');
            } else {
                // 연동 전 가짜 데이터
                const me = (typeof _currentUser !== 'undefined' && _currentUser) ? _currentUser.name : '나';
                listEl.innerHTML = `
                      <div style="display:flex; align-items:center; justify-content:space-between;">
                        <div style="display:flex; align-items:center; gap:12px;">
                          <div style="width:36px; height:36px; border-radius:50%; background:var(--sage); color:#fff; display:flex; align-items:center; justify-content:center; font-weight:800; font-size:14px;">${me[0]}</div>
                          <div style="font-weight:700; font-size:14px; color:#111;">${me} <span style="color:#2563EB; font-weight:800;">(소유자)</span></div>
                        </div>
                        <div style="font-size:12px; color:var(--text3);">소유자</div>
                      </div>`;
            }
        }
    }

    /* ────────────────────────────────────────────────
     * 15. 멤버 초대   POST /api/trips/{tripId}/members
     * ──────────────────────────────────────────────── */
    async function doShareInvite() {
        const tripId = window._currentTripId || sessionStorage.getItem('plannerDraftId');
        const input  = document.getElementById('shareEmailInput');
        if (!input?.value.trim()) { toast('이메일을 입력해주세요.'); return; }
        if (!tripId) { toast('공유할 플랜이 없습니다.'); return; }

        const emails = input.value.split(',').map(e => e.trim()).filter(Boolean);
        let successCount = 0;
        for (const email of emails) {
            // 편집자(EDITOR) 권한 부여
            const res = await api.post(`/api/trips/${tripId}/members`, { email, role: 'EDITOR' });
            if(res.success) successCount++;
        }
        if(successCount > 0) {
            toast('✅ 초대(편집 권한)가 발송되었습니다.');
            input.value = '';
            await loadShareModal();
        } else {
            toast('⚠️ 초대 실패. 가입된 유저인지 확인해주세요.');
        }
    }

    /* ────────────────────────────────────────────────
     * 16. 공유 링크 복사 (읽기 전용)
     * ──────────────────────────────────────────────── */
    function doShareLinkCopy() {
        const linkEl = document.getElementById('share-link-url');
        if (!linkEl || !linkEl.value) {
            toast('링크가 없습니다.'); return;
        }
        navigator.clipboard?.writeText(linkEl.value)
            .then(() => toast('✅ 읽기 전용 링크가 복사되었습니다!'))
            .catch(() => toast('링크 복사에 실패했습니다.'));
    }

    /* ────────────────────────────────────────────────
     * 17. openShareModal — 열릴 때 데이터 자동 로드
     * ──────────────────────────────────────────────── */
    function openShareModal() {
        document.getElementById('shareModal').classList.add('open');
        // 열릴 때 기본적으로 '공유' 탭 표시
        setShareTab(document.querySelector('.share-tab'), 'members');
        loadShareModal();
    }

    /* ────────────────────────────────────────────────
     * 18. 공유 팝업 탭 전환 로직
     * ──────────────────────────────────────────────── */
    function setShareTab(btn, tab) {
        if(!btn) return;
        document.querySelectorAll('#shareModal .share-tab').forEach(b => {
            b.classList.remove('on');
            b.style.color = 'var(--text3)';
            b.style.borderBottom = 'none';
        });
        btn.classList.add('on');
        btn.style.color = 'var(--sage-d)';
        btn.style.borderBottom = '3px solid var(--sage)';

        document.getElementById('share-members').style.display = tab === 'members' ? 'block' : 'none';
        document.getElementById('share-post').style.display    = tab === 'post' ? 'block' : 'none';
    }

    /* ────────────────────────────────────────────────
     * 내부 헬퍼
     * ──────────────────────────────────────────────── */
    function _setText(id, text) {
        const el = document.getElementById(id);
        if (el) el.textContent = text;
    }
    function _setHTML(id, html) {
        const el = document.getElementById(id);
        if (el) el.innerHTML = html;
    }
    function esc(str) {
        return (str || '').replace(/'/g, "\\'");
    }
