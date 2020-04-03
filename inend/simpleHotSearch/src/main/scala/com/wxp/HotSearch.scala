package com.wxp

import java.util.Properties

import org.apache.spark.SparkConf
import org.apache.spark.sql.SparkSession
import org.apache.spark.streaming.dstream.{DStream, ReceiverInputDStream}
import org.apache.spark.streaming.kafka.KafkaUtils
import org.apache.spark.streaming.{Seconds, StreamingContext}

import scala.collection.mutable.ArrayBuffer

object HotSearch{

  def hotSearch()={
    System.setProperty("HADOOP_USER_NAME", "hadoop");
    var in=HotSearch.getClass.getClassLoader.getResourceAsStream("windowsOrLinux.properties");


    var propertis:Properties=new Properties()

    propertis.load(in);

    var zk=propertis.getProperty("zookeeper.address");
    val checkpoint="/home/hadoop/data/checkpoint"            //放在linux上，要为linux的路径
    var sparkConf=new SparkConf()
      .setAppName("hotSearch")
      .setMaster("local[3]")

    var ssc=new StreamingContext(sparkConf,Seconds(10.toLong))
    ssc.checkpoint(checkpoint);

    var spark=SparkSession.builder().config(sparkConf).getOrCreate()
    var sparkSql=spark.sqlContext
    val sparkContext=spark.sparkContext

    //获得foods中的所有foodName

    var p=new Properties();
    p.put("driver",propertis.getProperty("mysql.driver"))
    p.put("user",propertis.getProperty("mysql.username"));
    p.put("password",propertis.getProperty("mysql.password"));

    var foodDFrame=sparkSql.read.jdbc(propertis.getProperty("mysql.url"),"food",p)

    var nameRDD=foodDFrame.rdd.map(row=>{

      row.getAs[String]("foodName")
    });

    var nameArray =nameRDD.top(100)

    val value: ReceiverInputDStream[(String, String)] = KafkaUtils.createStream(ssc, zk, "hotSearch", Map(propertis.getProperty("sparkStreaming.kafka.topic.name") -> 3))
    var wordOneDStream= value.transform(rdd=>{
      rdd.flatMap(e=>{

        var foodsNames=new ArrayBuffer[String]()
        val cols=e._2.split(" ");
        if(cols(6).contains("=")){
          var word=java.net.URLDecoder.decode(cols(6).split("=")(1),"utf-8")    //取出查找关键字，并为（关键字,1）

          for(foodName <- nameArray){

            if(foodName.contains(word))
              foodsNames.append(foodName)
          }

        }
        foodsNames
      }).map(e=>{
        (e,1)
      })
    });

    var totalDStream = wordOneDStream.updateStateByKey[Int]((seq:Seq[Int],history:Option[Int])=> {

      Option(history.getOrElse(0) + seq.size);
    }
    ).transform(rdd => {
      rdd.map(e=>{
        (e._2,e._1)
      })
    });

    /* //主题：20秒对“过去一个小时”的“数据量”进行一次统计
     var totalDStream=wordOneDStream.reduceByKeyAndWindow((e1:Int,e2:Int)=>{e1+e2},Seconds(3600),Seconds(20))
       .transform(rdd => {
         rdd.map(e=>{
           (e._2,e._1)
         })
       });*/

    totalDStream.transform(rdd=>{
      rdd.sortByKey(false,1);
    }).foreachRDD(rdd=>{

      rdd.foreachPartition(partition=>{
        var con=ConnectionPool.getConnection
        var sta=con.prepareStatement("insert into hotSearch values(?,?) on duplicate key update foodName=?,number=?")

        partition.foreach(e=>{
          sta.setString(1,e._2)
          sta.setInt(2,e._1)
          sta.setString(3,e._2)
          sta.setInt(4,e._1)
          sta.addBatch()

        })

        sta.executeBatch()
        ConnectionPool.close(con)
      })



    })



    ssc.start();
    ssc.awaitTermination();


  }

  def main(args: Array[String]): Unit = {
    hotSearch()
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
