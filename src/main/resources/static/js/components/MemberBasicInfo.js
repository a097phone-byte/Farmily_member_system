// 會員「基本資料」分頁（account-basic-info 風）：唯讀摘要 + 可編輯表單 + 註銷危險區。
// 串接：GET 已由 store.memberFetchMe 還原；PUT /api/member/me（store.memberUpdate）；DELETE /api/member/me。
import { store } from '../store.js';
import { navigate } from '../router.js';
import { toast, confirmDialog } from '../ui.js';
import DistrictPicker from './DistrictPicker.js';
import Avatar from './Avatar.js';

export default {
    name: 'MemberBasicInfo',
    components: { DistrictPicker, Avatar },
    data() {
        return {
            store, saving: false,
            edit: { userName: '', userNickname: '', userPhoneNum: '', userAddress: '', districtId: null, birthday: null },
        };
    },
    computed: { p() { return this.store.member.profile || {}; } },
    mounted() {
        const p = this.store.member.profile;
        if (p) {
            this.edit.userName = p.userName;
            this.edit.userNickname = p.userNickname;
            this.edit.userPhoneNum = p.userPhoneNum;
            this.edit.userAddress = p.userAddress;
            this.edit.districtId = p.districtId;
            this.edit.birthday = p.birthday;
        }
    },
    methods: {
        async saveProfile() {
            this.saving = true;
            try {
                await store.memberUpdate({ ...this.edit, birthday: this.edit.birthday || null });
                toast('資料已更新');
            } catch (e) {
                toast(e.status === 400 ? '欄位格式不符' : (e.message || '更新失敗'), 'err');
            } finally { this.saving = false; }
        },
        async removeAccount() {
            const ok = await confirmDialog({
                title: '註銷帳號',
                message: '確定要註銷帳號嗎？此動作無法復原。',
                variant: 'danger', confirmText: '確認註銷',
            });
            if (!ok) return;
            try { await store.memberDelete(); navigate('/'); }
            catch (e) { toast(e.message || '註銷失敗', 'err'); }
        },
    },
    template: `
    <div>
      <!-- 摘要 -->
      <div class="card">
        <div class="card-body" style="display:flex;align-items:center;gap:16px">
          <avatar :name="p.userName" size="lg"></avatar>
          <div style="flex:1;min-width:0">
            <h3 style="margin:0">{{ p.userName }}<span v-if="p.userNickname" class="muted">（{{ p.userNickname }}）</span></h3>
            <div class="muted">{{ p.email }}</div>
          </div>
          <div style="text-align:right">
            <span class="badge info">{{ p.spendingTier || '—' }}</span>
            <div class="muted" style="font-size:12px;margin-top:4px">本月消費 {{ p.monthlySpending }} 元</div>
          </div>
        </div>
      </div>

      <!-- 可編輯 -->
      <div class="card">
        <div class="card-head"><h3>基本資料</h3><div class="sub">更新你的聯絡與個人資訊</div></div>
        <div class="card-body">
          <div class="form-rows">
            <div class="form-row"><div class="lbl">姓名</div><input v-model="edit.userName" /></div>
            <div class="form-row"><div class="lbl">暱稱</div><input v-model="edit.userNickname" /></div>
            <div class="form-row"><div class="lbl">手機</div><input v-model="edit.userPhoneNum" /></div>
            <div class="form-row"><div class="lbl">所在地區</div><district-picker v-model="edit.districtId"></district-picker></div>
            <div class="form-row"><div class="lbl">詳細地址</div><input v-model="edit.userAddress" /></div>
            <div class="form-row"><div class="lbl">生日</div><input v-model="edit.birthday" type="date" /></div>
            <div class="form-row"><div class="lbl">帳號狀態 <small>登入方式 {{ p.authProvider }}</small></div><div><span class="badge dot s-active">{{ p.userStatus }}</span></div></div>
          </div>
        </div>
        <div class="card-foot">
          <button class="btn" :disabled="saving" @click="saveProfile">{{ saving ? '儲存中…' : '儲存變更' }}</button>
        </div>
      </div>

      <!-- 危險區 -->
      <div class="card danger-zone">
        <div class="card-head"><h3>註銷帳號</h3><div class="sub">永久刪除你的帳號與資料，無法復原</div></div>
        <div class="card-foot" style="justify-content:flex-start">
          <button class="btn danger" @click="removeAccount">我要註銷帳號</button>
        </div>
      </div>
    </div>
  `,
};
