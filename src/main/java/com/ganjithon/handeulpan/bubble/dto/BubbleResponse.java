package com.ganjithon.handeulpan.bubble.dto;

/**
 * @param posted  실제로 게시됐는지. 위험 키워드가 있고 confirm=false면 false
 * @param flagged 위험 키워드가 들어 있었는지
 */
public record BubbleResponse(boolean posted, boolean flagged) {
}
