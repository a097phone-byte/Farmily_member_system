// 這支檔案：把「呼叫後端 API」這件事包成好用的小工具。
// 重點是每次請求都帶 cookie（credentials: 'include'），後端才認得我們已登入。

// 共用的底層函式：path 例如 '/member/login'（前面會自動補 /api）
async function apiFetch(path, options = {}) {
    const res = await fetch('/api' + path, {
        credentials: 'include',                 // ★ 關鍵：帶上 cookie（JSESSIONID）
        headers: { 'Content-Type': 'application/json', ...(options.headers || {}) },
        ...options,
    });

    // 後端用 HTTP 狀態碼表示成敗：不是 2xx 就視為錯誤丟出去
    if (!res.ok) {
        const err = new Error('API 錯誤');
        err.status = res.status;                // 把狀態碼帶著，呼叫端可判斷 401/403/409…
        throw err;
    }

    // 有些 API 回 JSON（資料），有些回純文字（例如登出回「已登出」）
    const text = await res.text();
    try { return JSON.parse(text); } catch (e) { return text; }
}

// 對外提供四個常用捷徑：get / post / put / del
export const api = {
    get:  (p) => apiFetch(p),
    post: (p, body) => apiFetch(p, { method: 'POST', body: JSON.stringify(body) }),
    put:  (p, body) => apiFetch(p, { method: 'PUT', body: body != null ? JSON.stringify(body) : undefined }),
    del:  (p) => apiFetch(p, { method: 'DELETE' }),
};