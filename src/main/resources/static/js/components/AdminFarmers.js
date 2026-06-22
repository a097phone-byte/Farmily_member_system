// 小農管理：列表 / 搜尋 / 分頁 / 查看明細 / 停權 / 恢復。
// 串接後端（皆已存在）：
//   GET  /api/admin/farmers              列表
//   GET  /api/admin/farmers/{id}         單一小農
//   PUT  /api/admin/farmers/{id}/suspend   停權
//   PUT  /api/admin/farmers/{id}/reinstate 恢復
import { api } from '../api.js';
import { toast, confirmDialog } from '../ui.js';
import Modal from './Modal.js';
import Avatar from './Avatar.js';
import Icon from './Icon.js';

const PAGE_SIZE = 10;

export default {
    name: 'AdminFarmers',
    components: { Modal, Avatar, Icon },
    data() {
        return { rows: [], loading: true, q: '', sel: null, busy: false, page: 1 };
    },
    watch: { q() { this.page = 1; } },
    computed: {
        filtered() {
            const q = this.q.trim().toLowerCase();
            if (!q) return this.rows;
            return this.rows.filter((r) =>
                (r.email || '').toLowerCase().includes(q) ||
                (r.farmName || '').toLowerCase().includes(q));
        },
        totalPages() { return Math.max(1, Math.ceil(this.filtered.length / PAGE_SIZE)); },
        paged() {
            const p = Math.min(this.page, this.totalPages);
            return this.filtered.slice((p - 1) * PAGE_SIZE, p * PAGE_SIZE);
        },
        isSuspended() { return this.sel && String(this.sel.farmerStatus).toUpperCase() === 'SUSPENDED'; },
    },
    async mounted() { await this.load(); },
    methods: {
        async load() {
            this.loading = true;
            try { this.rows = await api.get('/admin/farmers'); }
            catch (e) { toast('載入小農清單失敗', 'err'); }
            finally { this.loading = false; }
        },
        async open(id) {
            try { this.sel = await api.get('/admin/farmers/' + id); }
            catch (e) { toast('讀取小農資料失敗', 'err'); }
        },
        async act(action) {
            if (!this.sel) return;
            const label = action === 'suspend' ? '停權' : '恢復';
            const ok = await confirmDialog({
                title: label + '小農',
                message: '確定要' + label + '「' + this.sel.farmName + '」嗎？',
                variant: action === 'suspend' ? 'danger' : 'primary',
                confirmText: '確認' + label,
            });
            if (!ok) return;
            this.busy = true;
            try {
                const updated = await api.put('/admin/farmers/' + this.sel.farmerId + '/' + action);
                this.sel = updated;
                const i = this.rows.findIndex((r) => r.farmerId === updated.farmerId);
                if (i >= 0) this.rows[i] = updated;
                toast(action === 'suspend' ? '已停權' : '已恢復');
            } catch (e) {
                toast(e.message || '操作失敗', 'err');
            } finally { this.busy = false; }
        },
        setPage(n) { if (n >= 1 && n <= this.totalPages) this.page = n; },
        badge(s) { return 'badge dot s-' + String(s || '').toLowerCase(); },
    },
    template: `
    <div>
      <div class="page-head">
        <h2>小農管理</h2>
        <div class="search"><icon name="search" size="sm"></icon><input v-model="q" placeholder="搜尋 Email / 農場名稱" /></div>
      </div>

      <div class="card">
        <div v-if="loading" class="empty">載入中…</div>
        <div v-else-if="!filtered.length" class="empty">沒有符合的小農</div>
        <template v-else>
          <div class="table-wrap">
            <table class="table">
              <thead><tr><th>農場</th><th>電話</th><th>帳號狀態</th><th>審核</th><th></th></tr></thead>
              <tbody>
                <tr v-for="r in paged" :key="r.farmerId">
                  <td>
                    <div class="cell-user">
                      <avatar :name="r.farmName" size="sm"></avatar>
                      <div><div class="nm">{{ r.farmName }}</div><div class="em">{{ r.email }}</div></div>
                    </div>
                  </td>
                  <td>{{ r.farmerPhoneNum || '—' }}</td>
                  <td><span :class="badge(r.farmerStatus)">{{ r.farmerStatus }}</span></td>
                  <td>
                    <span v-if="r.reviewStatus" class="badge" :class="'s-' + r.reviewStatus.toLowerCase()">{{ r.reviewStatus }}</span>
                    <span v-else class="muted">未送審</span>
                  </td>
                  <td><div class="row-actions"><button class="btn outline sm" @click="open(r.farmerId)">查看</button></div></td>
                </tr>
              </tbody>
            </table>
          </div>
          <div class="pagination">
            <span>共 {{ filtered.length }} 筆</span>
            <div class="pages">
              <button class="pg" :disabled="page===1" @click="setPage(page-1)">上一頁</button>
              <button v-for="n in totalPages" :key="n" class="pg" :class="{active:n===page}" @click="setPage(n)">{{ n }}</button>
              <button class="pg" :disabled="page===totalPages" @click="setPage(page+1)">下一頁</button>
            </div>
          </div>
        </template>
      </div>

      <modal v-if="sel" :title="sel.farmName + '（#' + sel.farmerId + '）'" @close="sel=null">
        <div class="rw"><span class="k">Email</span><span>{{ sel.email }}</span></div>
        <div class="rw"><span class="k">電話</span><span>{{ sel.farmerPhoneNum || '—' }}</span></div>
        <div class="rw"><span class="k">地址</span><span>{{ sel.cityName }}{{ sel.distName }} {{ sel.farmAddress || '' }}</span></div>
        <div class="rw"><span class="k">座標</span><span>{{ sel.locLat || '—' }}, {{ sel.locLong || '—' }}</span></div>
        <div class="rw"><span class="k">農場介紹</span><span>{{ sel.farmDesc || '—' }}</span></div>
        <div class="rw"><span class="k">最新審核</span><span>
          <span v-if="sel.reviewStatus" class="badge" :class="'s-' + sel.reviewStatus.toLowerCase()">{{ sel.reviewStatus }}</span>
          <span v-else class="muted">未送審</span>
          （第 {{ sel.reviewRound || '—' }} 輪）</span></div>
        <div class="rw"><span class="k">帳號狀態</span><span><span :class="badge(sel.farmerStatus)">{{ sel.farmerStatus }}</span></span></div>

        <template #foot>
          <button v-if="!isSuspended" class="btn danger sm" :disabled="busy" @click="act('suspend')">停權</button>
          <button v-else class="btn sm" :disabled="busy" @click="act('reinstate')">恢復帳號</button>
        </template>
      </modal>
    </div>
  `,
};
