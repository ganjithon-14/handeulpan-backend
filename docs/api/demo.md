# 데모 모드 API (D)

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

## POST /api/demo/seed — 시연 데이터 만들기·초기화 (BE-B)

헤더 `X-Demo-Key` 필요 (기기 토큰은 필요 없음). 데모 계정과 시연용 들판을 만듭니다. **이미 있으면 지우고 같은 상태로 다시 만들기** 때문에 몇 번을 불러도 결과가 같습니다. 시연 직전에 한 번 부르면 됩니다.

응답 `201`
```json
{
  "pastureId": "k3x9...",
  "password": "demo1234!",
  "members": [
    { "email": "doobu@demo.test", "sheepName": "두부", "sheepId": 1, "distance": "CENTER", "napping": false, "me": true },
    { "email": "kong@demo.test",  "sheepName": "콩이", "sheepId": 2, "distance": "CENTER", "napping": false, "me": false }
  ]
}
```

모든 데모 계정의 비밀번호는 `password` 값 하나입니다. 로그인은 평소처럼 `POST /api/auth/login`으로 합니다. (`me: true`가 시연 시작 계정)

| 양 | 이메일 | 털 색 | 상태 |
|---|---|---|---|
| 두부 (나) | `doobu@demo.test` | CREAM | 가운데. 친구 셋에게 목도리를 받았고 콩이(MINT)의 것을 두름 |
| 콩이 | `kong@demo.test` | MINT | 가운데. 말풍선 "시험 끝났다!". **되감기로 멀어지는 모습을 보여 줄 양** |
| 보리 | `bori@demo.test` | YELLOW | 산책 중 (3일 전에 쓰다듬음) |
| 별이 | `byeol@demo.test` | LAVENDER | 낮잠 중 (3일) |
| 구름 | `cloud@demo.test` | SKY | 가운데. 말풍선 "오늘도 맑음" |

- 양은 5마리라 **한 자리가 비어 있습니다.** 링크(`/p/{pastureId}`)로 합류하는 장면을 보여 줄 수 있습니다.
- 두부가 아직 아무에게도 목도리를 보내지 않은 상태라, 시연에서 바로 보낼 수 있습니다.
- 이름·털 색·말풍선·시나리오를 바꾸려면 `DemoSeedService`를 고칩니다.

| HTTP | code | 언제 |
|---|---|---|
| 403 | `INVALID_DEMO_KEY` | 키가 없거나 틀림, 또는 서버에 데모 키가 설정되지 않음 |

시연 흐름 예: 두부로 로그인 → 홈에서 들판 확인 → 콩이를 4일 되감기(`/rewind` `{"days":4}`) → 두부 홈에 "콩이 양이 조금 멀리 갔어요" 배너 → 두부가 콩이를 쓰다듬고 목도리를 보냄.

## DELETE /api/demo/seed — 시연 데이터 지우기

헤더 `X-Demo-Key` 필요. 응답 `204`. 데모 계정(이메일이 `@demo.test`로 끝나는 계정)과 그 들판, 거기에 있던 양·말풍선·목도리를 모두 지웁니다. 데모 데이터가 없어도 `204`입니다. 일반 사용자 계정과 다른 들판은 건드리지 않습니다.

## 데모 키

| 환경 | 값 |
|---|---|
| local (`./gradlew bootRun`) | `local-demo` |
| 배포 서버 | 환경변수 `HANDEULPAN_DEMO_KEY` (비워 두면 데모 API 꺼짐) |
