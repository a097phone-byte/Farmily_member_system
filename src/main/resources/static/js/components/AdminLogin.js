import { store } from '../store.js';
import PasswordField from './PasswordField.js';

export default {
    name: 'AdminLogin',
    components: { PasswordField },
    data() { return { email: '', password: '', loading: false, errorMsg: '' }; },
    methods: {
        async onSubmit() {
            this.errorMsg = ''; this.loading = true;
            try {
                await store.adminLogin(this.email, this.password);   // 已含登入後補打 /me（權限會立刻出現）
            } catch (e) {
                this.errorMsg = e.status === 401 ? '帳號或密碼錯誤'
                    : e.status === 409 ? '帳號已停權或刪除' : '登入失敗';
            } finally { this.loading = false; }
        },
    },
    template: `
    <div class="auth-wrap">
      <div class="auth-card">
        <h2>管理後台</h2>
        <p class="lead">請以管理員帳號登入</p>
        <form class="form-grid" @submit.prevent="onSubmit">
          <div class="field"><label>Email</label><input v-model="email" type="email" required /></div>
          <div class="field"><label>密碼</label><password-field v-model="password" placeholder="密碼"></password-field></div>
          <p v-if="errorMsg" class="err">{{ errorMsg }}</p>
          <button class="btn block" type="submit" :disabled="loading">{{ loading ? '登入中…' : '登入' }}</button>
        </form>
        <p class="hint">測試帳號：admin01@farm.com / admin1234（全權限）</p>
      </div>
    </div>
  `,
};
