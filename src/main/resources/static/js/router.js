// 這支檔案：我們「自己手寫」的迷你路由（不用 vue-router 這種陌生套件）。
// 做法：用網址 # 後面的字串決定要顯示哪一頁。
// 例如 http://localhost:8080/#/member/login → 路徑就是 '/member/login'

// 把目前網址的 hash 取出來（去掉開頭的 #）；空的就當首頁 '/'
function currentHash() {
    return window.location.hash.slice(1) || '/';
}

// 響應式的路由狀態：path 一變，畫面就會跟著換
export const router = Vue.reactive({ path: currentHash() });

// 換頁：只要改 hash，瀏覽器就會觸發下面的 hashchange 事件
export function navigate(path) {
    window.location.hash = path;
}

// 監聽網址 hash 變化（使用者點 #連結、按上一頁、重新整理都會觸發）
window.addEventListener('hashchange', () => {
    router.path = currentHash();
});