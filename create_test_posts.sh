#!/bin/bash

# 테스트용 소량 Post 생성 스크립트 (인증 없이 테스트)
# My-Piano 애플리케이션의 POST /api/v1/community/posts API를 사용

# 설정
BASE_URL="http://localhost:8080"
API_ENDPOINT="${BASE_URL}/api/v1/community/posts"
TOTAL_POSTS=10  # 테스트용으로 10개만

# 카운터
success_count=0
failed_count=0

# 샘플 제목과 내용 배열
titles=(
    "피아노 연주 팁: 스케일 연습법"
    "초보자를 위한 아르페지오 가이드"
    "손가락 독립성 연주법 공유"
    "클래식 페달 사용법 추천"
    "재즈 피아노 리듬감 이야기"
    "화성학 악보 분석"
    "피아노 테크닉: 즉흥연주"
    "시트 리딩 연습 방법"
    "음악 이론: 메트로놈 활용"
    "피아노 레슨 후기: 손목 자세"
)

contents=(
    "안녕하세요! 오늘은 피아노 연습에 대해 이야기해보려고 합니다. 꾸준한 연습이 가장 중요하다고 생각합니다."
    "피아노 테크닉에 관한 글을 써봅니다. 최근에 스케일 연습을 하면서 깨달은 것이 있어서 공유드립니다."
    "음악 이론을 공부하다가 알게 된 팁입니다. 1. 정확한 리듬감 유지 2. 올바른 자세 3. 집중력 향상"
    "오늘은 페달 사용법에 대해 나누고 싶습니다. 페달은 피아노 연주에서 매우 중요한 요소입니다."
    "화성학 기초에 대해 설명드리겠습니다. 코드 진행을 이해하면 피아노 연주가 훨씬 쉬워집니다."
    "메트로놈 활용법을 공유합니다. 정확한 템포 유지는 연주의 기본입니다."
    "아르페지오 연습 방법을 알려드립니다. 손목의 유연성이 매우 중요합니다."
    "즉흥연주의 세계로 초대합니다. 스케일과 코드를 자유롭게 활용해보세요."
    "시트 리딩 실력 향상 팁을 나눕니다. 매일 조금씩이라도 새로운 악보를 읽어보세요."
    "연습 계획 세우기에 대해 이야기해보겠습니다. 목표를 설정하고 체계적으로 연습하는 것이 효과적입니다."
)

# 포스트 생성 함수
create_post() {
    local title="$1"
    local content="$2"
    
    # JSON 데이터 생성
    local json_data=$(cat <<EOF
{
    "title": "$title",
    "content": "$content"
}
EOF
)
    
    echo "Attempting to create post: $title"
    
    # API 호출 (상세 응답 확인)
    response=$(curl -s -w "HTTPSTATUS:%{http_code}" \
        -X POST \
        -H "Content-Type: application/json" \
        -H "Accept: application/json" \
        -d "$json_data" \
        "$API_ENDPOINT" 2>/dev/null)
    
    # HTTP 상태 코드 추출
    http_code=$(echo $response | tr -d '\n' | sed -e 's/.*HTTPSTATUS://')
    response_body=$(echo $response | sed -e 's/HTTPSTATUS\:.*//g')
    
    echo "HTTP Code: $http_code"
    echo "Response: $response_body"
    
    if [ "$http_code" -eq 200 ] || [ "$http_code" -eq 201 ]; then
        ((success_count++))
        echo "✅ Success!"
        return 0
    else
        ((failed_count++))
        echo "❌ Failed!"
        return 1
    fi
    echo "---"
}

# 메인 실행 부분
echo "🎹 My-Piano Test Post Creation Script"
echo "Target: $TOTAL_POSTS posts"
echo "API Endpoint: $API_ENDPOINT"
echo "$(printf '%.0s-' {1..50})"

# 서버 연결 확인
echo "Checking server connection..."
server_check=$(curl -s -o /dev/null -w "%{http_code}" "$API_ENDPOINT" 2>/dev/null || echo "000")
echo "Server response: $server_check"

if [ "$server_check" == "000" ]; then
    echo "⚠️  Error: Cannot connect to server"
    echo "Please make sure the Spring Boot application is running on $BASE_URL"
    exit 1
fi

# 포스트 생성 시작
echo "Starting post creation..."
start_time=$(date +%s)

for ((i=1; i<=TOTAL_POSTS; i++)); do
    # 순차적으로 제목과 내용 선택
    title_index=$(((i-1) % ${#titles[@]}))
    content_index=$(((i-1) % ${#contents[@]}))
    
    title="${titles[$title_index]} #$i"
    content="${contents[$content_index]}"
    
    echo "Post $i/$TOTAL_POSTS:"
    
    # 포스트 생성
    create_post "$title" "$content"
    
    # 짧은 딜레이
    sleep 1
done

end_time=$(date +%s)
duration=$((end_time - start_time))

# 결과 요약
echo "$(printf '%.0s=' {1..50})"
echo "📊 FINAL RESULTS"
echo "$(printf '%.0s=' {1..50})"
echo "✅ Successfully created: $success_count posts"
echo "❌ Failed: $failed_count posts"
echo "📈 Success rate: $(( (success_count * 100) / TOTAL_POSTS ))%"
echo "⏱️  Total time: ${duration}s"

if [ $failed_count -eq $TOTAL_POSTS ]; then
    echo -e "\n❌ All posts failed. This is likely due to authentication requirements."
    echo "The API requires authentication (JWT token or login)."
    echo "Please check the application security configuration."
elif [ $failed_count -gt 0 ]; then
    echo -e "\n⚠️  Some posts failed to create. Check server logs for details."
else
    echo -e "\n🎉 All $TOTAL_POSTS posts created successfully!"
fi