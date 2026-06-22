// 會員「連結帳號」分頁（account-integrations 風）：顯示第三方登入連結狀態。
// 後端目前只有「用 Google idToken 登入/建立帳號」（POST /api/member/oauth/google），
// 沒有「在設定頁解除連結」的 endpoint，因此這裡以顯示狀態為主，並誠實標註限制。
import { store } from '../store.js';

export default {
    name: 'MemberIntegrations',
    data() { return { store }; },
    computed: {
        p() { return this.store.member.profile || {}; },
        googleLinked() { return String(this.p.authProvider).toUpperCase() === 'GOOGLE'; },
    },
    template: `
    <div class="card">
      <div class="card-head"><h3>連結帳號</h3><div class="sub">管理與第三方服務的登入連結</div></div>
      <div class="card-body">
        <div class="form-rows">
          <div class="form-row">
            <div class="lbl">Google<small>使用 Google 帳號登入 Farmily</small></div>
            <div>
              <span v-if="googleLinked" class="badge dot s-active">已連結</span>
              <span v-else class="badge">未連結</span>
            </div>
          </div>
          <div class="form-row">
            <div class="lbl">本機密碼<small>是否設有帳密登入</small></div>
            <div><span class="badge" :class="p.hasPassword ? 'dot s-active' : ''">{{ p.hasPassword ? '已設定' : '未設定' }}</span></div>
          </div>
        </div>
        <p class="hint" style="text-align:left;margin-top:14px">
          目前 Google 連結是在「登入頁」用 Google 登入時自動建立；後端尚未提供「解除連結」功能。
        </p>
      </div>
    </div>
  `,
};
