package org.fsn.codingtest.web;

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
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.http.MediaType;
import org.springframework.restdocs.JUnitRestDocumentation;
import org.springframework.restdocs.payload.JsonFieldType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.fsn.codingtest.DocumentFormatGenerator.getDateFormat;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.documentationConfiguration;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.get;
import static org.springframework.restdocs.payload.PayloadDocumentation.*;
import static org.springframework.restdocs.request.RequestDocumentation.parameterWithName;
import static org.springframework.restdocs.request.RequestDocumentation.pathParameters;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureRestDocs
public class StatisticApiControllerTest {

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private StatisticRepository statisticRepository;
    @Autowired
    private StatisticService statisticService;

    private MockMvc mvc;

    @Autowired
    private WebApplicationContext context;

    @Before
    public void setUp() {
        this.mvc = MockMvcBuilders.webAppContextSetup(this.context)
                .apply(documentationConfiguration(this.restDocumentation))
                .build();
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
        LocalDate date = LocalDate.now();
        int time=20, req = 1,res=1,clk=1;

        Statistic stat1 = statisticRepository.save(Statistic.builder()
                .date(date).time(time).request(req).response(res).click(clk).build());
        Statistic stat2 = statisticRepository.save(Statistic.builder()
                .date(date).time(time+3).request(req+1).response(res+1).click(clk).build());
        Statistic stat3 = statisticRepository.save(Statistic.builder()
                .date(date.plusDays(1)).time(time).request(req+2).response(res+2).click(clk).build()); // stat3 다른 날짜

        SearchResponseDto dto = new SearchResponseDto(stat1.getRequest()+stat2.getRequest(),
                stat1.getResponse()+ stat2.getResponse(),
                stat1.getClick()+stat2.getClick());

        String url = "http://localhost:"+port+"/api/v1/date={date}";

        //when
        ResultActions result = mvc.perform(get(url,date.toString()).contentType(MediaType.APPLICATION_JSON));

        //then
        result.andExpect(status().isOk())
                .andDo(document("search-by-date",
                        pathParameters(
                                parameterWithName("date").attributes(getDateFormat()).description("날짜")
                        ),
                        //requestFields(),
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
}
