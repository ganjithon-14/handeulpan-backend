package com.ganjithon.handeulpan.sheep;

/** 들판 가운데에서 얼마나 멀어졌는지. 기획서 6장 상태 규칙. */
public enum Distance {
    /** 들판 가운데 — 최근에 쓰다듬음 */
    CENTER,
    /** 산책 중 — 가운데에서 조금 멀어짐 */
    WALK,
    /** 들판 가장자리 — 친구들 화면에 배너 */
    EDGE
}
