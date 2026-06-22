// 小農「農場資料」分頁：唯讀農場資訊 + 可即時編輯的聯絡資訊（電話/描述）。
// 串接：PUT /api/farmer/me（store.farmerUpdateContact，欄位=FarmerProfileUpdateRequest）。
// 農場名稱/地址/座標/證明文件屬「審核欄位」，改它＝重新送審，移到「審核狀態」分頁處理。
import { store } from '../store.js';
import { toast } from '../ui.js';
import Avatar from './Avatar.js';

export default {
    name: 'FarmerBasicInfo',
    components: { Avatar },
    data() { return { store, edit: { farmerPhoneNum: '', farmDesc: '' }, saving: false }; },
    computed: { p() { return this.store.farmer.profile || {}; } },
    mounted() {
        const p = this.store.farmer.profile;
        if (p) { this.edit.farmerPhoneNum = p.farmerPhoneNum; this.edit.farmDesc = p.farmDesc; }
    },
    methods: {
        async save() {
            this.saving = true;
            try { await store.farmerUpdateContact({ ...this.edit }); toast('已更新'); }
            catch (e) { toast(e.message || '更新失敗', 'err'); }
            finally { this.saving = false; }
        },
        badge(s) { return 'badge dot s-' + String(s || '').toLowerCase(); },
    },
    template: `
    <div>
      <div class="card">
        <div class="card-body" style="display:flex;align-items:center;gap:16px">
          <avatar :name="p.farmName" size="lg"></avatar>
          <div style="flex:1;min-width:0">
            <h3 style="margin:0">{{ p.farmName }}</h3>
            <div class="muted">{{ p.email }}</div>
          </div>
          <span :class="badge(p.farmerStatus)">{{ p.farmerStatus }}</span>
        </div>
      </div>

      <div class="card">
        <div class="card-head"><h3>農場資訊</h3><div class="sub">名稱／地址／座標屬審核欄位，需到「審核狀態」重新送審才能改</div></div>
        <div class="card-body">
          <div class="rw"><span class="k">農場地址</span><span>{{ p.cityName }}{{ p.distName }} {{ p.farmAddress }}</span></div>
          <div class="rw"><span class="k">座標</span><span>{{ p.locLat || '—' }}, {{ p.locLong || '—' }}</span></div>
          <div class="rw"><span class="k">最新審核</span><span><span class="badge s-review">{{ p.reviewStatus || '—' }}</span>（第 {{ p.reviewRound || '—' }} 輪）</span></div>
        </div>
      </div>

      <div class="card">
        <div class="card-head"><h3>聯絡資訊</h3><div class="sub">這些欄位即時生效，不需重新送審</div></div>
        <div class="card-body">
          <div class="form-rows">
            <div class="form-row"><div class="lbl">聯絡電話</div><input v-model="edit.farmerPhoneNum" /></div>
            <div class="form-row"><div class="lbl">農場描述</div><textarea v-model="edit.farmDesc" rows="3"></textarea></div>
          </div>
        </div>
        <div class="card-foot">
          <button class="btn" :disabled="saving" @click="save">{{ saving ? '儲存中…' : '儲存' }}</button>
        </div>
      </div>
    </div>
  `,
};
