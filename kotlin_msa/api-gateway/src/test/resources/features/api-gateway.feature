# language: ko
기능: API Gateway 라우팅
  외부 요청은 API Gateway를 통해 내부 커머스 서비스로 라우팅되어야 한다.

  시나리오: 커머스 서비스 라우트 등록
    조건 API Gateway 설정이 로드되어 있다
    만일 Gateway 라우트 목록을 조회한다
    그러면 "product-service" 라우트가 등록되어 있다
    그리고 "inventory-service" 라우트가 등록되어 있다
    그리고 "payment-service" 라우트가 등록되어 있다
    그리고 "order-service" 라우트가 등록되어 있다

