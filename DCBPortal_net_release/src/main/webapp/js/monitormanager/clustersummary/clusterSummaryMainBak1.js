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
var MyChart1=new Object();
var MyChart2=new Object();
var MyChart3=new Object();
var MyChart4=new Object();

/**
 * 初始化
 */
$(document).ready(function () {
    mini.parse();
    //取得表格

    MyChart["main"] = echarts.init(document.getElementById('main'));

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
var xdate = [];
var r1Data=[];
var r2Data =[];

var r3Data = [];

var r4Data = [];

for(var k = 0 ; k< 20;k++){
    line1Data.data.push(0);
    line2Data.data.push(0);
    line3Data.data.push(0);
    line4Data.data.push(0);
    r1Data.push(0);
    r2Data.push(0);
    r3Data.push(0);
    r4Data.push(0);
    xdate.push(k);
}


var index =1;

var isFirst= true;

/**
 * 定时任务,获取
 */
function getData() {
    if(index>=10){
        index = 10;
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
            line2Data.data.shift();
            line3Data.data.shift();
            line4Data.data.shift();

            r1Data.shift();
            r2Data.shift();
            r3Data.shift();
            r4Data.shift();

            xdate.shift();
        }

        r1Data.push(result.DATA[line1Data.name]);
        r2Data.push(result.DATA[line2Data.name]);
        r3Data.push(result.DATA[line3Data.name]);
        r4Data.push(result.DATA[line4Data.name]);

        //var provNum1 = Math.ceil((result.DATA[line1Data.name] -r1Data)/3);
        //var provNum2 =  Math.ceil((result.DATA[line2Data.name] -r2Data)/3);
        //var provNum3 =  Math.ceil(result.DATA[line3Data.name] -r3Data)/3;
        //var provNum4 =  Math.ceil(result.DATA[line4Data.name] -r4Data)/3;

        var provNum1=0;
        var provNum2=0;
        var provNum3=0;
        var provNum4=0;
        var aa = parseInt(result.DATA[line2Data.name]);
        var bb = r2Data[r2Data.length-2];
        console.log(aa +" " +bb );
        for(var y = 1 ; y <= index-1;y++){
          /*  provNum1 += parseInt(r1Data[r1Data.length-y]);
            provNum2 += parseInt(r2Data[r2Data.length-y]);
            provNum3 += parseInt(r3Data[r3Data.length-y]);
            provNum4 += parseInt(r4Data[r4Data.length-y]);*/


            //provNum1 += Math.ceil((parseInt(result.DATA[line1Data.name]) -parseInt(r1Data[r1Data.length-y]))/3);
            provNum1 += Math.ceil((parseInt(r1Data[r1Data.length-y]) -parseInt(r1Data[r1Data.length-y-1]))/3);

            provNum2 += Math.ceil((parseInt(r2Data[r2Data.length-y]) -parseInt(r2Data[r2Data.length-y-1]))/3);
            provNum3 += Math.ceil((parseInt(r3Data[r3Data.length-y]) -parseInt(r3Data[r3Data.length-y-1]))/3);
            provNum4 += Math.ceil((parseInt(r4Data[r4Data.length-y]) -parseInt(r4Data[r4Data.length-y-1]))/3);

        }

        var provNum1 = Math.ceil(provNum1/index);
        var provNum2 =  Math.ceil(provNum2/index);
        var provNum3 =  Math.ceil(provNum3/index);
        var provNum4 =  Math.ceil(provNum4/index);

        /*r1Data=result.DATA[line1Data.name];
        r2Data = result.DATA[line2Data.name];
        r3Data = result.DATA[line3Data.name];
        r4Data = result.DATA[line4Data.name];*/

        if(provNum1 < 0){
            provNum1= 0;
        }
        if(provNum2 < 0){
            provNum2= 0;
        }
        if(provNum3 < 0){
            provNum3= 0;
        }
        if(provNum4 < 0){
            provNum4= 0;
        }




        line1Data.data.push(provNum1);
        line2Data.data.push(provNum2);
        line3Data.data.push(provNum3);
        line4Data.data.push(provNum4);




        index++;

        xdate.push(date);


        MyChart["main"].setOption(template());
    },"",null,false);
    // MyChart["main"].setOption(option);
}
function secondToDate(result) {
    var h = Math.floor(result / 3600) < 10 ? '0'+Math.floor(result / 3600) : Math.floor(result / 3600);
    var m = Math.floor((result / 60 % 60)) < 10 ? '0' + Math.floor((result / 60 % 60)) : Math.floor((result / 60 % 60));
    var s = Math.floor((result % 60)) < 10 ? '0' + Math.floor((result % 60)) : Math.floor((result % 60));
    return result = h + ":" + m + ":" + s;
}
function template() {

    var option = {
        title : {
            text : "TPS",
            x : "left",
            y : "top"
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
            data:[line1Data.name,line2Data.name,line3Data.name,line4Data.name]
        },

        calculable : true,

        xAxis : [
            {
                axisLabel:{
                    rotate: 70,
                    interval:0
                },
                axisLine:{
                    lineStyle :{
                        color: '#CECECE'
                    }
                },
                type : 'category',
                boundaryGap : false,
                data : xdate

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
                name:line4Data.name,
                type:'line',
                symbol:'none',
                smooth: 0.2,
                // color:['#33de08'],
                data:line4Data.data,
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
            },
            {
                name:line3Data.name,
                type:'line',
                symbol:'none',
                smooth: 0.2,
                // color:['#ecd306'],
                data:line3Data.data,
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
            },
            {
                name:line2Data.name,
                type:'line',
                symbol:'none',
                smooth: 0.2,
                // color:['#ec0000'],
                data:line2Data.data,itemStyle: {
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
            },
            {
                name:line1Data.name,
                type:'line',
                symbol:'none',
                smooth: 0.2,
                // color:['#0007ec'],
                data:line1Data.data,
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