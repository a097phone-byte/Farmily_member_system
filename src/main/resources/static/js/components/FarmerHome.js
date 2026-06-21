// 小農受保護首頁：顯示農場資料 + 立即生效的「電話/描述」編輯（驗證小農 PUT /me）。
import { store } from '../store.js';

export default {
    name: 'FarmerHome',
    data() { return { store, edit: { farmerPhoneNum: '', farmDesc: '' }, msg: '' }; },
    computed: { p() { return this.store.farmer.profile || {}; } },
    mounted() {
        const p = this.store.farmer.profile;
        if (p) { this.edit.farmerPhoneNum = p.farmerPhoneNum; this.edit.farmDesc = p.farmDesc; }
    },
    methods: {
        async save() {
            this.msg = '';
            try { await store.farmerUpdateContact({ ...this.edit }); this.msg = '已更新'; }
            catch (e) { this.msg = '更新失敗'; }
        },
    },
    template: `
    <div>
      <h2>小農中心</h2>
      <section class="box">
        <p><b>農場：</b>{{ p.farmName }}</p>
        <p><b>Email：</b>{{ p.email }}</p>
        <p><b>地址：</b>{{ p.cityName }}{{ p.distName }} {{ p.farmAddress }}</p>
        <p><b>帳號狀態：</b>{{ p.farmerStatus }}</p>
        <p><b>最新審核狀態：</b>{{ p.reviewStatus || '—' }}（第 {{ p.reviewRound || '—' }} 輪）</p>
      </section>
      <section class="box">
        <h3>修改聯絡資訊（立即生效）</h3>
        <label>電話 <input v-model="edit.farmerPhoneNum" /></label>
        <label>農場描述 <textarea v-model="edit.farmDesc" rows="3"></textarea></label>
        <button @click="save">儲存</button> <span class="ok">{{ msg }}</span>
      </section>
      <p style="font-size:12px;color:#888">農場名／地址／區域／座標／證明文件屬「審核欄位」，修改＝重新送審，之後再做完整表單。</p>
    </div>
  `,
};