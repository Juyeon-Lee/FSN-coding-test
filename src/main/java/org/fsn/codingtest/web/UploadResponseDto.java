package org.fsn.codingtest.web;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public class UploadResponseDto {
    private final String message;    // 업로드 결과
}
