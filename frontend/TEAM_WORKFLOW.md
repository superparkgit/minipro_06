# React 팀 작업 규칙

## 담당 범위

- 서희: `pages/program`, `pages/reservation`, `api/programApi.js`, `api/reservationApi.js`, `routes/programReservationRoutes.jsx`
- 용재: `pages/community`, `pages/review`, `api/postApi.js`, `api/commentApi.js`, `api/reviewApi.js`, `routes/communityRoutes.jsx`
- 창민: `pages/auth`, `pages/admin`, `api/authApi.js`, `api/adminUserApi.js`, `routes/authAdminRoutes.jsx`
- 공통 취합: `App.jsx`, `layouts/AppLayout.jsx`, `routes/appRoutes.jsx`, `api/apiClient.js`, `auth/authStorage.js`, 공통 CSS

## 작업 순서

1. 작업 전 최신 `main`을 받는다.
2. 각자 새 브랜치에서 본인 담당 파일만 수정한다.
3. 공통 파일 수정이 필요하면 임의로 바꾸지 말고 취합 담당자에게 요청한다.
4. PR 전 `npm run build`와 `npm run lint`를 실행한다.
5. PR을 하나씩 머지하고 다음 사람은 최신 `main`을 다시 받는다.

## 공통 인증 규칙

- Access Token: `accessToken`
- Refresh Token: `refreshToken`
- 로그인 사용자: `fitReserveUser`
- 역할: `ROLE_USER`, `ROLE_TRAINER`, `ROLE_ADMIN`

로그인 응답은 현재 토큰만 반환한다. 사용자 ID·이름·역할을 어떻게 받을지는 인증 담당자와 합의한 후 적용한다.
