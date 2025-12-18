import { fetchAPIWithBody, fetchAPI } from './api_fetcher.js';
import { API_BASE } from '../../config.js';

/**
 * POST 개인 설문 제출
 * @returns {Promise<Response>}
 */
export function submitSingle(groupId, formData) {
    return fetchAPIWithBody(`${API_BASE}/submission/${groupId}/user`, 'POST', JSON.stringify(formData));
}


/**
 * POST 통합 제출
 * @param {Number} groupId - 그룹 ID
 * @returns {Promise<Response>}
 */
export function submitAggregation(groupId) {
    return fetchAPI(`${API_BASE}/submission/total/${groupId}`, 'POST');
}