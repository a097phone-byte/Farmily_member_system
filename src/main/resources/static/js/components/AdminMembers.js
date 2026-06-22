// 會員管理：列表 / 搜尋 / 分頁 / 查看明細 / 變更帳號狀態。
// 串接後端（皆已存在）：
//   GET  /api/admin/members            列表
//   GET  /api/admin/members/{id}       單一會員
//   PUT  /api/admin/members/{id}/status  body: { status }
import { api } from '../api.js';
import { toast, confirmDialog } from '../ui.js';
import Modal from './Modal.js';
import Avatar from './Avatar.js';
import Icon from './Icon.js';

const STATUSES = ['ACTIVE', 'WARNED', 'SUSPENDED'];
const PAGE_SIZE = 10;

export default {
    name: 'AdminMembers',
    components: { Modal, Avatar, Icon },
    data() {
        return { rows: [], loading: true, q: '', sel: null, busy: false, statuses: STATUSES, page: 1 };
    },
    watch: { q() { this.page = 1; } },
    computed: {
        filtered() {
            const q = this.q.trim().toLowerCase();
            if (!q) return this.rows;
            return this.rows.filter((r) =>
                (r.email || '').toLowerCase().includes(q) ||
                (r.userName || '').toLowerCase().includes(q));
        },
        totalPages() { return Math.max(1, Math.ceil(this.filtered.length / PAGE_SIZE)); },
        paged() {
            const p = Math.min(this.page, this.totalPages);
            return this.filtered.slice((p - 1) * PAGE_SIZE, p * PAGE_SIZE);
        },
    },
    async mounted() { await this.load(); },
    methods: {
        async load() {
            this.loading = true;
            try { this.rows = await api.get('/admin/members'); }
            catch (e) { toast('載入會員清單失敗', 'err'); }
            finally { this.loading = false; }
        },
        async open(id) {
            try { this.sel = await api.get('/admin/members/' + id); }
            catch (e) { toast('讀取會員資料失敗', 'err'); }
        },
        async setStatus(status) {
            if (!this.sel || this.sel.userStatus === status) return;
            const ok = await confirmDialog({
                title: '變更會員狀態',
                message: '確定將「' + this.sel.userName + '」的狀態改為 ' + status + '？',
                variant: status === 'SUSPENDED' ? 'danger' : 'primary',
                confirmText: '確認變更',
            });
            if (!ok) return;
            this.busy = true;
            try {
                const updated = await api.put('/admin/members/' + this.sel.userId + '/status', { status });
                this.sel = updated;
                const i = this.rows.findIndex((r) => r.userId === updated.userId);
                if (i >= 0) this.rows[i] = updated;
                toast('已將狀態更新為 ' + status);
            } catch (e) {
                toast(e.message || '狀態更新失敗', 'err');
            } finally { this.busy = false; }
        },
        setPage(n) { if (n >= 1 && n <= this.totalPages) this.page = n; },
        badge(s) { return 'badge dot s-' + String(s || '').toLowerCase(); },
    },
    template: `
    <div>
      <div class="page-head">
        <h2>會員管理</h2>
        <div class="search"><icon name="search" size="sm"></icon><input v-model="q" placeholder="搜尋 Email / 姓名" /></div>
      </div>

      <div class="card">
        <div v-if="loading" class="empty">載入中…</div>
        <div v-else-if="!filtered.length" class="empty">沒有符合的會員</div>
        <template v-else>
          <div class="table-wrap">
            <table class="table">
              <thead><tr><th>會員</th><th>手機</th><th>消費級距</th><th>狀態</th><th></th></tr></thead>
              <tbody>
                <tr v-for="r in paged" :key="r.userId">
                  <td>
                    <div class="cell-user">
                      <avatar :name="r.userName" size="sm"></avatar>
                      <div><div class="nm">{{ r.userName }}</div><div class="em">{{ r.email }}</div></div>
                    </div>
                  </td>
                  <td>{{ r.userPhoneNum || '—' }}</td>
                  <td><span class="badge info">{{ r.spendingTier || '—' }}</span></td>
                  <td><span :class="badge(r.userStatus)">{{ r.userStatus }}</span></td>
                  <td><div class="row-actions"><button class="btn outline sm" @click="open(r.userId)">查看</button></div></td>
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

      <modal v-if="sel" :title="'會員 #' + sel.userId" @close="sel=null">
        <div class="rw"><span class="k">Email</span><span>{{ sel.email }}</span></div>
        <div class="rw"><span class="k">姓名 / 暱稱</span><span>{{ sel.userName }}（{{ sel.userNickname || '—' }}）</span></div>
        <div class="rw"><span class="k">手機</span><span>{{ sel.userPhoneNum || '—' }}</span></div>
        <div class="rw"><span class="k">地址</span><span>{{ sel.cityName }}{{ sel.distName }} {{ sel.userAddress || '' }}</span></div>
        <div class="rw"><span class="k">消費級距</span><span><span class="badge info">{{ sel.spendingTier || '—' }}</span>（本月 {{ sel.monthlySpending }} 元）</span></div>
        <div class="rw"><span class="k">登入方式</span><span>{{ sel.authProvider }}</span></div>
        <div class="rw"><span class="k">小農身分</span><span>{{ sel.farmerIdentity ? '是' : '否' }}</span></div>
        <div class="rw"><span class="k">目前狀態</span><span><span :class="badge(sel.userStatus)">{{ sel.userStatus }}</span></span></div>

        <template #foot>
          <span class="muted" style="margin-right:auto">變更狀態：</span>
          <button v-for="s in statuses" :key="s" class="btn outline sm"
                  :disabled="busy || sel.userStatus === s" @click="setStatus(s)">{{ s }}</button>
        </template>
      </modal>
    </div>
  `,
};
