# Tài liệu DTS Gateway

Đọc [LOCAL_SETUP.md](LOCAL_SETUP.md) để chạy Java trên host với đủ URL service. [GATEWAY_FEATURES.md](GATEWAY_FEATURES.md) giải thích chọn route, rewrite prefix, filter và phần Eureka.

[API blueprints](api-blueprint/README.md) gồm bảng route, guard Result, xác thực/trace/lỗi, health và ranh giới tích hợp từng repo. Đây là tài liệu source hiện có, không phải báo cáo đã gọi API hay xác minh environment của tiến trình Gateway đang chạy.

Đợt chuẩn bị Git chỉ bổ sung tài liệu, .env.example và quy tắc bỏ qua file local. Các Java/YAML/pom/Dockerfile, wrapper và test đã có được giữ nguyên. Những lệnh khởi động trong tài liệu dành cho thành viên thiết lập môi trường, không cần chạy lại khi chỉ đang commit source đã test.
