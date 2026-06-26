// 前台進入點。三種畫面：公開頁（首頁/登入）、會員儀表板、小農儀表板。
import { store } from './store.js';
import { router, navigate } from './router.js';

import AppNav from './components/AppNav.js';
import HomeLanding from './components/HomeLanding.js';
import MemberLogin from './components/MemberLogin.js';
import FarmerLogin from './components/FarmerLogin.js';
import ForgotPassword from './components/ForgotPassword.js';
import ResetPassword from './components/ResetPassword.js';
import ResendVerification from './components/ResendVerification.js';
import Forbidden from './components/Forbidden.js';
import Toast from './components/Toast.js';
import ConfirmDialog from './components/ConfirmDialog.js';
import DashboardLayout from './components/DashboardLayout.js';

// 會員儀表板分頁
import MemberBasicInfo from './components/MemberBasicInfo.js';
import MemberPassword from './components/MemberPassword.js';
import MemberIntegrations from './components/MemberIntegrations.js';
// 小農儀表板分頁
import FarmerBasicInfo from './components/FarmerBasicInfo.js';
import FarmerPassword from './components/FarmerPassword.js';
import FarmerReviewStatus from './components/FarmerReviewStatus.js';

const LoadingView = { template: `<div class="empty">載入中…</div>` };

// 會員 / 小農的路由表（path → 元件 + 標題）
const MEMBER_ROUTES = {
    '/member/me':           { comp: 'MemberBasicInfo',    title: '基本資料' },
    '/member/password':     { comp: 'MemberPassword',     title: '修改密碼' },
    '/member/integrations': { comp: 'MemberIntegrations', title: '連結帳號' },
};
const FARMER_ROUTES = {
    '/farmer/home':     { comp: 'FarmerBasicInfo',    title: '農場資料' },
    '/farmer/password': { comp: 'FarmerPassword',     title: '修改密碼' },
    '/farmer/review':   { comp: 'FarmerReviewStatus', title: '審核狀態' },
};

const MEMBER_NAV = [
    { path: '/member/me',           label: '基本資料', icon: 'user' },
    { path: '/member/password',     label: '修改密碼', icon: 'lock' },
    { path: '/member/integrations', label: '連結帳號', icon: 'link' },
];
const FARMER_NAV = [
    { path: '/farmer/home',     label: '農場資料', icon: 'leaf' },
    { path: '/farmer/password', label: '修改密碼', icon: 'lock' },
    { path: '/farmer/review',   label: '審核狀態', icon: 'clipboard' },
];

const App = {
    components: {
        AppNav, HomeLanding, MemberLogin, FarmerLogin, ForgotPassword, ResetPassword, ResendVerification,
        Forbidden, Toast, ConfirmDialog, DashboardLayout, LoadingView,
        MemberBasicInfo, MemberPassword, MemberIntegrations,
        FarmerBasicInfo, FarmerPassword, FarmerReviewStatus,
    },
    data() { return { store, router, memberNav: MEMBER_NAV, farmerNav: FARMER_NAV }; },
    computed: {
        // 決定目前在哪個區：public / member / farmer（含登入與載入中的中間狀態）
        zone() {
            const path = this.router.path;
            if (MEMBER_ROUTES[path]) {
                if (!this.store.member.loaded) return 'loading';
                return this.store.member.profile ? 'member' : 'member-login';
            }
            if (FARMER_ROUTES[path]) {
                if (!this.store.farmer.loaded) return 'loading';
                return this.store.farmer.profile ? 'farmer' : 'farmer-login';
            }
            return 'public';
        },
        memberRoute() { return MEMBER_ROUTES[this.router.path] || MEMBER_ROUTES['/member/me']; },
        farmerRoute() { return FARMER_ROUTES[this.router.path] || FARMER_ROUTES['/farmer/home']; },
        // 公開頁要顯示哪個元件
        publicView() {
            // 重設密碼頁網址會帶 ?token=...，比對前先把問號後面切掉
            const path = this.router.path.split('?')[0];
            const map = {
                '/member/login': 'MemberLogin',
                '/farmer/login': 'FarmerLogin',
                '/forgot-password': 'ForgotPassword',
                '/reset-password': 'ResetPassword',
                '/resend-verification': 'ResendVerification',
                '/forbidden': 'Forbidden',
            };
            return map[path] || 'HomeLanding';
        },
        memberName() { return this.store.member.profile ? this.store.member.profile.userName : ''; },
        farmerName() { return this.store.farmer.profile ? this.store.farmer.profile.farmName : ''; },
    },
    methods: {
        navigate,
        async logout() { await this.store.logout(); navigate('/'); },
    },
    async mounted() {
        // 進站時用既有 cookie 還原會員/小農登入
        this.store.memberFetchMe().catch(() => {});
        this.store.farmerFetchMe().catch(() => {});
    },
    template: `
    <div>
      <!-- 會員儀表板 -->
      <dashboard-layout v-if="zone === 'member'"
        brand="Farmily" :nav-items="memberNav" :user-name="memberName" role="會員"
        :title="memberRoute.title" :notifications="[{text:'歡迎回到 Farmily！',time:'剛剛'}]" :narrow="true"
        @logout="logout">
        <component :is="memberRoute.comp"></component>
      </dashboard-layout>

      <!-- 小農儀表板 -->
      <dashboard-layout v-else-if="zone === 'farmer'"
        brand="Farmily 小農" :nav-items="farmerNav" :user-name="farmerName" role="小農"
        :title="farmerRoute.title" :notifications="[{text:'審核狀態若有更新會通知你',time:'今天'}]" :narrow="true"
        @logout="logout">
        <component :is="farmerRoute.comp"></component>
      </dashboard-layout>

      <!-- 公開頁 / 登入頁 / 載入中 -->
      <template v-else>
        <app-nav></app-nav>
        <main>
          <loading-view v-if="zone === 'loading'"></loading-view>
          <member-login v-else-if="zone === 'member-login'"></member-login>
          <farmer-login v-else-if="zone === 'farmer-login'"></farmer-login>
          <component v-else :is="publicView"></component>
        </main>
        <footer class="footer">© Farmily 你儂我農　·　<a href="admin.html">管理員入口</a></footer>
      </template>

      <toast></toast>
      <confirm-dialog></confirm-dialog>
    </div>
  `,
};

Vue.createApp(App).mount('#app');
