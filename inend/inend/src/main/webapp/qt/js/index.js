$(function(){

	//“图片滚动函数”调用
	imgSrcoll();

    //“页面刷新时”，判断是否有用户登陆
    userOnFresh()

	//首页展示
    foodPageShow(1,"hi")
    $("#ul3 li:eq(1),#headOfShow li:eq(0)").on("click",function () {
        foodPageShow(1,"point");
    })

    //上一页
    $("#footOfShow li").eq(0).on("click",function(){foodPageShow(1,"down")});

    //下一页
    $("#footOfShow li").eq(2).on("click",function(){foodPageShow(1,"up")});

    //指定页
    $("#pageNow input").on("change",function(){
        var pageNow=$("#pageNow input").val();
        if(pageNow=="")
            pageNow=1;
        foodPageShow(pageNow,"point")
    })

    //食物在左侧分类展示
    foodShowByType("主食",0);
    foodShowByType("风味小炒",1);
    foodShowByType("汤类",2);
    foodShowByType("凉菜",3);

    //对面商品的“购物面板”展示
    $("#bodyOfShow").on("click","[align='right']",function(e){
        //foodShowInBuy($(e.target))
        foodShowInBuy($(e.target).parent().parent().children().eq(2).text().trim()   )

    })

    $(".ul4").on("click","li",function(e){
        foodShowInBuy($(e.target).text())
    });

    //登陆页面展示、退出
    $("#ul2 li").eq(1).on("click",function () {
        $("#loginWindow").css("display","block");
        $("#userButton").val("登陆")
    })

    $("#exitLogin").on("click",function () {
        $("#loginWindow").css("display","none");
    })

    //登陆页面切换
    $("#loginChange").on("click",function () {
        if($("#userName").css("display")=='none'){
            $("#userName,#userPwd,#userButton").css("display","block");
            $("#adminName,#adminPwd,#adminButton").css("display","none");
        }else{
            $("#userName,#userPwd,#userButton").css("display","none");
            $("#adminName,#adminPwd,#adminButton").css("display","block");
        }
    })

    //“普通用户、管理员”登陆
    $("#userButton").on("click",function(){userLogin(); })
    $("#adminButton").on("click",function(){adminLogin(); })

    //添加商品到“购物车”
    $("#buyFood tr:last-child td:last-child").on("click",function(){addFoodToCar() })

    //查看“购物车”
    $("#ul3 li:nth-child(1)").on("click",function () {
        pageNow=1;
        foodCarShow();
    })

    //购物车的“上下页”
    $("#footOfCar li:nth-child(1)").on("click",function () {
        pageNow=1;
        foodCarShow();
    })
    $("#footOfCar li:nth-child(2)").on("click",function () {
        pageNow=pageNow-1;
        foodCarShow();
    })
    $("#footOfCar li:nth-child(3)").on("click",function () {
        pageNow=pageNow+1;
        foodCarShow();
    })
    $("#footOfCar li:nth-child(4)").on("click",function () {
        pageNow=pageCount;
        foodCarShow();
    })

    //退出购物车
    $("#exitCar").on("click",function () {
            $("#buyCarWindow").css("display","none")
    })

    //注销
    $("#afterLogin div:last-child").on("click",function () {
        userCancel()
    })

    //“购物车”的“记录删除”
    $("#buyCarWindow").on("click","tr td:last-child",function (e) {

        //一个选择符指向多个标签时，为该“选择符绑定时间”时，会由于多个指定无法绑定？所以只能绑定它可以唯一确定的父元素？
        deleteFoodInCar($(e.target).parent().children().eq(1).text())
    })

    //购物车的提交
    $("#buyCarWindow").on("click","tr:first-child button",function () {

        submitCar();
    })

    //“中部分类展示”
    $("#elemOfLeft").on("click","div",function (e) {
        foodShowOfTypeInMid($(e.target).text().trim())
    })
    $("#elemOfLeft").on("click","li li:nth-child(10)",function (e) {
        foodShowOfTypeInMid($(e.target).parent().parent().children().eq(0).text().trim())
    })

    //查找
    $("#midOfHead input:nth-child(2)").on("click",function () {

        findInEs();

    })

    //“热搜数据”的加载
    hotSearch();

    $("#midOfHead form input[type='text']").on("focus",function () {
        hotSearch();
    });

    userActionMsg("",null)


})
////////

//注销
function userCancel(){
var cancelButton=$('#afterLogin :last-child');
    $.ajax({
        url:"userCancel",
        type:"GET",
        beforeSend:function(){
            cancelButton.text("注销中...");
        },
        success:function () {
            cancelButton.text("注销")
            $("#beforeLogin").css("display", "block");
            $("#afterLogin").css("display", "none")
        },
        error:function(){
            cancelButton.text("注销")
        },
        timeout:10000
    })
}

//获取用户行为信息
var ip=returnCitySN["cip"]
var refer=document.referrer
refer=refer==""?"-":refer;
var date=new Date()
var sourceURL=document.URL
var appName=navigator.appName
var osName=navigator.platform
var osLanguage=navigator.language
var agent=navigator.userAgent
var linkType=navigator.connection.type

function userActionMsg(source,value) {

    // var map={"ip":ip,"refer":refer,"date":date,"sourceURL":sourceURL,"appName":appName,"osName":osName,"osLanguage":osLanguage,"agent":agent}

    source=source==""?"":"/"+source;
    value=value==null?"-":value;

    $.ajax({
        url:"userActionMsg",
        type:"POST",
        data:{
            ip:ip,
            refer:refer,
            date:date,
            sourceURL:sourceURL+source,
            value:value,
            linkType:linkType,
            appName:appName,
            osName:osName,
            osLanguage:osLanguage,
            agent:agent

        },
        timeout:10000
    })

}

//热搜
function hotSearch(){

    $.ajax({
        url:"hotSearch",
        type:"POST",
        success:function (data) {
            $("#hotList ul").html("<div>热搜</div>")

            $.each(data.hotList,function(index,e){
                var redu="热度："
                if(e.number==0) {
                    redu=""
                    e.number = ""
                }
                if(index<10)
                    $("#hotList ul").append("<li><span>"+(index+1)+"</span><div>"+e.foodName+"</div><span>"+redu+e.number+"</span></li>");
            })
        }
    })
}

//查找
function findInEs(){

    //获得用户感兴趣的关键字
    var value=$("#midOfHead input:first-child").val()
    userActionMsg("findInEs",value)

    $.ajax({
        url:"findInEs",
        type:"GET",
        data:{
            value:value
        },
        beforeSend:function(){
            $("#footOfShow").css("display","none");
            $("#bodyOfShow").html("<center>正在加载中...</center>");
        },
        error:function(){
            $("#bodyOfShow").html("<center>加载失败...</center>");
        },
        success:function (data) {
            //隐藏页码、购物板
            $("#footOfShow").css("display","none");
            $("#buyFood").css("display","none");
            $("body").css("background-color","rgba(200,200,200,0.3)")
            //商品页面展示
            $("#bodyOfShow").css("display","block");
            $("#bodyOfShow").html("");

            $.each(data.esFoods,function(index,e){

                var goodPrencent=e.goodResponse/e.saleNumber*100;
                if(e.saleNumber==0)
                    goodPrencent=100;
                $("#bodyOfShow").append("<li>\n" +
                    "<table border=\"0px\" cellspacing=\"0\">\n" +
                    "<tr>\n" +
                    "<td colspan=\"2\"><img src=\"qt/foodImg/"+e.img+"\" /></td>\n" +
                    "</tr>\n" +
                    "<tr class=\"shodow\">\n" +
                    "<td colspan=\"2\"></td>\n" +
                    "</tr>\n" +
                    "<tr class=\"comment\">\n" +
                    "<td colspan=\"2\">"+e.foodName+"</td>\n" +
                    "</tr>\n" +
                    "<tr class=\"saleMsg\">\n" +
                    "<td>月售"+e.saleNumber+"份</td>\n" +
                    "<td>好评率"+goodPrencent+"%</td>\n" +
                    "</tr>\n" +
                    "<tr class=\"price\">\n" +
                    "<td>￥"+e.price+"</td>\n" +
                    "<td align=\"right\"></td>\n" +
                    "</tr>\n" +
                    "</table>\n" +
                    "</li>\n")

            })
        }
    })

}

//“中部分类展示”
function foodShowOfTypeInMid(type) {

    //获得用户感兴趣的关键字
    userActionMsg("foodShowOfTypeInMid",type)

    $.ajax({
        url:"foodShowOfTypeInMid",
        type:"POST",
        data:{
            type:type
        },
        success:function (data) {

            //隐藏页码、购物板
            $("#footOfShow").css("display","none");
            $("#buyFood").css("display","none");
            $("body").css("background-color","rgba(200,200,200,0.3)")
            //商品页面展示
            $("#bodyOfShow").css("display","block");
            $("#bodyOfShow").html("");

            $.each(data.partFoods,function(index,e){

                var goodPrencent=e.goodResponse/e.saleNumber*100;
                if(e.saleNumber==0)
                    goodPrencent=100;
                $("#bodyOfShow").append("<li>\n" +
                    "<table border=\"0px\" cellspacing=\"0\">\n" +
                    "<tr>\n" +
                    "<td colspan=\"2\"><img src=\"qt/foodImg/"+e.img+"\" /></td>\n" +
                    "</tr>\n" +
                    "<tr class=\"shodow\">\n" +
                    "<td colspan=\"2\"></td>\n" +
                    "</tr>\n" +
                    "<tr class=\"comment\">\n" +
                    "<td colspan=\"2\">"+e.foodName+"</td>\n" +
                    "</tr>\n" +
                    "<tr class=\"saleMsg\">\n" +
                    "<td>月售"+e.saleNumber+"份</td>\n" +
                    "<td>好评率"+goodPrencent+"%</td>\n" +
                    "</tr>\n" +
                    "<tr class=\"price\">\n" +
                    "<td>￥"+e.price+"</td>\n" +
                    "<td align=\"right\"></td>\n" +
                    "</tr>\n" +
                    "</table>\n" +
                    "</li>\n")

            })
        },
        timeout:10000
    })
    
}

//购物车的提交
function submitCar(){
    //获得用户访问的具体资源
    userActionMsg("submitCar",null)

    $.ajax({
        url:"submitCar",
        type:"POST",
        beforeSend:function(){
            $("#buyCarWindow table tr:first-child td button").text("提交中...")
        },
        success:function () {
            $("#buyCarWindow table tr:first-child td button").text("提交购物车")
            clearCarTable()
            $("#buyCarWindow table").append("购物车已提交！")
        },
        error:function(){

            $("#buyCarWindow table tr:first-child td button").text("提交购物车")
            $("#buyCarWindow table").append("提交失败！")
        },
        timeout:10000
    })
}

//“购物车”的“记录删除”
function deleteFoodInCar(foodName){
    //获得用户访问的具体资源
    userActionMsg("deleteFoodInCar",null)

    $.ajax({
        url:"deleteFoodInCar",
        type:"POST",
        data:{
            foodName:foodName
        },
        success:function () {
            foodCarShow()
        },
        timeout:1000
    })
}

//查看“购物车”
var pageNow=1;
var pageCount=1;
function foodCarShow() {
    //获得用户访问的具体资源
    userActionMsg("foodCarShow",null)

    $.ajax({
        url:"foodCarShow",
        type:"POST",
        data:{
            pageNow:pageNow
        },
        success:function (data) {

            if(data.noLogin==1) {   //是否有用户登陆
                $("#buyCarWindow").css("display", "block")


                if (data.empty == 1) {  //是否为空

                    pageCount=data.pageCount;

                    //恢复原状
                    clearCarTable()

                    $.each(data.car, function (index, e) {

                        $("#buyCarWindow table").append("<tr>" +
                            "<td>" + (index + 1) + "</td><td>" + e.foodName + "</td><td>" + e.price + "</td><td>" + e.number + "</td>" +
                            "<td>" + e.totalPrice + "</td><td>" + e.type + "</td><td>删除</td>" +
                            "</tr>" +
                            "")
                    })
                } else {
                    clearCarTable()
                    $("#buyCarWindow table").append("购物车为空！")
                }
            }else{
                $("#loginWindow").css("display","block");
            }
        },
        timeout:10000
    })
}

//购物车恢复“空表”的状态
function clearCarTable(){

    //恢复原状
    $("#buyCarWindow table").html("<tr>\n" +
        "<td colspan=\"7\">\n" +
        "<div>购物车</div>\n" +
        "<button>提交购物车</button>\n" +
        "</td>\n" +
        "</tr>\n" +
        "<tr>\n" +
        "<th>序号</th><th>菜名</th><th>单价</th><th>数量</th>\n" +
        "<th>价格</th><th>种类</th><th>删除</th>\n" +
        "</tr>\n")
}


//添加商品到“购物车”
function addFoodToCar() {

    //获得用户感兴趣的关键字
    var value=$("#buyFoodName").text()
    userActionMsg("addFoodToCar",value)

    var s_price=$("#buyPrice").text().toString();
    var price=s_price.substr(1,s_price.length-1)


    $.ajax({
        url:"addFoodToCar",
        type:"POST",
        data:{
            foodName:$("#buyFoodName").text(),
            price:price,
            number:$("#buyNumber input").val()
        },
        success:function (data) {

            if(data.noLogin==1) {
                if (data.noMargin == 0) {
                    var content= $("#buyPrice").text();
                    $("#buyPrice").append("<span style='color:orange;font-size:1.3rem;font-weight:bold;float:right;'>库存不足,剩" + data.margin + "个!</span>")
                    setTimeout(function () {
                        $("#buyPrice").html(content)

                    }, 2000)

                } else {

                $("#buyFood tr:last-child td:last-child").html("<span style='color:orange;font-size:1.3rem;font-weight:bold;'>+" + $("#buyNumber input").val() + "</span>")
                setTimeout(function () {
                    $("#buyFood tr:last-child td:last-child").html("")

                }, 1000)
                }
            }else{

                    $("#loginWindow").css("display","block");
            }



        },
        timeout:10000
    })

}

//“页面刷新时”，判断是否有用户登陆
function userOnFresh(){
    $.ajax({
        url:"userOnFresh",
        type:"POST",
        success:function (data) {
            if(data.flag==1) {

                $("#loginWindow").css("display","none");

                $("#beforeLogin").css("display", "none");
                $("#afterLogin").css("display", "block")
                $("#afterLogin img").prop("src", "qt/favicon.ico");
                $("#afterLogin div:nth-child(2)").text(data.user.userName)
            }
        },
        timeout:10000
    })

}

//“普通用户、管理员”登陆
function userLogin() {


        $.ajax({
            url:"userLogin",
            type:"POST",
            data:{
                userName:$("#userName input").val(),
                userPwd:$("#userPwd input").val()
            },
            success:function (data) {
               if(data.flag==1) {

                   //登陆成功动画
                   var i=3;
                   $("#userButton").val("登陆成功,"+i+"秒之后进行跳转")

                   var clear=setInterval(function () {

                       i--;

                       if(i==0) {
                           clearInterval(clear)


                           //显示头像
                           $("#loginWindow").css("display","none");

                           $("#beforeLogin").css("display", "none");
                           $("#afterLogin").css("display", "block")
                           $("#afterLogin img").prop("src", "qt/favicon.ico");
                           $("#afterLogin div:nth-child(2)").text(data.user.userName)


                       }
                       $("#userButton").val("登陆成功,"+i+"秒之后进行跳转")


                   },1000)


                }else{
                    alert("登陆失败");
                }
            },
            timeout:10000
        })



}

//"管理员”登陆
function adminLogin() {
    $.ajax({
        url:"adminLogin",
        type:"POST",
        data:{
            adminName:$("#adminName input").val(),
            adminPwd:$("#adminPwd input").val()
        },
        success:function (data) {
            if(data.flag==1){
                location.href="admin.html";
            }else{
                alert("用户名或密码错误")
            }
        },
        timeout:10000
    })
}

//食物在“购物面板”展示
function foodShowInBuy(foodName2) {
    //获得用户感兴趣的关键字
    var value=foodName2
    userActionMsg("getFoodByFoodName",value)

    $.ajax({
        url:"getFoodByFoodName",
        type:"POST",
        data:{foodName:foodName2},
        success:function (data) {
            //商品展示 - 购买界面 的切换
            $("#bodyOfShow").css("display","none");
            $("#footOfShow").css("display","none");
            $("#buyFood").css("display","block");
            $("body").css("background-color","rgba(0,0,0,0.2)")

            var food = data.food;
            $("#buyFoodName").text(food.foodName);
            $("#buyImg img").prop("src","qt/foodImg/"+food.img)
            $("#comment").text(food.comment);
            $("#buySaleNumber").text("月售"+food.saleNumber+"份");
            var goodPrecent=parseFloat(food.goodResponse)/parseFloat(food.saleNumber)
            if(parseFloat(food.saleNumber)==0.0)
                goodPrecent=100;
            $("#buyGoodResponse").text("好评率"+goodPrecent+"%");
            $("#buyPrice").text("￥"+food.price);

        },
        timeout:10000
    })
}

//食物在左侧分类展示
function foodShowByType(type1,index){
    $.ajax({
        url:"foodShowByType",
        type:"POST",
        data:{
            type:type1
        },
        beforeSend:function () {
            $(".ul4").eq(index).html("<center>正在加载中...</center>")
        },
        error:function(){
            $(".ul4").eq(index).html("<center>加载失败...</center>")
        },
        success:function (data) {
            $(".ul4").eq(index).html("")
            $.each(data.partFoods,function(i,e){

                if(i<=8) {
                    $(".ul4").eq(index).append("<li>" + e + "</li>");
                }else if(i==9){
                    $(".ul4").eq(index).append("<li>全部</li>");
                }
            })
        },
        timeout:10000
    })
}

//食物分页展示
function foodPageShow(pageNow,h) {
	$.ajax({
		url:"foodPageShow",
		type:"POST",
		data:{
		    pageNow2:pageNow,
            handle:h
        },
        beforeSend:function(){
            $("#footOfShow").css("display","none");
            $("#bodyOfShow").html("<center>正在加载中...</center>");
        },
        error:function(){
            $("#bodyOfShow").html("<center>加载失败...</center>");
        },
		success:function (data) {
		    //分页展示
            $("#footOfShow").css("display","block");
            $("#footOfShow li input").val(data.pageNow)
            $("#footOfShow li span").text(data.pageCount)

            //隐藏购物面板
            $("#buyFood").css("display","none");
            $("body").css("background-color","rgba(200,200,200,0.3)")

            //商品页面展示
            $("#bodyOfShow").css("display","block");
            $("#bodyOfShow").html("");

            $.each(data.foods,function(index,e){

                var goodPrencent=e.goodResponse/e.saleNumber*100;
                if(e.saleNumber==0)
                    goodPrencent=100;
                $("#bodyOfShow").append("<li>\n" +
                    "<table border=\"0px\" cellspacing=\"0\">\n" +
                    "<tr>\n" +
                    "<td colspan=\"2\"><img src=\"qt/foodImg/"+e.img+"\" /></td>\n" +
                    "</tr>\n" +
                    "<tr class=\"shodow\">\n" +
                    "<td colspan=\"2\"></td>\n" +
                    "</tr>\n" +
                    "<tr class=\"comment\">\n" +
                    "<td colspan=\"2\">"+e.foodName+"</td>\n" +
                    "</tr>\n" +
                    "<tr class=\"saleMsg\">\n" +
                    "<td>月售"+e.saleNumber+"份</td>\n" +
                    "<td>好评率"+goodPrencent+"%</td>\n" +
                    "</tr>\n" +
                    "<tr class=\"price\">\n" +
                    "<td>￥"+e.price+"</td>\n" +
                    "<td align=\"right\"></td>\n" +
                    "</tr>\n" +
                    "</table>\n" +
                    "</li>\n")

            })
        },
        timeout:10000
	})
}

//图片滚动函数
function imgSrcoll(){
    setInterval(function(){


        var img1Src=$("#img1").prop("src");

        $("#img1").animate({height:"0px",width:"0px",top:"100px",opacity:"0"},1000,function(){


            $("#img1").prop("src",$("#img2").prop("src"));
            $("#img1").css({"height":"150px","width":"50%","top":"25px","opacity":"1"});

        });

        $("#img2").animate({height:"150px",top:"25px",left:"0px"},1000,function(){

            $("#img2").prop("src",$("#img3").prop("src"));

            $("#img2").css({"height":"200px","top":"0px","left":"25%","z-index":"999"});

        });


        $("#img3").css("z-index","999");
        $("#img3").animate({height:"200px",top:"0px",left:"25%"},1000,function(){


            $("#img3").prop("src",img1Src);

            $("#img3").css({"height":"150px","left":"50%","width":"50%","top":"25px","z-index":"1"});
        });

        $("#img4").prop("src",img1Src);

        $("#img4").animate({height:"150px",width:"50%",top:"25px",left:"50%",zIndex:"1",opacity:"1"},1000,function(){


            $("#img4").css({"height":"0px","left":"100%","width":"0px","top":"100px","z-index":"1","opacity":"0"});
        });


    },6000);

}