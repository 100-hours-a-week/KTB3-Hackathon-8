import { isInvalidNickname } from '../../js/common/validators.js';
import { loadHeader } from '../../layout/header/header.js';

window.addEventListener('DOMContentLoaded', async () => {
    // 헤더 로드
    const headerContainer = document.getElementById('header-container');
    await loadHeader(headerContainer);
});

const nicknameInput = document.getElementById('nickname');
const helperText = document.querySelector('.nickname-helper-text');

nicknameInput.addEventListener('input', () => {
    const nickename = nicknameInput.value;
    if (isInvalidNickname(nickename)) {
        helperText.textContent = '닉네임은 10자 이내여야 하며, 공백을 포함할 수 없습니다.';
    } else {
        helperText.textContent = '';
    }
    updateButtonState();
})

function updateButtonState() {
    const isFormValid = 
        !isInvalidNickname(nicknameInput.value)
    
    loginBtn.disabled = !isFormValid;
}
