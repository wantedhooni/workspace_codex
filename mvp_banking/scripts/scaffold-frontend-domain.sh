#!/usr/bin/env bash
set -euo pipefail

SCRIPT_DIR=$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)
ROOT_DIR=$(cd "$SCRIPT_DIR/.." && pwd)
TEMPLATE_ROOT="$ROOT_DIR/frontend/templates"

TARGET="both"
DOMAIN=""
OVERWRITE=0

usage() {
  cat <<'USAGE'
사용법:
  ./scripts/scaffold-frontend-domain.sh --domain <kebab-case> [--target user|admin|both] [--overwrite]

예시:
  ./scripts/scaffold-frontend-domain.sh --domain transfer-limit
  ./scripts/scaffold-frontend-domain.sh --domain transfer-limit --target user
  ./scripts/scaffold-frontend-domain.sh --domain transfer-limit --target admin --overwrite
USAGE
}

to_pascal_case() {
  local input="$1"
  echo "$input" | awk -F'-' '{
    for (i = 1; i <= NF; i++) {
      printf toupper(substr($i, 1, 1)) substr($i, 2)
    }
  }'
}

to_camel_case() {
  local pascal
  pascal="$(to_pascal_case "$1")"
  echo "$pascal" | awk '{
    printf tolower(substr($0, 1, 1)) substr($0, 2)
  }'
}

to_title_case() {
  local input="$1"
  echo "$input" | awk -F'-' '{
    for (i = 1; i <= NF; i++) {
      if (i > 1) {
        printf " "
      }
      printf toupper(substr($i, 1, 1)) substr($i, 2)
    }
  }'
}

render_template() {
  local template_path="$1"
  local output_path="$2"
  local domain_kebab="$3"
  local domain_camel="$4"
  local domain_pascal="$5"
  local domain_title="$6"

  if [[ -f "$output_path" && "$OVERWRITE" -ne 1 ]]; then
    echo "skip: $output_path (이미 파일이 존재합니다. --overwrite 옵션으로 덮어쓸 수 있습니다.)"
    return 0
  fi

  sed \
    -e "s/__DOMAIN_KEBAB__/${domain_kebab}/g" \
    -e "s/__DOMAIN_CAMEL__/${domain_camel}/g" \
    -e "s/__DOMAIN_PASCAL__/${domain_pascal}/g" \
    -e "s/__DOMAIN_TITLE__/${domain_title}/g" \
    "$template_path" >"$output_path"

  echo "generated: $output_path"
}

while [[ $# -gt 0 ]]; do
  case "$1" in
    --domain)
      DOMAIN="${2:-}"
      shift 2
      ;;
    --target)
      TARGET="${2:-}"
      shift 2
      ;;
    --overwrite)
      OVERWRITE=1
      shift
      ;;
    -h|--help)
      usage
      exit 0
      ;;
    *)
      if [[ -z "$DOMAIN" ]]; then
        DOMAIN="$1"
        shift
      else
        echo "알 수 없는 인자: $1" >&2
        usage
        exit 1
      fi
      ;;
  esac
done

if [[ -z "$DOMAIN" ]]; then
  echo "도메인명을 입력해 주세요." >&2
  usage
  exit 1
fi

if [[ ! "$DOMAIN" =~ ^[a-z][a-z0-9-]*$ ]]; then
  echo "도메인명은 kebab-case 소문자 형식이어야 합니다. 예: transfer-limit" >&2
  exit 1
fi

if [[ "$TARGET" != "user" && "$TARGET" != "admin" && "$TARGET" != "both" ]]; then
  echo "--target 값은 user|admin|both 중 하나여야 합니다." >&2
  exit 1
fi

DOMAIN_PASCAL="$(to_pascal_case "$DOMAIN")"
DOMAIN_CAMEL="$(to_camel_case "$DOMAIN")"
DOMAIN_TITLE="$(to_title_case "$DOMAIN")"

if [[ "$TARGET" == "user" || "$TARGET" == "both" ]]; then
  USER_DOMAIN_DIR="$ROOT_DIR/frontend/user-web-app/src/domains/$DOMAIN"
  mkdir -p "$USER_DOMAIN_DIR"

  render_template "$TEMPLATE_ROOT/user-domain/types.ts.tpl" "$USER_DOMAIN_DIR/types.ts" "$DOMAIN" "$DOMAIN_CAMEL" "$DOMAIN_PASCAL" "$DOMAIN_TITLE"
  render_template "$TEMPLATE_ROOT/user-domain/api.ts.tpl" "$USER_DOMAIN_DIR/api.ts" "$DOMAIN" "$DOMAIN_CAMEL" "$DOMAIN_PASCAL" "$DOMAIN_TITLE"
  render_template "$TEMPLATE_ROOT/user-domain/Page.tsx.tpl" "$USER_DOMAIN_DIR/${DOMAIN_PASCAL}Page.tsx" "$DOMAIN" "$DOMAIN_CAMEL" "$DOMAIN_PASCAL" "$DOMAIN_TITLE"
  render_template "$TEMPLATE_ROOT/user-domain/index.ts.tpl" "$USER_DOMAIN_DIR/index.ts" "$DOMAIN" "$DOMAIN_CAMEL" "$DOMAIN_PASCAL" "$DOMAIN_TITLE"
fi

if [[ "$TARGET" == "admin" || "$TARGET" == "both" ]]; then
  ADMIN_PAGE_DIR="$ROOT_DIR/frontend/admin-portal/src/pages"
  mkdir -p "$ADMIN_PAGE_DIR"

  render_template "$TEMPLATE_ROOT/admin-page/Page.tsx.tpl" "$ADMIN_PAGE_DIR/${DOMAIN_PASCAL}Page.tsx" "$DOMAIN" "$DOMAIN_CAMEL" "$DOMAIN_PASCAL" "$DOMAIN_TITLE"
fi

cat <<EOF

[생성 완료]
- domain: $DOMAIN
- target: $TARGET

[다음 단계]
1. backend API 경로와 응답 스키마를 템플릿 파일 기준으로 실제 도메인에 맞게 수정하세요.
2. user-web-app: src/App.tsx 에 import와 Route를 추가하세요.
3. admin-portal: src/App.tsx 에 lazy import와 Route를 추가하세요.
4. 페이지 타이틀/컬럼/상태 값은 운영 도메인 규칙에 맞게 조정하세요.

[권장 참고 문서]
- $ROOT_DIR/docs/frontend-domain-template.md
EOF
