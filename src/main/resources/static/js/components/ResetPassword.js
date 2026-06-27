// 忘記密碼 - 第二步：使用者從信中連結進來（網址帶 token），輸入新密碼。
// 對應後端：POST /api/auth/reset-password  { token, newPassword }
import { api } from '../api.js';
import { navigate } from '../router.js';
import PasswordField from './PasswordField.js';      // 密碼欄（含顯示/隱藏眼睛）

export default {
    name: 'ResetPassword',
    components: { PasswordField },
    data() {
        return {
            token: '',
            newPassword: '',
            confirmPassword: '',
            loading: false,
            errorMsg: '',
            okMsg: '',
            countdown: 3,           // 重設成功後倒數秒數
            timer: null,            // setInterval 的代號，離開頁面時要清掉
        };
    },
    // 進到這頁時，從網址 hash 把 token 取出來
    // 網址長相：http://localhost:8080/#/reset-password?token=xxxx
    mounted() {
        const hash = window.location.hash;          // "#/reset-password?token=xxxx"
        const qIndex = hash.indexOf('?');
        if (qIndex >= 0) {
            const query = hash.slice(qIndex + 1);   // "token=xxxx"
            const params = new URLSearchParams(query);
            this.token = params.get('token') || '';
        }
        if (!this.token) {
            this.errorMsg = '連結無效或缺少參數，請重新申請忘記密碼';
        }
    },
    methods: {
        async onSubmit() {
            this.errorMsg = '';
            this.okMsg = '';

            // 前端先做基本檢查（真正把關在後端）
            if (this.newPassword.length < 8) { this.errorMsg = '新密碼至少 8 碼'; return; }
            if (this.newPassword !== this.confirmPassword) { this.errorMsg = '兩次輸入的密碼不一致'; return; }

            this.loading = true;
            try {
                await api.post('/auth/reset-password', {
                    token: this.token,
                    newPassword: this.newPassword,
                });
                this.okMsg = '密碼重設成功！';
                this.startCountdown();          // 開始逐秒倒數，數到 0 自動回登入頁
            } catch (e) {
                // 後端：404=連結無效、409=連結已用過或過期
                this.errorMsg = e.status === 409 ? '連結已使用過或已過期，請重新申請'
                    : e.status === 404 ? '連結無效，請重新申請'
                    : e.status === 400 ? '密碼格式不符（至少 8 碼）'
                    : '重設失敗，請稍後再試';
            } finally {
                this.loading = false;
            }
        },
        // 每秒把 countdown 減 1，數到 0 就清掉計時器並回登入頁
        startCountdown() {
            this.timer = setInterval(() => {
                this.countdown -= 1;
                if (this.countdown <= 0) {
                    clearInterval(this.timer);
                    navigate('/member/login');
                }
            }, 1000);
        },
    },
    // 使用者若提早離開，把計時器清掉，避免之後又跳一次
    beforeUnmount() {
        if (this.timer) clearInterval(this.timer);
    },
    template: `
    <div class="auth-wrap">
      <div class="auth-card">
        <div class="auth-brand">
          <h2>重設密碼</h2>
          <p class="sub">請輸入新的密碼</p>
        </div>

        <p v-if="errorMsg" class="err">{{ errorMsg }}</p>
        <p v-if="okMsg" class="ok">{{ okMsg }}{{ countdown }} 秒後帶你回登入頁。</p>

        <form class="form-grid" @submit.prevent="onSubmit">
          <div class="field"><label>新密碼</label><password-field v-model="newPassword" placeholder="至少 8 碼"></password-field></div>
          <div class="field"><label>再次輸入新密碼</label><password-field v-model="confirmPassword" placeholder="再次輸入新密碼"></password-field></div>
          <button class="btn block" type="submit" :disabled="loading || !token">{{ loading ? '送出中…' : '確認重設' }}</button>
        </form>

        <p class="hint" style="margin-top:12px"><a href="#/forgot-password">重新申請忘記密碼</a></p>
      </div>
    </div>
  `,
};
