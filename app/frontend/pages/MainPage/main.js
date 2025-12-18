import { fetchAPIWithBody, fetchAPI } from '../../js/api/api_fetcher.js';
import { showToast } from '../../js/common/messages.js';
import { loadHeader } from '../../layout/header.js';

// 지하철 역 목록 (예시 - 실제로는 서버에서 가져오거나 더 많은 역을 포함)
const STATIONS = [
    '강남역', '역삼역', '선릉역', '삼성역', '종합운동장역',
    '올림픽공원역', '방이역', '개롱역', '거여역', '마천역',
    '잠실역', '신천역', '종합운동장역', '삼성역', '선릉역',
    '판교역', '정자역', '미금역', '동천역', '수지구청역',
    '성남역', '이매역', '야탑역', '모란역', '태평역',
    '가천대역', '복정역', '산성역', '남한산성입구역', '단대오거리역',
    '신흥역', '수진역', '모란역', '태평역', '야탑역',
    '이매역', '서현역', '수내역', '정자역', '미금역',
    '동천역', '삼성역', '선릉역', '역삼역', '강남역'
];

// 날짜 형식 변환 함수 (YYYY-MM-DDTHH:mm -> YYYY-MM-DD HH24:MI:SS)
function formatDateTime(dateTimeString) {
    if (!dateTimeString) return null;
    // datetime-local 형식 (YYYY-MM-DDTHH:mm)을 YYYY-MM-DD HH24:MI:SS로 변환
    return dateTimeString.replace('T', ' ') + ':00';
}

// 전역 상태
let groupData = null;
let submissionData = {
    submitted: 0,
    total: 0,
    members: []
};

// DOM 요소
const modalOverlay = document.getElementById('modalOverlay');
const modalClose = document.getElementById('modalClose');
const groupCreateBtn = document.getElementById('groupCreateBtn');
const groupForm = document.getElementById('groupForm');
const dateToggle = document.getElementById('dateToggle');
const dateArea = document.getElementById('dateArea');
const stationInput = document.getElementById('stationInput');
const stationDropdown = document.getElementById('stationDropdown');
const urlSection = document.getElementById('urlSection');
const urlValue = document.getElementById('urlValue');
const copyBtn = document.getElementById('copyBtn');
const submissionStatus = document.getElementById('submissionStatus');
const statusDisplay = document.getElementById('statusDisplay');
const submissionList = document.getElementById('submissionList');
const memberList = document.getElementById('memberList');
const alertMessage = document.getElementById('alertMessage');
const alertClose = document.getElementById('alertClose');
const myPickBtn = document.getElementById('myPickBtn');
const submitAllBtn = document.getElementById('submitAllBtn');
const viewResultBtn = document.getElementById('viewResultBtn');

// 페이지 로드 시 헤더 로드 및 모달 자동 표시
window.addEventListener('DOMContentLoaded', async () => {
    // 헤더 로드
    const headerContainer = document.getElementById('header-container');
    await loadHeader(headerContainer);
    
    // 모달 자동 표시
    openModal();
});

// 모달 열기
function openModal() {
    modalOverlay.classList.add('active');
    document.body.style.overflow = 'hidden';
}

// 모달 닫기
function closeModal() {
    modalOverlay.classList.remove('active');
    document.body.style.overflow = 'auto';
    // 폼 초기화
    groupForm.reset();
    dateArea.style.display = 'none';
    dateToggle.checked = false;
    stationDropdown.style.display = 'none';
}

// 모달 닫기 이벤트
modalClose.addEventListener('click', closeModal);
groupCreateBtn.addEventListener('click', openModal);

// 모달 외부 클릭 시 닫기
modalOverlay.addEventListener('click', (e) => {
    if (e.target === modalOverlay) {
        closeModal();
    }
});

// 날짜 토글 처리
dateToggle.addEventListener('change', (e) => {
    if (e.target.checked) {
        dateArea.style.display = 'block';
        document.getElementById('dateStart').required = true;
        document.getElementById('dateEnd').required = true;
    } else {
        dateArea.style.display = 'none';
        document.getElementById('dateStart').required = false;
        document.getElementById('dateEnd').required = false;
        document.getElementById('dateStart').value = '';
        document.getElementById('dateEnd').value = '';
    }
});

// 역 검색 기능
let filteredStations = [];

stationInput.addEventListener('input', (e) => {
    const searchTerm = e.target.value.toLowerCase();
    
    if (searchTerm.length === 0) {
        stationDropdown.style.display = 'none';
        return;
    }

    filteredStations = STATIONS.filter(station => 
        station.toLowerCase().includes(searchTerm)
    );

    if (filteredStations.length > 0) {
        displayStationDropdown(filteredStations);
    } else {
        stationDropdown.style.display = 'none';
    }
});

function displayStationDropdown(stations) {
    stationDropdown.innerHTML = '';
    stations.forEach(station => {
        const item = document.createElement('div');
        item.className = 'station-dropdown-item';
        item.textContent = station;
        item.addEventListener('click', () => {
            stationInput.value = station;
            stationDropdown.style.display = 'none';
        });
        stationDropdown.appendChild(item);
    });
    stationDropdown.style.display = 'block';
}

// 외부 클릭 시 드롭다운 닫기
document.addEventListener('click', (e) => {
    if (!stationInput.contains(e.target) && !stationDropdown.contains(e.target)) {
        stationDropdown.style.display = 'none';
    }
});

// 폼 제출 처리
groupForm.addEventListener('submit', async (e) => {
    e.preventDefault();

    // 필수 필드 검증
    const memberCount = document.getElementById('memberCount').value;
    const budgetMin = document.getElementById('budgetMin').value;
    const budgetMax = document.getElementById('budgetMax').value;
    const station = stationInput.value;

    if (!memberCount || !budgetMin || !budgetMax || !station) {
        showToast('필수 항목을 모두 입력해주세요.');
        return;
    }

    // 날짜 검증
    if (dateToggle.checked) {
        const dateStart = document.getElementById('dateStart').value;
        const dateEnd = document.getElementById('dateEnd').value;
        
        if (!dateStart || !dateEnd) {
            showToast('날짜 범위를 입력해주세요.');
            return;
        }

        if (new Date(dateStart) > new Date(dateEnd)) {
            showToast('시작 날짜가 종료 날짜보다 늦을 수 없습니다.');
            return;
        }
    }

    // 데이터 준비
    // 예산은 만원 단위로 입력받지만, 서버에는 원 단위로 전송
    const budgetMinWon = parseInt(budgetMin) * 10000;
    const budgetMaxWon = parseInt(budgetMax) * 10000;
    
    // 예산은 평균값 또는 범위로 전송 (요구사항에 따라 조정 가능)
    const budget = Math.floor((budgetMinWon + budgetMaxWon) / 2).toString();
    
    const formData = {
        max_capacity: memberCount,
        budget: budget,
        station: station.replace('역', ''), // '역' 제거
        date_range: dateToggle.checked ? [
            formatDateTime(document.getElementById('dateStart').value),
            formatDateTime(document.getElementById('dateEnd').value)
        ] : null
    };

    try {
        // API 호출 (실제 엔드포인트로 변경 필요)
        const response = await fetchAPIWithBody(
            '/api/groups/create', // 실제 API 엔드포인트로 변경 필요
            'POST',
            JSON.stringify(formData)
        );

        if (response.ok) {
            const data = await response.json();
            
            // 그룹 데이터 저장
            groupData = data;
            submissionData.total = parseInt(memberCount);

            // URL 표시
            if (data.url) {
                urlValue.textContent = data.url;
                urlSection.style.display = 'block';
            }

            // 모달 닫기
            closeModal();

            // Toast 메시지
            showToast('그룹이 생성되었습니다!');

            // 제출 현황 표시
            updateSubmissionStatus(0, parseInt(memberCount));

            // 그룹 생성 버튼 숨기기
            groupCreateBtn.style.display = 'none';

            // 상태 폴링 시작
            startStatusPolling();
        } else {
            const errorData = await response.json();
            showToast(errorData.message || '그룹 생성에 실패했습니다.');
        }
    } catch (error) {
        console.error('Error creating group:', error);
        showToast('그룹 생성 중 오류가 발생했습니다.');
    }
});

// URL 복사 기능
copyBtn.addEventListener('click', async () => {
    const url = urlValue.textContent;
    
    try {
        await navigator.clipboard.writeText(url);
        showToast('클립보드에 복사되었습니다.');
    } catch (error) {
        console.error('Failed to copy:', error);
        // Fallback: 텍스트 영역 사용
        const textArea = document.createElement('textarea');
        textArea.value = url;
        document.body.appendChild(textArea);
        textArea.select();
        document.execCommand('copy');
        document.body.removeChild(textArea);
        showToast('클립보드에 복사되었습니다.');
    }
});

// 제출 현황 업데이트
function updateSubmissionStatus(submitted, total) {
    submissionData.submitted = submitted;
    submissionData.total = total;

    document.getElementById('submissionCount').textContent = submitted;
    document.getElementById('totalMembers').textContent = total;

    submissionStatus.style.display = 'block';

    // 모든 멤버가 제출 완료한 경우
    if (submitted === total && total > 0) {
        alertMessage.style.display = 'block';
        myPickBtn.disabled = true;
        submitAllBtn.disabled = true;
        viewResultBtn.disabled = false;
    }
}

// 제출 현황 클릭 시 멤버 리스트 토글
statusDisplay.addEventListener('click', () => {
    if (submissionList.style.display === 'none') {
        loadSubmissionMembers();
        submissionList.style.display = 'block';
    } else {
        submissionList.style.display = 'none';
    }
});

// 제출한 멤버 목록 로드
async function loadSubmissionMembers() {
    try {
        // API 호출 (실제 엔드포인트로 변경 필요)
        const response = await fetchAPI(
            `/api/groups/${groupData?.id}/members`, // 실제 API 엔드포인트로 변경 필요
            'GET'
        );

        if (response.ok) {
            const data = await response.json();
            submissionData.members = data.members || [];
            displayMemberList(submissionData.members);
        } else {
            // 임시 데이터 (실제로는 API에서 가져옴)
            displayMemberList(['김총무', '이부장', '박과장']);
        }
    } catch (error) {
        console.error('Error loading members:', error);
        // 임시 데이터
        displayMemberList(['김총무', '이부장', '박과장']);
    }
}

// 멤버 리스트 표시
function displayMemberList(members) {
    memberList.innerHTML = '';
    members.forEach(member => {
        const li = document.createElement('li');
        li.textContent = member;
        memberList.appendChild(li);
    });
}

// 알림 닫기
alertClose.addEventListener('click', () => {
    alertMessage.style.display = 'none';
});

// 나의 회식픽 버튼
myPickBtn.addEventListener('click', () => {
    if (groupData?.url) {
        window.location.href = groupData.url;
    } else {
        showToast('그룹 URL이 없습니다.');
    }
});

// 전체 회식픽 제출 버튼
submitAllBtn.addEventListener('click', async () => {
    try {
        // API 호출 (실제 엔드포인트로 변경 필요)
        const response = await fetchAPIWithBody(
            `/api/groups/${groupData?.id}/submit-all`, // 실제 API 엔드포인트로 변경 필요
            'POST',
            JSON.stringify({})
        );

        if (response.ok) {
            showToast('전체 회식픽이 제출되었습니다.');
            myPickBtn.disabled = true;
            submitAllBtn.disabled = true;
            viewResultBtn.disabled = false;
        } else {
            showToast('제출에 실패했습니다.');
        }
    } catch (error) {
        console.error('Error submitting all:', error);
        showToast('제출 중 오류가 발생했습니다.');
    }
});

// 결과보기 버튼
viewResultBtn.addEventListener('click', () => {
    // 결과 페이지로 이동 (실제 경로로 변경 필요)
    window.location.href = '/pages/ResultPage/result.html';
});

// 주기적으로 제출 현황 업데이트 (폴링)
let statusPollingInterval = null;

function startStatusPolling() {
    if (statusPollingInterval) {
        clearInterval(statusPollingInterval);
    }
    
    statusPollingInterval = setInterval(async () => {
        if (!groupData?.id) {
            clearInterval(statusPollingInterval);
            return;
        }
        
        try {
            const response = await fetchAPI(
                `/api/groups/${groupData.id}/status`, // 실제 API 엔드포인트로 변경 필요
                'GET'
            );

            if (response.ok) {
                const data = await response.json();
                updateSubmissionStatus(data.submitted || 0, data.total || 0);
            }
        } catch (error) {
            console.error('Error fetching status:', error);
        }
    }, 5000); // 5초마다 업데이트
}

// 그룹 생성 성공 시 폴링 시작
// (groupForm submit 핸들러에서 호출)

