package com.wxp.inend.util;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.hbase.Cell;
import org.apache.hadoop.hbase.CellUtil;
import org.apache.hadoop.hbase.HBaseConfiguration;
import org.apache.hadoop.hbase.TableName;
import org.apache.hadoop.hbase.client.*;
import org.apache.hadoop.hbase.mapreduce.TableMapReduceUtil;
import org.apache.hadoop.hbase.mapreduce.TableReducer;
import org.apache.hadoop.hbase.util.Bytes;
import org.apache.hadoop.io.*;


import org.apache.hadoop.mapred.jobcontrol.JobControl;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.jobcontrol.ControlledJob;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;


import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;

import java.sql.Connection;
import java.sql.PreparedStatement;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.util.*;

public class UserImg {


    static Configuration conf=new Configuration();

    static FileSystem fs=null;

    static Properties properties;

    static{
        System.setProperty("HADOOP_USER_NAME", "hadoop");
        InputStream in = UserImg.class.getClassLoader().getResourceAsStream("windowsOrLinux.properties");
        properties=new Properties();
        try {
            fs = FileSystem.get(new URI("hdfs://192.168.2.101:9000"), conf, "hadoop");
            properties.load(in);
        }catch(Exception e){
            e.printStackTrace();
        }
    }

    public static int UVNumber;

    //192.168.2.1 - - [07/Mar/2019:12:52:13 +0800] "GET /elm/findInEs?value=%E7%95%AA%E8%8C%84%E7%82%92%E8%9B%8B HTTP/1.1" 500   5744 "http://192.168.2.101/elm/index.html"
    //  0         1 2           3             4      5                      6                                       7       8      9                10
    //  ip         user        date          时区  请求方式               访问的资源                                 http版本  响应码  port             外链
    // "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/71.0.3578.98 Safari/537.36"
    //      11          12   13  14     15   16         17              18    19    20      21                  22
    //                                              客户端的浏览器信息

    //（1）第一个job开始：通过“ip、date”划分session
    private static class UVGroup extends WritableComparator {

        UVGroup(){
            super(UVBean.class,true);
        }

        @Override
        public int compare(WritableComparable a, WritableComparable b) {
            if(!(a instanceof UVBean) || !(b instanceof UVBean)){
                throw new RuntimeException("类型比较错误");
            }

            UVBean u1=(UVBean)a;
            UVBean u2=(UVBean)b;

            return u1.getIp().compareTo(u2.getIp());
        }
    }

    private static class UVBean implements WritableComparable<UVBean>{

        private String ip;
        private String date;

        private String sessionID="";

        public UVBean(){
            super();

        }

        public UVBean(String i,String d){
            ip=i;
            date=d;
        }

        public String getIp() {
            return ip;
        }

        public void setIp(String ip) {
            this.ip = ip;
        }

        public String getDate() {
            return date;
        }

        public void setDate(String date) {
            this.date = date;
        }

        public String getSessionID() {
            return sessionID;
        }

        public void setSessionID(String sessionID) {
            this.sessionID = sessionID;
        }

        @Override
        public int compareTo(UVBean other) {
            long res1=ip.compareTo(other.getIp());
            if(res1==0){
                return (int)MyDateUtil.dateMinusDate(date,other.getDate());
            }else if(res1>0){
                return 1;
            }else{
                return -1;
            }


        }

        @Override
        public void write(DataOutput dataOutput) throws IOException {
            dataOutput.writeUTF(ip);
            dataOutput.writeUTF(date);
            dataOutput.writeUTF(sessionID);
        }

        @Override
        public void readFields(DataInput dataInput) throws IOException {
            ip=dataInput.readUTF();
            date=dataInput.readUTF();
            sessionID=dataInput.readUTF();
        }

        @Override
        public String toString() {
            return sessionID+"\t"+ip +"\t" +date;
        }
    }


    private static class UVMapper extends Mapper<LongWritable,Text,UVBean,Text>{
        UVBean mk=new UVBean();
        Text mv=new Text();
        @Override
        protected void map(LongWritable key, Text value, Context context) throws IOException, InterruptedException {

            String[] fileds=value.toString().split(" ");

            String ip=fileds[0];
            String date=MyDateUtil.getDateTimeByDateTime(fileds[3].substring(1));

            mk.setIp(ip);
            mk.setDate(date);

            mv.set(date);
            context.write(mk,mv);

        }
    }

    private static class UVReducer extends Reducer<UVBean,Text,UVBean,NullWritable>{
        Text rk=new Text();
        Text rv=new Text();
        String previous=null;

        @Override
        protected void reduce(UVBean key, Iterable<Text> values, Context context) throws IOException, InterruptedException {


                String sessionID= UUID.randomUUID().toString();
                for(Text value:values){

                    if(previous==null){
                        previous=value.toString();
                        key.setSessionID(sessionID);
                        context.write(key,NullWritable.get());
                        continue;
                    }

                        if (MyDateUtil.dateMinusDate(value.toString(), previous) < (15 * 60) ) {

                            key.setSessionID(sessionID);
                        } else {
                            sessionID = UUID.randomUUID().toString();
                            key.setSessionID(sessionID);
                        }

                        previous = value.toString();
                        context.write(key, NullWritable.get());



                }

        }
    }

    //第一个job结束

    //（2）第二个job开始：对session进行去重

    private static class UVDistinctMapper extends Mapper<LongWritable,Text,Text,NullWritable>{
        Text mk=new Text();
        @Override
        protected void map(LongWritable key, Text value, Context context) throws IOException, InterruptedException {

            mk.set(value.toString().split("\t")[0]);
            context.write(mk,NullWritable.get());
        }
    }

    private static class UVDistinctReducer extends Reducer<Text,NullWritable,Text,NullWritable>{
        @Override
        protected void reduce(Text key, Iterable<NullWritable> values, Context context) throws IOException, InterruptedException {
            context.write(key,NullWritable.get());
        }
    }

    //第二个job结束

    //（3）第三个job开始：根据sessionID，计算出UV量
    private static class UVFinalMapper extends Mapper<LongWritable,Text,NullWritable,Text>{
        Text mk=new Text();
        @Override
        protected void map(LongWritable key, Text value, Context context) throws IOException, InterruptedException {

            mk.set(value.toString().split("\t")[0]);
            context.write(NullWritable.get(),mk);
        }
    }

    private static class UVFinalReducer extends Reducer<NullWritable,Text,Text,IntWritable>{
        private int i=0;
        private Text rk=new Text();
        private IntWritable rv=new IntWritable();
        @Override
        protected void reduce(NullWritable key, Iterable<Text> values, Context context) throws IOException, InterruptedException {
            for(Text value:values){
                i++;
            }


            UVNumber=i;

        }
    }

    //第三个job结束


    //（4）求UV的driver驱动
    public static void UVDriver(){
        //启动一个Job  创建一个job对象
        try {



            Job job1=Job.getInstance(conf);
            job1.setJarByClass(UserImg.class);
            job1.setMapperClass(UVMapper.class);
            job1.setReducerClass(UVReducer.class);

            job1.setOutputKeyClass(UVBean.class);
            job1.setOutputKeyClass(NullWritable.class);
            job1.setMapOutputKeyClass(UVBean.class);
            job1.setMapOutputValueClass(Text.class);

            //设置分组
            job1.setGroupingComparatorClass(UVGroup.class);

            Path inPath=new Path(properties.getProperty("userImg.uv.input.dir"));
            Path outPath=new Path("hdfs://192.168.2.101:9000/nginx-logs-out");
            fs.delete(outPath, true);

            FileInputFormat.addInputPath(job1, inPath);
            FileOutputFormat.setOutputPath(job1, outPath);

            //先不执行job1


            //创建job2对象
            Job job2=Job.getInstance(conf);
            job2.setJarByClass(UserImg.class);
            job2.setMapperClass(UVDistinctMapper.class);
            job2.setReducerClass(UVDistinctReducer.class);

            job2.setOutputKeyClass(Text.class);
            job2.setOutputValueClass(NullWritable.class);



            //输入路径为job2的输出目录
            Path inPath2=outPath;
            FileInputFormat.addInputPath(job2, inPath2);

            Path outPath2=new Path("hdfs://192.168.2.101:9000/nginx-logs-out/nginx-logs-out-distinct");
            fs.delete(outPath2, true);
            FileOutputFormat.setOutputPath(job2, outPath2);

            //创建job3对象
            Job job3=Job.getInstance(conf);
            job3.setJarByClass(UserImg.class);
            job3.setMapperClass(UVFinalMapper.class);
            job3.setReducerClass(UVFinalReducer.class);

            job3.setOutputKeyClass(Text.class);
            job3.setOutputKeyClass(IntWritable.class);
            job3.setMapOutputKeyClass(NullWritable.class);
            job3.setMapOutputValueClass(Text.class);

            Path inPath3=outPath2;
            FileInputFormat.addInputPath(job3, inPath3);

            Path outPath3=new Path("hdfs://192.168.2.101:9000/nginx-logs-out/nginx-logs-out-final");
            fs.delete(outPath3, true);
            FileOutputFormat.setOutputPath(job3, outPath3);


            //创建JobControl对象
            //参数：组名   会将添加到这个jc对象的所有job放在一个组中  提交一个组的job
            JobControl jc=new JobControl("myGroupName");
            //添加job之间的依赖  ControlledJob  将job转换为ControlledJob
            //job1  参数：jobConf  job1.getConfiguration()  xml
            ControlledJob cjob1=new ControlledJob(job1.getConfiguration());
            //job2
            ControlledJob cjob2=new ControlledJob(job2.getConfiguration());

            ControlledJob cjob3=new ControlledJob(job3.getConfiguration());
            //添加依赖关系
            cjob2.addDependingJob(cjob1);
            cjob3.addDependingJob(cjob2);

            jc.addJob(cjob1);
            jc.addJob(cjob2);
            jc.addJob(cjob3);


            //提交  jc   class JobControl implements Runnable
            Thread t=new Thread(jc);
            //启动
            t.start();

            //jc  判断jc组中所有的job是否执行完成
            //jc.allFinished()
            while(!jc.allFinished()){
                Thread.sleep(500);
            }
            jc.stop();

        } catch (Exception e) {

            e.printStackTrace();
        }
    }

    /////////////////////////////////////////////////////////////////////////////////////
//userName+" "+ip+" "+port+" "+" "+refer+" "+date+" "+sourceURL+" "+value+" "+appName+" "+osName+" "+osLanguage+" "+agent                         固定写法             (操作系统信息)           处理器内核信息           固定写法            浏览器名/版本      AppleWebKit的开发商
// wxp 171.83.56.146 6640  - Fri Mar 15 2019 20:48:30 GMT+0800 (中国标准时间) http://localhost:8080/index.html/foodCarShow - 4g Netscape Win32 zh-CN Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/64.0.3282.140    Safari/537.36   Edge/17.17134
//  0         1       2    3  4   5  6    7     8        9          10                  11                              12 13    14    15     16      17          18    19  20    21     22         23             24    25    26            27                 28              29
//姓名 年龄 省 城市 app名称 外链 操作系统 联网方式 关键字   时间
//                  14    3    18     13     12     4-8
    //

    //（1）用户画像：把”用户名 字段 值“进行wordCount
    private static class UserImgMapper extends Mapper<LongWritable,Text,Text,IntWritable>{

        //private List<User> userImgs=new ArrayList<>();

        @Override
        protected void setup(Context context) throws IOException, InterruptedException {
            Connection con=null;
            try {
               con=ConnectionPool.getConnection();
               PreparedStatement statement=con.prepareStatement("select * from user");
               ResultSet res=statement.executeQuery();
               ResultSetMetaData meta = res.getMetaData();

                String user=null;
                mv.set(1);
                while(res.next()) {

                    user=res.getString("userName");

                    mk.set(user+"\tAGE\t"+res.getInt("age"));
                    context.write(mk,mv);

                    mk.set(user+"\tPROVINCE\t"+res.getString("province"));
                    context.write(mk,mv);

                    mk.set(user+"\tCITY\t"+res.getString("city"));
                    context.write(mk,mv);
                }


            }catch (Exception e){
                e.printStackTrace();
            }finally {
                try{
                ConnectionPool.close(con);
                }catch(Exception e){
                    e.printStackTrace();
                }
            }
        }

        Text mk=new Text();
        IntWritable mv=new IntWritable();

        //姓名 年龄 省 城市 app名称 外链 操作系统 联网方式 关键字   时间
//                         14    12    18     13     3     4-10

        @Override
        protected void map(LongWritable key, Text value, Context context) throws IOException, InterruptedException {
            String[] fileds = value.toString().split(" ");
            String user=fileds[0];

            if(user.equals("-"))
                return;

            mv.set(1);

            StringBuffer sb=new StringBuffer();
            for(int i=4;i<=8;++i){

                sb.append(fileds[i]+" ");
            }

            String date=MyDateUtil.getDateTimeByJSDateTime(sb.toString().substring(0,sb.length()-1));
            date=date.substring(0,date.length()-6);
            mk.set(user+"\tDATE\t"+date);
            context.write(mk,mv);

            for(int i=0;i<fileds.length;++i){

                switch (i){

                    case 3:
                        mk.set(user+"\tREFER\t"+fileds[i]);
                        context.write(mk,mv);
                        break;
                    case 12:
                        mk.set(user+"\tKEYS\t"+fileds[i]);
                        if(!fileds[i].equals("-"))
                            context.write(mk,mv);
                        break;
                    case 13:
                        mk.set(user+"\tLINK_TYPE\t"+fileds[i]);
                        context.write(mk,mv);
                        break;
                    case 14:
                        mk.set(user+"\tAPP_NAME\t"+fileds[i]);
                        context.write(mk,mv);
                        break;
                    case 18:
                        mk.set(user+"\tOS_NAME\t"+fileds[i].substring(1));
                        context.write(mk,mv);
                        break;
                }

            }

        }
    }

    private static class UserImgReducer extends Reducer<Text,IntWritable,Text,IntWritable>{
        IntWritable rk=new IntWritable();

        @Override
        protected void reduce(Text key, Iterable<IntWritable> values, Context context) throws IOException, InterruptedException {
            int sum=0;
            for(IntWritable i:values){
                sum++;
            }
            rk.set(sum);
            context.write(key,rk);
        }
    }

    //（2）用户画像：求出把”用户名 字段 值“中最热的
    private static class ImgMsg implements WritableComparable<ImgMsg>{
        // 用户名  \t  字段名  \t  字段值  \t  数量
        private String msg;
        public String getMsg() {
            return msg;
        }

        public void setMsg(String msg) {
            this.msg = msg;
        }

        @Override
        public String toString() {
            return msg;
        }

        @Override
        public int compareTo(ImgMsg o) {
            String[] fs1=msg.split("\t");
            String[] fs2=o.getMsg().split("\t");
            int flag=(fs1[0]+fs1[1]).compareTo(fs2[0]+fs2[1]);
            if(flag==0){
                return Integer.parseInt(fs2[3])-Integer.parseInt(fs1[3]);
            }else if(flag<0){
                return -1;
            }else
                return 1;

        }

        @Override
        public void write(DataOutput dataOutput) throws IOException {
            dataOutput.writeUTF(msg);
        }

        @Override
        public void readFields(DataInput dataInput) throws IOException {
            msg=dataInput.readUTF();
        }
    }

    private static class UserImgGroup extends WritableComparator{

        public UserImgGroup(){
            super(ImgMsg.class,true);
        }

        @Override
        public int compare(WritableComparable a, WritableComparable b) {
            String[] o1=((ImgMsg)a).getMsg().split("\t");
            String[] o2=((ImgMsg)b).getMsg().split("\t");
            return (o1[0]+o1[1]).compareTo(o2[0]+o2[1]);
        }
    }

    private static class UserImgTop3Mapper extends Mapper<LongWritable,Text,ImgMsg,NullWritable>{
      ImgMsg mk=new ImgMsg();
        @Override
        protected void map(LongWritable key, Text value, Context context) throws IOException, InterruptedException {
            mk.setMsg(value.toString());
            context.write(mk,NullWritable.get());
        }
    }

    private static class UserImgTop3Reducer extends Reducer<ImgMsg,NullWritable,ImgMsg,NullWritable>{

        @Override
        protected void reduce(ImgMsg key, Iterable<NullWritable> values, Context context) throws IOException, InterruptedException {
            int i=0;
            for(NullWritable value:values){
                ++i;
                context.write(key,NullWritable.get());
                if(i==3)
                    break;
            }
        }
    }

   private static class UserImgToHBaseReducer extends TableReducer<ImgMsg,NullWritable,NullWritable> {

       @Override
       protected void reduce(ImgMsg key, Iterable<NullWritable> values, Context context) throws IOException, InterruptedException {
           int i=0;
           String[] fields=null;
           Put put=null;
           String fs="";
           for(NullWritable value:values){
               ++i;
               fields = key.getMsg().split("\t");
               put = new Put(fields[0].getBytes());


               fs=fs+fields[2] + " " + fields[3]+" | ";

               if(i>=3)
                   break;
           }
           put.addColumn("msg".getBytes(), fields[1].getBytes(), fs.substring(0,fs.length()-2).getBytes());
           context.write(NullWritable.get(), put);
       }
   }

    public static void userImgDriver(){

        try {
            Job job1 = Job.getInstance(conf);
            job1.setJarByClass(UserImg.class);
            job1.setMapperClass(UserImgMapper.class);
            job1.setReducerClass(UserImgReducer.class);

            job1.setOutputKeyClass(Text.class);
            job1.setOutputKeyClass(IntWritable.class);
            job1.setMapOutputKeyClass(Text.class);
            job1.setMapOutputValueClass(IntWritable.class);


            Path inPath = new Path(properties.getProperty("userImg.img.input.dir"));
            Path outPath = new Path("hdfs://192.168.2.101:9000/custom-logs-out");
            fs.delete(outPath, true);

            FileInputFormat.addInputPath(job1, inPath);
            FileOutputFormat.setOutputPath(job1, outPath);

            //创建job2对象
            Job job2=Job.getInstance(conf);
            job2.setJarByClass(UserImg.class);
            job2.setMapperClass(UserImgTop3Mapper.class);
            job2.setReducerClass(UserImgTop3Reducer.class);

            job2.setOutputKeyClass(ImgMsg.class);
            job2.setOutputValueClass(NullWritable.class);

            job2.setGroupingComparatorClass(UserImgGroup.class);

            //输入路径为job2的输出目录
            Path inPath2=outPath;
            FileInputFormat.addInputPath(job2, inPath2);

            Path outPath2=new Path("hdfs://192.168.2.101:9000/custom-logs-out/custom-logs-out-final");
            fs.delete(outPath2, true);
            FileOutputFormat.setOutputPath(job2, outPath2);

            JobControl jc=new JobControl("myGroupName");
            //添加job之间的依赖  ControlledJob  将job转换为ControlledJob
            //job1  参数：jobConf  job1.getConfiguration()  xml
            ControlledJob cjob1=new ControlledJob(job1.getConfiguration());
            //job2
            ControlledJob cjob2=new ControlledJob(job2.getConfiguration());

            //添加依赖关系
            cjob2.addDependingJob(cjob1);

            jc.addJob(cjob1);
            jc.addJob(cjob2);


            //提交  jc   class JobControl implements Runnable
            Thread t=new Thread(jc);
            //启动
            t.start();

            //jc  判断jc组中所有的job是否执行完成
            //jc.allFinished()
            while(!jc.allFinished()){
                Thread.sleep(500);
            }
            jc.stop();

        }catch(Exception e){
            e.printStackTrace();
        }
    }

    public static void userImgToHBaseDriver(){
        try {

            //整合hbase时，需要添加
            conf.set("hbase.zookeeper.quorum",properties.getProperty("zookeeper.real.address"));

            Job job1 = Job.getInstance(conf);
            job1.setJarByClass(UserImg.class);
            job1.setMapperClass(UserImgMapper.class);
            job1.setReducerClass(UserImgReducer.class);

            job1.setOutputKeyClass(Text.class);
            job1.setOutputKeyClass(IntWritable.class);
            job1.setMapOutputKeyClass(Text.class);
            job1.setMapOutputValueClass(IntWritable.class);

            Path inPath = new Path(properties.getProperty("userImg.img.input.dir"));
            Path outPath = new Path("hdfs://192.168.2.101:9000/custom-logs-out");
            fs.delete(outPath, true);

            FileInputFormat.addInputPath(job1, inPath);
            FileOutputFormat.setOutputPath(job1, outPath);

            //创建job2对象
            Job job2=Job.getInstance(conf);
            job2.setJarByClass(UserImg.class);
            job2.setMapperClass(UserImgTop3Mapper.class);
            job2.setReducerClass(UserImgToHBaseReducer.class);

            job2.setMapOutputKeyClass(ImgMsg.class);
            job2.setMapOutputValueClass(NullWritable.class);
            job2.setOutputKeyClass(NullWritable.class);
            job2.setOutputValueClass(Mutation.class);

            job2.setGroupingComparatorClass(UserImgGroup.class);

            //输入路径为job2的输出目录
            Path inPath2=outPath;
            FileInputFormat.addInputPath(job2, inPath2);

            TableMapReduceUtil.initTableReducerJob(properties.getProperty("userImg.img.hbase.output.table"), UserImgToHBaseReducer.class, job2, null, null, null, null, false);


            JobControl jc=new JobControl("myGroupName");
            //添加job之间的依赖  ControlledJob  将job转换为ControlledJob
            //job1  参数：jobConf  job1.getConfiguration()  xml
            ControlledJob cjob1=new ControlledJob(job1.getConfiguration());
            //job2
            ControlledJob cjob2=new ControlledJob(job2.getConfiguration());

            //添加依赖关系
            cjob2.addDependingJob(cjob1);

            jc.addJob(cjob1);
            jc.addJob(cjob2);


            //提交  jc   class JobControl implements Runnable
            Thread t=new Thread(jc);
            //启动
            t.start();

            //jc  判断jc组中所有的job是否执行完成
            //jc.allFinished()
            while(!jc.allFinished()){
                Thread.sleep(500);
            }
            jc.stop();

        }catch(Exception e){
            e.printStackTrace();
        }
    }

    //用户画像结束

    //用户画像的读取
    public static Map readUserImg(String userName){
        Configuration conf = HBaseConfiguration.create();
        conf.set("hbase.zookeeper.quorum",properties.getProperty("zookeeper.real.address"));
        Map map=new HashMap();
        try {
            org.apache.hadoop.hbase.client.Connection con = ConnectionFactory.createConnection(conf);
            Table userImg = con.getTable(TableName.valueOf(properties.getProperty("userImg.img.hbase.output.table")));
            Get get=new Get(userName.getBytes());
            Result res = userImg.get(get);

            List<Cell> cells = res.listCells();
            String value=null;
            String[] fs=null;
            String[] fss=null;
            String colName=null;

            for(Cell cell:cells){
                value=Bytes.toString(CellUtil.cloneValue(cell)).trim();
                //System.out.println(value);
                if(value.contains("null") || value.contains("-"))
                    continue;

                colName=Bytes.toString(CellUtil.cloneQualifier(cell));

                fs=value.split(" ");

                if(colName.equals("AGE")){

                    map.put(fs[0]+"岁",fs[1]);
                    continue;
                }else if(colName.equals("DATE")){

                    map.put(fs[0]+" "+fs[1],fs[2]);
                    continue;
                }else if(colName.equals("OS_NAME")){
                    map.put(fs[0],Integer.parseInt(fs[1])/20);
                    continue;
                }else if(colName.equals("APP_NAME")){
                    map.put(fs[0],Integer.parseInt(fs[1])/10);
                    continue;
                }

                if(value.contains(" | ")){
                    fs=value.split(" \\| ");
                    for(String f:fs){

                        fss=f.split(" ");
                        map.put(fss[0],fss[1]);
                    }
                }else{

                    map.put(fs[0],fs[1]);
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
        }

        return map;
    }

}
