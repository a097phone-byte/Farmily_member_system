// 會員登入／註冊（同一頁，用上方分頁切換）。
import { store } from '../store.js';
import { api } from '../api.js';
import { navigate } from '../router.js';
import PasswordField from './PasswordField.js';      // 密碼欄（含顯示/隱藏眼睛）
import DistrictPicker from './DistrictPicker.js';

// ★ Google Client ID：必須等於後端 GoogleTokenVerifier.java 第 13 行那組。
//   開發時直接沿用後端那組即可；要換自己的，前後端兩處要一起換。
const GOOGLE_CLIENT_ID = '528090060943-mooi8topfu9g29umm7f67579vvsk553e.apps.googleusercontent.com';

export default {
    name: 'MemberLogin',
    components: { PasswordField, DistrictPicker },        // 註冊密碼元件，模板裡才能用 <password-field>
    data() {
        return {
            mode: 'login',                    // 'login' 或 'register'，控制顯示哪個分頁
            login: { email: '', password: '' },
            reg: {                            // 欄位名對齊後端 UserRegisterRequest
                email: '', password: '', userName: '', userNickname: '',
                userPhoneNum: '', userAddress: '', districtId: null, birthday: null,
            },
            loading: false, errorMsg: '', okMsg: '',
        };
    },
    methods: {
        // 切換分頁時順手清掉訊息
        switchMode(m) { this.mode = m; this.errorMsg = ''; this.okMsg = ''; },

        // ===== 帳密登入 =====
        async onLogin() {
            this.errorMsg = ''; this.loading = true;
            try {
                // store.memberLogin 內部登入後會再打一次 /me，確保拿到完整資料（含消費級距）
                await store.memberLogin(this.login.email, this.login.password);
                navigate('/member/me');
            } catch (e) {
                // 後端：401=帳密錯、409=帳號狀態異常
                this.errorMsg = e.status === 401 ? '帳號或密碼錯誤'
                    : e.status === 409 ? '帳號狀態異常（可能已被停權）'
                        : '登入失敗，請稍後再試';
            } finally { this.loading = false; }
        },

        // ===== 註冊 =====
        async onRegister() {
            this.errorMsg = ''; this.okMsg = '';
            // 前端先做基本驗證（體驗用，真正的把關在後端 DTO 的 @Size）
            if ((this.reg.password || '').length < 8) { this.errorMsg = '密碼至少 8 碼'; return; }
            this.loading = true;
            try {
                // 生日空字串要轉成 null，避免後端解析失敗
                const payload = { ...this.reg, birthday: this.reg.birthday || null };
                await api.post('/member/register', payload);
                this.okMsg = '註冊成功！請用剛剛的帳密登入。';
                this.login.email = this.reg.email;          // 順手帶到登入分頁
                setTimeout(() => this.switchMode('login'), 900);
            } catch (e) {
                // 後端：409=Email 重複、400=欄位驗證失敗（例如密碼太短）
                this.errorMsg = e.status === 409 ? 'Email 已被使用'
                    : e.status === 400 ? '欄位格式不符（例如密碼至少 8 碼）'
                        : '註冊失敗，請檢查欄位';
            } finally { this.loading = false; }
        },

        // ===== Google 登入 =====
        // 使用者用 Google 登入成功後，Google 會自動呼叫這個函式；response.credential 就是 idToken。
        async handleCredential(response) {
            this.errorMsg = '';
            try {
                const res = await fetch('/api/member/oauth/google', {
                    method: 'POST', credentials: 'include',
                    headers: { 'Content-Type': 'application/json' },
                    body: JSON.stringify({ idToken: response.credential }),   // 欄位名一字不差：idToken
                });
                if (!res.ok) throw new Error('google login failed');
                store.member.profile = await res.json();
                store.member.loaded = true;
                navigate('/member/me');
            } catch (e) { this.errorMsg = 'Google 登入失敗，請稍後再試'; }
        },

        // 等 Google 函式庫(gsi/client)載入好，初始化並把官方按鈕畫到 ref="gbtn" 的 div。
        setupGoogle() {
            if (!window.google || !window.google.accounts) { setTimeout(this.setupGoogle, 300); return; }
            window.google.accounts.id.initialize({ client_id: GOOGLE_CLIENT_ID, callback: this.handleCredential });
            if (this.$refs.gbtn) {
                window.google.accounts.id.renderButton(this.$refs.gbtn,
                    { type: 'standard', theme: 'outline', size: 'large', width: 320 });
            }
        },
    },

    mounted() { this.setupGoogle(); },
    // 從註冊分頁切回登入分頁時，Google 按鈕的 div 才重新出現 → 再渲染一次
    updated() { if (this.mode === 'login') this.setupGoogle(); },

    template: `
    <div class="auth-wrap">
      <div class="auth-card">
        <div class="auth-brand">
          <span class="logo">
            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
              <path d="M7 20h10"/><path d="M10 20c5.5-2.5.8-6.4 3-10"/>
              <path d="M9.5 9.4c1.1.8 1.8 2.2 2.3 3.7-2 .4-3.5.4-4.8-.3-1.2-.6-2.3-1.9-3-4.2 2.8-.5 4.4 0 5.5.8z"/>
              <path d="M14.1 6a7 7 0 0 0-1.1 4c1.9-.1 3.3-.6 4.3-1.4 1-1 1.6-2.3 1.7-4.6-2.7.1-4 1-4.9 2z"/>
            </svg>
          </span>
          <h2>歡迎來到 Farmily</h2>
          <p class="sub">登入或註冊，開始選購當季小農鮮貨</p>
        </div>
        <div class="tabs">
          <button class="tab" :class="{active: mode==='login'}" @click="switchMode('login')">登入</button>
          <button class="tab" :class="{active: mode==='register'}" @click="switchMode('register')">註冊</button>
        </div>

        <p v-if="errorMsg" class="err">{{ errorMsg }}</p>
        <p v-if="okMsg" class="ok">{{ okMsg }}</p>

        <!-- 登入分頁 -->
        <form v-if="mode==='login'" class="form-grid" @submit.prevent="onLogin">
          <div class="field"><label>Email</label><input v-model="login.email" type="email" required /></div>
          <div class="field"><label>密碼</label><password-field v-model="login.password" placeholder="密碼"></password-field></div>
          <button class="btn block" type="submit" :disabled="loading">{{ loading ? '登入中…' : '登入' }}</button>

          <div class="divider">或</div>
          <div ref="gbtn" style="display:flex;justify-content:center"></div>
          <p class="hint">Google 登入成功後會送 idToken 到後端驗證</p>
        </form>

        <!-- 註冊分頁 -->
        <form v-else class="form-grid" @submit.prevent="onRegister">
          <div class="field"><label>Email *</label><input v-model="reg.email" type="email" required /></div>
          <div class="field"><label>密碼 *</label><password-field v-model="reg.password" placeholder="至少 8 碼"></password-field></div>
          <div class="field"><label>姓名 *</label><input v-model="reg.userName" required /></div>
          <div class="field"><label>暱稱</label><input v-model="reg.userNickname" /></div>
          <div class="field"><label>手機</label><input v-model="reg.userPhoneNum" /></div>
          <div class="field"><label>地址</label><input v-model="reg.userAddress" /></div>
          <div class="field"><label>所在地區</label><district-picker v-model="reg.districtId"></district-picker></div>
          <div class="field"><label>生日</label><input v-model="reg.birthday" type="date" /></div>
          <button class="btn block" type="submit" :disabled="loading">{{ loading ? '送出中…' : '註冊' }}</button>
        </form>
      </div>
    </div>
  `,
};
