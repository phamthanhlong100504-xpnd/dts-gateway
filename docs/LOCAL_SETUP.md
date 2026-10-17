# Chạy DTS Gateway local

## 1. Yêu cầu và cấu trúc

Dùng JDK21. Gateway có Maven Wrapper dạng only-script, phiên bản Maven trong .mvn/wrapper/maven-wrapper.properties; không cần thêm maven-wrapper.jar vào Git. Lần chạy đầu wrapper tải Maven nếu cache chưa có. Có thể dùng Maven hệ thống tương thích nếu đã cài.

- Gateway root: D:\DTS\dts-gateway, main class dts.com.gateway.GatewayApplication, port8888.
- Eureka riêng: D:\DTS\dts-gateway\eureka-server, port8761, có pom/wrapper riêng; không phải một module tự chạy cùng Gateway.
- Nhánh đang bàn giao: chore/backend-service-integration, nhánh local đã có trước khi chuẩn bị Git.

```powershell
Set-Location -LiteralPath 'D:\DTS\dts-gateway'
java -version
.\mvnw.cmd -version
```

## 2. URL của các backend khi chạy Java trên host

| Biến environment / tham số | Giá trị host-local | Repo sở hữu dữ liệu |
| --- | --- | --- |
| IDENTITY_SERVICE_URL | http://localhost:8081 | dts-identity |
| PRACTICE_SERVICE_URL | http://localhost:8087 | dts-practice |
| PROGRESS_SERVICE_URL | http://localhost:8083 | dts-progress |
| CONTENT_BUILDER_SERVICE_URL | http://localhost:8082 | dts-content-builder |
| EXAMINATION_SERVICE_URL | http://localhost:8088 | dts-examination |
| RESULT_SERVICE_URL | http://localhost:8086 | dts-result |
| MEDIA_SERVICE_URL | http://localhost:8080 | dts-media |

Các URL là origin service, không kèm /api/v1 hay prefix frontend. Route trong Gateway tự xử lý prefix. Bật backend cần cho màn hình trước, rồi Gateway; không chạy hai Java process cùng cổng8888. Các database/Redis/Kafka/MinIO cần cho backend do từng repo hướng dẫn, Gateway không tự tạo chúng.

## 3. Lệnh chạy đủ URL service

Mở PowerShell riêng cho Gateway và chạy:

```powershell
Set-Location -LiteralPath 'D:\DTS\dts-gateway'

$gatewayArguments = @(
    '--server.port=8888'
    '--IDENTITY_SERVICE_URL=http://localhost:8081'
    '--PRACTICE_SERVICE_URL=http://localhost:8087'
    '--PROGRESS_SERVICE_URL=http://localhost:8083'
    '--CONTENT_BUILDER_SERVICE_URL=http://localhost:8082'
    '--EXAMINATION_SERVICE_URL=http://localhost:8088'
    '--RESULT_SERVICE_URL=http://localhost:8086'
    '--MEDIA_SERVICE_URL=http://localhost:8080'
    '--EUREKA_ENABLED=false'
) -join ' '

.\mvnw.cmd spring-boot:run "-Dspring-boot.run.arguments=$gatewayArguments"
```

Nếu dùng Maven hệ thống, thay .\mvnw.cmd bằng mvn với các tham số còn lại giống nhau. Command-line properties ở đây được dùng cho lần khởi động đó, không ghi file application.yaml hoặc thay cấu hình process cũ. Giữ terminal mở để service tiếp tục chạy.

Source mặc định nhiều URL dùng hostname Docker, riêng Result mặc định trỏ một origin public. Nếu chạy Java trên Windows mà không truyền đủ URL, có thể gọi nhầm môi trường hoặc không resolve được hostname. Đặc biệt PROGRESS_SERVICE_URL và EXAMINATION_SERVICE_URL phải đúng để các màn hình đã duyệt đọc dữ liệu qua Gateway; việc source không đổi không chứng minh process mới được khởi động với cùng environment.

## 4. Cách dùng environment/IDE thay cho tham số dài

.env.example là mẫu, không chứa credential thật và không tự được Maven/Spring Boot nạp. Có thể nhập các KEY=value đó vào cấu hình environment của IDE. Khi start bằng environment, giữ đủ7 URL và EUREKA_ENABLED=false như bảng trên; không đồng thời thêm command-line URL khác rồi kỳ vọng environment ghi đè command-line.

Nếu muốn lưu cấu hình máy mới, tạo .env từ mẫu và tự nạp environment cho tiến trình theo công cụ của nhóm. .env/.env.* được ignore, .env.example được versioned. Không chép snapshot đã che giá trị về application.yaml và không thay JWT secret/filter chỉ để chạy lệnh local.

## 5. Eureka có cần chạy không?

Với EUREKA_ENABLED=false và các route uri tĩnh nêu trên, Gateway không cần Eureka để chọn những route này. Gateway tắt discovery locator và register/fetch registry theo biến đó. Không bắt buộc start Eureka chỉ vì repo có thư mục eureka-server.

Nếu môi trường chủ động dùng discovery, chạy Eureka trong terminal khác:

```powershell
Set-Location -LiteralPath 'D:\DTS\dts-gateway\eureka-server'
.\mvnw.cmd spring-boot:run
```

Eureka mặc định http://localhost:8761; Gateway dùng EUREKA_URI=http://localhost:8761/eureka/ và EUREKA_ENABLED=true khi muốn bật cơ chế đó. Các route uri tĩnh hiện có không tự chuyển thành lb:// chỉ vì bật discovery. Tên đăng ký và endpoint discovery của những backend khác thuộc cấu hình từng repo.

## 6. Docker và local khác nhau

docker-compose.yml chạy Gateway/Eureka trong external network dts-network. docker-compose.prod.yml dùng image và các URL riêng của deployment, một số port/hostname khác host-local, ví dụ Result trong Docker có thể dùng port8080 của container. Không thay những giá trị deployment bằng localhost của Windows trong đợt dọn Git.

Không chạy compose để tạo trùng Gateway đã có trên8888. Khi Java chạy trong container, localhost là container đó; các origin phải truy cập được từ đúng container/network. Khi Java chạy trên Windows, dùng origin host-local ở bảng trên.

## 7. Điểm truy cập và đọc lỗi

- Gateway: http://localhost:8888
- GET /health trả status UP của Gateway, không kiểm tra sức khỏe tất cả backend.
- Web thường ở http://localhost:3000 và dùng URL proxy theo frontend; backend port không phải frontend origin.
- Không có database/migration riêng cần chạy cho Gateway trong source hiện có.

| Hiện tượng | Căn cứ cần đối chiếu |
| --- | --- |
| Backend trực tiếp200 nhưng qua Gateway500 | URL hiệu lực của Gateway, DNS Docker và log Gateway Error/Caused by |
| Connection refused /503 | Backend/cổng không nghe hoặc origin không đúng |
| Proxy Result nội bộ404 | ResultInternalRouteGuard chặn đúng phạm vi public proxy |
| Auth401/403 | Contract và token/quyền của backend; AuthenticationFilter Gateway đang chưa đăng ký |
| Trình duyệt CORS trong khi gọi trực tiếp được | Origin trình duyệt so với allowedOrigins trong YAML; không tự sửa CORS khi đóng gói Git |

Bước chuẩn bị Git không chạy các lệnh vận hành trong tài liệu này, không gọi API, restart hoặc đổi environment tiến trình Gateway đang dùng.
