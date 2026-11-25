/* page_weather.html 에서 옮김 (CSP: 인라인 스크립트를 쓰지 않음) */
        (function initWeatherTab() {
            const DAYS_KO = ['일','월','화','수','목','금','토'];

            // 기상청 WeatherResponseDto → 화면 표시용 객체 변환
            function toDisplayItem(dto) {
                const skyIconMap  = { 1:'☀️', 3:'⛅', 4:'☁️' };
                const ptyIconMap  = { 1:'🌧️', 2:'🌧️', 3:'🌨️', 4:'🌦️' };
                const skyDescMap  = { 1:'맑음', 3:'구름많음', 4:'흐림' };
                const ptyDescMap  = { 1:'비', 2:'비/눈', 3:'눈', 4:'소나기' };

                const warn = dto.pop >= 60 || dto.pty > 0;
                const icon = dto.pty > 0 ? (ptyIconMap[dto.pty] || '🌧️') : (skyIconMap[dto.sky] || '⛅');
                const desc = dto.pty > 0 ? (ptyDescMap[dto.pty] || '강수') : (skyDescMap[dto.sky] || '');

                return { icon, desc, temp: dto.tmp, rain: dto.pop, warn };
            }

            // 그리드 HTML 생성 (page_planner 팝업과 공용)
            function buildGridHTML(weatherItems, dateObjs) {
                const fmt = d => (d.getMonth()+1).toString().padStart(2,'0') + '/' + d.getDate().toString().padStart(2,'0');
                const warnDays = [];
                const html = weatherItems.map(function(w, i) {
                    const d = dateObjs[i];
                    const label = i === 0 ? '오늘' : i === 1 ? '내일' : fmt(d) + ' (' + DAYS_KO[d.getDay()] + ')';
                    if (w.warn) warnDays.push(fmt(d));
                    return '<div class="w-item' + (w.warn ? ' w-warn' : '') + '">' +
                        '<div class="w-icon">' + w.icon + '</div>' +
                        '<div class="w-day">' + label + '</div>' +
                        '<div class="w-temp">' + w.temp + '°C</div>' +
                        '<div class="w-desc">' + w.desc + '</div>' +
                        (w.rain > 0 ? '<div style="font-size:9px;color:var(--sky);margin-top:2px">💧 ' + w.rain + '%</div>' : '') +
                        (w.warn ? '<div style="font-size:9px;color:var(--coral);margin-top:2px">실내 추천</div>' : '') +
                        '</div>';
                }).join('');
                return { html, warnDays };
            }

            // 날씨 탭 렌더링 — region 인자 없으면 '서울' 기본값
            async function renderWeatherTab(region) {
                const titleEl = document.getElementById('weather-tab-title');
                const grid    = document.getElementById('weather-tab-grid');
                const alertEl = document.getElementById('weather-tab-alert');
                if (!grid) return;

                // 로딩 표시
                if (titleEl) titleEl.textContent = '🌤️ 날씨 예보 불러오는 중...';
                grid.innerHTML = '<div style="font-size:13px;color:var(--text3);padding:16px 0">로딩 중...</div>';

                const targetRegion = region || '서울';

                try {
                    const res  = await fetch('/api/maps/weather?region=' + encodeURIComponent(targetRegion));
                    const json = await res.json();

                    if (!json.success || !json.data || json.data.length === 0) {
                        throw new Error('데이터 없음');
                    }

                    const today = new Date();
                    const days  = Array.from({ length: 7 }, function(_, i) {
                        const d = new Date(today);
                        d.setDate(today.getDate() + i);
                        return d;
                    });

                    const fmt = function(d) {
                        return (d.getMonth()+1).toString().padStart(2,'0') + '/' + d.getDate().toString().padStart(2,'0');
                    };

                    if (titleEl) titleEl.textContent = '🌤️ ' + targetRegion + ' 날씨 예보 (' + fmt(days[0]) + '~' + fmt(days[6]) + ')';

                    // 백엔드 응답 7일치 slice → 화면용 변환
                    const weatherItems = json.data.slice(0, 7).map(toDisplayItem);
                    const { html, warnDays } = buildGridHTML(weatherItems, days);
                    grid.innerHTML = html;

                    if (alertEl) {
                        if (warnDays.length > 0) {
                            alertEl.style.display = 'block';
                            alertEl.textContent = '⚠️ ' + warnDays.join(', ') + ' 악천후 감지 — 실내 장소 우선 배치';
                        } else {
                            alertEl.style.display = 'none';
                        }
                    }

                } catch (e) {
                    if (titleEl) titleEl.textContent = '🌤️ 날씨 예보';
                    grid.innerHTML = '<div style="font-size:13px;color:var(--text3);padding:16px 0">날씨 정보를 불러올 수 없습니다.</div>';
                    if (alertEl) alertEl.style.display = 'none';
                    console.warn('[WeatherTab] API 호출 실패:', e);
                }
            }

            // ✅ window에 노출 — go()에서 호출 가능 + page_planner에서 지역값 전달 가능
            // 사용법: window.renderWeatherTab()           → 서울 기본
            //         window.renderWeatherTab('부산')      → 지역 지정
            window.renderWeatherTab = renderWeatherTab;

        })();
    
