package com.ganjithon.handeulpan.scarf.dto;

/** @param scarfId 두를 목도리. null이면(생략 포함) 벗습니다. */
public record WearScarfRequest(Long scarfId) {
}
