// 公開首頁：Hero + 會員/小農兩個入口卡片 + 服務分類（靜態佔位）。無 emoji、淺色。
import { navigate } from '../router.js';

export default {
    name: 'HomeLanding',
    data() {
        return {
            features: [
                { title: '當季蔬果', desc: '產地直送的新鮮蔬果' },
                { title: '在地米糧', desc: '小農自種、安心好米' },
                { title: '商城', desc: '優惠商品與團購' },
                { title: '最新消息', desc: '平台公告與活動' },
            ],
        };
    },
    methods: { navigate },
    template: `
    <div class="content-public">
      <section class="hero">
        <h1>你儂我農，把好食材直接送到你家</h1>
        <p>連結在地小農與消費者的農產平台</p>
      </section>

      <section class="entry-grid">
        <div class="entry-card">
          <h3>我是消費者</h3>
          <p>登入或註冊會員，開始選購在地新鮮農產。</p>
          <button class="btn block" @click="navigate('/member/login')">會員登入 / 註冊</button>
        </div>
        <div class="entry-card">
          <h3>我是小農</h3>
          <p>登入管理你的農場，或送出加入申請。</p>
          <button class="btn outline block" @click="navigate('/farmer/login')">小農登入 / 申請</button>
        </div>
      </section>

      <section class="feature-grid">
        <div class="feature" v-for="f in features" :key="f.title">
          <h4>{{ f.title }}</h4>
          <p>{{ f.desc }}</p>
          <small class="soon">即將推出</small>
        </div>
      </section>
    </div>
  `,
};
