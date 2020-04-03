/**
 * Created with IntelliJ IDEA.
 * Creater: zhongsixue
 * Date: 16-8-17
 * Time: 下午15:10
 * To change this template use File | Settings | File Templates.
 */

/**
 * 定义变量， 通常是页面控件和参数
 */
var JsVar=new Object();
var MyChart=new Object();

/**
 * 初始化
 */
$(document).ready(function () {
    mini.parse();
    //取得表格

    MyChart["main1"] = echarts.init(document.getElementById('main1'));
    MyChart["main2"] = echarts.init(document.getElementById('main2'));
    MyChart["main3"] = echarts.init(document.getElementById('main3'));
    MyChart["main4"] = echarts.init(document.getElementById('main4'));

    // 为echarts对象加载数据
    // MyChart["main"].setOption(option);
    // MyChart["main"].setOption(tp);

//循环执行，每隔1秒钟执行一次 1000
     var t1=window.setInterval(getData, 3000);
    //去掉定时器的方法
    // window.clearInterval(t1);


    getData();

});


var line1Data = new Object();
line1Data.data=[];
var line2Data = new Object();
line2Data.data=[];
var line3Data = new Object();
line3Data.data=[];

var line4Data = new Object();
line4Data.data=[];
var xdate1 = [];
var xdate2 = [];
var xdate3 = [];
var xdate4 = [];
var r1Data=[];
var r2Data =[];

var r3Data = [];

var r4Data = [];

var refreshTime= 5;

for(var k = 0 ; k< 20;k++){
    line1Data.data.push(0);
    line2Data.data.push(0);
    line3Data.data.push(0);
    line4Data.data.push(0);
    r1Data.push(0);
    r2Data.push(0);
    r3Data.push(0);
    r4Data.push(0);
    xdate1.push(k);
    xdate2.push(k);
    xdate3.push(k);
    xdate4.push(k);
}
r2Data.push(20);
r2Data.push(40);

function getTestData(){
    return r2Data[r2Data.length-1]+20;
}


var index1 =1;
var index2 =1;
var index3 =1;
var index4 =1;


var tpsIndexByzear1=1;
var tpsIndexByzear2=1;
var tpsIndexByzear3=1;
var tpsIndexByzear4=1;

var avgCount = 10;
/**
 * 定时任务,获取
 */
function getData() {
    if(index1>=avgCount){
        index1 = avgCount;
    }
    if(index2>=avgCount){
        index2 = avgCount;
    }
    if(index3>=avgCount){
        index3 = avgCount;
    }
    if(index4>=avgCount){
        index4 = avgCount;
    }
    var url = Globals.ctx+'/host/getTpsData';
    getJsonDataByPost(url, null,null, function (result) {
        var date = result.TIME;



        if (!result["IPS"] || result["IPS"].length<1) {
            return;
        }

        if (!line1Data.name) {
            line1Data.name = result.IPS[0];
            line2Data.name = result.IPS[1];
            line3Data.name = result.IPS[2];
            line4Data.name = result.IPS[3];
        }

        if (line1Data.data.length > 200) {
            line1Data.data.shift();
            r1Data.shift();
            xdate1.shift();
        }
        if (line2Data.data.length > 200) {
            line2Data.data.shift();
            r2Data.shift();
            xdate2.shift();
        }
        if (line3Data.data.length > 200) {

            line3Data.data.shift();
            r3Data.shift();
            xdate3.shift();
        }
        if (line4Data.data.length > 200) {
            line4Data.data.shift();
            r4Data.shift();
            xdate4.shift();
        }

        r1Data.push(result.DATA[line1Data.name]);
        //r2Data.push(result.DATA[line2Data.name]);
        r2Data.push(getTestData());
        r3Data.push(result.DATA[line3Data.name]);
        r4Data.push(result.DATA[line4Data.name]);

        //var provNum1 = Math.ceil((result.DATA[line1Data.name] -r1Data)/3);
        //var provNum2 =  Math.ceil((result.DATA[line2Data.name] -r2Data)/3);
        //var provNum3 =  Math.ceil(result.DATA[line3Data.name] -r3Data)/3;
        //var provNum4 =  Math.ceil(result.DATA[line4Data.name] -r4Data)/3;
        //console.log("count:"+result.DATA[line2Data.name] + ":"+r2Data[r2Data.length-2] +"," + (parseInt(r2Data[r2Data.length-1]) -parseInt(r2Data[r2Data.length-2])));
        console.log("count:"+r2Data[r2Data.length-1] + ":"+r2Data[r2Data.length-2] +"," + (parseInt(r2Data[r2Data.length-1]) -parseInt(r2Data[r2Data.length-2])));
        var provNum1=0;
        var provNum2=0;
        var provNum3=0;
        var provNum4=0;

        for(var y = 1 ; y <= index1-1;y++){

            provNum1 += Math.ceil((parseInt(r1Data[r1Data.length-y]) -parseInt(r1Data[r1Data.length-y-1]))/refreshTime);

        }
        var bbbb= 0;
        for(var y = 1 ; y <= index2-1;y++){

            provNum2 += Math.ceil((parseInt(r2Data[r2Data.length-y]) -parseInt(r2Data[r2Data.length-y-1]))/refreshTime);
            bbbb++;
        }
        for(var y = 1 ; y <= index3-1;y++){

            provNum3 += Math.ceil((parseInt(r3Data[r3Data.length-y]) -parseInt(r3Data[r3Data.length-y-1]))/refreshTime);

        }

        for(var y = 1 ; y <= index4-1;y++){

            provNum4 += Math.ceil((parseInt(r4Data[r4Data.length-y]) -parseInt(r4Data[r4Data.length-y-1]))/refreshTime);

        }

        console.log("bbbb:" + bbbb+ ",index2:" + (index2-1) +","+ provNum2);

        if(index1-1==0){
             index1=1;
            var provNum1 = Math.ceil(provNum1/(index1));
        }else{
            var provNum1 = Math.ceil(provNum1/(index1-1));
        }
        if(index2-1==0){
            index2 =1;
            var provNum2 =  Math.ceil(provNum2/(index2));
        }else{
            var provNum2 =  Math.ceil(provNum2/(index2-1));
        }


        if(index3-1==0){
            index3 =1;
            var provNum3 =  Math.ceil(provNum3/(index3));
        }else{
            var provNum3 =  Math.ceil(provNum3/(index3-1));
        }
        if(index4-1==0){
            index4 =1;
            var provNum4 =  Math.ceil(provNum4/(index4));
        }else{
            var provNum4 =  Math.ceil(provNum4/(index4-1));
        }

        /*var provNum1 = Math.ceil(provNum1/(index1-1));
        var provNum2 =  Math.ceil(provNum2/(index2-1));
        var provNum3 =  Math.ceil(provNum3/(index3-1));
        var provNum4 =  Math.ceil(provNum4/(index4-1));*/


        /*r1Data=result.DATA[line1Data.name];
        r2Data = result.DATA[line2Data.name];
        r3Data = result.DATA[line3Data.name];
        r4Data = result.DATA[line4Data.name];*/

        if(provNum1 <= 0){
            provNum1= 0;
            if(tpsIndexByzear1>=2){
                index1=2;
            }
                tpsIndexByzear1++;

        }else{
            tpsIndexByzear1=1;
        }
        if(provNum2 <= 0){
            provNum2= 0;
            if(tpsIndexByzear2>=2){
                index2=2;
            }
                tpsIndexByzear2++;

        }else{
            tpsIndexByzear2=1;
        }
        if(provNum3 <= 0){
            provNum3= 0;
            if(tpsIndexByzear3>=2){
                index3=2;
            }
                tpsIndexByzear3++;

        }else{
            tpsIndexByzear3=1;
        }
        if(provNum4 <= 0){
            provNum4= 0;
            if(tpsIndexByzear4>=2){
                index4=2;
            }
                tpsIndexByzear4++;

        }else{
            tpsIndexByzear4=1;
        }

        if(tpsIndexByzear1 <=2 || tpsIndexByzear2 <=2 || tpsIndexByzear3 <=2 || tpsIndexByzear4 <=2){
            line1Data.data.push(provNum1);
            xdate1.push(date);
            index1++;
            line2Data.data.push(provNum2);
            xdate2.push(date);
            index2++;
            line3Data.data.push(provNum3);
            xdate3.push(date);
            index3++;
            line4Data.data.push(provNum4);
            xdate4.push(date);
            index4++;
        }


       /* if(tpsIndexByzear1 <=2){
            line1Data.data.push(provNum1);
            xdate1.push(date);
            index1++;
        }
        if(tpsIndexByzear2 <=2){
            line2Data.data.push(provNum2);
            xdate2.push(date);
            index2++;
        }
        if(tpsIndexByzear3 <=2){
            line3Data.data.push(provNum3);
            xdate3.push(date);
            index3++;
        }
        if(tpsIndexByzear4 <=2){
            line4Data.data.push(provNum4);
            xdate4.push(date);
            index4++;
        }*/


        MyChart["main1"].setOption(template(line1Data.name,line1Data.data,xdate1));
        MyChart["main2"].setOption(template(line2Data.name,line2Data.data,xdate2));
        MyChart["main3"].setOption(template(line3Data.name,line3Data.data,xdate3));
        MyChart["main4"].setOption(template(line4Data.name,line4Data.data,xdate4));
    },"",null,false);

}
function secondToDate(result) {
    var h = Math.floor(result / 3600) < 10 ? '0'+Math.floor(result / 3600) : Math.floor(result / 3600);
    var m = Math.floor((result / 60 % 60)) < 10 ? '0' + Math.floor((result / 60 % 60)) : Math.floor((result / 60 % 60));
    var s = Math.floor((result % 60)) < 10 ? '0' + Math.floor((result % 60)) : Math.floor((result % 60));
    return result = h + ":" + m + ":" + s;
}
function template(serviceName,serviceData,serviceXdate) {

    var option = {
        title : {
            text : "TPS",
            x : "left",
            y : "top"
        },
        grid: {
            y2: 80
        },
        backgroundColor: '#FBFBFB',
        tooltip : {
            trigger: 'axis'
        },
        dataZoom:{
            show:true,
            start:0,
            end:100,
            realtime:true
        },

        legend: {
            data:[serviceName]
        },

        calculable : true,

        xAxis : [
            {
                axisLabel:{
                    rotate: 70,
                    interval:10
                },
                axisLine:{
                    lineStyle :{
                        color: '#CECECE'
                    }
                },
                type : 'category',
                boundaryGap : false,
                data : serviceXdate

            }
        ],
        yAxis : [
            {
                type : 'value',
                axisLine:{
                    lineStyle :{
                        color: '#CECECE'
                    }
                },
                axisLabel : {
                    formatter : '{value} tps'
                }

            }
        ],
        series : [
            {
                name:serviceName,
                type:'line',
                symbol:'none',
                smooth: 0.2,
                // color:['#33de08'],
                data:serviceData,
                itemStyle: {
                    emphasis: {
                        shadowBlur: 10,
                        shadowOffsetX: 0,
                        shadowColor: 'rgba(0, 0, 0, 0.5)'
                    },
                    normal:{
                        label:{
                            show: true,
                            formatter: '{b}({d}tps)'
                        },
                        labelLine :{show:true}
                    }
                }
            }
        ]
    };

    return option;

}