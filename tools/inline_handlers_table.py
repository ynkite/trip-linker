"""data-on* 처리기 표(src/main/resources/static/js/inline_handlers_table.js)를 만든다.

    python tools/inline_handlers_table.py          (저장소 맨 위에서)

- 템플릿 마크업과 화면 JS 의 data-on<이벤트>="..." 글 가운데 고정된 것(값이 끼워지지 않는 것)을
  실제 함수로 만들어 표에 넣는다. 브라우저가 속성에서 읽을 글과 같은 글을 열쇠로 쓴다.
- 값이 끼워지는 글은 실행할 때 inline_handlers.js 의 작은 해석기가 처리한다.
  해석기가 부를 수 있는 함수 이름 목록도 여기서 만든다(화면 코드가 선언한 전역 함수).
- 표를 만든 뒤 node 가 있으면 문법을 확인한다.
"""
import html
import json
import re
import shutil
import subprocess
import sys
from pathlib import Path

ROOT = Path(__file__).resolve().parent.parent
RES = ROOT / "src/main/resources"
OUT = RES / "static/js/inline_handlers_table.js"

ATTR = re.compile(r'(?<=\s)data-on([a-z]+)=(\\?["\'])(.*?)\2', re.S)
SCRIPT_BLOCK = re.compile(r'<script\b[^>]*>.*?</script>', re.S)
COMMENT = re.compile(r'<!--.*?-->', re.S)
BLOCK_COMMENT = re.compile(r'/\*.*?\*/', re.S)
JS_ESCAPE = re.compile(r'\\(.)', re.S)
CONCAT = re.compile(r"""['"`]\s*\+|\+\s*['"`]""")

DECL = [
    re.compile(r'(?:^|[\s;(){}])(?:async\s+)?function\s+([A-Za-z_$][\w$]*)\s*\('),
    re.compile(r'\bwindow\.([A-Za-z_$][\w$]*)\s*=(?!=)'),
    re.compile(r'(?m)^(?:const|let|var)\s+([A-Za-z_$][\w$]*)\s*=\s*(?:async\s*)?(?:function\b|\([^)]*\)\s*=>|[A-Za-z_$][\w$]*\s*=>)'),
]
SKIP_NAMES = {"eval", "Function", "setTimeout", "setInterval", "fetch", "open", "alert", "confirm", "prompt"}


def js_files():
    return sorted((RES / "static/js").rglob("*.js"))


def templates():
    return sorted((RES / "templates").glob("*.html"))


def static_bodies():
    bodies = {}
    for f in templates():
        s = SCRIPT_BLOCK.sub("", COMMENT.sub("", f.read_text(encoding="utf-8")))
        for m in ATTR.finditer(s):
            bodies.setdefault(html.unescape(m.group(3)), f.name)
    for f in js_files():
        if f == OUT or f.name == "inline_handlers.js":
            continue
        for m in ATTR.finditer(BLOCK_COMMENT.sub("", f.read_text(encoding="utf-8"))):
            body = m.group(3)
            if "${" in body or CONCAT.search(body):
                continue
            bodies.setdefault(html.unescape(JS_ESCAPE.sub(r"\1", body)), f.name)
    return bodies


def global_names():
    found = set()
    for f in js_files():
        if f == OUT or f.name == "inline_handlers.js":
            continue
        s = f.read_text(encoding="utf-8")
        for p in DECL:
            found.update(p.findall(s))
    return sorted(n for n in found if n not in SKIP_NAMES and not n.startswith("__inline"))


def main():
    bodies = static_bodies()
    names = global_names()
    lines = [
        "/* tools/inline_handlers_table.py 가 만든 파일. 손으로 고치지 않는다. */",
        "/* 고정된 data-on* 글 → 처리기 함수 (this = 요소, event = 이벤트) */",
        "window.__inlineHandlerTable = {",
    ]
    for body in sorted(bodies):
        lines.append(f"  {json.dumps(body, ensure_ascii=False)}: function (event) {{\n{body}\n  }},")
    lines.append("};")
    lines.append("/* 값이 끼워지는 data-on* 에서 부를 수 있는 전역 함수 (화면 코드가 선언한 것만) */")
    lines.append("window.__inlineHandlerNames = {")
    for n in names:
        lines.append(f"  {json.dumps(n)}: function () {{ return typeof {n} === 'function' ? {n} : undefined; }},")
    lines.append("};")
    OUT.write_text("\n".join(lines) + "\n", encoding="utf-8", newline="\n")
    print(f"고정 처리기 {len(bodies)}개, 부를 수 있는 함수 {len(names)}개 → {OUT.relative_to(ROOT)}")

    node = shutil.which("node")
    if node:
        r = subprocess.run([node, "--check", str(OUT)], capture_output=True, text=True)
        if r.returncode != 0:
            print(r.stderr, file=sys.stderr)
            sys.exit(1)
        print("node --check 통과")


if __name__ == "__main__":
    main()
