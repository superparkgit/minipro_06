import PagePlaceholder from '../PagePlaceholder'

function SignupPage() {
  return <PagePlaceholder title="회원가입" owner="창민" description="이메일, 비밀번호, 이름을 입력해 일반 회원으로 가입합니다." api="POST /api/auth/signup" />
}

export default SignupPage
