# API Blueprint: Auth, trace ID, CORS và lỗi Gateway

## Xác thực hiện có

AuthenticationFilter có // @Component, chưa được đăng ký thành bean trong source snapshot. Không có @Bean khác đăng ký class này ở những file đã đọc. Danh sách public endpoint và logic JwtUtil trong class không tự có hiệu lực cho mọi request. JwtUtil vẫn là @Component, nhưng điều đó không tự chạy AuthenticationFilter.

Không mô tả Gateway đang bắt buộc Bearer ở mọi route hoặc luôn ghi đè X-User-Id/X-User-Username/X-User-Roles. Backend sở hữu endpoint chịu trách nhiệm xác thực JWT/phân quyền/ownership theo source riêng. Client vẫn gửi Authorization Bearer tới các API bảo vệ, không chọn user qua header tùy ý. Đợt chuẩn bị Git giữ nguyên trạng thái đăng ký filter.

## X-Request-ID

TraceIdFilter là GlobalFilter @Component, order HIGHEST_PRECEDENCE. Với request được route: lấy X-Request-ID nếu không trắng, nếu không có sinh UUID, ghi log method/path, forward header và thêm header response cùng tên X-Request-ID. Tên trong code là X-Request-ID, không phải X-Trace-Id dù một comment lịch sử nhắc tên đó.

Request ID dùng lần theo log, không phải userId hoặc quyền truy cập. Cấu hình CORS hiện chưa khai báo exposedHeaders riêng, vì vậy không khẳng định mọi frontend origin đều đọc được header tùy ý từ JavaScript chỉ vì server đã gửi header.

## CORS checked-in

allowedOrigins có http://localhost:3000, http://hoangcn.com:3000 và http://103.20.96.85:3000; allowedMethods/allowedHeaders là *, allowCredentials=true. Không thêm origin mới trong đợt Git. Hiệu lực CORS còn phụ thuộc request/origin và cấu hình thực tế; gọi HTTP bằng công cụ khác không tương đương kiểm tra CORS trình duyệt.

## Phân biệt nguồn lỗi

| Nguồn | Hành vi hiện có |
| --- | --- |
| Downstream đã trả response | Giữ contract của service đó, không bọc lại mọi response |
| ResultInternalRouteGuard | HTTP404, complete, không tạo JSON body |
| ResponseStatusException ở Gateway | Theo status exception trong GlobalErrorWebExceptionHandler |
| ConnectException hoặc message chứa Connection refused | Handler ánh xạ503, message Service is temporarily unavailable or down |
| Exception khác tại Gateway | Mặc định500 Internal Server Error trong handler |
| Response đã committed | Handler trả Mono.error, không hứa viết lại JSON hoàn chỉnh |

Ví dụ JSON lỗi do handler Gateway tự tạo:

```json
{
  "success": false,
  "message": "Service is temporarily unavailable or down",
  "data": null,
  "timestamp": "2026-10-18T00:00:00Z"
}
```

Handler này không thêm errorCode/traceId vào JSON. Không giả định DNS UnknownHost luôn là503, hoặc mọi404 đều có wrapper. HTTP401/403 từ backend đọc theo contract backend. Khi có lỗi hạ tầng cần đối chiếu route URI hiệu lực và log Gateway Error/Caused by, không sửa tên bài thi/grade trong dữ liệu để xử lý lỗi chuyển tiếp.

Nguồn: [AuthenticationFilter](../../src/main/java/dts/com/gateway/filter/AuthenticationFilter.java), [TraceIdFilter](../../src/main/java/dts/com/gateway/filter/TraceIdFilter.java), [GlobalErrorWebExceptionHandler](../../src/main/java/dts/com/gateway/exception/GlobalErrorWebExceptionHandler.java).
