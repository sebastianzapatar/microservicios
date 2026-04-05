locals {
  ecr_services = [
    "apigetaway",
    "eureka",
    "serversc",
    "product-service",
    "order-service"
  ]
}

resource "aws_ecr_repository" "services" {
  for_each             = toset(local.ecr_services)
  name                 = "${var.project_name}/${each.key}"
  image_tag_mutability = var.ecr_image_tag_mutability

  image_scanning_configuration {
    scan_on_push = true
  }

  tags = merge(var.tags, { Name = "${var.project_name}-${each.key}" })
}

# Keep only the last N images to save storage costs
resource "aws_ecr_lifecycle_policy" "services" {
  for_each   = aws_ecr_repository.services
  repository = each.value.name

  policy = jsonencode({
    rules = [
      {
        rulePriority = 1
        description  = "Keep last ${var.ecr_image_retention_count} images"
        selection = {
          tagStatus   = "any"
          countType   = "imageCountMoreThan"
          countNumber = var.ecr_image_retention_count
        }
        action = {
          type = "expire"
        }
      }
    ]
  })
}
