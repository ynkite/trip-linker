/* page_planner.html 에서 옮김 (CSP: 인라인 스크립트를 쓰지 않음) */
        const DAYS_KO = ['일','월','화','수','목','금','토'];
        const WEATHER_PREVIEW_DAYS = 7;
        const WEATHER_MAX_FUTURE   = 14;

        function onStartDateChange() {
            const startEl  = document.getElementById('s1-date-start');
            const endInput = document.getElementById('s1-date-end');
            const today    = new Date().toISOString().split('T')[0];
            const val      = startEl.value;
            if (!val) { updateWeatherBtn(); return; }
            if (val < today) {
                toast('⚠️ 출발일은 오늘 이후 날짜만 선택할 수 있습니다.');
                startEl.value = '';
                updateWeatherBtn();
                return;
            }
            endInput.min = val;
            // 최대 3박 4일: 귀환일 max = 출발일 + 3일
            const maxEnd = new Date(val);
            maxEnd.setDate(maxEnd.getDate() + 3);
            endInput.max = maxEnd.toISOString().split('T')[0];
            if (endInput.value && endInput.value < val) {
                endInput.value = '';
                toast('⚠️ 귀환일이 출발일보다 이전이어서 초기화되었습니다.');
            } else if (endInput.value && endInput.value > endInput.max) {
                endInput.value = '';
                toast('⚠️ 최대 3박 4일까지만 선택할 수 있습니다.');
            }
            updateWeatherBtn();
        }

        function onEndDateChange() {
            const startEl = document.getElementById('s1-date-start');
            const endEl   = document.getElementById('s1-date-end');
            const start   = startEl.value;
            const end     = endEl.value;
            if (!end) { updateWeatherBtn(); return; }
            if (start && end < start) {
                toast('⚠️ 귀환일은 출발일과 같거나 이후 날짜만 선택할 수 있습니다.');
                endEl.value = '';
                updateWeatherBtn();
                return;
            }
            if (start) {
                const maxEnd = new Date(start);
                maxEnd.setDate(maxEnd.getDate() + 3);
                if (end > maxEnd.toISOString().split('T')[0]) {
                    toast('⚠️ 최대 3박 4일까지만 선택할 수 있습니다.');
                    endEl.value = '';
                    updateWeatherBtn();
                    return;
                }
            }
            updateWeatherBtn();
        }

        function updateWeatherBtn() {
            const s    = document.getElementById('s1-date-start').value;
            const e    = document.getElementById('s1-date-end').value;
            const wrap = document.getElementById('weather-btn-wrap');
            const lbl  = document.getElementById('weather-btn-label');
            if (!s || !e) { wrap.style.display = 'none'; return; }
            const fmt = d => `${d.getMonth()+1}/${d.getDate()}`;
            lbl.textContent = `${fmt(new Date(s))} – ${fmt(new Date(e))} 날씨 보기`;   // 휴대폰 반 칸에 한 줄로 들어가게 짧게
            wrap.style.display = 'block';
        }

        async function openWeatherPopup() {
            const s = document.getElementById('s1-date-start').value;
            const e = document.getElementById('s1-date-end').value;
            if (!s || !e) return;

            const ds       = new Date(s);
            const de       = new Date(e);
            const today    = new Date(); today.setHours(0,0,0,0);
            const diffDays = Math.floor((ds - today) / 86400000);

            const destProv = document.getElementById('dest-prov');
            const destCity = document.getElementById('dest-city');
            const loc = (destCity?.value && destCity.value !== '전체')
                ? destCity.options[destCity.selectedIndex]?.text
                : destProv?.options[destProv.selectedIndex]?.text || '';
            const locStr = (loc && loc !== '도/시 선택') ? loc : '서울';

            document.getElementById('wp-title').textContent = `🌤️ ${locStr} 여행 날씨 예보`;

            const noData   = document.getElementById('wp-no-data');
            const dataWrap = document.getElementById('wp-data-wrap');

            if (diffDays > WEATHER_MAX_FUTURE) {
                noData.style.display   = 'block';
                dataWrap.style.display = 'none';
                document.getElementById('weatherPopup').classList.add('open');
                return;
            }

            noData.style.display   = 'none';
            dataWrap.style.display = 'block';
            document.getElementById('weatherPopup').classList.add('open');

            const grid = document.getElementById('wp-grid');
            grid.innerHTML = '<div style="font-size:13px;color:var(--text3);padding:16px 0">로딩 중...</div>';

            try {
                const res  = await fetch('/api/maps/weather?region=' + encodeURIComponent(locStr));
                const json = await res.json();

                if (!json.success || !json.data || json.data.length === 0) throw new Error('데이터 없음');

                const allDays = [];
                for (let d = new Date(ds); d <= de; d.setDate(d.getDate()+1)) allDays.push(new Date(d));
                const previewDays = allDays.slice(0, WEATHER_PREVIEW_DAYS);
                const clipped     = allDays.length > WEATHER_PREVIEW_DAYS;

                grid.style.gridTemplateColumns = `repeat(${previewDays.length}, 1fr)`;

                const skyIconMap = { 1:'☀️', 3:'⛅', 4:'☁️' };
                const ptyIconMap = { 1:'🌧️', 2:'🌧️', 3:'🌨️', 4:'🌦️' };
                const skyDescMap = { 1:'맑음', 3:'구름많음', 4:'흐림' };
                const ptyDescMap = { 1:'비', 2:'비/눈', 3:'눈', 4:'소나기' };

                const fmt = d => `${String(d.getMonth()+1).padStart(2,'0')}/${String(d.getDate()).padStart(2,'0')} (${DAYS_KO[d.getDay()]})`;
                const warnDays = [];

                grid.innerHTML = previewDays.map((d, i) => {
                    const dto  = json.data[i] || json.data[json.data.length - 1];
                    const warn = dto.pop >= 60 || dto.pty > 0;
                    const icon = dto.pty > 0 ? (ptyIconMap[dto.pty] || '🌧️') : (skyIconMap[dto.sky] || '⛅');
                    const desc = dto.pty > 0 ? (ptyDescMap[dto.pty] || '강수') : (skyDescMap[dto.sky] || '');
                    if (warn) warnDays.push(`${d.getMonth()+1}/${d.getDate()}`);
                    return `<div class="w-item${warn ? ' w-warn' : ''}">
          <div class="w-icon">${icon}</div>
          <div class="w-day">${fmt(d)}</div>
          <div class="w-temp">${dto.tmp}°C</div>
          <div class="w-desc">${desc}</div>
          ${dto.pop > 0 ? `<div style="font-size:9px;color:var(--sky);margin-top:2px">💧 ${dto.pop}%</div>` : ''}
          ${warn ? '<div style="font-size:9px;color:var(--coral);margin-top:2px">실내 추천</div>' : ''}
        </div>`;
                }).join('');

                if (clipped) {
                    grid.innerHTML += `<div style="grid-column:1/-1;font-size:11px;color:var(--text3);text-align:center;padding:8px 0;border-top:1px dashed var(--border)">
          📌 이후 ${allDays.length - WEATHER_PREVIEW_DAYS}일(${fmt(allDays[WEATHER_PREVIEW_DAYS])} ~ ${fmt(allDays[allDays.length-1])})은 예보 범위 초과로 표시되지 않습니다
        </div>`;
                }

                const alertEl = document.getElementById('wp-alert');
                if (warnDays.length) {
                    alertEl.style.display = 'block';
                    alertEl.textContent = `⚠️ ${warnDays.join(', ')} 악천후 감지 — 실내 장소 우선 배치를 권장합니다`;
                } else {
                    alertEl.style.display = 'none';
                }

            } catch (err) {
                grid.innerHTML = '<div style="font-size:13px;color:var(--text3);padding:16px 0">날씨 정보를 불러올 수 없습니다.</div>';
                console.warn('[WeatherPopup] API 호출 실패:', err);
            }
        }

        function closeWeatherPopup() {
            document.getElementById('weatherPopup').classList.remove('open');
        }

        function initPlannerDateConstraints() {
            const today   = new Date().toISOString().split('T')[0];
            const startEl = document.getElementById('s1-date-start');
            const endEl   = document.getElementById('s1-date-end');

            if (!startEl || !endEl) {
                setTimeout(initPlannerDateConstraints, 50);
                return;
            }

            startEl.min = today;
            if (startEl.value && startEl.value < today) startEl.value = '';

            endEl.min = today;
            if (endEl.value && endEl.value < today) endEl.value = '';

            if (startEl.value) endEl.min = startEl.value;
            updateWeatherBtn();
            _initStep1AutoSave();
        }

        if (document.readyState === 'loading') {
            document.addEventListener('DOMContentLoaded', initPlannerDateConstraints);
        } else {
            initPlannerDateConstraints();
        }
    
