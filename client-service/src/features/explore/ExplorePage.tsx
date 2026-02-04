import { useEffect, useState } from 'react'
import { sourceApi, type RecommendedSourceData } from '../../services/sourceApi'


import sourceIcon from '../../assets/explore/source-icon.svg'
import trendingIcon from '../../assets/explore/trending-icon.svg'
import profileImage from '../../assets/explore/fireship.png' // Placeholder

// Extend the types to support new visual data if needed
type TrendingKeyword = {
  id: number
  name: string
  contentCount: string
  changePercent: string
  theme: 'purple' | 'blue' | 'green' | 'orange'
}

type RecommendedSourceUI = {
  sourceId: number
  name: string
  type: string
  description: string
  subscribers: string
  image: string
  url: string
}

// Updated data with theme info to match design
const trendingKeywords: TrendingKeyword[] = [
  {
    id: 1,
    name: 'ChatGPT',
    contentCount: '1,240개',
    changePercent: '+45%',
    theme: 'purple',
  },
  {
    id: 2,
    name: 'Tailwind CSS',
    contentCount: '856개',
    changePercent: '+32%',
    theme: 'blue',
  },
]

export function ExplorePage() {
  const [recommendedSources, setRecommendedSources] = useState<RecommendedSourceUI[]>([])

  useEffect(() => {
    const fetchRecommended = async () => {
      try {
        const responseData = await sourceApi.getRecommended(10)
        const mappedSources = responseData.map((item: RecommendedSourceData) => {
           let hostname = item.url;
           try {
             hostname = new URL(item.url).hostname;
           } catch {
             // ignore invalid url
           }
           
          return {
            sourceId: item.sourceId,
            name: hostname,
            type: 'RSS', // Default type as API doesn't provide it
            description: item.url, // Use URL as description for now
            subscribers: `${item.subscriberCount}명 구독`,
            image: profileImage, // Default placeholder
            url: item.url
          }
        })
        setRecommendedSources(mappedSources)
      } catch (error) {
        console.error('Failed to fetch recommended sources:', error)
      }
    }

    fetchRecommended()
  }, [])

  return (
    <div className="min-h-screen bg-black text-white font-['Inter','Noto_Sans_KR',sans-serif] pb-24">
      <div className="w-full max-w-[440px] mx-auto pt-[60px] px-5 flex flex-col gap-8">
        
        {/* Header Section */}
        <header className="flex flex-col gap-4">
          <h1 className="text-[26px] font-bold tracking-[-0.02em] text-white">
            탐색
          </h1>
          <div className="relative">
             <div className="absolute left-4 top-1/2 -translate-y-1/2 w-5 h-5 flex items-center justify-center opacity-50">
               <svg width="20" height="20" viewBox="0 0 20 20" fill="none" xmlns="http://www.w3.org/2000/svg">
                 <path d="M9.16667 15.8333C12.8486 15.8333 15.8333 12.8486 15.8333 9.16667C15.8333 5.48477 12.8486 2.5 9.16667 2.5C5.48477 2.5 2.5 5.48477 2.5 9.16667C2.5 12.8486 5.48477 15.8333 9.16667 15.8333Z" stroke="#9CA3AF" strokeWidth="1.66667" strokeLinecap="round" strokeLinejoin="round"/>
                 <path d="M17.5 17.5L13.875 13.875" stroke="#9CA3AF" strokeWidth="1.66667" strokeLinecap="round" strokeLinejoin="round"/>
               </svg>
             </div>
            <input
              type="text"
              placeholder="키워드, 소스, 주제 검색..."
              className="w-full h-[52px] bg-[#1e2939] rounded-[16px] pl-[48px] pr-4 text-white placeholder-slate-400 border border-slate-700/50 focus:outline-none focus:border-blue-500 transition-colors text-[15px]"
            />
          </div>
        </header>

        {/* Trending Keywords Section */}
        <section>
          <div className="flex items-center gap-2 mb-4">
            <img src={trendingIcon} alt="" className="w-5 h-5" />
            <h2 className="text-lg font-bold tracking-tight">트렌딩 키워드</h2>
          </div>
          <div className="grid grid-cols-2 gap-3">
            {trendingKeywords.map((keyword) => (
              <div
                key={keyword.id}
                className={`relative overflow-hidden rounded-[20px] p-4 h-[120px] flex flex-col justify-between border ${
                  keyword.theme === 'purple'
                    ? 'bg-gradient-to-br from-[#2D1B4E] to-[#1A1025] border-purple-500/20'
                    : 'bg-gradient-to-br from-[#172554] to-[#0F172A] border-blue-500/20'
                }`}
              >
                {/* Background decorative glow */}
                <div className={`absolute -top-10 -right-10 w-32 h-32 rounded-full blur-[40px] opacity-30 ${
                   keyword.theme === 'purple' ? 'bg-purple-500' : 'bg-blue-500'
                }`} />
                
                <div className="flex items-start justify-between relative z-10">
                  <div className="w-6 h-6 rounded-full bg-white/10 flex items-center justify-center backdrop-blur-sm">
                    <span className="text-xs font-bold">{keyword.id}</span>
                  </div>
                  <div className="px-2 py-1 rounded-lg bg-white/10 backdrop-blur-sm border border-white/5">
                    <span className="text-[11px] font-medium">{keyword.changePercent}</span>
                  </div>
                </div>
                
                <div className="relative z-10">
                  <h3 className="font-semibold text-lg leading-tight mb-1">{keyword.name}</h3>
                  <p className="text-sm text-slate-400">{keyword.contentCount}</p>
                </div>
              </div>
            ))}
          </div>
        </section>

        {/* Recommended Sources Section */}
        <section>
          <div className="flex items-center justify-between mb-4">
            <div className="flex items-center gap-2">
              <img src={sourceIcon} alt="" className="w-5 h-5" />
              <h2 className="text-lg font-bold tracking-tight">추천 소스</h2>
            </div>
            <button className="text-sm text-slate-400 flex items-center gap-1">
              더보기
              <svg width="16" height="16" viewBox="0 0 16 16" fill="none" xmlns="http://www.w3.org/2000/svg">
                <path d="M6 12L10 8L6 4" stroke="currentColor" strokeWidth="1.5" strokeLinecap="round" strokeLinejoin="round"/>
              </svg>
            </button>
          </div>
          
          <div className="flex flex-col gap-3">
            {recommendedSources.map((source) => (
              <div
                key={source.sourceId}
                className="bg-[#111827] border border-[#1F2937] rounded-[20px] p-4 flex items-center gap-4"
              >
                <img
                  src={source.image}
                  alt={source.name}
                  className="w-[52px] h-[52px] rounded-[14px] object-cover bg-slate-800"
                />
                
                <div className="flex-1 min-w-0">
                  <div className="flex items-center gap-2 mb-0.5">
                    <h3 className="text-[15px] font-semibold text-white leading-tight truncate">
                      {source.name}
                    </h3>
                    <span className="px-1.5 py-0.5 rounded-[6px] bg-[#1F2937] border border-slate-700 text-[10px] text-slate-400 font-medium">
                      {source.type}
                    </span>
                  </div>
                  <p className="text-[13px] text-slate-400 truncate mb-1.5">
                    {source.description}
                  </p>
                  <p className="text-xs text-slate-500 font-medium tracking-wide">
                    {source.subscribers}
                  </p>
                </div>

                <button
                  type="button"
                  className="shrink-0 bg-[#3B82F6] hover:bg-[#2563EB] active:bg-[#1D4ED8] text-white text-[13px] font-medium py-1.5 px-3.5 rounded-full transition-colors"
                >
                  + 추가
                </button>
              </div>
            ))}
            {recommendedSources.length === 0 && (
                <div className="text-center text-slate-500 py-8">
                    추천 소스를 불러오는 중입니다...
                </div>
            )}
          </div>
        </section>
      </div>
    </div>
  )
}

