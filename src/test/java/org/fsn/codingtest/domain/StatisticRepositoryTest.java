package org.fsn.codingtest.domain;


import org.fsn.codingtest.service.StatisticService;
import org.fsn.codingtest.web.SearchResponseDto;
import org.fsn.codingtest.web.UploadRequestDto;
import org.junit.After;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(SpringRunner.class)
@SpringBootTest
public class StatisticRepositoryTest {

    @Autowired
    StatisticRepository statisticRepository;
    @Autowired
    StatisticService statisticService;

    private final LocalDate date = LocalDate.now();
    private final int time=20, req = 1,res=1,clk=1;

    @After
    public void cleanup(){
        statisticRepository.deleteAll();
    }

    @Test
    public void 통계저장_불러오기(){
        //given
        UploadRequestDto dto = new UploadRequestDto(date.toString(),Integer.toString(time),req,res,clk);
        statisticService.upload(dto);

        //when
        List<Statistic> statisticList = statisticRepository.findAll();

        //then
        Statistic statistic = statisticList.get(0);
        assertThat(statistic.getTime()).isEqualTo(time);
        assertThat(statistic.getDate()).isNotEqualTo(date.plusDays(1));
    }

    @Test
    public void 통계_날짜로_조회(){
        //given
        Statistic stat1 = statisticRepository.save(Statistic.builder()
                .date(date).time(time).request(req).response(res).click(clk).build());
        Statistic stat2 = statisticRepository.save(Statistic.builder()
                .date(date).time(time+3).request(req+1).response(res+1).click(clk).build());
        Statistic stat3 = statisticRepository.save(Statistic.builder()
                .date(date.plusDays(1)).time(time).request(req+2).response(res+2).click(clk).build()); // stat3 다른 날짜

        //when
        List<Statistic> statisticList = statisticRepository.findAllByDate(date);

        //then
        assertThat(statisticList.size()).isEqualTo(2);
        assertThat(statisticList.get(0)).isEqualToComparingFieldByField(stat1); // object 는 다르지만 내부 value는 같을 때
        assertThat(statisticList.get(1)).isEqualToComparingFieldByField(stat2);
    }

    @Test
    public void 없는_날짜로_조회(){
        //given
        Statistic stat1 = statisticRepository.save(Statistic.builder()
                .date(date).time(time).request(req).response(res).click(clk).build());
        Statistic stat2 = statisticRepository.save(Statistic.builder()
                .date(date).time(time+3).request(req+1).response(res+1).click(clk).build());        // stat2 - 다른 시각
        Statistic stat3 = statisticRepository.save(Statistic.builder()
                .date(date.plusDays(1)).time(time).request(req+2).response(res+2).click(clk).build()); // stat3 - 다른 날짜
        SearchResponseDto emptyDto = new SearchResponseDto(0,0,0);

        //when
        List<Statistic> allByDate = statisticRepository.findAllByDate(date.minusDays(3));
        SearchResponseDto result = statisticService.findSumByDate(date.minusDays(3).toString());

        //then
        assertThat(allByDate.size()).isEqualTo(0);
        assertThat(result).isEqualToComparingFieldByField(emptyDto);
    }

    @Test
    public void 통계_날짜_시간으로_조회(){
        //given
        Statistic stat1 = statisticRepository.save(Statistic.builder()
                .date(date).time(time).request(req).response(res).click(clk).build());
        Statistic stat2 = statisticRepository.save(Statistic.builder()
                .date(date).time(time+3).request(req+1).response(res+1).click(clk).build());        // stat2 - 다른 시각
        Statistic stat3 = statisticRepository.save(Statistic.builder()
                .date(date.plusDays(1)).time(time).request(req+2).response(res+2).click(clk).build()); // stat3 - 다른 날짜

        //when
        Statistic statistic = statisticRepository.findByDateAndTime(date, time)
                .orElseThrow(()-> new IllegalArgumentException("해당 날짜와 시간에는 데이터가 없습니다."));

        //then
        assertThat(statistic).isEqualToComparingFieldByField(stat1);
    }

    @Test
    public void 없는_날짜_시각으로_조회(){
        //given
        Statistic stat1 = statisticRepository.save(Statistic.builder()
                .date(date).time(time).request(req).response(res).click(clk).build());
        Statistic stat2 = statisticRepository.save(Statistic.builder()
                .date(date).time(time+3).request(req+1).response(res+1).click(clk).build());        // stat2 - 다른 시각
        Statistic stat3 = statisticRepository.save(Statistic.builder()
                .date(date.plusDays(1)).time(time).request(req+2).response(res+2).click(clk).build()); // stat3 - 다른 날짜
        SearchResponseDto emptyDto = new SearchResponseDto(0,0,0);

        //when
        Optional<Statistic> result1 = statisticRepository.findByDateAndTime(date.plusDays(3), time);
        Optional<Statistic> result2 = statisticRepository.findByDateAndTime(date, time - 3);
        SearchResponseDto dto1 = statisticService.findByDateTime(date.plusDays(3).toString(), Integer.toString(time));
        SearchResponseDto dto2 = statisticService.findByDateTime(date.toString(), Integer.toString(time-3));

        //then
        assertThat(result1).isInstanceOf(Optional.class).isEmpty();
        assertThat(result2).isInstanceOf(Optional.class).isEmpty();
        assertThat(dto1).isEqualToComparingFieldByField(emptyDto);
        assertThat(dto2).isEqualToComparingFieldByField(emptyDto);
    }
}
