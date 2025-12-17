package com.ktb.user.domain;

import jakarta.persistence.*;
import lombok.*;

@Getter
@Entity
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class UserIdentifier {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE,
    generator = "user_seq")
    @SequenceGenerator(name = "user_seq",
            sequenceName = "user_seq",
            allocationSize = 100)
    private Long id;

    private String username;

    private String nickname;

    private String password;

    public UserIdentifier(String username, String nickname, String encryptedPassword) {
        this.username = username;
        this.nickname = nickname;
        this.password = encryptedPassword;
    }
}
