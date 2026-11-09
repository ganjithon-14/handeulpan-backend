package com.ganjithon.handeulpan.pasture.dto;

/** pastureId는 링크(/p/{pastureId})에 쓰는 공개 ID입니다. */
public record PastureCreatedResponse(String pastureId, Long sheepId) {
}
