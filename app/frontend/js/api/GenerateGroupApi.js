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
    return await fetchAPIWithBody(
        '/api/v1/group',
        'POST',
        JSON.stringify(formData)
    );
}

/**
 * 그룹 멤버 목록 조회
 * @param {string} groupId - 그룹 ID
 * @returns {Promise<Response>}
 */
export async function getGroupMembers(groupId) {
    return await fetchAPI(
        `/api/groups/${groupId}/members`,
        'GET'
    );
}

/**
 * 그룹 제출 현황 조회
 * @param {string} groupId - 그룹 ID
 * @returns {Promise<Response>}
 */
export async function getGroupStatus(groupId) {
    return await fetchAPI(
        `/api/groups/${groupId}/status`,
        'GET'
    );
}

/**
 * 전체 회식픽 제출
 * @param {string} groupId - 그룹 ID
 * @returns {Promise<Response>}
 */
export async function submitAllPicks(groupId) {
    return await fetchAPIWithBody(
        `/api/groups/${groupId}/submit-all`,
        'POST',
        JSON.stringify({})
    );
}

/**
 * 그룹 결과 조회 (날짜 및 장소 랭킹)
 * @param {string} groupId - 그룹 ID
 * @returns {Promise<Response>}
 */
export async function getGroupResults(groupId) {
    return await fetchAPI(
        `/api/v1/group/${groupId}/results`,
        'GET'
    );
}

/**
 * 그룹 설정값 조회 (날짜 설정 여부, 날짜 범위 등)
 * @param {string} groupId - 그룹 ID
 * @returns {Promise<Response>}
 */
export async function getGroupSettings(groupId) {
    return await fetchAPI(
        `/api/v1/group/${groupId}`,
        'GET'
    );
}

