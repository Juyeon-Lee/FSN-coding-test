package org.fsn.codingtest.web;

import lombok.RequiredArgsConstructor;
import org.fsn.codingtest.service.StatisticService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RequiredArgsConstructor
@RestController
public class StatisticApiController {

    @Autowired
    private final StatisticService statisticService;

    /*
    -	통계 조회 기능 - overloading
        -	파라메터로 날짜 정보 전달 : 해당 날짜의 요청 수, 응답 수, 클릭 수 합계를 JSON 형식으로 응답
        -	파라메터로 날짜와 시각 정보 전달 : 해당 날짜의 시각에 요청 수, 응답 수, 클릭 수 JSON 형식으로 응답
     */
    @GetMapping("/api/v1/date={date}")
    public ResponseEntity<SearchResponseDto> search(@PathVariable("date") String date){
        SearchResponseDto dto = statisticService.findSumByDate(date);
        if (dto == null){
            return ResponseEntity.notFound().build();
        }else{
            return ResponseEntity.ok(dto);
        }
    }
    @GetMapping({"/api/v1/date={date}/time={time}"
            , "/api/v1/time={time}/date={date}"})
    public ResponseEntity<SearchResponseDto> search(@PathVariable("date") String date,
                                                    @PathVariable("time") int time){
        SearchResponseDto dto = statisticService.findByDateTime(date,time);
        if (dto == null){
            return ResponseEntity.notFound().build();
        }else{
            return ResponseEntity.ok(dto);
        }
    }

    /*
    -	통계 데이터 업로드 기능
        -	날짜, 시각, 요청 수, 응답 수, 클릭 수 정보를 담고 있는 JSON 형식 파일 업로드
        -   업로드 결과(success, error) return
     */
    @PostMapping("/api/v1")
    public ResponseEntity<UploadResponseDto> upload(@RequestBody UploadRequestDto dto){
        UploadResponseDto response = statisticService.upload(dto);
        return ResponseEntity.ok(response);
    }
}
