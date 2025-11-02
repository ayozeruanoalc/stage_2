
# Search Engine - Stage 2

## Overview

```bash
├── benchmarking/                                    # Load-testing scripts, scenarios and result collectors (wrk/hey/ab examples)  
├── control/                                         # Orchestration & management helpers (docker-compose, deployment scripts, start/stop helpers)  
├── ingestion-service/                               # Service that fetches and pre-processes documents (collectors, parsers, normalizers)  
├── indexing-service/                                # Service responsible for building/storing indexes (tokenization, metadata extraction)  
├── search-service/                                  # REST search API and retrieval logic (query endpoints, ranking, pagination)  
├── src/main/resources/additional-files-for-testing/ # Small corpora, fixtures and test resources used by unit/integration tests  
├── .gitignore                                       # Git ignore rules  
├── pom.xml                                          # Maven multi-module configuration (project build)  
└── README.md                                        # This file
```

---

## Prerequisites

Install the following on your development machine:

- Java JDK 17
    - Verify: `java -version`
- Maven 3.6+
    - Verify: `mvn -v`
- curl (for quick endpoint checks)

---

## Building

Two common approaches:

1) Build one microservice (replace `{service-dir}` with actual directory)
```bash
cd {service-dir}
mvn clean package
```

2) Build entire multi-module project from repo root
```bash
# from repository root
mvn -T1C -pl ingestion-service,indexing-service,search-service,control clean package
```

Notes:
- Artifact JARs are produced under each service's `target/` directory (e.g. `ingestion-service/target/ingestion-service-1.0-SNAPSHOT.jar`).

---

## Running

### Ingestion Service

```bash
# (from repo root or individual service directories)
# Start ingestion-service
java -jar ingestion-service/target/ingestion-service-1.0-SNAPSHOT.jar [datalakePath] [logsFilePath]
```
| Argument / Option       | Purpose / meaning                                                            | Example value                                 |
|-------------------------|-------------------------------------------------------------------------------|-------------------------------------------|
| datalakePath            | Path to folder where raw files will be stored                               | `/data/datalake`                | 
| logsFilePath            | File where the app writes its own ingestion log                              | `/var/log/ingestion.log`                  | 

#### Usage
```bash
curl -X POST http://localhost:7001/ingest/1
```
```bash
curl -X GET http://localhost:7001/ingest/status/1
```
```bash
curl -X GET http://localhost:7001/ingest/list
```

### Indexing Service

```bash
# (from repo root or individual service directories)
# Start indexing-service
java -jar indexing-service/target/indexing-service-1.0-SNAPSHOT.jar [datalakePath] [metadataDBPath] [stopWordsReference] [MongoDBName] [MongoDBCollectionName] [MongoDBURI]
```
| Argument / Option       | Purpose / meaning                                                            | Example value                                 |
|-------------------------|-------------------------------------------------------------------------------|-------------------------------------------|
| datalakePath            | Path to datalake                               | `/data/datalake`                | 
| metadataDBPath            | File for storing metadata (SQlite database)                              | `/metadata/metadata.db`                  | 
| stopWordsReference | JSON with stopword references for a bunch of languages (file provided in resources)| `indexing-service/src/main/resources/stopwords-iso.json` |
| MongoDBName | MongoDB database name | `Big Data` |
| MongoDBCollectionName | MongoDB collection name | `Inverted Index` |
| MongoDBURI | MongoDB connection URI | `mongodb://localhost:27017` |


#### Usage
```bash
curl -X POST http://localhost:7002/index/update/1
```
```bash
curl -X POST http://localhost:7002/index/rebuild
```
```bash
curl -X GET http://localhost:7002/index/status
```

<!--

### B. Run with Maven (for development)
```bash
# from a service directory
mvn spring-boot:run
```
This is convenient for live code changes when using an IDE.

## Verify / Test Queries

Use the health endpoints and simple API calls to verify services are running.

### Health check
Most Spring Boot apps provide an actuator health endpoint:
```bash
curl -s http://localhost:8080/actuator/health | jq
```

Replace port with the service port.

### Example GET request
Assume `service-a` exposes `GET /api/v1/items` on port 8081:

```bash
curl -v http://localhost:8081/api/v1/items
```

### Example POST request
```bash
curl -X POST http://localhost:8081/api/v1/items \
  -H "Content-Type: application/json" \
  -d '{
    "name": "example",
    "value": 42
  }' | jq
```

### Gateway / Aggregation example
If you have an API gateway at port 8080 that routes to other services:

```bash
curl -v http://localhost:8080/api/items
```

### Postman
- Import your Postman collection (if available) and run the requests.
- Use environment variables for base URL and ports.

---

## Running Automated Tests

### Unit tests
Run unit tests for a single module:
```bash
cd service-a
mvn test
```

Run tests for the entire multi-module project:
```bash
mvn -T1C test
```

### Integration tests
Integration tests may require running dependent services (DB, external infra) or use Maven Failsafe:

```bash
# run integration tests that use the failsafe plugin
mvn verify -P integration-tests
# or
mvn -DskipUnitTests=false -Pintegration verify
```

Check your `pom.xml` for test profiles and instructions.

---

## Benchmarking / Load Test

Below are example benchmark steps and a couple of example scripts. Adapt endpoints/ports to your services.

### Quick single-endpoint test with hey
Install `hey` and run:

```bash
# Run 10k requests with concurrency 200 to gateway endpoint
hey -n 10000 -c 200 http://localhost:8080/api/items
```

Common `hey` options:
- `-n` total requests
- `-c` concurrency
- `-m` method (GET/POST)
- `-H` add header
- `-d` request body for POSTs

### Example benchmark script (scripts/bench_hey.sh)
Save this as `scripts/bench_hey.sh` and `chmod +x`:

```bash
#!/usr/bin/env bash
# scripts/bench_hey.sh
BASE="http://localhost:8080"
ENDPOINT="/api/items"
ITERATIONS=3
REQUESTS=5000
CONCURRENCY=100

echo "timestamp,endpoint,requests,concurrency,avg_ms,p50_ms,p95_ms,p99_ms,errors" > bench-results.csv

for i in $(seq 1 $ITERATIONS); do
  echo "Run $i: $REQUESTS requests at concurrency $CONCURRENCY on $BASE$ENDPOINT"
  OUT=$(hey -n $REQUESTS -c $CONCURRENCY -q 0 $BASE$ENDPOINT 2>/dev/null)
  # parse sample fields (hey format may vary)
  AVG=$(echo "$OUT" | awk '/Average/ {print $2}')
  P50=$(echo "$OUT" | awk '/50%/ {print $2}')
  P95=$(echo "$OUT" | awk '/95%/ {print $2}')
  P99=$(echo "$OUT" | awk '/99%/ {print $2}')
  ERRORS=$(echo "$OUT" | awk '/Total Error/ {print $3}' | tr -d '\r\n' )
  echo "$(date -u +%FT%TZ),$ENDPOINT,$REQUESTS,$CONCURRENCY,$AVG,$P50,$P95,$P99,$ERRORS" >> bench-results.csv
done

echo "Results appended to bench-results.csv"
```

### More advanced: wrk
Example wrk command:
```bash
wrk -t12 -c400 -d60s -s post.lua http://localhost:8080/api/items
```
Use `post.lua` to define POST body and headers if needed.

---

## Troubleshooting & Tips

- Port conflicts:
  - Ensure each service uses a distinct port. Check `application.properties` / `application.yml` or override with `--server.port=XXXX`.
- Logs:
  - For local runs: logs print to console. For Docker: `docker-compose logs -f`.
- Common failure reasons:
  - Missing configuration (config server not started)
  - External resource unavailable (DB, messaging system)
  - Wrong environment variables
- Health & readiness:
  - Use `/actuator/health` and `/actuator/health/readiness` (if defined) before running load tests.
- Memory:
  - For large load tests, increase Java heap: `-Xms512m -Xmx2g` by setting `JAVA_OPTS=-Xmx2g` before `java -jar`.
- Running in IDE:
  - Use your IDE run configurations; pass VM args and environment variables there.

---

## Example: Full quickstart (local)

1. Build everything:
```bash
mvn -T1C clean package -DskipTests
```

2. Start services (example):
```bash
java -jar service-config/target/service-config-1.0.jar &   # config server
java -jar service-a/target/service-a-1.0.jar --server.port=8081 &
java -jar service-b/target/service-b-1.0.jar --server.port=8082 &
java -jar service-gateway/target/service-gateway-1.0.jar --server.port=8080 &
```

3. Verify:
```bash
curl http://localhost:8080/actuator/health | jq
curl http://localhost:8081/api/v1/items | jq
```

4. Run a quick benchmark:
```bash
hey -n 5000 -c 50 http://localhost:8080/api/items
```

---

## Contributing

- Follow the repository style guide (if present).
- Run `mvn test` before submitting PRs.
- Provide Postman collection or example cURL snippets for any new endpoints.

---

-->