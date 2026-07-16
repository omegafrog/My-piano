# AWS 단일 EC2 배포

이 Terraform 구성은 서울 리전에 다음 자원만 생성합니다.

- VPC, Internet Gateway, public subnet, route table
- EC2 1대와 30 GiB 이하 gp3 root volume
- EC2에 연결되는 Elastic IP 1개
- 8080 ingress 보안 그룹
- SSH 없이 Session Manager를 쓰기 위한 IAM role/profile

NAT Gateway, Load Balancer, RDS는 생성하지 않습니다.

## 비용 주의

AWS는 연결 여부와 관계없이 모든 Elastic IP/Public IPv4에 시간당 요금을 부과합니다. EC2 Free Tier가 활성 상태라면 월 750시간의 in-use public IPv4가 제공될 수 있지만 계정 생성 시점, 남은 기간, 다른 리전 사용량에 따라 실제 청구액이 달라집니다. 사용하지 않을 때 인스턴스만 중지하지 말고 `terraform destroy`로 Elastic IP까지 해제하세요.

기본 `t3.micro`는 비용 안전성을 우선한 값입니다. Elasticsearch를 포함한 전체 스택은 4 GiB swap을 사용하며 첫 빌드와 기동이 오래 걸릴 수 있습니다.

## 적용

```bash
cd infra/aws/ec2
cp terraform.tfvars.example terraform.tfvars
terraform init
terraform plan -out=tfplan
terraform apply tfplan
terraform output healthcheck_url
```

상태 확인:

```bash
curl "$(terraform output -raw healthcheck_url)"
terraform output -raw ssm_start_session
# 출력된 aws ssm start-session 명령을 실행합니다.
```

인스턴스 내부 로그:

```bash
sudo tail -f /var/log/my-piano-bootstrap.log
cd /opt/my-piano
sudo docker compose -f docker-compose.aws.yml ps
sudo docker compose -f docker-compose.aws.yml logs --tail=200 app
```

## 삭제

```bash
terraform destroy
```

`terraform.tfstate`는 생성 자원을 안전하게 삭제하는 데 필요합니다. Git에는 커밋하지 않으며 이 디렉터리에서 보존해야 합니다.
