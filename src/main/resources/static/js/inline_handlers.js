/*
 * 인라인 이벤트 속성(onclick="...") 대신 쓰는 data-onclick="..." 을 실행한다.
 *
 * CSP 의 script-src 에서 'unsafe-inline' 을 빼면 브라우저가 onclick 같은 속성을 실행하지 않는다.
 * 화면 코드는 onclick 자리에 data-onclick 처럼 data-on<이벤트> 를 쓰고, 이 파일이 그 글을 처리기로 바꾼다.
 *   1) 고정된 글은 inline_handlers_table.js 에 미리 함수로 만들어 둔 것을 쓴다.
 *      화면의 data-on* 를 고치거나 더하면 `python tools/inline_handlers_table.py` 로 표를 다시 만든다.
 *   2) 값이 끼워져 실행할 때 정해지는 글은 아래의 작은 해석기로 실행한다.
 *      되는 것: 함수 이름(값, ...) 을 ; 로 잇기, event.stopPropagation(), event.preventDefault(),
 *               return false, if(event.target===this) 한 문장.
 *      값: 글자 · 숫자 · true/false/null · this · this.value · this.checked · event.
 *      부를 수 있는 함수는 화면 코드가 만든 전역 함수(표에 이름이 있는 것)뿐이다. eval 은 쓰지 않는다.
 * 처리기는 이벤트가 문서에 들어오는 순간(capture) 경로 위 요소에 el.onclick 처럼 붙인다.
 * 그래서 이후 순서 · this · return false 는 브라우저가 인라인 처리기와 같게 다룬다.
 * 화면 코드가 el.onclick = ... 으로 직접 바꾼 요소는 건드리지 않는다(인라인 속성을 바꿔 끼우던 것과 같음).
 */
(function () {
    'use strict';

    var TYPES = ['click', 'dblclick', 'change', 'input', 'keydown', 'keyup', 'mousedown', 'mouseup',
        'mouseover', 'mouseout', 'focus', 'blur', 'error', 'load', 'submit'];
    var cache = new Map();

    function table() { return window.__inlineHandlerTable || {}; }
    function names() { return window.__inlineHandlerNames || {}; }

    /* ---------- 작은 해석기 ---------- */

    function tokenize(src) {
        var out = [], i = 0, n = src.length;
        while (i < n) {
            var c = src[i];
            if (/\s/.test(c)) { i++; continue; }
            if (/[A-Za-z_$]/.test(c)) {
                var j = i + 1;
                while (j < n && /[\w$]/.test(src[j])) j++;
                out.push({ t: 'id', v: src.slice(i, j) }); i = j; continue;
            }
            if (/[0-9]/.test(c)) {
                var k = i + 1;
                while (k < n && /[0-9.]/.test(src[k])) k++;
                var num = Number(src.slice(i, k));
                if (isNaN(num)) return null;
                out.push({ t: 'num', v: num }); i = k; continue;
            }
            if (c === "'" || c === '"') {
                var s = '', m = i + 1, closed = false;
                while (m < n) {
                    var d = src[m];
                    if (d === '\\') {
                        var e = src[m + 1];
                        if (e === 'n') s += '\n';
                        else if (e === 't') s += '\t';
                        else if (e === 'r') s += '\r';
                        else if (e === 'u' && /^[0-9a-fA-F]{4}$/.test(src.substr(m + 2, 4))) {
                            s += String.fromCharCode(parseInt(src.substr(m + 2, 4), 16)); m += 4;
                        } else if (e === undefined) return null;
                        else s += e;
                        m += 2; continue;
                    }
                    if (d === c) { closed = true; break; }
                    s += d; m++;
                }
                if (!closed) return null;
                out.push({ t: 'str', v: s }); i = m + 1; continue;
            }
            if (src.substr(i, 3) === '===') { out.push({ t: 'p', v: '===' }); i += 3; continue; }
            if ('(),;.-'.indexOf(c) >= 0) { out.push({ t: 'p', v: c }); i++; continue; }
            return null;
        }
        return out;
    }

    function parse(src) {
        var toks = tokenize(src);
        if (!toks) return null;
        var pos = 0;

        function peek(v) { var t = toks[pos]; return t && (v === undefined || t.v === v) ? t : null; }
        function take(v) { var t = peek(v); if (!t) throw 0; pos++; return t; }
        function isId(v) { var t = toks[pos]; return t && t.t === 'id' && t.v === v; }

        function arg() {
            var t = toks[pos];
            if (!t) throw 0;
            if (t.t === 'str' || t.t === 'num') { pos++; var lit = t.v; return function () { return lit; }; }
            if (t.t === 'p' && t.v === '-') {
                pos++; var neg = take(); if (neg.t !== 'num') throw 0;
                return function () { return -neg.v; };
            }
            if (t.t === 'id') {
                pos++;
                if (t.v === 'true') return function () { return true; };
                if (t.v === 'false') return function () { return false; };
                if (t.v === 'null') return function () { return null; };
                if (t.v === 'undefined') return function () { return undefined; };
                if (t.v === 'event') return function (el, ev) { return ev; };
                if (t.v === 'this') {
                    if (peek('.')) {
                        pos++; var prop = take().v;
                        if (prop !== 'value' && prop !== 'checked') throw 0;
                        return function (el) { return el[prop]; };
                    }
                    return function (el) { return el; };
                }
            }
            throw 0;
        }

        function statement() {
            if (isId('return')) {
                pos++; var r = take();
                if (r.v !== 'false' && r.v !== 'true') throw 0;
                var rv = r.v === 'true';
                return function () { return { ret: rv }; };
            }
            if (isId('event') && toks[pos + 1] && toks[pos + 1].v === '.') {
                pos += 2; var meth = take().v;
                if (meth !== 'stopPropagation' && meth !== 'preventDefault') throw 0;
                take('('); take(')');
                return function (el, ev) { ev[meth](); };
            }
            if (isId('if')) {
                pos++; take('(');
                if (!isId('event')) throw 0; pos++; take('.');
                if (take().v !== 'target') throw 0;
                take('===');
                if (!isId('this')) throw 0; pos++;
                take(')');
                var inner = statement();
                return function (el, ev) { return ev.target === el ? inner(el, ev) : undefined; };
            }
            // [window.] 이름 ( 값, ... )
            if (isId('window')) { pos++; take('.'); }
            var nameTok = take();
            if (nameTok.t !== 'id') throw 0;
            var name = nameTok.v;
            var getter = Object.prototype.hasOwnProperty.call(names(), name) ? names()[name] : null;
            if (!getter) throw 0;
            take('(');
            var args = [];
            if (!peek(')')) {
                args.push(arg());
                while (peek(',')) { pos++; args.push(arg()); }
            }
            take(')');
            return function (el, ev) {
                var fn = getter();
                if (typeof fn !== 'function') throw new Error(name + ' 함수가 없습니다');
                fn.apply(window, args.map(function (a) { return a(el, ev); }));
            };
        }

        try {
            var stmts = [];
            while (pos < toks.length) {
                if (peek(';')) { pos++; continue; }
                stmts.push(statement());
                if (pos < toks.length) take(';');
            }
            return function (ev) {
                for (var i = 0; i < stmts.length; i++) {
                    var r = stmts[i](this, ev);
                    if (r && Object.prototype.hasOwnProperty.call(r, 'ret')) return r.ret;
                }
            };
        } catch (e) {
            return null;
        }
    }

    function compile(src) {
        if (cache.has(src)) return cache.get(src);
        var t = table();
        var fn = Object.prototype.hasOwnProperty.call(t, src) ? t[src] : parse(src);
        if (!fn) console.error('[inline-handlers] 실행할 수 없는 처리기:', src);
        cache.set(src, fn);
        return fn;
    }

    /* ---------- 붙이기 ---------- */

    function bind(el, type) {
        var attr = 'data-on' + type;
        if (!el.hasAttribute(attr)) return;
        var src = el.getAttribute(attr);
        var bound = el.__inlineHandlers || (el.__inlineHandlers = {});
        if (bound[type] === src) return;
        // 화면 코드가 이 요소에 직접 el.onclick = ... 을 넣었으면 그것이 인라인 속성을 대신한 것
        if (bound[type] === undefined && el['on' + type]) return;
        var fn = compile(src);
        bound[type] = src;
        el['on' + type] = fn || null;
    }

    TYPES.forEach(function (type) {
        document.addEventListener(type, function (e) {
            var path = e.composedPath ? e.composedPath() : [];
            for (var i = 0; i < path.length; i++) {
                if (path[i].nodeType === 1) bind(path[i], type);
            }
        }, true);
    });

    // 시험용: 화면에 있는 data-on* 를 실행하지 않고 해석만 해 본다
    window.__inlineHandlers = { compile: compile, parse: parse };
})();
