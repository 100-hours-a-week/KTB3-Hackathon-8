// import { API_BASE } from '../config.js';
// import { fetchAPI, fetchAPIWithBody, fetchAPIWithFile } from '../common/api_fetcher.js';
// import { GET_PRESIGNED_URL } from '../config.js';
// import { removeUserIdFromSession } from '../common/session_managers.js';

// export async function getPresignedUrl(filename) {
//     return await fetch(
//             GET_PRESIGNED_URL, {
//                 method: 'POST',
//                 body: JSON.stringify({ filename: filename , contentType: "image/*" }),
//             }
//     );
// }

// export async function uploadToS3(presignedUrl, fileType, file) {
//     return await fetch(presignedUrl, {
//                             method: 'PUT',
//                             headers: {
//                                     'Content-Type': fileType,
//                                     "access-control-allow-origin": "*",
//                             },  
//                             body: file,
//                         });
// }


// // GET 이용약관, 개인정보처리방침
// export function getLegalHTML(type) {
//     return fetchAPI(`${API_BASE}/legal/${type}`, 'GET');
// }

// // POST 회원가입
// export function signUp(email, password, nickname) {
//     return fetchAPIWithBody(`${API_BASE}/users/signUp`, 'POST', JSON.stringify({ email, password, nickname }));
// }