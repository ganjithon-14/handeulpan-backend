package com.ganjithon.handeulpan.pasture.dto;

import com.ganjithon.handeulpan.sheep.Sheep;
import com.ganjithon.handeulpan.sheep.WoolColor;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/** 링크로 들어와 "내 양 풀어놓기". agreed=true("멀어지면 들판 친구들이 알 수 있어요" 동의)가 아니면 합류 불가. */
public record JoinPastureRequest(
        @NotBlank @Size(max = Sheep.NAME_MAX_LENGTH) String sheepName,
        @NotNull WoolColor woolColor,
        @AssertTrue boolean agreed) {
}
