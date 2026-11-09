package com.ganjithon.handeulpan.pasture.dto;

import com.ganjithon.handeulpan.sheep.Distance;
import java.util.List;

/**
 * 홈(내 들판 목록) 한 줄.
 * edgeFriendNames: 가장자리에 있는 친구 양 이름 — 앱을 열 때 "두부 양이 조금 멀리 갔어요" 배너용 (F6)
 */
public record MyPastureResponse(
        String pastureId,
        String name,
        Long mySheepId,
        Distance myDistance,
        boolean pattedToday,
        List<String> edgeFriendNames) {
}
