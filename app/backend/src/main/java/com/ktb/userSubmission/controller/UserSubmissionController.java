package com.ktb.userSubmission.controller;

import com.ktb.ResponseDto;
import com.ktb.userSubmission.domain.UserSubmission;
import com.ktb.userSubmission.dto.PromptRequestDto;
import com.ktb.userSubmission.dto.PromptResponseDto;
import com.ktb.userSubmission.service.UserSubmissionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class UserSubmissionController {


    UserSubmissionService userSubmissionService;

    @Autowired
    public UserSubmissionController(UserSubmissionService userSubmissionService){
        this.userSubmissionService = userSubmissionService;
    }



    //맴버 개별 제출
    @PostMapping("/userSubmission")
    public ResponseDto userSubmit(@RequestBody UserSubmission userSubmission){

        try{

            UserSubmission saved = userSubmissionService.userSubmit(userSubmission);

            if(true){ //총무(인증) 확인 조건
                return new ResponseDto(saved, "success, owner");
            }else{
                return new ResponseDto(saved, "success, member");
            }

        }catch(IllegalStateException e){
            return new ResponseDto(null, "failed, IllegalStateException");
        }

    }



    //총무 통합 제출
    @PostMapping("userSubmission/total/{groupId}")
    public ResponseDto totalSubmit(@PathVariable Long groupId){

        PromptResponseDto promptResponse = userSubmissionService.totalSubmit(groupId);

        return new ResponseDto(null, "success");

    }








}
