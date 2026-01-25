import { useEffect, useState } from 'react'
import { TagIcon, TrashIcon, PlusIcon } from '../../../components/common/Icons'
import { keywordApi, type Keyword } from '../../../services/keywordApi'

type KeywordManagementModalProps = {
  isOpen: boolean
  onClose: () => void
}

export function KeywordManagementModal({ isOpen, onClose }: KeywordManagementModalProps) {
  const [keywords, setKeywords] = useState<Keyword[]>([])
  const [inputText, setInputText] = useState('')
  const [isLoading, setIsLoading] = useState(false)
  const [isSubmitting, setIsSubmitting] = useState(false)

  // Load keywords when modal opens
  // Load keywords when modal opens
  useEffect(() => {
    if (isOpen) {
      loadKeywords()
      document.body.style.overflow = 'hidden'
    } else {
      document.body.style.overflow = 'unset'
    }
    
    return () => {
      document.body.style.overflow = 'unset'
    }
  }, [isOpen])

  const loadKeywords = async () => {
    setIsLoading(true)
    try {
      const data = await keywordApi.getKeywords()
      setKeywords(data)
    } catch (error) {
      console.error('Failed to load keywords', error)
    } finally {
      setIsLoading(false)
    }
  }

  const handleAddKeyword = async () => {
    const text = inputText.trim()
    if (!text) return
    
    // Prevent duplicates
    if (keywords.some(k => k.name.toLowerCase() === text.toLowerCase())) {
        alert('이미 등록된 키워드입니다.')
        return
    }

    setIsSubmitting(true)
    try {
      const newKeyword = await keywordApi.addKeyword(text)
      setKeywords(prev => [newKeyword, ...prev])
      setInputText('')
    } catch (error) {
      console.error('Failed to add keyword', error)
      alert('키워드 추가에 실패했습니다.')
    } finally {
      setIsSubmitting(false)
    }
  }

  const handleDeleteKeyword = async (id: number) => {
    if (!window.confirm('이 키워드를 삭제하시겠습니까?')) return

    try {
      await keywordApi.deleteKeyword(id)
      setKeywords(prev => prev.filter(k => k.keywordId !== id))
    } catch (error) {
      console.error('Failed to delete keyword', error)
      alert('삭제에 실패했습니다.')
    }
  }

  if (!isOpen) return null

  return (
    <div className="fixed inset-0 z-50 flex items-end justify-center sm:items-center">
      <div className="absolute inset-0 bg-black/80 backdrop-blur-sm" onClick={onClose} />
      
      <div className="relative w-full max-w-[440px] h-[80vh] sm:h-auto sm:max-h-[80vh] bg-black border-t sm:border border-[#1e2939] rounded-t-[24px] sm:rounded-[24px] flex flex-col overflow-hidden animate-slide-up sm:animate-fade-in shadow-2xl">
        
        {/* Header */}
        <div className="flex items-center justify-between p-6 pb-4 shrink-0">
          <div className="flex items-center gap-3">
             <div className="w-12 h-12 rounded-[16px] bg-gradient-to-br from-[#2b7fff]/20 to-[#ad46ff]/20 flex items-center justify-center">
               <TagIcon className="w-6 h-6 text-[#7B61FF]" />
             </div>
             <h2 className="text-[20px] font-bold text-white tracking-tight">키워드 관리</h2>
          </div>
          <button 
            onClick={onClose}
            className="p-2 text-slate-400 hover:text-white transition-colors cursor-pointer"
          >
            <svg width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
              <line x1="18" y1="6" x2="6" y2="18"></line>
              <line x1="6" y1="6" x2="18" y2="18"></line>
            </svg>
          </button>
        </div>

        {/* Content */}
        <div className="flex-1 overflow-y-auto px-6 py-2 flex flex-col gap-6">
          
          {/* Input */}
          <div className="relative shrink-0">
            <div className="absolute left-4 top-1/2 -translate-y-1/2 text-slate-500">
              <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
                <circle cx="11" cy="11" r="8"></circle>
                <line x1="21" y1="21" x2="16.65" y2="16.65"></line>
              </svg>
            </div>
            <input
              type="text"
              value={inputText}
              onChange={(e) => setInputText(e.target.value)}
              onKeyDown={(e) => e.key === 'Enter' && handleAddKeyword()}
              placeholder="새 키워드를 입력하세요..."
              className="w-full h-[52px] bg-[#1e2939] border border-[#344054] rounded-[16px] pl-11 pr-14 text-white placeholder-slate-500 focus:outline-none focus:border-[#7B61FF] transition-colors"
            />
            <button
              onClick={handleAddKeyword}
              disabled={!inputText.trim() || isSubmitting}
              className="absolute right-2 top-1/2 -translate-y-1/2 w-9 h-9 bg-[#3B82F6] hover:bg-[#2563EB] disabled:opacity-50 disabled:bg-slate-600 rounded-[12px] flex items-center justify-center text-white transition-colors"
            >
              <PlusIcon className="w-5 h-5" />
            </button>
          </div>

          {/* List */}
          <div className="flex flex-col gap-3 min-h-[200px]">
            {isLoading ? (
               <div className="flex items-center justify-center h-[100px] text-slate-500">
                 불러오는 중...
               </div>
            ) : keywords.length === 0 ? (
               <div className="flex flex-col items-center justify-center h-[160px] text-slate-500 gap-2">
                 <TagIcon className="w-8 h-8 opacity-30" />
                 <p className="text-sm">등록된 키워드가 없습니다.</p>
               </div>
            ) : (
                keywords.map((keyword) => (
                  <div 
                    key={keyword.keywordId}
                    className="w-full h-[72px] bg-[#101828] border border-[#1e2939] rounded-[20px] px-5 flex items-center justify-between group hover:border-[#3B82F6]/50 transition-colors"
                  >
                    <div>
                      <h3 className="text-[16px] font-semibold text-white mb-0.5">{keyword.name}</h3>
                      <div className="flex items-center gap-1.5">
                        <div className={`w-1.5 h-1.5 rounded-full ${keyword.isNotificationEnabled ? 'bg-[#3B82F6]' : 'bg-slate-600'}`} />
                        <span className="text-[12px] text-slate-400">{keyword.isNotificationEnabled ? '알림 켜짐' : '알림 꺼짐'}</span>
                      </div>
                    </div>
                    <button
                      onClick={() => handleDeleteKeyword(keyword.keywordId)}
                      className="w-9 h-9 rounded-full hover:bg-white/5 flex items-center justify-center text-slate-500 hover:text-[#FB2C36] transition-colors group-hover:opacity-100"
                      aria-label="삭제"
                    >
                      <TrashIcon className="w-5 h-5" />
                    </button>
                  </div>
                ))
            )}
          </div>
        </div>

        {/* Footer */}
        <div className="p-6 pt-4 border-t border-[#1e2939] bg-black/50 backdrop-blur-md">
          <button
            onClick={onClose}
            className="w-full h-[52px] bg-white hover:bg-slate-200 rounded-[16px] text-black text-[16px] font-bold tracking-[-0.3px] transition-colors"
          >
            완료
          </button>
        </div>

      </div>
    </div>
  )
}
