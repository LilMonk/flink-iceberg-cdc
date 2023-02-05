# Flink Mysql CDC Iceberg POC

In this poc we are streaming cdc events from Mysql to Iceberg tables in Minio(S3).

## How to write queries for flink:

You can pass multiple valid queries in these files:

1. source_queries.txt
2. sink_queries.txt
3. queries.txt
   All the queries should be valid and end with `;`, as the parser will use this character to split multiple queries.

## Steps to run:

1. Start docker containers. Run
    ```shell
   docker-compose build & docker-compose up -d
   ```
2. Build the jar.
    ```shell
    mvn clean install
    ```
   This will build jar in : `target/cdc-iceberg-poc-1.0-SNAPSHOT.jar`
3. Open flink ui in browser: **http://localhost:8081**
4. Go to `Submit New Job` in the left menu.
5. Upload the jar from target folder.
6. Click on the uploaded jar and in the **Program Argument** box paste the commandline options for the app:
   ```shell
   -s /data/source_queries.txt -o /data/sink_queries.txt -p /data/queries.txt
   ```
7. Submit the job. Ignore the error pop up message if it says that could not wait for the job to over. Check the
   **running jobs** section.
8. Open the minio in browser: **http://localhost:9001**
9. Take the username and password from the **docker-compose.yaml** file. Check the bucket for data.
10. Open the spark notebook in browser: **http://localhost:8888**
11. Open **notebook/Query_Iceberg_Tables.ipynb** and run the notebook to see the iceberg table data.