package com.ktb.submission.domain;

import com.ktb.group.domain.Group;
import jakarta.persistence.*;
import lombok.Getter;

@Entity
@Getter
public class Submission {
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

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    private Group group;

    private String nickname;

    private String likedFoods;

    private String disLikedFoods;

    private String forbiddenFoods;
}
