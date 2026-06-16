package com.farmily.user.controller;

import com.farmily.user.dto.*;
import com.farmily.user.security.MemberUserDetails;
import com.farmily.user.security.service.MemberUserDetailsService;
import com.farmily.user.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/member")
public class UserController {

    private final UserService userService;
    private final MemberUserDetailsService memberUserDetailsService;

    public UserController(UserService userService,
                          MemberUserDetailsService memberUserDetailsService) {
        this.userService = userService;
        this.memberUserDetailsService = memberUserDetailsService;
    }

    // 一般會員註冊
    @PostMapping("/register")
    public ResponseEntity<UserProfileResponse> register(
            @RequestBody @Valid UserRegisterRequest reg){

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(userService.register(reg));
    }

    // 一般會員登入
    @PostMapping("/login")
    public ResponseEntity<UserProfileResponse> login(
            @RequestBody @Valid LoginRequest  log,
            HttpServletRequest request) {

        // step1: 呼叫 Service，判斷帳號狀態、比對密碼，回傳 dto
        UserProfileResponse response = userService.login(log);

        // step2: 通知 Spring Security 此人已通過驗證
        UserDetails userDetails = memberUserDetailsService.loadUserByUsername(log.getEmail());
        UsernamePasswordAuthenticationToken authToken =
                new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(authToken);

        // step3: 把 SecurityContext 存進 HttpSession，後續請求才能持續認得他
        HttpSession session = request.getSession(true);
        session.setAttribute(
                HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY,
                SecurityContextHolder.getContext());
        return ResponseEntity.ok(response);
    }

    // 查自己資料
    @GetMapping("/me")
    public ResponseEntity<UserProfileResponse> getMe(
            @AuthenticationPrincipal MemberUserDetails me){
        UserProfileResponse response = userService.getMyProfile(me.getUserId());
        return ResponseEntity.ok(response);
    }

    // 修改自己資料
    @PutMapping("/me")
    public ResponseEntity<UserProfileResponse> updateMe(
            @AuthenticationPrincipal MemberUserDetails me,
            @RequestBody @Valid UserUpdateRequest update){
        UserProfileResponse response = userService.updateMyProfile(me.getUserId(), update);
        return ResponseEntity.ok(response);
    }

    // 修改自己密碼
    @PutMapping("/me/password")
    public ResponseEntity<String> changePassword(
            @AuthenticationPrincipal MemberUserDetails me,
            @RequestBody @Valid ChangePasswordRequest pw) {
        userService.changePassword(me.getUserId(), pw);
        return ResponseEntity.ok("密碼修改成功！請使用新密碼登入");
    }


    // 註銷自己帳號
    @DeleteMapping("/me")
    public ResponseEntity<String> deleteMe(
            @AuthenticationPrincipal MemberUserDetails me){
        userService.deleteUser(me.getUserId());
        return ResponseEntity.ok("註銷成功!");
    }


}
