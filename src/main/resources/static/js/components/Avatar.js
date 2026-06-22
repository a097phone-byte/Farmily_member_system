// 姓名首字圓形頭像（無圖檔，純色塊 + 首字）。用於 sidebar 與 list-table。
// 用法：<avatar :name="user.userName" size="sm"></avatar>
export default {
    name: 'Avatar',
    props: {
        name: { type: String, default: '' },
        size: { type: String, default: '' },   // '', 'sm', 'lg'
    },
    computed: {
        initial() {
            const s = (this.name || '').trim();
            return s ? s.charAt(0) : '?';
        },
        cls() { return 'avatar' + (this.size ? ' ' + this.size : ''); },
    },
    template: `<span :class="cls">{{ initial }}</span>`,
};
