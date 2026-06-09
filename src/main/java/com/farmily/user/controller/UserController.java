package com.farmily.user.controller;

import com.farmily.user.dto.*;
import com.farmily.user.model.User;
import com.farmily.user.repository.UserRepository;
import com.farmily.user.service.UserService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/general/users")
public class UserController {

    @Autowired
    private UserService userService;


    // 一般會員註冊
    @PostMapping("/register")
    public ResponseEntity<UserProfileResponse> register(
            @RequestBody @Valid UserRegisterRequest reg){
        UserProfileResponse response = userService.register(reg);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    // 一般會員登入
    @PostMapping("/login")
    public ResponseEntity<UserProfileResponse> login(
            @RequestBody @Valid UserLoginRequest req) {
        return ResponseEntity.ok(userService.login(req));
    }

    // 修改會員資料
    @PutMapping("/{userId}")
    public ResponseEntity<UserProfileResponse> update(
            @PathVariable Integer userId,
            @RequestBody @Valid UserUpdateRequest update){
        UserProfileResponse response = userService.updateMyProfile(userId, update);
        return ResponseEntity.ok(response);
    }

    // 修改密碼
    @PutMapping("/{userId}/password")
    public ResponseEntity<String> changePassword(
            @PathVariable Integer userId,
            @RequestBody @Valid ChangePasswordRequest pw) {
        userService.changePassword(userId, pw);
        return ResponseEntity.ok("密碼修改成功！請使用新密碼登入");
    }


    // 刪除會員
    @DeleteMapping("/{userId}")
    public ResponseEntity<String> delete(
            @PathVariable Integer userId){
        userService.deleteUser(userId);
        return ResponseEntity.ok("會員刪除成功");
    }

    // 查會員個人資料
    @GetMapping("/{userId}")
    public ResponseEntity<UserProfileResponse> read(
            @PathVariable Integer userId){
        UserProfileResponse response = userService.getMyProfile(userId);
        return ResponseEntity.ok(response);
    }

}
