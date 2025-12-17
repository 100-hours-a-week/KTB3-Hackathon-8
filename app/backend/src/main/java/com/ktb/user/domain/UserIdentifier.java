package com.ktb.user.domain;

import jakarta.persistence.*;
import lombok.NoArgsConstructor;

@Entity
@NoArgsConstructor

public class UserIdentifier {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE,
    generator = "user_seq")
    @SequenceGenerator(name = "user_seq",
            sequenceName = "user_seq",
            allocationSize = 100)
    private Long id;

    private String username; // null이 아니면 총무
    private String nickname;
    private String password;

}
