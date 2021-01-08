package org.fsn.codingtest.web;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@Getter
public class ErrorResponseDto {
    private String error;
    private String message;

    @Builder
    public ErrorResponseDto(String error, String message) {
        this.error = error;
        this.message = message;
    }
}
