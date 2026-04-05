# ─── RDS Subnet Group ──────────────────────────────────────────────────────────
resource "aws_db_subnet_group" "main" {
  name       = "${var.project_name}-db-subnet-group"
  subnet_ids = aws_subnet.private[*].id

  tags = merge(var.tags, { Name = "${var.project_name}-db-subnet-group" })
}

# ─── RDS PostgreSQL Instance ───────────────────────────────────────────────────
resource "aws_db_instance" "postgres" {
  identifier            = "${var.project_name}-postgres"
  engine                = "postgres"
  engine_version        = "15.8"
  instance_class        = var.db_instance_class
  allocated_storage     = var.db_allocated_storage
  max_allocated_storage = 100
  storage_type          = "gp2"
  storage_encrypted     = true

  db_name  = "postgres"
  username = var.db_username
  password = var.db_password

  vpc_security_group_ids = [aws_security_group.rds.id]
  db_subnet_group_name   = aws_db_subnet_group.main.name

  backup_retention_period    = 7
  backup_window              = "03:00-04:00"
  maintenance_window         = "Mon:04:00-Mon:05:00"
  auto_minor_version_upgrade = true

  skip_final_snapshot = true  # Set false for production
  deletion_protection = false # Set true for production
  publicly_accessible = false

  tags = merge(var.tags, { Name = "${var.project_name}-postgres" })
}
