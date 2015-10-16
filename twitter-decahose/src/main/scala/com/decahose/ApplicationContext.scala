/**
 * (C) Copyright IBM Corp. 2015, 2015
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */
package com.decahose

import org.apache.spark.SparkContext
import org.apache.spark.SparkContext._
import org.apache.spark.SparkConf
import org.apache.spark.sql.hive._
import org.apache.spark.streaming._
import org.apache.spark.streaming.StreamingContext._
import org.apache.spark.streaming.dstream.DStream
import org.elasticsearch.spark._ 
import java.io.BufferedInputStream
import java.io.File
import java.io.FileInputStream
import java.io.InputStream
import org.apache.hadoop.conf._
import org.apache.hadoop.fs._
import java.net.URI


object ApplicationContext
{
    val sparkConf = new SparkConf()
    //sparkConf.setMaster(masterNode)
    sparkConf.setAppName(LoadConf.globalConf.getString("appName") + " - Decahose")
    sparkConf.set("spark.scheduler.mode", "FAIR")
    
    //Spark master resources
    sparkConf.set("spark.executor.memory",s"""${LoadConf.sparkConf.getString("decahose.executorMemory")}""")
    sparkConf.set("spark.ui.port",s"""${LoadConf.sparkConf.getString("decahose.sparkUIPort")}""")
    sparkConf.set("spark.cores.max",s"""${LoadConf.sparkConf.getInt("decahose.totalCores")}""")

    //Do not allow infer schema. Schema must be defined at ES before start the app
    sparkConf.set("es.index.auto.create", "false")
    sparkConf.set("es.batch.size.bytes", "300000000")
    sparkConf.set("es.batch.size.entries", "10000")
    sparkConf.set("es.batch.write.refresh", "false")
    sparkConf.set("es.batch.write.retry.count", "50")
    sparkConf.set("es.batch.write.retry.wait", "500")
    sparkConf.set("es.http.timeout", "5m")
    sparkConf.set("es.http.retries", "50")
    sparkConf.set("es.action.heart.beat.lead", "50")

    val sparkContext = new SparkContext(sparkConf)
    val sqlContext = new HiveContext(sparkContext)

    /* config sqlContext */
    sqlContext.setConf("spark.sql.shuffle.partitions", s"""${LoadConf.sparkConf.getInt("partitionNumber")}""")
    sqlContext.setConf("spark.sql.codegen", "true")

    /* delete HDFS processed files */
    val hadoopFS :FileSystem = FileSystem.get(sparkContext.hadoopConfiguration)
}