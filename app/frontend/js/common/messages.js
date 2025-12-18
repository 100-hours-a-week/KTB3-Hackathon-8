/**
 * Toast 메시지 표시 함수
 * @param {string} message - 표시할 메시지
 * @param {number} duration - 표시 시간 (밀리초, 기본값: 3000)
 */
export function showToast(message, duration = 3000) {
    const toastContainer = document.getElementById('toastContainer');
    
    if (!toastContainer) {
        console.error('Toast container not found');
        return;
    }

    // Toast 메시지 요소 생성
    const toast = document.createElement('div');
    toast.className = 'toast-message';
    toast.textContent = message;

    // 컨테이너에 추가
    toastContainer.appendChild(toast);

    // 애니메이션 후 제거
    setTimeout(() => {
        toast.classList.add('fade-out');
        setTimeout(() => {
            if (toast.parentNode) {
                toast.parentNode.removeChild(toast);
            }
        }, 300);
    }, duration);
}

