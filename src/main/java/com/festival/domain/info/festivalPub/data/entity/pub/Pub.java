package com.festival.domain.info.festivalPub.data.entity.pub;

import com.festival.domain.admin.data.entity.Admin;
import com.festival.domain.info.festivalPub.data.dto.request.PubRequest;
import com.festival.domain.info.festivalPub.data.entity.file.PubImage;
import com.festival.common.base.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Pub extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "title", nullable = false)
    private String title;

    @Column(name = "sub_title", nullable = false)
    private String subTitle;

    @Column(name = "content", nullable = false)
    private String content;

    @OneToOne(fetch = FetchType.LAZY, cascade = CascadeType.REMOVE)
    @JoinColumn(name = "pub_image_id")
    private PubImage pubImage;

    @Column(name = "latitude", nullable = false) // 위도
    private int latitude;

    @Column(name = "longitude", nullable = false) // 경도
    private int longitude;

    @Column(name = "pub_state", nullable = false)
    private Boolean pubState;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "admin_id", nullable = false)
    private Admin admin;

    public Pub(String title, String subTitle, String content, int latitude, int longitude, Boolean pubState, Admin admin) {
        this.title = title;
        this.subTitle = subTitle;
        this.content = content;
        this.latitude = latitude;
        this.longitude = longitude;
        this.pubState = pubState;
        this.admin = admin;
    }

    public Pub(PubRequest pubRequest, Admin admin) {
        this.title = pubRequest.getTitle();
        this.subTitle = pubRequest.getSubTitle();
        this.content = pubRequest.getContent();
        this.latitude = pubRequest.getLatitude();
        this.longitude = pubRequest.getLongitude();
        this.pubState = pubRequest.getPubState();
        this.admin = admin;
    }

    public void setPubImage(PubImage pubImage) {
        this.pubImage = pubImage;
    }

    public void modify(PubRequest pubRequest) {
        this.title = pubRequest.getTitle();
        this.subTitle = pubRequest.getSubTitle();
        this.content = pubRequest.getContent();
        this.latitude = pubRequest.getLatitude();
        this.longitude = pubRequest.getLongitude();
        this.pubState = pubRequest.getPubState();
    }
}