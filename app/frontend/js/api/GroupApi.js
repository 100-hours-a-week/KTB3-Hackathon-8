/**
 * 그룹 관련 API 호출 함수들
 */
import { fetchAPIWithBody, fetchAPI } from './api_fetcher.js';

/**
 * 그룹 생성
 * @param {Object} formData - 그룹 생성 데이터
 * @returns {Promise<Response>}
 */
export async function createGroup(formData) {
    return await fetchAPIWithBody('/group', 'POST', JSON.stringify(formData));
}
