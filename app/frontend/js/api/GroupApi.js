/**
 * 그룹 관련 API 호출 함수들
 */
import { fetchAPIWithBody, fetchAPI } from './api_fetcher.js';
import { API_BASE } from '../common/constants.js';
/**
 * 그룹 생성
 * @param {Object} formData - 그룹 생성 데이터
 * @returns {Promise<Response>}
 */
export async function createGroup(formData) {
    return await fetchAPIWithBody(
        `${API_BASE}/group`,
        'POST',
        JSON.stringify(formData)
    );
}

/**
 * 그룹 설정값 조회 (날짜 설정 여부, 날짜 범위 등)
 * @param {string} groupId - 그룹 ID
 * @returns {Promise<Response>}
 */
export async function getGroupSettings(groupId) {
    return await fetchAPI(
        `${API_BASE}/group/${groupId}`,
        'GET'
    );
}

/**
 * 그룹 집계 조회 
 */
export async function getGroupSubmitStatus(groupId, ownerId) {
    return await fetchAPI(
        `${API_BASE}/group/${groupId}/${ownerId}/aggregation`,
        'GET'
    );
}
