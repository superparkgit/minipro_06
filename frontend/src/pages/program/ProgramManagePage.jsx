import { useEffect, useState } from 'react'
import { Link, useParams } from 'react-router-dom'
import { addTrainer, cancelProgram, completeProgram, getProgram, getPrograms, getTrainers, removeTrainer } from '../../api/programApi'
import { getApiErrorMessage } from '../../api/apiError'
import { hasRole, useCurrentUser } from '../../hooks/useCurrentUser'
import { getProgramById } from './programData'

function ProgramManagePage() {
  const { programId } = useParams()
  const { user, loading: userLoading } = useCurrentUser()
  const [program, setProgram] = useState(null)
  const [trainerId, setTrainerId] = useState('')
  const [trainerOptions, setTrainerOptions] = useState([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState('')
  const [message, setMessage] = useState('')
  const [trainerListNotice, setTrainerListNotice] = useState('')

  useEffect(() => {
    getProgram(programId)
      .then(({ data }) => setProgram(data))
      .catch((requestError) => {
        if (localStorage.getItem('accessToken') === 'demo-access-token') {
          const demo = getProgramById(programId)
          if (demo) setProgram({ ...demo, status: 'OPEN', trainers: [{ id: demo.trainerId, name: demo.trainer, assignmentRole: 'MAIN' }] })
          else setError('프로그램을 찾을 수 없습니다.')
        } else {
          setError(getApiErrorMessage(requestError, '프로그램을 불러오지 못했습니다.'))
        }
      })
      .finally(() => setLoading(false))

    getTrainers()
      .then(({ data }) => setTrainerOptions(data))
      .catch(async () => {
        try {
          const { data: programs } = await getPrograms()
          const trainersById = new Map()
          programs.forEach((item) => {
            item.trainers?.forEach((trainer) => trainersById.set(trainer.id, trainer))
          })
          setTrainerOptions([...trainersById.values()])
          setTrainerListNotice('전체 트레이너 목록을 불러오지 못해 기존 프로그램 담당자만 표시합니다.')
        } catch {
          setTrainerListNotice('트레이너 목록을 불러오지 못했습니다. 잠시 후 다시 시도해 주세요.')
        }
      })
  }, [programId])

  const changeStatus = async (nextStatus) => {
    const label = nextStatus === 'COMPLETED' ? '수업 완료' : '폐강'
    if (!window.confirm(`${program.title}을(를) ${label} 처리할까요?`)) return
    setError(''); setMessage('')
    try {
      if (localStorage.getItem('accessToken') !== 'demo-access-token') {
        if (nextStatus === 'COMPLETED') await completeProgram(program.id)
        else await cancelProgram(program.id)
      }
      setProgram((current) => ({ ...current, status: nextStatus }))
      setMessage(`${label} 처리가 반영되었습니다.`)
    } catch (requestError) { setError(getApiErrorMessage(requestError, `${label} 처리하지 못했습니다.`)) }
  }

  const addAssistant = async (event) => {
    event.preventDefault()
    setError(''); setMessage('')
    try {
      if (localStorage.getItem('accessToken') === 'demo-access-token') {
        setProgram((current) => ({ ...current, trainers: [...current.trainers, { id: Number(trainerId), name: `트레이너 #${trainerId}`, assignmentRole: 'ASSISTANT' }] }))
      } else {
        const { data } = await addTrainer(program.id, { trainerId: Number(trainerId) })
        setProgram(data)
      }
      setTrainerId(''); setMessage('보조 트레이너를 추가했습니다.')
    } catch (requestError) { setError(getApiErrorMessage(requestError, '보조 트레이너를 추가하지 못했습니다.')) }
  }

  const removeAssistant = async (assistant) => {
    if (!window.confirm(`${assistant.name} 트레이너를 담당에서 제외할까요?`)) return
    setError(''); setMessage('')
    try {
      if (localStorage.getItem('accessToken') !== 'demo-access-token') await removeTrainer(program.id, assistant.id)
      setProgram((current) => ({ ...current, trainers: current.trainers.filter((trainer) => trainer.id !== assistant.id) }))
      setMessage('보조 트레이너를 제외했습니다.')
    } catch (requestError) { setError(getApiErrorMessage(requestError, '보조 트레이너를 제외하지 못했습니다.')) }
  }

  if (loading || userLoading) return <p className="notice">프로그램을 불러오는 중입니다.</p>
  if (!program) return <section className="page-card"><h1>프로그램을 찾을 수 없습니다.</h1><p>{error}</p></section>

  const mainTrainer = program.trainers?.find((trainer) => trainer.assignmentRole === 'MAIN')
  const assistants = program.trainers?.filter((trainer) => trainer.assignmentRole === 'ASSISTANT') ?? []
  const assignedTrainerIds = new Set(program.trainers?.map((trainer) => trainer.id) ?? [])
  const availableTrainers = trainerOptions.filter((trainer) => !assignedTrainerIds.has(trainer.id))
  if (!hasRole(user, 'ROLE_TRAINER') || mainTrainer?.id !== user?.id) return <section className="page-card"><h1>접근할 수 없습니다.</h1><p>해당 프로그램의 MAIN 트레이너만 관리할 수 있습니다.</p></section>

  return (
    <section>
      <div className="section-heading"><div><h1>{program.title} 관리</h1><p>MAIN 트레이너가 프로그램 상태와 담당 강사를 관리합니다.</p></div><Link className="button button-secondary" to={`/programs/${program.id}/edit`}>프로그램 수정</Link></div>
      {error && <p className="notice notice-error">{error}</p>}{message && <p className="notice">{message}</p>}
      <div className="grid manage-grid">
        <article className="card"><h2>수업 상태</h2><p className="muted">현재 상태: <span className={`badge ${program.status.toLowerCase()}`}>{program.status}</span></p><div className="row-actions"><button className="button button-secondary" disabled={program.status !== 'OPEN'} onClick={() => changeStatus('COMPLETED')}>수업 완료</button><button className="button button-danger" disabled={program.status !== 'OPEN'} onClick={() => changeStatus('CANCELED')}>폐강 처리</button></div></article>
        <article className="card"><h2>예약 관리</h2><p className="muted">신청 회원을 승인·거절하고 수업 후 출석을 처리합니다.</p><Link className="button button-primary" to={`/programs/${program.id}/reservations`}>예약자 보기</Link></article>
      </div>
      <article className="card assistant-card">
        <div className="section-heading"><div><h2>담당 트레이너</h2><p>대표 담당자와 보조 담당자를 확인합니다.</p></div><form className="row-actions" onSubmit={addAssistant}><select value={trainerId} onChange={(event) => setTrainerId(event.target.value)} required><option value="">보조 트레이너 선택</option>{availableTrainers.map((trainer) => <option key={trainer.id} value={trainer.id}>{trainer.name}</option>)}</select><button className="button button-secondary" disabled={!trainerId}>보조 강사 추가</button></form></div>
        {trainerListNotice && <p className="notice">{trainerListNotice}</p>}
        {availableTrainers.length === 0 && !trainerListNotice && <p className="muted">현재 선택 가능한 트레이너가 없습니다.</p>}
        <div className="reservation-list">
          <div className="reservation-row"><div><h3>{mainTrainer?.name ?? '미정'}</h3><p>대표 담당 트레이너</p></div><span className="badge">MAIN</span></div>
          {assistants.map((assistant) => <div className="reservation-row" key={assistant.id}><div><h3>{assistant.name}</h3><p>보조 담당 트레이너</p></div><div className="row-actions"><span className="badge">ASSISTANT</span><button className="button button-danger" onClick={() => removeAssistant(assistant)}>제거</button></div></div>)}
        </div>
      </article>
    </section>
  )
}

export default ProgramManagePage
