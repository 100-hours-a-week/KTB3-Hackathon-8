import { isInvalidNickname } from '../../js/common/validators.js';
import { loadHeader } from '../../layout/header/header.js';
import { getGroupSettings } from '../../js/api/GenerateGroupApi.js';
import { showToast } from '../../js/common/messages.js';
import { fetchAPIWithBody } from '../../js/api/api_fetcher.js';

// 전역 변수
let groupSettings = null;
let groupId = null;

// URL에서 groupId 추출
function getGroupIdFromUrl() {
    const urlParams = new URLSearchParams(window.location.search);
    return urlParams.get('groupId');
}

// 그룹 설정값 조회 및 처리
async function loadGroupSettings(groupIdParam) {
    try {
        const response = await getGroupSettings(groupIdParam);
        
        if (!response.ok) {
            throw new Error(`HTTP error! status: ${response.status}`);
        }
        
        // 백엔드 응답: GroupMeta { isOwner: boolean, ownerNickname: String, hasScheduledDate: boolean, startDate: Date, endDate: Date }
        const data = await response.json();
        groupSettings = data;
        
        console.log('Group settings loaded:', data);
        
        // 1. 날짜 컨테이너 표시/숨김 처리
        const dateCategory = document.querySelector('.date-category');
        const dateFieldGroup = dateCategory?.nextElementSibling;
        
        if (data.hasScheduledDate) {
            // 날짜가 설정되어 있으면 표시
            if (dateCategory) dateCategory.style.display = 'block';
            if (dateFieldGroup) dateFieldGroup.style.display = 'flex';
        } else {
            // 날짜가 설정되어 있지 않으면 숨김
            if (dateCategory) dateCategory.style.display = 'none';
            if (dateFieldGroup) dateFieldGroup.style.display = 'none';
        }
        
        // 2. isOwner에 따른 닉네임 자동 채우기
        const nicknameInput = document.getElementById('nickname');
        if (data.isOwner && data.ownerNickname && nicknameInput) {
            nicknameInput.value = data.ownerNickname;
            nicknameInput.disabled = true; // 총무는 닉네임 수정 불가
            updateButtonState();
        }
        
        // 날짜 범위 제한 설정 (필요시)
        if (data.hasScheduledDate && data.startDate && data.endDate) {
            // flatpickr 날짜 범위 제한 설정
            const datesInput = document.getElementById('dates');
            if (datesInput && window.flatpickr) {
                const fp = datesInput._flatpickr;
                if (fp) {
                    fp.set('minDate', new Date(data.startDate));
                    fp.set('maxDate', new Date(data.endDate));
                }
            }
        }
        
        return data;
    } catch (error) {
        console.error('Error loading group settings:', error);
        showToast('그룹 설정을 불러오는 중 오류가 발생했습니다.');
        throw error;
    }
}

// 폼 제출 처리
async function handleFormSubmit(e) {
    e.preventDefault();
    
    if (!groupId) {
        showToast('그룹 ID가 없습니다.');
        return;
    }
    
    const nicknameInput = document.getElementById('nickname');
    // 성별과 나이 select는 name이 "language"로 되어 있음 (HTML 구조상)
    const selects = document.querySelectorAll('select[name="language"]');
    const genderSelect = selects[0]; // 성별 select
    const ageSelect = selects[1]; // 나이 select
    const datesInput = document.getElementById('dates');
    const likeInput = document.getElementById('like');
    const dislikeInput = document.getElementById('dislike');
    const exceptionInput = document.getElementById('exception');
    
    // 닉네임 검증
    if (!nicknameInput || isInvalidNickname(nicknameInput.value)) {
        showToast('닉네임을 올바르게 입력해주세요.');
        return;
    }
    
    // 날짜 파싱 (flatpickr 형식: "2024-12-25,2024-12-26")
    let excludedDates = [];
    if (datesInput && datesInput.value && groupSettings?.hasScheduledDate) {
        const dateStrings = datesInput.value.split(',');
        excludedDates = dateStrings
            .map(dateStr => dateStr.trim())
            .filter(dateStr => dateStr)
            .map(dateStr => {
                // "YYYY-MM-DD" 형식을 Date로 변환
                const date = new Date(dateStr + 'T00:00:00');
                return date.toISOString();
            });
    }
    
    // 제출 데이터 준비
    const submitData = {
        nickname: nicknameInput.value,
        gender: genderSelect?.value || null,
        age: ageSelect ? parseInt(ageSelect.value.replace('대', '')) : null,
        excluded_dates: excludedDates,
        preferred_foods: likeInput?.value || '',
        avoided_foods: dislikeInput?.value || '',
        excluded_foods: exceptionInput?.value || ''
    };
    
    try {
        const response = await fetchAPIWithBody(
            `/api/v1/submission/${groupId}/user`,
            'POST',
            JSON.stringify(submitData)
        );
        
        if (response.ok) {
            showToast('제출이 완료되었습니다!');
            
            // 3. isOwner에 따른 페이지 이동
            if (groupSettings?.isOwner) {
                // 총무는 main page로 이동
                const currentPath = window.location.pathname;
                const mainPagePath = currentPath.replace(/\/pages\/[^/]+\/[^/]+\.html$/, '/pages/MainPage/main.html');
                window.location.href = `${window.location.origin}${mainPagePath}`;
            } else {
                // 일반 사용자는 submitCompleted page로 이동
                const currentPath = window.location.pathname;
                const submitCompletedPath = currentPath.replace(/\/pages\/[^/]+\/[^/]+\.html$/, '/pages/SubmitCompletedPage/submitCompleted.html');
                window.location.href = `${window.location.origin}${submitCompletedPath}`;
            }
        } else {
            const errorData = await response.json().catch(() => ({}));
            showToast(errorData.message || '제출에 실패했습니다.');
        }
    } catch (error) {
        console.error('Error submitting form:', error);
        showToast('제출 중 오류가 발생했습니다.');
    }
}

window.addEventListener('DOMContentLoaded', async () => {
    // 헤더 로드
    const headerContainer = document.getElementById('header-container');
    await loadHeader(headerContainer);
    
    // groupId 가져오기
    groupId = getGroupIdFromUrl();
    
    if (groupId) {
        // 그룹 설정값 조회
        try {
            await loadGroupSettings(groupId);
        } catch (error) {
            console.error('Failed to load group settings:', error);
        }
    } else {
        showToast('그룹 ID가 없습니다.');
    }
    
    // 폼 제출 이벤트 리스너
    const submitForm = document.querySelector('.submit-form');
    if (submitForm) {
        submitForm.addEventListener('submit', handleFormSubmit);
    }
});

const nicknameInput = document.getElementById('nickname');
const helperText = document.querySelector('.nickname-helper-text');
const submitBtn = document.getElementById('submitBtn');

if (nicknameInput && helperText) {
    nicknameInput.addEventListener('input', () => {
        const nickname = nicknameInput.value;
        if (isInvalidNickname(nickname)) {
            helperText.textContent = '닉네임은 10자 이내여야 하며, 공백을 포함할 수 없습니다.';
        } else {
            helperText.textContent = '';
        }
        updateButtonState();
    });
}

function updateButtonState() {
    if (!submitBtn || !nicknameInput) return;
    
    const isFormValid = !isInvalidNickname(nicknameInput.value);
    submitBtn.disabled = !isFormValid;
}
