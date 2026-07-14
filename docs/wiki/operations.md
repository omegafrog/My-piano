# 운영

## 로컬 인프라

```bash
docker-compose up -d
./gradlew bootRun
```

로컬 인프라는 Kafka, Elasticsearch, Kibana, Redis를 제공한다.

## Wiki

```bash
./scripts/build-wiki.sh
./scripts/serve-wiki.sh
```

Wiki 실행 전 root `venv`에 `docs/wiki/requirements.txt` 의존성을 설치한다.
