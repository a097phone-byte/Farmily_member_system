package com.farmily.user.service.impl;

import com.farmily.user.dto.UserProfileResponse;
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

    // 列出所有會員：DB 撈出轉 DTO
    @Override
    @Transactional(readOnly = true)
    public List<UserProfileResponse> listAll() {
        List<User> users = userRepository.findAll();
        List<UserProfileResponse> result = new ArrayList<>();

        for (User u : users) {
            // +消費級距
            Integer amount = u.getMonthlySpending() != null ? u.getMonthlySpending() : 0;
            String tierName = spendingTierRepository.findTierNameByAmount(amount);

            result.add(UserProfileResponse.from(u, tierName));
        }
        return result;
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

    // 依條件篩選會員：tierNames（消費級距，可複選）、statuses（會員狀態，可複選），皆可為 null 或空清單（＝不限）
    @Override
    @Transactional(readOnly = true)
    public List<UserProfileResponse> list(List<String> tierNames, List<String> statuses) {
        List<User> users = userRepository.findAll();
        List<UserProfileResponse> result = new ArrayList<>();

        for (User u : users) {
            // 先算出這位會員的消費級距名稱
            Integer amount = u.getMonthlySpending() != null ? u.getMonthlySpending() : 0;
            String userTier = spendingTierRepository.findTierNameByAmount(amount);

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
            newStatus = User.UserStatus.valueOf(status);   // "SUSPENDED" : UserStatus.SUSPENDED
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("不支援的會員狀態: " + status);
        }
        user.setUserStatus(newStatus);
        return UserProfileResponse.from(userRepository.save(user));
    }
}
