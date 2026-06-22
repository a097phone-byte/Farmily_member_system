// 全站共用的 UI 狀態：toast 通知 + confirm 確認對話框。
// confirm=null 代表沒有對話框；有值時 <confirm-dialog> 會跳出來。
export const ui = Vue.reactive({ toasts: [], confirm: null });

let seq = 0;

// 程式化確認對話框（取代 window.confirm，樣式統一）。回傳 Promise<boolean>。
// 用法：if (!(await confirmDialog({ title:'…', message:'…', variant:'danger' }))) return;
export function confirmDialog(opts) {
    return new Promise((resolve) => {
        ui.confirm = {
            title: opts.title || '請確認',
            message: opts.message || '',
            confirmText: opts.confirmText || '確認',
            cancelText: opts.cancelText || '取消',
            variant: opts.variant || 'primary',   // 'primary' | 'danger'
            resolve,
        };
    });
}

// toast('已儲存')            → 綠色成功
// toast('更新失敗', 'err')   → 紅色錯誤
// toast('提醒…', 'warn')     → 黃色提醒
export function toast(message, type = 'ok', ms = 2800) {
    const id = ++seq;
    ui.toasts.push({ id, message, type });
    setTimeout(() => {
        const i = ui.toasts.findIndex((t) => t.id === id);
        if (i >= 0) ui.toasts.splice(i, 1);
    }, ms);
}