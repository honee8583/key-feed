import fireshipImage from '../../assets/explore/fireship.png'
import searchIcon from '../../assets/navigation/search_btn.png'
import sourceIcon from '../../assets/explore/source-icon.svg'
import techcrunchImage from '../../assets/explore/techcrunch.png'
import trendingIcon from '../../assets/explore/trending-icon.svg'
import vercelBlogImage from '../../assets/explore/vercel-blog.png'

type TrendingKeyword = {
  name: string
  contentCount: string
  changePercent: string
}

type RecommendedSource = {
  name: string
  type: string
  description: string
  image: string
}

const trendingKeywords: TrendingKeyword[] = [
  {
    name: 'ChatGPT',
    contentCount: '1240개 콘텐츠',
    changePercent: '+45%',
  },
  {
    name: 'Tailwind CSS',
    contentCount: '856개 콘텐츠',
    changePercent: '+32%',
  },
  {
    name: '블록체인',
    contentCount: '642개 콘텐츠',
    changePercent: '+28%',
  },
  {
    name: 'Flutter',
    contentCount: '523개 콘텐츠',
    changePercent: '+21%',
  },
]

const recommendedSources: RecommendedSource[] = [
  {
    name: 'Vercel Blog',
    type: '블로그',
    description: 'Next.js와 웹 개발 관련 최신 소식',
    image: vercelBlogImage,
  },
  {
    name: 'TechCrunch',
    type: '뉴스',
    description: '기술 스타트업과 혁신 소식',
    image: techcrunchImage,
  },
  {
    name: 'Fireship',
    type: '유튜브',
    description: '빠르고 재미있는 개발 튜토리얼',
    image: fireshipImage,
  },
]

export function ExplorePage() {
  return (
    <div className="min-h-screen bg-[radial-gradient(circle_at_15%_15%,rgba(255,255,255,0.08),transparent_55%),#050505] text-slate-50 font-['Inter','Noto_Sans_KR',system-ui,sans-serif]">
      <div className="w-full max-w-[440px] mx-auto pt-7 px-4 pb-24">
      <header className="bg-gradient-to-br from-[rgba(15,15,20,0.95)] to-[rgba(10,10,16,0.85)] border border-white/8 shadow-[0_18px_30px_rgba(2,6,23,0.4)] rounded-[28px] p-6 flex flex-col gap-4">
          <div className="flex items-center justify-between gap-3">
            <div className="inline-flex items-center gap-2.5">
              <img src={searchIcon} alt="" className="w-5 h-5 flex-shrink-0 opacity-80" aria-hidden="true" />
              <h1 className="m-0 text-[26px] tracking-[-0.02em] inline-flex items-center gap-2 text-slate-50">
                탐색
              </h1>
            </div>
           
          </div>
        </header>
        {/* Trending Keywords Section */}
        <section className="pt-4">
          <div className="flex items-center gap-2 mb-3">
            <img src={trendingIcon} alt="" className="w-5 h-5 flex-shrink-0 opacity-80" aria-hidden="true" />
            <h2 className="text-lg font-semibold text-white leading-[1.555em] tracking-[-0.024em]">트렌딩 키워드</h2>
          </div>
          <div className="grid grid-cols-2 gap-3">
            {trendingKeywords.map((keyword) => (
              <div
                key={keyword.name}
                className="flex flex-col gap-2 p-[17px] rounded-[10px] border border-[#FFD6A7]"
                style={{
                  background: 'linear-gradient(135deg, rgba(255, 247, 237, 1) 0%, rgba(254, 242, 242, 1) 100%)',
                }}
              >
                <div className="flex items-start justify-between gap-[24.41px]">
                  <h3 className="text-base font-medium text-[#101828] leading-[1.5em] tracking-[-0.02em] flex-1 min-w-0">
                    {keyword.name}
                  </h3>
                  <span className="flex items-center justify-center px-2 py-0.5 rounded-[6.8px] bg-[#F54900] text-white text-xs font-medium leading-[1.333em] whitespace-nowrap flex-shrink-0">
                    HOT
                  </span>
                </div>
                <p className="text-sm text-[#4A5565] leading-[1.429em] tracking-[-0.011em]">{keyword.contentCount}</p>
                <p className="text-xs text-[#F54900] leading-[1.333em]">{keyword.changePercent}</p>
              </div>
            ))}
          </div>
        </section>

        {/* Recommended Sources Section */}
        <section className="pt-[17px] border-t border-white/10 mt-[17px]">
          <div className="flex items-center gap-2 mb-3">
            <img src={sourceIcon} alt="" className="w-5 h-5 flex-shrink-0 opacity-80" aria-hidden="true" />
            <h2 className="text-lg font-semibold text-white leading-[1.555em] tracking-[-0.024em]">추천 소스</h2>
          </div>
          <div className="flex flex-col gap-3">
            {recommendedSources.map((source) => (
              <div
                key={source.name}
                className="rounded-[10px] border border-white/10 bg-white/5 backdrop-blur-sm p-px hover:border-white/20 transition-colors"
              >
                <div className="flex gap-3 p-3 rounded-[10px] bg-gradient-to-br from-white/8 to-white/3">
                  <img
                    src={source.image}
                    alt={source.name}
                    className="w-16 h-16 rounded-[10px] object-cover flex-shrink-0"
                  />
                  <div className="flex-1 min-w-0 flex flex-col">
                    <div className="flex items-center gap-2 mb-2">
                      <h3 className="text-base font-medium text-white leading-[1.5em] tracking-[-0.02em]">
                        {source.name}
                      </h3>
                      <span className="flex items-center justify-center px-2 py-0.5 rounded-[6.8px] border border-white/20 bg-white/10 text-white text-xs font-medium leading-[1.333em] whitespace-nowrap flex-shrink-0">
                        {source.type}
                      </span>
                    </div>
                    <p className="text-sm text-slate-300 leading-[1.429em] tracking-[-0.011em] mb-[15px]">{source.description}</p>
                    <button
                      type="button"
                      className="text-sm text-[#155DFC] leading-[1.429em] tracking-[-0.011em] text-left hover:underline focus:outline-none focus:underline w-fit"
                    >
                      + 추가하기
                    </button>
                  </div>
                </div>
              </div>
            ))}
          </div>
        </section>
      </div>
    </div>
  )
}
