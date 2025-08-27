package com.project.maumii_be.exception;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.http.HttpStatus;

@NoArgsConstructor
@Getter
@Setter
public class DMLException extends RuntimeException {
    private String message;
    private String title;
    private HttpStatus status;

    public DMLException(String message,String title,HttpStatus status) {
        super(message);
        this.message = message;
        this.title = title;
        this.status = status;
    }

    public DMLException(String message,String title) {
        this(message,title,HttpStatus.EXPECTATION_FAILED);
    }
}
