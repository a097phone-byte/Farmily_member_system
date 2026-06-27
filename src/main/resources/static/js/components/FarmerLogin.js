import { store } from '../store.js';
import { api } from '../api.js';
import { navigate } from '../router.js';
import { toast } from '../ui.js';
import PasswordField from './PasswordField.js';
import DistrictPicker from './DistrictPicker.js';
import FileDrop from './FileDrop.js';

function fileToBase64(file) {
    return new Promise((resolve, reject) => {
        const reader = new FileReader();
        reader.onload = () => resolve(String(reader.result).split(',')[1]);
        reader.onerror = reject;
        reader.readAsDataURL(file);
    });
}

export default {
    name: 'FarmerLogin',
    components: { PasswordField, DistrictPicker, FileDrop },
    data() {
        return {
            mode: 'login',
            submitted: false,                   // ← 申請成功後切到「成功畫面」
            login: { email: '', password: '', rememberMe: false },
            apply: {
                email: '', password: '', farmName: '', farmAddress: '',
                districtId: null, farmerPhoneNum: '', farmDesc: '', locLat: null, locLong: null,
            },
            files: { certFileLand: null, certFileProduct: null, certFileIdentity: null },
            // 查詢進度 / 補件（免登入，給尚未通過審核的 PENDING 申請者）
            track: { email: '', password: '' },
            reviewInfo: null,                   // 後端 FarmerReviewResponse
            showResubmit: false,
            resubmit: { farmName: '', farmAddress: '', districtId: null, locLat: null, locLong: null },
            trackFiles: { certFileLand: null, certFileProduct: null, certFileIdentity: null },
            loading: false, errorMsg: '', okMsg: '',
        };
    },
    computed: {
        reviewBadge() { return 'badge s-' + String(this.reviewInfo && this.reviewInfo.reviewStatus || 'review').toLowerCase(); },
        isApproved() { return this.reviewInfo && String(this.reviewInfo.reviewStatus).toUpperCase() === 'APPROVED'; },
        isRejected() { return this.reviewInfo && String(this.reviewInfo.reviewStatus).toUpperCase() === 'REJECTED'; },
        // 已核准且已完成 Email 驗證 → 可直接登入；未驗證 → 需先點啟用連結
        isVerified() { return this.reviewInfo && this.reviewInfo.emailVerified === true; },
    },
    methods: {
        switchMode(m) { this.mode = m; this.errorMsg = ''; this.okMsg = ''; },

        async onLogin() {
            this.errorMsg = ''; this.loading = true;
            try {
                await store.farmerLogin(this.login.email, this.login.password, this.login.rememberMe);
                navigate('/farmer/home');
            } catch (e) {
                this.errorMsg = e.status === 401 ? '帳號或密碼錯誤'
                    : e.status === 409 ? '無法登入：帳號尚未通過審核、尚未完成 Email 驗證，或已停權。通過審核後請先點擊啟用信連結完成驗證' : '登入失敗';
            } finally { this.loading = false; }
        },

        grabLocation(target) {
            const t = target === 'resubmit' ? this.resubmit : this.apply;
            if (!navigator.geolocation) { toast('此瀏覽器不支援定位', 'warn'); return; }
            navigator.geolocation.getCurrentPosition((pos) => {
                t.locLat = pos.coords.latitude.toFixed(8);
                t.locLong = pos.coords.longitude.toFixed(8);
            });
        },

        // 查詢最新審核進度（免登入：email + 密碼驗身）
        async onTrack() {
            this.errorMsg = ''; this.okMsg = ''; this.loading = true;
            try {
                this.reviewInfo = await api.post('/farmer/application/status', {
                    email: this.track.email, password: this.track.password,
                });
                this.showResubmit = false;
            } catch (e) {
                this.reviewInfo = null;
                this.errorMsg = e.status === 401 ? '帳號或密碼錯誤'
                    : e.status === 404 ? '查無審核紀錄'
                    : '查詢失敗，請稍後再試';
            } finally { this.loading = false; }
        },

        // 重寄啟用信：已核准但未驗證、連結過期或沒收到時使用（沿用查詢時輸入的 email + 密碼驗身）
        async onResendActivation() {
            this.errorMsg = ''; this.okMsg = ''; this.loading = true;
            try {
                await api.post('/farmer/application/resend-activation', {
                    email: this.track.email, password: this.track.password,
                });
                toast('已重寄啟用信，請至信箱點擊連結完成 Email 驗證');
            } catch (e) {
                this.errorMsg = e.status === 401 ? '帳號或密碼錯誤'
                    : e.status === 409 ? '此帳號已完成驗證或尚未通過審核'
                    : '重寄失敗，請稍後再試';
            } finally { this.loading = false; }
        },

        // 打開補件表單：用上一輪提交的快照預填（地區無法預填，需重選）
        openResubmit() {
            const r = this.reviewInfo || {};
            this.resubmit = {
                farmName: r.submittedFarmName || '', farmAddress: r.submittedFarmAddress || '',
                districtId: null, locLat: r.submittedLocLat || null, locLong: r.submittedLocLong || null,
            };
            this.trackFiles = { certFileLand: null, certFileProduct: null, certFileIdentity: null };
            this.showResubmit = true;
        },

        // 重新送審（免登入）
        async onResubmit() {
            this.errorMsg = '';
            if (!this.resubmit.farmName || !this.resubmit.farmAddress) { this.errorMsg = '請填寫農場名稱與地址'; return; }
            this.loading = true;
            try {
                const payload = {
                    email: this.track.email, password: this.track.password,
                    ...this.resubmit,
                };
                if (this.trackFiles.certFileLand)     payload.certFileLand     = await fileToBase64(this.trackFiles.certFileLand);
                if (this.trackFiles.certFileProduct)  payload.certFileProduct  = await fileToBase64(this.trackFiles.certFileProduct);
                if (this.trackFiles.certFileIdentity) payload.certFileIdentity = await fileToBase64(this.trackFiles.certFileIdentity);

                this.reviewInfo = await api.post('/farmer/application/resubmit', payload);  // 回最新 FarmerReviewResponse
                this.showResubmit = false;
                toast('已重新送審，請等待管理員審核');
            } catch (e) {
                this.errorMsg = e.status === 401 ? '帳號或密碼錯誤'
                    : e.status === 409 ? '此帳號已通過審核，請直接登入'
                    : e.status === 404 ? '查無所選地區，請重新選擇'
                    : '送出失敗，請檢查欄位';
            } finally { this.loading = false; }
        },

        resetApplyForm() {
            this.apply = { email: '', password: '', farmName: '', farmAddress: '',
                districtId: null, farmerPhoneNum: '', farmDesc: '', locLat: null, locLong: null };
            this.files = { certFileLand: null, certFileProduct: null, certFileIdentity: null };
        },

        async onApply() {
            this.errorMsg = '';
            // 前端先做基本驗證（體驗用，真正把關在後端）
            if ((this.apply.password || '').length < 8) { this.errorMsg = '密碼至少 8 碼'; return; }
            this.loading = true;
            try {
                const payload = { ...this.apply };
                if (this.files.certFileLand)     payload.certFileLand     = await fileToBase64(this.files.certFileLand);
                if (this.files.certFileProduct)  payload.certFileProduct  = await fileToBase64(this.files.certFileProduct);
                if (this.files.certFileIdentity) payload.certFileIdentity = await fileToBase64(this.files.certFileIdentity);

                await api.post('/farmer/register', payload);

                // ✅ 成功：toast 告知，再清空表單、切到成功畫面
                toast('申請已送出！請等待管理員審核');
                this.resetApplyForm();
                this.submitted = true;
            } catch (e) {
                this.errorMsg = e.status === 409 ? 'Email 已被使用'
                    : e.status === 400 ? '欄位格式不符（例如密碼至少 8 碼）'
                        : '申請送出失敗，請檢查欄位';
            } finally { this.loading = false; }
        },
    },
    template: `
    <div class="auth-wrap">
      <div class="auth-card">

        <!-- 申請成功畫面：取代表單，明確告知要等審核 -->
        <div v-if="submitted" style="text-align:center">
          <h2>申請已送出</h2>
          <p class="muted">你的小農申請已送出，請等待管理員審核。<br>審核通過後，我們會寄出「啟用信」，請點擊信中連結完成 Email 驗證，<br>完成後即可用 Email 與密碼登入。</p>
          <button class="btn block" @click="submitted=false; switchMode('track')">查詢審核進度</button>
          <button class="btn block outline" style="margin-top:8px" @click="submitted=false; switchMode('login')">回到登入</button>
          <p class="hint" style="margin-top:12px"><a href="#/">回首頁</a></p>
        </div>

        <!-- 正常的 登入 / 申請 分頁 -->
        <div v-else>
          <div class="auth-brand">
            <span class="logo">
              <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                <path d="M7 20h10"/><path d="M10 20c5.5-2.5.8-6.4 3-10"/>
                <path d="M9.5 9.4c1.1.8 1.8 2.2 2.3 3.7-2 .4-3.5.4-4.8-.3-1.2-.6-2.3-1.9-3-4.2 2.8-.5 4.4 0 5.5.8z"/>
                <path d="M14.1 6a7 7 0 0 0-1.1 4c1.9-.1 3.3-.6 4.3-1.4 1-1 1.6-2.3 1.7-4.6-2.7.1-4 1-4.9 2z"/>
              </svg>
            </span>
            <h2>小農專區</h2>
            <p class="sub">登入、申請加入或查詢審核進度</p>
          </div>
          <div class="tabs">
            <button class="tab" :class="{active: mode==='login'}" @click="switchMode('login')">登入</button>
            <button class="tab" :class="{active: mode==='apply'}" @click="switchMode('apply')">申請加入</button>
            <button class="tab" :class="{active: mode==='track'}" @click="switchMode('track')">查詢進度</button>
          </div>

          <p v-if="errorMsg" class="err">{{ errorMsg }}</p>
          <p v-if="okMsg" class="ok">{{ okMsg }}</p>

          <form v-if="mode==='login'" class="form-grid" @submit.prevent="onLogin">
            <div class="field"><label>Email</label><input v-model="login.email" type="email" required /></div>
            <div class="field"><label>密碼</label><password-field v-model="login.password" placeholder="密碼"></password-field></div>
            <label class="remember"><input type="checkbox" v-model="login.rememberMe" /> 記住我</label>
            <button class="btn block" type="submit" :disabled="loading">{{ loading ? '登入中…' : '登入' }}</button>
            <p class="hint" style="text-align:center">
              <a href="#/forgot-password">忘記密碼？</a>
            </p>
            <p class="hint">尚未通過審核或被退件無法登入</p>
          </form>

          <form v-else-if="mode==='apply'" class="form-grid" @submit.prevent="onApply">
            <div class="field"><label>Email *</label><input v-model="apply.email" type="email" required /></div>
            <div class="field"><label>密碼 *</label><password-field v-model="apply.password" placeholder="至少 8 碼"></password-field></div>
            <div class="field"><label>農場名稱 *</label><input v-model="apply.farmName" required /></div>
            <div class="field"><label>農場地址 *</label><input v-model="apply.farmAddress" required /></div>
            <div class="field"><label>所在地區</label><district-picker v-model="apply.districtId"></district-picker></div>
            <div class="field"><label>聯絡電話 *</label><input v-model="apply.farmerPhoneNum" required /></div>
            <div class="field"><label>農場介紹 *</label><textarea v-model="apply.farmDesc" rows="3" required></textarea></div>
            <div class="field"><label>緯度 / 經度</label>
              <div class="inline-fields">
                <input v-model="apply.locLat" placeholder="緯度" />
                <input v-model="apply.locLong" placeholder="經度" />
                <button type="button" class="btn outline sm" @click="grabLocation('apply')">自動抓取</button>
              </div>
            </div>
            <div class="field"><label>土地證明</label><file-drop v-model="files.certFileLand" label="土地證明"></file-drop></div>
            <div class="field"><label>產品證明</label><file-drop v-model="files.certFileProduct" label="產品證明"></file-drop></div>
            <div class="field"><label>身分證明</label><file-drop v-model="files.certFileIdentity" label="身分證明"></file-drop></div>
            <button class="btn block" type="submit" :disabled="loading">{{ loading ? '送出中…' : '送出申請' }}</button>
          </form>

          <!-- 查詢進度 / 補件：給尚未通過審核、無法登入的申請者 -->
          <div v-else-if="mode==='track'">
            <form class="form-grid" @submit.prevent="onTrack">
              <p class="hint">尚未通過審核無法登入，可在此用 Email 與密碼查詢進度或補件</p>
              <div class="field"><label>Email</label><input v-model="track.email" type="email" required /></div>
              <div class="field"><label>密碼</label><password-field v-model="track.password" placeholder="密碼"></password-field></div>
              <button class="btn block" type="submit" :disabled="loading">{{ loading ? '查詢中…' : '查詢進度' }}</button>
            </form>

            <!-- 查詢結果 -->
            <div v-if="reviewInfo" class="card" style="margin-top:16px">
              <div class="card-body">
                <div class="rw"><span class="k">目前狀態</span><span><span :class="reviewBadge">{{ reviewInfo.reviewStatus || '—' }}</span></span></div>
                <div class="rw"><span class="k">審核輪次</span><span>第 {{ reviewInfo.reviewRound || '—' }} 輪</span></div>
                <div v-if="reviewInfo.rejectReason" class="rw"><span class="k">退件理由</span><span>{{ reviewInfo.rejectReason }}</span></div>
              </div>
              <div class="card-foot" style="flex-direction:column;align-items:flex-start;gap:6px">
                <template v-if="isApproved">
                  <!-- 已核准且完成驗證：可直接登入 -->
                  <p v-if="isVerified" class="ok" style="margin:0">已通過審核且完成 Email 驗證，請回「登入」分頁直接登入。</p>
                  <!-- 已核准但尚未驗證：提示點啟用信，並提供重寄 -->
                  <template v-else>
                    <p class="ok" style="margin:0">已通過審核！請至信箱點擊「啟用連結」完成 Email 驗證後，再回「登入」分頁登入。</p>
                    <button class="btn outline" :disabled="loading" @click="onResendActivation">沒收到或連結過期？重寄啟用信</button>
                    <p class="muted" style="margin:0">若已完成 Email 驗證仍無法順利登入，請聯繫客服：supportFarmily@gmail.com</p>
                  </template>
                </template>
                <button v-else-if="isRejected && !showResubmit" class="btn outline" @click="openResubmit">重新送審 / 補件</button>
                <p v-else-if="!showResubmit" class="muted">審核中，請耐心等候審核結果，退件後才可重新送審。</p>
              </div>
            </div>

            <!-- 補件表單：用與申請表單一致的單欄結構，避免在窄版登入卡溢位 -->
            <form v-if="reviewInfo && showResubmit && isRejected" class="form-grid" @submit.prevent="onResubmit" style="margin-top:16px">
              <div>
                <h3 style="margin:0 0 2px">重新送審</h3>
                <p class="hint" style="margin:0">修改後重新提交，會新增一輪審核</p>
              </div>
              <div class="field"><label>農場名稱</label><input v-model="resubmit.farmName" /></div>
              <div class="field"><label>所在地區 <small class="muted">需重新選擇</small></label><district-picker v-model="resubmit.districtId"></district-picker></div>
              <div class="field"><label>農場地址</label><input v-model="resubmit.farmAddress" /></div>
              <div class="field"><label>緯度 / 經度</label>
                <div class="inline-fields">
                  <input v-model="resubmit.locLat" placeholder="緯度" />
                  <input v-model="resubmit.locLong" placeholder="經度" />
                  <button type="button" class="btn outline sm" @click="grabLocation('resubmit')">自動抓取</button>
                </div>
              </div>
              <div class="field"><label>土地證明 <small class="muted">未選＝沿用上一輪</small></label><file-drop v-model="trackFiles.certFileLand" label="土地證明"></file-drop></div>
              <div class="field"><label>產品證明</label><file-drop v-model="trackFiles.certFileProduct" label="產品證明"></file-drop></div>
              <div class="field"><label>身分證明</label><file-drop v-model="trackFiles.certFileIdentity" label="身分證明"></file-drop></div>
              <div class="inline-fields" style="justify-content:flex-end">
                <button type="button" class="btn ghost" :disabled="loading" @click="showResubmit=false">取消</button>
                <button class="btn" type="submit" :disabled="loading">{{ loading ? '送出中…' : '送出審核' }}</button>
              </div>
            </form>
          </div>
        </div>

      </div>
    </div>
  `,
};
