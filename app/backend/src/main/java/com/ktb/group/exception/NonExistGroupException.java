package com.ktb.group.exception;

public class NonExistGroupException extends RuntimeException {
    public NonExistGroupException() {
        super();
    }

    public NonExistGroupException(String message) {
        super(message);
    }
}
