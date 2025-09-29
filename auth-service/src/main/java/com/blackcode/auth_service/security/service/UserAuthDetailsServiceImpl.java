package com.blackcode.auth_service.security.service;

import com.blackcode.auth_service.model.UserAuth;
import com.blackcode.auth_service.repository.UserAuthRepository;
import jakarta.transaction.Transactional;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class UserAuthDetailsServiceImpl implements UserDetailsService {

    private final UserAuthRepository userAuthRepository;

    public UserAuthDetailsServiceImpl(UserAuthRepository userAuthRepository) {
        this.userAuthRepository = userAuthRepository;
    }

    @Override
    @Transactional
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        UserAuth userAuth = userAuthRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("Staff Not Found with username: " + username));;

        return UserAuthDetailsImpl.build(userAuth);
    }

}
