package com.blackcode.auth_service.security.jwt;

import com.blackcode.auth_service.exception.InvalidJwtException;
import com.blackcode.auth_service.exception.TokenExpiredException;
import com.blackcode.auth_service.model.Token;
import com.blackcode.auth_service.model.UserAuth;
import com.blackcode.auth_service.repository.TokenRepository;
import com.blackcode.auth_service.repository.UserAuthRepository;
import com.blackcode.auth_service.security.service.UserAuthDetailsServiceImpl;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Optional;

@Component
public class AuthTokenFilter extends OncePerRequestFilter {

    private static final Logger logger = LoggerFactory.getLogger(AuthTokenFilter.class);

    private final JwtUtils jwtUtils;

    private final UserAuthDetailsServiceImpl userAuthDetailsServiceImpl;

    private final TokenRepository tokenRepository;

    private final UserAuthRepository userAuthRepository;

    public AuthTokenFilter(JwtUtils jwtUtils,
                           UserAuthDetailsServiceImpl userAuthDetailsServiceImpl,
                           TokenRepository tokenRepository,
                           UserAuthRepository userAuthRepository) {
        this.jwtUtils = jwtUtils;
        this.userAuthDetailsServiceImpl = userAuthDetailsServiceImpl;
        this.tokenRepository = tokenRepository;
        this.userAuthRepository = userAuthRepository;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        try{
            System.out.println("Proccess 1 authtokenfilter");
            String jwt = parseJwt(request);
            System.out.println("Incoming JWT Token: " + jwt);
            if(jwt != null){
                jwtUtils.assertValidToken(jwt);

                String username = jwtUtils.getUserNameFromJwtToken(jwt);

                Optional<UserAuth> dataUserAuth = userAuthRepository.findByUsername(username);
                if (dataUserAuth.isEmpty()) {
                    response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "User not found");
                    return;
                }

                UserAuth userAuth = dataUserAuth.get();
                Optional<Token> dataToken = tokenRepository.findByUserId(userAuth.getUserId());
                if (dataToken.isEmpty()) {
                    response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Token not found");
                    return;
                }

                Token token = dataToken.get();

                if (!token.getToken().equals(jwt)) {
                    response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Token has been invalidated");
                    return;
                }

                if (!token.getIsActive()) {
                    response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Token is inactive or expired");
                    return;
                }

                UserDetails userDetails = userAuthDetailsServiceImpl.loadUserByUsername(username);
                UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
                        userDetails, null, userDetails.getAuthorities());
                auth.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                SecurityContextHolder.getContext().setAuthentication(auth);

            }
        }catch (TokenExpiredException e) {
            logger.warn("Expired token: {}", e.getMessage());
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Token expired");
            return;
        } catch (InvalidJwtException e) {
            logger.warn("Invalid token: {}", e.getMessage());
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Invalid token");
            return;
        } catch (Exception e) {
            logger.error("Cannot set user authentication: {}", e.getMessage(), e);
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Authentication error");
            return;
        }
        filterChain.doFilter(request, response);
    }

    private String parseJwt(HttpServletRequest request){
        String headerAuth = request.getHeader("Authorization");
        if(StringUtils.hasText(headerAuth) && headerAuth.startsWith("Bearer ")){
            return headerAuth.substring(7);
        }
        return null;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) throws ServletException {
        return !request.getRequestURI().startsWith("/api/auth/");
    }

}
