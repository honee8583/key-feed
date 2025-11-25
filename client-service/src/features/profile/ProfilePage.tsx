import './ProfilePage.css'
import { useNavigate } from 'react-router-dom'
import { useAuth } from '../auth'

const figureAssets = {
  settings: 'http://localhost:3845/assets/5d4a45f0326f262829346093f202523a31967c88.svg',
  notification: 'http://localhost:3845/assets/155733f40f9ba1a0977946287da5216e91d52c9b.svg',
  theme: 'http://localhost:3845/assets/d17fa9aa00bc6edab95c741d88f7f148421bbaca.svg',
  general: 'http://localhost:3845/assets/377629d33313052cbebb6bb9e11d22d4ac452771.svg',
  help: 'http://localhost:3845/assets/64e0f923ffb923a33e5f4675b04dd2fac38c9779.svg',
  stats: 'http://localhost:3845/assets/aedcc951b7b008f22809207205bbe0b51761fcbd.svg',
  logout: 'http://localhost:3845/assets/9bd56683fafd1fcbf8990b5aaa6315a6a9e1fd53.svg',
  chevron: 'http://localhost:3845/assets/3d25bd377c1a23a7fad2473510412a222380c603.svg',
  addSource: 'http://localhost:3845/assets/0559802f2e20b199fbbcf963a04ae79ad9da0402.svg',
}

const kpiCards = [
  { id: 'sources', icon: '📚', value: '12', label: '구독 소스' },
  { id: 'keywords', icon: '🔖', value: '8', label: '활성 키워드' },
  { id: 'saved', icon: '💾', value: '40', label: '저장된 콘텐츠' },
  { id: 'read', icon: '📖', value: '156', label: '읽은 콘텐츠' },
]

const managementLinks = [
  {
    id: 'sources',
    title: '소스 관리',
    subtitle: '수집 채널, RSS 등을 구성하세요',
    icon: figureAssets.addSource,
    path: '/profile/sources',
  },
]

const settingLinks = [
  { id: 'notify', title: '알림 설정', subtitle: '푸시·이메일 옵션', icon: figureAssets.notification },
  { id: 'theme', title: '테마 설정', subtitle: '밝기, 가독성, 대비', icon: figureAssets.theme },
  { id: 'general', title: '일반 설정', subtitle: '보안, 계정, 기타', icon: figureAssets.general },
]

const infoLinks = [
  { id: 'help', title: '도움말', subtitle: '자주 묻는 질문', icon: figureAssets.help },
  { id: 'stats', title: '통계', subtitle: '사용 리포트', icon: figureAssets.stats, badge: 'NEW' },
]

type LinkItem = {
  id: string
  title: string
  subtitle: string
  icon: string
  badge?: string
  path?: string
}

export function ProfilePage() {
  const { user, logout } = useAuth()
  const navigate = useNavigate()

  const handleLogout = () => {
    if (window.confirm('정말 로그아웃하시겠어요?')) {
      logout()
    }
  }

  return (
    <div className="profile-page">
      <div className="profile-page__glow profile-page__glow--blue" aria-hidden />
      <div className="profile-page__glow profile-page__glow--purple" aria-hidden />
      <div className="profile-page__content">
        <section className="profile-hero" aria-label="사용자 요약">
          <div className="profile-card">
            <div className="profile-card__avatar" aria-hidden>
              <span role="img" aria-label="사용자 아바타">
                👤
              </span>
            </div>
            <div className="profile-card__info">
              <p className="profile-card__name">{user?.name ?? '사용자'}</p>
              <div className="profile-card__status">
                <span className="status-dot" aria-hidden />
                <span>{user?.email ?? 'user@example.com'}</span>
              </div>
            </div>
            <button type="button" className="profile-card__settings" aria-label="설정">
              <img src={figureAssets.settings} alt="" aria-hidden />
            </button>
          </div>

          <div className="profile-kpi" aria-label="콘텐츠 활동 요약">
            {kpiCards.map((kpi) => (
              <article key={kpi.id} className="profile-kpi__card">
                <span className="profile-kpi__icon" aria-hidden>
                  {kpi.icon}
                </span>
                <p className="profile-kpi__value">{kpi.value}</p>
                <p className="profile-kpi__label">{kpi.label}</p>
              </article>
            ))}
          </div>
        </section>

        <ProfileSection title="관리" icon={figureAssets.settings}>
          {managementLinks.map((item) => (
            <ProfileListItem
              key={item.id}
              {...item}
              onSelect={item.path ? () => navigate(item.path as string) : undefined}
            />
          ))}
        </ProfileSection>

        <ProfileSection title="설정" icon={figureAssets.settings}>
          {settingLinks.map((item) => (
            <ProfileListItem key={item.id} {...item} />
          ))}
        </ProfileSection>

        <ProfileSection title="정보" icon={figureAssets.settings}>
          {infoLinks.map((item) => (
            <ProfileListItem key={item.id} {...item} />
          ))}
        </ProfileSection>

        <button type="button" className="profile-logout" onClick={handleLogout}>
          <img src={figureAssets.logout} alt="" aria-hidden />
          로그아웃
        </button>
        <p className="profile-version">버전 1.0.0</p>
      </div>
    </div>
  )
}

type ProfileSectionProps = {
  title: string
  icon: string
  children: React.ReactNode
}

function ProfileSection({ title, icon, children }: ProfileSectionProps) {
  return (
    <section className="profile-section">
      <div className="profile-section__header">
        <h2>{title}</h2>
        <img src={icon} alt="" aria-hidden />
      </div>
      <div className="profile-section__body">{children}</div>
    </section>
  )
}

type ProfileListItemProps = LinkItem & {
  onSelect?: () => void
}

function ProfileListItem({ title, subtitle, icon, badge, onSelect }: ProfileListItemProps) {
  return (
    <button type="button" className="profile-list-item" onClick={onSelect}>
      <div className="profile-list-item__icon">
        <img src={icon} alt="" aria-hidden />
      </div>
      <div className="profile-list-item__text">
        <p className="profile-list-item__title">{title}</p>
        <p className="profile-list-item__subtitle">{subtitle}</p>
      </div>
      {badge ? <span className="profile-list-item__badge">{badge}</span> : null}
      <img className="profile-list-item__chevron" src={figureAssets.chevron} alt="" aria-hidden />
    </button>
  )
}
