export function getApiErrorMessage(error, fallback = '요청을 처리하지 못했습니다.') {
  const data = error?.response?.data
  return data?.message ?? data?.detail ?? data?.error ?? fallback
}
