
/**
 * 회원가입, 로그인 폼 유효성 검사 함수들
 */

/**
 * 폼 데이터에서 닉네임 유효성 검사
 * @param {string} nickname - 닉네임
 * @returns {boolean} - 유효 여부
 * 
 * nickname: 10자 이내 입력 (+ 공백 금지)
 */
export function isInvalidNickname(nickname) {
  return (
    nickname.length === 0 ||
    nickname.length > 10 ||
    /\s/.test(nickname)
  );
}

/**
 * 폼 데이터에서 아이디 유효성 검사
 * @param {string} id - 아이디
 * @returns {boolean} - 유효 여부
 * 
 * id: 영문/숫자 포함 8자 이상 (영문+숫자만 허용, 둘 다 반드시 포함)
 */
export function isInvalidId(id) {
  return (
    id.length < 8 ||
    !/^[A-Za-z0-9]+$/.test(id) || // 영문/숫자만
    !/[A-Za-z]/.test(id) ||       // 영문 포함
    !/[0-9]/.test(id)             // 숫자 포함
  );
}

/**
 * 폼 데이터에서 패스워드 유효성 검사
 * @param {string} password - 패스워드
 * @returns {boolean} - 유효 여부
 * 
 * password: 숫자 4자리
 */
export function isInvalidPassword(password) {
  return !/^\d{4}$/.test(password);
}

/**
 * 그룹 생성 폼 유효성 검사 함수들
 */

/**
 * 그룹 생성 폼의 필수 필드 검증 (버튼 활성화/비활성화용)
 * @param {Object} formData - 폼 데이터 객체
 * @param {string} formData.memberCount - 멤버 수
 * @param {string} formData.budgetMin - 예산 최소값
 * @param {string} formData.budgetMax - 예산 최대값
 * @param {string} formData.station - 모임 장소
 * @param {boolean} formData.dateToggleChecked - 날짜 토글 상태
 * @param {string} formData.dateStart - 시작 날짜
 * @param {string} formData.dateEnd - 종료 날짜
 * @returns {boolean} - 유효성 검사 통과 여부
 */
export function validateGroupForm(formData) {
    const { memberCount, budgetMin, budgetMax, station, dateToggleChecked, dateStart, dateEnd } = formData;
    
    // 기본 필수 필드 검증
    if (!memberCount || !budgetMin || !budgetMax || !station) {
        return false;
    }
    
    // 날짜 토글이 켜져 있으면 날짜도 필수
    if (dateToggleChecked) {
        if (!dateStart || !dateEnd) {
            return false;
        }
        
        // 날짜 범위 검증
        if (new Date(dateStart) > new Date(dateEnd)) {
            return false;
        }
    }
    
    return true;
}

/**
 * 그룹 생성 폼 제출 전 검증 (에러 메시지 반환)
 * @param {Object} formData - 폼 데이터 객체
 * @param {string} formData.memberCount - 멤버 수
 * @param {string} formData.budgetMin - 예산 최소값
 * @param {string} formData.budgetMax - 예산 최대값
 * @param {string} formData.station - 모임 장소
 * @param {boolean} formData.dateToggleChecked - 날짜 토글 상태
 * @param {string} formData.dateStart - 시작 날짜
 * @param {string} formData.dateEnd - 종료 날짜
 * @returns {Object} - { isValid: boolean, errorMessage: string }
 */
export function validateGroupFormSubmit(formData) {
    const { memberCount, budgetMin, budgetMax, station, dateToggleChecked, dateStart, dateEnd } = formData;
    
    // 기본 필수 필드 검증
    if (!memberCount || !budgetMin || !budgetMax || !station) {
        return {
            isValid: false,
            errorMessage: '필수 항목을 모두 입력해주세요.'
        };
    }
    
    // 날짜 검증
    if (dateToggleChecked) {
        if (!dateStart || !dateEnd) {
            return {
                isValid: false,
                errorMessage: '날짜 범위를 입력해주세요.'
            };
        }
        
        if (new Date(dateStart) > new Date(dateEnd)) {
            return {
                isValid: false,
                errorMessage: '시작 날짜가 종료 날짜보다 늦을 수 없습니다.'
            };
        }
    }
    
    // 예산 범위 검증
    const budgetMinNum = parseInt(budgetMin);
    const budgetMaxNum = parseInt(budgetMax);
    
    if (budgetMinNum < 0 || budgetMaxNum < 0) {
        return {
            isValid: false,
            errorMessage: '예산은 0 이상이어야 합니다.'
        };
    }
    
    if (budgetMinNum > budgetMaxNum) {
        return {
            isValid: false,
            errorMessage: '최소 예산이 최대 예산보다 클 수 없습니다.'
        };
    }
    
    // 멤버 수 검증
    const memberCountNum = parseInt(memberCount);
    if (memberCountNum < 1) {
        return {
            isValid: false,
            errorMessage: '멤버 수는 1명 이상이어야 합니다.'
        };
    }
    
    return {
        isValid: true,
        errorMessage: null
    };
}

/**
 * 폼 데이터에서 그룹 생성 검증용 데이터 추출
 * @param {HTMLElement} formElement - 폼 요소
 * @returns {Object} - 검증용 폼 데이터
 */
export function extractGroupFormData(formElement) {
    const memberCount = formElement.querySelector('#memberCount')?.value?.trim() || '';
    const budgetMin = formElement.querySelector('#budgetMin')?.value?.trim() || '';
    const budgetMax = formElement.querySelector('#budgetMax')?.value?.trim() || '';
    const station = formElement.querySelector('#stationInput')?.value?.trim() || '';
    const dateToggle = formElement.querySelector('#dateToggle');
    const dateToggleChecked = dateToggle?.checked || false;
    const dateStart = formElement.querySelector('#dateStart')?.value || '';
    const dateEnd = formElement.querySelector('#dateEnd')?.value || '';
    
    return {
        memberCount,
        budgetMin,
        budgetMax,
        station,
        dateToggleChecked,
        dateStart,
        dateEnd
    };
}