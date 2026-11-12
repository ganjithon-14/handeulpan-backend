package com.ganjithon.handeulpan.scarf.dto;

import com.ganjithon.handeulpan.sheep.WoolColor;

/** @param color 보낸 목도리의 색 (= 보낸 양의 털 색) */
public record SendScarfResponse(WoolColor color) {
}
