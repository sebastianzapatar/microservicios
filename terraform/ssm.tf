# ─── SSM Parameter Store (Secrets & Config) ───────────────────────────────────

resource "aws_ssm_parameter" "db_password" {
  name  = "/${var.project_name}/db-password"
  type  = "SecureString"
  value = var.db_password

  tags = var.tags
}

resource "aws_ssm_parameter" "db_url_products" {
  name  = "/${var.project_name}/db-url-products"
  type  = "String"
  value = "jdbc:postgresql://${aws_db_instance.postgres.address}:5432/products"

  tags = var.tags
}

resource "aws_ssm_parameter" "db_url_orders" {
  name  = "/${var.project_name}/db-url-orders"
  type  = "String"
  value = "jdbc:postgresql://${aws_db_instance.postgres.address}:5432/orders"

  tags = var.tags
}

resource "aws_ssm_parameter" "db_username" {
  name  = "/${var.project_name}/db-username"
  type  = "String"
  value = var.db_username

  tags = var.tags
}

resource "aws_ssm_parameter" "eureka_url" {
  name  = "/${var.project_name}/eureka-url"
  type  = "String"
  value = "http://eureka.${var.project_name}.local:8761/eureka/"

  tags = var.tags
}

resource "aws_ssm_parameter" "config_server_url" {
  name  = "/${var.project_name}/config-server-url"
  type  = "String"
  value = "http://serversc.${var.project_name}.local:8888"

  tags = var.tags
}

resource "aws_ssm_parameter" "kafka_bootstrap_servers" {
  name  = "/${var.project_name}/kafka-bootstrap-servers"
  type  = "String"
  value = "kafka.${var.project_name}.local:9092"

  tags = var.tags
}
