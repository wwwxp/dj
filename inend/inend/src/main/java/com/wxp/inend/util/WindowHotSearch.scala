package com.wxp.inend.util

import java.util
import java.util.Properties

import org.apache.spark.SparkConf
import org.apache.spark.sql.SparkSession
import org.apache.spark.streaming.dstream.{DStream, ReceiverInputDStream}
import org.apache.spark.streaming.kafka.KafkaUtils
import org.apache.spark.streaming.{Seconds, StreamingContext}

object WindowHotSearch{

  var res = new java.util.HashMap[String,Int]()


  def hotSearch()={
    var in=WindowHotSearch.getClass.getClassLoader.getResourceAsStream("windowsOrLinux.properties");

    var propertis:Properties=new Properties()
    propertis.load(in)
    var zk=propertis.getProperty("zookeeper.address");
    val checkpoint=propertis.getProperty("spark.checkpoint.path"); //放在linux上，要为linux的路径
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

    val value: ReceiverInputDStream[(String, String)] = KafkaUtils.createStream(ssc, zk, "hotSearch", Map(propertis.getProperty("sparkStreaming.kafka.topic.name")  -> 3))
    var wordOneDStream= value.transform(rdd=>{
      rdd.map(e=>{
        val cols=e._2.split(" ");
        if(cols(6).contains("=")){
          var word=cols(6).split("=")(1)

        (java.net.URLDecoder.decode(word,"utf-8"),1)

        }else
          ("",-1)

      })
    });

   /* var totalDStream:DStream[(String,Int)] = wordOneDStream.updateStateByKey[Int]((seq:Seq[Int],history:Option[Int])=> {

      if (seq.size>0) {
      var sum = 0;
      for (i <- seq) {
        if(i != -1) {
          sum += i;
        }
      }
        println("history="+history.getOrElse(0))
        println("sum="+sum)
      Some(history.getOrElse(0) + sum);
    }else
        None
    });*/

    //主题：20秒对“过去一个小时”的“数据量”进行一次统计
    var totalDStream=wordOneDStream.reduceByKeyAndWindow((e1:Int,e2:Int)=>{e1+e2},Seconds(3600),Seconds(20))
      .transform(rdd => {
        rdd.map(e=>{
          (e._2,e._1)
        })
      });

      totalDStream.transform(rdd=>{
      rdd.sortByKey(false,1);
    }).foreachRDD(rdd=>{
      rdd.foreach(e=>{

        for(elem <- nameArray){

          if(e._2!="" && elem.contains(e._2)) {

              res.put(e._2,e._1)

          }
        }

      })
    })



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

