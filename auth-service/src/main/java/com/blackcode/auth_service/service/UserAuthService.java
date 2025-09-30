package com.blackcode.auth_service.service;

import com.blackcode.auth_service.dto.*;
import jakarta.servlet.http.HttpServletRequest;
import reactor.core.publisher.Mono;

public interface UserAuthService {

    JwtRes signIn(LoginReq loginRequest);

    MessageRes signUp(SignUpReq signUpReq);

    TokenRefreshRes refreshToken(TokenRefreshReq request);

    MessageRes signOut(HttpServletRequest request);

}
