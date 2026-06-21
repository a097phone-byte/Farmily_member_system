// 後台首頁：依 permissionCodes「動態顯示選單」+ 內含「改自己名字」驗證 mutation。
import { store } from '../store.js';

export default {
    name: 'AdminDashboard',
    data() { return { store, newName: '', msg: '' }; },
    computed: {
        p() { return this.store.admin.profile || {}; },
        isSuper() { return this.store.adminHasPerm('ADMIN'); },
        // 每個選單標出「需要的權限」，allowed 由 store.adminHasPerm 動態算出
        menus() {
            return [
                { key: 'member', emoji: '👥', title: '會員管理', allowed: store.adminHasPerm('MEMBER') },
                { key: 'farmer', emoji: '🧑‍🌾', title: '小農管理', allowed: store.adminHasPerm('FARMER') },
                { key: 'review', emoji: '📋', title: '小農審核', allowed: store.adminHasPerm('FARMER') },
                { key: 'admin',  emoji: '🛡️', title: '管理員管理', allowed: store.adminHasPerm('ADMIN') },
            ];
        },
    },
    mounted() { this.newName = this.store.admin.profile ? this.store.admin.profile.adminName : ''; },
    methods: {
        clickTile(m) {
            if (!m.allowed) return;
            window.alert('「' + m.title + '」頁面將於下一階段實作');
        },
        async saveName() {
            this.msg = '';
            try { await store.adminUpdateName(this.newName); this.msg = '已更新'; }
            catch (e) { this.msg = '更新失敗'; }
        },
    },
    template: `
    <div>
      <h2>管理後台</h2>
      <section class="box">
        <p><b>管理員：</b>{{ p.adminName }}（{{ p.adminEmail }}）　<b>狀態：</b>{{ p.adminStatus }}</p>
        <p><b>權限：</b>
          <span v-for="c in p.permissionCodes" :key="c" class="pill">{{ c }}</span>
          <span v-if="isSuper" class="pill super">超級管理員</span>
        </p>
      </section>

      <section class="menu">
        <div v-for="m in menus" :key="m.key" class="tile" :class="{ off: !m.allowed }" @click="clickTile(m)">
          <div style="font-size:30px">{{ m.emoji }}</div>
          <div>{{ m.title }}</div>
          <small class="soon">{{ m.allowed ? '可進入（下一階段）' : '無權限' }}</small>
        </div>
      </section>

      <section class="box" style="margin-top:14px">
        <h3>修改我的名稱</h3>
        <label>名稱 <input v-model="newName" /></label>
        <button @click="saveName">儲存</button> <span class="ok">{{ msg }}</span>
      </section>
    </div>
  `,
};