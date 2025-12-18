import { fetchAPIWithBody, fetchAPI } from "./api_fetcher.js";
import { API_BASE } from '../../config.js';

/**
 * POST 회원가입
 
 */
export async function signUp(id, nickname, password) {
    return await fetchAPIWithBody(`${API_BASE}/users`,'POST', JSON.stringify({ id, nickname, password }) );
}

/**
 * POST 로그인

 */
export async function signIn(username, password) {
    return await fetchAPIWithBody(`${API_BASE}/auth/login`,'POST', JSON.stringify({ username, password }) );
}


/**
 * 로그아웃

 */
export async function signOut() {
    return await fetchAPI(`${API_BASE}/auth/logout`,'POST');
}