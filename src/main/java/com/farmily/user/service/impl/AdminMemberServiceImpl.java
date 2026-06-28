package com.farmily.user.service.impl;

import com.farmily.user.dto.UserProfileResponse;
import com.farmily.user.model.SpendingTier;
import com.farmily.user.model.User;
import com.farmily.user.repository.SpendingTierRepository;
import com.farmily.user.repository.UserRepository;
import com.farmily.user.service.AdminMemberService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

// 管理員管理一般會員
@Service
@Transactional
public class AdminMemberServiceImpl implements AdminMemberService {

    private final UserRepository userRepository;
    private final SpendingTierRepository spendingTierRepository;

    public AdminMemberServiceImpl(UserRepository userRepository, SpendingTierRepository spendingTierRepository) {
        this.userRepository = userRepository;
        this.spendingTierRepository = spendingTierRepository;
    }

    // 列出所有會員
    @Override
    @Transactional(readOnly = true)
    public List<UserProfileResponse> listAll() {
        List<User> users = userRepository.findAll();

        // 消費級距表只查 1 次，迴圈內在記憶體比對，避免每筆會員都查一次 DB (n+1)
        List<SpendingTier> tiers = spendingTierRepository.findAll();
        List<UserProfileResponse> result = new ArrayList<>();

        for (User u : users) {
            Integer amount = u.getMonthlySpending() != null ? u.getMonthlySpending() : 0;
            String tierName = resolveTierName(tiers, amount);

            result.add(UserProfileResponse.from(u, tierName));
        }
        return result;
    }

    // 在記憶體用金額比對出級距名稱（找不到回傳 null）
    private String resolveTierName(List<SpendingTier> tiers, int amount) {
        for (SpendingTier tier : tiers) {
            boolean aboveMin = tier.getMinAmount() <= amount;
            boolean belowMax = tier.getMaxAmount() == null || amount <= tier.getMaxAmount();
            if (aboveMin && belowMax) {
                return tier.getTierName();
            }
        }
        return null;
    }

    // 查單一會員
    @Override
    @Transactional(readOnly = true)
    public UserProfileResponse getById(Integer userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("查無此會員"));

        // +消費級距
        Integer amount = user.getMonthlySpending() != null ? user.getMonthlySpending() : 0;
        String tierName = spendingTierRepository.findTierNameByAmount(amount);

        return UserProfileResponse.from(user, tierName);
    }

    // 依條件篩選會員：消費級距、狀態（皆可複選；null 或空清單 = 不限）
    @Override
    @Transactional(readOnly = true)
    public List<UserProfileResponse> list(List<String> tierNames, List<String> statuses) {
        List<User> users = userRepository.findAll();

        // 消費級距對照表只查 1 次，迴圈內在記憶體比對，避免每筆會員都查一次 DB
        List<SpendingTier> tiers = spendingTierRepository.findAll();
        List<UserProfileResponse> result = new ArrayList<>();

        for (User u : users) {
            Integer amount = u.getMonthlySpending() != null ? u.getMonthlySpending() : 0;
            String userTier = resolveTierName(tiers, amount);

            // 條件 1：消費級距（有勾選才比對；級距不在勾選清單就跳過這筆）
            if (tierNames != null && !tierNames.isEmpty() && !tierNames.contains(userTier)) {
                continue;
            }

            // 條件 2：會員狀態（有勾選才比對）
            if (statuses != null && !statuses.isEmpty()) {
                String userStatus = u.getUserStatus() != null ? u.getUserStatus().name() : null;
                if (!statuses.contains(userStatus)) {
                    continue;
                }
            }

            // 兩個條件都通過，才加進結果
            result.add(UserProfileResponse.from(u, userTier));
        }
        return result;
    }

    // 改狀態：字串轉 enum
    @Override
    public UserProfileResponse updateStatus(Integer userId, String status) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("查無此會員"));
        User.UserStatus newStatus;
        try {
            newStatus = User.UserStatus.valueOf(status);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("不支援的會員狀態: " + status);
        }
        user.setUserStatus(newStatus);
        return UserProfileResponse.from(userRepository.save(user));
    }
}
