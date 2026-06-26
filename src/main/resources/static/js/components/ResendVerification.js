// 重寄 Email 驗證信：給註冊後沒收到信、或連結過期的人。
// 對應後端：POST /api/auth/resend-verification  { email, accountType }
import { api } from '../api.js';

export default {
    name: 'ResendVerification',
    data() {
        return {
            email: '',
            accountType: 'MEMBER',          // MEMBER = 會員、FARMER = 小農
            loading: false,
            errorMsg: '',
            okMsg: '',
        };
    },
    methods: {
        async onSubmit() {
            this.errorMsg = '';
            this.okMsg = '';
            this.loading = true;
            try {
                await api.post('/auth/resend-verification', {
                    email: this.email,
                    accountType: this.accountType,
                });
                // 後端為了安全，不論帳號是否存在 / 是否已驗證都回相同訊息
                this.okMsg = '若帳號存在且尚未驗證，我們已重新寄出驗證信，請至信箱收信。';
            } catch (e) {
                this.errorMsg = '送出失敗，請稍後再試';
            } finally {
                this.loading = false;
            }
        },
    },
    template: `
    <div class="auth-wrap">
      <div class="auth-card">
        <div class="auth-brand">
          <h2>重寄驗證信</h2>
          <p class="sub">沒收到驗證信或連結過期了？在這裡重新寄一封</p>
        </div>

        <p v-if="errorMsg" class="err">{{ errorMsg }}</p>
        <p v-if="okMsg" class="ok">{{ okMsg }}</p>

        <form class="form-grid" @submit.prevent="onSubmit">
          <div class="field">
            <label>身分</label>
            <select v-model="accountType">
              <option value="MEMBER">會員</option>
              <option value="FARMER">小農</option>
            </select>
          </div>
          <div class="field"><label>Email</label><input v-model="email" type="email" required /></div>
          <button class="btn block" type="submit" :disabled="loading">{{ loading ? '寄送中…' : '重寄驗證信' }}</button>
        </form>

        <p class="hint" style="margin-top:12px">
          <a href="#/member/login">會員登入</a> ·
          <a href="#/farmer/login">小農登入</a>
        </p>
      </div>
    </div>
  `,
};
