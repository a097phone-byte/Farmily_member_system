package com.farmily.user.dto;

// 管理員修改「自己」時可送的資料（只開放安全欄位：名字）
public class AdminSelfUpdateRequest {

    private String name;   // 新名字

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
}