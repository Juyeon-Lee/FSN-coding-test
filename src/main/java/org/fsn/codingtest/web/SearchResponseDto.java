package org.fsn.codingtest.web;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public class SearchResponseDto {
    //해당 날짜의 요청 수, 응답 수, 클릭 수 합계
    private final int request;
    private final int response;
    private final int click;
}
