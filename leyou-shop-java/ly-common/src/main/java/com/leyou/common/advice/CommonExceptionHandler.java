package com.leyou.common.advice;

import com.leyou.common.enums.ExceptionEnums;
import com.leyou.common.exception.LyException;
import com.leyou.common.vo.ExceptionResult;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class CommonExceptionHandler {
//    @ExceptionHandler(LyException.class)
//    public ResponseEntity<ExceptionResult> handleException(LyException e) {
//        ExceptionEnums em = e.getExceptionEnums();
//        return ResponseEntity.status(em.getCode()).body(new ExceptionResult(em));
//    }
    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<String> handleException(LyException e) {
        return ResponseEntity.status(400).body(e.getMessage());
    }
}
