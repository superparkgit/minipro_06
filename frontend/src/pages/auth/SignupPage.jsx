import { useState } from 'react'
import { Link, useNavigate } from 'react-router-dom'
import { signup } from '../../api/authApi'
import { getApiErrorMessage } from '../../api/apiError'
import './AuthPages.css'

const INITIAL_FORM = {
  name: '',
  email: '',
  password: '',
  passwordConfirm: '',
}

const PASSWORD_MIN_LENGTH = 8
const PASSWORD_MAX_LENGTH = 64
const EMAIL_PATTERN = /^[^\s@]+@[^\s@]+$/

const getEmailHelpClassName = (email) => {
  if (!email) return 'auth-help'

  return `auth-help ${EMAIL_PATTERN.test(email) ? 'auth-help-valid' : 'auth-help-invalid'}`
}

const getPasswordHelpClassName = (password) => {
  if (!password) return 'auth-help'

  const isValidLength = password.length >= PASSWORD_MIN_LENGTH
    && password.length <= PASSWORD_MAX_LENGTH

  return `auth-help ${isValidLength ? 'auth-help-valid' : 'auth-help-invalid'}`
}

function SignupPage() {
  const navigate = useNavigate()
  const [form, setForm] = useState(INITIAL_FORM)
  const [errorMessage, setErrorMessage] = useState('')
  const [isSubmitting, setIsSubmitting] = useState(false)

  const handleChange = (event) => {
    const { name, value } = event.target
    setForm((current) => ({ ...current, [name]: value }))
    setErrorMessage('')
  }

  const handleSubmit = async (event) => {
    event.preventDefault()
    if (isSubmitting) return

    setErrorMessage('')

    if (form.password !== form.passwordConfirm) {
      setErrorMessage('비밀번호가 일치하지 않습니다.')
      return
    }

    setIsSubmitting(true)

    const payload = {
      name: form.name.trim(),
      email: form.email.trim(),
      password: form.password,
    }

    try {
      const { data } = await signup(payload)
      navigate('/login', {
        replace: true,
        state: {
          email: data?.email ?? payload.email,
          signupComplete: true,
        },
      })
    } catch (error) {
      setErrorMessage(
        getApiErrorMessage(
          error,
          '회원가입에 실패했습니다. 입력 내용을 확인하고 다시 시도해 주세요.',
        ),
      )
    } finally {
      setIsSubmitting(false)
    }
  }

  return (
    <section className="auth-page" aria-labelledby="signup-title">
      <div className="auth-shell auth-shell-signup">
        <aside className="auth-context" aria-label="회원가입 안내">
          <span className="auth-mark" aria-hidden="true">FR</span>
          <div>
            <p className="auth-kicker">START YOUR ROUTINE</p>
            <h2>오늘의 선택을<br />꾸준한 루틴으로.</h2>
            <p className="auth-context-copy">
              계정을 만들고 원하는 프로그램을 찾아 나만의 운동 일정을 시작해 보세요.
            </p>
          </div>
          <ul className="auth-benefits" aria-label="회원가입 후 이용 가능 기능">
            <li>프로그램 예약과 취소</li>
            <li>참여한 수업 리뷰 작성</li>
          </ul>
        </aside>

        <div className="auth-panel">
          <header className="auth-heading">
            <p className="auth-eyebrow">새 계정 만들기</p>
            <h1 id="signup-title">운동 루틴을 시작하세요.</h1>
            <p>필수 정보만 입력하면 바로 가입할 수 있습니다.</p>
          </header>

          {errorMessage && (
            <p className="auth-message auth-message-error" role="alert">
              {errorMessage}
            </p>
          )}

          <form className="auth-form" onSubmit={handleSubmit}>
            <label htmlFor="signup-name">
              이름
              <input
                id="signup-name"
                name="name"
                type="text"
                value={form.name}
                onChange={handleChange}
                placeholder="이름 입력"
                autoComplete="name"
                maxLength={30}
                required
                disabled={isSubmitting}
              />
            </label>

            <label htmlFor="signup-email">
              이메일
              <input
                id="signup-email"
                name="email"
                type="email"
                value={form.email}
                onChange={handleChange}
                placeholder="name@example.com"
                autoComplete="username"
                inputMode="email"
                aria-describedby="signup-email-help"
                required
                disabled={isSubmitting}
              />
              <span
                className={getEmailHelpClassName(form.email)}
                id="signup-email-help"
                aria-live="polite"
              >
                올바른 이메일 형식으로 입력해 주세요.
              </span>
            </label>

            <label htmlFor="signup-password">
              비밀번호
              <input
                id="signup-password"
                name="password"
                type="password"
                value={form.password}
                onChange={handleChange}
                placeholder="8자 이상 입력"
                autoComplete="new-password"
                minLength={PASSWORD_MIN_LENGTH}
                maxLength={PASSWORD_MAX_LENGTH}
                aria-describedby="signup-password-help"
                required
                disabled={isSubmitting}
              />
              <span
                className={getPasswordHelpClassName(form.password)}
                id="signup-password-help"
                aria-live="polite"
              >
                8자 이상 64자 이하로 입력해 주세요.
              </span>
            </label>

            <label htmlFor="signup-password-confirm">
              비밀번호 확인
              <input
                id="signup-password-confirm"
                name="passwordConfirm"
                type="password"
                value={form.passwordConfirm}
                onChange={handleChange}
                placeholder="비밀번호 다시 입력"
                autoComplete="new-password"
                minLength={PASSWORD_MIN_LENGTH}
                maxLength={PASSWORD_MAX_LENGTH}
                aria-describedby="signup-password-confirm-help"
                required
                disabled={isSubmitting}
              />
              <span
                className={getPasswordHelpClassName(form.passwordConfirm)}
                id="signup-password-confirm-help"
                aria-live="polite"
              >
                8자 이상 64자 이하로 입력해 주세요.
              </span>
            </label>

            <button
              className="button button-primary auth-submit"
              type="submit"
              disabled={isSubmitting}
            >
              {isSubmitting ? '가입 중…' : '회원가입'}
            </button>
          </form>

          <p className="auth-switch">
            이미 계정이 있나요? <Link to="/login">로그인</Link>
          </p>
        </div>
      </div>
    </section>
  )
}

export default SignupPage
