# DTS Gateway — chức năng và nghiệp vụ

## 1. Vị trí của Gateway

Web/mobile gọi một origin Gateway. Gateway chọn route dựa vào path, thay prefix khi cần và forward request đến repo sở hữu endpoint. Gateway không lưu user, câu hỏi, tiến độ, media metadata, bài thi hoặc kết quả; không tính grade/EXP, tạo migration hay quyết định lịch công bố kết quả.

| Thành phần trong repo | Trách nhiệm |
| --- | --- |
| application.yaml | Port, uri service, path predicate, rewrite, CORS, discovery |
| GatewayApplication | Khởi động ứng dụng reactive Spring Cloud Gateway |
| TraceIdFilter | GlobalFilter thêm/chuyển X-Request-ID cho request được route |
| ResultInternalRouteGuard | GlobalFilter chặn nhánh Result proxy không được phép |
| AuthenticationFilter | Source JWT filter hiện chưa đăng ký @Component |
| JwtUtil | Bean tiện ích JWT; không tự biến AuthenticationFilter thành filter hoạt động |
| GlobalErrorWebExceptionHandler | Định dạng lỗi phát sinh ở tầng Gateway |
| HealthController | GET /health, trạng thái tiến trình Gateway |
| eureka-server/ | Ứng dụng service registry độc lập |

## 2. Các nhóm chức năng web đi qua Gateway

- Identity: đăng nhập, đăng ký, OTP, refresh, profile và admin theo route Identity; nghiệp vụ/role nằm ở Identity.
- Practice: catalog câu hỏi, học/luyện thi, cấu hình và phiên bài, các endpoint khác qua alias Practice; nội dung và chấm bài thuộc Practice.
- Progress: chuyển tiếp các API tiến độ, không tính lại dữ liệu progress trong Gateway.
- Content Builder: quản trị nội dung/chapter/câu hỏi/reference content theo route Content Builder; Gateway không publish ngân hàng câu hỏi.
- Examination: chuyển tiếp API thi chính thức qua alias riêng; không đổi thời điểm công bố, ownership hoặc grade.
- Result: chuyển tiếp /v1 của lịch sử, leaderboard và analytics; có guard chặn đường nội bộ qua public proxy.
- Media: chuyển tiếp /api/v1/media/**, không proxy thay presigned PUT binary trực tiếp tới storage.

Xem [route contract](api-blueprint/route_contract.md) để biết prefix chính xác; không gọi /api/v1/admin hoặc mọi endpoint Practice trực tiếp chỉ vì upstream có đường đó. Predicate phải match trước khi rewrite được dùng.

## 3. Public Result proxy

Result upstream không có prefix /api/v1 cho các API này. /api/result/v1/results/me/history được rewrite thành /v1/results/me/history. Guard mới chỉ cho qua path bắt đầu /api/result/v1/ và từ chối các dạng chứa .., ;, backslash hoặc // trong nhánh /api/result.

Bị chặn trả404 và complete response ngay, không gọi chain/backend; body có thể rỗng. Đây là giới hạn đường public Result, không phải kiểm tra Bearer/ownership hoặc allow-list mọi endpoint /v1. Internal source-to-source đi thẳng giữa các backend theo contract riêng.

## 4. Xác thực và quyền theo source hiện tại

AuthenticationFilter có code kiểm tra Bearer, danh sách public endpoint và chèn X-User-* nếu chạy, nhưng annotation @Component đang được comment. Không thấy đăng ký filter này bằng bean khác trong source snapshot. Vì vậy không mô tả mọi route được Gateway xác thực JWT hay được Gateway bảo đảm chèn X-User-Id.

Ứng dụng vẫn phải gửi Authorization Bearer đúng contract downstream; mỗi backend áp dụng xác thực/phân quyền và ownership của mình. Header userId do client tự gửi không thay thế JWT. ResultInternalRouteGuard đang là @Component và độc lập với AuthenticationFilter; không bật/tắt auth filter để nối dữ liệu hoặc chuẩn bị Git.

## 5. Trace, lỗi và CORS

TraceIdFilter chạy ở HIGHEST_PRECEDENCE cho request qua route: dùng X-Request-ID không trắng đã gửi, nếu thiếu sinh UUID, forward cùng header và thêm header response. Guard Result ở HIGHEST_PRECEDENCE+2. Không coi request ID như token hay định danh tài khoản. GET /health là controller của Gateway, không đồng nghĩa đi qua toàn bộ GlobalFilter như request proxy.

Lỗi đã thành HTTP response từ downstream thường đi qua theo contract downstream. ErrorWebExceptionHandler xử lý exception tại Gateway, mặc định500, ResponseStatusException theo status, ConnectException/chuỗi Connection refused thành503 theo nhánh hiện có. Không hứa mọi lỗi hạ tầng đều là503 hoặc mọi404 có JSON wrapper; guard404 hoàn tất không có body.

CORS cho các origin đang ghi trong YAML, allowedMethods/Headers là *, allowCredentials=true. Đợt này không chỉnh danh sách origin; CORS không phải cơ chế xác thực hay đảm bảo backend đang online.

## 6. Source, cấu hình và dữ liệu máy mới

Các URL có giá trị mặc định theo Docker/deployment. Thành viên chạy Java trên host truyền đủ7 URL localhost trong LOCAL_SETUP. Environment hiệu lực của process cũ không nằm trong Git; restart máy rồi chạy thiếu tham số có thể làm những trang vốn chạy được lỗi route dù source giữ nguyên.

Repo không có bảng, datasource, Flyway hay SQL migration ứng dụng trong snapshot này. Các database và nội dung web thuộc repo khác, không bị đóng gói vào Gateway. .agents là gitlink riêng, không sửa nội dung; target/ đã ignore và không cần chuyển/xóa khi chuẩn bị commit.
