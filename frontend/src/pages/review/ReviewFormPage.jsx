import { useState } from 'react'
import { Link, useLocation, useNavigate, useParams, useSearchParams } from 'react-router-dom'
import { createReview, updateReview } from '../../api/reviewApi'
import { getApiErrorMessage } from '../../api/apiError'

function ReviewFormPage() {
  const { reviewId } = useParams()
  const location = useLocation()
  const navigate = useNavigate()
  const [searchParams] = useSearchParams()
  const isEdit = Boolean(reviewId)

  const review = location.state?.review
  const reservationId = searchParams.get('reservationId')
  const programId = isEdit ? review?.programId : searchParams.get('programId')
  const programTitle = isEdit ? review?.programTitle : searchParams.get('programTitle')

  const [rating, setRating] = useState(review?.rating ?? 5)
  const [content, setContent] = useState(review?.content ?? '')
  const [submitting, setSubmitting] = useState(false)
  const [error, setError] = useState('')

  if (isEdit && !review) {
    return (
      <section className="page-card">
        <h1>리뷰 정보를 찾을 수 없습니다.</h1>
        <p>목록에서 다시 수정 버튼을 눌러주세요.</p>
        {programId
          ? <Link to={`/programs/${programId}/reviews`}>리뷰 목록으로 돌아가기</Link>
          : <Link to="/programs">프로그램 목록으로 돌아가기</Link>}
      </section>
    )
  }

  if (!isEdit && !reservationId) {
    return (
      <section className="page-card">
        <h1>작성할 예약을 찾을 수 없습니다.</h1>
        <p>참여 완료(출석)한 예약에서 리뷰 작성 버튼으로 접근해주세요.</p>
        <Link to="/reservations/me">내 예약으로 이동</Link>
      </section>
    )
  }

  const submit = async (event) => {
    event.preventDefault()
    setSubmitting(true)
    setError('')
    try {
      if (isEdit) {
        await updateReview(reviewId, { rating, content })
      } else {
        await createReview(reservationId, { rating, content })
      }
      navigate(programId ? `/programs/${programId}/reviews` : '/reservations/me')
    } catch (requestError) {
      setError(getApiErrorMessage(requestError, '리뷰를 저장하지 못했습니다.'))
    } finally {
      setSubmitting(false)
    }
  }

  return (
    <section className="card form-card">
      <div className="section-heading">
        <div>
          <h1>{isEdit ? '리뷰 수정' : '리뷰 작성'}</h1>
          <p>{programTitle ? `${programTitle} 프로그램에 대한 리뷰입니다.` : '참여한 프로그램에 대한 솔직한 후기를 남겨주세요.'}</p>
        </div>
      </div>
      <form className="form-grid" onSubmit={submit}>
        <label>평점
          <div className="rating-input">
            {[1, 2, 3, 4, 5].map((value) => (
              <button
                type="button"
                key={value}
                className={`star-button ${value <= rating ? 'active' : ''}`}
                onClick={() => setRating(value)}
                aria-label={`${value}점`}
              >★</button>
            ))}
          </div>
        </label>
        <label>내용
          <textarea required minLength={10} maxLength={500} rows="6" value={content} onChange={(event) => setContent(event.target.value)} placeholder="10자 이상 500자 이하" />
        </label>
        <div className="form-actions">
          <button className="button button-primary" type="submit" disabled={submitting}>{isEdit ? '수정 저장' : '등록하기'}</button>
        </div>
      </form>
      {error && <p className="notice notice-error">{error}</p>}
    </section>
  )
}

export default ReviewFormPage
