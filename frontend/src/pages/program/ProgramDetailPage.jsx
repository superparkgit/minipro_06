import { useEffect, useState } from 'react'
import { Link, useParams } from 'react-router-dom'
import { getProgram } from '../../api/programApi'
import { createReservation } from '../../api/reservationApi'
import { getApiErrorMessage } from '../../api/apiError'

const formatDateTime = (value) => value
  ? new Intl.DateTimeFormat('ko-KR', { dateStyle: 'long', timeStyle: 'short' }).format(new Date(value))
  : '일정 미정'

function ProgramDetailPage() {
  const { programId } = useParams()
  const [program, setProgram] = useState(null)
  const [loading, setLoading] = useState(true)
  const [reserved, setReserved] = useState(false)
  const [submitting, setSubmitting] = useState(false)
  const [error, setError] = useState('')

  useEffect(() => {
    getProgram(programId)
      .then(({ data }) => setProgram(data))
      .catch((requestError) => setError(getApiErrorMessage(requestError, '프로그램을 불러오지 못했습니다.')))
      .finally(() => setLoading(false))
  }, [programId])

  const reserve = async () => {
    setSubmitting(true)
    setError('')
    try {
      await createReservation({ programId: Number(programId) })
      setReserved(true)
    } catch (requestError) {
      setError(getApiErrorMessage(requestError, '예약을 신청하지 못했습니다.'))
    } finally {
      setSubmitting(false)
    }
  }

  if (loading) return <p className="notice">프로그램을 불러오는 중입니다.</p>
  if (!program) return <section className="page-card"><h1>프로그램을 찾을 수 없습니다.</h1><p>{error}</p><Link to="/programs">목록으로 돌아가기</Link></section>

  const mainTrainer = program.trainers?.find((trainer) => trainer.assignmentRole === 'MAIN')
  const reservable = program.status === 'OPEN'

  return (
    <section className="detail-layout">
      <article className="card detail-card">
        <div className="program-icon">{program.type === 'PT' ? '🏋️' : '🧘'}</div>
        <span className="badge">{program.type}</span>
        <h1>{program.title}</h1>
        <p className="muted">{program.description}</p>
        <hr />
        <p><strong>담당 트레이너</strong><br />{mainTrainer?.name ?? '미정'}</p>
        <p><strong>수업 일정</strong><br />{formatDateTime(program.startAt)} ~ {formatDateTime(program.endAt)}</p>
      </article>
      <aside className="card booking-card">
        <h2>예약 신청</h2>
        <dl>
          <dt>정원</dt><dd>{program.capacity}명</dd>
          <dt>상태</dt><dd>{program.status}</dd>
        </dl>
        <div className="row-actions">
          <button className="button button-primary" disabled={reserved || submitting || !reservable} onClick={reserve}>
            {reserved ? '예약 신청 완료' : submitting ? '신청 중...' : reservable ? '예약 신청하기' : '예약 불가'}
          </button>
          <Link className="button button-secondary" to={`/programs/${program.id}/manage`}>트레이너 관리</Link>
        </div>
        {reserved && <p className="notice">예약이 신청되었습니다. 트레이너 승인을 기다려 주세요.</p>}
        {error && <p className="notice notice-error">{error}</p>}
      </aside>
    </section>
  )
}

export default ProgramDetailPage
