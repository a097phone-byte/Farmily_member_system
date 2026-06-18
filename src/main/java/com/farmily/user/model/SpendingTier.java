package com.farmily.user.model;


import jakarta.persistence.*;

import java.io.Serializable;

@Entity
@Table(name = "spending_tier")
public class SpendingTier implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    @Column(name = "tier_id", updatable = false)
    private Integer tierId;

    @Column(name = "tier_name")
    private String tierName;

    @Column(name = "min_amount")
    private Integer minAmount;

    @Column(name = "max_amount")
    private Integer maxAmount;

    @Column(name = "description")
    private String description;


    public Integer getTierId() {
        return tierId;
    }

    public void setTierId(Integer tierId) {
        this.tierId = tierId;
    }

    public String getTierName() {
        return tierName;
    }

    public void setTierName(String tierName) {
        this.tierName = tierName;
    }

    public Integer getMinAmount() {
        return minAmount;
    }

    public void setMinAmount(Integer minAmount) {
        this.minAmount = minAmount;
    }

    public Integer getMaxAmount() {
        return maxAmount;
    }

    public void setMaxAmount(Integer maxAmount) {
        this.maxAmount = maxAmount;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
