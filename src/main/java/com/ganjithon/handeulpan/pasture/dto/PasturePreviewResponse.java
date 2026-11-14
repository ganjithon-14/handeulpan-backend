package com.ganjithon.handeulpan.pasture.dto;

import com.ganjithon.handeulpan.sheep.Distance;
import com.ganjithon.handeulpan.sheep.WoolColor;
import java.util.List;

/** 링크를 받은 사람이 합류 전에 보는 들판. 토큰 없이 볼 수 있어 양 id는 내보내지 않습니다. */
public record PasturePreviewResponse(
        String pastureId,
        String name,
        int maxSheep,
        boolean full,
        List<PreviewSheep> sheep) {

    public record PreviewSheep(String name, WoolColor woolColor, Distance distance, boolean napping) {
    }
}
