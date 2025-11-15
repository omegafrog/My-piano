import requests
import sys
import os
import random
import time
import json
import concurrent.futures
import argparse
from tqdm import tqdm

# --- 설정 ---
BASE_URL = "http://localhost:8080/api/v1"
LOGIN_CREDENTIALS = {"username": "username1", "password": "password123"}
MAX_WORKERS = 50  # 동시에 실행할 스레드 수

# --- 무작위 데이터 생성기 ---
korean_titles = [
    "아름다운 피아노 선율",
    "쇼팽 에튀드 Op. 10, No. 4",
    "베토벤 월광 소나타 3악장",
    "이루마 - River Flows in You",
    "히사이시 조 - 인생의 회전목마",
    "브람스 헝가리 무곡 5번",
    "모차르트 피아노 소나타 11번",
    "드뷔시 달빛",
    "라흐마니노프 피아노 협주곡 2번",
    "리스트 사랑의 꿈"
]

korean_contents = [
    "이 악보는 초보자도 쉽게 연주할 수 있도록 편곡되었습니다.",
    "영화 '하울의 움직이는 성' OST로 유명한 곡입니다. 서정적인 멜로디가 인상적입니다.",
    "빠르고 격정적인 패시지가 특징인 곡으로, 연주에 많은 연습이 필요합니다.",
    "전 세계적으로 사랑받는 뉴에이지 피아노곡입니다. 감미로운 선율을 느껴보세요.",
    "클래식 피아노의 정수를 느낄 수 있는 명곡입니다. 깊이 있는 해석이 중요합니다.",
    "낭만적인 분위기의 곡으로, 특별한 날 연주하기에 좋습니다.",
    "경쾌하고 발랄한 느낌의 곡입니다. 연주하는 내내 즐거움을 느낄 수 있을 것입니다.",
    "드라마 '경성스캔들'에 삽입되어 많은 사랑을 받은 곡입니다.",
    "웅장하고 화려한 오케스트라와 피아노의 조화가 돋보이는 대곡입니다.",
    "슬프면서도 아름다운 멜로디가 마음을 울리는 곡입니다."
]

def get_random_korean_title():
    return random.choice(korean_titles)

def get_random_korean_content():
    return random.choice(korean_contents)

# --- API 호출 함수 ---

def login(session, username, password):
    """세션을 사용하여 로그인하고 토큰과 쿠키를 저장합니다."""
    login_url = f"{BASE_URL}/user/login"
    print("로그인 시도 중...")
    try:
        response = session.post(
            login_url,
            data={'username': username, 'password': password},
            timeout=10
        )
        response.raise_for_status()
        token_data = response.json()
        access_token = token_data.get('data', {}).get('access token')
        if access_token:
            print("✅ 로그인 성공!")
            session.headers.update({'Authorization': f"{access_token}"})
            return True
        else:
            print(f"❌ 로그인 실패: 응답에서 'access token'을 찾을 수 없습니다.")
            return False
    except requests.exceptions.RequestException as e:
        print(f"❌ 로그인 요청 실패: {e}")
        return False

def create_dummy_pdf(filename="dummy.pdf", size_bytes=100):
    """간단한 PDF 파일을 생성합니다."""
    content = b'%PDF-1.0\n'
    content += b'1 0 obj<</Type/Catalog/Pages 2 0 R>>endobj\n'
    content += b'2 0 obj<</Type/Pages/Kids[3 0 R]/Count 1>>endobj\n'
    content += b'3 0 obj<</Type/Page/MediaBox[0 0 100 100]>>endobj\n'
    padding = size_bytes - len(content)
    if padding > 0:
        content += b' ' * padding
    with open(filename, 'wb') as f:
        f.write(content)
    return filename

def create_one_post(session, pdf_path, post_id, verbose=False):
    """하나의 포스트를 생성하는 과정 (파일 업로드, 포스트 작성)"""
    try:
        # 1. 파일 업로드 (미리 생성된 파일 재사용)
        upload_url = f"{BASE_URL}/files/upload"
        if verbose:
            tqdm.write(f"\n--- Upload File Request (ID: {post_id}) ---")
            tqdm.write(f"URL: {upload_url}")
            tqdm.write(f"Headers: {session.headers}")
            tqdm.write("--------------------------")

        with open(pdf_path, 'rb') as f:
            files = {'file': (os.path.basename(pdf_path), f, 'application/pdf')}
            response = session.post(upload_url, files=files, timeout=30)
            response.raise_for_status()
        
        upload_id = response.json().get('data', {}).get('uploadId')
        if not upload_id:
            if verbose: tqdm.write(f"Upload failed for {post_id}: No uploadId")
            return False

        # 2. 게시물 작성
        post_url = f"{BASE_URL}/sheet-post"
        title = get_random_korean_title()
        content = get_random_korean_content()
        payload = {
          "title": title, "content": content, "price": 10000, "discountRate": 0,
          "sheet": {
            "title": title, "difficulty": 0, "instrument": 0,
            "genres": {"genre1": "CAROL", "genre2": "NEW_AGE"},
            "isSolo": True, "lyrics": True
          },
          "uploadId": upload_id
        }

        if verbose:
            tqdm.write(f"\n--- Create Sheet Post Request (ID: {post_id}) ---")
            tqdm.write(f"URL: {post_url}")
            full_headers = {{**session.headers, 'Content-Type': 'application/json'}}
            tqdm.write(f"Headers: {full_headers}")
            tqdm.write(f"Body: {json.dumps(payload, indent=2, ensure_ascii=False)}")
            tqdm.write("-------------------------------")

        response = session.post(post_url, headers={'Content-Type': 'application/json'}, json=payload, timeout=30)
        response.raise_for_status()
        return True
    except requests.exceptions.RequestException as e:
        if verbose:
            tqdm.write(f"Request failed for post {post_id}: {e}")
            if e.response:
                tqdm.write(f"Response: {e.response.text}")
        return False

def main(num_posts, batch_size, verbose):
    """메인 실행 함수"""
    print(f"🎹 My-Piano API 포스트 생성 스크립트 (병렬 실행, {MAX_WORKERS} 워커)")
    print(f"목표: {num_posts}개 포스트 생성, 배치 크기: {batch_size}")
    print("-" * 50)

    pdf_path = None
    try:
        pdf_path = create_dummy_pdf("shared_dummy.pdf")
        print(f"📄 임시 PDF 파일 생성: {pdf_path}")

        with requests.Session() as session:
            if not login(session, **LOGIN_CREDENTIALS):
                print("스크립트를 종료합니다.")
                return

            total_success_count = 0
            with tqdm(total=num_posts, desc="전체 진행률") as pbar:
                for i in range(0, num_posts, batch_size):
                    batch_num = (i // batch_size) + 1
                    current_batch_size = min(batch_size, num_posts - i)
                    
                    with concurrent.futures.ThreadPoolExecutor(max_workers=MAX_WORKERS) as executor:
                        futures = [executor.submit(create_one_post, session, pdf_path, i + j, verbose) for j in range(current_batch_size)]
                        
                        batch_success_count = 0
                        for future in concurrent.futures.as_completed(futures):
                            if future.result():
                                batch_success_count += 1
                            pbar.update(1)
                    
                    total_success_count += batch_success_count
                    tqdm.write(f"📦 배치 {batch_num} 완료 (성공: {batch_success_count}/{current_batch_size})")

                    if i + batch_size < num_posts:
                        tqdm.write("... 1초 대기 ...")
                        time.sleep(1)

        print("\n" + "="*50)
        print("📊 최종 결과")
        print(f"✅ 총 성공: {total_success_count}개")
        print(f"❌ 총 실패: {num_posts - total_success_count}개")
        print("="*50)

    finally:
        if pdf_path and os.path.exists(pdf_path):
            print(f"\n🗑️  임시 PDF 파일 삭제: {pdf_path}")
            os.remove(pdf_path)

if __name__ == "__main__":
    parser = argparse.ArgumentParser(description="My-Piano API 포스트 생성 스크립트")
    parser.add_argument("num_posts", type=int, help="생성할 포스트 개수")
    parser.add_argument("-b", "--batch-size", type=int, default=100, help="한 번에 처리할 배치 크기 (기본값: 100)")
    parser.add_argument("-v", "--verbose", action="store_true", help="상세한 요청 정보를 출력합니다.")
    
    args = parser.parse_args()

    if args.num_posts <= 0:
        print("오류: 포스트 개수는 0보다 큰 정수여야 합니다.")
        sys.exit(1)
        
    main(args.num_posts, args.batch_size, args.verbose)