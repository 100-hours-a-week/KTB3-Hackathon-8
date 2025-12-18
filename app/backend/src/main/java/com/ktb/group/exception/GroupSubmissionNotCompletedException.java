package com.ktb.group.exception;

public class GroupSubmissionNotCompletedException extends RuntimeException{
    public GroupSubmissionNotCompletedException() {
        super();
    }

    public GroupSubmissionNotCompletedException(String message) {
        super(message);
    }
}
