import { store } from './store.js';
import { router, navigate } from './router.js';

import AppNav from './components/AppNav.js';
import HomeLanding from './components/HomeLanding.js';
import MemberLogin from './components/MemberLogin.js';
import MemberProfile from './components/MemberProfile.js';
import FarmerLogin from './components/FarmerLogin.js';
import FarmerHome from './components/FarmerHome.js';
import Forbidden from './components/Forbidden.js';
// 註：AdminLogin / AdminDashboard 已移到後台 admin.html，前台不再 import

const LoadingView = { template: `<p style="text-align:center;color:var(--text-faint)">載入中…</p>` };

const App = {
    components: {
        AppNav, HomeLanding, MemberLogin, MemberProfile, FarmerLogin, FarmerHome, Forbidden, LoadingView,
    },
    data() { return { store, router }; },
    methods: {
        navigate,
        guard(line, pageComp, loginComp) {
            const s = this.store[line];
            if (!s.loaded) return 'LoadingView';
            if (!s.profile) return loginComp;
            return pageComp;
        },
    },
    computed: {
        currentView() {
            const path = this.router.path;
            const publicPages = {
                '/': 'HomeLanding',
                '/member/login': 'MemberLogin',
                '/farmer/login': 'FarmerLogin',
                '/forbidden': 'Forbidden',
            };
            if (publicPages[path]) return publicPages[path];
            if (path === '/member/me')   return this.guard('member', 'MemberProfile', 'MemberLogin');
            if (path === '/farmer/home') return this.guard('farmer', 'FarmerHome', 'FarmerLogin');
            return 'HomeLanding';
        },
    },
    async mounted() {
        // 前台只還原會員/小農，不再管理員
        this.store.memberFetchMe().catch(() => {});
        this.store.farmerFetchMe().catch(() => {});
    },
    // 頁尾的管理員入口改成「真正的另一個網頁」連結（不是 hash 路由）
    template: `
    <div>
      <app-nav></app-nav>
      <main class="content">
        <component :is="currentView"></component>
      </main>
      <footer class="footer">
        © Farmily 你儂我農　·　<a href="admin.html">管理員入口</a>
      </footer>
    </div>
  `,
};

Vue.createApp(App).mount('#app');