package org.fsn.codingtest.web;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.time.LocalDate;

@RequiredArgsConstructor
@Getter
public class UploadRequestDto {
    //날짜, 시각, 요청 수, 응답 수, 클릭 수
    private final LocalDate date;
    private final int time;
    private final int request;
    private final int response;
    private final int click;
}
