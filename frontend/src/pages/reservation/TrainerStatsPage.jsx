import { useEffect, useMemo, useState } from 'react'
import { Link } from 'react-router-dom'
import { getPrograms } from '../../api/programApi'
import { programs as demoPrograms } from '../program/programData'

const readReservations = () => {
  try { return JSON.parse(localStorage.getItem('demoReservations') ?? '[]') }
  catch { return [] }
}

function TrainerStatsPage() {
  let user = null
  try { user = JSON.parse(localStorage.getItem('fitReserveUser')) } catch { user = null }
  const [programs, setPrograms] = useState([])
  const [reservations, setReservations] = useState(readReservations)
  const [loading, setLoading] = useState(true)

  useEffect(() => {
    if (localStorage.getItem('accessToken') === 'demo-access-token') {
      setPrograms(demoPrograms.filter((program) => program.trainerId === user?.id))
      setReservations(readReservations())
      setLoading(false)
      return
    }
    getPrograms()
      .then(({ data }) => setPrograms(data.filter((program) => program.trainers?.some((trainer) => trainer.id === user?.id))))
      .finally(() => setLoading(false))
  }, [user?.id])

  const counts = useMemo(() => programs.map((program) => {
    const matched = reservations.filter((reservation) => reservation.programId === program.id)
    return {
      ...program,
      total: matched.length,
      pending: matched.filter((reservation) => reservation.status === 'PENDING').length,
      approved: matched.filter((reservation) => reservation.status === 'APPROVED').length,
    }
  }), [programs, reservations])

  return (
    <section>
      <div className="section-heading">
        <div><p className="eyebrow">TRAINER</p><h1>{user?.name}님의 프로그램</h1><p>내가 담당하는 프로그램과 예약 회원을 관리하세요.</p></div>
        <Link className="button button-secondary" to="/programs/new">프로그램 등록</Link>
      </div>
      {loading && <p className="notice">담당 프로그램을 불러오는 중입니다.</p>}
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
