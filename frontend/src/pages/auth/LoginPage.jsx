import PagePlaceholder from '../PagePlaceholder'

function LoginPage() {
  return <PagePlaceholder title="로그인" owner="창민" description="로그인 성공 후 Access Token을 저장하고 권한별 메뉴를 표시합니다." api="POST /api/auth/login" />
}

export default LoginPage
