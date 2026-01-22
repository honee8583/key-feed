import { useEffect, useState } from 'react';
import { useDebounce } from '../../hooks/useDebounce';
import { ToggleSwitch } from '../../components/ToggleSwitch';
import { sourceApi, type CreatedSource } from '../../services/sourceApi';



type ManagedSource = CreatedSource & {
  status: 'active' | 'paused';
  type: string;
  frequency: string;
  tags: string[];
};

export function SourceManagementPage() {
  const [items, setItems] = useState<ManagedSource[]>([]);
  const [isLoading, setIsLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [searchKeyword, setSearchKeyword] = useState('');
  const debouncedKeyword = useDebounce(searchKeyword, 500);

  useEffect(() => {
    let cancelled = false;

    const fetchSources = async () => {
      setIsLoading(true);
      setError(null);
      try {
        let response;
        if (debouncedKeyword) {
          response = await sourceApi.searchMy(debouncedKeyword);
        } else {
          response = await sourceApi.listMy();
        }
        
        if (cancelled) return;
        setItems((response || []).map(mapSourceToCard));
      } catch (fetchError) {
        if (cancelled) return;
        setError(
          fetchError instanceof Error
            ? fetchError.message
            : '소스를 불러오지 못했습니다.'
        );
      } finally {
        if (cancelled) return;
        setIsLoading(false);
      }
    };

    void fetchSources();

    return () => {
      cancelled = true;
    };
  }, [debouncedKeyword]);

  const handleToggle = (userSourceId: number) => {
    setItems((prev) =>
      prev.map((item) =>
        item.userSourceId === userSourceId
          ? {
              ...item,
              status: item.status === 'active' ? 'paused' : 'active',
            }
          : item
      )
    );
  };

  return (
    <div className='relative flex min-h-screen justify-center overflow-hidden bg-[radial-gradient(circle_at_top,#050b16_50%,#03050a_80%)] px-4 py-8 pb-40 text-white'>
      <div
        className='absolute top-[-120px] left-10 h-[420px] w-[420px] rounded-full bg-[rgba(81,162,255,0.6)] opacity-30 blur-[140px]'
        aria-hidden
      />
      <div
        className='absolute right-[-160px] bottom-[60px] h-[480px] w-[480px] rounded-full bg-[rgba(194,122,255,0.4)] opacity-30 blur-[140px]'
        aria-hidden
      />
      <div className='relative z-10 flex w-full max-w-[378px] flex-col gap-6'>
        <header className='flex flex-col gap-4'>
          <div>
            <p className='m-0 text-sm tracking-[0.12em] text-white/60 uppercase'>
              소스 관리
            </p>
            <h1 className='m-0 text-[32px] font-bold'>내 콘텐츠 허브</h1>
            <p className='m-0 leading-relaxed text-[#cdd6ff]'>
              구독 중인 채널을 정리하고 새 소스를 연결하세요.
            </p>
          </div>
          <button
            type='button'
            className='cursor-pointer self-start rounded-[18px] border-none bg-gradient-to-r from-[#155dfc] to-[#4f39f6] px-5 py-3.5 font-semibold text-white shadow-[0_15px_30px_rgba(21,93,252,0.4)] hover:opacity-90'
          >
            + 새 소스 추가
          </button>
        </header>

        <div className='flex gap-2.5 max-[420px]:flex-col'>
          <input
            type='search'
            placeholder='소스 이름 또는 URL 검색'
            aria-label='소스 검색'
            className='flex-1 rounded-[18px] border border-white/20 bg-white/8 px-4 py-3 text-white placeholder:text-white/60 focus:ring-2 focus:ring-white/30 focus:outline-none'
            value={searchKeyword}
            onChange={(e) => setSearchKeyword(e.target.value)}
          />
        </div>

        <section className='flex flex-col gap-4' aria-label='연결된 소스 목록'>
          {isLoading ? (
            <p className='m-0 rounded-[18px] bg-white/8 p-[18px] text-center text-[#c8d6ff]'>
              연결된 소스를 불러오는 중...
            </p>
          ) : null}
          {error ? (
            <p className='m-0 rounded-[18px] bg-white/8 p-[18px] text-center text-[#ffb4b4]'>
              {error}
            </p>
          ) : null}
          {!isLoading && !error && items.length === 0 ? (
            <p className='m-0 rounded-[18px] bg-white/8 p-[18px] text-center text-[#c8d6ff]'>
              아직 연결된 소스가 없어요. 새 소스를 추가해보세요.
            </p>
          ) : null}
          {items.map((source) => (
            <article
              key={source.userSourceId}
              className='rounded-[20px] border border-slate-200/50 bg-white/92 p-[18px] text-[#101828] shadow-[0_25px_45px_rgba(15,23,42,0.3)]'
            >
              <header className='mb-2.5 flex justify-between gap-3'>
                <div>
                  <p className='m-0 text-lg font-semibold'>
                    {source.userDefinedName}
                  </p>
                  <p className='m-0 mt-1 text-[13px] text-[#6a7282]'>
                    {source.type} · {source.frequency}
                  </p>
                </div>
                <ToggleSwitch
                  active={source.status === 'active'}
                  ariaLabel={
                    source.status === 'active' ? '활성화됨' : '비활성화됨'
                  }
                  onToggle={() => handleToggle(source.userSourceId)}
                />
              </header>
              <p className='mt-2.5 mb-0 text-sm text-[#4c576c]'>{source.url}</p>
              <div className='mt-3 flex flex-wrap gap-1.5'>
                {source.tags.map((tag) => (
                  <span
                    key={tag}
                    className='rounded-xl bg-[#f0f4ff] px-2.5 py-1 text-xs text-[#155dfc]'
                  >
                    {tag}
                  </span>
                ))}
              </div>
              <div className='mt-4 flex gap-2.5'>
                <button
                  type='button'
                  className='flex-1 cursor-pointer rounded-[14px] border-none bg-[#eef2ff] py-2.5 font-semibold text-[#155dfc] hover:bg-[#e0e7ff]'
                >
                  편집
                </button>
                <button
                  type='button'
                  className='flex-1 cursor-pointer rounded-[14px] border-none bg-[#eef2ff] py-2.5 font-semibold text-[#155dfc] hover:bg-[#e0e7ff]'
                >
                  동기화
                </button>
                <button
                  type='button'
                  className='flex-1 cursor-pointer rounded-[14px] border-none bg-[rgba(243,244,246,0.8)] py-2.5 font-semibold text-[#d92d20] hover:bg-[rgba(243,244,246,1)]'
                >
                  삭제
                </button>
              </div>
            </article>
          ))}
        </section>
      </div>
    </div>
  );
}

function mapSourceToCard(source: CreatedSource): ManagedSource {
  const hostname = safeHostname(source.url);
  const type = deriveSourceType(hostname);
  const frequency = type === 'YouTube' ? '실시간' : '매일 업데이트';
  return {
    ...source,
    status: 'active',
    type,
    frequency,
    tags: [hostname],
  };
}

function safeHostname(url: string) {
  try {
    return new URL(url).hostname.replace('www.', '');
  } catch (error) {
    return url;
  }
}

function deriveSourceType(host: string) {
  if (host.includes('youtube') || host.includes('youtu.be')) return 'YouTube';
  if (host.includes('newsletter')) return '뉴스레터';
  if (host.includes('community')) return '커뮤니티';
  return 'RSS';
}
