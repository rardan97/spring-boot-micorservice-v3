package com.blackcode.task_service.exception;

public class DataNotFoundException extends RuntimeException{

    public DataNotFoundException(String message){
        super(message);
    }
}
