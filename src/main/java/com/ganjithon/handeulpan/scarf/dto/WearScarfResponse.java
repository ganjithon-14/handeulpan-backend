package com.ganjithon.handeulpan.scarf.dto;

import com.ganjithon.handeulpan.sheep.WoolColor;

/** @param wornScarf 지금 두른 목도리 색. 벗었으면 null */
public record WearScarfResponse(WoolColor wornScarf) {
}
