import { useEffect, useState } from 'react'
import { Link, useParams } from 'react-router-dom'
import { getProgram } from '../../api/programApi'
import { createReservation } from '../../api/reservationApi'
import { getApiErrorMessage } from '../../api/apiError'
import { hasRole, useCurrentUser } from '../../hooks/useCurrentUser'
import { getProgramById } from './programData'

const formatDateTime = (value) => value
  ? new Intl.DateTimeFormat('ko-KR', { dateStyle: 'long', timeStyle: 'short' }).format(new Date(value))
  : '일정 미정'

function ProgramDetailPage() {
  const { programId } = useParams()
  const { user } = useCurrentUser()
  const [program, setProgram] = useState(null)
  const [loading, setLoading] = useState(true)
  const [reserved, setReserved] = useState(false)
  const [submitting, setSubmitting] = useState(false)
  const [error, setError] = useState('')

  useEffect(() => {
    getProgram(programId)
      .then(({ data }) => setProgram(data))
      .catch((requestError) => {
        if (localStorage.getItem('accessToken') === 'demo-access-token') {
          const demo = getProgramById(programId)
          if (demo) setProgram({ ...demo, status: 'OPEN', startAt: new Date(Date.now()+86400000).toISOString(), endAt: new Date(Date.now()+90000000).toISOString(), trainers: [{ id: demo.trainerId, name: demo.trainer, assignmentRole: 'MAIN' }] })
          else setError('프로그램을 찾을 수 없습니다.')
        } else {
          setError(getApiErrorMessage(requestError, '프로그램을 불러오지 못했습니다.'))
        }
      })
      .finally(() => setLoading(false))
  }, [programId])

  const reserve = async () => {
    setSubmitting(true)
    setError('')
    try {
      if (localStorage.getItem('accessToken') === 'demo-access-token') {
        const saved = JSON.parse(localStorage.getItem('demoReservations') ?? '[]')
        if (!saved.some((item) => item.programId === Number(programId))) {
          saved.unshift({
            id: Date.now(),
            programId: Number(programId),
            programName: program.title,
            userId: user.id,
            userName: user.name,
            status: 'PENDING',
            attendanceStatus: 'NOT_CHECKED',
          })
          localStorage.setItem('demoReservations', JSON.stringify(saved))
        }
      } else {
        await createReservation({ programId: Number(programId) })
      }
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
  const isTrainer = hasRole(user, 'ROLE_TRAINER')
  const isMainTrainer = isTrainer && mainTrainer?.id === user?.id

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
        <Link className="button button-secondary" to={`/programs/${program.id}/reviews`}>리뷰 보기</Link>
      </article>
      <aside className="card booking-card">
        <h2>{isTrainer ? '프로그램 운영' : '예약 신청'}</h2>
        <dl>
          <dt>정원</dt><dd>{program.capacity}명</dd>
          <dt>상태</dt><dd>{program.status}</dd>
        </dl>
        <div className="row-actions">
          {isMainTrainer ? (
            <Link className="button button-secondary" to={`/programs/${program.id}/manage`}>프로그램 관리</Link>
          ) : isTrainer ? (
            <span className="notice">담당 프로그램만 관리할 수 있습니다.</span>
          ) : user ? (
            <button className="button button-primary" disabled={reserved || submitting || !reservable} onClick={reserve}>
              {reserved ? '예약 신청 완료' : submitting ? '신청 중...' : reservable ? '예약 신청하기' : '예약 불가'}
            </button>
          ) : (
            <Link className="button button-primary" to="/login">로그인 후 예약하기</Link>
          )}
        </div>
        {reserved && <p className="notice">예약이 신청되었습니다. 트레이너 승인을 기다려 주세요.</p>}
        {error && <p className="notice notice-error">{error}</p>}
      </aside>
    </section>
  )
}

export default ProgramDetailPage
