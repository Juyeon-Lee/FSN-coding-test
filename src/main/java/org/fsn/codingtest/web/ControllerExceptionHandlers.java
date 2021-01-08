package org.fsn.codingtest.web;


import com.fasterxml.jackson.core.JsonParseException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.format.DateTimeParseException;


@RestController
@RestControllerAdvice
public class ControllerExceptionHandlers {
    //Json 형식 오류
    @ExceptionHandler
    public ResponseEntity<ErrorResponseDto> handleIllJson(JsonParseException ex){
        return new ResponseEntity<>(ErrorResponseDto.builder()
                .error("Json Error").message(ex.getMessage()+"invalid json").build(),
                HttpStatus.BAD_REQUEST);
    }

    // 날짜 or 시각 형식 오류
    @ExceptionHandler
    public ResponseEntity<ErrorResponseDto> handleIllDateFormat(DateTimeParseException ex){
        return new ResponseEntity<>(ErrorResponseDto.builder()
                .error("Format Error").message(ex.getMessage()+"invalid Date").build(),
                HttpStatus.BAD_REQUEST);
    }

    // 조회 파라미터 생략 오류
    @ExceptionHandler
    public ResponseEntity<ErrorResponseDto> handleMissingParams(MissingServletRequestParameterException ex) {
        // Actual exception handling
        return new ResponseEntity<>(ErrorResponseDto.builder()
                .error("Missing Parameters").message(ex.getMessage()+"need valid parameters").build(),
                HttpStatus.BAD_REQUEST);
    }

    // 업로드 데이터 검증
    @ExceptionHandler
    public ResponseEntity<ErrorResponseDto> handleNotValidArgument(MethodArgumentNotValidException ex){
        return new ResponseEntity<>(ErrorResponseDto.builder()
                .error("Not Valid Arguments").message(ex.getMessage()+"not valid Arguments").build(),
                HttpStatus.BAD_REQUEST);
    }

    // Not convertible because it has wrong type / Json parse error
    @ExceptionHandler
    public ResponseEntity<ErrorResponseDto> handleCannotDeserialize(HttpMessageNotReadableException ex){
        return new ResponseEntity<>(ErrorResponseDto.builder()
                .error("JSON parse error (Not convertible)").message(ex.getMessage()).build(),
                HttpStatus.BAD_REQUEST);
    }
}
