# DTS Gateway

Điểm vào HTTP của web DTS, chạy Spring Cloud Gateway trên port8888. Gateway chọn service theo path, rewrite prefix và chuyển tiếp request/response. Repo gồm Gateway và ứng dụng Eureka Server độc lập trong eureka-server/.

| Nội dung | Tài liệu |
| --- | --- |
| Chạy local với đủ 7 service URL | [Local setup](docs/LOCAL_SETUP.md) |
| Chức năng, filter và ranh giới nghiệp vụ | [Gateway features](docs/GATEWAY_FEATURES.md) |
| Bảng route và contract proxy | [API blueprints](docs/api-blueprint/README.md) |
| Mẫu environment host-local | [.env.example](.env.example) |

Phần chức năng mới so với main của đợt bàn giao là ResultInternalRouteGuard: public proxy Result chỉ cho qua nhánh /api/result/v1/... và chặn các dạng path không hợp lệ theo điều kiện code. Guard không tính điểm, thay đổi lịch sử hoặc cấp quyền đọc kết quả của người khác; những nghiệp vụ đó do service sở hữu dữ liệu kiểm tra.

AuthenticationFilter trong source hiện có // @Component nên chưa được đăng ký làm GlobalFilter. Backend phía sau vẫn chịu trách nhiệm xác thực Bearer và phân quyền của mình. Quá trình chuẩn bị Git giữ nguyên trạng thái filter, Java, YAML, Maven, Docker và mọi URL đang cấu hình; không bật JWT filter hay sửa route để khớp tài liệu.

Gateway không kết nối database, không có migration ứng dụng trong snapshot này. Source mới không cần SQL. Khi thay đổi schema ở repo khác vẫn phải có migration tại repo sở hữu dữ liệu. Pull Gateway không tự khởi động những backend còn lại; làm theo hướng dẫn từng repo để có đủ chức năng web.
