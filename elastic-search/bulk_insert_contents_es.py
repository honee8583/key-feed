from elasticsearch import Elasticsearch, helpers
from faker import Faker
import random
from datetime import datetime, timedelta
import base64

# ==========================================
# 1. 설정 (Configuration)
# ==========================================
ES_HOST = "http://localhost:9200"
INDEX_NAME = "contents"
TOTAL_DOCS = 1000000   # 생성할 전체 데이터 수 (100만 건)
BATCH_SIZE = 5000      # Bulk Insert 배치 크기
TARGET_RATIO = 0.05    # 타겟(검색 키워드 포함) 데이터 비율 (5%)

# ES 클라이언트 및 Faker 초기화
es = Elasticsearch(ES_HOST)
fake = Faker()

# 검색 테스트용 키워드 목록
KEYWORDS = ["Kafka", "Spring Boot", "Redis", "MSA", "Docker", "AWS", "Elasticsearch", "Kotlin"]

# ==========================================
# 2. 데이터 생성 제너레이터
# ==========================================
def generate_docs():
    print(f"🚀 {TOTAL_DOCS}건 데이터 생성 및 삽입 시작...")
    
    target_count = int(TOTAL_DOCS * TARGET_RATIO) 
    now = datetime.now()

    for i in range(TOTAL_DOCS):
        doc_id = i + 1
        is_target = i < target_count
        
        # [수정] 날짜 랜덤화 로직 (타겟/노이즈 상관없이 전체 기간에 고르게 분포)
        # 0일 ~ 365일 전 사이의 랜덤한 날짜
        days_ago = random.randint(0, 365)
        
        # [수정] 시간(시/분/초) 랜덤화
        # 같은 날짜라도 시간이 다르게 하기 위해 0 ~ 86400초(24시간) 사이 랜덤 값을 뺌
        random_seconds = random.randint(0, 86400)
        
        if is_target:
            # 타겟 데이터: 키워드 포함
            keyword = random.choice(KEYWORDS)
            title = f"{keyword} 관련 기술 블로그 글 {doc_id}"
            summary = f"이 글에서는 {keyword}와 {random.choice(KEYWORDS)}를 활용한 아키텍처를 다룹니다."
        else:
            # 노이즈 데이터: 일반 텍스트
            title = f"일상 이야기 및 여행 후기 {doc_id}"
            summary = fake.text(max_nb_chars=100)

        # 최종 날짜 계산 (현재 - 랜덤일 - 랜덤초)
        pub_date = now - timedelta(days=days_ago, seconds=random_seconds)
        
        # [중요] Java의 LocalDateTime 포맷(yyyy-MM-ddTHH:mm:ss.SSS)에 맞춘 문자열 변환
        # %f는 마이크로초(6자리)이므로 뒤에 3자리를 잘라서 밀리초(3자리)로 맞춤
        pub_date_str = pub_date.strftime('%Y-%m-%dT%H:%M:%S.%f')[:-3]

        # 고유 ID 생성 (URL 기반 해시)
        original_url = f"https://test.com/post/{doc_id}"
        unique_id_str = f"{random.randint(1,100)}-{original_url}"
        es_id = base64.b64encode(unique_id_str.encode('utf-8')).decode('utf-8')

        # 문서 구조 생성 (Java 엔티티의 @Field name 속성과 일치시킴 - Snake Case)
        doc = {
            "_index": INDEX_NAME,
            "_id": es_id,
            "_source": {
                "content_id": doc_id,
                "source_id": random.randint(1, 100),
                "source_name": "테스트 블로그",
                "title": title,
                "summary": summary,
                "original_url": original_url,
                "thumbnail_url": None,
                "published_at": pub_date_str,   # String (Date format)
                "created_at": pub_date_str      # String (Date format)
            }
        }
        yield doc

# ==========================================
# 3. 실행 (Execution)
# ==========================================
try:
    # 인덱스 생성/삭제 로직은 제거했습니다. (Spring Boot 앱 실행 시 자동 생성됨을 전제)
    # 이미 존재하는 'contents' 인덱스에 데이터만 밀어넣습니다.
    
    success, failed = helpers.bulk(es, generate_docs(), chunk_size=BATCH_SIZE)
    print(f"✅ 완료! 성공: {success}, 실패: {failed}")

except Exception as e:
    print(f"❌ 에러 발생: {e}")
