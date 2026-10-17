# API Blueprint: Gateway route và rewrite

## Origin và cách đọc

Gateway local http://localhost:8888. application.yaml có8 entry cho7 service vì Practice có thêm route ảnh. Các origin mặc định dưới đây là cấu hình checked-in, không phải environment hiệu lực của process Java đang chạy. Khi chạy host-local, truyền đủ URL localhost theo LOCAL_SETUP.

| Route ID | Biến URI | Mặc định checked-in | URI host-local |
| --- | --- | --- | --- |
| identity-service | IDENTITY_SERVICE_URL | http://dts-identity-service:8081 | http://localhost:8081 |
| practice-service | PRACTICE_SERVICE_URL | http://dts-practice-service:8087 | http://localhost:8087 |
| practice-question-images | PRACTICE_SERVICE_URL | Như practice-service | Như practice-service |
| progress-service | PROGRESS_SERVICE_URL | http://dts-progress:8083 | http://localhost:8083 |
| content-builder-service | CONTENT_BUILDER_SERVICE_URL | http://content-builder:8082 | http://localhost:8082 |
| examination-service | EXAMINATION_SERVICE_URL | http://examination:8088 | http://localhost:8088 |
| result-service | RESULT_SERVICE_URL | http://hoangcn.com:8086 | http://localhost:8086 |
| media-service | MEDIA_SERVICE_URL | http://dts-media:8080 | http://localhost:8080 |

## Path predicates và rewrite

| Route | Path match | Rewrite |
| --- | --- | --- |
| Identity | /api/identity/**, /api/v1/auth/**, /api/v1/users/** | /api/identity/v1/{path} → /api/v1/{path} |
| Practice | /api/practice/**, /api/v1/practices/**, /api/v1/exams/**, /api/v1/questions/**, /api/v1/exam-sessions/** | /api/practice/v1/{path} → /api/v1/{path} |
| Ảnh câu hỏi | /question-images/** | Giữ path |
| Progress | /api/progress/**, /api/v1/progress/** | /api/progress/v1/{path} → /api/v1/{path} |
| Content Builder | /api/content-builder/** | /api/content-builder/v1/{path} → /api/v1/content-builder/{path} |
| Examination | /api/examination/** | /api/examination/v1/{path} → /api/v1/{path} |
| Result | /api/result/** | /api/result/{path} → /{path}, thêm guard chỉ nhánh v1 hợp lệ |
| Media | /api/v1/media/** | Giữ path |

{path} là ký hiệu giải thích segment, không phải tham số body. Alias phải có đúng đoạn v1 như rewrite nêu; match predicate rộng không làm mọi biến thể prefix tự thành endpoint hợp lệ. Request method/body nghiệp vụ theo backend; route không khai báo Method predicate.

## Ví dụ liên kết

| Request Gateway | Path upstream |
| --- | --- |
| /api/v1/users/me | /api/v1/users/me ở Identity |
| /api/identity/v1/users/me | /api/v1/users/me ở Identity |
| /api/identity/v1/admin/users | /api/v1/admin/users ở Identity |
| /api/practice/v1/exams/config?examType=A1 | /api/v1/exams/config?examType=A1 ở Practice |
| /api/progress/v1/progress/{path} | /api/v1/progress/{path} ở Progress |
| /api/content-builder/v1/questions | /api/v1/content-builder/questions ở Content Builder |
| /api/examination/v1/{path} | /api/v1/{path} ở Examination |
| /api/result/v1/results/me/history | /v1/results/me/history ở Result |
| /api/result/v1/leaderboards | /v1/leaderboards ở Result |
| /api/v1/media/uploads/initialize | /api/v1/media/uploads/initialize ở Media |

Ví dụ với {path} chỉ minh họa phép rewrite, không khẳng định một endpoint thật có tên {path}. Query nghiệp vụ giữ trong request; Gateway không tính lại page/filter/license/grade.

## Response và lỗi

Downstream trả DTO/Page/ApiResponse theo contract từng repo, Gateway không thêm data wrapper thống nhất. AuthenticationFilter hiện không đăng ký, xem auth_trace_errors.md. Không match route thường404 của tầng Gateway; Result guard có404 rỗng; lỗi kết nối có thể503/500 theo exception. Dùng đúng mapping, không thêm một /api/v1 nữa trước prefix Result.

Nguồn: [application.yaml](../../src/main/resources/application.yaml), [ResultInternalRouteGuard](../../src/main/java/dts/com/gateway/filter/ResultInternalRouteGuard.java). Các liên kết source trong docs/api-blueprint được tính từ thư mục chứa tài liệu.
