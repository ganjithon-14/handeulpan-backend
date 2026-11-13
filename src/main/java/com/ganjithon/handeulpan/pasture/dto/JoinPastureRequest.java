package com.ganjithon.handeulpan.pasture.dto;

import com.ganjithon.handeulpan.sheep.WoolColor;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotNull;

/**
 * 링크로 들어와 "내 양 풀어놓기". 양 이름은 가입한 닉네임입니다.
 * agreed=true("멀어지면 들판 친구들이 알 수 있어요" 동의)가 아니면 합류 불가.
 */
public record JoinPastureRequest(
        @NotNull WoolColor woolColor,
        @AssertTrue boolean agreed) {
}
