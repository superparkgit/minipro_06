import { useEffect, useState } from 'react'
import { useParams } from 'react-router-dom'
import {
  createReviewReply,
  deleteReview,
  getProgramRating,
  getProgramReviews,
  getTrainerRating,
  reportReview,
  updateReviewReply,
} from '../../api/reviewApi'
import { getApiErrorMessage } from '../../api/apiError'
import { hasRole, useCurrentUser } from '../../hooks/useCurrentUser'
import Pagination from '../../components/Pagination'
import ReviewCard, { stars } from './components/ReviewCard'

const formatRating = (value) => Number(value).toFixed(1)

function ProgramReviewsPage() {
  const { programId } = useParams()
  const { user } = useCurrentUser()
  const [programRating, setProgramRating] = useState(null)
  const [trainerRating, setTrainerRating] = useState(null)
  const [reviews, setReviews] = useState([])
  const [page, setPage] = useState(0)
  const [totalPages, setTotalPages] = useState(0)
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState('')

  useEffect(() => {
    getProgramRating(programId)
      .then(({ data }) => setProgramRating(data))
      .catch(() => {})
  }, [programId])

  useEffect(() => {
    setLoading(true)
    setError('')
    getProgramReviews(programId, { page })
      .then(({ data }) => {
        setReviews(data.content)
        setTotalPages(data.totalPages)
        const trainer = data.content[0]
        if (trainer) {
          getTrainerRating(trainer.trainerId).then(({ data: rating }) => setTrainerRating({ ...rating, name: trainer.trainerName }))
        }
      })
      .catch((requestError) => setError(getApiErrorMessage(requestError, '리뷰 목록을 불러오지 못했습니다.')))
      .finally(() => setLoading(false))
  }, [programId, page])

  const removeReview = async (reviewId) => {
    if (!window.confirm('리뷰를 삭제할까요?')) return
    setError('')
    try {
      await deleteReview(reviewId)
      setReviews((items) => items.filter((item) => item.id !== reviewId))
    } catch (requestError) {
      setError(getApiErrorMessage(requestError, '리뷰를 삭제하지 못했습니다.'))
    }
  }

  const submitReply = async (review, content) => {
    setError('')
    try {
      const action = review.reply ? updateReviewReply : createReviewReply
      const { data } = await action(review.id, { content })
      setReviews((items) => items.map((item) => item.id === review.id ? { ...item, reply: data } : item))
    } catch (requestError) {
      setError(getApiErrorMessage(requestError, '답변을 저장하지 못했습니다.'))
    }
  }

  const submitReport = async (review, reason) => {
    setError('')
    try {
      await reportReview(review.id, { reason })
      setReviews((items) => items.map((item) => item.id === review.id
        ? { ...item, status: 'HIDDEN', content: '트레이너의 권리 침해 신고로 가려진 리뷰입니다.' }
        : item))
    } catch (requestError) {
      setError(getApiErrorMessage(requestError, '삭제 요청을 등록하지 못했습니다.'))
    }
  }

  return (
    <>
      <section className="section-heading">
        <div>
          <h1>프로그램 리뷰</h1>
          <p>참여자들이 남긴 후기와 트레이너 답변을 확인하세요.</p>
        </div>
      </section>

      <div className="grid rating-summary-grid">
        <div className="card stat-card">
          <p className="muted">프로그램 평점</p>
          {programRating ? (
            <div className="rating-line">
              <span className="star-rating">{stars(Math.round(programRating.averageRating))}</span>
              <h2>{formatRating(programRating.averageRating)} ({programRating.reviewCount}건)</h2>
            </div>
          ) : <h2>-</h2>}
        </div>
        {trainerRating && (
          <div className="card stat-card">
            <p className="muted">{trainerRating.name} 트레이너 평점</p>
            <div className="rating-line">
              <span className="star-rating">{stars(Math.round(trainerRating.averageRating))}</span>
              <h2>{formatRating(trainerRating.averageRating)} ({trainerRating.reviewCount}건)</h2>
            </div>
          </div>
        )}
      </div>

      {loading && <p className="notice">리뷰를 불러오는 중입니다.</p>}
      {error && <p className="notice notice-error">{error}</p>}
      {!loading && !error && reviews.length === 0 && <p className="page-card">등록된 리뷰가 없습니다.</p>}

      <div className="review-list">
        {reviews.map((review) => (
          <ReviewCard
            key={review.id}
            review={review}
            isOwner={user?.id === review.userId && review.status !== 'HIDDEN'}
            isAssignedTrainer={hasRole(user, 'ROLE_TRAINER') && user?.id === review.trainerId && review.status !== 'HIDDEN'}
            onDelete={removeReview}
            onSubmitReply={submitReply}
            onSubmitReport={submitReport}
          />
        ))}
      </div>

      <Pagination page={page} totalPages={totalPages} onChange={setPage} />
    </>
  )
}

export default ProgramReviewsPage
