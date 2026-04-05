# ─── Cloud Map (Service Discovery) ────────────────────────────────────────────
# Allows microservices to find each other by DNS name (e.g. eureka.microservices.local)

resource "aws_service_discovery_private_dns_namespace" "main" {
  name = "${var.project_name}.local"
  vpc  = aws_vpc.main.id

  tags = var.tags
}

# ─── CloudWatch Log Groups ─────────────────────────────────────────────────────
resource "aws_cloudwatch_log_group" "services" {
  for_each          = toset(["kafka", "zookeeper", "serversc", "eureka", "apigetaway", "product-service", "order-service"])
  name              = "/ecs/${var.project_name}/${each.key}"
  retention_in_days = 7

  tags = var.tags
}

# ─── ECS Cluster ──────────────────────────────────────────────────────────────
resource "aws_ecs_cluster" "main" {
  name = "${var.project_name}-cluster"

  configuration {
    execute_command_configuration {
      logging = "DEFAULT"
    }
  }

  tags = merge(var.tags, { Name = "${var.project_name}-cluster" })
}

resource "aws_ecs_cluster_capacity_providers" "main" {
  cluster_name       = aws_ecs_cluster.main.name
  capacity_providers = ["FARGATE", "FARGATE_SPOT"]

  default_capacity_provider_strategy {
    base              = 1
    weight            = 100
    capacity_provider = "FARGATE"
  }
}

# ─── SERVICE DISCOVERY entries ─────────────────────────────────────────────────
locals {
  discovery_services = {
    zookeeper       = { port = 2181 }
    kafka           = { port = 9092 }
    eureka          = { port = 8761 }
    serversc        = { port = 8888 }
    product-service = { port = 8084 }
    order-service   = { port = 8083 }
    apigetaway      = { port = 8089 }
  }
}

resource "aws_service_discovery_service" "services" {
  for_each = local.discovery_services

  name = each.key

  dns_config {
    namespace_id   = aws_service_discovery_private_dns_namespace.main.id
    routing_policy = "MULTIVALUE"

    dns_records {
      ttl  = 10
      type = "A"
    }
  }

  health_check_custom_config {
    failure_threshold = 1
  }
}

# ─── ZOOKEEPER Task Definition ─────────────────────────────────────────────────
resource "aws_ecs_task_definition" "zookeeper" {
  family                   = "${var.project_name}-zookeeper"
  requires_compatibilities = ["FARGATE"]
  network_mode             = "awsvpc"
  cpu                      = 256
  memory                   = 512
  execution_role_arn       = aws_iam_role.ecs_task_execution.arn
  task_role_arn            = aws_iam_role.ecs_task.arn

  container_definitions = jsonencode([
    {
      name         = "zookeeper"
      image        = "confluentinc/cp-zookeeper:7.4.0"
      essential    = true
      portMappings = [{ containerPort = 2181, protocol = "tcp" }]
      environment = [
        { name = "ZOOKEEPER_CLIENT_PORT", value = "2181" },
        { name = "ZOOKEEPER_TICK_TIME", value = "2000" }
      ]
      logConfiguration = {
        logDriver = "awslogs"
        options = {
          "awslogs-group"         = aws_cloudwatch_log_group.services["zookeeper"].name
          "awslogs-region"        = var.aws_region
          "awslogs-stream-prefix" = "ecs"
        }
      }
    }
  ])

  tags = var.tags
}

resource "aws_ecs_service" "zookeeper" {
  name            = "${var.project_name}-zookeeper"
  cluster         = aws_ecs_cluster.main.id
  task_definition = aws_ecs_task_definition.zookeeper.arn
  desired_count   = 1
  launch_type     = "FARGATE"

  network_configuration {
    subnets          = aws_subnet.private[*].id
    security_groups  = [aws_security_group.ecs_tasks.id]
    assign_public_ip = false
  }

  service_registries {
    registry_arn = aws_service_discovery_service.services["zookeeper"].arn
  }

  tags = var.tags
}

# ─── KAFKA Task Definition ─────────────────────────────────────────────────────
resource "aws_ecs_task_definition" "kafka" {
  family                   = "${var.project_name}-kafka"
  requires_compatibilities = ["FARGATE"]
  network_mode             = "awsvpc"
  cpu                      = 512
  memory                   = 1024
  execution_role_arn       = aws_iam_role.ecs_task_execution.arn
  task_role_arn            = aws_iam_role.ecs_task.arn

  container_definitions = jsonencode([
    {
      name      = "kafka"
      image     = "confluentinc/cp-kafka:7.4.0"
      essential = true
      portMappings = [
        { containerPort = 9092, protocol = "tcp" },
        { containerPort = 29092, protocol = "tcp" }
      ]
      environment = [
        { name = "KAFKA_BROKER_ID", value = "1" },
        { name = "KAFKA_ZOOKEEPER_CONNECT", value = "zookeeper.${var.project_name}.local:2181" },
        { name = "KAFKA_ADVERTISED_LISTENERS", value = "PLAINTEXT://kafka.${var.project_name}.local:9092" },
        { name = "KAFKA_LISTENER_SECURITY_PROTOCOL_MAP", value = "PLAINTEXT:PLAINTEXT" },
        { name = "KAFKA_INTER_BROKER_LISTENER_NAME", value = "PLAINTEXT" },
        { name = "KAFKA_OFFSETS_TOPIC_REPLICATION_FACTOR", value = "1" },
        { name = "KAFKA_AUTO_CREATE_TOPICS_ENABLE", value = "true" }
      ]
      logConfiguration = {
        logDriver = "awslogs"
        options = {
          "awslogs-group"         = aws_cloudwatch_log_group.services["kafka"].name
          "awslogs-region"        = var.aws_region
          "awslogs-stream-prefix" = "ecs"
        }
      }
    }
  ])

  tags = var.tags
}

resource "aws_ecs_service" "kafka" {
  name            = "${var.project_name}-kafka"
  cluster         = aws_ecs_cluster.main.id
  task_definition = aws_ecs_task_definition.kafka.arn
  desired_count   = 1
  launch_type     = "FARGATE"

  network_configuration {
    subnets          = aws_subnet.private[*].id
    security_groups  = [aws_security_group.ecs_tasks.id]
    assign_public_ip = false
  }

  service_registries {
    registry_arn = aws_service_discovery_service.services["kafka"].arn
  }

  depends_on = [aws_ecs_service.zookeeper]

  tags = var.tags
}

# ─── EUREKA Task Definition ────────────────────────────────────────────────────
data "aws_ecr_image" "eureka" {
  repository_name = aws_ecr_repository.services["eureka"].name
  most_recent     = true
  depends_on      = [aws_ecr_repository.services]
}

resource "aws_ecs_task_definition" "eureka" {
  family                   = "${var.project_name}-eureka"
  requires_compatibilities = ["FARGATE"]
  network_mode             = "awsvpc"
  cpu                      = var.ecs_task_cpu
  memory                   = var.ecs_task_memory
  execution_role_arn       = aws_iam_role.ecs_task_execution.arn
  task_role_arn            = aws_iam_role.ecs_task.arn

  container_definitions = jsonencode([
    {
      name         = "eureka"
      image        = "${aws_ecr_repository.services["eureka"].repository_url}:latest"
      essential    = true
      portMappings = [{ containerPort = 8761, protocol = "tcp" }]
      environment = [
        { name = "EUREKA_INSTANCE_HOSTNAME", value = "eureka.${var.project_name}.local" },
        { name = "EUREKA_CLIENT_REGISTER_WITH_EUREKA", value = "false" },
        { name = "EUREKA_CLIENT_FETCH_REGISTRY", value = "false" }
      ]
      logConfiguration = {
        logDriver = "awslogs"
        options = {
          "awslogs-group"         = aws_cloudwatch_log_group.services["eureka"].name
          "awslogs-region"        = var.aws_region
          "awslogs-stream-prefix" = "ecs"
        }
      }
    }
  ])

  tags = var.tags
}

resource "aws_ecs_service" "eureka" {
  name            = "${var.project_name}-eureka"
  cluster         = aws_ecs_cluster.main.id
  task_definition = aws_ecs_task_definition.eureka.arn
  desired_count   = var.services_desired_count
  launch_type     = "FARGATE"

  network_configuration {
    subnets          = aws_subnet.private[*].id
    security_groups  = [aws_security_group.ecs_tasks.id]
    assign_public_ip = false
  }

  service_registries {
    registry_arn = aws_service_discovery_service.services["eureka"].arn
  }

  tags = var.tags
}

# ─── CONFIG SERVER (serversc) Task Definition ──────────────────────────────────
resource "aws_ecs_task_definition" "serversc" {
  family                   = "${var.project_name}-serversc"
  requires_compatibilities = ["FARGATE"]
  network_mode             = "awsvpc"
  cpu                      = var.ecs_task_cpu
  memory                   = var.ecs_task_memory
  execution_role_arn       = aws_iam_role.ecs_task_execution.arn
  task_role_arn            = aws_iam_role.ecs_task.arn

  container_definitions = jsonencode([
    {
      name         = "serversc"
      image        = "${aws_ecr_repository.services["serversc"].repository_url}:latest"
      essential    = true
      portMappings = [{ containerPort = 8888, protocol = "tcp" }]
      environment = [
        { name = "EUREKA_CLIENT_SERVICEURL_DEFAULTZONE", value = "http://eureka.${var.project_name}.local:8761/eureka/" }
      ]
      logConfiguration = {
        logDriver = "awslogs"
        options = {
          "awslogs-group"         = aws_cloudwatch_log_group.services["serversc"].name
          "awslogs-region"        = var.aws_region
          "awslogs-stream-prefix" = "ecs"
        }
      }
    }
  ])

  tags = var.tags
}

resource "aws_ecs_service" "serversc" {
  name            = "${var.project_name}-serversc"
  cluster         = aws_ecs_cluster.main.id
  task_definition = aws_ecs_task_definition.serversc.arn
  desired_count   = var.services_desired_count
  launch_type     = "FARGATE"

  network_configuration {
    subnets          = aws_subnet.private[*].id
    security_groups  = [aws_security_group.ecs_tasks.id]
    assign_public_ip = false
  }

  service_registries {
    registry_arn = aws_service_discovery_service.services["serversc"].arn
  }

  depends_on = [aws_ecs_service.eureka]

  tags = var.tags
}

# ─── PRODUCT-SERVICE Task Definition ──────────────────────────────────────────
resource "aws_ecs_task_definition" "product_service" {
  family                   = "${var.project_name}-product-service"
  requires_compatibilities = ["FARGATE"]
  network_mode             = "awsvpc"
  cpu                      = var.ecs_task_cpu
  memory                   = var.ecs_task_memory
  execution_role_arn       = aws_iam_role.ecs_task_execution.arn
  task_role_arn            = aws_iam_role.ecs_task.arn

  container_definitions = jsonencode([
    {
      name         = "product-service"
      image        = "${aws_ecr_repository.services["product-service"].repository_url}:latest"
      essential    = true
      portMappings = [{ containerPort = 8084, protocol = "tcp" }]
      secrets = [
        { name = "SPRING_DATASOURCE_PASSWORD", valueFrom = aws_ssm_parameter.db_password.arn },
        { name = "SPRING_DATASOURCE_URL", valueFrom = aws_ssm_parameter.db_url_products.arn }
      ]
      environment = [
        { name = "SPRING_DATASOURCE_USERNAME", value = var.db_username },
        { name = "SPRING_KAFKA_BOOTSTRAP_SERVERS", value = "kafka.${var.project_name}.local:9092" },
        { name = "EUREKA_CLIENT_SERVICEURL_DEFAULTZONE", value = "http://eureka.${var.project_name}.local:8761/eureka/" },
        { name = "SPRING_CONFIG_IMPORT", value = "configserver:http://serversc.${var.project_name}.local:8888" }
      ]
      logConfiguration = {
        logDriver = "awslogs"
        options = {
          "awslogs-group"         = aws_cloudwatch_log_group.services["product-service"].name
          "awslogs-region"        = var.aws_region
          "awslogs-stream-prefix" = "ecs"
        }
      }
    }
  ])

  tags = var.tags
}

resource "aws_ecs_service" "product_service" {
  name            = "${var.project_name}-product-service"
  cluster         = aws_ecs_cluster.main.id
  task_definition = aws_ecs_task_definition.product_service.arn
  desired_count   = var.services_desired_count
  launch_type     = "FARGATE"

  network_configuration {
    subnets          = aws_subnet.private[*].id
    security_groups  = [aws_security_group.ecs_tasks.id]
    assign_public_ip = false
  }

  service_registries {
    registry_arn = aws_service_discovery_service.services["product-service"].arn
  }

  depends_on = [aws_ecs_service.serversc, aws_ecs_service.kafka]

  tags = var.tags
}

# ─── ORDER-SERVICE Task Definition ────────────────────────────────────────────
resource "aws_ecs_task_definition" "order_service" {
  family                   = "${var.project_name}-order-service"
  requires_compatibilities = ["FARGATE"]
  network_mode             = "awsvpc"
  cpu                      = var.ecs_task_cpu
  memory                   = var.ecs_task_memory
  execution_role_arn       = aws_iam_role.ecs_task_execution.arn
  task_role_arn            = aws_iam_role.ecs_task.arn

  container_definitions = jsonencode([
    {
      name         = "order-service"
      image        = "${aws_ecr_repository.services["order-service"].repository_url}:latest"
      essential    = true
      portMappings = [{ containerPort = 8083, protocol = "tcp" }]
      secrets = [
        { name = "SPRING_DATASOURCE_PASSWORD", valueFrom = aws_ssm_parameter.db_password.arn },
        { name = "SPRING_DATASOURCE_URL", valueFrom = aws_ssm_parameter.db_url_orders.arn }
      ]
      environment = [
        { name = "SPRING_DATASOURCE_USERNAME", value = var.db_username },
        { name = "PRODUCT_SERVICE_URL", value = "http://product-service.${var.project_name}.local:8084" },
        { name = "SPRING_KAFKA_BOOTSTRAP_SERVERS", value = "kafka.${var.project_name}.local:9092" },
        { name = "EUREKA_CLIENT_SERVICEURL_DEFAULTZONE", value = "http://eureka.${var.project_name}.local:8761/eureka/" },
        { name = "SPRING_CONFIG_IMPORT", value = "configserver:http://serversc.${var.project_name}.local:8888" }
      ]
      logConfiguration = {
        logDriver = "awslogs"
        options = {
          "awslogs-group"         = aws_cloudwatch_log_group.services["order-service"].name
          "awslogs-region"        = var.aws_region
          "awslogs-stream-prefix" = "ecs"
        }
      }
    }
  ])

  tags = var.tags
}

resource "aws_ecs_service" "order_service" {
  name            = "${var.project_name}-order-service"
  cluster         = aws_ecs_cluster.main.id
  task_definition = aws_ecs_task_definition.order_service.arn
  desired_count   = var.services_desired_count
  launch_type     = "FARGATE"

  network_configuration {
    subnets          = aws_subnet.private[*].id
    security_groups  = [aws_security_group.ecs_tasks.id]
    assign_public_ip = false
  }

  service_registries {
    registry_arn = aws_service_discovery_service.services["order-service"].arn
  }

  depends_on = [aws_ecs_service.product_service]

  tags = var.tags
}

# ─── API GATEWAY Task Definition ──────────────────────────────────────────────
resource "aws_ecs_task_definition" "apigetaway" {
  family                   = "${var.project_name}-apigetaway"
  requires_compatibilities = ["FARGATE"]
  network_mode             = "awsvpc"
  cpu                      = var.ecs_task_cpu
  memory                   = var.ecs_task_memory
  execution_role_arn       = aws_iam_role.ecs_task_execution.arn
  task_role_arn            = aws_iam_role.ecs_task.arn

  container_definitions = jsonencode([
    {
      name         = "apigetaway"
      image        = "${aws_ecr_repository.services["apigetaway"].repository_url}:latest"
      essential    = true
      portMappings = [{ containerPort = 8089, protocol = "tcp" }]
      environment = [
        { name = "EUREKA_CLIENT_SERVICEURL_DEFAULTZONE", value = "http://eureka.${var.project_name}.local:8761/eureka/" }
      ]
      logConfiguration = {
        logDriver = "awslogs"
        options = {
          "awslogs-group"         = aws_cloudwatch_log_group.services["apigetaway"].name
          "awslogs-region"        = var.aws_region
          "awslogs-stream-prefix" = "ecs"
        }
      }
    }
  ])

  tags = var.tags
}

resource "aws_ecs_service" "apigetaway" {
  name            = "${var.project_name}-apigetaway"
  cluster         = aws_ecs_cluster.main.id
  task_definition = aws_ecs_task_definition.apigetaway.arn
  desired_count   = var.services_desired_count
  launch_type     = "FARGATE"

  network_configuration {
    subnets          = aws_subnet.private[*].id
    security_groups  = [aws_security_group.ecs_tasks.id]
    assign_public_ip = false
  }

  service_registries {
    registry_arn = aws_service_discovery_service.services["apigetaway"].arn
  }

  load_balancer {
    target_group_arn = aws_lb_target_group.apigetaway.arn
    container_name   = "apigetaway"
    container_port   = 8089
  }

  depends_on = [
    aws_ecs_service.eureka,
    aws_ecs_service.serversc,
    aws_lb_listener.http
  ]

  tags = var.tags
}
