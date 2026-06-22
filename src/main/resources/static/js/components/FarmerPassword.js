// 小農「修改密碼」分頁。串接：PUT /api/farmer/me/password（ChangePasswordRequest）。
// store 沒有對應 method，這裡直接用 api（保持 store 專注於 session 狀態）。
import { api } from '../api.js';
import { toast } from '../ui.js';
import PasswordField from './PasswordField.js';

export default {
    name: 'FarmerPassword',
    components: { PasswordField },
    data() { return { pw: { oldPassword: '', newPassword: '', confirm: '' }, saving: false }; },
    methods: {
        async save() {
            if ((this.pw.newPassword || '').length < 8) { toast('新密碼至少 8 碼', 'warn'); return; }
            if (this.pw.newPassword !== this.pw.confirm) { toast('兩次新密碼不一致', 'warn'); return; }
            this.saving = true;
            try {
                await api.put('/farmer/me/password', { oldPassword: this.pw.oldPassword, newPassword: this.pw.newPassword });
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
          <div class="form-row"><div class="lbl">目前密碼</div><password-field v-model="pw.oldPassword" placeholder="目前密碼"></password-field></div>
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
