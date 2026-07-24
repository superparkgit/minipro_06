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

function TrainerStatsPage() {
  const { user, loading: userLoading } = useCurrentUser()
  const [programs, setPrograms] = useState([])
  const [reservations, setReservations] = useState(readReservations)
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState('')

  useEffect(() => {
    if (userLoading) return
    if (!hasRole(user, 'ROLE_TRAINER')) {
      setLoading(false)
      return
    }
    if (localStorage.getItem('accessToken') === 'demo-access-token') {
      setPrograms(demoPrograms.filter((program) => program.trainerId === user?.id))
      setReservations(readReservations())
      setLoading(false)
      return
    }
    getPrograms()
      .then(async ({ data }) => {
        const assignedPrograms = data.filter((program) => program.trainers?.some((trainer) => trainer.id === user?.id))
        const responses = await Promise.all(assignedPrograms.map((program) => getProgramReservations(program.id)))
        setPrograms(assignedPrograms)
        setReservations(responses.flatMap(({ data: items }) => items))
      })
      .catch(() => setError('트레이너 프로그램과 예약 현황을 불러오지 못했습니다.'))
      .finally(() => setLoading(false))
  }, [user, userLoading])

  const counts = useMemo(() => programs.map((program) => {
    const matched = reservations.filter((reservation) => reservation.programId === program.id)
    return {
      ...program,
      total: matched.length,
      pending: matched.filter((reservation) => reservation.status === 'PENDING').length,
      approved: matched.filter((reservation) => reservation.status === 'APPROVED').length,
    }
  }), [programs, reservations])

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
      <div className="reservation-list">
        {counts.map((program) => (
          <article className="reservation-row" key={program.id}>
            <div><h3>{program.title}</h3><p>전체 예약 {program.total}명 · 승인 대기 {program.pending}명 · 승인 {program.approved}명</p></div>
            <div className="row-actions">
              <Link className="button button-secondary" to={`/programs/${program.id}`}>상세</Link>
              <Link className="button button-primary" to={`/programs/${program.id}/reservations`}>예약 회원 관리</Link>
            </div>
          </article>
        ))}
      </div>
    </section>
  )
}

export default TrainerStatsPage
