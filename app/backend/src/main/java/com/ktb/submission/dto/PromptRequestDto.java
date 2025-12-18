package com.ktb.submission.dto;

import lombok.Getter;

@Getter
public class PromptRequestDto {
    TotalUserSubmission totalUserSubmission;

    public PromptRequestDto(Object groupProfile, TotalUserSubmission totalUserSubmission) {

        //this.groupProfile = groupProfile;
        this.totalUserSubmission = totalUserSubmission;
    }
}
