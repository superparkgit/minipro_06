import { useEffect, useState } from 'react'
import { useParams } from 'react-router-dom'
import { approveReservation, getProgramReservations, rejectReservation, updateAttendance } from '../../api/reservationApi'
import { getApiErrorMessage } from '../../api/apiError'

function TrainerReservationsPage() {
  const { programId } = useParams()
  const [applicants, setApplicants] = useState([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState('')

  useEffect(() => {
    getProgramReservations(programId)
      .then(({ data }) => setApplicants(data))
      .catch((requestError) => setError(getApiErrorMessage(requestError, '예약자 목록을 불러오지 못했습니다.')))
      .finally(() => setLoading(false))
  }, [programId])

  const runAction = async (id, action) => {
    setError('')
    try {
      const { data } = await action(id)
      setApplicants((items) => items.map((item) => item.id === id ? data : item))
    } catch (requestError) {
      setError(getApiErrorMessage(requestError))
    }
  }

  return (
    <section>
      <div className="section-heading"><div><h1>예약 관리</h1><p>대기 예약을 승인·거절하고 수업 후 출석을 처리합니다.</p></div></div>
      {loading && <p className="notice">예약자 목록을 불러오는 중입니다.</p>}
      {error && <p className="notice notice-error">{error}</p>}
      {!loading && applicants.length === 0 && <p className="page-card">신청한 회원이 없습니다.</p>}
      <div className="reservation-list">
        {applicants.map((applicant) => (
          <article className="reservation-row" key={applicant.id}>
            <div><h3>회원 #{applicant.userId}</h3><p>{applicant.programName} · 출석 {applicant.attendanceStatus}</p></div>
            <div className="row-actions">
              <span className={`badge ${applicant.status.toLowerCase()}`}>{applicant.status}</span>
              {applicant.status === 'PENDING' && <><button className="button button-primary" onClick={() => runAction(applicant.id, approveReservation)}>승인</button><button className="button button-danger" onClick={() => runAction(applicant.id, rejectReservation)}>거절</button></>}
              {applicant.status === 'APPROVED' && applicant.attendanceStatus === 'NOT_CHECKED' && <>
                <button className="button button-secondary" onClick={() => runAction(applicant.id, (id) => updateAttendance(id, { attendanceStatus: 'ATTENDED' }))}>출석</button>
                <button className="button button-danger" onClick={() => runAction(applicant.id, (id) => updateAttendance(id, { attendanceStatus: 'NO_SHOW' }))}>결석</button>
              </>}
            </div>
          </article>
        ))}
      </div>
    </section>
  )
}

export default TrainerReservationsPage
