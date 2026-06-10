package com.farmily.user.controller;

import com.farmily.user.dto.*;
import com.farmily.user.security.MyUserDetailsService;
import com.farmily.user.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/general/users")
public class UserController {

    @Autowired
    private UserService userService;

    @Autowired
    private MyUserDetailsService myUserDetailsService;


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
            @RequestBody @Valid UserLoginRequest req,
            HttpServletRequest request) {

        // step1: 呼叫 Service，判斷帳號狀態、比對密碼，回傳 dto
        UserProfileResponse response = userService.login(req);

        // step2: 成功後，通知 Spring Security「這個人已通過驗證(登入)」
        UserDetails userDetails = myUserDetailsService.loadUserByUsername(req.getEmail());
        UsernamePasswordAuthenticationToken authToken =
                new UsernamePasswordAuthenticationToken(
                        userDetails, null, userDetails.getAuthorities()
                );
        SecurityContextHolder.getContext().setAuthentication(authToken);

        // step3: 把 SecurityContext 存進 HttpSession，後續請求才能持續認得他
        HttpSession session = request.getSession(true);
        session.setAttribute(
                HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY,
                SecurityContextHolder.getContext()
        );
        return ResponseEntity.ok(response);
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
