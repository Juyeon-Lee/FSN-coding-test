package org.fsn.codingtest.domain;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import java.time.LocalDate;

@Getter
@NoArgsConstructor
@Entity
public class Statistic {
    @Id @GeneratedValue
    private Long id;

    @Column(nullable = false)
    private LocalDate date;     // 집계 날짜
    @Column(nullable = false)
    private int time;   // 집계 시각

    private int request;    // 광고 요청 수
    private int response;   // 광고 응답 수
    private int click;      // 광고 클릭 수

    @Builder
    public Statistic(LocalDate date, int time, int request, int response, int click) {
        this.date = date;
        this.time = time;
        this.request = request;
        this.response = response;
        this.click = click;
    }

    public void updateThreeData(int request, int response, int click){
        this.request = request;
        this.response = response;
        this.click = click;
    }
}
