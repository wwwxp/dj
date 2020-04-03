package com.wxp.inend.util



import java.util
import java.util.Properties

import org.apache.spark.SparkConf
import org.apache.spark.sql.types.{DataTypes, StructField, StructType}
import org.apache.spark.sql.{Row, SparkSession}
import org.apache.spark.streaming.dstream.ReceiverInputDStream
import org.apache.spark.streaming.kafka.KafkaUtils
import org.apache.spark.streaming.{Seconds, StreamingContext}

import scala.collection.mutable.ArrayBuffer

object EsHotSearch{


  var res=new Array[String](10);

  def hotSearch()={
    var in=EsHotSearch.getClass.getClassLoader.getResourceAsStream("windowsOrLinux.properties");

    var propertis:Properties=new Properties()
    var zk=propertis.getProperty("zookeeper.address");
    val checkpoint=propertis.getProperty("spark.checkpoint.path");              //放在linux上，要为linux的路径
    var sparkConf=new SparkConf()
      .setAppName("hotSearch")
      .setMaster("local[3]");

    var ssc=new StreamingContext(sparkConf,Seconds(10.toLong))

    var spark=SparkSession.builder().config(sparkConf).getOrCreate()
    var sparkSql=spark.sqlContext
    val sparkContext=spark.sparkContext

    val value: ReceiverInputDStream[(String, String)] = KafkaUtils.createStream(ssc, zk, "hotSearch", Map("esFood" -> 3))

    res=new Array[String](10);
    var index=sparkContext.longAccumulator("foodNum")

    val rowDStream=value.transform(rdd=>{
      rdd.map( e => {


        index.reset()

        var map=EsHotSearch.jsonStringToMap(e._2)
        (map.get("score").get.toString.toDouble,map.get("foodName").get.toString)
      }).sortByKey(false,1)



    }).foreachRDD(rdd=>{
      rdd.foreach(e=>{

        if(index.value.toInt<10) {
          res(index.value.toInt) = e._2;
          index.add(1)
          println(res(index.value.toInt))
        }

      })


    })

    ssc.checkpoint(checkpoint);

    ssc.start();
    ssc.awaitTermination();


  }

  def jsonStringToMap(jsonString:String):scala.collection.mutable.HashMap[Any,Any]={
    //jsonString的格式：{"键":值,"键":"值"}

    var jsonString2 = jsonString.substring(1,jsonString.length-1)

    var kvs=jsonString2.split(",\"");               //以,"为分隔符，为了防止“描述字段”中有逗号
    var map=scala.collection.mutable.HashMap[Any,Any]()
    for( kv <- kvs){
      var kv2=kv.split(":")
      var k=kv2(0).substring(0,kv2(0).length-1)   //取出键
      var v=kv2(1)                                //取出值
      if(v.endsWith("\"")){                       //如果值为字符串类型
        v=v.substring(1,v.length-1)
      }
      if(v.matches("[0-9]{1,}"))
          v=v+Math.random()*10
      map.put(k,v)
    }
    map
  }


}

