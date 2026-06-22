// 拖曳上傳元件（modals-05 風）：虛線框 + 上傳圖示 + 點擊/拖放選檔 + 圖片縮圖預覽 + 移除。
// v-model 綁定一個 File 物件（父層負責 base64 轉換與送出）。
// 用法：<file-drop v-model="files.certFileLand" label="土地證明"></file-drop>
import Icon from './Icon.js';

export default {
    name: 'FileDrop',
    components: { Icon },
    props: {
        modelValue: { default: null },                 // File | null
        label: { type: String, default: '' },
        accept: { type: String, default: 'image/*' },
    },
    emits: ['update:modelValue'],
    data() { return { over: false, previewUrl: null }; },
    watch: { modelValue(v) { this.makePreview(v); } },
    mounted() { this.makePreview(this.modelValue); },
    beforeUnmount() { if (this.previewUrl) URL.revokeObjectURL(this.previewUrl); },
    methods: {
        makePreview(f) {
            if (this.previewUrl) { URL.revokeObjectURL(this.previewUrl); this.previewUrl = null; }
            if (f && f.type && f.type.startsWith('image/')) this.previewUrl = URL.createObjectURL(f);
        },
        browse() { this.$refs.input.click(); },
        onPick(e) { const f = e.target.files[0]; if (f) this.$emit('update:modelValue', f); },
        onDrop(e) { this.over = false; const f = e.dataTransfer.files[0]; if (f) this.$emit('update:modelValue', f); },
        clear() { this.$emit('update:modelValue', null); if (this.$refs.input) this.$refs.input.value = ''; },
    },
    template: `
    <div class="filedrop" :class="{ over }"
         @dragover.prevent="over=true" @dragleave.prevent="over=false" @drop.prevent="onDrop" @click="browse">
      <input ref="input" type="file" :accept="accept" @change="onPick" hidden />
      <template v-if="modelValue">
        <img v-if="previewUrl" :src="previewUrl" class="filedrop-thumb" alt="" />
        <span v-else class="filedrop-thumb ph"><icon name="image"></icon></span>
        <div class="filedrop-meta">
          <div class="nm">{{ modelValue.name }}</div>
          <button type="button" class="btn ghost sm" @click.stop="clear">移除</button>
        </div>
      </template>
      <template v-else>
        <icon name="upload"></icon>
        <div class="filedrop-text"><b>點擊或拖曳檔案到此</b><span>{{ label }}（支援圖片）</span></div>
      </template>
    </div>
  `,
};
