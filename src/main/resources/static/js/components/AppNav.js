// 頂部導覽列。未登入時只顯示「會員 / 小農」入口；管理員入口不放這（移到頁尾）。
import { store } from '../store.js';
import { navigate } from '../router.js';

export default {
    name: 'AppNav',
    data() { return { store }; },
    methods: {
        navigate,
        async logout() { await store.logout(); navigate('/'); },
    },
    template: `
    <header class="navbar">
      <span class="brand" @click="navigate('/')">🌾 Farmily</span>
      <nav>
        <template v-if="store.member.profile">
          <span class="hi">會員：{{ store.member.profile.userName }}</span>
          <a href="#/member/me">個人資料</a>
          <button @click="logout">登出</button>
        </template>
        <template v-else-if="store.farmer.profile">
          <span class="hi">小農：{{ store.farmer.profile.farmName }}</span>
          <a href="#/farmer/home">小農中心</a>
          <button @click="logout">登出</button>
        </template>
        <template v-else-if="store.admin.profile">
          <span class="hi">管理員：{{ store.admin.profile.adminName }}</span>
          <a href="#/admin">後台</a>
          <button @click="logout">登出</button>
        </template>
        <template v-else>
          <a href="#/member/login">會員登入</a>
          <a href="#/farmer/login">小農登入</a>
        </template>
      </nav>
    </header>
  `,
};