# API Blueprint: Giới hạn public proxy Result

## Phạm vi

ResultInternalRouteGuard là GlobalFilter @Component mới so với main lúc review, order Ordered.HIGHEST_PRECEDENCE+2. Không có endpoint/controller mới. Guard chạy cho exchange qua pipeline route Gateway, xét path request ban đầu trước RewritePath của route Result.

## Logic theo code

Với path bằng /api/result hoặc bắt đầu /api/result/, từ chối nếu một điều kiện sau đúng:

- Không bắt đầu /api/result/v1/.
- Chứa chuỗi .., dấu ;, ký tự backslash hoặc chuỗi //.

Trường hợp từ chối đặt HTTP404 và setComplete, không gọi chain; body có thể rỗng. Các path khác gọi chain.filter(exchange). Đây là những phép kiểm tra chuỗi hiện có, không tuyên bố một cơ chế chuẩn hóa URL hay chống mọi dạng encoding ngoài những gì code kiểm tra.

| Ví dụ path ban đầu | Hành vi guard |
| --- | --- |
| /api/result/internal/results/users/any/overview | 404, không forward |
| /api/result | 404 khi đi vào filter |
| /api/result/v1 | 404 khi đi vào filter vì thiếu / sau v1 |
| /api/result/v1/results/me/history | Đi tiếp tới route Result |
| /api/result/v1/leaderboards | Đi tiếp tới route Result |
| /api/result/v1/results/../internal | Từ chối nếu path đến filter còn chuỗi .. |
| /api/v1/users/me | Không thuộc phạm vi Result, đi tiếp |

Một request được guard cho qua chưa có nghĩa API tồn tại, token hợp lệ hoặc chủ thể sở hữu dữ liệu. Backend Result quyết định các điều kiện đó. Guard không chặn toàn bộ upstream Result nội bộ nếu gọi trực tiếp ngoài Gateway; việc bảo vệ API nội bộ thuộc cấu hình/contract service đó.

## Contract client

- Lịch sử: /api/result/v1/results/me/history và detail theo tài liệu Result.
- Leaderboard: /api/result/v1/leaderboards.
- Analytics: /api/result/v1/results/me/analytics.
- Gửi Bearer và query/body đúng contract Result; không dùng Gateway để gọi /api/result/internal/....
- Có thể gặp404 body rỗng, client không gọi parse JSON vô điều kiện.

## Dữ liệu và triển khai

Không ghi database, không đổi schema hay dữ liệu lịch sử, không thêm migration. Guard và test đã có sẵn trên máy ở snapshot; bước chuẩn bị Git chỉ stage/commit nguyên bản, không chỉnh điều kiện.

Nguồn: [ResultInternalRouteGuard](../../src/main/java/dts/com/gateway/filter/ResultInternalRouteGuard.java), [file test đã có](../../src/test/java/dts/com/gateway/filter/ResultInternalRouteGuardTest.java).
