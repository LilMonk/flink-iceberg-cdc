package io.lilmonk

import org.apache.flink.streaming.api.CheckpointingMode
import org.apache.flink.streaming.api.scala._
import org.apache.flink.table.api.bridge.scala.StreamTableEnvironment
import org.apache.flink.table.api.{EnvironmentSettings, FieldExpression}
import org.apache.hadoop.conf.Configuration
import org.apache.iceberg.catalog.TableIdentifier
import org.apache.iceberg.flink.sink.FlinkSink
import org.apache.iceberg.flink.{CatalogLoader, FlinkSchemaUtil, TableLoader}
import org.apache.iceberg.types.Types
import org.apache.iceberg.{DistributionMode, PartitionSpec, Schema}

import java.util


object DataStreamCDC {
  def main(args: Array[String]) {
    val settings = EnvironmentSettings.newInstance().inStreamingMode().build()
    val env = StreamExecutionEnvironment.getExecutionEnvironment
    env.enableCheckpointing(1000L, CheckpointingMode.EXACTLY_ONCE)
    //    env.setStateBackend(new HashMapStateBackend())
    //    env.getCheckpointConfig.setCheckpointStorage("file:///tmp/flink-checkpoint")
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

    val customersSinkSQL =
      """
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
        |    'uri'='http://localhost:8181',
        |    'io-impl'='org.apache.iceberg.aws.s3.S3FileIO',
        |    's3-endpoint'='http://localhost:9000',
        |    'warehouse'='s3://warehouse/wh/'
        |  );
        |""".stripMargin
    tableEnv.executeSql(customersSinkSQL)

    val customersTable = tableEnv.from("customers").select($"*")

    // To print cdc data
    // customersTable.execute().print() // You can either print the cdc data or push to sink.

    val hadoopConf = new Configuration()
    val catalogProperties = new util.HashMap[String, String]()
    catalogProperties.put("uri", "http://localhost:8181")
    catalogProperties.put("io-impl", "org.apache.iceberg.aws.s3.S3FileIO")
    catalogProperties.put("warehouse", "s3://warehouse/wh/")
    catalogProperties.put("s3.endpoint", "http://localhost:9000")
    val catalogLoader = CatalogLoader.custom("demo", catalogProperties, hadoopConf, "org.apache.iceberg.rest.RESTCatalog")
    val schema = new Schema(
      Types.NestedField.required(1, "id", Types.DecimalType.of(20, 0)),
      Types.NestedField.optional(2, "first_name", Types.StringType.get),
      Types.NestedField.required(3, "last_name", Types.StringType.get),
      Types.NestedField.required(4, "email", Types.StringType.get),
      Types.NestedField.required(5, "database_name", Types.StringType.get),
      Types.NestedField.required(6, "table_name", Types.StringType.get)
    )
    val catalog = catalogLoader.loadCatalog
    val databaseName = "default"
    val tableName = "customers_sink"
    val outputTable = TableIdentifier.of(databaseName, tableName)
    if (!catalog.tableExists(outputTable)) catalog.createTable(outputTable, schema, PartitionSpec.unpartitioned)

    val customerDS = tableEnv.toChangelogStream(customersTable).javaStream
    FlinkSink.forRow(customerDS, FlinkSchemaUtil.toSchema(schema))
      .tableLoader(TableLoader.fromCatalog(catalogLoader, outputTable))
      .distributionMode(DistributionMode.HASH)
      .writeParallelism(2)
      .append()

    // execute the program
    env.execute("Flink DataStream CDC POC in Scala")
  }
}
