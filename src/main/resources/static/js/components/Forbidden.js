// 403 頁面：登入了但權限不足時顯示。
export default {
    name: 'Forbidden',
    template: `
    <div style="text-align:center;padding:48px">
      <h2>沒有權限</h2>
      <p>你的帳號沒有存取此頁面的權限。</p>
      <a href="#/">回首頁</a>
    </div>
  `,
};