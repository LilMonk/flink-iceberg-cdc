package io.lilmonk

import io.lilmonk.utils.{ArgumentParser, FileReader}
import org.apache.flink.streaming.api.CheckpointingMode
import org.apache.flink.streaming.api.scala.StreamExecutionEnvironment
import org.apache.flink.table.api.EnvironmentSettings
import org.apache.flink.table.api.bridge.scala.StreamTableEnvironment

object SQLQueryCDC {
  def main(args: Array[String]): Unit = {
    val appOption = ArgumentParser.getOptions(args)
    val settings = EnvironmentSettings.newInstance().inStreamingMode().build()
    val env = StreamExecutionEnvironment.getExecutionEnvironment
    env.enableCheckpointing(1000L, CheckpointingMode.EXACTLY_ONCE)
    val tableEnv = StreamTableEnvironment.create(env, settings)
    val fileReader = FileReader()

    val sourceQueries = fileReader.read(appOption.sourceQueriesFile)
    val sinkQueries = fileReader.read(appOption.sinkQueriesFile)
    val queries = fileReader.read(appOption.queriesFile)

    sourceQueries.foreach(q => tableEnv.executeSql(q))
    sinkQueries.foreach(q => tableEnv.executeSql(q))
    queries.foreach(q => tableEnv.executeSql(q).await())

    //    val FILE_SINK_LOCATION = "/home/sentinel/upworks/mohsin_hashmi/flink_cdc_poc"
    //    val customersFileSinkSQL =
    //      s"""
    //         |CREATE TABLE customers_sink (
    //         |    `id` DECIMAL(20, 0) NOT NULL,
    //         |    first_name STRING,
    //         |    last_name STRING,
    //         |    email STRING,
    //         |    PRIMARY KEY (`id`) NOT ENFORCED
    //         |  ) WITH (
    //         |    'connector'='iceberg',
    //         |    'catalog-name'='iceberg_catalog',
    //         |    'catalog-type'='hadoop',
    //         |    'warehouse'='file://$FILE_SINK_LOCATION/warehouse'
    //         |  );
    //         |""".stripMargin
    //
    //    val REST_CATALOG_URI = "http://localhost:8181"
    //    val S3_ENDPOINT = "http://localhost:9000"
    //    val S3_WAREHOUSE_LOCATION = "s3://warehouse/wh/"
    //    val S3_ACCESS_KEY = "root_user"
    //    val S3_SECRET_KEY = "root_pass"
    //
    //    val customersSinkSQL =
    //      s"""
    //         |CREATE TABLE customers_sink (
    //         |    `id` DECIMAL(20, 0) NOT NULL,
    //         |    first_name STRING,
    //         |    last_name STRING,
    //         |    email STRING,
    //         |    PRIMARY KEY (`id`) NOT ENFORCED
    //         |  ) WITH (
    //         |    'connector'='iceberg',
    //         |    'catalog-name'='iceberg_catalog',
    //         |    'catalog-impl'='org.apache.iceberg.rest.RESTCatalog',
    //         |    'uri'='$REST_CATALOG_URI',
    //         |    'io-impl'='org.apache.iceberg.aws.s3.S3FileIO',
    //         |    's3.endpoint'='$S3_ENDPOINT',
    //         |    's3.path.style.access'='true',
    //         |    's3.access-key'='$S3_ACCESS_KEY',
    //         |    's3.secret-key'='$S3_SECRET_KEY',
    //         |    'warehouse'='$S3_WAREHOUSE_LOCATION'
    //         |  );
    //         |""".stripMargin
    //
    //    println(customersSinkSQL)
    //    tableEnv.executeSql(customersSinkSQL)

    // To print the cdc data.
    //    val table: Table = tableEnv.sqlQuery("SELECT * FROM customers")
    //    table.execute().print()

    //    val customersTable = tableEnv.from("customers").select($"*")
    //    customersTable.executeInsert("customers_sink").await()

    env.execute("Flink SQL CDC POC in Scala")
  }
}
