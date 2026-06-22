// 公開頁（首頁 / 登入）用的頂欄。登入後的會員/小農改用 sidebar 儀表板，不再用這個。
import { navigate } from '../router.js';

export default {
    name: 'AppNav',
    methods: { navigate },
    template: `
    <header class="nav-public">
      <span class="brand" @click="navigate('/')"><span class="dot"></span>Farmily</span>
      <nav>
        <button class="btn ghost sm" @click="navigate('/member/login')">會員登入</button>
        <button class="btn outline sm" @click="navigate('/farmer/login')">小農登入</button>
      </nav>
    </header>
  `,
};
