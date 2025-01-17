package com.festival.domain.program.controller;

import com.festival.common.base.OperateStatus;
import com.festival.common.exception.ErrorCode;
import com.festival.common.exception.custom_exception.NotFoundException;
import com.festival.domain.image.model.Image;
import com.festival.domain.member.model.Member;
import com.festival.domain.program.dto.ProgramPageRes;
import com.festival.domain.program.dto.ProgramReq;
import com.festival.domain.program.dto.ProgramRes;
import com.festival.domain.program.model.Program;
import com.festival.domain.program.model.ProgramType;
import com.festival.domain.program.repository.ProgramRepository;
import com.festival.domain.util.ControllerTestSupport;
import com.festival.domain.util.TestImageUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import java.time.LocalDate;
import java.util.List;

import static com.festival.domain.member.model.MemberRole.ADMIN;
import static com.festival.domain.member.model.MemberRole.MANAGER;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.groups.Tuple.tuple;
import static org.springframework.http.MediaType.MULTIPART_FORM_DATA;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class ProgramIntegrationTest extends ControllerTestSupport {

    @Autowired
    private ProgramRepository programRepository;

    private Member member;

    private Member differentMember;

    @BeforeEach
    void setUp() {
        member = Member.builder()
                .username("testUser")
                .password("12345")
                .memberRole(ADMIN)
                .build();
        memberRepository.saveAndFlush(member);

        differentMember = Member.builder()
                .username("differentUser")
                .password("12345")
                .memberRole(MANAGER)
                .build();
        memberRepository.saveAndFlush(differentMember);

        Member differentMember2 = Member.builder()
                .username("differentUser2")
                .password("12345")
                .memberRole(MANAGER)
                .build();
        memberRepository.saveAndFlush(differentMember2);
    }

    @WithMockUser(username = "testUser", roles = "ADMIN")
    @DisplayName("프로그램 게시물을 생성한다.")
    @Test
    void createProgram() throws Exception {
        //given
        LocalDate registeredStartDate = LocalDate.of(2023, 9, 26);
        LocalDate registeredEndDate = LocalDate.of(2023, 10, 31);

        MockMultipartFile mainFile = TestImageUtils.generateMockImageFile("mainFile");
        List<MockMultipartFile> subFiles = List.of(
                TestImageUtils.generateMockImageFile("subFiles"),
                TestImageUtils.generateMockImageFile("subFiles"),
                TestImageUtils.generateMockImageFile("subFiles")
        );

        ProgramReq programReq = ProgramReq.builder()
                .title("title")
                .subTitle("subTitle")
                .content("content")
                .latitude(50.0f)
                .longitude(50.0f)
                .type("EVENT")
                .operateStatus("OPERATE")
                .mainFile(mainFile)
                .startDate(registeredStartDate)
                .endDate(registeredEndDate)
                .build();

        //when
        MvcResult mvcResult = mockMvc.perform(
                        multipart("/api/v2/program")
                                .file(mainFile)
                                .file(subFiles.get(0))
                                .file(subFiles.get(1))
                                .file(subFiles.get(2))
                                .param("title", programReq.getTitle())
                                .param("subTitle", programReq.getSubTitle())
                                .param("content", programReq.getContent())
                                .param("latitude", String.valueOf(programReq.getLatitude()))
                                .param("longitude", String.valueOf(programReq.getLongitude()))
                                .param("type", programReq.getType())
                                .param("operateStatus", programReq.getOperateStatus())
                                .param("startDate", String.valueOf(programReq.getStartDate()))
                                .param("endDate", String.valueOf(programReq.getEndDate()))
                                .contentType(MediaType.MULTIPART_FORM_DATA)
                )
                .andDo(print())
                .andExpect(status().isOk())
                .andReturn();

        //then
        long id = Long.parseLong(mvcResult.getResponse().getContentAsString());
        Program program = programRepository.findById(id).orElseThrow(() -> new NotFoundException(ErrorCode.NOT_FOUND_PROGRAM));
        assertThat(program).isNotNull()
                .extracting("title", "subTitle", "content", "latitude", "longitude", "operateStatus", "type")
                .containsExactly(programReq.getTitle(), programReq.getSubTitle(),
                        programReq.getContent(),
                        programReq.getLatitude(), programReq.getLongitude(),
                        OperateStatus.OPERATE, ProgramType.EVENT);
    }

    @WithMockUser(username = "testUser", roles = "ADMIN")
    @DisplayName("프로그램 게시물을 수정한다.")
    @Test
    void updateProgram() throws Exception {
        //given
        LocalDate registeredStartDate = LocalDate.of(2023, 9, 26);
        LocalDate registeredEndDate = LocalDate.of(2023, 10, 31);

        MockMultipartFile mainFile = TestImageUtils.generateMockImageFile("mainFile");
        List<MockMultipartFile> subFiles = List.of(
                TestImageUtils.generateMockImageFile("subFiles"),
                TestImageUtils.generateMockImageFile("subFiles"),
                TestImageUtils.generateMockImageFile("subFiles")
        );

        ProgramReq programReq = ProgramReq.builder()
                .title("title")
                .subTitle("subTitle")
                .content("content")
                .latitude(50.0f)
                .longitude(50.0f)
                .type("EVENT")
                .operateStatus("OPERATE")
                .mainFile(mainFile)
                .startDate(registeredStartDate)
                .endDate(registeredEndDate)
                .build();
        Program program = Program.of(programReq, LocalDate.now());
        program.setImage(createImage());
        program.connectMember(member);
        Program savedProgram = programRepository.saveAndFlush(program);

        //when
        MockHttpServletRequestBuilder request = makeMultiPartRequest(savedProgram, mainFile, subFiles);

        MvcResult mvcResult = mockMvc.perform(request)
                .andDo(print())
                .andExpect(status().isOk())
                .andReturn();

        //then
        long id = Long.parseLong(mvcResult.getResponse().getContentAsString());
        Program updatedProgram = programRepository.findById(id).orElseThrow(() -> new NotFoundException(ErrorCode.NOT_FOUND_PROGRAM));
        assertThat(updatedProgram).isNotNull()
                .extracting("title", "subTitle", "content", "latitude", "longitude", "operateStatus", "type")
                .containsExactly("updateTitle", "updateSubTitle", "updateContent", 30.0f, 30.0f, OperateStatus.OPERATE, ProgramType.GAME);
    }

    @WithMockUser(username = "differentUser", roles = "MANAGER")
    @DisplayName("권한이 없는 프로그램 게시물은 수정할 수 없다.")
    @Test
    void updateProgramNotMine() throws Exception {
        //given
        LocalDate registeredDate = LocalDate.of(2023, 9, 26);
        LocalDate registeredStartDate = LocalDate.of(2023, 9, 26);
        LocalDate registeredEndDate = LocalDate.of(2023, 10, 31);

        MockMultipartFile mainFile = TestImageUtils.generateMockImageFile("mainFile");
        List<MockMultipartFile> subFiles = List.of(
                TestImageUtils.generateMockImageFile("subFiles"),
                TestImageUtils.generateMockImageFile("subFiles"),
                TestImageUtils.generateMockImageFile("subFiles")
        );

        ProgramReq programReq = ProgramReq.builder()
                .title("title")
                .subTitle("subTitle")
                .content("content")
                .latitude(50.0f)
                .longitude(50.0f)
                .type("EVENT")
                .operateStatus("OPERATE")
                .mainFile(mainFile)
                .startDate(registeredStartDate)
                .endDate(registeredEndDate)
                .build();
        Program program = Program.of(programReq, registeredDate);
        program.connectMember(member);
        Program savedProgram = programRepository.saveAndFlush(program);

        //when //then
        MockHttpServletRequestBuilder request = makeMultiPartRequest(savedProgram, mainFile, subFiles);
        mockMvc.perform(request)
                .andDo(print())
                .andExpect(status().isForbidden());
    }

    @WithMockUser(username = "testUser", roles = "ADMIN")
    @DisplayName("존재하지 않는 프로그램 게시물은 수정할 수 없다.")
    @Test
    void updateProgramNotFound() throws Exception {
        //given
        MockMultipartFile mainFile = TestImageUtils.generateMockImageFile("mainFile");
        List<MockMultipartFile> subFiles = List.of(
                TestImageUtils.generateMockImageFile("subFiles"),
                TestImageUtils.generateMockImageFile("subFiles"),
                TestImageUtils.generateMockImageFile("subFiles")
        );

        //when //then
        MockHttpServletRequestBuilder request = makeMultiPartRequest(Program.builder().id(1L).build(), mainFile, subFiles);
        mockMvc.perform(request)
                .andDo(print())
                .andExpect(status().isNotFound());
    }

    @WithMockUser(username = "testUser", roles = "ADMIN")
    @DisplayName("사용자는 상태값만 수정할수도 있다.")
    @Test
    void updateProgramStatus() throws Exception {
        //given
        LocalDate registeredStartDate = LocalDate.of(2023, 9, 26);
        LocalDate registeredEndDate = LocalDate.of(2023, 10, 31);

        MockMultipartFile mainFile = TestImageUtils.generateMockImageFile("mainFile");
        List<MockMultipartFile> subFiles = List.of(
                TestImageUtils.generateMockImageFile("subFiles"),
                TestImageUtils.generateMockImageFile("subFiles"),
                TestImageUtils.generateMockImageFile("subFiles")
        );

        ProgramReq programReq = ProgramReq.builder()
                .title("title")
                .subTitle("subTitle")
                .content("content")
                .latitude(50.0f)
                .longitude(50.0f)
                .type("EVENT")
                .operateStatus("OPERATE")
                .mainFile(mainFile)
                .startDate(registeredStartDate)
                .endDate(registeredEndDate)
                .build();
        Program program = Program.of(programReq, LocalDate.now());
        program.connectMember(member);
        Program savedProgram = programRepository.saveAndFlush(program);

        //when
        MvcResult mvcResult = mockMvc.perform(
                        patch("/api/v2/program/" + savedProgram.getId())
                                .param("status", "TERMINATE")
                )
                .andDo(print())
                .andExpect(status().isOk())
                .andReturn();

        //then
        long id = Long.parseLong(mvcResult.getResponse().getContentAsString());
        Program updatedProgram = programRepository.findById(id).orElseThrow(() -> new NotFoundException(ErrorCode.NOT_FOUND_PROGRAM));
        assertThat(updatedProgram.getOperateStatus()).isEqualTo(OperateStatus.TERMINATE);
    }

    @WithMockUser(username = "differentUser2", roles = "MANAGER")
    @DisplayName("권한이 없는 사용자는 상태값을 수정할 수 없다.")
    @Test
    void updateProgramStatusWithoutPermission() throws Exception {
        //given
        LocalDate registeredStartDate = LocalDate.of(2023, 9, 26);
        LocalDate registeredEndDate = LocalDate.of(2023, 10, 31);

        MockMultipartFile mainFile = TestImageUtils.generateMockImageFile("mainFile");
        List<MockMultipartFile> subFiles = List.of(
                TestImageUtils.generateMockImageFile("subFiles"),
                TestImageUtils.generateMockImageFile("subFiles"),
                TestImageUtils.generateMockImageFile("subFiles")
        );

        ProgramReq programReq = ProgramReq.builder()
                .title("title")
                .subTitle("subTitle")
                .content("content")
                .latitude(50.0f)
                .longitude(50.0f)
                .type("EVENT")
                .operateStatus("OPERATE")
                .mainFile(mainFile)
                .startDate(registeredStartDate)
                .endDate(registeredEndDate)
                .build();
        Program program = Program.of(programReq, LocalDate.now());
        program.connectMember(differentMember);
        Program savedProgram = programRepository.saveAndFlush(program);

        //when //then
        mockMvc.perform(
                        patch("/api/v2/program/" + savedProgram.getId())
                                .param("status", "TERMINATE")
                )
                .andDo(print())
                .andExpect(status().isForbidden());
    }

    @WithMockUser(username = "testUser", roles = "ADMIN")
    @DisplayName("프로그램 게시물을 삭제한다.")
    @Test
    void deleteProgram() throws Exception {
        //given
        LocalDate registeredDate = LocalDate.of(2023, 9, 26);
        LocalDate registeredStartDate = LocalDate.of(2023, 9, 26);
        LocalDate registeredEndDate = LocalDate.of(2023, 10, 31);

        MockMultipartFile mainFile = TestImageUtils.generateMockImageFile("mainFile");
        List<MockMultipartFile> subFiles = List.of(
                TestImageUtils.generateMockImageFile("subFiles"),
                TestImageUtils.generateMockImageFile("subFiles"),
                TestImageUtils.generateMockImageFile("subFiles")
        );

        ProgramReq programReq = ProgramReq.builder()
                .title("title")
                .subTitle("subTitle")
                .content("content")
                .latitude(50.0f)
                .longitude(50.0f)
                .type("EVENT")
                .operateStatus("OPERATE")
                .mainFile(mainFile)
                .startDate(registeredStartDate)
                .endDate(registeredEndDate)
                .build();
        Program program = Program.of(programReq, registeredDate);
        program.connectMember(member);
        Program savedProgram = programRepository.saveAndFlush(program);

        //when
        mockMvc.perform(
                        delete("/api/v2/program/" + savedProgram.getId())
                )
                .andDo(print())
                .andExpect(status().isOk());

        //then
        Program findProgram = programRepository.findById(savedProgram.getId()).orElseThrow(() -> new NotFoundException(ErrorCode.NOT_FOUND_PROGRAM));
        assertThat(findProgram.isDeleted()).isEqualTo(true);
    }

    @WithMockUser(username = "differentUser", roles = "MANAGER")
    @DisplayName("권한이 없는 프로그램 게시물은 삭제할 수 없다.")
    @Test
    void deleteProgramNotMine() throws Exception {
        //given
        LocalDate registeredDate = LocalDate.of(2023, 9, 26);
        LocalDate registeredStartDate = LocalDate.of(2023, 9, 26);
        LocalDate registeredEndDate = LocalDate.of(2023, 10, 31);

        MockMultipartFile mainFile = TestImageUtils.generateMockImageFile("mainFile");
        List<MockMultipartFile> subFiles = List.of(
                TestImageUtils.generateMockImageFile("subFiles"),
                TestImageUtils.generateMockImageFile("subFiles"),
                TestImageUtils.generateMockImageFile("subFiles")
        );

        ProgramReq programReq = ProgramReq.builder()
                .title("title")
                .subTitle("subTitle")
                .content("content")
                .latitude(50.0f)
                .longitude(50.0f)
                .type("EVENT")
                .operateStatus("OPERATE")
                .mainFile(mainFile)
                .startDate(registeredStartDate)
                .endDate(registeredEndDate)
                .build();
        Program program = Program.of(programReq, registeredDate);
        program.connectMember(member);
        Program savedProgram = programRepository.saveAndFlush(program);

        //when //then
        mockMvc.perform(
                        delete("/api/v2/program/" + savedProgram.getId())
                )
                .andDo(print())
                .andExpect(status().isForbidden());
    }

    @DisplayName("존재하지 않는 프로그램 게시물은 삭제할 수 없다.")
    @WithMockUser(username = "testUser", roles = "ADMIN")
    @Test
    void deleteProgramNotFound() throws Exception {
        //when //then
        mockMvc.perform(
                        delete("/api/v2/program/1")
                )
                .andDo(print())
                .andExpect(status().isNotFound());
    }

    @DisplayName("프로그램 게시물을 조회한다.")
    @Test
    void getProgram() throws Exception {
        //given
        LocalDate registeredDate = LocalDate.of(2023, 9, 26);
        LocalDate registeredStartDate = LocalDate.of(2023, 9, 26);
        LocalDate registeredEndDate = LocalDate.of(2023, 10, 31);

        MockMultipartFile mainFile = TestImageUtils.generateMockImageFile("mainFile");
        List<MockMultipartFile> subFiles = List.of(
                TestImageUtils.generateMockImageFile("subFiles"),
                TestImageUtils.generateMockImageFile("subFiles"),
                TestImageUtils.generateMockImageFile("subFiles")
        );

        ProgramReq programReq = ProgramReq.builder()
                .title("title")
                .subTitle("subTitle")
                .content("content")
                .latitude(50.0f)
                .longitude(50.0f)
                .type("EVENT")
                .operateStatus("OPERATE")
                .mainFile(mainFile)
                .startDate(registeredStartDate)
                .endDate(registeredEndDate)
                .build();
        Program program = Program.of(programReq, registeredDate);
        program.connectMember(member);
        Image image = Image.builder()
                .mainFilePath("mainFilePath")
                .subFilePaths(List.of("subFilePath1", "subFilePath2", "subFilePath3"))
                .build();
        program.setImage(image);
        Program savedProgram = programRepository.saveAndFlush(program);

        //when
        MvcResult mvcResult = mockMvc.perform(
                        get("/api/v2/program/" + savedProgram.getId())
                )
                .andDo(print())
                .andExpect(status().isOk())
                .andReturn();

        //then
        ProgramRes programRes = objectMapper.readValue(mvcResult.getResponse().getContentAsString(), ProgramRes.class);
        assertThat(programRes)
                .extracting("title", "subTitle", "content", "latitude", "longitude", "operateStatus", "type")
                .containsExactly(programReq.getTitle(), programReq.getSubTitle(),
                        programReq.getContent(),
                        programReq.getLatitude(), programReq.getLongitude(),
                        programReq.getOperateStatus(), programReq.getType());
    }

    @DisplayName("프로그램 게시물을 목록조회한다. 페이지당 게시물은 6개이다.")
    @Test
    void getProgramList() throws Exception {
        //given
        LocalDate registeredDate = LocalDate.of(2023, 9, 26);
        LocalDate registeredStartDate = LocalDate.of(2023, 9, 26);
        LocalDate registeredEndDate = LocalDate.of(2023, 10, 31);

        MockMultipartFile mainFile = TestImageUtils.generateMockImageFile("mainFile");
        List<MockMultipartFile> subFiles = List.of(
                TestImageUtils.generateMockImageFile("subFiles"),
                TestImageUtils.generateMockImageFile("subFiles"),
                TestImageUtils.generateMockImageFile("subFiles")
        );

        Image image = Image.builder()
                .mainFilePath("mainFilePath")
                .subFilePaths(List.of("subFilePath1", "subFilePath2", "subFilePath3"))
                .build();

        ProgramReq programReq = ProgramReq.builder()
                .title("title")
                .subTitle("subTitle")
                .content("content")
                .latitude(50.0f)
                .longitude(50.0f)
                .type("EVENT")
                .operateStatus("OPERATE")
                .mainFile(mainFile)
                .startDate(registeredStartDate)
                .endDate(registeredEndDate)
                .build();
        Program program1 = Program.of(programReq, registeredDate);
        program1.connectMember(member);
        program1.setImage(image);
        Program program2 = Program.of(programReq, registeredDate);
        program2.connectMember(member);
        program2.setImage(image);
        Program program3 = Program.of(programReq, registeredDate);
        program3.connectMember(member);
        program3.setImage(image);
        Program program4 = Program.of(programReq, registeredDate);
        program4.connectMember(member);
        program4.setImage(image);
        Program program5 = Program.of(programReq, registeredDate);
        program5.connectMember(member);
        program5.setImage(image);
        Program program6 = Program.of(programReq, registeredDate);
        program6.connectMember(member);
        program6.setImage(image);
        Program program7 = Program.of(programReq, registeredDate);
        program7.connectMember(member);
        program7.setImage(image);

        programRepository.saveAll(List.of(program1, program2, program3, program4, program5,
                program6, program7));

        //when
        Pageable pageable = PageRequest.of(0, 6);
        MvcResult mvcResult = mockMvc.perform(
                        get("/api/v2/program/list")
                                .param("status", "OPERATE")
                                .param("type", "EVENT")
                                .param("page", String.valueOf(pageable.getPageNumber()))
                                .param("size", String.valueOf(pageable.getPageSize()))
                )
                .andDo(print())
                .andExpect(status().isOk())
                .andReturn();

        //then
        ProgramPageRes programPageRes = objectMapper.readValue(mvcResult.getResponse().getContentAsString(), ProgramPageRes.class);
        assertThat(programPageRes.getProgramList()).hasSize(6)
                .extracting("title", "subTitle", "content", "latitude", "longitude", "operateStatus", "type")
                .containsExactly(
                        tuple(programReq.getTitle(), programReq.getSubTitle(),
                                programReq.getContent(),
                                programReq.getLatitude(), programReq.getLongitude(),
                                programReq.getOperateStatus(), programReq.getType()),
                        tuple(programReq.getTitle(), programReq.getSubTitle(),
                                programReq.getContent(),
                                programReq.getLatitude(), programReq.getLongitude(),
                                programReq.getOperateStatus(), programReq.getType()),
                        tuple(programReq.getTitle(), programReq.getSubTitle(),
                                programReq.getContent(),
                                programReq.getLatitude(), programReq.getLongitude(),
                                programReq.getOperateStatus(), programReq.getType()),
                        tuple(programReq.getTitle(), programReq.getSubTitle(),
                                programReq.getContent(),
                                programReq.getLatitude(), programReq.getLongitude(),
                                programReq.getOperateStatus(), programReq.getType()),
                        tuple(programReq.getTitle(), programReq.getSubTitle(),
                                programReq.getContent(),
                                programReq.getLatitude(), programReq.getLongitude(),
                                programReq.getOperateStatus(), programReq.getType()),
                        tuple(programReq.getTitle(), programReq.getSubTitle(),
                                programReq.getContent(),
                                programReq.getLatitude(), programReq.getLongitude(),
                                programReq.getOperateStatus(), programReq.getType())
                );

        assertThat(programPageRes).isNotNull()
                .extracting("totalCount", "totalPage", "pageNumber", "pageSize")
                .contains(7L, 2, 0, 6);
    }

    private static MockHttpServletRequestBuilder makeMultiPartRequest(Program savedProgram, MockMultipartFile mainFile, List<MockMultipartFile> subFiles) {
        LocalDate registeredStartDate = LocalDate.of(2023, 9, 26);
        LocalDate registeredEndDate = LocalDate.of(2023, 10, 31);

        return multipart("/api/v2/program/" + savedProgram.getId())
                .file(mainFile)
                .file(subFiles.get(0))
                .file(subFiles.get(1))
                .file(subFiles.get(2))
                .param("title", "updateTitle")
                .param("subTitle", "updateSubTitle")
                .param("content", "updateContent")
                .param("latitude", String.valueOf(30.0f))
                .param("longitude", String.valueOf(30.0f))
                .param("type", "GAME")
                .param("operateStatus", "OPERATE")
                .param("startDate", registeredStartDate.toString())
                .param("endDate", registeredEndDate.toString())
                .contentType(MULTIPART_FORM_DATA)
                .with(request -> {
                    request.setMethod("PUT");
                    return request;
                });
    }

    private Image createImage() {
        return Image.builder()
                .mainFilePath("/mainFile")
                .subFilePaths(List.of("/subFile1", "/subFile2"))
                .build();
    }

}