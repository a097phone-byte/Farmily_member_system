// 小農審核：待審/審核中清單 / 認領（開始審核）/ 查看本輪快照 + 證明文件 / 核准 / 退件 / 查歷史輪次。
// 後端為「兩階段」審核流程：
//   GET  /api/admin/reviews/pending            待審清單（PENDING）
//   GET  /api/admin/reviews/reviewing          審核中清單（REVIEWING）
//   GET  /api/admin/reviews/farmer/{farmerId}  某小農所有審核紀錄
//   PUT  /api/admin/reviews/{reviewId}/start    認領（PENDING → REVIEWING，記錄審核者）
//   PUT  /api/admin/reviews/{reviewId}/approve 核准（需先認領，且只有認領者本人可操作）
//   PUT  /api/admin/reviews/{reviewId}/reject  body: { rejectReason }（需先認領，且只有認領者本人可操作）
// 證明文件預覽需後端新增（見專案說明）：
//   GET  /api/admin/reviews/{reviewId}/cert/{type}   type = land | product | identity，回傳檔案 bytes
import { store } from '../store.js';
import { api } from '../api.js';
import { toast, confirmDialog } from '../ui.js';
import Modal from './Modal.js';
import Avatar from './Avatar.js';
import Icon from './Icon.js';

const CERTS = [
    { type: 'land', label: '土地證明', flag: 'hasCertLand' },
    { type: 'product', label: '產品證明', flag: 'hasCertProduct' },
    { type: 'identity', label: '身分證明', flag: 'hasCertIdentity' },
];

export default {
    name: 'AdminReviews',
    components: { Modal, Avatar, Icon },
    data() {
        return {
            store,
            tab: 'pending',            // pending（待審）| reviewing（審核中）
            rows: [], loading: true, busy: false,
            sel: null,                 // 目前檢視的審核紀錄
            rejecting: false, rejectReason: '',
            history: null,             // 該小農的歷史輪次（點「歷史」才載入）
            cert: null,                // { label, url, kind: 'image'|'pdf'|'other' }
            certs: CERTS,
        };
    },
    computed: {
        // 目前登入管理員 email（用來判斷案件是否由本人認領）
        myEmail() { return this.store.admin.profile ? this.store.admin.profile.adminEmail : null; },
    },
    async mounted() { await this.load(); },
    methods: {
        async switchTab(t) {
            if (this.tab === t) return;
            this.tab = t;
            this.close();
            await this.load();
        },
        async load() {
            this.loading = true;
            const path = this.tab === 'reviewing' ? '/admin/reviews/reviewing' : '/admin/reviews/pending';
            try { this.rows = await api.get(path); }
            catch (e) { toast('載入清單失敗', 'err'); }
            finally { this.loading = false; }
        },
        open(r) {
            this.sel = r;
            this.rejecting = false; this.rejectReason = '';
            this.history = null;
            this.clearCert();
        },
        close() { this.clearCert(); this.sel = null; },

        // 案件是否由「我」認領（後端僅允許認領者本人核准/退件）
        isReviewing(r) { return String(r && r.reviewStatus).toUpperCase() === 'REVIEWING'; },
        isMine(r) { return !!(r && r.adminEmail && this.myEmail && r.adminEmail === this.myEmail); },

        // 認領案件：PENDING → REVIEWING，之後才能核准/退件
        async start() {
            this.busy = true;
            try {
                const updated = await api.put('/admin/reviews/' + this.sel.reviewId + '/start');
                toast('已認領，可開始審核');
                this.sel = updated;        // 變成 REVIEWING 並記錄審核者（本人）
                this.tab = 'reviewing';
                await this.load();          // 重新載入審核中清單
            } catch (e) { toast(e.message || '認領失敗', 'err'); }
            finally { this.busy = false; }
        },

        async approve() {
            const ok = await confirmDialog({
                title: '核准申請',
                message: '確定核准「' + (this.sel.farmName || this.sel.submittedFarmName || this.sel.farmerEmail) + '」的申請？核准後小農即可登入。',
                confirmText: '確認核准',
            });
            if (!ok) return;
            this.busy = true;
            try {
                await api.put('/admin/reviews/' + this.sel.reviewId + '/approve');
                toast('已核准，小農可登入');
                this.close();
                await this.load();
            } catch (e) { toast(e.message || '核准失敗', 'err'); }
            finally { this.busy = false; }
        },
        async reject() {
            if (!this.rejectReason.trim()) { toast('請填寫退件原因', 'warn'); return; }
            this.busy = true;
            try {
                await api.put('/admin/reviews/' + this.sel.reviewId + '/reject', { rejectReason: this.rejectReason.trim() });
                toast('已退件');
                this.close();
                await this.load();
            } catch (e) { toast(e.message || '退件失敗', 'err'); }
            finally { this.busy = false; }
        },

        async loadHistory() {
            try { this.history = await api.get('/admin/reviews/farmer/' + this.sel.farmerId); }
            catch (e) { toast('載入歷史紀錄失敗', 'err'); }
        },

        // ===== 證明文件預覽 =====
        async viewCert(c) {
            this.clearCert();
            try {
                const res = await fetch('/api/admin/reviews/' + this.sel.reviewId + '/cert/' + c.type, { credentials: 'include' });
                if (!res.ok) throw new Error('fetch failed ' + res.status);
                const blob = await res.blob();
                const t = blob.type || '';
                const kind = t.startsWith('image/') ? 'image' : (t.includes('pdf') ? 'pdf' : 'other');
                this.cert = { label: c.label, url: URL.createObjectURL(blob), kind };
            } catch (e) {
                toast('文件預覽失敗（後端尚未提供下載 endpoint）', 'err');
            }
        },
        clearCert() {
            if (this.cert && this.cert.url) URL.revokeObjectURL(this.cert.url);
            this.cert = null;
        },
        fmt(s) { return s ? String(s).replace('T', ' ').slice(0, 19) : '—'; },
        // 本輪提交地址：拼起來；若全空＝這輪沒改
        snapAddr(s) {
            const a = ((s.submittedCityName || '') + (s.submittedDistName || '') + ' ' + (s.submittedFarmAddress || '')).trim();
            return a || '（本輪未修改）';
        },
    },
    beforeUnmount() { this.clearCert(); },
    template: `
    <div>
      <div class="page-head"><h2>小農審核</h2></div>

      <div class="tabs">
        <button class="tab" :class="{ active: tab === 'pending' }" @click="switchTab('pending')">待審</button>
        <button class="tab" :class="{ active: tab === 'reviewing' }" @click="switchTab('reviewing')">審核中</button>
      </div>

      <div class="card">
        <div v-if="loading" class="empty">載入中…</div>
        <div v-else-if="!rows.length" class="empty">{{ tab === 'reviewing' ? '目前沒有審核中案件' : '目前沒有待審件' }}</div>
        <div v-else class="table-wrap">
          <table class="table">
            <thead><tr><th>農場</th><th>輪次</th><th>狀態</th><th v-if="tab === 'reviewing'">認領者</th><th>送審時間</th><th></th></tr></thead>
            <tbody>
              <tr v-for="r in rows" :key="r.reviewId">
                <td>
                  <div class="cell-user">
                    <avatar :name="r.farmName || r.submittedFarmName || r.farmerEmail" size="sm"></avatar>
                    <div><div class="nm">{{ r.farmName || r.submittedFarmName || '（未命名農場）' }}</div><div class="em">{{ r.farmerEmail }}</div></div>
                  </div>
                </td>
                <td>第 {{ r.reviewRound }} 輪</td>
                <td><span class="badge" :class="'s-' + String(r.reviewStatus || 'review').toLowerCase()">{{ r.reviewStatus }}</span></td>
                <td v-if="tab === 'reviewing'">{{ r.adminName || r.adminEmail || '—' }}<span v-if="isMine(r)" class="muted">（我）</span></td>
                <td>{{ fmt(r.submittedAt) }}</td>
                <td><div class="row-actions"><button class="btn sm" @click="open(r)">{{ tab === 'reviewing' ? (isMine(r) ? '繼續審核' : '查看') : '查看 / 認領' }}</button></div></td>
              </tr>
            </tbody>
          </table>
        </div>
      </div>

      <modal v-if="sel" :title="'審核 #' + sel.reviewId + '（第 ' + sel.reviewRound + ' 輪）'" :wide="true" @close="close">
        <div class="rw"><span class="k">小農 Email</span><span>{{ sel.farmerEmail }}</span></div>
        <div class="rw"><span class="k">目前農場名稱</span><span>{{ sel.farmName || '—' }}</span></div>
        <div class="rw"><span class="k">本輪提交名稱</span><span>{{ sel.submittedFarmName || '（本輪未修改）' }}</span></div>
        <div class="rw"><span class="k">本輪提交地址</span><span>{{ snapAddr(sel) }}</span></div>
        <div class="rw"><span class="k">座標</span><span>{{ sel.submittedLocLat || '—' }}, {{ sel.submittedLocLong || '—' }}</span></div>
        <div class="rw"><span class="k">送審 / 審核時間</span><span>{{ fmt(sel.submittedAt) }} / {{ fmt(sel.reviewedAt) }}</span></div>
        <div class="rw"><span class="k">審核者</span><span>{{ sel.adminName || sel.adminEmail || '尚未認領' }}<span v-if="isMine(sel)" class="muted">（我）</span></span></div>

        <div class="cert-row">
          <span class="k">證明文件</span>
          <template v-for="c in certs" :key="c.type">
            <button class="btn outline sm" v-if="sel[c.flag]" @click="viewCert(c)"><icon name="eye" size="sm"></icon>{{ c.label }}</button>
            <span class="muted" v-else>{{ c.label }}（未上傳）</span>
          </template>
        </div>

        <!-- 文件預覽區 -->
        <div v-if="cert" class="cert-preview">
          <div class="cert-preview-head">
            <b>{{ cert.label }}</b>
            <span><a :href="cert.url" target="_blank" rel="noopener">另開分頁</a> · <button class="btn ghost sm" @click="clearCert">收合</button></span>
          </div>
          <img v-if="cert.kind === 'image'" :src="cert.url" alt="證明文件" />
          <iframe v-else-if="cert.kind === 'pdf'" :src="cert.url"></iframe>
          <p v-else class="muted">此檔案類型無法內嵌預覽，請點「另開分頁」下載查看。</p>
        </div>

        <!-- 歷史輪次 -->
        <div style="margin-top:14px">
          <button class="btn ghost sm" @click="loadHistory">查看此小農歷史輪次</button>
          <div v-if="history" class="table-wrap" style="margin-top:8px">
            <table class="table mini">
              <thead><tr><th>輪次</th><th>狀態</th><th>審核者</th><th>退件原因</th><th>時間</th></tr></thead>
              <tbody>
                <tr v-for="h in history" :key="h.reviewId">
                  <td>第 {{ h.reviewRound }} 輪</td>
                  <td><span class="badge" :class="'s-' + String(h.reviewStatus || 'review').toLowerCase()">{{ h.reviewStatus }}</span></td>
                  <td>{{ h.adminName || '—' }}</td>
                  <td>{{ h.rejectReason || '—' }}</td>
                  <td>{{ fmt(h.reviewedAt || h.submittedAt) }}</td>
                </tr>
              </tbody>
            </table>
          </div>
        </div>

        <!-- 退件原因輸入 -->
        <div v-if="rejecting" class="field" style="margin-top:14px">
          <label>退件原因</label>
          <textarea v-model="rejectReason" rows="3" placeholder="請說明退件原因，會回傳給小農"></textarea>
        </div>

        <template #foot>
          <!-- 尚未認領（PENDING）：先認領才能審核 -->
          <template v-if="!isReviewing(sel)">
            <span class="muted" style="margin-right:auto">須先認領案件才能核准／退件</span>
            <button class="btn sm" :disabled="busy" @click="start">開始審核（認領）</button>
          </template>
          <!-- 審核中、且由他人認領：鎖定 -->
          <template v-else-if="!isMine(sel)">
            <span class="muted">此案件已由「{{ sel.adminName || sel.adminEmail }}」認領，您無法審核。</span>
          </template>
          <!-- 審核中、且由本人認領：可核准／退件 -->
          <template v-else-if="!rejecting">
            <button class="btn danger sm" :disabled="busy" @click="rejecting = true">退件…</button>
            <button class="btn sm" :disabled="busy" @click="approve">核准</button>
          </template>
          <template v-else>
            <button class="btn ghost sm" :disabled="busy" @click="rejecting = false">取消</button>
            <button class="btn danger sm" :disabled="busy" @click="reject">確認退件</button>
          </template>
        </template>
      </modal>
    </div>
  `,
};
