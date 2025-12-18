/**
 * 헤더 컴포넌트 로드 함수
 * @param {HTMLElement} container - 헤더를 삽입할 컨테이너 요소
 */
export async function loadHeader(container) {
    try {
        const response = await fetch('../../layout/header/header.html');
        const html = await response.text();
        
        if (container) {
            container.innerHTML = html;
        } else {
            // container가 없으면 body의 첫 번째 자식으로 삽입
            const tempDiv = document.createElement('div');
            tempDiv.innerHTML = html;
            const header = tempDiv.firstElementChild;
            document.body.insertBefore(header, document.body.firstChild);
        }
    } catch (error) {
        console.error('Error loading header:', error);
    }
}

