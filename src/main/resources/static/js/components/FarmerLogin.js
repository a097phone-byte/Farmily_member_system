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
            login: { email: '', password: '' },
            apply: {
                email: '', password: '', farmName: '', farmAddress: '',
                districtId: null, farmerPhoneNum: '', farmDesc: '', locLat: null, locLong: null,
            },
            files: { certFileLand: null, certFileProduct: null, certFileIdentity: null },
            loading: false, errorMsg: '', okMsg: '',
        };
    },
    methods: {
        switchMode(m) { this.mode = m; this.errorMsg = ''; this.okMsg = ''; },

        async onLogin() {
            this.errorMsg = ''; this.loading = true;
            try {
                await store.farmerLogin(this.login.email, this.login.password);
                navigate('/farmer/home');
            } catch (e) {
                this.errorMsg = e.status === 401 ? '帳號或密碼錯誤'
                    : e.status === 409 ? '帳號尚未通過審核或已停權' : '登入失敗';
            } finally { this.loading = false; }
        },

        grabLocation() {
            if (!navigator.geolocation) { toast('此瀏覽器不支援定位', 'warn'); return; }
            navigator.geolocation.getCurrentPosition((pos) => {
                this.apply.locLat = pos.coords.latitude.toFixed(8);
                this.apply.locLong = pos.coords.longitude.toFixed(8);
            });
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
          <p class="muted">你的小農申請已送出，請等待管理員審核。<br>審核通過後就能用 Email 與密碼登入。</p>
          <button class="btn block" @click="submitted=false; switchMode('login')">回到登入</button>
          <p class="hint" style="margin-top:12px"><a href="#/">回首頁</a></p>
        </div>

        <!-- 正常的 登入 / 申請 分頁 -->
        <div v-else>
          <h2>小農</h2>
          <div class="tabs">
            <button class="tab" :class="{active: mode==='login'}" @click="switchMode('login')">登入</button>
            <button class="tab" :class="{active: mode==='apply'}" @click="switchMode('apply')">申請加入</button>
          </div>

          <p v-if="errorMsg" class="err">{{ errorMsg }}</p>
          <p v-if="okMsg" class="ok">{{ okMsg }}</p>

          <form v-if="mode==='login'" class="form-grid" @submit.prevent="onLogin">
            <div class="field"><label>Email</label><input v-model="login.email" type="email" required /></div>
            <div class="field"><label>密碼</label><password-field v-model="login.password" placeholder="密碼"></password-field></div>
            <button class="btn block" type="submit" :disabled="loading">{{ loading ? '登入中…' : '登入' }}</button>
            <p class="hint">尚未通過審核或被退件無法登入</p>
          </form>

          <form v-else class="form-grid" @submit.prevent="onApply">
            <div class="field"><label>Email *</label><input v-model="apply.email" type="email" required /></div>
            <div class="field"><label>密碼 *</label><password-field v-model="apply.password" placeholder="至少 8 碼"></password-field></div>
            <div class="field"><label>農場名稱 *</label><input v-model="apply.farmName" required /></div>
            <div class="field"><label>農場地址 *</label><input v-model="apply.farmAddress" required /></div>
            <div class="field"><label>所在地區</label><district-picker v-model="apply.districtId"></district-picker></div>
            <div class="field"><label>聯絡電話</label><input v-model="apply.farmerPhoneNum" /></div>
            <div class="field"><label>農場介紹</label><textarea v-model="apply.farmDesc" rows="3"></textarea></div>
            <div class="field"><label>緯度 / 經度</label>
              <div class="inline-fields">
                <input v-model="apply.locLat" placeholder="緯度" />
                <input v-model="apply.locLong" placeholder="經度" />
                <button type="button" class="btn outline sm" @click="grabLocation">自動抓取</button>
              </div>
            </div>
            <div class="field"><label>土地證明</label><file-drop v-model="files.certFileLand" label="土地證明"></file-drop></div>
            <div class="field"><label>產品證明</label><file-drop v-model="files.certFileProduct" label="產品證明"></file-drop></div>
            <div class="field"><label>身分證明</label><file-drop v-model="files.certFileIdentity" label="身分證明"></file-drop></div>
            <button class="btn block" type="submit" :disabled="loading">{{ loading ? '送出中…' : '送出申請' }}</button>
          </form>
        </div>

      </div>
    </div>
  `,
};
