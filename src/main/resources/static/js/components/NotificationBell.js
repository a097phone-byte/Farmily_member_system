// topbar 通知鈴鐺 + 下拉清單。給會員/小農的「sidebar-with-notification」版型。
// 目前通知是示意（假）資料，由父層以 items 傳入；之後有通知 API 再換成真的。
import Icon from './Icon.js';

export default {
    name: 'NotificationBell',
    components: { Icon },
    props: { items: { type: Array, default: () => [] } },
    data() { return { open: false }; },
    computed: { hasUnread() { return this.items.length > 0; } },
    methods: {
        toggle() { this.open = !this.open; },
        close() { this.open = false; },
    },
    template: `
    <div class="bell">
      <button class="bell-btn" @click="toggle" aria-label="通知">
        <icon name="bell"></icon>
        <span v-if="hasUnread" class="dot-badge"></span>
      </button>
      <template v-if="open">
        <div style="position:fixed;inset:0;z-index:39" @click="close"></div>
        <div class="menu">
          <div class="mh">通知</div>
          <div v-if="!items.length" class="item"><div class="t muted">目前沒有通知</div></div>
          <div v-for="(n, i) in items" :key="i" class="item">
            <div>
              <div class="t">{{ n.text }}</div>
              <div class="ts">{{ n.time }}</div>
            </div>
          </div>
        </div>
      </template>
    </div>
  `,
};
