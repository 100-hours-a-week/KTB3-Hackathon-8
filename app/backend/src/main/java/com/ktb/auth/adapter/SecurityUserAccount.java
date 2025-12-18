package com.ktb.auth.adapter;

import com.ktb.user.domain.UserIdentifier;
import lombok.Getter;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;

import java.util.List;

@Getter
public class SecurityUserAccount extends User {
    private final UserIdentifier account;
    public SecurityUserAccount(UserIdentifier account) {
        super(
                account.getUsername(),
                account.getPassword(),
                List.of(new SimpleGrantedAuthority("ROLE_USER"))
        );
        this.account = account;
    }
}