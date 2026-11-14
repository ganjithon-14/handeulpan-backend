package com.ganjithon.handeulpan.sheep.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

/** 낮잠 일수. 기획서 9장: 최대 7일 */
public record NapRequest(@NotNull @Min(1) @Max(7) Integer days) {
}
