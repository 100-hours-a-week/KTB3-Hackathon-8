export function getGroupIdFromSession() {
    return sessionStorage.getItem('groupId');
}


export function setSessionAsLoggedIn() {
    return sessionStorage.setItem('isLoggedIn', 'true');
}

export function setSessionAsLoggedOut() {
    return sessionStorage.setItem('isLoggedIn', 'false');
}  

export function getIsLoggedInFromSession() {
    return sessionStorage.getItem('isLoggedIn') === 'true';
}