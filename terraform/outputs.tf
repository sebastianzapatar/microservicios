output "alb_dns_name" {
  description = "Public DNS name of the Application Load Balancer"
  value       = aws_lb.main.dns_name
}

output "alb_url" {
  description = "Full URL to access the API Gateway through the ALB"
  value       = "http://${aws_lb.main.dns_name}"
}

output "ecr_urls" {
  description = "ECR repository URLs for each microservice"
  value = {
    for name, repo in aws_ecr_repository.services : name => repo.repository_url
  }
}

output "rds_endpoint" {
  description = "RDS PostgreSQL endpoint"
  value       = aws_db_instance.postgres.address
  sensitive   = true
}

output "ecs_cluster_name" {
  description = "ECS Cluster name"
  value       = aws_ecs_cluster.main.name
}

output "github_actions_access_key_id" {
  description = "AWS Access Key ID for GitHub Actions — add this as secret AWS_ACCESS_KEY_ID"
  value       = aws_iam_access_key.github_actions.id
  sensitive   = true
}

output "github_actions_secret_access_key" {
  description = "AWS Secret Access Key for GitHub Actions — add this as secret AWS_SECRET_ACCESS_KEY"
  value       = aws_iam_access_key.github_actions.secret
  sensitive   = true
}

output "service_discovery_namespace" {
  description = "Private DNS namespace for internal service communication"
  value       = aws_service_discovery_private_dns_namespace.main.name
}
