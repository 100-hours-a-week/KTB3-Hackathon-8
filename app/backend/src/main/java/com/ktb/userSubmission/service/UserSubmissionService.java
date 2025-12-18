package com.ktb.userSubmission.service;

import com.ktb.userSubmission.domain.UserSubmission;
import com.ktb.userSubmission.dto.PromptRequestDto;
import com.ktb.userSubmission.dto.PromptResponseDto;
import com.ktb.userSubmission.dto.TotalUserSubmission;
import com.ktb.userSubmission.repository.UserSubmissionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.servlet.ServletComponentScan;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@Service
public class UserSubmissionService {


    UserSubmissionRepository userSubmissionRepository;

    @Autowired
    public UserSubmissionService(UserSubmissionRepository userSubmissionRepository){
        this.userSubmissionRepository = userSubmissionRepository;
    }





    public UserSubmission userSubmit(UserSubmission userSubmission){

        Optional<UserSubmission> existingSubmission =
                userSubmissionRepository.findByNickname(userSubmission.getNickname());

        if (existingSubmission.isPresent()) {
            System.out.println("이미 제출한 사용자입니다.");
            throw new IllegalStateException("이미 제출한 사용자입니다.");
        }

        return userSubmissionRepository.save(userSubmission).orElse(new UserSubmission());

    }



    public PromptResponseDto totalSubmit(Long groupId){

        List<UserSubmission> submissions =
                userSubmissionRepository.findAllByGroupId(groupId);

        TotalUserSubmission total = new TotalUserSubmission();

        for (UserSubmission userSubmission : submissions) {

            if (userSubmission.getLikedFoods() != null
                    && !userSubmission.getLikedFoods().isBlank()) {
                total.getLikedFoodsList()
                        .add(userSubmission.getLikedFoods());
            }

            if (userSubmission.getDisLikedFoods() != null
                    && !userSubmission.getDisLikedFoods().isBlank()) {
                total.getDisLikedFoodsList()
                        .add(userSubmission.getDisLikedFoods());
            }

            if (userSubmission.getForbiddenFoods() != null
                    && !userSubmission.getForbiddenFoods().isBlank()) {
                total.getForbiddenFoodsList()
                        .add(userSubmission.getForbiddenFoods());
            }
        }


        //1. LLM 요청
        PromptRequestDto promptRequest = new PromptRequestDto(null, total);

        //2. LLM 응답 -> 파싱&포매팅 후 return
        return null;




    }










}
