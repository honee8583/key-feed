import { useNavigate } from 'react-router-dom'
import { useAuth } from '../auth'
import {
  BarChartIcon,
  ChevronRightIcon,
  HelpCircleIcon,
  LogOutIcon,
  SettingsIcon,
  TrashIcon,
} from '../../components/common/Icons'
import { UserIcon } from '../auth/components/AuthIcons'
import sourceManagementIcon from '../../assets/profile/source_management_icon.png'
import notificationIcon from '../../assets/profile/notification_icon.png'
import securityIcon from '../../assets/profile/security_icon.png'
import generalIcon from '../../assets/profile/general_settings_icon.png'

type LinkItem = {
  id: string
  title: string
  icon: React.ReactNode
  badge?: string
  path?: string
  onClick?: () => void
}

export function ProfilePage() {
  const { user, logout } = useAuth()
  const navigate = useNavigate()

  const handleLogout = () => {
    if (window.confirm('정말 로그아웃하시겠어요?')) {
      logout()
    }
  }

  const handleDeleteAccount = () => {
    if (window.confirm('정말 계정을 삭제하시겠어요? 이 작업은 되돌릴 수 없습니다.')) {
      alert('계정 삭제 기능은 아직 구현되지 않았습니다.')
    }
  }

  const managementLinks: LinkItem[] = [
    {
      id: 'sources',
      title: '소스 관리',
      icon: <img src={sourceManagementIcon} alt="소스 관리" className="w-[40px] h-[40px] object-contain" />,
      path: '/profile/sources',
    },
  ]

  const settingLinks: LinkItem[] = [
    {
      id: 'notify',
      title: '알림 설정',
      icon: <img src={notificationIcon} alt="알림 설정" className="w-[40px] h-[40px] object-contain" />,
    },
    {
      id: 'security',
      title: '보안 설정',
      icon: <img src={securityIcon} alt="보안 설정" className="w-[40px] h-[40px] object-contain" />,
    },
    {
      id: 'general',
      title: '일반 설정',
      icon: <img src={generalIcon} alt="일반 설정" className="w-[40px] h-[40px] object-contain" />,
    },
  ]

  const infoLinks: LinkItem[] = [
    {
      id: 'help',
      title: '도움말',
      icon: <HelpCircleIcon className="w-[35px] h-[35px]" />,
    },
    {
      id: 'stats',
      title: '통계',
      icon: <BarChartIcon className="w-[35px] h-[35px]" />,
      badge: 'NEW',
    },
  ]

  return (
    <div className="min-h-screen bg-black flex justify-center py-8 px-5 pb-[120px] font-['Pretendard','Noto_Sans_KR',system-ui,sans-serif]">
      <div className="w-full max-w-[393px] flex flex-col gap-6">
        
        {/* Unified Header Card */}
        <div className="flex flex-col gap-6 p-6 rounded-[24px] bg-[#101828] border border-white/5 shadow-xl relative overflow-hidden">
           {/* Background Decoration */}
           <div className="absolute top-0 right-0 w-[200px] h-[200px] bg-blue-500/5 rounded-full blur-[60px] translate-x-1/2 -translate-y-1/2 pointer-events-none"></div>

          {/* Top Section: User Info */}
          <div className="flex items-center gap-4 relative z-10">
            <div className="w-[64px] h-[64px] rounded-[20px] bg-gradient-to-br from-[#2b7fff] to-[#4f39f6] flex items-center justify-center text-white shadow-lg">
              <UserIcon className="w-8 h-8 opacity-90" />
            </div>
            <div className="flex-1 min-w-0">
              <div className="flex items-center gap-2 mb-1">
                <h2 className="m-0 text-[20px] font-bold text-white tracking-tight">
                  {user?.name ?? '사용자'}
                </h2>
                <span className="px-1.5 py-0.5 rounded-[6px] bg-white/10 border border-white/10 text-[10px] font-bold text-blue-200">
                  PRO
                </span>
              </div>
              <p className="m-0 text-[13px] text-slate-400 truncate font-medium">
                {user?.email ?? 'user@example.com'}
              </p>
            </div>
            <button
              type="button"
              className="w-10 h-10 rounded-[14px] bg-[#1a2436] border border-white/5 flex items-center justify-center text-slate-400 hover:text-white hover:bg-[#252f42] transition-colors cursor-pointer"
            >
              <SettingsIcon className="w-5 h-5" />
            </button>
          </div>

          {/* Divider */}
          <div className="h-px w-full bg-white/5"></div>

          {/* Bottom Section: KPIs */}
          <div className="grid grid-cols-3 gap-2 relative z-10">
            <div className="flex flex-col items-center gap-1">
              <span className="text-[18px] font-bold text-white">12</span>
              <span className="text-[12px] text-slate-400">구독 소스</span>
            </div>
            <div className="flex flex-col items-center gap-1 border-l border-white/5">
              <span className="text-[18px] font-bold text-white">8</span>
              <span className="text-[12px] text-slate-400">폴더</span>
            </div>
             <div className="flex flex-col items-center gap-1 border-l border-white/5">
              <span className="text-[18px] font-bold text-white">40</span>
              <span className="text-[12px] text-slate-400">저장됨</span>
            </div>
          </div>
        </div>

        {/* Links Sections */}
        <div className="flex flex-col gap-6">
          <ProfileSection title="관리">
            {managementLinks.map((item) => (
              <ProfileListItem
                key={item.id}
                {...item}
                onClick={item.path ? () => navigate(item.path!) : undefined}
              />
            ))}
          </ProfileSection>

          <ProfileSection title="설정">
            {settingLinks.map((item) => (
              <ProfileListItem key={item.id} {...item} />
            ))}
          </ProfileSection>

          <ProfileSection title="정보">
            {infoLinks.map((item) => (
              <ProfileListItem key={item.id} {...item} />
            ))}
          </ProfileSection>
        </div>

        {/* Footer Actions */}
        <div className="flex flex-col gap-3 mt-2">
          <button
            onClick={handleLogout}
            className="w-full h-[56px] rounded-[16px] bg-[#101828] border border-white/5 flex items-center justify-center gap-2 text-[15px] font-semibold text-slate-300 hover:bg-[#1a2436] hover:text-white transition-colors cursor-pointer"
          >
            <LogOutIcon className="w-4 h-4" />
            <span>로그아웃</span>
          </button>
          
          <button
            onClick={handleDeleteAccount}
            className="w-full h-[56px] rounded-[16px] bg-[#101828] border border-white/5 flex items-center justify-center gap-2 text-[15px] font-semibold text-rose-500 hover:bg-rose-500/10 transition-colors cursor-pointer"
          >
            <TrashIcon className="w-4 h-4" />
            <span>계정 삭제</span>
          </button>
        </div>

        <p className="text-center text-[12px] text-slate-600 font-medium py-2">버전 1.0.0</p>
      </div>
    </div>
  )
}

function ProfileSection({ title, children }: { title: string; children: React.ReactNode }) {
  return (
    <div className="flex flex-col gap-3">
      <h3 className="m-0 px-1 text-[13px] font-bold text-slate-500 uppercase tracking-wider">{title}</h3>
      <div className="flex flex-col gap-0 bg-[#101828] border border-white/5 rounded-[20px] overflow-hidden shadow-sm">
        {children}
      </div>
    </div>
  )
}

function ProfileListItem({
  title,
  icon,
  badge,
  onClick,
}: LinkItem) {
  return (
    <button
      type="button"
      onClick={onClick}
      className="flex items-center gap-4 w-full p-4 text-left border-b border-white/5 last:border-b-0 hover:bg-white/5 transition-colors cursor-pointer group"
    >
      <div className="flex items-center justify-center text-slate-400 group-hover:text-white transition-colors">
        {icon}
      </div>
      <div className="flex-1 min-w-0">
        <div className="flex items-center gap-2">
          <span className="text-[15px] font-medium text-white">{title}</span>
          {badge && (
            <span className="px-1.5 py-0.5 rounded-[6px] bg-blue-500/20 text-blue-400 text-[10px] font-bold">
              {badge}
            </span>
          )}
        </div>
      </div>
      <ChevronRightIcon className="w-5 h-5 text-slate-600 group-hover:text-slate-400 transition-colors" />
    </button>
  )
}
