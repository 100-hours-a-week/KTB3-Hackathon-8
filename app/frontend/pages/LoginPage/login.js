import { signIn } from '../../api/auth.js';
import { loadHeader, initializeBackButton } from '../../layout/header-with-back-button/header.js';

window.addEventListener('DOMContentLoaded', async () => {
    // 헤더 로드
    const headerContainer = document.getElementById('header-container');
    await loadHeader(headerContainer);
    await initializeBackButton();
});

const idInput = document.getElementById('id');
const passwordInput = document.getElementById('password');
const loginButton = document.getElementById('authBtn');

loginButton.addEventListener('click', async () => {
    const userId = idInput.value;
    const password = passwordInput.value;

    try {
        const response = await signIn(userId, password);
        if (response.ok) {
            window.location.href = '/pages/MainPage/main.html';
        } else if (response.status === 401) {
            // 인증 실패 처리
        } else {
            // 그외 400, 500번대 에러 처리
        }
    } catch (error) {
    }
});