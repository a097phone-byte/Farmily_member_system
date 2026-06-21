// 這支檔案：全站「共用狀態」。三條登入線（會員 / 小農 / 管理員）各自的登入資料都放這。
// 用 Vue.reactive 把物件變成「響應式」——資料一改，畫面會自動更新（這就是 Vue 的核心）。

import { api } from './api.js';

export const store = Vue.reactive({
    // 三條線各自的狀態。profile=null 代表未登入；loaded 代表「是否已用 cookie 確認過」
    member: { profile: null, loaded: false },
    farmer: { profile: null, loaded: false },
    admin:  { profile: null, loaded: false },

    // ========== 會員 ==========
    async memberLogin(email, password) {
        this.member.profile = await api.post('/member/login', { email, password });
        this.member.loaded = true;
    },
    async memberFetchMe() {                       // 用既有 cookie 取回自己的資料（重新整理後還原登入）
        try { this.member.profile = await api.get('/member/me'); }
        catch (e) { this.member.profile = null; throw e; }
        finally { this.member.loaded = true; }
    },
    async memberUpdate(payload) {                 // 欄位對齊 UserUpdateRequest
        this.member.profile = await api.put('/member/me', payload);
    },
    async memberChangePassword(oldPassword, newPassword) {
        return api.put('/member/me/password', { oldPassword, newPassword });
    },
    async memberDelete() {
        await api.del('/member/me');
        this.member.profile = null;
    },

    // ========== 小農 ==========
    async farmerLogin(email, password) {
        this.farmer.profile = await api.post('/farmer/login', { email, password });
        this.farmer.loaded = true;
    },
    async farmerFetchMe() {
        try { this.farmer.profile = await api.get('/farmer/me'); }
        catch (e) { this.farmer.profile = null; throw e; }
        finally { this.farmer.loaded = true; }
    },
    async farmerUpdateContact(payload) {          // 立即生效：FarmerProfileUpdateRequest = farmerPhoneNum / farmDesc
        this.farmer.profile = await api.put('/farmer/me', payload);
    },

    // ========== 管理員 ==========
    async adminLogin(email, password) {
        this.admin.profile = await api.post('/admin/login', { email, password });
        this.admin.loaded = true;
    },
    async adminFetchMe() {
        try { this.admin.profile = await api.get('/admin/me'); }
        catch (e) { this.admin.profile = null; throw e; }
        finally { this.admin.loaded = true; }
    },
    async adminUpdateName(name) {                 // AdminSelfUpdateRequest = { name }
        this.admin.profile = await api.put('/admin/me', { name });
    },
    // 權限判斷：有 ADMIN（超級管理員）一律放行；否則需擁有指定代碼之一。
    // 對齊後端規則：members→(ADMIN|MEMBER)、farmers/reviews→(ADMIN|FARMER)、admins→ADMIN
    adminHasPerm(...codes) {
        const mine = this.admin.profile ? (this.admin.profile.permissionCodes || []) : [];
        if (mine.includes('ADMIN')) return true;
        return codes.some((c) => mine.includes(c));
    },

    // ========== 共用登出（三身分共用 /api/logout，會讓整個 session 失效） ==========
    async logout() {
        try { await api.post('/logout'); }
        finally {
            this.member.profile = null;
            this.farmer.profile = null;
            this.admin.profile  = null;
        }
    },
});