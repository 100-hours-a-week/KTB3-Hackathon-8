package com.ktb.user.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@AllArgsConstructor
@Data
public class UserDto {
    private String id;
    private String nickname;
    private String password;
}
