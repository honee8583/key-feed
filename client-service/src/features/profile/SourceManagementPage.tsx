import { useEffect, useState } from 'react'
import { useNavigate } from 'react-router-dom'
import toast from 'react-hot-toast'
import { useDebounce } from '../../hooks/useDebounce'
import { ToggleSwitch } from '../../components/ToggleSwitch'
import { sourceApi, type CreatedSource } from '../../services/sourceApi'
import {
  ArrowLeftIcon,
  ExternalLinkIcon,
  PlusIcon,
  RefreshCwIcon,
  SearchIcon,
  TrendingUpIcon,
  AlertCircleIcon,
} from '../../components/common/Icons'
import { AddSourceSheet } from '../home/components/AddSourceSheet'
import { formatRelativePublishedAt } from '../../utils/dateUtils'
import trashIcon from '../../assets/profile/trash_icon.svg'

type ManagedSource = CreatedSource & {
  tags: string[]
}

export function SourceManagementPage() {
  const navigate = useNavigate()
  const [items, setItems] = useState<ManagedSource[]>([])
  const [isLoading, setIsLoading] = useState(true)
  const [error, setError] = useState<string | null>(null)
  const [searchKeyword, setSearchKeyword] = useState('')
  const debouncedKeyword = useDebounce(searchKeyword, 500)
  const [isAddSourceOpen, setIsAddSourceOpen] = useState(false)
  const [refreshTrigger, setRefreshTrigger] = useState(0)
  const [togglingId, setTogglingId] = useState<number | null>(null)

  useEffect(() => {
    let cancelled = false

    const fetchSources = async () => {
      setIsLoading(true)
      setError(null)
      try {
        let response
        if (debouncedKeyword) {
          response = await sourceApi.searchMy(debouncedKeyword)
        } else {
          response = await sourceApi.listMy()
        }

        if (cancelled) return
        setItems((response || []).map(mapSourceToCard))
      } catch (fetchError) {
        if (cancelled) return
        setError(
          fetchError instanceof Error
            ? fetchError.message
            : '소스를 불러오지 못했습니다.'
        )
      } finally {
        if (!cancelled) {
          setIsLoading(false)
        }
      }
    }

    void fetchSources()

    return () => {
      cancelled = true
    }
  }, [debouncedKeyword, refreshTrigger])

  const handleToggle = async (userSourceId: number) => {
    setTogglingId(userSourceId)
    try {
      const updated = await sourceApi.toggleReceiveFeed(userSourceId)
      setItems((prev) =>
        prev.map((item) =>
          item.userSourceId === userSourceId
            ? { ...item, receiveFeed: updated.receiveFeed }
            : item
        )
      )
    } catch (toggleError) {
      toast.error(
        toggleError instanceof Error
          ? toggleError.message
          : '피드 수신 설정 변경에 실패했습니다.',
          { duration: 3000 }
      )
    } finally {
      setTogglingId(null)
    }
  }

  const handleDelete = (userSourceId: number) => {
    toast.custom((t) => (
      <div
        className={`${
          t.visible ? 'animate-enter' : 'animate-leave'
        } max-w-md w-full bg-[#1e2939] shadow-lg rounded-2xl pointer-events-auto flex flex-col border border-white/10`}
        style={{
          animation: t.visible 
            ? '0.35s cubic-bezier(0.21, 1.02, 0.73, 1) forwards enter-animation' 
            : '0.23s forwards leave-animation'
        }}
      >
        <div className="p-4 flex flex-col gap-3">
          <div className="flex items-center gap-3">
             <div className="w-10 h-10 rounded-full bg-red-500/10 flex items-center justify-center shrink-0">
               <AlertCircleIcon className="w-6 h-6 text-red-500" />
             </div>
             <div className="flex-1 min-w-0">
               <h3 className="text-[15px] font-bold text-white mb-0.5">소스를 삭제하시겠습니까?</h3>
               <p className="text-[13px] text-slate-400 leading-snug">
                 삭제된 소스는 복구할 수 없습니다.
               </p>
             </div>
          </div>
        </div>
        <div className="flex border-t border-white/10">
          <button
            onClick={() => toast.dismiss(t.id)}
            className="flex-1 px-4 py-3 text-sm font-medium text-slate-300 hover:text-white hover:bg-white/5 transition-colors first:rounded-bl-2xl"
          >
            취소
          </button>
          <div className="w-[1px] bg-white/10" />
          <button
             onClick={() => {
                toast.dismiss(t.id)
                
                // Allow UI to update (close toast) before starting async operation
                setTimeout(async () => {
                    // Global dismiss backup not needed inside here if we trust specific ID default
                    
                    try {
                      await sourceApi.delete(userSourceId)
                      setItems(prev => prev.filter(item => item.userSourceId !== userSourceId))
                      
                      const successId = toast.success('소스가 삭제되었습니다', {
                          duration: 3000,
                          id: `src-del-success-${Date.now()}`,
                          style: {
                            background: '#1e2939',
                            color: '#fff',
                            borderRadius: '16px',
                            border: '1px solid rgba(255, 255, 255, 0.1)',
                          },
                          iconTheme: {
                            primary: '#fff',
                            secondary: '#1e2939',
                          },
                      })
                      
                      setTimeout(() => {
                        toast.dismiss(successId)
                      }, 3000)

                    } catch (error) {
                       console.error(error)
                       const errorId = toast.error('삭제에 실패했습니다', {
                          duration: 3000,
                          id: `src-del-error-${Date.now()}`,
                          style: {
                            background: '#1e2939',
                            color: '#ff4b4b',
                            borderRadius: '16px',
                            border: '1px solid rgba(255, 255, 255, 0.1)',
                          }
                       })
                       
                       setTimeout(() => {
                        toast.dismiss(errorId)
                       }, 3000)
                    }
                }, 0)
             }}
            className="flex-1 px-4 py-3 text-sm font-bold text-red-500 hover:bg-red-500/10 transition-colors last:rounded-br-2xl"
          >
            삭제
          </button>
        </div>
      </div>
    ), {
      duration: Infinity,
      position: 'top-center',
    })
  }

  return (
    <div className='min-h-screen bg-[#050b16] px-5 py-4 text-white font-["Pretendard"] pb-[100px]'>
      {/* Header */}
      <header className='flex items-center justify-between mb-6'>
        <button
          onClick={() => navigate(-1)}
          className='text-white p-1'
          aria-label="뒤로 가기"
        >
          <ArrowLeftIcon className="w-6 h-6" />
        </button>
        <div className='flex flex-col items-center'>
          <h1 className='text-[16px] font-bold leading-tight'>소스 관리</h1>
          <p className='text-[12px] text-[#64748b] leading-tight mt-0.5'>구독 중인 콘텐츠 소스</p>
        </div>
        <button
          className='w-10 h-10 rounded-full bg-[#3b82f6] flex items-center justify-center shadow-[0_4px_12px_rgba(59,130,246,0.4)] hover:bg-[#2563eb] transition-colors'
          aria-label="새 소스 추가"
          onClick={() => setIsAddSourceOpen(true)}
        >
          <PlusIcon className="w-5 h-5 text-white" />
        </button>
      </header>

      {/* Search Bar */}
      <div className='relative mb-6'>
        <div className='absolute left-4 top-1/2 -translate-y-1/2 text-[#64748b]'>
          <SearchIcon className="w-5 h-5" />
        </div>
        <input
          type='search'
          placeholder='소스 이름 또는 URL 검색...'
          className='w-full h-[48px] bg-[#1e293b] rounded-[16px] pl-11 pr-4 text-[14px] text-white placeholder:text-[#64748b] focus:outline-none focus:ring-1 focus:ring-[#3b82f6]'
          value={searchKeyword}
          onChange={(e) => setSearchKeyword(e.target.value)}
        />
      </div>

      {/* Source List */}
      <div className='flex flex-col gap-3'>
        {isLoading ? (
          <p className='text-center text-[#64748b] py-8'>로딩 중...</p>
        ) : error ? (
          <p className='text-center text-red-400 py-8'>{error}</p>
        ) : items.length === 0 ? (
          <p className='text-center text-[#64748b] py-8'>연결된 소스가 없습니다.</p>
        ) : (
          items.map((source) => (
            <div
              key={source.userSourceId}
              className='bg-[#0f172a] rounded-[20px] p-5 border border-[#1e293b]'
            >
              <div className='flex justify-between items-start mb-1'>
                <h3 className='text-[16px] font-bold text-white max-w-[70%] truncate'>
                  {source.userDefinedName}
                </h3>
              </div>

              <div className='flex items-center gap-1.5 mb-4'>
                <ExternalLinkIcon className="w-3.5 h-3.5 text-[#64748b]" />
                <a
                  href={source.url}
                  target="_blank"
                  rel="noreferrer"
                  className='text-[13px] text-[#64748b] truncate hover:text-[#94a3b8] transition-colors'
                >
                  {source.url}
                </a>
              </div>

              <div className='flex items-center justify-between'>
                <div className='flex items-center gap-1.5 text-[#64748b]'>
                  <RefreshCwIcon className="w-3.5 h-3.5" />
                  <span className='text-[12px]'>
                    {source.lastCrawledAt ? formatRelativePublishedAt(source.lastCrawledAt) : '아직 수집되지 않음'}
                  </span>
                </div>

                <div className='flex items-center gap-3'>
                  <button
                    onClick={() => handleDelete(source.userSourceId)}
                    className='text-[#64748b] hover:text-red-400 transition-colors p-1'
                  >
                    <img src={trashIcon} alt="삭제" className="w-5 h-5 opacity-70 hover:opacity-100" />
                  </button>
                  <ToggleSwitch
                    active={source.receiveFeed}
                    ariaLabel={source.receiveFeed ? '피드 수신 중' : '피드 수신 중지'}
                    onToggle={() => handleToggle(source.userSourceId)}
                    disabled={togglingId === source.userSourceId}
                  />
                </div>
              </div>
            </div>
          ))
        )}
      </div>

      {/* Tip Section */}
      <div className='mt-6 bg-[#0f172a]/50 rounded-[20px] p-5 border border-[#1e293b]/50'>
        <div className='flex gap-4'>
          <div className='w-10 h-10 rounded-full bg-[#1e293b] flex items-center justify-center flex-shrink-0'>
            <TrendingUpIcon className="w-5 h-5 text-[#3b82f6]" />
          </div>
          <div>
            <h4 className='text-[14px] font-bold text-white mb-1'>소스 관리 팁</h4>
            <p className='text-[12px] text-[#94a3b8] leading-relaxed'>
              비활성화된 소스는 새 콘텐츠를 가져오지 않습니다.
              필요 없는 소스는 삭제하여 피드를 깔끔하게 유지하세요.
            </p>
          </div>
        </div>
      </div>

      <AddSourceSheet
        isOpen={isAddSourceOpen}
        onClose={() => setIsAddSourceOpen(false)}
        onSubmit={() => setRefreshTrigger((prev) => prev + 1)}
      />
    </div>
  )
}

function mapSourceToCard(source: CreatedSource): ManagedSource {
  const hostname = safeHostname(source.url)
  return {
    ...source,
    tags: [hostname],
  }
}

function safeHostname(url: string) {
  try {
    return new URL(url).hostname.replace('www.', '')
  } catch {
    return url
  }
}
