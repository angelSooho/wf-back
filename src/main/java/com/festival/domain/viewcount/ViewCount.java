package com.festival.domain.viewcount;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity(name = "view_count")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
public class ViewCount {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    private long count;


    public ViewCount(long count) {
        this.count = count;
    }

    public void plus(long viewCount) {
        this.count += viewCount;
    }
}
