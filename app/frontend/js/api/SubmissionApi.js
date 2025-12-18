import { fetchAPIWithBody, fetchAPI } from './api_fetcher.js';
import { API_BASE } from '../../config.js';

/**
 * POST 개인 설문 제출
 * @returns {Promise<Response>}
 */
export function submitSingle(groupId, formData) {
    return fetchAPIWithBody(`${API_BASE}/submission/${groupId}/user`, 'POST', JSON.stringify(formData));
}
