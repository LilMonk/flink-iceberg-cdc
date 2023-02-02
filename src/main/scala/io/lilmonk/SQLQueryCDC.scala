package io.lilmonk

import org.apache.flink.streaming.api.CheckpointingMode
import org.apache.flink.streaming.api.scala.StreamExecutionEnvironment
import org.apache.flink.table.api.bridge.scala.StreamTableEnvironment
import org.apache.flink.table.api.{EnvironmentSettings, FieldExpression}

object SQLQueryCDC {
  def main(args: Array[String]) {
    val settings = EnvironmentSettings.newInstance().inStreamingMode().build()
    val env = StreamExecutionEnvironment.getExecutionEnvironment
    env.enableCheckpointing(1000L, CheckpointingMode.EXACTLY_ONCE)
    val tableEnv = StreamTableEnvironment.create(env, settings)

    val customersSQL =
      """
        |CREATE TABLE customers (
        |    database_name STRING METADATA VIRTUAL,
        |    table_name STRING METADATA VIRTUAL,
        |    `id` DECIMAL(20, 0) NOT NULL,
        |    first_name STRING,
        |    last_name STRING,
        |    email STRING,
        |    PRIMARY KEY (`id`) NOT ENFORCED
        |  ) WITH (
        |    'connector' = 'mysql-cdc',
        |    'hostname' = 'localhost',
        |    'port' = '3306',
        |    'username' = 'root',
        |    'password' = 'root_pass',
        |    'database-name' = 'inventory',
        |    'table-name' = 'customers'
        |  );
        |""".stripMargin
    tableEnv.executeSql(customersSQL)

    val FILE_SINK_LOCATION = "/home/sentinel/upworks/mohsin_hashmi/flink_cdc_poc"
    val customersFileSinkSQL =
      s"""
         |CREATE TABLE customers_sink (
         |    database_name STRING,
         |    table_name STRING,
         |    `id` DECIMAL(20, 0) NOT NULL,
         |    first_name STRING,
         |    last_name STRING,
         |    email STRING,
         |    PRIMARY KEY (`id`) NOT ENFORCED
         |  ) WITH (
         |    'connector'='iceberg',
         |    'catalog-name'='iceberg_catalog',
         |    'catalog-type'='hadoop',
         |    'warehouse'='file://$FILE_SINK_LOCATION/warehouse'
         |  );
         |""".stripMargin

    val REST_CATALOG_URI = "http://localhost:8181"
    val S3_ENDPOINT = "http://localhost:9000"
    val S3_WAREHOUSE_LOCATION = "s3://warehouse/wh/"
    val customersSinkSQL =
      s"""
         |CREATE TABLE customers_sink (
         |    database_name STRING,
         |    table_name STRING,
         |    `id` DECIMAL(20, 0) NOT NULL,
         |    first_name STRING,
         |    last_name STRING,
         |    email STRING,
         |    PRIMARY KEY (`id`) NOT ENFORCED
         |  ) WITH (
         |    'connector'='iceberg',
         |    'catalog-name'='iceberg_catalog',
         |    'catalog-impl'='org.apache.iceberg.rest.RESTCatalog',
         |    'uri'='$REST_CATALOG_URI',
         |    'io-impl'='org.apache.iceberg.aws.s3.S3FileIO',
         |    's3-endpoint'='$S3_ENDPOINT',
         |    'warehouse'='$S3_WAREHOUSE_LOCATION'
         |  );
         |""".stripMargin
    tableEnv.executeSql(customersSinkSQL)

    // To print the cdc data.
    //    val table: Table = tableEnv.sqlQuery("SELECT * FROM customers")
    //    table.execute().print()

    val customersTable = tableEnv.from("customers").select($"*")
    customersTable.executeInsert("customers_sink")

    env.execute("Flink SQL CDC POC in Scala")
  }
}
