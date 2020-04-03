package com.wxp.inend.util;


import com.alibaba.fastjson.JSONObject;
import com.wxp.inend.entity.Food;
import com.wxp.inend.entity.FoodAllField;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.update.UpdateResponse;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.transport.TransportAddress;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.rest.RestStatus;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.transport.client.PreBuiltTransportClient;

import java.io.InputStream;
import java.lang.reflect.Field;
import java.net.InetAddress;
import java.sql.*;
import java.util.*;

public class ESUtil {

    public static Connection jdbc() throws Exception{
        InputStream in = ESUtil.class.getClassLoader().getResourceAsStream("windowsOrLinux.properties");
        Properties p=new Properties();
        p.load(in);

        Class.forName(p.getProperty("mysql.driver"));

        return DriverManager.getConnection(p.getProperty("mysql.url"),p.getProperty("mysql.username"),p.getProperty("mysql.password"));

    }

    //获得food表中的所有数据
    public static List<FoodAllField> getFoodMag(){

        Connection con=null;
        List<FoodAllField> foods=new ArrayList<FoodAllField>();
        try {
            con = jdbc();
            PreparedStatement pstat= con.prepareStatement("select * from food");

            ResultSet res=pstat.executeQuery();
            ResultSetMetaData md=res.getMetaData();



            Class food=Class.forName("com.wxp.inend.entity.FoodAllField");

            FoodAllField fa=null;
            Field filed=null;
            while(res.next()) {
                fa = new FoodAllField();
                for (int i = 1; i <= md.getColumnCount();++i){
                    filed=food.getDeclaredField(md.getColumnName(i));
                    filed.setAccessible(true);

                    if(md.getColumnClassName(i).equals("java.lang.String") || md.getColumnClassName(i).equals("java.sql.Date"))
                        filed.set(fa,res.getString(i));
                    else if(md.getColumnClassName(i).equals("java.math.BigDecimal"))
                        filed.set(fa,res.getFloat(i));
                    else if(md.getColumnClassName(i).equals("java.lang.Integer"))
                        filed.set(fa,res.getInt(i));
                }
                foods.add(fa);
            }

            res.close();

        }catch(Exception e){
            e.printStackTrace();
        }finally {
            return foods;
        }
    }

    //连接es服务器

    public static PreBuiltTransportClient getEsClient(){

        /*为了解决java.lang.IllegalStateException: availableProcessors is already set to [4], rejecting [4]*/
        System.setProperty("es.set.netty.runtime.available.processors", "false");
        Settings esCluster = Settings.builder()
                .put("cluster.name", "esCluster")
                .build();
        PreBuiltTransportClient client = new PreBuiltTransportClient(esCluster);

        //节点
        try {
            TransportAddress ta1 = new TransportAddress(InetAddress.getByName("192.168.2.101"), 9300);
            TransportAddress ta2 = new TransportAddress(InetAddress.getByName("192.168.2.102"), 9300);
            TransportAddress ta3 = new TransportAddress(InetAddress.getByName("192.168.2.103"), 9300);

            client.addTransportAddress(ta1);
            client.addTransportAddress(ta2);
            client.addTransportAddress(ta3);

        }catch (Exception e){
            e.printStackTrace();
        }

        return client;

    }

    //查询es中的所有数据（默认有分页）
    public static void selectAll(){
        PreBuiltTransportClient client = getEsClient();
        SearchResponse response = client.prepareSearch(new String[]{"db"}).get();
        SearchHits hits = response.getHits();
        Iterator<SearchHit> iterator = hits.iterator();

        while(iterator.hasNext()){
            SearchHit hit = iterator.next();
            System.out.println(hit.getSourceAsString());
        }

    }

    //从mysql -> es的数据转移
    public static void inserFoodtIntoEsTable() throws Exception{
        PreBuiltTransportClient client = getEsClient();

        List<FoodAllField> foods = getFoodMag();

        JSONObject row=new JSONObject();
        Class foodClass=FoodAllField.class;
        Field[] fields = foodClass.getDeclaredFields();
        fields[0].setAccessible(true);

        for(FoodAllField food:foods){

            for(int i=1;i<fields.length;++i){
                fields[i].setAccessible(true);
                row.put(fields[i].getName(),fields[i].get(food));
            }

            client.prepareIndex("foods","food",fields[0].get(food)+"").setSource(row).get();

        }

        System.out.println("从mysql -> es的数据转移完成" );

  /*      //第一步：创建“一行文档的数据”
        JSONObject row=new JSONObject();
        row.put("name","cff");
        row.put("age","22");

        //第二步：把数据添加到“es表中”
        client.prepareIndex("db","table2","4").setSource(row).get();*/
    }

    //通过“食物名称”获得“一个food”
    public static JSONObject getFoodByFoodName(String foodName){
        PreBuiltTransportClient client = getEsClient();

        SearchResponse response = client.prepareSearch(new String[]{"foods"}).setQuery(QueryBuilders.matchQuery("foodName",foodName)).get();
        Iterator<SearchHit> iterator = response.getHits().iterator();

        SearchHit hit=iterator.hasNext()?iterator.next():null;

        JSONObject parse = (JSONObject)JSONObject.parse(hit.getSourceAsString());

        parse.put("Id",hit.getId());

        return parse;
    }

    //es的数据更新
    public static void updateNumber(String foodName,int number){
        JSONObject json=new JSONObject();
        JSONObject foodMsg=getFoodByFoodName(foodName);
        json.put("margin",(Integer)foodMsg.get("margin")-number);
        json.put("saleNumber",(Integer)foodMsg.get("saleNumber")+number);

        PreBuiltTransportClient client = getEsClient();
        UpdateResponse res = client.prepareUpdate("foods", "food", foodMsg.get("Id") + "").setDoc(json).get();

    }

    //根据“字段名 - 值”获得“food表查询结果”
    public static List<FoodAllField> getFoodByCol(String col,String value){
        PreBuiltTransportClient client = getEsClient();

        SearchResponse response = client.prepareSearch(new String[]{"foods"}).setTypes("food").setQuery(QueryBuilders.matchPhrasePrefixQuery(col,value)).setFrom(0).setSize(5000).get();

        SearchHits hits = response.getHits();   //获得所有行
        JSONObject json=null;
        FoodAllField food=null;

        List<FoodAllField> foods=new ArrayList<>();

        for(SearchHit hit:hits){    //遍历每一行
               String s_json = hit.getSourceAsString();     //获得一行的“json字符串”
               json = (JSONObject)JSONObject.parse(s_json);            //解析为键值对对象

                    food=new FoodAllField(Integer.parseInt(hit.getId()),(String)json.get("foodName"),(String)json.get("brith"),(String)json.get("bzq"),Float.parseFloat(json.get("price").toString()),Float.parseFloat(json.get("oldPrice").toString()),(Integer) json.get("margin"),(Integer) json.get("saleNumber"),(String)json.get("type"),(String)json.get("comment"),(Integer) json.get("goodResponse"),(String)json.get("img"));
            foods.add(food);
        }
        return foods;
    }

    //根据“食品名称、或类型、或备注”进行查询
    public static Set<FoodAllField> findFoodByNameTypeComment(String value){

        List<FoodAllField> foodName = getFoodByCol("foodName", value);
        List<FoodAllField> type = getFoodByCol("type", value);
        List<FoodAllField> comment = getFoodByCol("comment", value);

        foodName.addAll(type);
        foodName.addAll(comment);

        return new HashSet<FoodAllField>(foodName);

    }

    public static void main(String[] args){
        Scanner sc=new Scanner(System.in);
        int n=sc.nextInt();
        if(!(n>2&&n<10000))
            return ;

        String balls1=sc.next();
        String[] balls2=balls1.split("");

        if(balls2.length!=n)
            return ;


        int[] balls=new int[balls2.length];
        for(int i=0;i<balls2.length;++i){
            balls[i]=Integer.parseInt(balls2[i]);
        }

        int max=0;
        for(int e:balls){
            if(e>max)
                max=e;
        }

        int[] zl=new int[max+1];
        for(int i=0;i<balls.length;++i){
            zl[balls[i]]++;
        }
        int res=0;
        int buf=0;
        boolean flag=true;
        for(int i=0;i<zl.length;++i){
            if(zl[i]!=0&&flag) {
                buf = zl[i];
                flag=false;
            }
            if(zl[i]!=0&&zl[i]==buf)
            {    res++;
                buf=zl[i];
            }
            if(zl[i]!=0&&zl[i]!=buf)
            {res=0;
                buf=zl[i];
                break;
            }

        }
        System.out.println(Arrays.toString(zl));

        System.out.println(res);
    }


}
