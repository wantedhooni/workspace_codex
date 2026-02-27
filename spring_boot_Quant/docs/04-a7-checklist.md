# A7 체크리스트

## 1) 백엔드 테스트

```bash
cd backend-mvp
gradle test
```

## 2) 운영 엔드포인트

- `GET /actuator/health`
- `GET /actuator/prometheus`

## 3) 스모크 테스트

```bash
cd /Users/revy/workspace_codex/spring_boot_Quant
./scripts/smoke_a7.sh
```

검증 항목:
- health 응답
- prometheus 응답
- 전표 생성 -> 승인 -> 전기
- 원장 검증 결과 `balanced=true`
