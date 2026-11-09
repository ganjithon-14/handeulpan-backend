# 데모 모드 API (D, BE-A)

시연에서 "며칠 지나면 멀어진다"를 몇 초 만에 보여주기 위한 API입니다.

## POST /api/demo/sheep/{sheepId}/rewind — 시간 되감기

헤더 `X-Demo-Key` 필요 (기기 토큰은 필요 없음). 그 양의 마지막 쓰다듬은 시각을 `days`일 과거로 옮깁니다.

요청
```json
{ "days": 4 }
```
`days`: 1~30

응답 `200`
```json
{ "sheepId": 2, "distance": "EDGE" }
```

| HTTP | code | 언제 |
|---|---|---|
| 403 | `INVALID_DEMO_KEY` | 키가 없거나 틀림, 또는 서버에 데모 키가 설정되지 않음 |
| 404 | `SHEEP_NOT_FOUND` | 없는 양 |
| 400 | `VALIDATION_FAILED` | `days`가 1~30 밖 |

되돌리기: 그 양의 주인이 쓰다듬으면 바로 가운데로 돌아옵니다.

## 데모 키

| 환경 | 값 |
|---|---|
| local (`./gradlew bootRun`) | `local-demo` |
| 배포 서버 | 환경변수 `HANDEULPAN_DEMO_KEY` (비워 두면 데모 API 꺼짐) |
