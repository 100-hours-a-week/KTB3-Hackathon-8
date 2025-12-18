package com.ktb.submission.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;
import java.util.Date;

@Entity
public class EventDate {
    @Id
    @GeneratedValue
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "submission_id", nullable = false)
    private Submission submission;

    @Temporal(TemporalType.TIMESTAMP)
    private Date value;

    protected static EventDate create(Submission submission, Date value) {
        EventDate date = new EventDate();
        date.submission = submission;
        date.value = value;

        return date;
    }
}
