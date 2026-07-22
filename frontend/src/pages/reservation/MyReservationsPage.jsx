import { useEffect, useState } from 'react'
import { cancelReservation, getMyReservations } from '../../api/reservationApi'
import { getApiErrorMessage } from '../../api/apiError'

function MyReservationsPage() {
  const [reservations, setReservations] = useState([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState('')

  useEffect(() => {
    getMyReservations()
      .then(({ data }) => setReservations(data))
      .catch((requestError) => setError(getApiErrorMessage(requestError, '예약 목록을 불러오지 못했습니다.')))
      .finally(() => setLoading(false))
  }, [])

  const cancel = async (reservationId) => {
    setError('')
    try {
      await cancelReservation(reservationId)
      setReservations((items) => items.map((item) => item.id === reservationId ? { ...item, status: 'CANCELED' } : item))
    } catch (requestError) {
      setError(getApiErrorMessage(requestError, '예약을 취소하지 못했습니다.'))
    }
  }

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
              {['PENDING', 'APPROVED'].includes(reservation.status) && <button className="button button-danger" onClick={() => cancel(reservation.id)}>취소</button>}
            </div>
          </article>
        ))}
      </div>
    </section>
  )
}

export default MyReservationsPage
