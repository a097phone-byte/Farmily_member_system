// 極簡單色線性 icon（feather 風）。取代全站 emoji。
// 用法：<icon name="user"></icon>  /  <icon name="bell" size="sm"></icon>
// 每個 icon 是 24x24 viewBox 的內層 SVG 標記，stroke 用 currentColor（跟著文字色）。
const PATHS = {
    home: '<path d="M3 9.5 12 3l9 6.5"/><path d="M5 9v11h14V9"/>',
    grid: '<rect x="4" y="4" width="7" height="7" rx="1.5"/><rect x="13" y="4" width="7" height="7" rx="1.5"/><rect x="4" y="13" width="7" height="7" rx="1.5"/><rect x="13" y="13" width="7" height="7" rx="1.5"/>',
    user: '<circle cx="12" cy="8" r="4"/><path d="M4 21c0-4 4-6 8-6s8 2 8 6"/>',
    users: '<circle cx="9" cy="8" r="3.5"/><path d="M3 20c0-3.3 2.7-5 6-5s6 1.7 6 5"/><path d="M16 5.2a3.5 3.5 0 0 1 0 6.6"/><path d="M18.5 20c0-2.6-1-4.2-2.5-4.9"/>',
    lock: '<rect x="4" y="11" width="16" height="9" rx="2"/><path d="M8 11V8a4 4 0 0 1 8 0v3"/>',
    link: '<path d="m9 15 6-6"/><path d="M11 6.5 13 4.5a4 4 0 0 1 6 6l-2 2"/><path d="M13 17.5 11 19.5a4 4 0 0 1-6-6l2-2"/>',
    leaf: '<path d="M11 20c0-7 3-12 10-13-1 7-4 11-10 13z"/><path d="M11 20C8 16 5 14 3 14c1 4 4 6 8 6z"/><path d="M11 20v-7"/>',
    clipboard: '<rect x="5" y="4.5" width="14" height="16.5" rx="2"/><rect x="9" y="3" width="6" height="3.5" rx="1"/><path d="M8.5 11h7M8.5 15h5"/>',
    shield: '<path d="M12 3 5 6v6c0 4 3 7 7 9 4-2 7-5 7-9V6z"/>',
    cart: '<circle cx="9" cy="20" r="1.4"/><circle cx="17" cy="20" r="1.4"/><path d="M3 4h2l2.4 12h10l1.8-8H6.2"/>',
    bell: '<path d="M6 9a6 6 0 0 1 12 0c0 5 2 6 2 6H4s2-1 2-6z"/><path d="M10 20a2 2 0 0 0 4 0"/>',
    search: '<circle cx="11" cy="11" r="7"/><path d="m20 20-3.2-3.2"/>',
    logout: '<path d="M14 4h3a2 2 0 0 1 2 2v12a2 2 0 0 1-2 2h-3"/><path d="M9 12h11"/><path d="m16 8 4 4-4 4"/>',
    menu: '<path d="M4 6h16M4 12h16M4 18h16"/>',
    chevron: '<path d="m9 6 6 6-6 6"/>',
    x: '<path d="M6 6l12 12M18 6 6 18"/>',
    mail: '<rect x="3" y="5" width="18" height="14" rx="2"/><path d="m3.5 7 8.5 6 8.5-6"/>',
    check: '<path d="M5 12.5 10 17 19 7"/>',
    eye: '<path d="M1 12s4-7 11-7 11 7 11 7-4 7-11 7S1 12 1 12z"/><circle cx="12" cy="12" r="3"/>',
    plus: '<path d="M12 5v14M5 12h14"/>',
    clock: '<circle cx="12" cy="12" r="8"/><path d="M12 8v4l3 2"/>',
    upload: '<path d="M12 16V5"/><path d="m7 9 5-5 5 5"/><path d="M5 20h14"/>',
    image: '<rect x="4" y="5" width="16" height="14" rx="2"/><circle cx="9" cy="10" r="1.6"/><path d="m5 17 4-4 4 4 3-3 3 3"/>',
};

export default {
    name: 'Icon',
    props: {
        name: { type: String, required: true },
        size: { type: String, default: '' },   // '', 'sm', 'lg'
    },
    computed: {
        inner() { return PATHS[this.name] || ''; },
        cls() { return 'icon' + (this.size ? ' ' + this.size : ''); },
    },
    template: `<svg :class="cls" viewBox="0 0 24 24" v-html="inner" aria-hidden="true"></svg>`,
};
