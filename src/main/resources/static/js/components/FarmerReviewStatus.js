// 小農「審核狀態」分頁：顯示最新審核狀態/輪次；提供「重新送審」表單（改審核欄位）。
// 串接：PUT /api/farmer/me/application（FarmerResubmitRequest）；成功後更新 store.farmer.profile。
import { store } from '../store.js';
import { api } from '../api.js';
import { toast, confirmDialog } from '../ui.js';
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
    name: 'FarmerReviewStatus',
    components: { DistrictPicker, FileDrop },
    data() {
        return {
            store, showForm: false, saving: false,
            // 對齊 FarmerResubmitRequest
            form: { farmName: '', districtId: null, farmAddress: '', locLat: null, locLong: null },
            files: { certFileLand: null, certFileProduct: null, certFileIdentity: null },
        };
    },
    computed: {
        p() { return this.store.farmer.profile || {}; },
        isRejected() { return String(this.p.reviewStatus).toUpperCase() === 'REJECTED'; },
        isApproved() { return String(this.p.reviewStatus).toUpperCase() === 'APPROVED'; },
        badge() { return 'badge s-' + String(this.p.reviewStatus || 'review').toLowerCase(); },
    },
    methods: {
        openForm() {
            const p = this.p;
            this.form = { farmName: p.farmName, districtId: p.districtId, farmAddress: p.farmAddress, locLat: p.locLat, locLong: p.locLong };
            this.files = { certFileLand: null, certFileProduct: null, certFileIdentity: null };
            this.showForm = true;
        },
        async submit() {
            const ok = await confirmDialog({
                title: '送出修改申請',
                message: '確定送出修改申請？送出後會新增一輪審核，需等待管理員重新審核。',
                confirmText: '確認送出',
            });
            if (!ok) return;
            this.saving = true;
            try {
                const payload = { ...this.form };
                if (this.files.certFileLand)     payload.certFileLand     = await fileToBase64(this.files.certFileLand);
                if (this.files.certFileProduct)  payload.certFileProduct  = await fileToBase64(this.files.certFileProduct);
                if (this.files.certFileIdentity) payload.certFileIdentity = await fileToBase64(this.files.certFileIdentity);
                const updated = await api.put('/farmer/me/application', payload);
                this.store.farmer.profile = updated;   // 後端回最新 FarmerProfileResponse
                toast('已重新送審，請等待管理員審核');
                this.showForm = false;
            } catch (e) {
                toast(e.message || '送審失敗，請檢查欄位', 'err');
            } finally { this.saving = false; }
        },
    },
    template: `
    <div>
      <div class="card">
        <div class="card-head"><h3>審核狀態</h3><div class="sub">你的小農申請審核進度</div></div>
        <div class="card-body">
          <div class="rw"><span class="k">目前狀態</span><span><span :class="badge">{{ p.reviewStatus || '—' }}</span></span></div>
          <div class="rw"><span class="k">審核輪次</span><span>第 {{ p.reviewRound || '—' }} 輪</span></div>
          <div class="rw"><span class="k">帳號狀態</span><span>{{ p.farmerStatus }}</span></div>
        </div>
        <div class="card-foot" style="justify-content:flex-start">
          <button v-if="!showForm && isRejected" class="btn outline" @click="openForm">重新送審 / 修改審核欄位</button>
          <p v-else-if="!showForm && isApproved" class="ok" style="margin:0">已通過審核，帳號已啟用，可正常使用。</p>
          <p v-else-if="!showForm" class="muted">審核中，請耐心等候審核結果，退件後才可重新送審。</p>
        </div>
      </div>

      <div v-if="showForm" class="card">
        <div class="card-head"><h3>重新送審</h3><div class="sub">修改農場名稱／地址／座標／證明文件後重新提交，會新增一輪審核</div></div>
        <div class="card-body">
          <div class="form-rows">
            <div class="form-row"><div class="lbl">農場名稱</div><input v-model="form.farmName" /></div>
            <div class="form-row"><div class="lbl">所在地區</div><district-picker v-model="form.districtId"></district-picker></div>
            <div class="form-row"><div class="lbl">農場地址</div><input v-model="form.farmAddress" /></div>
            <div class="form-row"><div class="lbl">緯度 / 經度</div>
              <div class="inline-fields"><input v-model="form.locLat" placeholder="緯度" /><input v-model="form.locLong" placeholder="經度" /></div>
            </div>
            <div class="form-row"><div class="lbl">土地證明 <small>未選＝沿用上一輪</small></div><file-drop v-model="files.certFileLand" label="土地證明"></file-drop></div>
            <div class="form-row"><div class="lbl">產品證明</div><file-drop v-model="files.certFileProduct" label="產品證明"></file-drop></div>
            <div class="form-row"><div class="lbl">身分證明</div><file-drop v-model="files.certFileIdentity" label="身分證明"></file-drop></div>
          </div>
        </div>
        <div class="card-foot">
          <button class="btn ghost" :disabled="saving" @click="showForm=false">取消</button>
          <button class="btn" :disabled="saving" @click="submit">{{ saving ? '送出中…' : '送出審核' }}</button>
        </div>
      </div>
    </div>
  `,
};
