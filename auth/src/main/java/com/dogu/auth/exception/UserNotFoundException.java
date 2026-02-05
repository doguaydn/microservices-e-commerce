package com.dogu.auth.exception;

public class UserNotFoundException extends RuntimeException {
    public UserNotFoundException(String message) {
        super(message);
    }

    public UserNotFoundException(int id) {
        super("User not found with id: " + id);
    }
}
