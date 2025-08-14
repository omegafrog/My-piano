#!/bin/bash

# 1000개의 Post를 생성하는 Bash 스크립트
# My-Piano 애플리케이션의 POST /api/v1/community/posts API를 사용

# 설정
BASE_URL="http://localhost:8080"
API_ENDPOINT="${BASE_URL}/api/v1/community/posts"
TOTAL_POSTS=1000

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
    "쇼팽 발라드 연주 팁"
    "베토벤 소나타 분석"
    "바흐 인벤션 학습법"
    "드뷔시 곡 해석"
    "모차르트 소나타 연습"
    "재즈 코드 진행"
    "블루스 스케일 활용"
    "스윙 리듬 마스터"
    "보사노바 연주법"
    "라틴 리듬 기초"
    "연습 계획 세우기"
    "무대 공포증 극복"
    "암보 효과적인 방법"
    "표현력 향상 팁"
    "다이나믹 조절법"
)

contents=(
    "안녕하세요! 오늘은 피아노 연습에 대해 이야기해보려고 합니다. 꾸준한 연습이 가장 중요하다고 생각합니다. 특히 정확한 손가락 포지션은 정말 중요한 포인트라고 생각합니다. 여러분의 의견도 댓글로 공유해주세요!"
    
    "피아노 테크닉에 관한 글을 써봅니다. 최근에 스케일 연습을 하면서 깨달은 것이 있어서 공유드립니다. 손가락 독립성은 생각보다 어려웠지만, 꾸준히 연습하니 실력이 늘더라고요. 초보자분들께 도움이 되길 바랍니다."
    
    "음악 이론을 공부하다가 알게 된 팁입니다. 1. 정확한 리듬감 유지 2. 올바른 자세 3. 집중력 향상 이 세 가지만 기억하셔도 큰 도움이 될 거예요! 질문 있으시면 언제든 댓글 남겨주세요."
    
    "오늘은 페달 사용법에 대해 나누고 싶습니다. 페달은 피아노 연주에서 매우 중요한 요소입니다. 적절한 페달링으로 음악의 표현력을 크게 향상시킬 수 있어요."
    
    "화성학 기초에 대해 설명드리겠습니다. 코드 진행을 이해하면 피아노 연주가 훨씬 쉬워집니다. 특히 재즈에서는 필수적인 지식이라고 할 수 있어요."
    
    "메트로놈 활용법을 공유합니다. 정확한 템포 유지는 연주의 기본입니다. 처음에는 느린 템포로 시작해서 점진적으로 빨라지는 것이 좋습니다."
    
    "아르페지오 연습 방법을 알려드립니다. 손목의 유연성이 매우 중요하며, 각 음이 균등하게 들리도록 연습해야 합니다."
    
    "즉흥연주의 세계로 초대합니다. 스케일과 코드를 자유롭게 활용하여 자신만의 음악을 만들어보세요. 창의성을 발휘할 수 있는 좋은 방법입니다."
    
    "시트 리딩 실력 향상 팁을 나눕니다. 매일 조금씩이라도 새로운 악보를 읽는 연습을 하시면 실력이 늘어요."
    
    "연습 계획 세우기에 대해 이야기해보겠습니다. 목표를 설정하고 체계적으로 연습하는 것이 효과적입니다."
)

# 진행률 표시 함수
show_progress() {
    local current=$1
    local total=$2
    local percent=$((current * 100 / total))
    local filled=$((percent / 2))
    local empty=$((50 - filled))
    
    printf "\rProgress: ["
    printf "%0.s#" $(seq 1 $filled)
    printf "%0.s-" $(seq 1 $empty)
    printf "] %d%% (%d/%d)" $percent $current $total
}

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
    
    # API 호출
    response=$(curl -s -w "HTTPSTATUS:%{http_code}" \
        -X POST \
        -H "Content-Type: application/json" \
        -H "Accept: application/json" \
        -d "$json_data" \
        "$API_ENDPOINT" 2>/dev/null)
    
    # HTTP 상태 코드 추출
    http_code=$(echo $response | tr -d '\n' | sed -e 's/.*HTTPSTATUS://')
    response_body=$(echo $response | sed -e 's/HTTPSTATUS\:.*//g')
    
    if [ "$http_code" -eq 200 ]; then
        ((success_count++))
        return 0
    else
        ((failed_count++))
        echo -e "\n❌ Failed to create post: HTTP $http_code"
        echo "Response: $response_body"
        return 1
    fi
}

# 메인 실행 부분
echo "🎹 My-Piano Post Creation Script"
echo "Target: $TOTAL_POSTS posts"
echo "API Endpoint: $API_ENDPOINT"
echo "$(printf '%.0s-' {1..50})"

# 서버 연결 확인
echo "Checking server connection..."
server_check=$(curl -s -o /dev/null -w "%{http_code}" "$BASE_URL/actuator/health" 2>/dev/null || echo "000")

if [ "$server_check" != "200" ]; then
    echo "⚠️  Warning: Server might not be running or accessible"
    echo "Please make sure the Spring Boot application is running on $BASE_URL"
    read -p "Continue anyway? (y/N): " -n 1 -r
    echo
    if [[ ! $REPLY =~ ^[Yy]$ ]]; then
        echo "Exiting..."
        exit 1
    fi
fi

# 포스트 생성 시작
echo "Starting post creation..."
start_time=$(date +%s)

for ((i=1; i<=TOTAL_POSTS; i++)); do
    # 랜덤 제목과 내용 선택
    title_index=$((RANDOM % ${#titles[@]}))
    content_index=$((RANDOM % ${#contents[@]}))
    
    title="${titles[$title_index]} #$i"
    content="${contents[$content_index]}"
    
    # 포스트 생성
    create_post "$title" "$content"
    
    # 진행률 표시
    show_progress $i $TOTAL_POSTS
    
    # API 부하 방지를 위한 짧은 딜레이
    sleep 0.1
done

end_time=$(date +%s)
duration=$((end_time - start_time))

# 결과 요약
echo -e "\n$(printf '%.0s=' {1..50})"
echo "📊 FINAL RESULTS"
echo "$(printf '%.0s=' {1..50})"
echo "✅ Successfully created: $success_count posts"
echo "❌ Failed: $failed_count posts"
echo "📈 Success rate: $(( success_count * 100 / TOTAL_POSTS ))%"
echo "⏱️  Total time: ${duration}s"

if [ $failed_count -gt 0 ]; then
    echo -e "\n⚠️  Some posts failed to create. Check server logs for details."
else
    echo -e "\n🎉 All $TOTAL_POSTS posts created successfully!"
fi