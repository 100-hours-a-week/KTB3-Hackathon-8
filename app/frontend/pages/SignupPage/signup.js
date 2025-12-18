import { isInvalidNickname, isInvalidId, isInvalidPassword } from '../../js/common/validators.js';
import { loadHeader, initializeBackButton } from '../../layout/header-with-back-button/header.js';

window.addEventListener('DOMContentLoaded', async () => {
    // 헤더 로드
    const headerContainer = document.getElementById('header-container');
    await loadHeader(headerContainer);
    await initializeBackButton();
    
    // DOM 요소 가져오기
    const nicknameInput = document.getElementById('nickname');
    const idInput = document.getElementById('id');
    const passwordInput = document.getElementById('password');
    const authBtn = document.getElementById('authBtn');
    const helperText = document.querySelector('.helper-text');
    
    // 요소가 존재하는지 확인
    if (!nicknameInput || !idInput || !passwordInput || !authBtn || !helperText) {
        console.error('Required DOM elements not found');
        return;
    }
    
    function updateButtonState() {
        const isFormValid = 
            !isInvalidNickname(nicknameInput.value) &&
            !isInvalidId(idInput.value) &&
            !isInvalidPassword(passwordInput.value);
        
        authBtn.disabled = !isFormValid;
    }
    
    nicknameInput.addEventListener('input', () => {
        const nickname = nicknameInput.value;
        if (isInvalidNickname(nickname)) {
            helperText.textContent = '닉네임은 10자 이내여야 하며, 공백을 포함할 수 없습니다.';
        } else {
            helperText.textContent = '';
        }
        updateButtonState();
    });
    
    idInput.addEventListener('input', () => {
        const id = idInput.value;
        if (isInvalidId(id)) {
            helperText.textContent = 'ID는 영문과 숫자를 포함한 8자 이상이어야 합니다.';
        } else {
            helperText.textContent = '';
        }
        updateButtonState();
    });
    
    passwordInput.addEventListener('input', () => {
        const password = passwordInput.value;
        if (isInvalidPassword(password)) {
            helperText.textContent = '비밀번호는 숫자 4자리여야 합니다.';
        } else {
            helperText.textContent = '';
        }
        updateButtonState();
    });
    
    // 초기 버튼 상태 설정
    updateButtonState();
});
