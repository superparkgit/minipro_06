import PagePlaceholder from '../PagePlaceholder'

function AdminUsersPage() {
  return (
    <PagePlaceholder
      title="회원 권한 관리"
      owner="관리자 담당"
      description="전체 회원을 조회하고 USER, TRAINER, ADMIN 역할을 변경합니다."
      api="GET /api/admin/users · PATCH /api/admin/users/{userId}/roles"
    />
  )
}

export default AdminUsersPage
