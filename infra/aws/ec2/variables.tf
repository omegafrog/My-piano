variable "aws_region" {
  description = "AWS region for the single-instance deployment."
  type        = string
  default     = "ap-northeast-2"
}

variable "project_name" {
  description = "Prefix used for AWS resource names and tags."
  type        = string
  default     = "my-piano-free-tier"
}

variable "instance_type" {
  description = "EC2 type. t3.micro is the conservative choice for the legacy 12-month Free Tier."
  type        = string
  default     = "t3.micro"

  validation {
    condition     = contains(["t3.micro", "t4g.micro", "t3.small", "t4g.small"], var.instance_type)
    error_message = "Use a Free Tier-marked burstable type: t3.micro, t4g.micro, t3.small, or t4g.small."
  }
}

variable "root_volume_size_gib" {
  description = "Root gp3 volume size. The legacy EC2 Free Tier includes up to 30 GB-months of eligible EBS usage."
  type        = number
  default     = 30

  validation {
    condition     = var.root_volume_size_gib >= 20 && var.root_volume_size_gib <= 30
    error_message = "root_volume_size_gib must be between 20 and 30 GiB."
  }
}

variable "api_allowed_cidr" {
  description = "IPv4 CIDR allowed to call port 8080. Narrow this to your public IP when possible."
  type        = string
  default     = "0.0.0.0/0"
}

variable "repository_url" {
  description = "Public Git repository cloned by cloud-init."
  type        = string
  default     = "https://github.com/omegafrog/My-piano.git"
}

variable "repository_ref" {
  description = "Branch or tag deployed by cloud-init."
  type        = string
  default     = "changes/CHG-20260716-001"
}

variable "swap_size_gib" {
  description = "Swap file size used to make the full Docker stack viable on a 1 GiB instance."
  type        = number
  default     = 4

  validation {
    condition     = var.swap_size_gib >= 2 && var.swap_size_gib <= 8
    error_message = "swap_size_gib must be between 2 and 8 GiB."
  }
}
