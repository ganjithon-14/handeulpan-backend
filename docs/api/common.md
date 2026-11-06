# 공통 규칙

> 기준: 개발 명세(MVP) 4장. 경로·용어는 명세(`pasture`, `/api/sheep/...`)를 따릅니다.
> 레포 초안(`field`, `/api/fields/...`)과 다른 부분은 [용어·경로 불일치](#용어경로-불일치-확인-필요)에 모아 두었습니다.

## 인증

- 모든 요청에 헤더 `X-Device-Token: {UUID}`를 보냅니다.
- 토큰이 없으면 `POST /api/users`로 발급받아 브라우저에 저장합니다. (BE-A 담당)
- 토큰이 곧 사용자입니다. 로그인, 비밀번호, JWT는 쓰지 않습니다.
- 헤더가 없거나 등록되지 않은 토큰이면 `401 INVALID_DEVICE_TOKEN`.

## 응답 형식

- 성공: 본문은 엔드포인트별 JSON 그대로입니다. 별도 래퍼(`data` 등)로 감싸지 않습니다. 본문이 없으면 `204`.
- 시각은 서버 시간(Asia/Seoul)을 기준으로 계산하고, 응답에는 마지막 쓰다듬은 시각을 넣지 않습니다. (사생활 보호)

## 에러 형식

```json
{ "code": "PASTURE_FULL", "message": "이 들판은 가득 찼어요." }
```

- `code`: 프론트가 분기에 쓰는 고정 문자열 (대문자 스네이크)
- `message`: 사람이 읽는 문장. 프론트는 그대로 보여주지 않아도 됩니다.
- 검증 오류도 같은 형식을 씁니다.

## 에러 코드

| HTTP | code | 언제 | 담당 |
|---|---|---|---|
| 401 | `INVALID_DEVICE_TOKEN` | 토큰이 없거나 모르는 값 | 공통 |
| 400 | `VALIDATION_FAILED` | 요청 형식이 잘못됨 (필수값 누락, 길이 초과 등) | 공통 |
| 500 | `INTERNAL_ERROR` | 예상하지 못한 서버 오류. 내부 메시지는 노출하지 않음 | 공통 |
| 404 | `PASTURE_NOT_FOUND` | 없는 들판 | BE-A |
| 404 | `SHEEP_NOT_FOUND` | 없는 양 | 공통 |
| 403 | `NOT_PASTURE_MEMBER` | 그 들판의 멤버가 아님 | 공통 |
| 403 | `NOT_MY_SHEEP` | 내 양이 아닌데 내 양만 가능한 API를 호출 | 공통 |
| 409 | `PASTURE_FULL` | 들판이 6마리로 가득 참 | BE-A |
| 409 | `ALREADY_JOINED` | 이미 이 들판에 합류함 | BE-A |
| 400 | `BUBBLE_TEXT_INVALID` | 말풍선이 비었거나 20자 초과 | BE-B |
| 404 | `BUBBLE_NOT_FOUND` | 지울 말풍선이 없음 | BE-B |
| 409 | `ALREADY_SENT_TODAY` | 같은 친구에게 오늘 이미 목도리를 보냄 | BE-B |
| 400 | `SELF` | 자기 자신에게 목도리를 보내려 함 | BE-B |
| 404 | `SCARF_NOT_FOUND` | 내가 받은 목도리가 아님 | BE-B |
| 403 | `INVALID_DEMO_KEY` | 데모 API의 `X-Demo-Key`가 틀림 | BE-A |

`PASTURE_FULL`, `ALREADY_JOINED`, `ALREADY_SENT_TODAY`, `SELF`는 명세에 있는 값이고, 나머지는 BE-B가 제안한 이름입니다.

## 용어·경로 불일치 (확인 필요)

| 항목 | 개발 명세 | 레포 초안 / 용어표 | 이 문서 |
|---|---|---|---|
| 들판 이름 | `pasture` | `field` | 명세 |
| 거리 단계 | `CENTER` / `WALK` / `EDGE` | `CENTER` / `WANDERING` / `EDGE` | 명세 |
| 말풍선 | `PUT /api/sheep/{id}/bubble` | `POST /api/fields/{fieldId}/bubble` | 명세 |
| 목도리 보내기 | `POST /api/sheep/{id}/scarves` | `POST /api/fields/{fieldId}/sheep/{sheepId}/scarves` | 명세 |
| 받은 목도리 목록 | 별도 API 없음 (들판 조회의 `myScarves`) | `GET /api/me/scarves` | 명세 |

팀이 한쪽으로 정하면 이 표와 각 문서를 함께 고칩니다.
