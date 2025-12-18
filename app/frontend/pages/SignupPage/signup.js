import { isInvalidNickname, isInvalidId, isInvalidPassword } from '../../js/common/validators.js';
import { loadHeader, initializeBackButton } from '../../layout/header-with-back-button/header.js';

window.addEventListener('DOMContentLoaded', async () => {
    // 헤더 로드
    const headerContainer = document.getElementById('header-container');
    await loadHeader(headerContainer);
    await initializeBackButton();
});

const nicknameInput = document.getElementById('nickname');
const idInput = document.getElementById('id');
const passwordInput = document.getElementById('password');
const loginBtn = document.getElementById('loginBtn');
const helperText = document.querySelector('.helper-text');

nicknameInput.addEventListener('input', () => {
    const nickename = nicknameInput.value;
    if (isInvalidNickname(nickename)) {
        helperText.textContent = '닉네임은 10자 이내여야 하며, 공백을 포함할 수 없습니다.';
    } else {
        helperText.textContent = '';
    }
    updateButtonState();
})

idInput.addEventListener('input', () => {
    const id = idInput.value;
    if (isInvalidId(id)) {
        helperText.textContent = 'ID는 영문과 숫자를 포함한 8자 이상이어야 합니다.';
    } else {
        helperText.textContent = '';
    }
    updateButtonState();
})

passwordInput.addEventListener('input', () => {
    const password = passwordInput.value;
    if (isInvalidPassword(password)) {
        helperText.textContent = '비밀번호는 숫자 4자리여야 합니다.';
    } else {
        helperText.textContent = '';
    }
    updateButtonState();
})

function updateButtonState() {
    const isFormValid = 
        !isInvalidNickname(nicknameInput.value) &&
        !isInvalidId(idInput.value) &&
        !isInvalidPassword(passwordInput.value);
    
    loginBtn.disabled = !isFormValid;
}
