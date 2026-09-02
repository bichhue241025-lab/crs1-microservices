import axios from 'axios';

const axiosClient = axios.create({
    baseURL: import.meta.env.VITE_API_BASE_URL,
    headers: {
        'Content-Type': 'application/json',
    },
});

// Request Interceptor
axiosClient.interceptors.request.use((config) => {
    const token = localStorage.getItem('crs_token');

    const method = config.method?.toLowerCase();
    const url = config.url ?? '';

    // GET /api/courses... la API public
    // Bao gom:
    // - GET /api/courses
    // - GET /api/courses?keyword=...
    // - GET /api/courses/{id}
    const isPublicCourseRequest =
        method === 'get' &&
        (
            url === '/api/courses' ||
            url.startsWith('/api/courses?') ||
            url.startsWith('/api/courses/')
        );

    // API dang nhap cung khong can JWT
    const isLoginRequest =
        method === 'post' &&
        url === '/api/auth/login';

    // Chi gui JWT cho API can xac thuc
    if (token && !isPublicCourseRequest && !isLoginRequest) {
        config.headers.Authorization = `Bearer ${token}`;
    }

    return config;
});

// Response Interceptor - Buoi 8
axiosClient.interceptors.response.use(
    (response) => response,

    (error) => {
        if (
            axios.isAxiosError(error) &&
            error.response?.status === 401
        ) {
            localStorage.removeItem('crs_token');
            localStorage.removeItem('crs_user');

            if (window.location.pathname !== '/login') {
                window.location.href = '/login';
            }
        }

        return Promise.reject(error);
    }
);

export default axiosClient;