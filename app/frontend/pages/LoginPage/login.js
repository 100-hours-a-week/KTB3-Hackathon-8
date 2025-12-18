import { loadHeader, initializeBackButton } from '../../layout/header-with-back-button/header.js';

window.addEventListener('DOMContentLoaded', async () => {
    // 헤더 로드
    const headerContainer = document.getElementById('header-container');
    await loadHeader(headerContainer);
    await initializeBackButton();
});