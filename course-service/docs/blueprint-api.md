# B.2. Blueprint API

Tài liệu này liệt kê toàn bộ endpoint dự kiến của hệ thống CRS, bao gồm các API công khai, API qua Gateway và API nội bộ giữa các service.

---

## 1. auth-service

- Cổng thực tế: `8094`
- Tiền tố khi qua Gateway: `/api/auth`

| Method | Endpoint | Mô tả | Yêu cầu |
| :--- | :--- | :--- | :--- |
| POST | /auth/login | Đăng nhập, trả về JWT | Public |
| POST | /auth/register | Đăng ký tài khoản | Public |

---

## 2. course-service

- Cổng thực tế: `8092`
- Tiền tố khi qua Gateway: `/api/courses`

| Method | Endpoint | Mô tả | Yêu cầu |
| :--- | :--- | :--- | :--- |
| GET | /courses | Danh sách môn học, hỗ trợ search và phân trang | Public |
| GET | /courses/{id} | Chi tiết một môn học | Public |
| POST | /courses | Thêm môn học | ADMIN |
| PUT | /courses/{id} | Sửa môn học | ADMIN |
| DELETE | /courses/{id} | Xoá môn học | ADMIN |

---

## 3. API nội bộ của course-service

Các API này chỉ được gọi trực tiếp từ `registration-service`, không expose qua API Gateway.

| Method | Endpoint | Mô tả |
| :--- | :--- | :--- |
| PATCH | /internal/courses/{id}/reserve-seat | Kiểm tra còn chỗ và trừ `soChoConLai` |
| PATCH | /internal/courses/{id}/release-seat | Hoàn trả một chỗ khi huỷ đăng ký |

---

## 4. registration-service

- Cổng thực tế: `8093`
- Tiền tố khi qua Gateway: `/api/registrations`

| Method | Endpoint | Mô tả | Yêu cầu |
| :--- | :--- | :--- | :--- |
| POST | /registrations | Đăng ký học phần | STUDENT |
| GET | /registrations/my | Danh sách đăng ký của sinh viên hiện tại | STUDENT |
| DELETE | /registrations/{id} | Huỷ đăng ký | STUDENT / ADMIN |

---

## 5. API qua Gateway

API Gateway chạy tại:

`http://localhost:8085`

| Service | Endpoint nội bộ | Endpoint qua Gateway |
| :--- | :--- | :--- |
| auth-service | /auth/login | /api/auth/login |
| auth-service | /auth/register | /api/auth/register |
| course-service | /courses | /api/courses |
| course-service | /courses/{id} | /api/courses/{id} |
| registration-service | /registrations | /api/registrations |
| registration-service | /registrations/my | /api/registrations/my |
| registration-service | /registrations/{id} | /api/registrations/{id} |
| course-service | /internal/courses/{id}/reserve-seat | KHÔNG expose |
| course-service | /internal/courses/{id}/release-seat | KHÔNG expose |

---

## 6. Quy tắc Authentication & Authorization

| API | Quyền / Cơ chế |
| :--- | :--- |
| POST /api/auth/login | Public |
| POST /api/auth/register | Public |
| GET /api/courses | Public |
| GET /api/courses/{id} | Public |
| POST /api/courses | JWT + ADMIN |
| PUT /api/courses/{id} | JWT + ADMIN |
| DELETE /api/courses/{id} | JWT + ADMIN |
| POST /api/registrations | JWT + STUDENT |
| GET /api/registrations/my | JWT + STUDENT |
| DELETE /api/registrations/{id} | JWT + STUDENT / ADMIN |
| /api/public/courses | X-API-KEY |
| /internal/courses/** | Chỉ registration-service gọi trực tiếp |

---

## 7. Giao tiếp liên-service

| Service gọi | Service được gọi | Method | Endpoint | Mục đích |
| :--- | :--- | :--- | :--- | :--- |
| registration-service | course-service | PATCH | /internal/courses/{id}/reserve-seat | Giữ chỗ trước khi lưu Registration |
| registration-service | course-service | PATCH | /internal/courses/{id}/release-seat | Hoàn trả chỗ khi huỷ đăng ký |

---

## 8. Luồng chính của API

| Luồng | Các bước |
| :--- | :--- |
| Đăng nhập | Frontend → Gateway → auth-service → JWT |
| Xem Course | Frontend → Gateway → course-service |
| Thêm/Sửa/Xoá Course | Frontend → Gateway → course-service |
| Đăng ký học phần | Frontend → Gateway → registration-service → course-service reserve-seat → lưu Registration |
| Huỷ đăng ký | Frontend → Gateway → registration-service → course-service release-seat → cập nhật Registration |
| Partner xem Course | Partner → Gateway → API Key → course-service |

---

## 9. Ghi chú

- Frontend chỉ gọi API Gateway.
- Frontend không gọi trực tiếp auth-service, course-service hoặc registration-service.
- registration-service không truy cập trực tiếp database của course-service.
- API `/internal/**` không được expose qua Gateway.