package com.festival.domain.timetable.service;

import com.festival.common.exception.ErrorCode;
import com.festival.common.exception.custom_exception.ForbiddenException;
import com.festival.common.exception.custom_exception.NotFoundException;
import com.festival.domain.timetable.dto.TimeTableCreateReq;
import com.festival.domain.timetable.dto.TimeTableDateReq;
import com.festival.domain.timetable.dto.TimeTableRes;
import com.festival.domain.timetable.dto.TimeTableSearchCond;
import com.festival.domain.timetable.model.TimeTable;
import com.festival.domain.timetable.repository.TimeTableRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static com.festival.domain.timetable.model.TimeTableStatus.TERMINATE;

@Transactional(readOnly = true)
@RequiredArgsConstructor
@Service
public class TimeTableService {

    private final TimeTableRepository timeTableRepository;

    @Transactional
    public Long create(TimeTableCreateReq timeTableCreateReq) {
        TimeTable timeTable = TimeTable.of(timeTableCreateReq);
        TimeTable savedTimeTable = timeTableRepository.save(timeTable);
        return savedTimeTable.getId();
    }

    @Transactional
    public Long update(Long timeTableId, TimeTableCreateReq timeTableCreateReq, String username) {
        TimeTable timeTable = timeTableRepository.findById(timeTableId).orElseThrow(() -> new NotFoundException(ErrorCode.NOT_FOUND_TIMETABLE));
        if (timeTable.getLastModifiedBy().equals(username)) {
            throw new ForbiddenException(ErrorCode.FORBIDDEN_UPDATE);
        }
        timeTable.update(timeTableCreateReq);
        return timeTable.getId();
    }

    @Transactional
    public void delete(Long id, String username) {
        TimeTable timeTable = timeTableRepository.findById(id).orElseThrow(() -> new NotFoundException(ErrorCode.NOT_FOUND_TIMETABLE));
        if (timeTable.getLastModifiedBy().equals(username)) {
            throw new ForbiddenException(ErrorCode.FORBIDDEN_DELETE);
        }
        timeTable.changeStatus(TERMINATE);
    }

    public List<TimeTableRes> getList(TimeTableDateReq timeTableDateReq) {
        TimeTableSearchCond timeTableSearchCond = new TimeTableSearchCond(
                timeTableDateReq.getStartTime(), timeTableDateReq.getEndTime(),
                timeTableDateReq.getStatus()
        );
        return timeTableRepository.getList(timeTableSearchCond);
    }

}
