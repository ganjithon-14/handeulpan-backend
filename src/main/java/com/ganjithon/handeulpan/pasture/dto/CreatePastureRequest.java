package com.ganjithon.handeulpan.pasture.dto;

import com.ganjithon.handeulpan.pasture.Pasture;
import com.ganjithon.handeulpan.sheep.Sheep;
import com.ganjithon.handeulpan.sheep.WoolColor;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/** 들판 만들기 = 들판 생성 + 만든 사람의 양 풀어놓기. */
public record CreatePastureRequest(
        @Size(max = Pasture.NAME_MAX_LENGTH) String name,
        @NotBlank @Size(max = Sheep.NAME_MAX_LENGTH) String sheepName,
        @NotNull WoolColor woolColor,
        @AssertTrue boolean agreed) {
}
