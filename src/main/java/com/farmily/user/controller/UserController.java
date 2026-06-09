package com.farmily.user.controller;

import com.farmily.user.dto.UserLoginRequest;
import com.farmily.user.dto.UserProfileResponse;
import com.farmily.user.dto.UserRegisterRequest;
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
    public ResponseEntity<UserProfileResponse> register(@RequestBody @Valid UserRegisterRequest reg){
        UserProfileResponse response = userService.register(reg);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    // 一般會員登入
    @PostMapping("/login")
    public UserProfileResponse login(@RequestBody @Valid UserLoginRequest req) {
        return userService.login(req);
    }

    @PutMapping("/{userId}")
    public ResponseEntity<UserProfileResponse> update(@PathVariable Integer userId,
                                                      @RequestBody @Valid UserRegisterRequest reg){

        User u = userRepository.findById(userId).orElse(null);

        // 先檢查會員是否存在
        if(u != null){
            u.setEmail(user.getEmail());
            u.setCityDistrict(user.getCityDistrict());
            u.setUserAddress(user.getUserAddress());
            u.setPassword(user.getPassword());
            u.setUserName(user.getUserName());
            u.setUserNickname(user.getUserNickname());
            u.setBirthday(user.getBirthday());
            u.setUserPhoneNum(user.getUserPhoneNum());

            // 根據請求端判斷是否開啟修改功能
//            u.setUserStatus(user.getUserStatus());
//            u.setMonthlySpending(user.getMonthlySpending());
//            u.setIsFarmer(user.getIsFarmer());

            userService.save(u);      // save() 同時有新增和修改功能

            return "會員資料修改成功";
        } else {
            return "修改失敗，會員數據不存在";
        }

    }

    // 刪除會員
    @DeleteMapping("/{userId}")
    public String delete(@PathVariable Integer userId){

        userRepository.deleteById(userId);

        return "會員刪除成功";
    }

    // 查詢會員
    @GetMapping("/{userId}")
    public User read(@PathVariable Integer userId){

        User user = userRepository.findById(userId)
                .orElse(null);                  // 返回 Optional: orElse 如果資料庫找不到，user = null

        return user;
    }

}
