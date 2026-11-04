# API 명세 (요청·응답 예시)

11/6 명세 확정 관문까지 여기에 엔드포인트별 예시를 고정합니다. 프론트는 이 JSON으로 목업을 만들어 먼저 개발합니다.

## 공통

- Base URL: `TBD`
- 인증 헤더: `X-Device-Token: {UUID}` (이름은 확정 시 수정)
- 공통 응답·에러 형식: BE-B가 확정 후 작성

## 엔드포인트 목록 (초안)

| 기능 | 메서드 | 경로 | 담당 | 상태 |
|---|---|---|---|---|
| F1 들판 만들기 | POST | `/api/fields` | BE-A | 초안 |
| F1 들판 미리보기 (링크 구경) | GET | `/api/fields/{fieldId}/preview` | BE-A | 초안 |
| F1 들판 합류 (양 만들기·동의) | POST | `/api/fields/{fieldId}/sheep` | BE-A | 초안 |
| 들판 조회 (화면 전체) | GET | `/api/fields/{fieldId}` | BE-A | 초안 |
| F2 쓰다듬기 | POST | `/api/fields/{fieldId}/pat` | BE-A | 초안 |
| F4 말풍선 쓰기 (위험 키워드 검사) | POST | `/api/fields/{fieldId}/bubble` | BE-B | 초안 |
| F7 목도리 보내기 | POST | `/api/fields/{fieldId}/sheep/{sheepId}/scarves` | BE-B | 초안 |
| F7 받은 목도리 목록·두르기 | GET / PATCH | `/api/me/scarves` | BE-B | 초안 |
| D 데모 되감기 | POST | `/api/demo/fields/{fieldId}/sheep/{sheepId}/rewind` | BE-A | 초안 |

경로는 예시입니다. 확정되면 아래처럼 파일을 하나씩 추가해 주세요.

```
docs/api/
  fields.md     # F1, 들판 조회
  pat.md        # F2
  bubble.md     # F4
  scarf.md      # F7
  demo.md       # D
```
