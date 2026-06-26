// Email 驗證頁：使用者點信中連結進來（網址帶 token），自動呼叫後端完成驗證，
// 顯示成功/失敗訊息後，再導回首頁。
// 對應後端：GET /api/auth/verify-email?token=xxx
import { api } from '../api.js';
import { navigate } from '../router.js';

export default {
    name: 'VerifyEmail',
    data() {
        return {
            status: 'loading',      // 'loading' | 'ok' | 'error'
            errorMsg: '',
            countdown: 3,           // 驗證成功後倒數秒數
            timer: null,            // setInterval 的代號，離開頁面時要清掉
        };
    },
    // 進到這頁時，從網址 hash 取出 token，自動送出驗證
    // 網址長相：http://localhost:8080/#/verify-email?token=xxxx
    mounted() {
        const hash = window.location.hash;          // "#/verify-email?token=xxxx"
        const qIndex = hash.indexOf('?');
        let token = '';
        if (qIndex >= 0) {
            const params = new URLSearchParams(hash.slice(qIndex + 1));
            token = params.get('token') || '';
        }
        if (!token) {
            this.status = 'error';
            this.errorMsg = '連結無效或缺少參數，請重新申請驗證信';
            return;
        }
        this.verify(token);
    },
    methods: {
        async verify(token) {
            try {
                await api.get('/auth/verify-email?token=' + encodeURIComponent(token));
                this.status = 'ok';
                this.startCountdown();          // 開始逐秒倒數，數到 0 自動導回首頁
            } catch (e) {
                // 後端：404=連結無效、409=連結已用過或過期
                this.status = 'error';
                this.errorMsg = e.status === 409 ? '連結已使用過或已過期，請重新申請驗證信'
                    : e.status === 404 ? '連結無效，請重新申請驗證信'
                    : '驗證失敗，請稍後再試';
            }
        },
        // 每秒把 countdown 減 1，數到 0 就清掉計時器並導回首頁
        startCountdown() {
            this.timer = setInterval(() => {
                this.countdown -= 1;
                if (this.countdown <= 0) {
                    clearInterval(this.timer);
                    navigate('/');
                }
            }, 1000);
        },
        goHome() { navigate('/'); },
    },
    // 使用者若提早離開（按按鈕或關頁），把計時器清掉，避免之後又跳一次
    beforeUnmount() {
        if (this.timer) clearInterval(this.timer);
    },
    template: `
    <div class="auth-wrap">
      <div class="auth-card">
        <div class="auth-brand">
          <h2>Email 驗證</h2>
        </div>

        <p v-if="status === 'loading'" class="muted" style="text-align:center">驗證中，請稍候…</p>

        <template v-else-if="status === 'ok'">
          <p class="ok">Email 驗證成功！{{ countdown }} 秒後自動帶你回首頁。</p>
          <button class="btn block" @click="goHome">立即回首頁登入</button>
        </template>

        <template v-else>
          <p class="err">{{ errorMsg }}</p>
          <p class="hint" style="text-align:center"><a href="#/resend-verification">重新申請驗證信</a></p>
        </template>
      </div>
    </div>
  `,
};
