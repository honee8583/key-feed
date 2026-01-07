import { useNavigate } from 'react-router-dom';
import { useAuth } from '../auth';

const figureAssets = {
  settings:
    'http://localhost:3845/assets/5d4a45f0326f262829346093f202523a31967c88.svg',
  notification:
    'http://localhost:3845/assets/155733f40f9ba1a0977946287da5216e91d52c9b.svg',
  theme:
    'http://localhost:3845/assets/d17fa9aa00bc6edab95c741d88f7f148421bbaca.svg',
  general:
    'http://localhost:3845/assets/377629d33313052cbebb6bb9e11d22d4ac452771.svg',
  help: 'http://localhost:3845/assets/64e0f923ffb923a33e5f4675b04dd2fac38c9779.svg',
  stats:
    'http://localhost:3845/assets/aedcc951b7b008f22809207205bbe0b51761fcbd.svg',
  logout:
    'http://localhost:3845/assets/9bd56683fafd1fcbf8990b5aaa6315a6a9e1fd53.svg',
  chevron:
    'http://localhost:3845/assets/3d25bd377c1a23a7fad2473510412a222380c603.svg',
  addSource:
    'http://localhost:3845/assets/0559802f2e20b199fbbcf963a04ae79ad9da0402.svg',
};

const kpiCards = [
  { id: 'sources', icon: '📚', value: '12', label: '구독 소스' },
  { id: 'keywords', icon: '🔖', value: '8', label: '활성 키워드' },
  { id: 'saved', icon: '💾', value: '40', label: '저장된 콘텐츠' },
  { id: 'read', icon: '📖', value: '156', label: '읽은 콘텐츠' },
];

const managementLinks = [
  {
    id: 'sources',
    title: '소스 관리',
    subtitle: '수집 채널, RSS 등을 구성하세요',
    icon: figureAssets.addSource,
    path: '/profile/sources',
  },
];

const settingLinks = [
  {
    id: 'notify',
    title: '알림 설정',
    subtitle: '푸시·이메일 옵션',
    icon: figureAssets.notification,
  },
  {
    id: 'theme',
    title: '테마 설정',
    subtitle: '밝기, 가독성, 대비',
    icon: figureAssets.theme,
  },
  {
    id: 'general',
    title: '일반 설정',
    subtitle: '보안, 계정, 기타',
    icon: figureAssets.general,
  },
];

const infoLinks = [
  {
    id: 'help',
    title: '도움말',
    subtitle: '자주 묻는 질문',
    icon: figureAssets.help,
  },
  {
    id: 'stats',
    title: '통계',
    subtitle: '사용 리포트',
    icon: figureAssets.stats,
    badge: 'NEW',
  },
];

type LinkItem = {
  id: string;
  title: string;
  subtitle: string;
  icon: string;
  badge?: string;
  path?: string;
};

export function ProfilePage() {
  const { user, logout } = useAuth();
  const navigate = useNavigate();

  const handleLogout = () => {
    if (window.confirm('정말 로그아웃하시겠어요?')) {
      logout();
    }
  };

  return (
    <div className='relative flex min-h-screen justify-center overflow-hidden bg-[radial-gradient(circle_at_top,#050b16_45%,#03050a_70%)] px-4 py-8 pb-40 text-white'>
      <div
        className='absolute top-[-140px] left-5 z-0 h-[420px] w-[420px] rounded-full bg-[rgba(81,162,255,0.6)] opacity-35 blur-[120px]'
        aria-hidden
      />
      <div
        className='absolute right-[-160px] bottom-[120px] z-0 h-[520px] w-[520px] rounded-full bg-[rgba(194,122,255,0.35)] opacity-35 blur-[120px]'
        aria-hidden
      />
      <div className='relative z-10 w-full max-w-[378px]'>
        <section className='mb-10 flex flex-col gap-6' aria-label='사용자 요약'>
          <div className='flex items-center gap-4 rounded-[28px] border border-white/20 bg-white/12 p-5 px-6 shadow-[0_20px_40px_rgba(5,10,28,0.35)] backdrop-blur-[18px]'>
            <div
              className='flex h-[72px] w-[72px] items-center justify-center rounded-3xl border-4 border-white/20 text-4xl'
              aria-hidden
            >
              <span role='img' aria-label='사용자 아바타'>
                👤
              </span>
            </div>
            <div className='min-w-0 flex-1'>
              <p className='m-0 text-xl font-bold tracking-[-0.02em]'>
                {user?.name ?? '사용자'}
              </p>
              <div className='mt-1 inline-flex items-center gap-2 text-sm text-[#b7c6ff]'>
                <span
                  className='h-2 w-2 rounded-full bg-[#05df72] shadow-[0_0_12px_rgba(5,223,114,0.35)]'
                  aria-hidden
                />
                <span>{user?.email ?? 'user@example.com'}</span>
              </div>
            </div>
            <button
              type='button'
              className='flex h-12 w-12 cursor-pointer items-center justify-center rounded-2xl border-none bg-white/20 shadow-[0_10px_25px_rgba(0,0,0,0.25)] hover:bg-white/25'
              aria-label='설정'
            >
              <img
                src={figureAssets.settings}
                alt=''
                className='h-5 w-5'
                aria-hidden
              />
            </button>
          </div>

          <div
            className='grid grid-cols-4 gap-2.5 max-[420px]:grid-cols-2'
            aria-label='콘텐츠 활동 요약'
          >
            {kpiCards.map((kpi) => (
              <article
                key={kpi.id}
                className='flex min-h-[108px] flex-col justify-center gap-1 rounded-[20px] border border-white/20 bg-white/18 p-3.5 px-2 text-center'
              >
                <span className='text-2xl' aria-hidden>
                  {kpi.icon}
                </span>
                <p className='m-0 text-lg font-semibold'>{kpi.value}</p>
                <p className='m-0 text-xs text-[#99a1af]'>{kpi.label}</p>
              </article>
            ))}
          </div>
        </section>

        <ProfileSection title='관리' icon={figureAssets.settings}>
          {managementLinks.map((item) => (
            <ProfileListItem
              key={item.id}
              {...item}
              onSelect={
                item.path ? () => navigate(item.path as string) : undefined
              }
            />
          ))}
        </ProfileSection>

        <ProfileSection title='설정' icon={figureAssets.settings}>
          {settingLinks.map((item) => (
            <ProfileListItem key={item.id} {...item} />
          ))}
        </ProfileSection>

        <ProfileSection title='정보' icon={figureAssets.settings}>
          {infoLinks.map((item) => (
            <ProfileListItem key={item.id} {...item} />
          ))}
        </ProfileSection>

        <button
          type='button'
          className='mt-6 flex w-full cursor-pointer items-center justify-center gap-2 rounded-[18px] border-none bg-gradient-to-br from-[#fef2f2] to-[#fdf2f8] p-4 text-base font-semibold text-[#e7000b] hover:opacity-90'
          onClick={handleLogout}
        >
          <img
            src={figureAssets.logout}
            alt=''
            className='h-5 w-5'
            aria-hidden
          />
          로그아웃
        </button>
        <p className='mt-3 mb-0 text-center text-xs text-[#99a1af]'>
          버전 1.0.0
        </p>
      </div>
    </div>
  );
}

type ProfileSectionProps = {
  title: string;
  icon: string;
  children: React.ReactNode;
};

function ProfileSection({ title, icon, children }: ProfileSectionProps) {
  return (
    <section className='relative z-10 mb-7'>
      <div className='mb-3 flex items-center gap-2'>
        <h2 className='m-0 text-lg'>{title}</h2>
        <img
          src={icon}
          alt=''
          className='h-[18px] w-[18px] opacity-75'
          aria-hidden
        />
      </div>
      <div className='overflow-hidden rounded-[20px] border border-[rgba(229,231,235,0.5)] bg-white/90 shadow-[0_25px_60px_rgba(15,23,42,0.3)]'>
        {children}
      </div>
    </section>
  );
}

type ProfileListItemProps = LinkItem & {
  onSelect?: () => void;
};

function ProfileListItem({
  title,
  subtitle,
  icon,
  badge,
  onSelect,
}: ProfileListItemProps) {
  return (
    <button
      type='button'
      className='relative flex w-full cursor-pointer items-center gap-3.5 border-t border-none border-[rgba(229,231,235,0.5)] bg-transparent px-[18px] py-4 text-left text-[#101828] first:border-t-0 hover:bg-slate-50/50'
      onClick={onSelect}
    >
      <div className='flex h-12 w-12 items-center justify-center rounded-2xl bg-[#f4f6fb] shadow-[0_4px_12px_rgba(15,23,42,0.08)]'>
        <img src={icon} alt='' className='h-[22px] w-[22px]' aria-hidden />
      </div>
      <div className='min-w-0 flex-1'>
        <p className='m-0 text-base leading-snug font-semibold text-[#101828]'>
          {title}
        </p>
        <p className='m-0 mt-0.5 text-[13px] text-[#6a7282]'>{subtitle}</p>
      </div>
      {badge ? (
        <span className='rounded-lg bg-gradient-to-r from-[#155dfc] to-[#4f39f6] px-3 py-1 text-xs font-semibold text-white'>
          {badge}
        </span>
      ) : null}
      <img
        className='h-[22px] w-[22px]'
        src={figureAssets.chevron}
        alt=''
        aria-hidden
      />
    </button>
  );
}
