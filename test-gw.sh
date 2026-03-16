sed -i '' '/ORDER_SERVICE_URL/a\
      - MANAGEMENT_ENDPOINTS_WEB_EXPOSURE_INCLUDE=*' compose.yml
docker compose up -d apigetaway
