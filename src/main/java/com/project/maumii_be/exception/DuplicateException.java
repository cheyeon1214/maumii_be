package com.project.maumii_be.exception;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.http.HttpStatus;

@NoArgsConstructor
@Getter
@Setter
public class DuplicateException extends RuntimeException {
    private String message;
    private String title;
    private HttpStatus status;

    public DuplicateException(String message, String title, HttpStatus status) {
        super(message);
        this.message = message;
        this.title = title;
        this.status = status;
    }

    public DuplicateException(String title, String message) {
        this(message,title,HttpStatus.EXPECTATION_FAILED);
    }
}
