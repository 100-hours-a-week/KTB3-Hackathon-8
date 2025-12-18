export async function fetchAPIWithBody(url, method, body) {
    return await fetch(url, {
                        method: method,
                        headers: { 'Content-Type': 'application/json' },
                        body: body,
                        credentials: 'include'
                    });
}

export async function fetchAPIWithFile(url, method, formData) {
    return await fetch(url, {
                        method: method,
                        body: formData,
                        credentials: 'include'
                    });
}

export async function fetchAPI(url, method) {
    return await fetch(url, {
                        method: method,
                        headers: { 'Content-Type': 'application/json' },
                        credentials: 'include'
                    });
}