package com.ktb.userSubmission.domain;

import jakarta.persistence.*;
import lombok.Getter;

import java.util.ArrayList;

@Entity
@Getter
public class UserSubmission {


    @Id
    @GeneratedValue(
            strategy = GenerationType.SEQUENCE,
            generator = "userSubmission_seq"
    )
    @SequenceGenerator(
            name = "userSubmission_seq",
            sequenceName = "userSubmission_seq",
            allocationSize = 100
    )
    private Long id;

    private Long groupId;


    private String nickname;

    private String likedFoods;
    private String disLikedFoods;
    private String forbiddenFoods;


    //private ArrayList<String> possibleDates;













}
