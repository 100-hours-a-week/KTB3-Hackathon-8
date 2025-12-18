package com.ktb.submission.exception;

public class AlreadySubmittedUserException extends RuntimeException {
    public AlreadySubmittedUserException() {
        super();
    }

    public AlreadySubmittedUserException(String message) {
        super(message);
    }
}
