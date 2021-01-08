package org.fsn.codingtest.web;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

public class UploadRequestDtoTest {
    @Test
    public void 업로드_롬복_기능_테스트(){
        //given
        String date="2020-12-30";
        String time="12";
        int request = 3, response = 3, click =10;

        //when
        UploadRequestDto dto = new UploadRequestDto(date,time,request,response,click);

        //then
        assertThat(dto.getDate()).isEqualTo(date);
        assertThat(dto.getTime()).isEqualTo(time);
        assertThat(dto.getRequest()).isEqualTo(request);
        assertThat(dto.getResponse()).isEqualTo(response);
        assertThat(dto.getClick()).isEqualTo(click);
    }
}
