import PostDetailPage from '../pages/community/PostDetailPage'
import PostFormPage from '../pages/community/PostFormPage'
import PostListPage from '../pages/community/PostListPage'
import AdminReviewModerationPage from '../pages/review/AdminReviewModerationPage'
import ProgramReviewsPage from '../pages/review/ProgramReviewsPage'
import ReviewFormPage from '../pages/review/ReviewFormPage'

export const communityRoutes = [
  { path: '/posts', element: <PostListPage /> },
  { path: '/posts/new', element: <PostFormPage /> },
  { path: '/posts/:postId/edit', element: <PostFormPage /> },
  { path: '/posts/:postId', element: <PostDetailPage /> },
  { path: '/programs/:programId/reviews', element: <ProgramReviewsPage /> },
  { path: '/reviews/write', element: <ReviewFormPage /> },
  { path: '/reviews/:reviewId/edit', element: <ReviewFormPage /> },
  { path: '/admin/reviews', element: <AdminReviewModerationPage /> },
]

export const communityNavigation = [
  { to: '/posts', label: '게시판' },
  { to: '/admin/reviews', label: '리뷰 심사' },
]
