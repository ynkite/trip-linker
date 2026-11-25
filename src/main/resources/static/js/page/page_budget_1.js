/* page_budget.html 에서 옮김 (CSP: 인라인 스크립트를 쓰지 않음) */
  function initLedgerSelector() {
    const container = document.getElementById('ledger-trip-cards');
    if (!container) return;
    container.innerHTML = '<div style="color:var(--text3);font-size:13px;padding:20px 0;text-align:center">불러오는 중...</div>';
  }
  document.addEventListener('DOMContentLoaded', initLedgerSelector);
