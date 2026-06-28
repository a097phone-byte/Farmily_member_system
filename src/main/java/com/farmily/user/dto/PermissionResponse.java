package com.farmily.user.dto;

import com.farmily.user.model.AdminRole;

// 回傳給前端的「權限定義」資料：前端用來動態產生可勾選的權限清單（不直接外露 entity）
public class PermissionResponse {

    private String code;          // 權限代碼，例如 MEMBER（對齊後端 PERM_MEMBER）
    private String name;          // 權限中文名稱，例如「會員管理」
    private String description;   // 權限說明

    // getter（setter 省略，輸出 JSON 用得到 getter 即可）
    public String getCode()        { return code; }
    public String getName()        { return name; }
    public String getDescription() { return description; }

    // 把 entity 轉成 DTO（跟其他 DTO 一樣用 from()）
    public static PermissionResponse from(AdminRole role) {
        PermissionResponse dto = new PermissionResponse();
        dto.code        = role.getPermissionCode();
        dto.name        = role.getPermissionName();
        dto.description = role.getDescription();
        return dto;
    }
}