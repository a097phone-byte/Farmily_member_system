// 會員「修改密碼」分頁（account-change-password 風）。
// 串接：PUT /api/member/me/password（store.memberChangePassword）。
// Google 帳號（hasPassword=false）沒有舊密碼可驗，隱藏舊密碼欄。
import { store } from '../store.js';
import { toast } from '../ui.js';
import PasswordField from './PasswordField.js';

export default {
    name: 'MemberPassword',
    components: { PasswordField },
    data() { return { store, pw: { oldPassword: '', newPassword: '', confirm: '' }, saving: false }; },
    computed: { hasPassword() { return this.store.member.profile ? this.store.member.profile.hasPassword : true; } },
    methods: {
        async save() {
            if ((this.pw.newPassword || '').length < 8) { toast('新密碼至少 8 碼', 'warn'); return; }
            if (this.pw.newPassword !== this.pw.confirm) { toast('兩次新密碼不一致', 'warn'); return; }
            this.saving = true;
            try {
                await store.memberChangePassword(this.pw.oldPassword, this.pw.newPassword);
                toast('密碼已更新，下次請用新密碼登入');
                this.pw = { oldPassword: '', newPassword: '', confirm: '' };
            } catch (e) {
                toast(e.status === 400 ? '密碼格式不符（至少 8 碼）' : '更新失敗（舊密碼可能錯誤）', 'err');
            } finally { this.saving = false; }
        },
    },
    template: `
    <div class="card">
      <div class="card-head"><h3>修改密碼</h3><div class="sub">為了帳號安全，建議定期更換密碼</div></div>
      <div class="card-body">
        <div class="form-rows">
          <div class="form-row" v-if="hasPassword"><div class="lbl">目前密碼</div><password-field v-model="pw.oldPassword" placeholder="目前密碼"></password-field></div>
          <div class="form-row"><div class="lbl">新密碼 <small>至少 8 碼</small></div><password-field v-model="pw.newPassword" placeholder="新密碼"></password-field></div>
          <div class="form-row"><div class="lbl">確認新密碼</div><password-field v-model="pw.confirm" placeholder="再輸入一次"></password-field></div>
        </div>
      </div>
      <div class="card-foot">
        <button class="btn" :disabled="saving" @click="save">{{ saving ? '更新中…' : '更新密碼' }}</button>
      </div>
    </div>
  `,
};
