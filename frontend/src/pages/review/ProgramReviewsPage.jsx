import { useEffect, useState } from 'react'
import { Link, useParams } from 'react-router-dom'
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

const formatDate = (value) => value
  ? new Intl.DateTimeFormat('ko-KR', { dateStyle: 'medium' }).format(new Date(value))
  : ''

const stars = (rating) => '★★★★★☆☆☆☆☆'.slice(5 - rating, 10 - rating)

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

  const [replyDrafts, setReplyDrafts] = useState({})
  const [reportDrafts, setReportDrafts] = useState({})
  const [openReplyId, setOpenReplyId] = useState(null)
  const [openReportId, setOpenReportId] = useState(null)

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

  const submitReply = async (review) => {
    const content = (replyDrafts[review.id] ?? '').trim()
    if (!content) return
    setError('')
    try {
      const action = review.reply ? updateReviewReply : createReviewReply
      const { data } = await action(review.id, { content })
      setReviews((items) => items.map((item) => item.id === review.id ? { ...item, reply: data } : item))
      setOpenReplyId(null)
    } catch (requestError) {
      setError(getApiErrorMessage(requestError, '답변을 저장하지 못했습니다.'))
    }
  }

  const submitReport = async (review) => {
    const reason = (reportDrafts[review.id] ?? '').trim()
    if (!reason) return
    setError('')
    try {
      await reportReview(review.id, { reason })
      setReviews((items) => items.map((item) => item.id === review.id
        ? { ...item, status: 'HIDDEN', content: '트레이너의 권리 침해 신고로 가려진 리뷰입니다.' }
        : item))
      setOpenReportId(null)
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
          <h2>{programRating ? `${programRating.averageRating} (${programRating.reviewCount}건)` : '-'}</h2>
        </div>
        {trainerRating && (
          <div className="card stat-card">
            <p className="muted">{trainerRating.name} 트레이너 평점</p>
            <h2>{trainerRating.averageRating} ({trainerRating.reviewCount}건)</h2>
          </div>
        )}
      </div>

      {loading && <p className="notice">리뷰를 불러오는 중입니다.</p>}
      {error && <p className="notice notice-error">{error}</p>}
      {!loading && !error && reviews.length === 0 && <p className="page-card">등록된 리뷰가 없습니다.</p>}

      <div className="review-list">
        {reviews.map((review) => {
          const isOwner = user?.id === review.userId && review.status !== 'HIDDEN'
          const isAssignedTrainer = hasRole(user, 'ROLE_TRAINER') && user?.id === review.trainerId && review.status !== 'HIDDEN'

          return (
          <article className="card review-card" key={review.id}>
            <div className="meta">
              <span className="star-rating">{stars(review.rating)}</span>
              <span>{review.userName}</span>
              <span>·</span><span>{formatDate(review.createdAt)}</span>
            </div>
            <p>{review.content}</p>

            {(isOwner || isAssignedTrainer) && (
              <div className="row-actions">
                {isOwner && (
                  <>
                    <Link className="button button-secondary" to={`/reviews/${review.id}/edit`} state={{ review }}>수정</Link>
                    <button className="button button-danger" onClick={() => removeReview(review.id)}>삭제</button>
                  </>
                )}
                {isAssignedTrainer && (
                  <>
                    <button className="button button-secondary" onClick={() => setOpenReplyId(openReplyId === review.id ? null : review.id)}>
                      {review.reply ? '답변 수정' : '답변 작성'}
                    </button>
                    <button className="button button-danger" onClick={() => setOpenReportId(openReportId === review.id ? null : review.id)}>삭제 요청</button>
                  </>
                )}
              </div>
            )}

            {review.reply && (
              <div className="review-reply">
                <strong>{review.reply.trainerName} 트레이너 답변</strong>
                <p>{review.reply.content}</p>
              </div>
            )}

            {openReplyId === review.id && (
              <div className="comment-edit">
                <textarea
                  rows="3"
                  maxLength={500}
                  placeholder="답변을 입력하세요"
                  defaultValue={review.reply?.content ?? ''}
                  onChange={(event) => setReplyDrafts((drafts) => ({ ...drafts, [review.id]: event.target.value }))}
                />
                <div className="row-actions">
                  <button className="button button-primary" onClick={() => submitReply(review)}>저장</button>
                  <button className="button button-secondary" onClick={() => setOpenReplyId(null)}>취소</button>
                </div>
              </div>
            )}

            {openReportId === review.id && (
              <div className="comment-edit">
                <textarea
                  rows="2"
                  maxLength={200}
                  placeholder="삭제 요청 사유를 입력하세요"
                  onChange={(event) => setReportDrafts((drafts) => ({ ...drafts, [review.id]: event.target.value }))}
                />
                <div className="row-actions">
                  <button className="button button-danger" onClick={() => submitReport(review)}>요청 제출</button>
                  <button className="button button-secondary" onClick={() => setOpenReportId(null)}>취소</button>
                </div>
              </div>
            )}
          </article>
          )
        })}
      </div>

      <Pagination page={page} totalPages={totalPages} onChange={setPage} />
    </>
  )
}

export default ProgramReviewsPage
