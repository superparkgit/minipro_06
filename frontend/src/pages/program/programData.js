export const programs = [
  {
    id: 1,
    emoji: '🧘',
    title: '퇴근 후 릴랙스 요가',
    type: 'GROUP',
    trainer: '김트레이너',
    schedule: '매주 월 · 수 · 금 19:00',
    capacity: 12,
    spotsLeft: 4,
    description: '하루의 긴장을 풀고 유연성과 코어를 함께 기르는 초급 요가 프로그램입니다.',
  },
  {
    id: 2,
    emoji: '🏋️',
    title: '맞춤형 1:1 PT',
    type: 'PT',
    trainer: '이트레이너',
    schedule: '일정 협의 후 진행',
    capacity: 1,
    spotsLeft: 1,
    description: '운동 목표와 체력 수준을 바탕으로 구성하는 개인 맞춤 트레이닝입니다.',
  },
  {
    id: 3,
    emoji: '🤸',
    title: '기초 필라테스',
    type: 'GROUP',
    trainer: '박트레이너',
    schedule: '매주 화 · 목 18:30',
    capacity: 8,
    spotsLeft: 2,
    description: '호흡과 코어 사용법부터 천천히 익히는 입문 필라테스 프로그램입니다.',
  },
]

export const getProgramById = (programId) => programs.find((program) => program.id === Number(programId))
