package com.ktb.userSubmission.dto;

import lombok.Getter;

@Getter
public class ResponseDto {
    Object data;

    String message;

    public ResponseDto(Object data, String message){
        this.data = data;
        this.message = message;
    }
}
