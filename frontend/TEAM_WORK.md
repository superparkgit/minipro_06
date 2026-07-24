# React 팀 작업 경계

## 공통 파일

다음 파일은 충돌이 잘 나므로 한 명만 수정합니다.

- `src/App.jsx`: 라우트
- `src/layouts/AppLayout.jsx`: 전체 메뉴
- `src/api/apiClient.js`: JWT와 공통 오류 처리
- `src/App.css`, `src/index.css`: 공통 스타일

## 담당별 작업 폴더

| 담당 | 화면 | API |
| --- | --- | --- |
| 창민 | `src/pages/auth/` | `src/api/authApi.js` |
| 서희 | `src/pages/program/`, `src/pages/reservation/` | `programApi.js`, `reservationApi.js` |
| 용재 | `src/pages/community/`, `src/pages/review/` | `postApi.js`, `commentApi.js`, `reviewApi.js` |
| 관리자 담당 | `src/pages/admin/` | `adminUserApi.js` |

## Git 순서

1. 자신의 `feature/<domain>-frontend` 브랜치를 만듭니다.
2. 담당 폴더와 API 모듈만 수정합니다.
3. `npm run lint` 및 `npm run build`를 실행합니다.
4. 자신의 브랜치를 push하고 PR을 엽니다.

API를 아직 머지하지 않았더라도 이 프론트의 함수 이름과 인자를 기준으로 화면을 작성할 수 있습니다.
