import { fetchAPIWithBody } from "./api_fetcher";
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
    return await fetchAPIWithBody(`/auth/login`,'POST', JSON.stringify({ username, password }) );
}
