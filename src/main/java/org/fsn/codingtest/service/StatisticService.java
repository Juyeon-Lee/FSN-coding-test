package org.fsn.codingtest.service;

import lombok.RequiredArgsConstructor;
import org.fsn.codingtest.domain.Statistic;
import org.fsn.codingtest.domain.StatisticRepository;
import org.fsn.codingtest.web.SearchResponseDto;
import org.fsn.codingtest.web.UploadRequestDto;
import org.fsn.codingtest.web.UploadResponseDto;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
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
    public SearchResponseDto findSumByDate(String string) {
        LocalDate date = LocalDate.parse(string, DateTimeFormatter.ISO_DATE);
        List<Statistic> list = statisticRepository.findAllByDate(date);
        SearchResponseDto dto;
        if(list.size()==0) { // 해당 날짜에 데이터가 아무것도 없으면
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
    public SearchResponseDto findByDateTime(String string, String time) {
        LocalDate date = LocalDate.parse(string, DateTimeFormatter.ISO_DATE);
        int intTime = validIntTime(time);

        Optional<Statistic> statistic = statisticRepository.findByDateAndTime(date, intTime);
        SearchResponseDto dto;
        if(statistic.isPresent()){
            Statistic data = statistic.get();
            dto = new SearchResponseDto(data.getRequest(), data.getResponse(), data.getClick());
        }else{
            dto = new SearchResponseDto(0,0,0);
        }
        return dto;
    }

    // time을 string->int 변환한다. 변환 시 format에러가 있을 경우 Exception을 throw한다.
    private int validIntTime(String time) {
        int intTime;
        try{
            intTime = Integer.parseInt(time);
            if (intTime < 0 || intTime >= 24) {
                throw new DateTimeParseException("time should have value between 0,23", time, 0);
            }
        }catch (NumberFormatException ex){  // 문자열 입력 시
            throw new DateTimeParseException("time should have value between 0,23", time, 0);
        }

        return intTime;
    }

    /*
    날짜와 시각이 존재하지 않으면 신규 추가, 존재하면 기존 데이터 업데이트
    날짜, 시각, 요청 수, 응답 수, 클릭 수 정보를 담고 있는 json 형식 파일 이용
     */
    @Transactional
    public UploadResponseDto upload(UploadRequestDto dto) {
        LocalDate date = LocalDate.parse(dto.getDate(), DateTimeFormatter.ISO_DATE);
        String time = dto.getTime();
        int intTime = validIntTime(time);

        Optional<Statistic> data = statisticRepository.findByDateAndTime(date, intTime);
        if(data.isPresent()){   //기존 데이터 업데이트
            Statistic statistic = data.get();
            statistic.updateThreeData(dto.getRequest(),dto.getResponse(),dto.getClick());
        }else{  // 신규 추가
            Statistic statistic = Statistic.builder()
                    .date(date).time(intTime)
                    .request(dto.getRequest()).response(dto.getResponse()).click(dto.getClick())
                    .build();
            statisticRepository.save(statistic);
        }
        return new UploadResponseDto("success");
    }
}
