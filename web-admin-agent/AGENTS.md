## 작업 지침
- 이 작업공간 안의 현재 프로젝트만 대상으로 작업하고, 다른 프로젝트 파일을 참고하거나 변경하지 않는다.
- 기능과 코드는 가능한 한 실무 운영 환경을 기준으로 설계한다.
- 작업 계획과 기록은 한글로 작성한다.
- 각 작업을 시작하거나 마무리할 때 `TASK.md`에 작업 내용을 한글로 남긴다.
- class, service, service method를 작성할 때는 한글 doc 주석을 붙인다.
- 프로젝트 단위 실행 스크립트는 `script/` 폴더에 둔다.
- `script/all-start.sh`, `script/all-stop.sh`, `script/all-restart.sh`를 유지한다.
- `all-start.sh`, `all-restart.sh`는 마지막에 접속 URL, API 서버 URL, 데모 계정 등 테스트에 필요한 정보를 출력한다.
- 작업 후 `README.md`를 최신 상태로 업데이트한다.

<!-- BEGIN:nextjs-agent-rules -->
# This is NOT the Next.js you know

This version has breaking changes — APIs, conventions, and file structure may all differ from your training data. Read the relevant guide in `node_modules/next/dist/docs/` before writing any code. Heed deprecation notices.
<!-- END:nextjs-agent-rules -->
