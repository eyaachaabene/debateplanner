package com.university.room_service.exception;

public class RoomAlreadyExistsException extends RuntimeException {

    public RoomAlreadyExistsException(String message) {
        super(message);
    }
}
