package com.farmily.user.service;

import org.springframework.security.core.session.SessionInformation;
import org.springframework.security.core.session.SessionRegistry;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

// 統一管理「哪個帳號目前有哪些登入中的 session」
// 包在 SessionRegistry 外面，登入時登記、改/重設密碼時讓 session 失效
@Service
public class SessionService {

    private final SessionRegistry sessionRegistry;

    public SessionService(SessionRegistry sessionRegistry) {
        this.sessionRegistry = sessionRegistry;
    }

    // 登入時呼叫：把這個 session 登記進來（手動登入不會自動登記）
    public void registerSession(String sessionId, Object principal) {
        sessionRegistry.registerNewSession(sessionId, principal);
    }

    // 把指定 email 帳號目前所有 session 設為過期（標記後，下次請求時會被擋下並失效）
    // keepSessionId：要保留不踢的 session id（修改密碼時用來保留操作者自己這台）；
    //                傳 null 代表全部都踢（忘記密碼用，因為當下沒有登入中的自己）
    public void expireSessions(String email, String keepSessionId) {

        // 拿出目前所有登入中的「身分」（登入時存進去的 UserDetails）
        for (Object principal : sessionRegistry.getAllPrincipals()) {

            // 取出這個身分的 email（UserDetails.getUsername() 就是 email）
            String username;
            if (principal instanceof UserDetails) {
                username = ((UserDetails) principal).getUsername();
            } else {
                username = principal.toString();
            }

            // email 相同 = 同一個帳號
            if (username.equals(email)) {
                for (SessionInformation info : sessionRegistry.getAllSessions(principal, false)) {

                    // 要保留的當前 session 就跳過，不踢
                    if (keepSessionId != null && info.getSessionId().equals(keepSessionId)) {
                        continue;
                    }
                    info.expireNow();
                }
            }
        }
    }
}