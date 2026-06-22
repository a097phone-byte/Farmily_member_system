// 三區（會員 / 小農 / 管理員）共用的 sidebar 儀表板外殼。
// 父層傳入 navItems / 使用者資訊 / 標題 / 通知；內容放進預設 slot。
// 用法：
//   <dashboard-layout brand="Farmily" :nav-items="items" :user-name="name" role="會員"
//                     :title="title" :notifications="notes" @logout="onLogout">
//     <component :is="page"></component>
//   </dashboard-layout>
import Icon from './Icon.js';
import Avatar from './Avatar.js';
import NotificationBell from './NotificationBell.js';
import { router, navigate } from '../router.js';

export default {
    name: 'DashboardLayout',
    components: { Icon, Avatar, NotificationBell },
    props: {
        brand: { type: String, default: 'Farmily' },
        navItems: { type: Array, default: () => [] },   // [{ path, label, icon, show? }]
        userName: { type: String, default: '' },
        role: { type: String, default: '' },
        title: { type: String, default: '' },
        notifications: { type: Array, default: null },   // null = 不顯示鈴鐺
        narrow: { type: Boolean, default: false },        // 內容區是否窄版（設定頁用）
    },
    emits: ['logout'],
    data() { return { router, mobileOpen: false }; },
    computed: {
        visibleItems() { return this.navItems.filter((i) => i.show !== false); },
    },
    methods: {
        go(path) { navigate(path); this.mobileOpen = false; },
        isActive(it) { return this.router.path === it.path; },
    },
    template: `
    <div class="shell">
      <div v-if="mobileOpen" class="scrim" @click="mobileOpen=false"></div>

      <aside class="sidebar" :class="{ open: mobileOpen }">
        <div class="brand" @click="go(visibleItems[0] ? visibleItems[0].path : '/')">
          <span class="dot"></span>{{ brand }}
        </div>
        <nav class="side-nav">
          <a v-for="it in visibleItems" :key="it.path" href="javascript:void(0)"
             :class="{ active: isActive(it) }" @click="go(it.path)">
            <icon :name="it.icon"></icon>{{ it.label }}
          </a>
        </nav>
        <div class="side-foot">
          <div class="side-user">
            <avatar :name="userName" size="sm"></avatar>
            <div class="meta">
              <div class="nm">{{ userName }}</div>
              <div class="role">{{ role }}</div>
            </div>
          </div>
          <button class="btn ghost block" @click="$emit('logout')"><icon name="logout"></icon>登出</button>
        </div>
      </aside>

      <div class="main">
        <header class="topbar">
          <div style="display:flex;align-items:center;gap:10px">
            <button class="hamburger" @click="mobileOpen=!mobileOpen" aria-label="選單"><icon name="menu"></icon></button>
            <span class="title">{{ title }}</span>
          </div>
          <div class="right">
            <notification-bell v-if="notifications" :items="notifications"></notification-bell>
            <slot name="topbar-right"></slot>
          </div>
        </header>
        <div class="page" :class="{ 'page-narrow': narrow }">
          <slot></slot>
        </div>
      </div>
    </div>
  `,
};
