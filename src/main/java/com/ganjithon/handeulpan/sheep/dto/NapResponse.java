package com.ganjithon.handeulpan.sheep.dto;

import com.ganjithon.handeulpan.sheep.Distance;

public record NapResponse(Long sheepId, boolean napping, Distance distance) {
}
