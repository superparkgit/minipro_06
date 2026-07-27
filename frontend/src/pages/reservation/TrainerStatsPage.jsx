import { useEffect, useMemo, useState } from 'react'
import { Link } from 'react-router-dom'
import { getPrograms } from '../../api/programApi'
import { getProgramReservations } from '../../api/reservationApi'
import { hasRole, useCurrentUser } from '../../hooks/useCurrentUser'
import { programs as demoPrograms } from '../program/programData'

const readReservations = () => {
  try { return JSON.parse(localStorage.getItem('demoReservations') ?? '[]') }
  catch { return [] }
}

const programStatusLabel = {
  OPEN: '진행 중',
  CLOSED: '마감',
  COMPLETED: '수업 완료',
  CANCELED: '폐강',
}

function TrainerStatsPage() {
  const { user, loading: userLoading } = useCurrentUser()
  const [programs, setPrograms] = useState([])
  const [reservations, setReservations] = useState(readReservations)
  const [statusFilter, setStatusFilter] = useState('OPEN')
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState('')

  useEffect(() => {
    if (userLoading) return
    if (!hasRole(user, 'ROLE_TRAINER')) {
      setLoading(false)
      return
    }
    if (localStorage.getItem('accessToken') === 'demo-access-token') {
      setPrograms(
        demoPrograms
          .filter((program) => program.trainerId === user?.id)
          .map((program) => ({
            ...program,
            status: 'OPEN',
            trainers: [{
              id: program.trainerId,
              name: program.trainer,
              assignmentRole: 'MAIN',
            }],
          })),
      )
      setReservations(readReservations())
      setLoading(false)
      return
    }
    getPrograms()
      .then(async ({ data }) => {
        const assignedPrograms = data.filter((program) => program.trainers?.some((trainer) => trainer.id === user?.id))
        setPrograms(assignedPrograms)
        const results = await Promise.allSettled(
          assignedPrograms.map((program) => getProgramReservations(program.id)),
        )
        setReservations(results.flatMap((result) =>
          result.status === 'fulfilled' ? result.value.data : [],
        ))
        if (results.some((result) => result.status === 'rejected')) {
          setError('일부 프로그램의 예약 인원을 불러오지 못했습니다. 프로그램은 계속 관리할 수 있습니다.')
        }
      })
      .catch(() => setError('담당 프로그램을 불러오지 못했습니다.'))
      .finally(() => setLoading(false))
  }, [user, userLoading])

  const counts = useMemo(() => programs.map((program) => {
    const matched = reservations.filter((reservation) => reservation.programId === program.id)
    const assignmentRole = program.trainers
      ?.find((trainer) => trainer.id === user?.id)
      ?.assignmentRole
      ?? (program.trainerId === user?.id ? 'MAIN' : null)
    return {
      ...program,
      assignmentRole,
      total: matched.length,
      pending: matched.filter((reservation) => reservation.status === 'PENDING').length,
      approved: matched.filter((reservation) => reservation.status === 'APPROVED').length,
    }
  }), [programs, reservations, user?.id])
  const visiblePrograms = statusFilter === 'ALL'
    ? counts
    : counts.filter((program) => program.status === statusFilter)

  if (userLoading) return <p className="notice">로그인 정보를 확인하는 중입니다.</p>
  if (!hasRole(user, 'ROLE_TRAINER')) return <section className="page-card"><h1>접근할 수 없습니다.</h1><p>트레이너만 통계를 확인할 수 있습니다.</p></section>

  return (
    <section>
      <div className="section-heading">
        <div><p className="eyebrow">TRAINER</p><h1>{user?.name}님의 프로그램</h1><p>내가 담당하는 프로그램과 예약 회원을 관리하세요.</p></div>
        <Link className="button button-secondary" to="/programs/new">프로그램 등록</Link>
      </div>
      {loading && <p className="notice">담당 프로그램을 불러오는 중입니다.</p>}
      {error && <p className="notice notice-error">{error}</p>}
      {!loading && counts.length === 0 && <p className="page-card">현재 담당 중인 프로그램이 없습니다.</p>}
      {!loading && counts.length > 0 && (
        <div className="post-tabs list-filter-tabs" aria-label="담당 프로그램 상태 필터">
          {[
            ['OPEN', '진행 중'],
            ['CLOSED', '마감'],
            ['COMPLETED', '수업 완료'],
            ['CANCELED', '폐강'],
            ['ALL', '전체'],
          ].map(([value, label]) => {
            const count = value === 'ALL'
              ? counts.length
              : counts.filter((program) => program.status === value).length
            return (
              <button
                key={value}
                className={`tab ${statusFilter === value ? 'active' : ''}`}
                onClick={() => setStatusFilter(value)}
              >
                {label} {count}
              </button>
            )
          })}
        </div>
      )}
      {!loading && counts.length > 0 && visiblePrograms.length === 0 && (
        <p className="page-card">해당 상태의 담당 프로그램이 없습니다.</p>
      )}
      <div className="reservation-list">
        {visiblePrograms.map((program) => (
          <article className="reservation-row" key={program.id}>
            <div>
              <h3>{program.title} <span className="badge">{program.assignmentRole === 'MAIN' ? '대표 담당' : '보조 담당'}</span></h3>
              <p>{programStatusLabel[program.status] ?? program.status} · 전체 예약 {program.total}명 · 승인 대기 {program.pending}명 · 승인 {program.approved}명</p>
            </div>
            <div className="row-actions">
              <Link className="button button-secondary" to={`/programs/${program.id}`}>상세</Link>
              <Link
                className="button button-primary"
                to={program.assignmentRole === 'MAIN'
                  ? `/programs/${program.id}/manage`
                  : `/programs/${program.id}/reservations`}
              >
                {program.assignmentRole === 'MAIN' ? '프로그램 관리' : '예약 회원 관리'}
              </Link>
            </div>
          </article>
        ))}
      </div>
    </section>
  )
}

export default TrainerStatsPage
