package org.fsn.codingtest.web;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class SearchResponseDtoTest {
    @Test
    public void 조회_롬복_기능_테스트(){
        //given
        int request=3,response=3, click=10;

        //when
        SearchResponseDto dto = new SearchResponseDto(request,response,click);

        //then
        assertThat(dto.getRequest()).isEqualTo(request);
        assertThat(dto.getResponse()).isEqualTo(response);
        assertThat(dto.getClick()).isEqualTo(click);
    }
}
