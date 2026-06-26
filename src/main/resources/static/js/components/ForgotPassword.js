// 忘記密碼 - 第一步：輸入 Email，請後端寄出「重設密碼信」。
// 對應後端：POST /api/auth/forgot-password  { email, accountType }
import { api } from '../api.js';

export default {
    name: 'ForgotPassword',
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
                await api.post('/auth/forgot-password', {
                    email: this.email,
                    accountType: this.accountType,
                });
                // 後端為了安全，不論帳號是否存在都回相同訊息
                this.okMsg = '若帳號存在，我們已寄出重設密碼信，請至信箱收信（連結 30 分鐘內有效）。';
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
          <h2>忘記密碼</h2>
          <p class="sub">輸入註冊用的 Email，我們會寄重設密碼的連結給你</p>
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
          <button class="btn block" type="submit" :disabled="loading">{{ loading ? '寄送中…' : '寄出重設信' }}</button>
        </form>

        <p class="hint" style="margin-top:12px">
          想起密碼了？
          <a href="#/member/login">會員登入</a> ·
          <a href="#/farmer/login">小農登入</a>
        </p>
      </div>
    </div>
  `,
};
