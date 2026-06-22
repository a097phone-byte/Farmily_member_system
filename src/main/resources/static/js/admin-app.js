// 後台進入點。未登入→登入頁；登入後用 DashboardLayout（sidebar）在各管理頁間切換。
import { store } from './store.js';
import { router, navigate } from './router.js';
import DashboardLayout from './components/DashboardLayout.js';
import AdminLogin from './components/AdminLogin.js';
import AdminDashboard from './components/AdminDashboard.js';
import AdminMembers from './components/AdminMembers.js';
import AdminFarmers from './components/AdminFarmers.js';
import AdminReviews from './components/AdminReviews.js';
import AdminAdmins from './components/AdminAdmins.js';
import AdminGroupBuy from './components/AdminGroupBuy.js';
import Toast from './components/Toast.js';
import ConfirmDialog from './components/ConfirmDialog.js';

// 子頁設定：path → 元件 + 需要的權限（沿用 store.adminHasPerm）+ 標題
const ROUTES = {
    '/members':  { comp: 'AdminMembers',  perm: 'MEMBER', title: '會員管理' },
    '/farmers':  { comp: 'AdminFarmers',  perm: 'FARMER', title: '小農管理' },
    '/reviews':  { comp: 'AdminReviews',  perm: 'FARMER', title: '小農審核' },
    '/admins':   { comp: 'AdminAdmins',   perm: 'ADMIN',  title: '管理員管理' },
    '/groupbuy': { comp: 'AdminGroupBuy', perm: 'MEMBER', title: '團購追蹤' },
};
// sidebar 導覽（perm=null 代表所有登入管理員可見）
const NAV = [
    { path: '/',         label: '總覽',       icon: 'grid',      perm: null },
    { path: '/members',  label: '會員管理',   icon: 'users',     perm: 'MEMBER' },
    { path: '/farmers',  label: '小農管理',   icon: 'leaf',      perm: 'FARMER' },
    { path: '/reviews',  label: '小農審核',   icon: 'clipboard', perm: 'FARMER' },
    { path: '/admins',   label: '管理員管理', icon: 'shield',    perm: 'ADMIN' },
    { path: '/groupbuy', label: '團購追蹤',   icon: 'cart',      perm: 'MEMBER' },
];

const AdminApp = {
    components: {
        DashboardLayout, AdminLogin, AdminDashboard, AdminMembers,
        AdminFarmers, AdminReviews, AdminAdmins, AdminGroupBuy, Toast, ConfirmDialog,
    },
    data() { return { store, router }; },
    computed: {
        // sidebar 項目（依權限決定 show）
        navItems() {
            return NAV.map((n) => ({ path: n.path, label: n.label, icon: n.icon, show: !n.perm || this.store.adminHasPerm(n.perm) }));
        },
        // 目前主內容
        current() {
            const r = ROUTES[this.router.path];
            if (!r) return { comp: 'AdminDashboard', title: '總覽' };
            if (!this.store.adminHasPerm(r.perm)) return { comp: '__forbidden', title: '無權限' };
            return r;
        },
        adminName() { return this.store.admin.profile ? this.store.admin.profile.adminName : ''; },
    },
    methods: {
        async logout() { await this.store.logout(); navigate('/'); },
    },
    mounted() { this.store.adminFetchMe().catch(() => {}); },
    template: `
    <div>
      <div v-if="!store.admin.loaded" class="auth-wrap"><div class="empty">載入中…</div></div>

      <admin-login v-else-if="!store.admin.profile"></admin-login>

      <dashboard-layout v-else
        brand="Farmily 後台" :nav-items="navItems" :user-name="adminName" role="管理員"
        :title="current.title" @logout="logout">
        <div v-if="current.comp === '__forbidden'" class="empty">你的帳號沒有此頁面的權限</div>
        <component v-else :is="current.comp"></component>
      </dashboard-layout>

      <toast></toast>
      <confirm-dialog></confirm-dialog>
    </div>
  `,
};

Vue.createApp(AdminApp).mount('#app');
