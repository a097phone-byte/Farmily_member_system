// 管理員管理：列表 / 新增 / 編輯（含權限、狀態）/ 刪除（軟刪除）。
// 串接後端（皆已存在）：
//   GET    /api/admin/admins         列表
//   GET    /api/admin/admins/{id}    單一（含權限）
//   POST   /api/admin/admins         body: AdminCreateRequest { email, password, name, permissionCodes[] }
//   PUT    /api/admin/admins/{id}    body: AdminUpdateRequest { updateName, updateStatus, updatePermissionCodes[] }
//   DELETE /api/admin/admins/{id}    後端會擋「刪自己」
import { api } from '../api.js';
import { store } from '../store.js';
import { toast, confirmDialog } from '../ui.js';
import Modal from './Modal.js';
import Avatar from './Avatar.js';

// 權限清單改由後端動態載入（GET /api/admin/permissions，讀 admin_role 表），不再寫死。
// 以後要新增權限，只要在 admin_role 表 INSERT 一列，這裡的勾選畫面就會自動出現。
const STATUSES = ['ACTIVE', 'SUSPENDED'];

export default {
    name: 'AdminAdmins',
    components: { Modal, Avatar },
    data() {
        return {
            store, rows: [], loading: true, busy: false,
            perms: [], statuses: STATUSES,   // perms 於 mounted 時向後端載入
            mode: null,                // 'create' | 'edit'
            form: {},                  // 編輯/新增共用表單
        };
    },
    computed: {
        myId() { return this.store.admin.profile ? this.store.admin.profile.adminId : null; },
    },
    async mounted() {
        await this.loadPerms();   // 先載入權限清單，勾選畫面才有選項
        await this.load();
    },
    methods: {
        // 向後端拿「所有可指派的權限」（取代原本寫死的 PERMS）
        async loadPerms() {
            try { this.perms = await api.get('/admin/permissions'); }
            catch (e) { toast('載入權限清單失敗', 'err'); }
        },
        async load() {
            this.loading = true;
            try { this.rows = await api.get('/admin/admins'); }
            catch (e) { toast('載入管理員清單失敗', 'err'); }
            finally { this.loading = false; }
        },
        startCreate() {
            this.mode = 'create';
            this.form = { email: '', password: '', name: '', permissionCodes: [] };
        },
        startEdit(r) {
            this.mode = 'edit';
            this.form = {
                adminId: r.adminId,
                updateName: r.adminName,
                updateStatus: r.adminStatus,
                updatePermissionCodes: [...(r.permissionCodes || [])],
            };
        },
        togglePerm(list, code) {
            const i = list.indexOf(code);
            if (i >= 0) list.splice(i, 1); else list.push(code);
        },
        async submitCreate() {
            if (!this.form.email || !this.form.name) { toast('Email 與名稱為必填', 'warn'); return; }
            if ((this.form.password || '').length < 8) { toast('密碼至少 8 碼', 'warn'); return; }
            this.busy = true;
            try {
                await api.post('/admin/admins', {
                    email: this.form.email, password: this.form.password,
                    name: this.form.name, permissionCodes: this.form.permissionCodes,
                });
                toast('已新增管理員');
                this.mode = null;
                await this.load();
            } catch (e) { toast(e.message || '新增失敗', 'err'); }
            finally { this.busy = false; }
        },
        async submitEdit() {
            this.busy = true;
            try {
                await api.put('/admin/admins/' + this.form.adminId, {
                    updateName: this.form.updateName,
                    updateStatus: this.form.updateStatus,
                    updatePermissionCodes: this.form.updatePermissionCodes,
                });
                toast('已更新');
                this.mode = null;
                await this.load();
            } catch (e) { toast(e.message || '更新失敗', 'err'); }
            finally { this.busy = false; }
        },
        async remove(r) {
            if (r.adminId === this.myId) { toast('不能刪除自己的帳號', 'warn'); return; }
            const ok = await confirmDialog({
                title: '刪除管理員',
                message: '確定刪除管理員「' + r.adminName + '」？此動作無法復原。',
                variant: 'danger', confirmText: '確認刪除',
            });
            if (!ok) return;
            try {
                await api.del('/admin/admins/' + r.adminId);
                toast('已刪除');
                await this.load();
            } catch (e) { toast(e.message || '刪除失敗', 'err'); }
        },
        badge(s) { return 'badge dot s-' + String(s || '').toLowerCase(); },
    },
    template: `
    <div>
      <div class="page-head">
        <h2>管理員管理</h2>
        <button class="btn sm" @click="startCreate">新增管理員</button>
      </div>

      <div class="card">
        <div v-if="loading" class="empty">載入中…</div>
        <div v-else-if="!rows.length" class="empty">沒有管理員資料</div>
        <div v-else class="table-wrap">
          <table class="table">
            <thead><tr><th>管理員</th><th>權限</th><th>狀態</th><th></th></tr></thead>
            <tbody>
              <tr v-for="r in rows" :key="r.adminId">
                <td>
                  <div class="cell-user">
                    <avatar :name="r.adminName" size="sm"></avatar>
                    <div><div class="nm">{{ r.adminName }}<span v-if="r.adminId === myId" class="me-tag">我</span></div><div class="em">{{ r.adminEmail }}</div></div>
                  </div>
                </td>
                <td><span v-for="c in r.permissionCodes" :key="c" class="pill">{{ c }}</span></td>
                <td><span :class="badge(r.adminStatus)">{{ r.adminStatus }}</span></td>
                <td>
                  <div class="row-actions">
                    <button class="btn outline sm" @click="startEdit(r)">編輯</button>
                    <button class="btn danger sm" :disabled="r.adminId === myId" @click="remove(r)">刪除</button>
                  </div>
                </td>
              </tr>
            </tbody>
          </table>
        </div>
      </div>

      <!-- 新增 -->
      <modal v-if="mode === 'create'" title="新增管理員" @close="mode=null">
        <div class="form-grid">
          <div class="field"><label>Email *</label><input v-model="form.email" type="email" /></div>
          <div class="field"><label>密碼 *</label><input v-model="form.password" type="password" placeholder="至少 8 碼" /></div>
          <div class="field"><label>名稱 *</label><input v-model="form.name" /></div>
          <div class="field">
            <label>權限</label>
            <div class="perm-list">
              <label v-for="p in perms" :key="p.code" class="perm-item" :title="p.description">
                <input type="checkbox" :checked="form.permissionCodes.includes(p.code)"
                       @change="togglePerm(form.permissionCodes, p.code)" /> {{ p.name }}
              </label>
            </div>
          </div>
        </div>
        <template #foot>
          <button class="btn ghost sm" :disabled="busy" @click="mode=null">取消</button>
          <button class="btn sm" :disabled="busy" @click="submitCreate">建立</button>
        </template>
      </modal>

      <!-- 編輯 -->
      <modal v-if="mode === 'edit'" :title="'編輯管理員 #' + form.adminId" @close="mode=null">
        <div class="form-grid">
          <div class="field"><label>名稱</label><input v-model="form.updateName" /></div>
          <div class="field"><label>狀態</label>
            <select v-model="form.updateStatus">
              <option v-for="s in statuses" :key="s" :value="s">{{ s }}</option>
            </select>
          </div>
          <div class="field">
            <label>權限</label>
            <div class="perm-list">
              <label v-for="p in perms" :key="p.code" class="perm-item" :title="p.description">
                <input type="checkbox" :checked="form.updatePermissionCodes.includes(p.code)"
                       @change="togglePerm(form.updatePermissionCodes, p.code)" /> {{ p.name }}
              </label>
            </div>
          </div>
        </div>
        <template #foot>
          <button class="btn ghost sm" :disabled="busy" @click="mode=null">取消</button>
          <button class="btn sm" :disabled="busy" @click="submitEdit">儲存</button>
        </template>
      </modal>
    </div>
  `,
};
