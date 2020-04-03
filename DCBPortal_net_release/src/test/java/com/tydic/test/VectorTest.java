package com.tydic.test;

import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.assertj.core.util.Maps;

import java.util.Collections;
import java.util.Comparator;
import java.util.Map;
import java.util.Vector;

public class VectorTest
{
    public static void main(String[] args) 
    {
//        Stack v = new Stack();
//        //依次将三个元素push入"栈"
//        v.push("疯狂Java讲义");
//        v.push("轻量级Java EE企业应用实战");
//        v.push("疯狂Android讲义");
//
//        //输出：[疯狂Java讲义, 轻量级Java EE企业应用实战 , 疯狂Android讲义]
//        System.out.println(v + " , --->"  + v.firstElement());
//
//        //访问第一个元素，但并不将其pop出"栈"，输出：疯狂Android讲义
//        System.out.println(v.peek());
//
//        //依然输出：[疯狂Java讲义, 轻量级Java EE企业应用实战 , 疯狂Android讲义]
//        System.out.println(v);
//
//        //pop出第一个元素，输出：疯狂Android讲义
//        System.out.println(v.pop());
//
//        //输出：[疯狂Java讲义, 轻量级Java EE企业应用实战]
//        System.out.println(v);

        Vector<Map<String, Object>> list = new Vector<>();
        Map<String, Object> maps = Maps.newHashMap();
        maps.put("fileName", "AAAAA");
        maps.put("fileTime", "2019-01-01 12:12:18");
        list.add(maps);

        Map<String, Object> maps2 = Maps.newHashMap();
        maps2.put("fileName", "BBBB");
        maps2.put("fileTime", "2017-01-01 12:12:18");
        list.add(maps2);

        Map<String, Object> maps3 = Maps.newHashMap();
        maps3.put("fileName", "CCCC");
        maps3.put("fileTime", "2015-01-01 12:12:18");
        list.add(maps3);

        System.out.println("before--->" + list.toString());
        Vector<Map<String, Object>> collList = new Vector<Map<String, Object>>();
        for (int i = 0; i < list.size(); i++) {
            Map<String, Object> transItem = list.get(i);
            int j = 0;
            for (; j < collList.size(); j++) {
                Map<String, Object> item = collList.get(j);
                if (ObjectUtils.toString(transItem.get("fileName")).compareTo(ObjectUtils.toString(item.get("fileName")))  < 0) {
                    break;
                }
            }
            collList.add(j, transItem);
        }
        //System.out.println(collList);


        System.out.println("开始时间---");
        String field = "fileTime";
        String sortType = "";
        Collections.sort(list, new Comparator<Map<String, Object>>() {
            private String field = "fileName";
            private String sortType = "asc";
            public Comparator setComparator(String field, String sortType) {
                this.field = field;
                this.sortType = sortType;
                return this;
            }
            @Override
            public int compare(Map<String, Object> o1, Map<String, Object> o2) {
                if (StringUtils.equalsIgnoreCase(sortType, "ASC")) {
                    return ObjectUtils.toString(o1.get(field)).compareTo(ObjectUtils.toString(o2.get(field)));
                } else {
                    return ObjectUtils.toString(o2.get(field)).compareTo(ObjectUtils.toString(o1.get(field)));
                }
            }
        }.setComparator(field, sortType));
        System.out.println("list3 --->" + list.toString());

    }
}