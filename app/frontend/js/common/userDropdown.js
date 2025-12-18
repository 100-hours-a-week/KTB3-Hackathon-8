import { signOut } from '../api/AuthApi.js';
import { setSessionAsLoggedOut } from './sessionManagers.js';   
/**
 * 로그인 상태 확인 함수
 */
export function checkLoginStatus() {
    // 쿠키에서 JWT 토큰 확인
    const cookieNames = ['accessToken', 'jwt', 'token', 'authToken'];
    
    for (const name of cookieNames) {
        const cookies = document.cookie.split(';');
        for (let cookie of cookies) {
            cookie = cookie.trim();
            if (cookie.startsWith(name + '=')) {
                const token = cookie.substring(name.length + 1);
                if (token && token !== '') {
                    return true;
                }
            }
        }
    }
    
    // localStorage에서도 확인 (대체 방법)
    const token = localStorage.getItem('token') || localStorage.getItem('accessToken') || localStorage.getItem('jwt');
    if (token && token !== '') {
        return true;
    }
    
    return false;
}

/**
 * 사용자 드롭다운 메뉴 초기화
 * @param {HTMLElement} container - 헤더 컨테이너 요소 (선택사항)
 */
export function initializeUserDropdown(container = null) {
    const userIcon = container 
        ? container.querySelector('#userIcon')
        : document.querySelector('#userIcon');
    const userDropdown = container 
        ? container.querySelector('#userDropdown')
        : document.querySelector('#userDropdown');
    const loginItem = container 
        ? container.querySelector('#loginItem')
        : document.querySelector('#loginItem');
    const signupItem = container 
        ? container.querySelector('#signupItem')
        : document.querySelector('#signupItem');
    const logoutItem = container 
        ? container.querySelector('#logoutItem')
        : document.querySelector('#logoutItem');
    
    if (!userIcon || !userDropdown) return;
    
    // 로그인 상태 확인
    const isLoggedIn = checkLoginStatus();
    
    // 로그인 상태에 따라 메뉴 항목 표시/숨김
    if (isLoggedIn) {
        if (loginItem) loginItem.style.display = 'none';
        if (signupItem) signupItem.style.display = 'none';
        if (logoutItem) logoutItem.style.display = 'block';
    } else {
        if (loginItem) loginItem.style.display = 'block';
        if (signupItem) signupItem.style.display = 'block';
        if (logoutItem) logoutItem.style.display = 'none';
    }
    
    // 프로필 아이콘 클릭 시 드롭다운 토글
    userIcon.addEventListener('click', (e) => {
        e.stopPropagation();
        if (userDropdown.style.display === 'none' || userDropdown.style.display === '') {
            userDropdown.style.display = 'block';
        } else {
            userDropdown.style.display = 'none';
        }
    });
    
    // 로그인 메뉴 클릭
    if (loginItem) {
        loginItem.addEventListener('click', (e) => {
            e.stopPropagation();
            window.location.href = '../../pages/LoginPage/login.html';
        });
    }
    
    // 회원가입 메뉴 클릭
    if (signupItem) {
        signupItem.addEventListener('click', (e) => {
            e.stopPropagation();
            window.location.href = '../../pages/SignupPage/signup.html';
        });
    }
    
    // 로그아웃 메뉴 클릭
    if (logoutItem) {
        logoutItem.addEventListener('click', async (e) => {
            e.stopPropagation();
            try {
                // 로그아웃 API 호출
                const response = await signOut();
                
                if (response.ok) {
                    // 쿠키와 localStorage 정리
                    document.cookie.split(";").forEach(c => {
                        document.cookie = c.replace(/^ +/, "").replace(/=.*/, "=;expires=" + new Date().toUTCString() + ";path=/");
                    });
                    localStorage.clear();
                    setSessionAsLoggedOut();
                    // 메인 페이지로 리다이렉트
                    window.location.href = '../../pages/MainPage/main.html';
                }
            } catch (error) {
                console.error('Logout error:', error);
                // 에러가 발생해도 로컬 정리 후 리다이렉트
                document.cookie.split(";").forEach(c => {
                    document.cookie = c.replace(/^ +/, "").replace(/=.*/, "=;expires=" + new Date().toUTCString() + ";path=/");
                });
                localStorage.clear();
                window.location.href = '../../pages/MainPage/main.html';
            }
        });
    }
    
    // 외부 클릭 시 드롭다운 닫기
    document.addEventListener('click', (e) => {
        if (userIcon && userDropdown) {
            const clickedInside = userIcon.contains(e.target) || userDropdown.contains(e.target);
            if (!clickedInside && userDropdown.style.display !== 'none') {
                userDropdown.style.display = 'none';
            }
        }
    });
}

