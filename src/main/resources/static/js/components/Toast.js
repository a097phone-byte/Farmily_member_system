// 通知容器：固定在畫面右下角，把 ui.toasts 一條條畫出來。
// 用法：在 App 模板最外層放一個 <toast></toast> 即可（每個頁面放一次）。
import { ui } from '../ui.js';

export default {
    name: 'Toast',
    data() { return { ui }; },
    template: `
    <div class="toast-wrap">
      <div v-for="t in ui.toasts" :key="t.id" class="toast" :class="t.type">{{ t.message }}</div>
    </div>
  `,
};