
# Search Engine - Stage 2

## Introduction

Welcome to the Search Engine Stage 2 project. This repository contains the source code and resources for building a scalable and efficient search engine composed of several modular microservices. Each module handles different aspects of the pipeline, including ingestion, indexing, searching, and control.

This README provides detailed instructions on building, running, benchmarking, and managing the components, helping developers and users to quickly get started and understand the workflow.



---

## Prerequisites

Install the following on your development machine:

- Java JDK 17
    - Verify: `java -version`
- Maven 3.6+
    - Verify: `mvn -v`
- `curl` (for quick endpoint checks)

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
curl -X POST http://localhost:7001/ingest/201
```
```json
{"status":"downloaded","book_id":201,"path":"C:/..."}
```
```bash
curl -X GET http://localhost:7001/ingest/status/201
```
```json
{"book_id":201,"status":"available"}
```
```bash
curl -X GET http://localhost:7001/ingest/list
```
```json
{"count":2,"books":[1,201]}
```

### Indexing Service

```bash
# (from repo root or individual service directories)
# Start indexing-service
java -jar indexing-service/target/indexing-service-1.0-SNAPSHOT.jar [datalakePath] [metadataDBPath] [stopWordsReference] [MongoDBName] [MongoDBCollectionName] [MongoDBURI]
```
| Argument / Option       | Purpose / meaning                                                            | Example value                               |
|-------------------------|-------------------------------------------------------------------------------|-----------------------------------------|
| datalakePath            | Path to datalake                               | `/data/datalake`              | 
| metadataDBPath            | File for storing metadata (SQlite database)                              | `/metadata/metadata.db`                | 
| stopWordsReference | JSON with stopword references for a bunch of languages (file provided in resources)| `indexing-service/src/main/resources/stopwords-iso.json` |
| MongoDBName | MongoDB database name | `BigData` |
| MongoDBCollectionName | MongoDB collection name | `InvertedIndex` |
| MongoDBURI | MongoDB connection URI | `mongodb://localhost:27017` |


#### Usage
```bash
curl -X POST http://localhost:7002/index/update/201
```
```json
{"book_id":201,"index":"updated"}
```
```bash
curl -X POST http://localhost:7002/index/rebuild
```
```json
{"elapsed_time":"1330,4s","books_processed":962}
```
```bash
curl -X GET http://localhost:7002/index/status
```
```json
{"last_update":"2025-11-03T21:48:14.161335400Z","books_indexed":962,"index_size_MB":"480.57"}
```

### Search Service

```bash
# (from repo root or individual service directories)
# Start search-service
java -jar search-service/target/search-service-1.0-SNAPSHOT.jar [metadataDBPath] [MongoDBURI] [MongoDBName] [MongoDBCollectionName] [sortingCriteria]
```
| Argument / Option       | Purpose / meaning                                                            | Example value                               |
|-------------------------|-------------------------------------------------------------------------------|-----------------------------------------|
| metadataDBPath            | Metadata SQlite Database                               | `/metadata/metadata.db`              | 
| MongoDBURI           | MongoDB Connection URI                             | `mongodb://localhost:27017`                | 
| MongoDBName | MongoDB database name | `BigData` |
| MongoDBCollectionName | MongoDB collection name | `InvertedIndex` |
| sortingCriteria | Indicate sorting mode for query results: by bookID / by number of appereances of a specific word | `id` / `frequency` |


#### Usage
```bash
curl -X GET "http://localhost:7003/search?q=mind"
```
```json
{
  "query": "mind",
  "filters": {},
  "count": 869,
  "results": [
    {
      "id": 100,
      "title": "The Complete Works of William Shakespeare",
      "author": "William Shakespeare",
      "language": "English",
      "year": 1994,
      "frequency": 400
    },
    {
      "id": 145,
      "title": "Middlemarch",
      "author": "George Eliot",
      "language": "English",
      "year": 1994,
      "frequency": 385
    }]
}
```
```bash
curl -X GET "http://localhost:7003/search?q=mind,heart"
```
```json
{
   "query":"mind,heart",
   "filters":{
      
   },
   "count":836,
   "results":[
      {
         "id":100,
         "title":"The Complete Works of William Shakespeare",
         "author":"William Shakespeare",
         "language":"English",
         "year":1994,
         "frequency":400
      },
      {
         "id":145,
         "title":"Middlemarch",
         "author":"George Eliot",
         "language":"English",
         "year":1994,
         "frequency":385
      }]
}
```
```bash
# A space is interpreted as %20 by curl
curl -X GET "http://localhost:7003/search?q=mind&author=William%20Shakespeare&language=English&year=1994"
```
```bash
# You can avoid that by using a browser:
http://localhost:7003/search?q=mind&author=William Shakespeare&language=English&year=1994
```
```json
{
   "query":"mind",
   "filters":{
      "year":"1994",
      "author":"William Shakespeare",
      "language":"English"
   },
   "count":1,
   "results":[
      {
         "id":100,
         "title":"The Complete Works of William Shakespeare",
         "author":"William Shakespeare",
         "language":"English",
         "year":1994,
         "frequency":400
      }
   ]
}
```

### Control Module

```bash
# (from repo root or individual service directories)
# Start control
java -jar control/target/control-1.0-SNAPSHOT.jar [stateJSON] [bookID | bookID1, bookID2...]
```
| Argument / Option       | Purpose / meaning                                                            | Example value                                 |
|-------------------------|-------------------------------------------------------------------------------|-------------------------------------------|
| stateJSON            | JSON file that tracks if a book has been already indexed                            | `/logs/state.json`                | 
| bookID(s)           | Book or books to be processed by the pipeline                            | `1` / `1 2 3`                  | 


#### Output examples
```bash
[STATE] Book 1 -> Stage: INGESTING (not persisted yet)
[STATE] Book 1 -> Stage: INDEXING (saved)
[STATE] Book 1 -> Stage: INDEXED (saved)
{"book_id":1,"status":"ok"}
[STATE] Book 2 -> Stage: INGESTING (not persisted yet)
[STATE] Book 2 -> Stage: INDEXING (saved)
[STATE] Book 2 -> Stage: INDEXED (saved)
{"book_id":2,"status":"ok"}
[STATE] Book 3 -> Stage: INGESTING (not persisted yet)
[STATE] Book 3 -> Stage: INDEXING (saved)
[STATE] Book 3 -> Stage: INDEXED (saved)
{"book_id":3,"status":"ok"}
state_size=3
```

## Benchmarks


To evaluate the performance of this project, microbenchmarking and integration benchmarking tests were conducted using **[JMH (Java Microbenchmark Harness)](https://openjdk.org/projects/code-tools/jmh/)** — an official OpenJDK tool designed for precise Java method performance measurement.
The benchmarks were run from IntelliJ IDEA using the ["JMH Java Microbenchmark Harness"](https://plugins.jetbrains.com/plugin/7529-jmh-java-microbenchmark-harness) from [Sergey Ponomarev](https://plugins.jetbrains.com/vendor/d848ca38-90d6-4adc-86df-9bf931fd8908).

### Configuration

Benchmarks were executed under the following conditions:

| Parameter | Value                                           |
|------------|-------------------------------------------------|
| **JMH Version** | 1.36                                            |
| **JDK** | OpenJDK 17                                      |
| **Benchmark Mode** | `Throughput` (measures the number of operations executed per second) |
| **Warmup** | 5 iterations                                    |
| **Measurement** | 10 iterations                                   |
| **Forks** | 1                                               |
| **jvmArgs** | {"-Xmx4G"}                                      |

### Running the Microbenchmarks

### Passing Arguments to Microbenchmarks

If a benchmark requires arguments, you only need to modify the Run Configuration of the benchmark by adding the necessary parameters according to the @Params annotations of the respective benchmark, and follow the steps below.

If a benchmark has a single @Param with predefined values, it can be run without specifying arguments.

```java
@Param({"10", "100", "1000"})
private int numberOfBooks;
```


For example, for the MetadataDatabaseInsertionBenchmark, you can run it with arguments as shown:
#### MetadataDatabaseInsertionBenchmark

```java
@Param({""})
private String datalakePath;

@Param({""})
private String metadataPath;

@Param({""})
private String idBook;
```


```bash
# Arguments:
com.guanchedata.benchmark.microbenchmark.indexingservice.databaseinsertion.MetadataDatabaseInsertionBenchmark.*
-p
datalakePath=[datalakePath]
-p
metadataPath=[metadataPath]
-p
idBook=[bookID]
```

| Argument      | Purpose / Description                           | Example value            |
|---------------|------------------------------------------------|-------------------------|
| datalakePath  | Path to the data lake directory                 | `/data/datalake`         |
| metadataPath  | Path to the metadata database                    | `/metadata/metadata.db`  |
| idBook        | Identifier of the book to process                | `201`                   |

### Running the Integration Benchmarks

To run the Integration benchmarks, first ensure that the three API services ingestion, indexing, and query are up and running. Once these services are started, you can execute the benchmarks without needing to specify any arguments.














