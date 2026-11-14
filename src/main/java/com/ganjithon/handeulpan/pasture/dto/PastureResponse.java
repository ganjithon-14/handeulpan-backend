package com.ganjithon.handeulpan.pasture.dto;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.ganjithon.handeulpan.sheep.Distance;
import com.ganjithon.handeulpan.sheep.WoolColor;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import lombok.Getter;

/**
 * 들판 조회 — 이 응답 하나로 들판 화면 전체를 그립니다.
 * BE-B 데이터(말풍선·목도리)는 {@link com.ganjithon.handeulpan.pasture.PastureViewExtension}으로 덧붙습니다.
 */
@Getter
@JsonPropertyOrder({"pastureId", "name", "maxSheep", "mySheepId", "sheep"})
public class PastureResponse {

    private final String pastureId;
    private final String name;
    private final int maxSheep;
    private final Long mySheepId;
    private final List<SheepView> sheep;
    private final Map<String, Object> extra = new LinkedHashMap<>();

    public PastureResponse(String pastureId, String name, int maxSheep, Long mySheepId, List<SheepView> sheep) {
        this.pastureId = pastureId;
        this.name = name;
        this.maxSheep = maxSheep;
        this.mySheepId = mySheepId;
        this.sheep = sheep;
    }

    @JsonAnyGetter
    public Map<String, Object> getExtra() {
        return extra;
    }

    @Getter
    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonPropertyOrder({"id", "name", "woolColor", "distance", "mine", "pattedToday", "napping"})
    public static class SheepView {

        private final Long id;
        private final String name;
        private final WoolColor woolColor;
        private final Distance distance;
        private final boolean mine;
        /** 내 양에만 내려갑니다. 친구 양은 null(필드 생략) — 접속·체크인 여부를 노출하지 않기 위해. */
        private final Boolean pattedToday;
        /** 낮잠 중인 양. 친구에게도 보입니다. 끝나는 시각은 내보내지 않습니다. */
        private final boolean napping;
        private final Map<String, Object> extra = new LinkedHashMap<>();

        public SheepView(Long id, String name, WoolColor woolColor, Distance distance, boolean mine, Boolean pattedToday,
                         boolean napping) {
            this.id = id;
            this.name = name;
            this.woolColor = woolColor;
            this.distance = distance;
            this.mine = mine;
            this.pattedToday = pattedToday;
            this.napping = napping;
        }

        @JsonAnyGetter
        public Map<String, Object> getExtra() {
            return extra;
        }
    }
}
