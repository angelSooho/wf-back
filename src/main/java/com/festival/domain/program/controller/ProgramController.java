package com.festival.domain.program.controller;

import com.festival.common.util.ValidationUtils;
import com.festival.domain.program.dto.ProgramListReq;
import com.festival.domain.program.dto.ProgramReq;
import com.festival.domain.program.dto.ProgramRes;
import com.festival.domain.program.service.ProgramService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v2/program")
public class ProgramController {

    private final ProgramService programService;
    private final ValidationUtils validationUtils;

    @PreAuthorize("hasAuthority({'ADMIN', 'MANAGER'})")
    @PostMapping
    public ResponseEntity<Long> create(@Valid ProgramReq programReqDto) throws Exception {
        if (!validationUtils.isProgramValid(programReqDto)) {
            throw new Exception();
        }
        return ResponseEntity.ok().body(programService.createProgram(programReqDto));
    }

    @PreAuthorize("hasAuthority({'ADMIN', 'MANAGER'})")
    @PutMapping("/{programId}")
    public ResponseEntity<Long> update(@PathVariable Long programId,
                                       @Valid ProgramReq programReqDto) throws Exception {
        if (!validationUtils.isProgramValid(programReqDto)) {
            throw new Exception();
        }
        return ResponseEntity.ok().body(programService.updateProgram(programId, programReqDto));
    }

    @PreAuthorize("hasAuthority({'ADMIN', 'MANAGER'})")
    @DeleteMapping("/{programId}")
    public ResponseEntity<Void> delete(@PathVariable Long programId) {
        programService.delete(programId);
        return ResponseEntity.ok().build();
    }

    @PreAuthorize("permitAll()")
    @GetMapping("/{programId}")
    public ResponseEntity<ProgramRes> getProgram(@PathVariable Long programId) {
        return ResponseEntity.ok().body(programService.getProgram(programId));
    }

    @PreAuthorize("permitAll()")
    @GetMapping("/list")
    public ResponseEntity<List<ProgramRes>> getList(@Valid ProgramListReq programListReqDto, Pageable pageable) {
        return ResponseEntity.ok().body(programService.getProgramList(programListReqDto, pageable));
    }

}