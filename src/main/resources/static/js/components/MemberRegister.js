// 會員註冊頁。欄位名稱嚴格對齊後端 UserRegisterRequest（一字不差才不會被忽略）。
import { api } from '../api.js';
import { navigate } from '../router.js';

export default {
    name: 'MemberRegister',
    data() {
        return {
            // 對齊 UserRegisterRequest
            form: {
                email: '', password: '', userName: '', userNickname: '',
                userPhoneNum: '', userAddress: '', districtId: null, birthday: null,
            },
            loading: false, errorMsg: '', okMsg: '',
        };
    },
    methods: {
        async onSubmit() {
            this.errorMsg = ''; this.okMsg = ''; this.loading = true;
            try {
                // 生日空字串要轉成 null，避免後端解析失敗
                const payload = { ...this.form, birthday: this.form.birthday || null };
                await api.post('/member/register', payload);
                this.okMsg = '註冊成功！即將前往登入…';
                setTimeout(() => navigate('/member/login'), 800);
            } catch (e) {
                this.errorMsg = e.status === 409 ? 'Email 已被使用' : '註冊失敗，請檢查欄位';
            } finally { this.loading = false; }
        },
    },
    template: `
    <div class="auth">
      <h2>會員註冊</h2>
      <form @submit.prevent="onSubmit">
        <label>Email* <input v-model="form.email" type="email" required /></label>
        <label>密碼* <input v-model="form.password" type="password" required /></label>
        <label>姓名* <input v-model="form.userName" required /></label>
        <label>暱稱 <input v-model="form.userNickname" /></label>
        <label>手機 <input v-model="form.userPhoneNum" /></label>
        <label>地址 <input v-model="form.userAddress" /></label>
        <label>行政區 ID <input v-model.number="form.districtId" type="number" placeholder="暫填數字（後端尚無區域清單 API）" /></label>
        <label>生日 <input v-model="form.birthday" type="date" /></label>
        <p v-if="errorMsg" class="err">{{ errorMsg }}</p>
        <p v-if="okMsg" class="ok">{{ okMsg }}</p>
        <button type="submit" :disabled="loading">{{ loading ? '送出中…' : '註冊' }}</button>
      </form>
      <a href="#/member/login">已有帳號？前往登入</a>
    </div>
  `,
};