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
        
        // 이미지 경로 동적 설정 (현재 페이지 기준)
        const profileImage = container 
            ? container.querySelector('.profile-image')
            : document.querySelector('.profile-image');
        
        if (profileImage) {
            // 현재 페이지의 위치를 기준으로 assets 경로 계산
            const currentPath = window.location.pathname;
            let assetsPath = '../../assets/profile.jpeg'; // 기본 경로
            
            if (currentPath.includes('/pages/')) {
                // pages 폴더 내에서 사용: ../../assets/profile.jpeg
                assetsPath = '../../assets/profile.jpeg';
            } else if (currentPath.includes('/frontend/')) {
                // frontend 루트에서 사용: ./assets/profile.jpeg
                assetsPath = './assets/profile.jpeg';
            }
            
            profileImage.src = assetsPath;
        }
    } catch (error) {
        console.error('Error loading header:', error);
    }
}

