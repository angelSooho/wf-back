package com.festival.domain.timetable.model;

import com.festival.common.base.BaseEntity;
import com.festival.domain.member.model.Member;
import com.festival.domain.timetable.dto.TimeTableReq;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity(name = "time_table")
public class TimeTable extends BaseEntity {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false)
    private LocalDateTime startTime;

    @Column(nullable = false)
    private LocalDateTime endTime;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id")
    private Member member;

    @Builder
    private TimeTable(String title, LocalDateTime startTime, LocalDateTime endTime, Boolean deleted) {
        this.title = title;
        this.startTime = startTime;
        this.endTime = endTime;
        this.deleted = deleted;
    }

    public static TimeTable of(TimeTableReq timeTableReq) {
        return TimeTable.builder()
                .title(timeTableReq.getTitle())
                .startTime(timeTableReq.getStartTime())
                .endTime(timeTableReq.getEndTime())
                .deleted(false)
                .build();
    }

    public void update(TimeTableReq timeTableReq) {
        this.title = timeTableReq.getTitle();
        this.startTime = timeTableReq.getStartTime();
        this.endTime = timeTableReq.getEndTime();
    }

    public void deleteTimeTable() {
        this.deleted = true;
    }

    public void connectMember(Member member) {
        this.member = member;
    }
}
