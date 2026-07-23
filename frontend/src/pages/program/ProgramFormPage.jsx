import { useEffect, useState } from 'react'
import { useNavigate, useParams } from 'react-router-dom'
import { createProgram, getProgram, updateProgram } from '../../api/programApi'
import { getApiErrorMessage } from '../../api/apiError'
import { hasRole, useCurrentUser } from '../../hooks/useCurrentUser'

const initialForm = { title: '', type: 'GROUP', description: '', capacity: 8, startAt: '', endAt: '' }

function ProgramFormPage() {
  const { programId } = useParams()
  const navigate = useNavigate()
  const isEdit = Boolean(programId)
  const { user, loading: userLoading } = useCurrentUser()
  const [form, setForm] = useState(initialForm)
  const [loading, setLoading] = useState(isEdit)
  const [submitting, setSubmitting] = useState(false)
  const [error, setError] = useState('')

  useEffect(() => {
    if (!isEdit) return
    getProgram(programId)
      .then(({ data }) => setForm({
        title: data.title,
        type: data.type,
        description: data.description ?? '',
        capacity: data.capacity,
        startAt: data.startAt?.slice(0, 16) ?? '',
        endAt: data.endAt?.slice(0, 16) ?? '',
      }))
      .catch((requestError) => setError(getApiErrorMessage(requestError, '프로그램을 불러오지 못했습니다.')))
      .finally(() => setLoading(false))
  }, [isEdit, programId])

  const change = (event) => setForm((current) => ({ ...current, [event.target.name]: event.target.value }))

  const submit = async (event) => {
    event.preventDefault()
    if (new Date(form.endAt) <= new Date(form.startAt)) {
      setError('종료 시간은 시작 시간보다 뒤여야 합니다.')
      return
    }
    setSubmitting(true)
    setError('')
    const payload = { ...form, capacity: Number(form.capacity) }
    try {
      if (localStorage.getItem('accessToken') === 'demo-access-token') {
        navigate(isEdit ? `/programs/${programId}` : '/programs')
        return
      }
      const { data } = isEdit ? await updateProgram(programId, payload) : await createProgram(payload)
      navigate(`/programs/${data.id}`)
    } catch (requestError) {
      setError(getApiErrorMessage(requestError, `${isEdit ? '수정' : '등록'}하지 못했습니다.`))
    } finally {
      setSubmitting(false)
    }
  }

  if (loading || userLoading) return <p className="notice">프로그램을 불러오는 중입니다.</p>
  if (!hasRole(user, 'ROLE_TRAINER')) return <section className="page-card"><h1>접근할 수 없습니다.</h1><p>트레이너만 프로그램을 등록하거나 수정할 수 있습니다.</p></section>

  return (
    <section className="card form-card">
      <div className="section-heading"><div><h1>{isEdit ? '프로그램 수정' : '프로그램 등록'}</h1><p>수업 정보와 운영 일정을 입력하세요.</p></div></div>
      {error && <p className="notice notice-error">{error}</p>}
      <form className="form-grid" onSubmit={submit}>
        <label>프로그램명<input name="title" value={form.title} onChange={change} required /></label>
        <label>유형<select name="type" value={form.type} onChange={change}><option value="GROUP">그룹 수업</option><option value="PT">1:1 PT</option></select></label>
        <label>수업 설명<textarea name="description" rows="4" value={form.description} onChange={change} /></label>
        <label>정원<input name="capacity" value={form.capacity} onChange={change} required min="1" type="number" /></label>
        <label>시작 시간<input name="startAt" value={form.startAt} onChange={change} required type="datetime-local" /></label>
        <label>종료 시간<input name="endAt" value={form.endAt} onChange={change} required type="datetime-local" /></label>
        <div className="form-actions"><button className="button button-primary" type="submit" disabled={submitting}>{submitting ? '저장 중...' : isEdit ? '수정 저장' : '등록하기'}</button></div>
      </form>
    </section>
  )
}

export default ProgramFormPage
