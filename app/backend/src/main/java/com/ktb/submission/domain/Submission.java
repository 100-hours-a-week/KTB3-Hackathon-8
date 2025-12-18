package com.ktb.submission.domain;

import com.ktb.group.domain.Group;
import jakarta.persistence.*;
import java.util.Collections;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
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

    @Column(name = "preferred_foods")
    private String preferredFoods;

    @Column(name = "avoided_foods")
    private String avoidedFoods;

    @Column(name = "excluded_foods")
    private String excludedFoods;

    @OneToMany(mappedBy = "submission", fetch = FetchType.LAZY)
    private List<EventDate> excludedDates;

    public static Submission create(
            Group group,
            String nickname,
            String preferredFoods,
            String avoidedFoods,
            String excludedFoods,
            List<Date> excludedDates
    ) {
        Submission submission = new Submission(null, group, nickname, preferredFoods, avoidedFoods, excludedFoods, Collections.EMPTY_LIST);

        submission.createEventDate(excludedDates);

        return submission;
    }

    private void createEventDate(List<Date> excludedDates) {
        excludedDates.forEach(date -> {
            addDate(this, date);
        });
    }

    private static void addDate(Submission submission, Date value) {
        EventDate eventDate = EventDate.create(submission, value);
        submission.excludedDates.add(eventDate);
    }
}
