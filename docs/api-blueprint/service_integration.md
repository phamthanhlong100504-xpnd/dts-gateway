# Gateway — ranh giới tích hợp các repo DTS

## Frontend gọi proxy đúng contract

Client giữ Authorization, request method, JSON body và query đúng API upstream. Gateway rewrite path theo route_contract.md, không tự thay DTO camelCase/snake_case hoặc thêm field thiếu. Response shape vẫn do từng repo quyết định: không dùng một parser ApiResponse cố định để xử lý cả DTO Media và wrapper Identity.

| Repo | Nhóm nghiệp vụ ở backend | Prefix Gateway |
| --- | --- | --- |
| Identity | Tài khoản, OTP/password, profile/GPLX, role/permission | /api/identity/v1/...; trực tiếp /api/v1/auth/... và /api/v1/users/... |
| Practice | Catalog, học/luyện, đề/phiên, reference content | /api/practice/v1/... và một số direct path trong predicate |
| Progress | Tiến độ người học | /api/progress/v1/... hoặc /api/v1/progress/... |
| Content Builder | Quản trị/publish ngân hàng và nội dung | /api/content-builder/v1/... |
| Examination | Kỳ thi chính thức, phiên, công bố và kết quả nguồn | /api/examination/v1/... |
| Result | Lịch sử, leaderboard, analytics | /api/result/v1/... |
| Media | Metadata và initialize/confirm upload | /api/v1/media/... |

Bảng này không tạo thêm endpoint. Xem tài liệu repo sở hữu dữ liệu cho danh sách path/method và quyền; ví dụ /api/v1/admin/** không nằm trong direct Identity predicate hiện tại, API admin Identity đi qua /api/identity/v1/admin/....

## Học, thi và kết quả

Gateway chuyển request tạo/tiếp tục/kết thúc phiên đến Practice hoặc Examination, không làm lại thao tác submit và không quản lý transaction. Result nhận dữ liệu từ hệ thống backend theo contract riêng; Gateway không phát Kafka event, không backfill hay chuẩn hóa grade.

Client xem detail theo source routing của Result và contract phiên nguồn, không tự lấy một ID bất kỳ rồi thử vào service khác. MANUAL/AFTER_EXAM_END và bảo mật kết quả chưa công bố do Examination/Result quản lý, không được suy ra chỉ từ việc route Gateway tồn tại.

## Upload

Khởi tạo/confirm/lấy metadata Media đi qua Gateway theo /api/v1/media/**. PUT binary vào presigned URL là request trực tiếp tới MinIO/storage theo URL Media cấp; không thêm prefix /api/v1/media vào URL đã ký hoặc sửa hostname sau khi ký.

## Chạy đủ tính năng trên máy mới

Dùng source và migration đúng version của từng backend, credential môi trường được nhóm bàn giao và đủ7 service URL. Git Gateway không chứa dữ liệu account, ảnh trong bucket, câu hỏi, lịch sử hay schema của repo khác. Gateway không có database migration ứng dụng ở phiên bản này.

Cấu hình checked-in và command line/environment của Java process có thể khác nhau. Đặc biệt sau restart máy, chạy lại Gateway với thiếu PROGRESS_SERVICE_URL hoặc EXAMINATION_SERVICE_URL có thể làm proxy lỗi trong khi gọi trực tiếp backend vẫn200. Đọc LOCAL_SETUP để chạy đủ URL; không sửa logic màn hình hoặc bảng nghiệp vụ để bù lỗi network/route.
