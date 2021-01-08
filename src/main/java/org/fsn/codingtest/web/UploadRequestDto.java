package org.fsn.codingtest.web;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@NoArgsConstructor
@Getter
public class UploadRequestDto {
    //날짜, 시각, 요청 수, 응답 수, 클릭 수
    @NotBlank
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private String date;

    @NotNull
    @Min(value = 0, message = "time은 0 이상을 입력하세요.")
    @Max(value = 23, message = "time은 23 이하를 입력하세요.")
    private int time;

    @NotNull
    @Min(value = 0, message = "request는 0 이상을 입력하세요.")
    private int request;

    @NotNull
    @Min(value = 0, message = "response는 0 이상을 입력하세요.")
    private int response;

    @NotNull
    @Min(value = 0, message = "click은 0 이상을 입력하세요.")
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
