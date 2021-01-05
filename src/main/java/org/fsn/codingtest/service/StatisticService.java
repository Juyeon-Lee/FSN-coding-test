package org.fsn.codingtest.service;

import lombok.RequiredArgsConstructor;
import org.fsn.codingtest.domain.Statistic;
import org.fsn.codingtest.domain.StatisticRepository;
import org.fsn.codingtest.web.SearchResponseDto;
import org.fsn.codingtest.web.UploadRequestDto;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@RequiredArgsConstructor
@Service
public class StatisticService {

    private final StatisticRepository statisticRepository;

    /*
    파라메터로 날짜 정보 전달 : 해당 날짜의 요청 수, 응답 수, 클릭 수 합계를 JSON 형식으로 응답
     */
    @Transactional(readOnly = true)
    public SearchResponseDto findSumByDate(LocalDate date) {
        List<Statistic> list = statisticRepository.findAllByDate(date);
        SearchResponseDto dto;
        if(list.size()==0) { // 해당 날짜에 데이터가 아무것도 없으면  TODO: 없애도 되지 않나? 에러나나?
            dto = new SearchResponseDto(0, 0, 0);
        }else{
            //list 안 요청수,응답수,클릭 수 합계를 계산
            int requestSum=0, responseSum=0, clickSum=0;
            for(Statistic hour : list){
                requestSum += hour.getRequest();
                responseSum += hour.getResponse();
                clickSum += hour.getClick();
            }
            dto = new SearchResponseDto(requestSum,responseSum,clickSum);
        }
        return dto;
    }

    /*
    오버로딩
    파라메터로 날짜와 시각 정보 전달 : 해당 날짜의 시각에 요청 수, 응답 수, 클릭 수 JSON 형식으로 응답
     */
    @Transactional(readOnly = true)
    public SearchResponseDto findByDateTime(LocalDate date, int time) {
        Optional<Statistic> statistic = statisticRepository.findByDateAndTime(date, time);
        SearchResponseDto dto;
        if(statistic.isPresent()){
            Statistic data = statistic.get();
            dto = new SearchResponseDto(data.getRequest(), data.getResponse(), data.getClick());
        }else{
            dto = new SearchResponseDto(0,0,0);
        }
        return dto;
    }

    /*
    날짜와 시각이 존재하지 않으면 신규 추가, 존재하면 기존 데이터 업데이트
    날짜, 시각, 요청 수, 응답 수, 클릭 수 정보를 담고 있는 json 형식 파일 이용
     */
    public String upload(UploadRequestDto dto) {
        Optional<Statistic> data = statisticRepository.findByDateAndTime(dto.getDate(), dto.getTime());
        if(data.isPresent()){   //기존 데이터 업데이트
            Statistic statistic = data.get();
            statistic.updateThreeData(dto.getRequest(),dto.getResponse(),dto.getClick());
        }else{  // 신규 추가
            Statistic statistic = Statistic.builder()
                    .date(dto.getDate()).time(dto.getTime())
                    .request(dto.getRequest()).response(dto.getResponse()).click(dto.getClick())
                    .build();
            statisticRepository.save(statistic);
        }
        return "success";
    }
}
