package com.example.groupbuyingweb.core.session;

import com.example.groupbuyingweb.core.error.BusinessException;
import com.example.groupbuyingweb.domain.enums.ErrorCode;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Component;

@Component
public class LoginSessionManager {

    public void login(
            HttpSession session,
            String userId
    ) {
        session.setAttribute(SessionConst.LOGIN_USER_ID, userId);
    }

    public String getLoginUserId(HttpSession session) {
        Object value = session.getAttribute(SessionConst.LOGIN_USER_ID);

        if (value == null) {
            return null;
        }

        return (String) value;
    }

    public String requireLoginUserId(HttpSession session) {
        String loginUserId = getLoginUserId(session);

        if (loginUserId == null) {
            throw new BusinessException(ErrorCode.LOGIN_REQUIRED);
        }

        return loginUserId;
    }
}