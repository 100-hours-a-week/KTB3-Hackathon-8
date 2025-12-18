async function fetchCsrfToken() {
    if (csrfTokenCache) return csrfTokenCache;

    const res = await fetch(`${API_BASE_URL}/csrf`, { credentials: 'include' });
    const data = await res.json();

    csrfTokenCache = data.token;

    return csrfTokenCache;
}

export async function fetchAPIWithBody(url, method, body) {
    const token = await fetchCsrfToken();
    const headers = new Headers(options.headers || {});

    headers.set('Content-Type', 'application/json');
    headers.set('X-XSRF-TOKEN', token);

    return await fetch(url, {
                        method: method,
                        headers,
                        body: body,
                        credentials: 'include'
                    });
}

export async function fetchAPIWithFile(url, method, formData) {
    const token = await fetchCsrfToken();
    const headers = new Headers(options.headers || {});

    headers.set('X-XSRF-TOKEN', token);

    return await fetch(url, {
                        method: method,
                        body: formData,
                        headers,
                        credentials: 'include'
                    });
}

export async function fetchAPI(url, method) {
    const token = await fetchCsrfToken();
    const headers = new Headers(options.headers || {});

    headers.set('Content-Type', 'application/json');
    headers.set('X-XSRF-TOKEN', token);

    return await fetch(url, {
                        method: method,
                        headers,
                        credentials: 'include'
                    });
}
