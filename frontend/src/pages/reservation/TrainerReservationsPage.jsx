import { useEffect, useState } from 'react'
import { useParams } from 'react-router-dom'
import { approveReservation, getProgramReservations, rejectReservation, updateAttendance } from '../../api/reservationApi'
import { getApiErrorMessage } from '../../api/apiError'

const demoApplicants = [
  { id: 101, userId: 4, userName: '박회원', programId: 1, programName: '퇴근 후 릴랙스 요가', status: 'APPROVED', attendanceStatus: 'NOT_CHECKED' },
  { id: 102, userId: 6, userName: '최회원', programId: 1, programName: '퇴근 후 릴랙스 요가', status: 'PENDING', attendanceStatus: 'NOT_CHECKED' },
  { id: 103, userId: 7, userName: '정회원', programId: 2, programName: '맞춤형 1:1 PT', status: 'PENDING', attendanceStatus: 'NOT_CHECKED' },
]

const readSaved = () => {
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

function TrainerReservationsPage() {
  const { programId } = useParams()
  const [applicants, setApplicants] = useState([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState('')
  const [message, setMessage] = useState('')

  useEffect(() => {
    if (localStorage.getItem('accessToken') === 'demo-access-token') {
      const id = Number(programId)
      setApplicants(applyUpdates([...readSaved(), ...demoApplicants]).filter((item) => item.programId === id))
      setLoading(false)
      return
    }
    getProgramReservations(programId)
      .then(({ data }) => setApplicants(data))
      .catch(() => setError('예약자 목록을 불러오지 못했습니다. 백엔드 연결을 확인해 주세요.'))
      .finally(() => setLoading(false))
  }, [programId])

  const runAction = async (id, type, action) => {
    const labels = { APPROVE: '승인', REJECT: '거절', ATTENDED: '출석', NO_SHOW: '결석' }
    const current = applicants.find((item) => item.id === id)
    if (!window.confirm(`${current?.userName ?? `회원 #${current?.userId}`}님의 예약을 ${labels[type]} 처리할까요?`)) return
    setError('')
    setMessage('')
    try {
      if (localStorage.getItem('accessToken') === 'demo-access-token') {
        const next = type === 'APPROVE' ? { ...current, status: 'APPROVED' }
          : type === 'REJECT' ? { ...current, status: 'REJECTED' }
          : { ...current, attendanceStatus: type }
        setApplicants((items) => items.map((item) => item.id === id ? next : item))
        const saved = readSaved().map((item) => item.id === id ? next : item)
        localStorage.setItem('demoReservations', JSON.stringify(saved))
        const updates = readUpdates()
        updates[id] = { status: next.status, attendanceStatus: next.attendanceStatus }
        localStorage.setItem('demoReservationUpdates', JSON.stringify(updates))
      } else {
        const { data } = await action(id)
        setApplicants((items) => items.map((item) => item.id === id ? data : item))
      }
      setMessage(`${labels[type]} 처리가 반영되었습니다.`)
    } catch (requestError) {
      setError(getApiErrorMessage(requestError))
    }
  }

  return (
    <section>
      <div className="section-heading"><div><h1>예약 관리</h1><p>대기 예약을 승인·거절하고 수업 후 출석을 처리합니다.</p></div></div>
      {loading && <p className="notice">예약자 목록을 불러오는 중입니다.</p>}
      {error && <p className="notice notice-error">{error}</p>}
      {message && <p className="notice">{message}</p>}
      {!loading && applicants.length === 0 && <p className="page-card">신청한 회원이 없습니다.</p>}
      <div className="reservation-list">
        {applicants.map((applicant) => (
          <article className="reservation-row" key={applicant.id}>
            <div><h3>{applicant.userName ?? `회원 #${applicant.userId}`}</h3><p>{applicant.programName} · 출석 {applicant.attendanceStatus}</p></div>
            <div className="row-actions">
              <span className={`badge ${applicant.status.toLowerCase()}`}>{applicant.status}</span>
              {applicant.status === 'PENDING' && <><button className="button button-primary" onClick={() => runAction(applicant.id, 'APPROVE', approveReservation)}>승인</button><button className="button button-danger" onClick={() => runAction(applicant.id, 'REJECT', rejectReservation)}>거절</button></>}
              {applicant.status === 'APPROVED' && applicant.attendanceStatus === 'NOT_CHECKED' && <>
                <button className="button button-secondary" onClick={() => runAction(applicant.id, 'ATTENDED', (id) => updateAttendance(id, { attendanceStatus: 'ATTENDED' }))}>출석</button>
                <button className="button button-danger" onClick={() => runAction(applicant.id, 'NO_SHOW', (id) => updateAttendance(id, { attendanceStatus: 'NO_SHOW' }))}>결석</button>
              </>}
            </div>
          </article>
        ))}
      </div>
    </section>
  )
}

export default TrainerReservationsPage
