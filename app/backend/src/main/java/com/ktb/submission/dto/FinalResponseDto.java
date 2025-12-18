package com.ktb.submission.dto;

import com.ktb.submission.dto.response.AiGenerateResponse;
import com.ktb.submission.dto.response.FinalDateResult;
import lombok.Getter;


@Getter
public class FinalResponseDto {

    AiGenerateResponse aiGenerateResponse;

    FinalDateResult finalDateResult;

    public FinalResponseDto(AiGenerateResponse aiGenerateResponse, FinalDateResult finalDateResult){
        this.aiGenerateResponse = aiGenerateResponse;
        this.finalDateResult = finalDateResult;
    }



}
