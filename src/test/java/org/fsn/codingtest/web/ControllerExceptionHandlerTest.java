package org.fsn.codingtest.web;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.fsn.codingtest.domain.Statistic;
import org.fsn.codingtest.domain.StatisticRepository;
import org.json.simple.JSONObject;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.http.MediaType;
import org.springframework.restdocs.payload.JsonFieldType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.fsn.codingtest.ApiDocumentUtils.getDocumentRequest;
import static org.fsn.codingtest.ApiDocumentUtils.getDocumentResponse;
import static org.fsn.codingtest.DocumentFormatGenerator.getDateFormat;
import static org.fsn.codingtest.DocumentFormatGenerator.getTimeFormat;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.get;
import static org.springframework.restdocs.payload.PayloadDocumentation.*;
import static org.springframework.restdocs.request.RequestDocumentation.parameterWithName;
import static org.springframework.restdocs.request.RequestDocumentation.requestParameters;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class ControllerExceptionHandlerTest {
    @LocalServerPort
    private int port;

    @Autowired
    private StatisticRepository statisticRepository;

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
        this.mvc = MockMvcBuilders.webAppContextSetup(this.context).build();
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

    @Test
    public void 날짜로_조회_날짜형식_오류() throws Exception {
        //given
        // 예상 responseDto
        String error = "Format Error";
        String wrongDate = date.toString().substring(0,7);
        String url = "http://localhost:"+port+"/api/v1?date={date}";

        //when
        ResultActions result = mvc.perform(get(url,wrongDate).contentType(MediaType.APPLICATION_JSON));

        //then
        result.andExpect(status().isBadRequest())
                .andExpect(jsonPath("$['error']").value(error));
    }

    @Test
    public void 날짜_시각으로_조회_시각형식_오류() throws Exception {
        //given
        // 예상 responseDto
        String error = "Format Error";
        String wrongTime1 = "26", wrongTime2 = "-2";
        String url = "http://localhost:"+port+"/api/v1?date={date}&time={time}";

        //when
        ResultActions result1 = mvc.perform(get(url,date.toString(),wrongTime1).contentType(MediaType.APPLICATION_JSON));
        ResultActions result2 = mvc.perform(get(url,date.toString(),wrongTime2).contentType(MediaType.APPLICATION_JSON));

        //then
        result1.andExpect(status().isBadRequest())
                .andExpect(jsonPath("$['error']").value(error));
        result2.andExpect(status().isBadRequest())
                .andExpect(jsonPath("$['error']").value(error));
    }

    @Test
    public void 조회_파라미터_생략_오류() throws Exception {
        //given
        // 예상 responseDto
        String error = "Missing Parameters";
        String url = "http://localhost:"+port+"/api/v1";

        //when
        ResultActions result1 = mvc.perform(get(url).contentType(MediaType.APPLICATION_JSON));
        ResultActions result2 = mvc.perform(get(url+"?other=33").contentType(MediaType.APPLICATION_JSON));
        ResultActions result3 = mvc.perform(get(url+"?time=3").contentType(MediaType.APPLICATION_JSON));

        //then
        result1.andExpect(status().isBadRequest())
                .andExpect(jsonPath("$['error']").value(error));
        result2.andExpect(status().isBadRequest())
                .andExpect(jsonPath("$['error']").value(error));
        result3.andExpect(status().isBadRequest())
                .andExpect(jsonPath("$['error']").value(error));
    }

    @Test
    public void 등록_오류_생략_date_or_time() throws Exception {
        //given
        String error = "Not Valid Arguments";

        UploadRequestDto dto1 = UploadRequestDto.builder()  // date 빠짐
                .time(Integer.toString(time)).request(req).response(res).click(clk)
                .build();
        UploadRequestDto dto2 = UploadRequestDto.builder()  // time 빠짐
                .date(date.toString()).request(req).response(res).click(clk)
                .build();
        String url = "http://localhost:"+port+"/api/v1";
        String json1 = new ObjectMapper().writeValueAsString(dto1);
        String json2 = new ObjectMapper().writeValueAsString(dto2);

        //when
        ResultActions result1 = mvc.perform(post(url)
                .contentType(MediaType.APPLICATION_JSON).content(json1));
        ResultActions result2 = mvc.perform(post(url)
                .contentType(MediaType.APPLICATION_JSON).content(json2));

        //then
        result1.andExpect(status().isBadRequest())
                .andExpect(jsonPath("$['error']").value(error));
        result2.andExpect(status().isBadRequest())
                .andExpect(jsonPath("$['error']").value(error));
        List<Statistic> all = statisticRepository.findAll();
        assertThat(all.size()).isEqualTo(3);
    }

    @Test
    public void 등록_형식_오류_date_or_time() throws Exception {
        //given
        String error = "Format Error";
        String wrongDate = date.toString().substring(0,7);
        String wrongTime1 = "26", wrongTime2 = "-2";

        UploadRequestDto dto1 = UploadRequestDto.builder()  // date 형식 오류
                .date(wrongDate).time(Integer.toString(time)).request(req).response(res).click(clk)
                .build();
        UploadRequestDto dto2 = UploadRequestDto.builder()  // time 23 초과
                .date(date.toString()).time(wrongTime1).request(req).response(res).click(clk)
                .build();
        UploadRequestDto dto3 = UploadRequestDto.builder()  // time 0 미만
                .date(date.toString()).time(wrongTime2).request(req).response(res).click(clk)
                .build();
        String url = "http://localhost:"+port+"/api/v1";
        String json1 = new ObjectMapper().writeValueAsString(dto1);
        String json2 = new ObjectMapper().writeValueAsString(dto2);
        String json3 = new ObjectMapper().writeValueAsString(dto3);

        //when
        ResultActions result1 = mvc.perform(post(url)
                .contentType(MediaType.APPLICATION_JSON).content(json1));
        ResultActions result2 = mvc.perform(post(url)
                .contentType(MediaType.APPLICATION_JSON).content(json2));
        ResultActions result3 = mvc.perform(post(url)
                .contentType(MediaType.APPLICATION_JSON).content(json3));

        //then
        result1.andExpect(status().isBadRequest())
                .andExpect(jsonPath("$['error']").value(error));
        result2.andExpect(status().isBadRequest())
                .andExpect(jsonPath("$['error']").value(error));
        result3.andExpect(status().isBadRequest())
                .andExpect(jsonPath("$['error']").value(error));
        List<Statistic> all = statisticRepository.findAll();
        assertThat(all.size()).isEqualTo(3);
    }

    @Test
    public void 등록_오류_음수값() throws Exception {
        //given
        String error = "Not Valid Arguments";

        UploadRequestDto dto1 = UploadRequestDto.builder()  // date 빠짐
                .date(date.toString()).time(Integer.toString(time)).request(-2).response(res).click(clk)
                .build();
        UploadRequestDto dto2 = UploadRequestDto.builder()  // time 빠짐
                .date(date.toString()).time(Integer.toString(time)).request(req).response(-2).click(clk)
                .build();
        UploadRequestDto dto3 = UploadRequestDto.builder()  // time 빠짐
                .date(date.toString()).time(Integer.toString(time)).request(req).response(res).click(-2)
                .build();
        String url = "http://localhost:"+port+"/api/v1";
        String json1 = new ObjectMapper().writeValueAsString(dto1);
        String json2 = new ObjectMapper().writeValueAsString(dto2);
        String json3 = new ObjectMapper().writeValueAsString(dto3);

        //when
        ResultActions result1 = mvc.perform(post(url)
                .contentType(MediaType.APPLICATION_JSON).content(json1));
        ResultActions result2 = mvc.perform(post(url)
                .contentType(MediaType.APPLICATION_JSON).content(json2));
        ResultActions result3 = mvc.perform(post(url)
                .contentType(MediaType.APPLICATION_JSON).content(json3));

        //then
        result1.andExpect(status().isBadRequest())
                .andExpect(jsonPath("$['error']").value(error));
        result2.andExpect(status().isBadRequest())
                .andExpect(jsonPath("$['error']").value(error));
        result3.andExpect(status().isBadRequest())
                .andExpect(jsonPath("$['error']").value(error));
        List<Statistic> all = statisticRepository.findAll();
        assertThat(all.size()).isEqualTo(3);
    }

    @Test
    public void 등록_오류_숫자자리에_문자열() throws Exception {
        //given
        String error = "JSON parse error (Not convertible)";

        JSONObject obj1 = new JSONObject();
        obj1.put("date", date.toString());
        obj1.put("time",Integer.toString(time));
        obj1.put("request","test"); //문자열
        obj1.put("response",res);
        obj1.put("click", clk);

        JSONObject obj2 = new JSONObject();
        obj2.put("date", date.toString());
        obj2.put("time",Integer.toString(time));
        obj2.put("request",req);
        obj2.put("response","test"); //문자열
        obj2.put("click", clk);

        JSONObject obj3 = new JSONObject();
        obj3.put("date", date.toString());
        obj3.put("time",Integer.toString(time));
        obj3.put("request",req);
        obj3.put("response",res);
        obj3.put("click", "test"); //문자열

        String url = "http://localhost:"+port+"/api/v1";
        String json1 = obj1.toJSONString();
        String json2 = obj3.toJSONString();
        String json3 = obj3.toJSONString();

        //when
        ResultActions result1 = mvc.perform(post(url)
                .contentType(MediaType.APPLICATION_JSON).content(json1));
        ResultActions result2 = mvc.perform(post(url)
                .contentType(MediaType.APPLICATION_JSON).content(json2));
        ResultActions result3 = mvc.perform(post(url)
                .contentType(MediaType.APPLICATION_JSON).content(json3));

        //then
        result1.andExpect(status().isBadRequest())
                .andExpect(jsonPath("$['error']").value(error));
        result2.andExpect(status().isBadRequest())
                .andExpect(jsonPath("$['error']").value(error));
        result3.andExpect(status().isBadRequest())
                .andExpect(jsonPath("$['error']").value(error));
        List<Statistic> all = statisticRepository.findAll();
        assertThat(all.size()).isEqualTo(3);
    }

    @Test
    public void 등록_오류_JSON() throws Exception {
        //given
        String error = "JSON parse error (Not convertible)";

        JSONObject obj1 = new JSONObject();
        obj1.put("date", date.toString());
        obj1.put("time",Integer.toString(time));
        obj1.put("request", req);
        obj1.put("response",res);
        obj1.put("click", clk);

        String url = "http://localhost:"+port+"/api/v1";
        String json1 = obj1.toJSONString().substring(0,obj1.toJSONString().length()-2); // } 하나 일부러 빼기

        //when
        ResultActions result1 = mvc.perform(post(url)
                .contentType(MediaType.APPLICATION_JSON).content(json1));

        //then
        result1.andExpect(status().isBadRequest())
                .andExpect(jsonPath("$['error']").value(error));
        List<Statistic> all = statisticRepository.findAll();
        assertThat(all.size()).isEqualTo(3);
    }
}
