package com.festival.domain.info.festivalPub.data.dto.response;

import com.festival.domain.info.festivalPub.data.entity.file.SubFilePath;
import com.festival.domain.info.festivalPub.data.entity.pub.Pub;
import com.querydsl.core.annotations.QueryProjection;
import jakarta.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PubResponse {

    private Long pubId;

    private String title;

    private String subTitle;

    private String content;

    private LocalDateTime createdDate;

    private LocalDateTime modifiedDate;

    private String filePath;

    private List<String> subFilePath;

    private int latitude;

    private int longitude;

    private Boolean pubState;

    @Builder
    @QueryProjection
    public PubResponse(Long pubId, String title, String subTitle, String content, LocalDateTime createdDate, LocalDateTime modifiedDate, String filePath, List<String> subFilePath, int latitude, int longitude, Boolean pubState) {
        this.pubId = pubId;
        this.title = title;
        this.subTitle = subTitle;
        this.content = content;
        this.createdDate = createdDate;
        this.modifiedDate = modifiedDate;
        this.filePath = filePath;
        this.subFilePath = subFilePath;
        this.latitude = latitude;
        this.longitude = longitude;
        this.pubState = pubState;
    }

    public static PubResponse of(final Pub pub, String filePath) {
        if (!pub.getPubImage().getSubFilePaths().isEmpty()) {
            List<String > list = new ArrayList<>();
            for (SubFilePath subFilePath: pub.getPubImage().getSubFilePaths()) {
                list.add(filePath + subFilePath.getFilePath());
            }
            return PubResponse.builder()
                    .pubId(pub.getId())
                    .title(pub.getTitle())
                    .subTitle(pub.getSubTitle())
                    .content(pub.getContent())
                    .createdDate(pub.getCreatedDate())
                    .modifiedDate(pub.getModifiedDate())
                    .filePath(pub.getPubImage().getMainFilePath())
                    .subFilePath(list)
                    .latitude(pub.getLatitude())
                    .longitude(pub.getLongitude())
                    .pubState(pub.getPubState())
                    .build();
        } else {
            return PubResponse.builder()
                    .pubId(pub.getId())
                    .title(pub.getTitle())
                    .content(pub.getContent())
                    .createdDate(pub.getCreatedDate())
                    .modifiedDate(pub.getModifiedDate())
                    .filePath(pub.getPubImage().getMainFilePath())
                    .latitude(pub.getLatitude())
                    .longitude(pub.getLongitude())
                    .pubState(pub.getPubState())
                    .build();
        }
    }
}