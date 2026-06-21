// 會員受保護頁：看資料 / 改資料（含縣市行政區下拉）/ 改密碼（含顯示眼睛）/ 註銷。
import { store } from '../store.js';
import { navigate } from '../router.js';
import DistrictPicker from './DistrictPicker.js';   // 縣市→行政區 兩層下拉
import PasswordField from './PasswordField.js';     // 密碼欄（含顯示/隱藏）

export default {
    name: 'MemberProfile',
    components: { DistrictPicker, PasswordField },
    data() {
        return {
            store,
            // 修改資料用的表單（含 districtId，交給 DistrictPicker 連動）
            edit: { userName: '', userNickname: '', userPhoneNum: '', userAddress: '', districtId: null, birthday: null },
            pw: { oldPassword: '', newPassword: '' },
            msgProfile: '', msgPw: '',
        };
    },
    computed: {
        p() { return this.store.member.profile || {}; },   // 方便模板取用
    },
    // 進頁時把目前的值帶進「修改表單」（含 districtId → 下拉會自動預選）
    mounted() {
        const p = this.store.member.profile;
        if (p) {
            this.edit.userName = p.userName;
            this.edit.userNickname = p.userNickname;
            this.edit.userPhoneNum = p.userPhoneNum;
            this.edit.userAddress = p.userAddress;
            this.edit.districtId = p.districtId;   // ★ 後端現在會回 districtId，下拉才能預選
            this.edit.birthday = p.birthday;
        }
    },
    methods: {
        async saveProfile() {
            this.msgProfile = '';
            try {
                await store.memberUpdate({ ...this.edit, birthday: this.edit.birthday || null });
                this.msgProfile = '已更新';
            } catch (e) {
                this.msgProfile = e.status === 400 ? '欄位格式不符' : '更新失敗';
            }
        },
        async savePassword() {
            this.msgPw = '';
            // 前端先做基本檢查（真正把關在後端 DTO 的 @Size）
            if ((this.pw.newPassword || '').length < 8) { this.msgPw = '新密碼至少 8 碼'; return; }
            try {
                await store.memberChangePassword(this.pw.oldPassword, this.pw.newPassword);
                this.msgPw = '密碼已更新，下次請用新密碼登入';
                this.pw.oldPassword = ''; this.pw.newPassword = '';
            } catch (e) {
                this.msgPw = e.status === 400 ? '密碼格式不符（至少 8 碼）' : '更新失敗（舊密碼可能錯誤）';
            }
        },
        async removeAccount() {
            if (!window.confirm('確定要註銷帳號嗎？此動作無法復原')) return;
            try { await store.memberDelete(); navigate('/'); }
            catch (e) { window.alert('註銷失敗'); }
        },
    },
    template: `
    <div v-if="store.member.profile">
      <h2>我的會員資料</h2>

      <section class="box">
        <div class="rw"><span class="k">Email</span><span>{{ p.email }}</span></div>
        <div class="rw"><span class="k">姓名</span><span>{{ p.userName }}（{{ p.userNickname || '—' }}）</span></div>
        <div class="rw"><span class="k">手機</span><span>{{ p.userPhoneNum || '—' }}</span></div>
        <div class="rw"><span class="k">地址</span><span>{{ p.cityName }}{{ p.distName }} {{ p.userAddress || '' }}</span></div>
        <div class="rw"><span class="k">消費級距</span><span><span class="tier">{{ p.spendingTier || '—' }}</span>（本月 {{ p.monthlySpending }} 元）</span></div>
        <div class="rw" style="border:none"><span class="k">帳號狀態 / 登入方式</span><span>{{ p.userStatus }} · {{ p.authProvider }}</span></div>
      </section>

      <section class="box">
        <h3>修改資料</h3>
        <div class="form-grid">
          <label>姓名 <input v-model="edit.userName" /></label>
          <label>暱稱 <input v-model="edit.userNickname" /></label>
          <label>手機 <input v-model="edit.userPhoneNum" /></label>
          <label>所在地區 <district-picker v-model="edit.districtId"></district-picker></label>
          <label>詳細地址 <input v-model="edit.userAddress" /></label>
          <label>生日 <input v-model="edit.birthday" type="date" /></label>
          <div><button @click="saveProfile">儲存</button> <span class="ok">{{ msgProfile }}</span></div>
        </div>
      </section>

      <section class="box">
        <h3>修改密碼</h3>
        <div class="form-grid">
          <label v-if="p.hasPassword">舊密碼 <password-field v-model="pw.oldPassword" placeholder="舊密碼"></password-field></label>
          <label>新密碼 <password-field v-model="pw.newPassword" placeholder="至少 8 碼"></password-field></label>
          <div><button @click="savePassword">更新密碼</button> <span class="ok">{{ msgPw }}</span></div>
        </div>
      </section>

      <section class="box">
        <h3>註銷帳號</h3>
        <button style="background:#c0392b" @click="removeAccount">我要註銷</button>
      </section>
    </div>
  `,
};