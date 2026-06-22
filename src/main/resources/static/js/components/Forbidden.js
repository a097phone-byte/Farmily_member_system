// 403 頁面：登入了但權限不足時顯示。
export default {
    name: 'Forbidden',
    template: `
    <div class="content-public">
      <div class="card"><div class="card-body" style="text-align:center;padding:48px">
        <h2>沒有權限</h2>
        <p class="muted">你的帳號沒有存取此頁面的權限。</p>
        <button class="btn outline" onclick="location.hash='#/'">回首頁</button>
      </div></div>
    </div>
  `,
};
