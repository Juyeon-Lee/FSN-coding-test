package org.fsn.codingtest.web;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.fsn.codingtest.domain.Statistic;
import org.fsn.codingtest.domain.StatisticRepository;
import org.fsn.codingtest.service.StatisticService;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.restdocs.AutoConfigureRestDocs;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.http.MediaType;
import org.springframework.restdocs.JUnitRestDocumentation;
import org.springframework.restdocs.payload.FieldDescriptor;
import org.springframework.restdocs.payload.JsonFieldType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.fsn.codingtest.ApiDocumentUtils.getDocumentRequest;
import static org.fsn.codingtest.ApiDocumentUtils.getDocumentResponse;
import static org.fsn.codingtest.DocumentFormatGenerator.getDateFormat;
import static org.fsn.codingtest.DocumentFormatGenerator.getTimeFormat;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.documentationConfiguration;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.get;
import static org.springframework.restdocs.payload.PayloadDocumentation.*;
import static org.springframework.restdocs.request.RequestDocumentation.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureRestDocs
public class StatisticApiControllerTest {

    @LocalServerPort
    private int port;

    @Autowired
    private StatisticRepository statisticRepository;
    @Autowired
    private StatisticService statisticService;

    private MockMvc mvc;

    @Autowired
    private WebApplicationContext context;

    private final LocalDate date = LocalDate.now();
    private final int time=20;
    private final int req = 1;
    private final int res=1;
    private final int clk=1;

    @Before
    public void setUp() {
        this.mvc = MockMvcBuilders.webAppContextSetup(this.context)
                .apply(documentationConfiguration(this.restDocumentation))
                .build();
    }

    @Before
    public void setSampleData() {
        // date & time
        statisticRepository.save(Statistic.builder()
                .date(date).time(time).request(req).response(res).click(clk).build());
        // date & time+3
        statisticRepository.save(Statistic.builder()
                .date(date).time(time+3).request(req+1).response(res+1).click(clk).build());
        // date+1 & time
        statisticRepository.save(Statistic.builder()
                .date(date.plusDays(1)).time(time).request(req+2).response(res+2).click(clk).build()); // stat3 다른 날짜
    }

    @After
    public void cleanup() throws Exception {
        statisticRepository.deleteAll();
    }

    @Rule
    public JUnitRestDocumentation restDocumentation = new JUnitRestDocumentation();
    //괄호안 "directory name" 설정할 수 있음.

    @Test
    public void 날짜로_조회() throws Exception {
        //given

        Statistic stat1 = statisticRepository.findByDateAndTime(date,time)
                .orElseThrow(()-> new IllegalArgumentException("erorr - date:"+date+" time:"+time));
        Statistic stat2 = statisticRepository.findByDateAndTime(date,time+3)
                .orElseThrow(()-> new IllegalArgumentException("erorr - date:"+date+" time:"+time+3));

        // 예상 responseDto
        SearchResponseDto dto = new SearchResponseDto(stat1.getRequest()+stat2.getRequest(),
                stat1.getResponse()+ stat2.getResponse(),
                stat1.getClick()+stat2.getClick());

        String url = "http://localhost:"+port+"/api/v1?date={date}";

        //when
        ResultActions result = mvc.perform(get(url,date.toString()).contentType(MediaType.APPLICATION_JSON));

        //then
        result.andExpect(status().isOk())
                .andDo(document("search-by-date",
                        getDocumentRequest(),
                        getDocumentResponse(),
                        requestParameters(
                                parameterWithName("date").attributes(getDateFormat()).description("날짜")
                        ),
                        responseFields(
                                fieldWithPath("request").type(JsonFieldType.NUMBER).description("해당 날짜의 요청 수 합계"),
                                fieldWithPath("response").type(JsonFieldType.NUMBER).description("해당 날짜의 응답 수 합계"),
                                fieldWithPath("click").type(JsonFieldType.NUMBER).description("해당 날짜의 클릭 수 합계")
                        )
                ))
                .andExpect(jsonPath("$['request']").value(dto.getRequest()))
                .andExpect(jsonPath("$['response']").value(dto.getResponse()))
                .andExpect(jsonPath("$['click']").value(dto.getClick()));
    }

    @Test
    public void 없는_날짜로_조회_then_0dto() throws Exception {
        //given
        // 예상 responseDto
        SearchResponseDto dto = new SearchResponseDto(0,0,0);

        String url = "http://localhost:"+port+"/api/v1?date={date}";

        //when
        ResultActions result = mvc.perform(get(url,
                date.minusDays(1).toString()).contentType(MediaType.APPLICATION_JSON));

        //then
        result.andExpect(status().isOk())
                .andExpect(jsonPath("$['request']").value(dto.getRequest()))
                .andExpect(jsonPath("$['response']").value(dto.getResponse()))
                .andExpect(jsonPath("$['click']").value(dto.getClick()));
    }

    @Test
    public void 날짜와_시각으로_조회() throws Exception {
        //given
        Statistic stat1 = statisticRepository.findByDateAndTime(date,time)
                .orElseThrow(()-> new IllegalArgumentException("erorr - date:"+date+" time:"+time));

        SearchResponseDto dto = new SearchResponseDto(  // 예상 responseDto
                stat1.getRequest(), stat1.getResponse(), stat1.getClick());

        String url = "http://localhost:"+port+"/api/v1?date={date}&time={time}";

        //when
        ResultActions result = mvc.perform(get(url,date.toString(),time).contentType(MediaType.APPLICATION_JSON));

        //then
        result.andExpect(status().isOk())
                .andDo(document("search-by-date-and-time",
                        getDocumentRequest(),
                        getDocumentResponse(),
                        requestParameters(
                                parameterWithName("date").attributes(getDateFormat()).description("날짜"),
                                parameterWithName("time").attributes(getTimeFormat()).description("시각")
                        ),
                        responseFields(
                                fieldWithPath("request").type(JsonFieldType.NUMBER).description("해당 날짜.시각의 요청 수"),
                                fieldWithPath("response").type(JsonFieldType.NUMBER).description("해당 날짜.시각의 응답 수"),
                                fieldWithPath("click").type(JsonFieldType.NUMBER).description("해당 날짜.시각의 클릭 수")
                        )
                ))
                .andExpect(jsonPath("$['request']").value(dto.getRequest()))
                .andExpect(jsonPath("$['response']").value(dto.getResponse()))
                .andExpect(jsonPath("$['click']").value(dto.getClick()));
    }

    @Test
    public void 없는_날짜와_시각으로_조회_then_return_0dto() throws Exception {
        //given
        SearchResponseDto dto = new SearchResponseDto(0,0,0);   // 예상 responseDto

        String url = "http://localhost:"+port+"/api/v1?date={date}&time={time}";

        //when
        ResultActions result = mvc.perform(get(url,
                date.minusDays(1).toString(),time).contentType(MediaType.APPLICATION_JSON));

        //then
        result.andExpect(status().isOk())
                .andExpect(jsonPath("$['request']").value(dto.getRequest()))
                .andExpect(jsonPath("$['response']").value(dto.getResponse()))
                .andExpect(jsonPath("$['click']").value(dto.getClick()));
    }

    @Test
    public void 데이터_등록_update() throws Exception {
        //given
        String resMessage = "success";    // 예상 response

        UploadRequestDto dto = UploadRequestDto.builder()
                .date(date.toString()).time(time).request(req+10).response(res+10).click(clk+10)
                .build();
        String url = "http://localhost:"+port+"/api/v1";
        String json = new ObjectMapper().writeValueAsString(dto);

        //when
        ResultActions result = mvc.perform(post(url)
                .contentType(MediaType.APPLICATION_JSON).content(json));

        //then
        result.andExpect(status().isOk())
                .andDo(document("upload",
                        getDocumentRequest(),
                        getDocumentResponse(),
                        requestFields(
                                fieldWithPath("date").attributes(getDateFormat()).type(JsonFieldType.STRING).description("통계 데이터의 날짜"),
                                fieldWithPath("time").attributes(getTimeFormat()).type(JsonFieldType.NUMBER).description("통계 데이터의 시각"),
                                fieldWithPath("request").optional().type(JsonFieldType.NUMBER).description("요청 수"),
                                fieldWithPath("response").optional().type(JsonFieldType.NUMBER).description("응답 수"),
                                fieldWithPath("click").optional().type(JsonFieldType.NUMBER).description("클릭 수")
                        ),
                        responseFields( // success / error
                                fieldWithPath("message").type(JsonFieldType.STRING)
                                        .description("업로드 결과 - 'success' : 성공, 'error' : 처리 중 에러발생")
                        )
                ))
                .andExpect(jsonPath("$['message']").value(resMessage));
        List<Statistic> all = statisticRepository.findAll();
        assertThat(all.size()).isEqualTo(3);
        Statistic updatedStatistic = statisticRepository.findByDateAndTime(date, time).orElseThrow(()-> new IllegalArgumentException("에러 발생"));
        assertThat(updatedStatistic.getRequest()).isEqualTo(req+10);
        assertThat(updatedStatistic.getResponse()).isEqualTo(res+10);
        assertThat(updatedStatistic.getClick()).isEqualTo(clk+10);
    }

    @Test
    public void 데이터_등록_new() throws Exception {
        //given
        String resMessage = "success";    // 예상 response

        UploadRequestDto dto = UploadRequestDto.builder()
                .date(date.toString()).time(time-3).request(req+10).response(res+10).click(clk+10)
                .build();
        String url = "http://localhost:"+port+"/api/v1";
        String json = new ObjectMapper().writeValueAsString(dto);

        //when
        ResultActions result = mvc.perform(post(url)
                .contentType(MediaType.APPLICATION_JSON).content(json));

        //then
        result.andExpect(status().isOk())
                .andExpect(jsonPath("$['message']").value(resMessage));
        List<Statistic> all = statisticRepository.findAll();
        assertThat(all.size()).isEqualTo(4);
        Statistic updatedStatistic = statisticRepository.findByDateAndTime(date, time-3).orElseThrow(()-> new IllegalArgumentException("에러 발생"));
        assertThat(updatedStatistic.getRequest()).isEqualTo(req+10);
        assertThat(updatedStatistic.getResponse()).isEqualTo(res+10);
        assertThat(updatedStatistic.getClick()).isEqualTo(clk+10);
    }
}
