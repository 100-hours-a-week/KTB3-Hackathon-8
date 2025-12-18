package com.ktb.userSubmission.dto;

import com.ktb.userSubmission.domain.UserSubmission;
import lombok.Getter;

import java.util.ArrayList;

@Getter
public class PromptRequestDto {
    TotalUserSubmission totalUserSubmission;

    public PromptRequestDto(Object groupProfile, TotalUserSubmission totalUserSubmission) {

        //this.groupProfile = groupProfile;
        this.totalUserSubmission = totalUserSubmission;
    }
}
