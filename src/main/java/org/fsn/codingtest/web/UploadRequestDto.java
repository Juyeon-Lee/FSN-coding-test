package org.fsn.codingtest.web;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@Getter
public class UploadRequestDto {
    //날짜, 시각, 요청 수, 응답 수, 클릭 수
    private String date;
    private int time;
    private int request;
    private int response;
    private int click;

    @Builder
    public UploadRequestDto(String date, int time, int request, int response, int click) {
        this.date = date;
        this.time = time;
        this.request = request;
        this.response = response;
        this.click = click;
    }
}
