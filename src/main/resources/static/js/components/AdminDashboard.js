// 後台總覽：統計卡片（依權限載入數量）+ 權限資訊 + 改自己名字。
import { store } from '../store.js';
import { api } from '../api.js';
import { navigate } from '../router.js';
import { toast } from '../ui.js';
import Icon from './Icon.js';

export default {
    name: 'AdminDashboard',
    components: { Icon },
    data() {
        return { store, newName: '', saving: false, stats: { members: null, farmers: null, pending: null } };
    },
    computed: {
        p() { return this.store.admin.profile || {}; },
        isSuper() { return this.store.adminHasPerm('ADMIN'); },
        canMember() { return this.store.adminHasPerm('MEMBER'); },
        canFarmer() { return this.store.adminHasPerm('FARMER'); },
        // 統計卡片（只放有權限看的）
        cards() {
            const c = [];
            if (this.canMember) c.push({ key: 'members', label: '會員總數', icon: 'users', value: this.stats.members, path: '/members' });
            if (this.canFarmer) c.push({ key: 'farmers', label: '小農總數', icon: 'leaf', value: this.stats.farmers, path: '/farmers' });
            if (this.canFarmer) c.push({ key: 'pending', label: '待審件', icon: 'clipboard', value: this.stats.pending, path: '/reviews' });
            return c;
        },
    },
    async mounted() {
        this.newName = this.store.admin.profile ? this.store.admin.profile.adminName : '';
        // 依權限載入數量（沒權限的就不打，避免 403）
        if (this.canMember) api.get('/admin/members').then((r) => { this.stats.members = r.length; }).catch(() => {});
        if (this.canFarmer) {
            api.get('/admin/farmers').then((r) => { this.stats.farmers = r.length; }).catch(() => {});
            api.get('/admin/reviews/pending').then((r) => { this.stats.pending = r.length; }).catch(() => {});
        }
    },
    methods: {
        navigate,
        async saveName() {
            this.saving = true;
            try { await store.adminUpdateName(this.newName); toast('名稱已更新'); }
            catch (e) { toast(e.message || '更新失敗', 'err'); }
            finally { this.saving = false; }
        },
    },
    template: `
    <div>
      <div class="page-head"><h2>總覽</h2></div>

      <div class="entry-grid" v-if="cards.length">
        <div class="card" v-for="c in cards" :key="c.key" style="cursor:pointer" @click="navigate(c.path)">
          <div class="card-body" style="display:flex;align-items:center;gap:14px">
            <span class="avatar"><icon :name="c.icon"></icon></span>
            <div>
              <div style="font-size:26px;font-weight:700">{{ c.value === null ? '…' : c.value }}</div>
              <div class="muted">{{ c.label }}</div>
            </div>
          </div>
        </div>
      </div>

      <div class="card">
        <div class="card-head"><h3>我的帳號</h3></div>
        <div class="card-body">
          <div class="rw"><span class="k">名稱 / Email</span><span>{{ p.adminName }}（{{ p.adminEmail }}）</span></div>
          <div class="rw"><span class="k">狀態</span><span><span class="badge dot s-active">{{ p.adminStatus }}</span></span></div>
          <div class="rw"><span class="k">權限</span><span>
            <span v-for="c in p.permissionCodes" :key="c" class="pill">{{ c }}</span>
            <span v-if="isSuper" class="pill super">超級管理員</span>
          </span></div>
        </div>
      </div>

      <div class="card">
        <div class="card-head"><h3>修改我的名稱</h3></div>
        <div class="card-body">
          <div class="form-rows"><div class="form-row"><div class="lbl">名稱</div><input v-model="newName" /></div></div>
        </div>
        <div class="card-foot"><button class="btn" :disabled="saving" @click="saveName">{{ saving ? '儲存中…' : '儲存' }}</button></div>
      </div>
    </div>
  `,
};
