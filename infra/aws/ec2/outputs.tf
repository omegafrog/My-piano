output "elastic_ip" {
  description = "Static public IPv4 address. It remains billable until terraform destroy releases it."
  value       = aws_eip.app.public_ip
}

output "api_url" {
  description = "Public backend base URL. Cloud-init can take several minutes to finish the first image build."
  value       = "http://${aws_eip.app.public_ip}:8080"
}

output "healthcheck_url" {
  value = "http://${aws_eip.app.public_ip}:8080/healthcheck"
}

output "instance_id" {
  value = aws_instance.app.id
}

output "ssm_start_session" {
  value = "aws ssm start-session --region ${var.aws_region} --target ${aws_instance.app.id}"
}
