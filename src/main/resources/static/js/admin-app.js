// 後台獨立應用的進入點。和前台 app.js 是「兩個分開的程式」，各自掛在各自的 HTML。
// 後台路由很單純：登入了就顯示後台首頁，沒登入就顯示登入頁（不需要 hash 路由）。

import { store } from './store.js';
import AdminLogin from './components/AdminLogin.js';
import AdminDashboard from './components/AdminDashboard.js';

const LoadingView = { template: `<p style="text-align:center;color:var(--text-faint)">載入中…</p>` };

const AdminApp = {
    components: { AdminLogin, AdminDashboard, LoadingView },
    data() { return { store }; },
    computed: {
        // 依登入狀態決定顯示哪個畫面
        view() {
            if (!this.store.admin.loaded) return 'LoadingView';       // 還在用 cookie 確認
            return this.store.admin.profile ? 'AdminDashboard' : 'AdminLogin';
        },
    },
    methods: {
        async logout() {
            await store.logout();   // profile 變 null → view 自動切回登入頁
        },
    },
    // 進站時用既有 cookie 嘗試還原管理員登入
    mounted() { this.store.adminFetchMe().catch(() => {}); },
    template: `
    <div>
      <header class="navbar">
        <span class="brand">🛡️ Farmily 管理後台</span>
        <nav>
          <template v-if="store.admin.profile">
            <span class="hi">{{ store.admin.profile.adminName }}</span>
            <button @click="logout">登出</button>
          </template>
          <a v-else href="index.html">← 回前台</a>
        </nav>
      </header>
      <main class="content">
        <component :is="view"></component>
      </main>
    </div>
  `,
};

Vue.createApp(AdminApp).mount('#app');