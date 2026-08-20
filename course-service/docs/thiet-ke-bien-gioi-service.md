# Thiết kế biên giới Service

## 1. Danh sách Service

| Service | Cổng | Database | Trách nhiệm chính |
| :--- | :--- | :--- | :--- |
| api-gateway | 8085 | Không có DB | Điểm vào duy nhất, định tuyến, kiểm tra Authorization Header, API Key, CORS |
| auth-service | 8094 | auth_db | Quản lý User, Student, đăng nhập, sinh JWT |
| course-service | 8092 | course_db | Quản lý Course, tìm kiếm, phân trang, quản lý số chỗ |
| registration-service | 8093 | registration_db | Quản lý Registration, gọi course-service khi đăng ký hoặc huỷ |

---

## 2. Nguyên tắc sở hữu dữ liệu

- Mỗi service có database riêng.
- Không service nào được truy cập trực tiếp database của service khác.
- Khi cần dữ liệu của service khác phải gọi REST API.
- `registration-service` chỉ lưu `courseId`, không có bảng Course.
- Không tạo Foreign Key giữa `registration_db` và `course_db`.
- `course-service` sở hữu dữ liệu Course và `soChoConLai`.
- `registration-service` sở hữu dữ liệu Registration.
- `auth-service` sở hữu User, Student và dữ liệu xác thực.
- `api-gateway` không có database nghiệp vụ.

### Data Ownership

| Dữ liệu | Service sở hữu | Cách service khác truy cập |
| :--- | :--- | :--- |
| User / Student | auth-service | REST API / JWT |
| JWT | auth-service | Token |
| Course | course-service | REST API |
| soChoToiDa | course-service | REST API |
| soChoConLai | course-service | REST API |
| Registration | registration-service | REST API |
| courseId trong Registration | registration-service | ID tham chiếu |

---

## 3. Bảng định tuyến Gateway

API Gateway chạy tại:

`http://localhost:8085`

| Route | Forward tới | Ghi chú |
| :--- | :--- | :--- |
| /api/auth/** | http://localhost:8094 | Login/Register public |
| /api/courses/** | http://localhost:8092 | GET public, thay đổi dữ liệu cần JWT |
| /api/registrations/** | http://localhost:8093 | Cần JWT |
| /api/public/courses | http://localhost:8092 | API Key dành cho Partner |

---

## 4. API công khai và API nội bộ

### API công khai

| Service | Endpoint | Được gọi bởi |
| :--- | :--- | :--- |
| auth-service | /auth/login | Frontend |
| auth-service | /auth/register | Frontend |
| course-service | GET /courses | Frontend qua Gateway |
| course-service | GET /courses/{id} | Frontend qua Gateway |
| course-service | POST /courses | Admin qua Gateway |
| course-service | PUT /courses/{id} | Admin qua Gateway |
| course-service | DELETE /courses/{id} | Admin qua Gateway |
| registration-service | POST /registrations | Frontend qua Gateway |
| registration-service | GET /registrations/my | Frontend qua Gateway |
| registration-service | DELETE /registrations/{id} | Frontend qua Gateway |

### API nội bộ

| Service | Endpoint | Được gọi bởi | Qua Gateway |
| :--- | :--- | :--- | :--- |
| course-service | /internal/courses/{id}/reserve-seat | registration-service | KHÔNG |
| course-service | /internal/courses/{id}/release-seat | registration-service | KHÔNG |

---

## 5. Biên giới registration-service và course-service

### Khi sinh viên đăng ký

| Bước | Service | Hoạt động |
| :--- | :--- | :--- |
| 1 | Frontend | Gửi POST /api/registrations |
| 2 | api-gateway | Forward sang registration-service |
| 3 | registration-service | Kiểm tra đăng ký trùng |
| 4 | registration-service | Gọi PATCH /internal/courses/{id}/reserve-seat |
| 5 | course-service | Kiểm tra `soChoConLai` |
| 6 | course-service | Trừ `soChoConLai` |
| 7 | registration-service | Lưu Registration |
| 8 | Frontend | Nhận kết quả |

### Khi huỷ đăng ký

| Bước | Service | Hoạt động |
| :--- | :--- | :--- |
| 1 | Frontend | DELETE /api/registrations/{id} |
| 2 | api-gateway | Forward sang registration-service |
| 3 | registration-service | Tìm Registration |
| 4 | registration-service | Gọi release-seat |
| 5 | course-service | Cộng lại `soChoConLai` |
| 6 | registration-service | Cập nhật trạng thái thành DA_HUY |
| 7 | Frontend | Nhận kết quả |

---

## 6. Biên giới course-service

| Thành phần | Thuộc course-service |
| :--- | :--- |
| Course | Có |
| tenMonHoc | Có |
| soTinChi | Có |
| soChoToiDa | Có |
| soChoConLai | Có |
| Registration | Không |
| User / Student | Không |
| auth_db | Không |
| registration_db | Không |

### Quy tắc số chỗ

| Thao tác | Nơi xử lý |
| :--- | :--- |
| Tạo Course | course-service |
| Sửa Course | course-service |
| Xoá Course | course-service |
| Khởi tạo soChoConLai | course-service |
| Trừ soChoConLai | reserve-seat |
| Hoàn soChoConLai | release-seat |
| registration-service tự sửa soChoConLai | KHÔNG |

---

## 7. Biên giới registration-service

| Thành phần | Thuộc registration-service |
| :--- | :--- |
| Registration | Có |
| studentId | Có |
| courseId | Có |
| trangThai | Có |
| ngayDangKy | Có |
| Course Entity | Không |
| course_db | Không |
| soChoConLai | Không |

### Quy tắc

- Chỉ lưu `courseId`.
- Không tạo bảng Course.
- Không truy cập trực tiếp `course_db`.
- Giữ chỗ phải gọi `reserve-seat`.
- Hoàn chỗ phải gọi `release-seat`.
- Chỉ lưu Registration sau khi reserve-seat thành công.

---

## 8. Biên giới auth-service

| Chức năng | auth-service |
| :--- | :--- |
| Quản lý User | Có |
| Quản lý Student | Có |
| Đăng nhập | Có |
| Kiểm tra username/password | Có |
| Sinh JWT | Có |
| Role ADMIN/STUDENT | Có |
| Course | Không |
| Registration | Không |
| course_db | Không |
| registration_db | Không |

---

## 9. Biên giới api-gateway

| Gateway được làm | Gateway không làm |
| :--- | :--- |
| Routing | Quản lý Course |
| RewritePath | Quản lý Registration |
| Kiểm tra Authorization Header | Truy cập database nghiệp vụ |
| Kiểm tra API Key | Thay đổi soChoConLai |
| CORS | Tạo Registration |
| Forward Request | Chứa business logic |
| Expose public API | Expose /internal/** |

### Security Boundary

| Route | Cơ chế |
| :--- | :--- |
| /api/auth/login | Public |
| /api/auth/register | Public |
| GET /api/courses/** | Public |
| POST/PUT/DELETE /api/courses/** | JWT + ADMIN |
| /api/registrations/** | JWT |
| /api/public/courses | X-API-KEY |
| /internal/courses/** | Không expose |

---

## 10. Biên giới Frontend

Frontend chạy tại:

`http://localhost:5173`

Frontend chỉ được phép gọi:

| Frontend được phép | Frontend không được phép |
| :--- | :--- |
| http://localhost:8085/api/auth/** | http://localhost:8094/** |
| http://localhost:8085/api/courses/** | http://localhost:8092/** |
| http://localhost:8085/api/registrations/** | http://localhost:8093/** |
| http://localhost:8085/api/public/courses | /internal/** |

### Axios Boundary

| Thành phần | Quy tắc |
| :--- | :--- |
| axiosClient.ts | Axios instance dùng chung |
| VITE_API_BASE_URL | `http://localhost:8085` |
| courseApi.ts | Gọi qua axiosClient |
| authApi.ts | Gọi qua axiosClient |
| registrationApi.ts | Gọi qua axiosClient |
| Hardcode localhost:8094 | Không |
| Hardcode localhost:8092 | Không |
| Hardcode localhost:8093 | Không |

---

## 11. Sơ đồ biên giới Service

```text
                         crs-frontend
                            :5173
                              |
                              | HTTP /api/**
                              v
                     +-------------------+
                     |    api-gateway    |
                     |       :8085       |
                     | Routing / JWT     |
                     | API Key / CORS    |
                     +---------+---------+
                               |
              +----------------+----------------+
              |                |                |
              v                v                v
       auth-service     course-service   registration-service
          :8094             :8092              :8093
            |                  |                  |
            v                  v                  v
         auth_db           course_db       registration_db
                               ^
                               |
                               | REST API nội bộ
                               |
                     registration-service