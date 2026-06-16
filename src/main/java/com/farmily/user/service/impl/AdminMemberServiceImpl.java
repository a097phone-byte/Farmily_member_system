package com.farmily.user.service.impl;

import com.farmily.user.dto.UserProfileResponse;
import com.farmily.user.model.User;
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

    public AdminMemberServiceImpl(UserRepository userRepository) {
        this.userRepository = userRepository;
    }


    // 列出所有會員：DB 撈出轉 DTO
    @Override
    @Transactional(readOnly = true)
    public List<UserProfileResponse> listAll() {
        List<User> users = userRepository.findAll();
        List<UserProfileResponse> result = new ArrayList<>();
        for (User u : users) {
            result.add(UserProfileResponse.from(u));
        }
        return result;
    }

    // 查單一會員
    @Override
    @Transactional(readOnly = true)
    public UserProfileResponse getById(Integer userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("查無此會員"));
        return UserProfileResponse.from(user);
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
