import { loadHeader } from '../../layout/header-with-back-button/header.js';
import { initializeBackButton } from '../../layout/header-with-back-button/header.js';
import { showToast } from '../../js/common/messages.js';
import { getGroupResults } from '../../js/api/GenerateGroupApi.js';

// DOM 요소
const dom = {
    loadingContainer: null,
    resultContainer: null,
    dateRankingList: null,
    placeRankingList: null,
    settlementBtn: null
};

// URL에서 groupId 추출
function getGroupIdFromUrl() {
    const urlParams = new URLSearchParams(window.location.search);
    return urlParams.get('groupId');
}

// 결과 조회 API 호출
async function fetchResults(groupId) {
    try {
        const response = await getGroupResults(groupId);
        
        if (!response.ok) {
            throw new Error(`HTTP error! status: ${response.status}`);
        }
        
        return await response.json();
    } catch (error) {
        console.error('Error fetching results:', error);
        throw error;
    }
}

// 날짜 포맷팅 (연도.월.일(요일)) - 연도는 2자리로 표시
function formatDate(dateString) {
    const date = new Date(dateString);
    const year = String(date.getFullYear()).slice(-2); // 연도 2자리
    const month = String(date.getMonth() + 1).padStart(2, '0');
    const day = String(date.getDate()).padStart(2, '0');
    const weekdays = ['일', '월', '화', '수', '목', '금', '토'];
    const weekday = weekdays[date.getDay()];
    
    return `${year}.${month}.${day}(${weekday})`;
}

// 날짜 랭킹 아이템 생성
function createDateRankingItem(dateData, rank) {
    const item = document.createElement('div');
    // 랭킹별 클래스 추가 (1순위, 2순위, 3순위)
    item.className = `ranking-item rank-${rank}`;
    
    const rankingNumber = document.createElement('div');
    rankingNumber.className = 'ranking-number';
    rankingNumber.textContent = `${rank}순위`;
    
    const dateInfo = document.createElement('div');
    dateInfo.className = 'date-info';
    
    const dateText = document.createElement('div');
    dateText.className = 'date-text';
    dateText.textContent = formatDate(dateData.date);
    
    const attendanceInfo = document.createElement('div');
    attendanceInfo.className = 'attendance-info';
    attendanceInfo.textContent = `${dateData.totalMembers}명 중 ${dateData.availableMembers}명 참석 가능`;
    
    dateInfo.appendChild(dateText);
    dateInfo.appendChild(attendanceInfo);
    
    item.appendChild(rankingNumber);
    item.appendChild(dateInfo);
    
    return item;
}

// 장소 랭킹 아이템 생성
function createPlaceRankingItem(placeData, rank) {
    const item = document.createElement('div');
    // 랭킹별 클래스 추가 (1순위, 2순위, 3순위)
    item.className = `ranking-item rank-${rank}`;
    
    const rankingNumber = document.createElement('div');
    rankingNumber.className = 'ranking-number';
    rankingNumber.textContent = `${rank}순위`;
    
    const placeInfo = document.createElement('div');
    placeInfo.className = 'place-info';
    
    const placeHeader = document.createElement('div');
    placeHeader.className = 'place-header';
    
    const placeName = document.createElement('div');
    placeName.className = 'place-name';
    placeName.textContent = placeData.name;
    placeHeader.appendChild(placeName);
    
    const recommendationReason = document.createElement('div');
    recommendationReason.className = 'recommendation-reason';
    recommendationReason.textContent = placeData.reason;
    
    placeInfo.appendChild(placeHeader);
    placeInfo.appendChild(recommendationReason);
    
    item.appendChild(rankingNumber);
    item.appendChild(placeInfo);
    
    return item;
}

// 결과 표시
function displayResults(results) {
    // 로딩 상태 숨기기
    if (dom.loadingContainer) {
        dom.loadingContainer.style.display = 'none';
    }
    
    // 결과 상태 표시
    if (dom.resultContainer) {
        dom.resultContainer.style.display = 'block';
    }
    
    // 날짜 랭킹 표시 (최대 3위까지만)
    if (dom.dateRankingList && results.dates) {
        dom.dateRankingList.innerHTML = '';
        const dateList = results.dates.slice(0, 3); // 최대 3위까지만
        dateList.forEach((dateData, index) => {
            const item = createDateRankingItem(dateData, index + 1);
            dom.dateRankingList.appendChild(item);
        });
    }
    
    // 장소 랭킹 표시 (최대 3위까지만)
    if (dom.placeRankingList && results.places) {
        dom.placeRankingList.innerHTML = '';
        const placeList = results.places.slice(0, 3); // 최대 3위까지만
        placeList.forEach((placeData, index) => {
            const item = createPlaceRankingItem(placeData, index + 1);
            dom.placeRankingList.appendChild(item);
        });
    }
}

// 정산 페이지로 이동
function handleSettlementClick() {
    const groupId = getGroupIdFromUrl();
    if (groupId) {
        window.location.href = `../CalculatePage/calculatePage.html?groupId=${groupId}`;
    } else {
        window.location.href = '../CalculatePage/calculatePage.html';
    }
}

// 초기화
async function init() {
    // DOM 요소 초기화
    dom.loadingContainer = document.getElementById('loadingContainer');
    dom.resultContainer = document.getElementById('resultContainer');
    dom.dateRankingList = document.getElementById('dateRankingList');
    dom.placeRankingList = document.getElementById('placeRankingList');
    dom.settlementBtn = document.getElementById('settlementBtn');
    
    // 헤더 로드
    const headerContainer = document.getElementById('header-container');
    await loadHeader(headerContainer);
    await initializeBackButton();
    
    // 정산 버튼 이벤트 리스너
    if (dom.settlementBtn) {
        dom.settlementBtn.addEventListener('click', handleSettlementClick);
    }
    
    // groupId 가져오기
    const groupId = getGroupIdFromUrl();
    
    if (!groupId) {
        showToast('그룹 ID가 없습니다. 메인 페이지로 이동합니다.');
        setTimeout(() => {
            window.location.href = '../MainPage/main.html';
        }, 2000);
        return;
    }
    
    // 결과 조회
    try {
        const results = await fetchResults(groupId);
        displayResults(results);
    } catch (error) {
        console.error('Error loading results:', error);
        showToast('결과를 불러오는 중 오류가 발생했습니다.');
        
        // 에러 발생 시 메인 페이지로 이동
        setTimeout(() => {
            window.location.href = '../MainPage/main.html';
        }, 2000);
    }
}

// 페이지 로드 시 초기화
window.addEventListener('DOMContentLoaded', init);

