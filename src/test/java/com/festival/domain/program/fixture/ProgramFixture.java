package com.festival.domain.program.fixture;

import com.festival.common.base.OperateStatus;
import com.festival.domain.program.model.Program;
import com.festival.domain.program.model.ProgramType;

public class ProgramFixture {
    public static Program EVENT = Program.builder()
            .title("이벤트 게시물 제목")
            .subTitle("이벤트 게시물 부제목")
            .status(OperateStatus.OPERATE)
            .content("이벤트 게시물 내용")
            .longitude(50)
            .latitude(50)
            .type(ProgramType.EVENT)
            .build();


    public static Program GAME = Program.builder()
            .title("게임 게시물 제목")
            .subTitle("게임 게시물 부제목")
            .status(OperateStatus.OPERATE)
            .content("게임 게시물 내용")
            .longitude(50)
            .latitude(50)
            .type(ProgramType.GAME)
            .build();
}
