import { useEffect, useState } from 'react'
import { cancelReservation, getMyReservations } from '../../api/reservationApi'
import { getApiErrorMessage } from '../../api/apiError'
import { useCurrentUser } from '../../hooks/useCurrentUser'

const sampleReservations = [
  { id: 1, programName: '초급 웨이트 트레이닝', status: 'APPROVED', attendanceStatus: 'NOT_CHECKED' },
  { id: 2, programName: '모닝 요가', status: 'PENDING', attendanceStatus: 'NOT_CHECKED' },
  { id: 3, programName: '코어 강화 클래스', status: 'APPROVED', attendanceStatus: 'ATTENDED' },
]

const readDemoReservations = () => {
  try { return JSON.parse(localStorage.getItem('demoReservations') ?? '[]') }
  catch { return [] }
}

const readUpdates = () => {
  try { return JSON.parse(localStorage.getItem('demoReservationUpdates') ?? '{}') }
  catch { return {} }
}

const applyUpdates = (items) => {
  const updates = readUpdates()
  return items.map((item) => ({ ...item, ...updates[item.id] }))
}

function MyReservationsPage() {
  const { user, loading: userLoading } = useCurrentUser()
  const [reservations, setReservations] = useState([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState('')

  useEffect(() => {
    if (userLoading) return
    if (!user) {
      setLoading(false)
      return
    }
    if (localStorage.getItem('accessToken') === 'demo-access-token') {
      setReservations(applyUpdates([...readDemoReservations(), ...sampleReservations]))
      setLoading(false)
      return
    }
    getMyReservations()
      .then(({ data }) => setReservations(data))
      .catch((requestError) => setError(getApiErrorMessage(requestError, '예약 목록을 불러오지 못했습니다.')))
      .finally(() => setLoading(false))
  }, [user, userLoading])

  const cancel = async (reservationId) => {
    if (!window.confirm('이 예약을 취소할까요? 취소 후에는 원래 상태로 되돌릴 수 없습니다.')) return
    setError('')
    try {
      if (localStorage.getItem('accessToken') === 'demo-access-token') {
        const saved = readDemoReservations().map((item) => item.id === reservationId ? { ...item, status: 'CANCELED' } : item)
        localStorage.setItem('demoReservations', JSON.stringify(saved))
        const updates = readUpdates()
        updates[reservationId] = { ...updates[reservationId], status: 'CANCELED' }
        localStorage.setItem('demoReservationUpdates', JSON.stringify(updates))
      } else {
        await cancelReservation(reservationId)
      }
      setReservations((items) => items.map((item) => item.id === reservationId ? { ...item, status: 'CANCELED' } : item))
    } catch (requestError) {
      setError(getApiErrorMessage(requestError, '예약을 취소하지 못했습니다.'))
    }
  }

  if (userLoading) return <p className="notice">로그인 정보를 확인하는 중입니다.</p>
  if (!user) return <section className="page-card"><h1>로그인이 필요합니다.</h1><p>내 예약을 확인하려면 먼저 로그인해 주세요.</p></section>

  return (
    <section>
      <div className="section-heading"><div><h1>내 예약</h1><p>신청한 수업의 상태를 확인하세요.</p></div></div>
      {loading && <p className="notice">예약 목록을 불러오는 중입니다.</p>}
      {error && <p className="notice notice-error">{error}</p>}
      {!loading && reservations.length === 0 && <p className="page-card">예약 내역이 없습니다.</p>}
      <div className="reservation-list">
        {reservations.map((reservation) => (
          <article className="reservation-row" key={reservation.id}>
            <div><h3>{reservation.programName}</h3><p>출석 상태: {reservation.attendanceStatus}</p></div>
            <div className="row-actions">
              <span className={`badge ${reservation.status.toLowerCase()}`}>{reservation.status}</span>
              {['PENDING', 'APPROVED'].includes(reservation.status) && reservation.attendanceStatus === 'NOT_CHECKED' && <button className="button button-danger" onClick={() => cancel(reservation.id)}>취소</button>}
            </div>
          </article>
        ))}
      </div>
    </section>
  )
}

export default MyReservationsPage
