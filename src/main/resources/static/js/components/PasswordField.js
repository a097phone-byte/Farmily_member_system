// 可重複使用的「密碼輸入框 + 顯示/隱藏眼睛」元件。
// 用法：<password-field v-model="login.password" placeholder="密碼"></password-field>
// （Vue 3 自訂元件的 v-model = 接收 props.modelValue、發出 update:modelValue 事件）
export default {
    name: 'PasswordField',
    props: {
        modelValue: { type: String, default: '' },
        placeholder: { type: String, default: '' },
    },
    data() { return { show: false }; },           // show=true 時把密碼變明文
    methods: {
        onInput(e) { this.$emit('update:modelValue', e.target.value); },
    },
    template: `
    <div class="pw-wrap">
      <input :type="show ? 'text' : 'password'"
             :value="modelValue" @input="onInput"
             :placeholder="placeholder" autocomplete="off" />
      <button type="button" class="pw-toggle" @click="show = !show"
              :aria-label="show ? '隱藏密碼' : '顯示密碼'">
        <!-- 眼睛 / 劃掉的眼睛，inline SVG 不依賴 icon 字型 -->
        <svg v-if="!show" viewBox="0 0 24 24" width="20" height="20" fill="none" stroke="currentColor" stroke-width="2">
          <path d="M1 12s4-7 11-7 11 7 11 7-4 7-11 7S1 12 1 12z"/><circle cx="12" cy="12" r="3"/>
        </svg>
        <svg v-else viewBox="0 0 24 24" width="20" height="20" fill="none" stroke="currentColor" stroke-width="2">
          <path d="M17.94 17.94A10.07 10.07 0 0 1 12 20C5 20 1 12 1 12a18.45 18.45 0 0 1 5.06-5.94M9.9 4.24A9.12 9.12 0 0 1 12 4c7 0 11 8 11 8a18.5 18.5 0 0 1-2.16 3.19m-6.72-1.07a3 3 0 1 1-4.24-4.24"/><line x1="1" y1="1" x2="23" y2="23"/>
        </svg>
      </button>
    </div>
  `,
};