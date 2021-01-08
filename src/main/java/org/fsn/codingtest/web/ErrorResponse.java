package org.fsn.codingtest.web;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@Getter
public class ErrorResponse {
    private String error;
    private String message;

    @Builder
    public ErrorResponse(String error, String message) {
        this.error = error;
        this.message = message;
    }
}
