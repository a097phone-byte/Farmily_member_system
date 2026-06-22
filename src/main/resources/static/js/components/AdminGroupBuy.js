// 【Bonus】團購進度追蹤（project-management-table 風）。
// 後端目前沒有商城/團購任何 API（屬組員模組），這裡先用「示意假資料」把版面與互動做好，
// 頁面頂部明確標註待串接。之後團購 API 完成，把 MOCK 換成 api.get('/admin/groupbuys') 即可。
import Avatar from './Avatar.js';

const MOCK = [
    { id: 1, host: '王小明', product: '當季麻豆文旦 10 斤裝', target: 50, joined: 38, status: 'ACTIVE',   deadline: '2026-07-05' },
    { id: 2, host: '陳美麗', product: '池上有機米 5kg',       target: 30, joined: 30, status: 'APPROVED', deadline: '2026-06-28' },
    { id: 3, host: '林大為', product: '高山高麗菜箱購',         target: 40, joined: 12, status: 'ACTIVE',   deadline: '2026-07-12' },
    { id: 4, host: '張雅婷', product: '玉井愛文芒果禮盒',       target: 60, joined: 5,  status: 'PENDING',  deadline: '2026-07-20' },
    { id: 5, host: '黃志豪', product: '產地直送雞蛋 30 入',     target: 80, joined: 80, status: 'APPROVED', deadline: '2026-06-25' },
    { id: 6, host: '吳佩珊', product: '梅山烏龍茶 150g',        target: 25, joined: 3,  status: 'REJECTED', deadline: '2026-06-30' },
];

const STATUS_LABEL = { ACTIVE: '進行中', APPROVED: '已成團', PENDING: '待開團', REJECTED: '已取消' };

export default {
    name: 'AdminGroupBuy',
    components: { Avatar },
    data() { return { rows: MOCK, q: '' }; },
    computed: {
        filtered() {
            const q = this.q.trim();
            if (!q) return this.rows;
            return this.rows.filter((r) => r.host.includes(q) || r.product.includes(q));
        },
    },
    methods: {
        pct(r) { return Math.min(100, Math.round((r.joined / r.target) * 100)); },
        statusLabel(s) { return STATUS_LABEL[s] || s; },
        badge(s) { return 'badge s-' + String(s || '').toLowerCase(); },
    },
    template: `
    <div>
      <div class="banner warn">這是示意假資料，待串接「團購 / 商城」後端 API（目前屬組員模組，尚未提供）。</div>

      <div class="page-head">
        <h2>團購追蹤</h2>
        <div class="search"><input v-model="q" placeholder="搜尋 團購主 / 商品" /></div>
      </div>

      <div class="card">
        <div v-if="!filtered.length" class="empty">沒有符合的團購</div>
        <div v-else class="table-wrap">
          <table class="table">
            <thead><tr><th>團購主</th><th>商品</th><th>進度</th><th>狀態</th><th>截止日</th></tr></thead>
            <tbody>
              <tr v-for="r in filtered" :key="r.id">
                <td>
                  <div class="cell-user">
                    <avatar :name="r.host" size="sm"></avatar>
                    <div><div class="nm">{{ r.host }}</div><div class="em">團購主</div></div>
                  </div>
                </td>
                <td>{{ r.product }}</td>
                <td style="min-width:160px">
                  <div style="display:flex;justify-content:space-between;font-size:12px;color:var(--muted);margin-bottom:4px">
                    <span>{{ r.joined }} / {{ r.target }} 份</span><span>{{ pct(r) }}%</span>
                  </div>
                  <div class="progress"><span :style="{ width: pct(r) + '%' }"></span></div>
                </td>
                <td><span :class="badge(r.status)">{{ statusLabel(r.status) }}</span></td>
                <td>{{ r.deadline }}</td>
              </tr>
            </tbody>
          </table>
        </div>
      </div>
    </div>
  `,
};
