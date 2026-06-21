// 可重複使用的「縣市 → 行政區」兩層下拉。
// 用法：<district-picker v-model="reg.districtId"></district-picker>
//   選好行政區後，會把該行政區的 districtId 透過 v-model 傳回父元件。
import { api } from '../api.js';

export default {
    name: 'DistrictPicker',
    props: { modelValue: { default: null } },     // 父元件綁的 districtId
    data() {
        return { rows: [], selectedCity: '', loading: true };
    },
    computed: {
        // 取出不重複的縣市（保留原本順序）
        cities() {
            const seen = [];
            this.rows.forEach((r) => { if (!seen.includes(r.cityName)) seen.push(r.cityName); });
            return seen;
        },
        // 目前所選縣市底下的行政區
        districts() {
            return this.rows.filter((r) => r.cityName === this.selectedCity);
        },
    },
    async mounted() {
        try {
            // 進來時撈一次全部（355 筆），之後切換都在前端處理、不再打後端
            this.rows = await api.get('/city-districts');
            // 若一開始就有 districtId（例如編輯資料），自動把對應縣市選起來
            if (this.modelValue) {
                const row = this.rows.find((r) => r.districtId === this.modelValue);
                if (row) this.selectedCity = row.cityName;
            }
        } finally { this.loading = false; }
    },
    methods: {
        // 換縣市 → 清掉已選的行政區（districtId 設回 null）
        onCityChange() { this.$emit('update:modelValue', null); },
        // 選行政區 → 把 districtId 傳回父元件
        onDistrictChange(e) {
            this.$emit('update:modelValue', e.target.value ? Number(e.target.value) : null);
        },
    },
    template: `
    <div style="display:flex; gap:8px">
      <select v-model="selectedCity" @change="onCityChange" style="flex:1">
        <option value="">請選擇縣市</option>
        <option v-for="c in cities" :key="c" :value="c">{{ c }}</option>
      </select>
      <select :value="modelValue || ''" @change="onDistrictChange" :disabled="!selectedCity" style="flex:1">
        <option value="">請選擇行政區</option>
        <option v-for="d in districts" :key="d.districtId" :value="d.districtId">{{ d.distName }}</option>
      </select>
    </div>
  `,
};