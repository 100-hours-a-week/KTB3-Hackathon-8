import { showToast } from '../../js/common/messages.js';
import { loadHeader } from '../../layout/header/header.js';
import { validateGroupForm, validateGroupFormSubmit, extractGroupFormData, loadStations, getStations } from '../../js/common/validators.js';
import { createGroup, getGroupStatus } from '../../js/api/GenerateGroupApi.js';
import { submitAggregation } from '../../js/api/SubmissionAPI.js';
import { getGroupIdFromSession } from '../../js/common/sessionManagers.js';
import { getGroupSubmitStatus } from '../../js/api/GroupApi.js';

// 날짜 형식 변환 함수
function formatDateTime(dateTimeString) {
    if (!dateTimeString) return null;
    return dateTimeString.replace('T', ' ') + ':00';
}

// 전역 상태
let groupData = null;
let submissionData = {
    submitted: 0,
    total: 0,
    members: []
};

// DOM 요소 관리 객체
const dom = {
    modalOverlay: null,
    modalClose: null,
    groupCreateBtn: null,
    groupForm: null,
    dateToggle: null,
    dateArea: null,
    stationInput: null,
    stationDropdown: null,
    urlSection: null,
    urlValue: null,
    copyBtn: null,
    submissionStatus: null,
    statusDisplay: null,
    submissionList: null,
    memberList: null,
    alertMessage: null,
    alertClose: null,
    myPickBtn: null,
    submitAllBtn: null,
    viewResultBtn: null,
    submitBtn: null
};

// DOM 요소 초기화
function initDOM() {
    dom.modalOverlay = document.getElementById('modalOverlay');
    dom.modalClose = document.getElementById('modalClose');
    dom.groupCreateBtn = document.getElementById('groupCreateBtn');
    dom.groupForm = document.getElementById('groupForm');
    dom.dateToggle = document.getElementById('dateToggle');
    dom.dateArea = document.getElementById('dateArea');
    dom.stationInput = document.getElementById('stationInput');
    dom.stationDropdown = document.getElementById('stationDropdown');
    dom.urlSection = document.getElementById('urlSection');
    dom.urlValue = document.getElementById('urlValue');
    dom.copyBtn = document.getElementById('copyBtn');
    dom.submissionStatus = document.getElementById('submissionStatus');
    dom.statusDisplay = document.getElementById('statusDisplay');
    dom.submissionList = document.getElementById('submissionList');
    dom.memberList = document.getElementById('memberList');
    dom.alertMessage = document.getElementById('alertMessage');
    dom.alertClose = document.getElementById('alertClose');
    dom.myPickBtn = document.getElementById('myPickBtn');
    dom.submitAllBtn = document.getElementById('submitAllBtn');
    dom.viewResultBtn = document.getElementById('viewResultBtn');
    dom.submitBtn = document.getElementById('submitBtn');
}

// 모달 관리
const modal = {
    open() {
        if (dom.modalOverlay) {
            dom.modalOverlay.classList.add('active');
            document.body.style.overflow = 'hidden';
        }
    },
    
    close() {
        if (dom.modalOverlay) {
            dom.modalOverlay.classList.remove('active');
            document.body.style.overflow = 'auto';
        }
        if (dom.groupForm) dom.groupForm.reset();
        if (dom.dateArea) dom.dateArea.style.display = 'none';
        if (dom.dateToggle) dom.dateToggle.checked = false;
        if (dom.stationDropdown) dom.stationDropdown.style.display = 'none';
        if (dom.submitBtn) dom.submitBtn.disabled = true;
    }
};

// 폼 검증
function validateForm() {
    if (!dom.groupForm || !dom.submitBtn) return false;
    
    const formData = extractGroupFormData(dom.groupForm);
    const isValid = validateGroupForm(formData);
    
    dom.submitBtn.disabled = !isValid;
    return isValid;
}

// 역 검색 드롭다운
function displayStationDropdown(stations) {
    if (!dom.stationDropdown) return;
    
    dom.stationDropdown.innerHTML = '';
    stations.forEach(station => {
        const item = document.createElement('div');
        item.className = 'station-dropdown-item';
        item.textContent = station;
        item.addEventListener('mousedown', (e) => {
            e.preventDefault(); // blur 이벤트가 발생하지 않도록 함
        });
        item.addEventListener('click', (e) => {
            e.stopPropagation(); // 이벤트 전파 중단
            if (dom.stationInput) {
                dom.stationInput.value = station;
                // input 이벤트도 트리거하여 검증 함수가 실행되도록 함
                dom.stationInput.dispatchEvent(new Event('input', { bubbles: true }));
            }
            if (dom.stationDropdown) {
                dom.stationDropdown.style.display = 'none';
            }
            validateForm();
        });
        dom.stationDropdown.appendChild(item);
    });
    dom.stationDropdown.style.display = 'block';
}

// 날짜 토글 처리
function handleDateToggle(e) {
    if (e.target.checked) {
        if (dom.dateArea) dom.dateArea.style.display = 'block';
        const dateStart = document.getElementById('dateStart');
        const dateEnd = document.getElementById('dateEnd');
        if (dateStart) dateStart.required = true;
        if (dateEnd) dateEnd.required = true;
    } else {
        if (dom.dateArea) dom.dateArea.style.display = 'none';
        const dateStart = document.getElementById('dateStart');
        const dateEnd = document.getElementById('dateEnd');
        if (dateStart) {
            dateStart.required = false;
            dateStart.value = '';
        }
        if (dateEnd) {
            dateEnd.required = false;
            dateEnd.value = '';
        }
    }
    validateForm();
}

// 역 검색 처리
function handleStationSearch(e) {
    const searchTerm = e.target.value.toLowerCase();
    const inputValue = e.target.value.trim();
    const stations = getStations();
    
    if (searchTerm.length === 0) {
        if (dom.stationDropdown) dom.stationDropdown.style.display = 'none';
    } else {
        const filteredStations = stations.filter(station => 
            station.toLowerCase().includes(searchTerm)
        );
        if (filteredStations.length > 0) {
            displayStationDropdown(filteredStations);
        } else {
            if (dom.stationDropdown) dom.stationDropdown.style.display = 'none';
        }
        
        // 입력값이 유효한 역 목록에 있는지 확인
        // 완전히 일치하는 역이 없으면 입력값을 초기화
        if (inputValue && !stations.includes(inputValue)) {
            // 드롭다운이 열려있지 않으면 입력값이 유효하지 않음
            // 하지만 사용자가 입력 중일 수 있으므로 검증만 수행
        }
    }
    validateForm();
}

// 폼 제출 처리
async function handleFormSubmit(e) {
    e.preventDefault();

    const formInputData = extractGroupFormData(dom.groupForm);
    const validation = validateGroupFormSubmit(formInputData);
    
    if (!validation.isValid) {
        showToast(validation.errorMessage);
        return;
    }
    
    const { memberCount, budget, station } = formInputData;

    // 데이터 준비
    const budgetWon = parseInt(budget) * 10000;
    
    const formData = {
        max_capacity: memberCount,
        has_scheduled_Date: formInputData.dateToggleChecked,
        start_date: formInputData.dateToggleChecked ? formatDateTime(formInputData.dateStart) : null,
        end_date: formInputData.dateToggleChecked ? formatDateTime(formInputData.dateEnd) : null,
        station: station.replace('역', ''),
        budget: budgetWon  // 정수로 전송
    };

    try {
        const response = await createGroup(formData);

        if (response.ok) {
            const data = await response.json();
            
            // 백엔드 응답: CreateGroupIdResponse { groupId: Long }
            groupData = { id: data.groupId };
            submissionData.total = parseInt(memberCount);

            // 클라이언트에서 URL 생성 (절대 경로 사용)
            if (data.groupId && dom.urlValue) {
                // 프로젝트 구조를 고려하여 절대 URL 생성
                // /pages/MainPage/main.html -> /pages/SubmitFormPage/submitForm.html
                const currentPath = window.location.pathname;
                // /pages/ 디렉토리 내의 모든 페이지에서 /pages/SubmitFormPage/submitForm.html로 변환
                const submitFormPath = currentPath.replace(/\/pages\/[^/]+\/[^/]+\.html$/, '/pages/SubmitFormPage/submitForm.html');
                const submitFormUrl = `${window.location.origin}${submitFormPath}?groupId=${data.groupId}`;
                dom.urlValue.textContent = submitFormUrl;
                if (dom.urlSection) dom.urlSection.style.display = 'block';
            }

            modal.close();
            showToast('그룹이 생성되었습니다!');
            updateSubmissionStatus(0, parseInt(memberCount));

            // 그룹 생성 후 UI 업데이트
            const emptyState = document.getElementById('emptyState');
            const actionButtons = document.getElementById('actionButtons');
            if (emptyState) emptyState.style.display = 'none';
            if (actionButtons) actionButtons.style.display = 'block';
            
            startStatusPolling();
        } else {
            const errorData = await response.json().catch(() => ({}));
            showToast(errorData.message || '그룹 생성에 실패했습니다.');
        }
    } catch (error) {
        console.error('Error creating group:', error);
        showToast('그룹 생성 중 오류가 발생했습니다.');
    }
}

// URL 복사
async function handleCopyUrl() {
    if (!dom.urlValue) return;
    
    const url = dom.urlValue.textContent;
    
    try {
        await navigator.clipboard.writeText(url);
        showToast('클립보드에 복사되었습니다.');
    } catch (error) {
        console.error('Failed to copy:', error);
        const textArea = document.createElement('textarea');
        textArea.value = url;
        document.body.appendChild(textArea);
        textArea.select();
        document.execCommand('copy');
        document.body.removeChild(textArea);
        showToast('클립보드에 복사되었습니다.');
    }
}

// 제출 현황 클릭 처리 (더 이상 필요 없음 - 멤버 리스트는 항상 표시)

// 나의 회식픽 버튼 클릭
function handleMyPickClick() {
    if (groupData?.id) {
        // 절대 경로 사용
        const currentPath = window.location.pathname;
        const submitFormPath = currentPath.replace(/\/pages\/[^/]+\/[^/]+\.html$/, '/pages/SubmitFormPage/submitForm.html');
        const submitFormUrl = `${window.location.origin}${submitFormPath}?groupId=${groupData.id}`;
        window.location.href = submitFormUrl;
    } else {
        showToast('그룹 정보가 없습니다.');
    }
}

// 전체 회식픽 제출 버튼 클릭
async function handleSubmitAllClick() {
    try {
        const groupId = getGroupIdFromSession();
        const response = await submitAggregation(groupId);

        if (response.ok) {
            showToast('전체 회식픽이 제출되었습니다.');
            if (dom.myPickBtn) dom.myPickBtn.disabled = true;
            if (dom.submitAllBtn) dom.submitAllBtn.disabled = true;
            if (dom.viewResultBtn) dom.viewResultBtn.disabled = false;
        } else {
            showToast('제출에 실패했습니다.');
        }
    } catch (error) {
        console.error('Error submitting all:', error);
        showToast('제출 중 오류가 발생했습니다.');
    }
}

// 결과보기 버튼 클릭
function handleViewResultClick() {
    if (groupData?.id) {
        // 절대 경로 사용
        const currentPath = window.location.pathname;
        const resultPagePath = currentPath.replace(/\/pages\/[^/]+\/[^/]+\.html$/, '/pages/ResultPage/resultPage.html');
        const resultPageUrl = `${window.location.origin}${resultPagePath}?groupId=${groupData.id}`;
        window.location.href = resultPageUrl;
    } else {
        showToast('그룹 정보를 찾을 수 없습니다.');
    }
}

// 제출 현황 업데이트
async function updateSubmissionStatus(submitted, total) {

    const submissionStatusResponse = await getGroupSubmitStatus(groupId, ownerId);
    let submitted;
    let total;
    let submissionList;
    if (submissionStatusResponse.ok) {
        const statusData = await submissionStatusResponse.json();
        submitted = statusData.submit_count || 0;
        total = statusData.total_user_count || 0;
        submissionList = statusData.user_nickname_list || [];
    }
    submissionData.submitted = submitted;
    submissionData.total = total;
    submissionData.members = submissionList;

    const submissionCountEl = document.getElementById('submissionCount');
    const totalMembersEl = document.getElementById('totalMembers');
    
    if (submissionCountEl) submissionCountEl.textContent = submitted;
    if (totalMembersEl) totalMembersEl.textContent = total;

    if (dom.submissionStatus) {
        dom.submissionStatus.style.display = 'block';
        // 제출 현황이 표시되면 자동으로 멤버 목록 로드
        displayMemberList(submissionData.members);
    }

    if (submitted === total && total > 0) {
        if (dom.alertMessage) dom.alertMessage.style.display = 'block';
        if (dom.myPickBtn) dom.myPickBtn.disabled = true;
        if (dom.submitAllBtn) dom.submitAllBtn.disabled = true;
        if (dom.viewResultBtn) dom.viewResultBtn.disabled = false;
    }
}

// 멤버 리스트 표시
function displayMemberList(members) {
    if (!dom.memberList) return;
    
    dom.memberList.innerHTML = '';
    members.forEach(member => {
        const li = document.createElement('li');
        li.textContent = member;
        dom.memberList.appendChild(li);
    });
}

// 상태 폴링
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
            const response = await getGroupStatus(groupData.id);

            if (response.ok) {
                const data = await response.json();
                updateSubmissionStatus(data.submitted || 0, data.total || 0);
            }
        } catch (error) {
            console.error('Error fetching status:', error);
        }
    }, 5000);
}

// 이벤트 리스너 설정
function setupEventListeners() {
    // 모달
    if (dom.modalClose) dom.modalClose.addEventListener('click', modal.close);
    if (dom.groupCreateBtn) dom.groupCreateBtn.addEventListener('click', modal.open);

    // 날짜 토글
    if (dom.dateToggle) {
        dom.dateToggle.addEventListener('change', handleDateToggle);
    }

    // 역 검색
    if (dom.stationInput) {
        dom.stationInput.addEventListener('input', handleStationSearch);
        // blur 이벤트: 포커스를 잃을 때 유효한 역인지 확인
        // 단, 드롭다운 아이템을 클릭한 경우는 제외
        let isClickingDropdown = false;
        if (dom.stationDropdown) {
            dom.stationDropdown.addEventListener('mousedown', () => {
                isClickingDropdown = true;
            });
            dom.stationDropdown.addEventListener('mouseup', () => {
                // mouseup 후에 blur가 발생하므로 약간의 지연을 둠
                setTimeout(() => {
                    isClickingDropdown = false;
                }, 100);
            });
        }
        
        dom.stationInput.addEventListener('blur', (e) => {
            // 드롭다운 아이템을 클릭하는 중이면 blur 이벤트 무시
            if (isClickingDropdown) {
                return;
            }
            
            const inputValue = e.target.value.trim();
            const stations = getStations();
            // 입력값이 있고, 유효한 역 목록에 없으면 초기화
            if (inputValue && !stations.includes(inputValue)) {
                e.target.value = '';
                if (dom.stationDropdown) dom.stationDropdown.style.display = 'none';
                validateForm();
            }
        });
    }

    // 외부 클릭 시 드롭다운 닫기 (이벤트 캡처 단계에서 처리)
    document.addEventListener('click', (e) => {
        if (dom.stationInput && dom.stationDropdown) {
            const clickedInside = dom.stationInput.contains(e.target) || 
                                  dom.stationDropdown.contains(e.target);
            if (!clickedInside && dom.stationDropdown.style.display !== 'none') {
                dom.stationDropdown.style.display = 'none';
            }
        }
    }, true); // 캡처 단계에서 처리

    // 폼 검증 이벤트
    const memberCountInput = document.getElementById('memberCount');
    const budgetInput = document.getElementById('budget');
    const dateStartInput = document.getElementById('dateStart');
    const dateEndInput = document.getElementById('dateEnd');
    
    if (memberCountInput) memberCountInput.addEventListener('input', validateForm);
    if (budgetInput) budgetInput.addEventListener('input', validateForm);
    if (dateStartInput) dateStartInput.addEventListener('input', validateForm);
    if (dateEndInput) dateEndInput.addEventListener('input', validateForm);

    // 폼 제출
    if (dom.groupForm) {
        dom.groupForm.addEventListener('submit', handleFormSubmit);
    }

    // URL 복사
    if (dom.copyBtn) {
        dom.copyBtn.addEventListener('click', handleCopyUrl);
    }

    // 제출 현황 클릭 제거 (멤버 리스트는 항상 표시됨)

    // 알림 닫기
    if (dom.alertClose) {
        dom.alertClose.addEventListener('click', () => {
            if (dom.alertMessage) dom.alertMessage.style.display = 'none';
        });
    }

    // 버튼들
    if (dom.myPickBtn) dom.myPickBtn.addEventListener('click', handleMyPickClick);
    if (dom.submitAllBtn) dom.submitAllBtn.addEventListener('click', handleSubmitAllClick);
    if (dom.viewResultBtn) dom.viewResultBtn.addEventListener('click', handleViewResultClick);
    
}

// 로그인 상태 확인 함수
function checkLoginStatus() {
    // 쿠키에서 JWT 토큰 확인
    // 일반적인 JWT 토큰 쿠키 이름들을 확인
    const cookieNames = ['accessToken', 'jwt', 'token', 'authToken'];
    
    for (const name of cookieNames) {
        const cookies = document.cookie.split(';');
        for (let cookie of cookies) {
            cookie = cookie.trim();
            if (cookie.startsWith(name + '=')) {
                const token = cookie.substring(name.length + 1);
                if (token && token !== '') {
                    return true;
                }
            }
        }
    }
    
    // localStorage에서도 확인 (대체 방법)
    const token = localStorage.getItem('token') || localStorage.getItem('accessToken') || localStorage.getItem('jwt');
    if (token && token !== '') {
        return true;
    }
    
    return false;
}

// 로그인 필요 toast 메시지 표시 및 리다이렉트
function showLoginRequiredToast() {
    showToast('로그인이 필요한 서비스입니다. 로그인 페이지로 이동합니다.');
    // 2초 후 로그인 페이지로 리다이렉트
    setTimeout(() => {
        window.location.href = '../../pages/LoginPage/login.html';
    }, 2000);
}

// 초기화
window.addEventListener('DOMContentLoaded', async () => {
    const headerContainer = document.getElementById('header-container');
    await loadHeader(headerContainer);
    
    // 역 목록 로드
    await loadStations();
    
    initDOM();
    setupEventListeners();
    
    // 로그인 상태 확인
    const isLoggedIn = checkLoginStatus();
    
    if (!isLoggedIn) {
        // 로그인하지 않은 경우 로그인 필요 toast 메시지 표시 및 리다이렉트
        showLoginRequiredToast();
    } else {
        // 로그인한 경우 그룹 생성 모달 표시
        modal.open();
    }
});
