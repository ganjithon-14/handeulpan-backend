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
