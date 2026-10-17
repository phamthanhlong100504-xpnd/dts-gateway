# Gateway API blueprints

Theo source local ngày18/10/2026, nhánh chore/backend-service-integration có base bằng main 3a34f373073b111641c4475fc53c32dbd2574cfb. Phần mới gồm ResultInternalRouteGuard và file test đi kèm; Java/route/auth đã có không bị chỉnh trong bước đóng gói.

| Nội dung | Blueprint |
| --- | --- |
| 8 route entries, 7 service origin và rewrite | [route_contract.md](route_contract.md) |
| Phần bổ sung chặn Result internal proxy | [result_proxy_guard.md](result_proxy_guard.md) |
| Xác thực hiện tại, trace ID, CORS và lỗi | [auth_trace_errors.md](auth_trace_errors.md) |
| GET /health | [health.md](health.md) |
| Ranh giới API từng repo và nối frontend | [service_integration.md](service_integration.md) |

Gateway không sở hữu response DTO của backend. Các trang mô tả proxy path và hành vi Gateway; field nghiệp vụ xem tài liệu repo sở hữu endpoint. [Local setup](../LOCAL_SETUP.md) có lệnh đủ service URL, [Gateway features](../GATEWAY_FEATURES.md) mô tả nghiệp vụ.
