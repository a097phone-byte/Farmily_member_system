package com.farmily.user.dto;

import jakarta.validation.constraints.NotBlank;

public class ReviewRejectRequest {

    @NotBlank
    private String rejectReason;

    public String getRejectReason() {
        return rejectReason;
    }

    public void setRejectReason(String rejectReason) {
        this.rejectReason = rejectReason;
    }
}
