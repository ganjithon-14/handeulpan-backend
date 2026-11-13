package com.ganjithon.handeulpan.pasture.dto;

import com.ganjithon.handeulpan.pasture.Pasture;
import com.ganjithon.handeulpan.sheep.WoolColor;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/** 들판 만들기 = 들판 생성 + 만든 사람의 양 풀어놓기. 양 이름은 가입한 닉네임입니다. */
public record CreatePastureRequest(
        @Size(max = Pasture.NAME_MAX_LENGTH) String name,
        @NotNull WoolColor woolColor,
        @AssertTrue boolean agreed) {
}
