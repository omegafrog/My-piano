#!/usr/bin/env python3
"""
1000개의 Post를 생성하는 스크립트
My-Piano 애플리케이션의 POST /api/v1/community/posts API를 사용
"""

import requests
import json
import time
import random
from faker import Faker

# 설정
BASE_URL = "http://localhost:8080"
API_ENDPOINT = f"{BASE_URL}/api/v1/community/posts"
TOTAL_POSTS = 1000
BATCH_SIZE = 50  # 한 번에 생성할 포스트 수

# 더미 데이터 생성기
fake = Faker('ko_KR')  # 한국어 데이터

# 샘플 제목과 내용 템플릿
TITLE_TEMPLATES = [
    "피아노 연주 팁: {}",
    "초보자를 위한 {} 가이드",
    "{} 연주법 공유",
    "클래식 {} 추천",
    "재즈 피아노 {} 이야기",
    "{} 악보 분석",
    "피아노 테크닉: {}",
    "{} 연습 방법",
    "음악 이론: {}",
    "피아노 레슨 후기: {}"
]

CONTENT_TEMPLATES = [
    """안녕하세요! 오늘은 {}에 대해 이야기해보려고 합니다.

{}

이 방법을 통해 많은 분들이 실력 향상을 경험하셨습니다. 
특히 {}는 정말 중요한 포인트라고 생각합니다.

여러분의 의견도 댓글로 공유해주세요!""",
    
    """{}에 관한 글을 써봅니다.

최근에 {}를 연습하면서 깨달은 것이 있어서 공유드립니다.
{}는 생각보다 어려웠지만, 꾸준히 연습하니 실력이 늘더라고요.

초보자분들께 도움이 되길 바랍니다.""",
    
    """{}를 공부하다가 알게 된 팁입니다.

1. {}
2. {}
3. {}

이 세 가지만 기억하셔도 큰 도움이 될 거예요!
질문 있으시면 언제든 댓글 남겨주세요."""
]

MUSIC_TOPICS = [
    "스케일 연습", "아르페지오", "손가락 독립성", "페달 사용법", "리듬감",
    "화성학", "즉흥연주", "시트 리딩", "메트로놈 활용", "손목 자세",
    "쇼팽 발라드", "베토벤 소나타", "바흐 인벤션", "드뷔시 곡", "모차르트",
    "재즈 코드", "블루스 스케일", "스윙 리듬", "보사노바", "라틴 리듬",
    "연습 계획", "무대 공포증", "암보 방법", "표현력", "다이나믹"
]

def generate_post_data():
    """랜덤한 포스트 데이터 생성"""
    topic = random.choice(MUSIC_TOPICS)
    title_template = random.choice(TITLE_TEMPLATES)
    content_template = random.choice(CONTENT_TEMPLATES)
    
    title = title_template.format(topic)
    
    # 내용 생성을 위한 추가 정보
    technique = random.choice(["정확한 손가락 포지션", "꾸준한 연습", "올바른 자세", "집중력"])
    detail = fake.text(max_nb_chars=100)
    point = random.choice(["표현력", "테크닉", "정확성", "음악성", "리듬감"])
    
    content = content_template.format(topic, detail, technique, point, technique, point)
    
    return {
        "title": title,
        "content": content
    }

def create_post(session, post_data):
    """단일 포스트 생성"""
    try:
        response = session.post(
            API_ENDPOINT,
            json=post_data,
            headers={
                'Content-Type': 'application/json',
                'Accept': 'application/json'
            },
            timeout=10
        )
        
        if response.status_code == 200:
            return True, response.json()
        else:
            return False, f"HTTP {response.status_code}: {response.text}"
            
    except requests.exceptions.RequestException as e:
        return False, f"Request failed: {str(e)}"

def create_posts_batch(session, batch_size=BATCH_SIZE):
    """배치 단위로 포스트 생성"""
    success_count = 0
    failed_count = 0
    
    print(f"Creating {batch_size} posts...")
    
    for i in range(batch_size):
        post_data = generate_post_data()
        success, result = create_post(session, post_data)
        
        if success:
            success_count += 1
            print(f"✅ Post {i+1}: {post_data['title'][:50]}...")
        else:
            failed_count += 1
            print(f"❌ Post {i+1} failed: {result}")
        
        # API 부하 방지를 위한 짧은 딜레이
        time.sleep(0.1)
    
    return success_count, failed_count

def login(username, password):
    """로그인하고 JWT 토큰을 받아옴"""
    login_url = f"{BASE_URL}/api/v1/user/login"
    try:
        response = requests.post(
            login_url,
            data={'username': username, 'password': password},
            timeout=10
        )
        if response.status_code == 200:
            # 응답 본문이 JSON 형식이라고 가정하고 파싱
            try:
                token_data = response.json()
                # 'data' 객체와 그 안의 'access token' 키 확인
                if 'data' in token_data and 'access token' in token_data['data']:
                    print("✅ Login successful!")
                    return token_data['data']['access token']
                else:
                    print(f"❌ Login failed: 'access token' not in response 'data' object. Response: {response.text}")
                    return None
            except json.JSONDecodeError:
                print(f"❌ Login failed: Could not parse JSON response. Response: {response.text}")
                return None
        else:
            print(f"❌ Login failed with status {response.status_code}: {response.text}")
            return None
    except requests.exceptions.RequestException as e:
        print(f"❌ Login request failed: {str(e)}")
        return None

def main():
    """메인 실행 함수"""
    print("🎹 My-Piano Post Creation Script")
    print(f"Target: {TOTAL_POSTS} posts")
    print(f"API Endpoint: {API_ENDPOINT}")
    print("-" * 50)

    # 로그인
    access_token = login("username1", "password123")
    if not access_token:
        print("Script aborted due to login failure.")
        return

    # 세션 생성 및 헤더 설정
    session = requests.Session()
    session.headers.update({'Authorization': access_token})
    
    total_success = 0
    total_failed = 0
    
    # 배치 단위로 포스트 생성
    batches = (TOTAL_POSTS + BATCH_SIZE - 1) // BATCH_SIZE
    
    for batch_num in range(batches):
        current_batch_size = min(BATCH_SIZE, TOTAL_POSTS - (batch_num * BATCH_SIZE))
        
        print(f"\n📦 Batch {batch_num + 1}/{batches} (Size: {current_batch_size})")
        
        success, failed = create_posts_batch(session, current_batch_size)
        total_success += success
        total_failed += failed
        
        print(f"Batch {batch_num + 1} completed: {success} success, {failed} failed")
        
        # 배치 간 딜레이
        if batch_num < batches - 1:
            time.sleep(1)
    
    # 결과 요약
    print("\n" + "="*50)
    print("📊 FINAL RESULTS")
    print("="*50)
    print(f"✅ Successfully created: {total_success} posts")
    print(f"❌ Failed: {total_failed} posts")
    print(f"📈 Success rate: {(total_success/TOTAL_POSTS)*100:.1f}%")
    
    if total_failed > 0:
        print(f"\n⚠️  Some posts failed to create. Check server logs for details.")
    else:
        print(f"\n🎉 All {TOTAL_POSTS} posts created successfully!")

if __name__ == "__main__":
    main()