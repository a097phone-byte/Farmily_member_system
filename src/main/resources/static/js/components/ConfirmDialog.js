// 全站共用的確認對話框（modals-07 風）。由 ui.confirm 驅動，每個 App 放一個 <confirm-dialog>。
// 透過 confirmDialog() 程式化呼叫，取代 window.confirm（樣式統一、可區分危險動作）。
import { ui } from '../ui.js';

export default {
    name: 'ConfirmDialog',
    data() { return { ui }; },
    computed: { c() { return this.ui.confirm; } },
    methods: {
        pick(val) {
            const c = this.ui.confirm;
            this.ui.confirm = null;
            if (c && c.resolve) c.resolve(val);
        },
    },
    template: `
    <div v-if="c" class="modal-backdrop" @click.self="pick(false)">
      <div class="modal" style="max-width:420px">
        <div class="modal-head"><h3>{{ c.title }}</h3><button class="modal-x" @click="pick(false)" aria-label="關閉">✕</button></div>
        <div class="modal-body"><p style="margin:0" class="muted">{{ c.message }}</p></div>
        <div class="modal-foot">
          <button class="btn ghost" @click="pick(false)">{{ c.cancelText }}</button>
          <button class="btn" :class="{ danger: c.variant === 'danger' }" @click="pick(true)">{{ c.confirmText }}</button>
        </div>
      </div>
    </div>
  `,
};
