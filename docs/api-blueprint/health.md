# API Blueprint: Gateway health

## Endpoint

GET /health trên origin Gateway, ví dụ http://localhost:8888/health. Không có query hoặc body nghiệp vụ. Đây là HealthController của Gateway, không phải một proxy endpoint của Identity hoặc Actuator.

## Response — 200

```json
{
  "status": "UP",
  "service": "DTS API Gateway",
  "message": "Gateway is running successfully!"
}
```

Response Map trực tiếp, thứ tự key không phải contract; không có wrapper success/data/timestamp. Không có body yêu cầu Bearer trong controller hiện tại.

## Ý nghĩa

200 chỉ chứng minh handler Gateway đang trả lời. Handler không gọi Identity, Practice, Progress, Content Builder, Examination, Result, Media, Eureka, Redis, Kafka hoặc database. Không dùng /health=UP để kết luận mọi trang web sẽ lấy được dữ liệu.

Nguồn: [HealthController](../../src/main/java/dts/com/gateway/controller/HealthController.java).
