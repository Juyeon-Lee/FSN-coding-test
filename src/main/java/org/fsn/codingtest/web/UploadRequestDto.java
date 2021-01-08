package org.fsn.codingtest.web;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;

import javax.validation.constraints.*;

@NoArgsConstructor
@Getter
public class UploadRequestDto {
    //날짜, 시각, 요청 수, 응답 수, 클릭 수
    @NotBlank
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private String date;

    @NotBlank
    private String time;

    @NotNull
    @Positive(message = "request는 양수를 입력하세요.")
    private int request;

    @NotNull
    @Positive(message = "request는 양수를 입력하세요.")
    private int response;

    @NotNull
    @Positive(message = "request는 양수를 입력하세요.")
    private int click;

    @Builder
    public UploadRequestDto(String date, String time, int request, int response, int click) {
        this.date = date;
        this.time = time;
        this.request = request;
        this.response = response;
        this.click = click;
    }
}
