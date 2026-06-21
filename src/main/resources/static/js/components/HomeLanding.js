// 公開首頁：Hero + 會員/小農 兩個入口卡片 + 服務分類（靜態佔位）。
// 管理員入口不放這裡（對外首頁不顯示），改放在頁尾。
import { navigate } from '../router.js';

export default {
    name: 'HomeLanding',
    data() {
        return {
            categories: [
                { emoji: '🥬', title: '當季蔬果', desc: '產地直送的新鮮蔬果' },
                { emoji: '🍚', title: '在地米糧', desc: '小農自種、安心好米' },
                { emoji: '🛒', title: '商城', desc: '優惠商品與團購' },
                { emoji: '📰', title: '最新消息', desc: '平台公告與活動' },
            ],
        };
    },
    methods: { navigate },
    template: `
    <div>
      <section class="hero">
        <h1>你儂我農，把好食材直接送到你家</h1>
        <p>連結在地小農與消費者的農產平台</p>
      </section>

      <!-- 只對外開放「會員」「小農」兩個入口 -->
      <section class="entry-grid">
        <div class="entry-card">
          <div class="ico">🧑‍🍳</div>
          <h3>我是消費者</h3>
          <p>登入或註冊會員，開始選購在地新鮮農產。</p>
          <button class="btn-block" @click="navigate('/member/login')">會員登入 / 註冊</button>
        </div>
        <div class="entry-card">
          <div class="ico">🧑‍🌾</div>
          <h3>我是小農</h3>
          <p>登入管理你的農場，或送出加入申請。</p>
          <button class="btn-block" @click="navigate('/farmer/login')">小農登入 / 申請</button>
        </div>
      </section>

      <section class="cards">
        <div class="card" v-for="c in categories" :key="c.title">
          <div style="font-size:34px">{{ c.emoji }}</div>
          <h3>{{ c.title }}</h3>
          <p style="color:var(--text-muted)">{{ c.desc }}</p>
          <small class="soon">（即將推出）</small>
        </div>
      </section>
    </div>
  `,
};