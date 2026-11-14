# 쓰다듬기 · 거리 규칙 API (F2·F3, BE-A)

## POST /api/sheep/{sheepId}/pat — 쓰다듬기 (체크인)

토큰 필요, **내 양만**. 어느 단계에서든 바로 가운데로 돌아옵니다. 요청 본문 없음.

응답 `200`
```json
{ "sheepId": 1, "distance": "CENTER", "pattedToday": true }
```

| HTTP | code | 언제 |
|---|---|---|
| 403 | `NOT_MY_SHEEP` | 내 양이 아님 |
| 404 | `SHEEP_NOT_FOUND` | 없는 양 |

## 거리 규칙 (F3)

양의 거리는 저장하지 않고, 요청마다 **마지막 쓰다듬은 시각**으로 계산합니다.

| 마지막 쓰다듬기 후 | distance | 화면 |
|---|---|---|
| 2일 미만 | `CENTER` | 들판 가운데 |
| 2일 이상 | `WALK` | 산책 중 (조금 멀어짐) |
| 4일 이상 | `EDGE` | 들판 가장자리, 친구들에게 배너 |

- 합류한 순간이 첫 쓰다듬기입니다.
- 기준값은 초안입니다. 팀 테스트 후 `application.yml`에서 바꿉니다.
  ```yaml
  handeulpan:
    distance:
      walk-after: 2d
      edge-after: 4d
  ```

## PUT /api/sheep/{sheepId}/nap — 낮잠 모드 (P1)

시험·여행·혼자 있고 싶을 때 "낮잠 자는 중"으로 둡니다. 토큰 필요, **내 양만**.

요청
```json
{ "days": 3 }
```
`days`: 1~7 (기획서 9장: 최대 7일). 이미 낮잠 중이면 **지금부터** `days`일로 다시 정합니다.

응답 `200`
```json
{ "sheepId": 2, "napping": true, "distance": "CENTER" }
```

- 낮잠 중에는 며칠이 지나도 `distance`가 `CENTER`이고, 친구 화면의 가장자리 배너(`edgeFriendNames`)에도 뜨지 않습니다.
- 낮잠이 **끝난 시점부터** 거리 계산을 다시 시작합니다. (예: 3일 낮잠 → 끝나고 2일 뒤 `WALK`, 4일 뒤 `EDGE`)
- 낮잠 중에 쓰다듬어도 낮잠은 유지됩니다.
- 응답과 들판 조회에는 `napping`(true/false)만 나가고, **끝나는 날짜·시각은 내보내지 않습니다.** (감시 방지)

| HTTP | code | 언제 |
|---|---|---|
| 400 | `VALIDATION_FAILED` | `days`가 없거나 1~7 밖 |
| 403 | `NOT_MY_SHEEP` | 내 양이 아님 |
| 404 | `SHEEP_NOT_FOUND` | 없는 양 / 들판을 나간 양 |

## DELETE /api/sheep/{sheepId}/nap — 낮잠 깨우기

내 양만. 응답 `204`. 깨운 시점부터 거리 계산을 다시 시작합니다. 낮잠 중이 아니어도 에러 없이 `204`이고 아무것도 바뀌지 않습니다.

## DELETE /api/sheep/{sheepId} — 들판 나가기 (P1)

내 양만. 언제든 한 번에 나갈 수 있습니다. 응답 `204`.

- 나간 양은 들판 조회·미리보기·내 들판 목록과 6마리 계산에서 빠지고, 그 양에 대한 모든 API가 `404 SHEEP_NOT_FOUND`가 됩니다. (쓰다듬기, 말풍선, 목도리 받기 포함)
- 나간 사람이 그 들판을 조회하면 `403 NOT_PASTURE_MEMBER`.
- 같은 링크로 **다시 합류**하면(`POST /api/pastures/{id}/sheep`) 같은 `sheepId`로 돌아오고, 이름·털 색을 새로 받아 가운데에서 시작합니다. 낮잠은 해제됩니다. 합류에는 동의를 다시 받습니다.
- 서버에는 양 행이 남아 있습니다. 말풍선·목도리 기록이 이 양을 참조하기 때문에 지우지 않고 "나감"으로 표시합니다. 다시 합류하면 24시간 안의 말풍선은 다시 보일 수 있습니다.

| HTTP | code | 언제 |
|---|---|---|
| 403 | `NOT_MY_SHEEP` | 내 양이 아님 |
| 404 | `SHEEP_NOT_FOUND` | 없는 양 / 이미 나간 양 |
