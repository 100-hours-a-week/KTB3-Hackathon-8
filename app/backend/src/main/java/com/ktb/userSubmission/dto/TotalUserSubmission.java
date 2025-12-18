package com.ktb.userSubmission.dto;

import lombok.Getter;

import java.util.ArrayList;

@Getter
public class TotalUserSubmission {



    ArrayList<String> likedFoodsList = new ArrayList<>();
    ArrayList<String> disLikedFoodsList= new ArrayList<>();
    ArrayList<String> forbiddenFoodsList= new ArrayList<>();



}
