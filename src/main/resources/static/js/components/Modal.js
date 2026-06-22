// 可重複使用的彈窗外框。內容用 slot 由父元件填入，按鈕區用具名 slot #foot。
// 用法：
//   <modal v-if="open" title="標題" @close="open=false">
//     <p>主體內容</p>
//     <template #foot><button @click="open=false">關閉</button></template>
//   </modal>
export default {
    name: 'Modal',
    props: {
        title: { type: String, default: '' },
        wide: { type: Boolean, default: false },   // wide=true 給內容較多的審核/文件預覽用
    },
    emits: ['close'],
    methods: {
        // 點背景（非卡片本身）才關閉
        onBackdrop(e) { if (e.target === e.currentTarget) this.$emit('close'); },
    },
    template: `
    <div class="modal-backdrop" @click="onBackdrop">
      <div class="modal" :class="{ wide }">
        <div class="modal-head">
          <h3>{{ title }}</h3>
          <button class="modal-x" @click="$emit('close')" aria-label="關閉">✕</button>
        </div>
        <div class="modal-body"><slot></slot></div>
        <div class="modal-foot"><slot name="foot"></slot></div>
      </div>
    </div>
  `,
};