import { useEffect, useState } from 'react'
import { bookmarkApi, type BookmarkFolderDto } from '../../services/bookmarkApi'
import { BookmarkFolderIcon } from './BookmarkFolderIcon'
import { type ColorType, type IconType } from './constants'
import { FolderManagementModal } from './FolderManagementModal'

type FolderSelectSheetProps = {
  isOpen: boolean
  onClose: () => void
  onSelectFolder: (folderId: number) => void
  folders: BookmarkFolderDto[]
  currentFolderId?: number | null
  onFolderCreated?: () => void
}

const DEFAULT_FOLDERS: BookmarkFolderDto[] = [
  { folderId: 0, name: '미분류', color: '#6a7282', icon: 'folder' },
]

export function FolderSelectSheet({ 
  isOpen, 
  onClose, 
  onSelectFolder, 
  folders, 
  currentFolderId,
  onFolderCreated
}: FolderSelectSheetProps) {
  const [isClosing, setIsClosing] = useState(false)
  const [selectedId, setSelectedId] = useState<number>(currentFolderId ?? 0)
  const [showCreateModal, setShowCreateModal] = useState(false)

  const handleCreateNewFolder = async (name: string, icon: IconType, color: ColorType) => {
    await bookmarkApi.createFolder({ name, icon, color })
    onFolderCreated?.()
  }



  const handleClose = () => {
    setIsClosing(true)
    setTimeout(() => {
      setIsClosing(false)
      onClose()
    }, 200)
  }

  const handleComplete = () => {
    setIsClosing(true)
    setTimeout(() => {
      setIsClosing(false)
      onSelectFolder(selectedId)
    }, 200)
  }

  useEffect(() => {
    if (!isOpen) return
    document.body.style.overflow = 'hidden'
    return () => {
      document.body.style.overflow = ''
    }
  }, [isOpen])

  if (!isOpen) return null

  // Merge default "Uncategorized" folder with user folders
  const allFolders = [...DEFAULT_FOLDERS, ...folders]

  return (
    <div
      className="fixed inset-0 z-[60] flex items-end justify-center"
      role="dialog"
      aria-modal
    >
      <div 
        className={`absolute inset-0 bg-black/50 backdrop-blur-[2px] z-[1] transition-opacity duration-200 ${isClosing ? 'opacity-0' : 'opacity-100'}`}
        onClick={handleClose} 
      />
      <div 
        className={`relative z-[2] w-full max-w-[478px] bg-[#1e2939] rounded-t-[24px] overflow-hidden flex flex-col h-[625px] max-h-[85vh]
        ${isClosing ? 'animate-[sheet-slide-down_200ms_ease-in_forwards]' : 'animate-[sheet-slide-up_240ms_ease-out]'}`}
      >
        {/* Header */}
        <div className="flex justify-between items-center p-5 pb-2 shrink-0">
          <h2 className="text-[18px] font-bold text-slate-50 m-0">폴더 선택</h2>
          <button
            type="button"
            className="w-8 h-8 flex items-center justify-center rounded-full bg-slate-800 text-slate-400 hover:text-white transition-colors"
            onClick={handleClose}
          >
            <svg width="24" height="24" viewBox="0 0 24 24" fill="none">
              <path d="M6 18L18 6M6 6L18 18" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round"/>
            </svg>
          </button>
        </div>

        {/* Scrollable Content */}
        <div className="flex-1 overflow-y-auto p-4 pt-2 flex flex-col gap-2">
          {/* Create New Folder Item */}
          <button
            type="button"
            className="flex items-center gap-3 p-3 rounded-xl hover:bg-white/5 transition-colors text-left group shrink-0"
            onClick={() => setShowCreateModal(true)}
          >
             <div className="w-10 h-10 rounded-lg bg-white/5 flex items-center justify-center shrink-0 text-slate-400">
               <PlusIcon />
             </div>
             <div className="flex-1">
               <p className="text-[15px] font-medium text-slate-200 group-hover:text-white transition-colors">
                 새 폴더 추가
               </p>
             </div>
          </button>

          <FolderManagementModal
            isOpen={showCreateModal}
            onClose={() => setShowCreateModal(false)}
            onSubmit={handleCreateNewFolder}
            zIndex={70}
          />

          {/* Folder List */}
          {allFolders.map((folder) => {
            const isSelected = selectedId === folder.folderId
            return (
              <button
                key={folder.folderId}
                type="button"
                className={`flex items-center gap-3 p-3 rounded-xl transition-colors text-left group shrink-0 ${isSelected ? 'bg-white/10' : 'hover:bg-white/5'}`}
                onClick={() => setSelectedId(folder.folderId)}
              >
                <div 
                  className="w-10 h-10 rounded-lg flex items-center justify-center shrink-0"
                  style={{ 
                    backgroundColor: `${folder.color || '#6a7282'}20`,
                    color: folder.color || '#6a7282'
                  }}
                >
                  <BookmarkFolderIcon icon={folder.icon} width={20} height={20} />
                </div>
                <div className="flex-1">
                  <p className={`text-[15px] font-medium transition-colors ${isSelected ? 'text-white' : 'text-slate-200'}`}>
                    {folder.name}
                  </p>
                </div>
                
                {/* Checkmark for selection */}
                <div className={`w-6 h-6 rounded-full border flex items-center justify-center transition-colors
                  ${isSelected ? 'bg-blue-500 border-blue-500 text-white' : 'border-slate-600'}`}>
                   {isSelected && (
                     <svg width="14" height="14" viewBox="0 0 14 14" fill="none">
                       <path d="M2.5 7L5.5 10L11.5 4" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round"/>
                     </svg>
                   )}
                </div>
              </button>
            )
          })}
        </div>
        
        {/* Footer Actions */}
        <div className="p-4 border-t border-white/5 bg-[#1e2939] shrink-0 flex flex-col gap-3 pb-8">
           <button
             type="button"
             className="w-full h-[52px] rounded-[14px] bg-[#3b82f6] text-white text-[16px] font-bold hover:bg-[#2563eb] transition-colors"
             onClick={handleComplete}
           >
             선택 완료
           </button>
           <button
             type="button"
             className="w-full h-[52px] rounded-[14px] border border-white/10 bg-white/5 text-[#d1d5dc] text-[16px] font-medium hover:bg-white/10 transition-colors"
             onClick={handleClose}
           >
             취소
           </button>
        </div>

      </div>
    </div>
  )
}



function PlusIcon() {
  return (
    <svg width="20" height="20" viewBox="0 0 24 24" fill="none">
       <path d="M12 5V19M5 12H19" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round"/>
    </svg>
  )
}
