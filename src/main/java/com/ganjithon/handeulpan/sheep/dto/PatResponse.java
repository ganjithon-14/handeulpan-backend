package com.ganjithon.handeulpan.sheep.dto;

import com.ganjithon.handeulpan.sheep.Distance;

public record PatResponse(Long sheepId, Distance distance, boolean pattedToday) {
}
