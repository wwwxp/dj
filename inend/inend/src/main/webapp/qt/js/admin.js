$(function(){

    //商品表的获取
    getFoodTable(null)

    $("#footOfGoodsShow li:nth-child(1)").on("click",function () {
        getFoodTable("down")

    })

    $("#footOfGoodsShow li:nth-child(3)").on("click",function () {
        getFoodTable("up")

    })

    //管理员对商品的修改
    $("#goodsShow table").on("click","tr td:last-child",function(e){

        var foodName=$(e.target).parent().children().children().eq(0).val()
        var price=$(e.target).parent().children().children().eq(1).val()
        var oldPrice=$(e.target).parent().children().children().eq(2).val()
        var margin=$(e.target).parent().children().children().eq(3).val()
        var type=$(e.target).parent().children().children().eq(4).val()
        var img=$(e.target).parent().children().children().eq(5).val()
        adminUpdateGood(foodName,price,oldPrice,margin,type,img)
    })

    //功能切换
    //切为求UV界面
    $("#bottom li:nth-child(3)").on("click",function () {
        $("#goodsShow").css("display","none")
        $("#userImg").css("display","none")
        $("#getUV").css("display","block")
        $("#content").text("UV量");

    })

    //切为商品表界面
    $("#bottom li:nth-child(1)").on("click",function () {
        $("#goodsShow").css("display","block")
        $("#userImg").css("display","none")
        $("#getUV").css("display","none")
        $("#content").text("商品表");
    })

    //切为用户画像界面
    $("#bottom li:nth-child(2)").on("click",function () {
        $("#goodsShow").css("display","none")
        $("#userImg").css("display","block")
        $("#getUV").css("display","none")
        $("#content").text("用户画像");
    })


//生成UV
    $("#getUV button").on("click",function () {
        getUV()
    })

    //加载出最新的”用户画像“倒HBase中
        $("#newUserImg").on("click",function () {
            newUserImg()
        })

    //获取特定的用户画像
    $("#userImg div:first-child button").on("click",function () {
        readUserImg()
    })

    //页面刷新时，获得管理员信息
    getAdminName()
})

function getAdminName() {
    $.ajax({
        url:"getAdminName",
        type:"GET",
        success:function (data) {
            if(data.adminName==null){
                $("#title span").text("界面")
                location.href="index.html"
            }else
                $("#title span").text("界面("+data.adminName+")");
        },
        timeout:10000
    })
}

function readUserImg(){
    var userName=$("#userImg input").val().trim()
    if(userName=="")
        return;
    else
        $.ajax({
            url:"readUserImg",
            type:"GET",
            data:{
                userName:$("#userImg input").val().trim()
            },
            beforeSend:function(){
                $("#userImg div:last-child").text("查询中...");
            },
            error:function(){
                $("#userImg div:last-child").text("查询失败");
            },
            success:function (data) {
                var userImg=data.userImg;
                var imgShow=$("#userImg div:last-child");
                imgShow.html("")
                var color=["red","green","white","orange","yellow","pink"];
                var ranNum=0;
                var index=0;
                for(var key in userImg){
                    //0-1.0
                    ranNum=parseInt(Math.random()*10);
                    index=ranNum%color.length
                    imgShow.append("<div Style='font-size:"+(userImg[key]%100+10+ranNum*3)+"px;color:"+color[index]+";margin-left:"+ranNum*3+"px;'>"+key+"&nbsp;&nbsp;</div>")
             }

            },
            timeout:100000
        
    })
}

function newUserImg() {
    var timeOut=null;
    $.ajax({
        url:"newUserImg",
        type:"POST",
        beforeSend:function () {
            var i=0;
            timeOut=setInterval(function () {

                switch (i%3){
                    case 0:$("#userImg div:last-child").text("导入HBase中.");++i;break;
                    case 1:$("#userImg div:last-child").text("导入HBase中..");++i;break;
                    case 2:$("#userImg div:last-child").text("导入HBase中...");++i;break;
                    case 3:$("#userImg div:last-child").text("导入HBase中");++i;break;
                }
            },1000)

        },
        success:function (data) {
            clearInterval(timeOut)
            $("#userImg div:last-child").html("<font size='30px'>导入成功</font>");
            setTimeout(function () {
                $("#userImg div:last-child").html("");
            },2000)
        },
        error:function () {
            clearInterval(timeOut)
            $("#userImg div:last-child").html("<font size='30px'>导入失败</font>");
        }
    })
}

function getUV(){
    var timeOut=null;
    $.ajax({
        url:"getUV",
        type:"POST",
        beforeSend:function () {
            var i=0;
            $("#getUV div").css("width","700px")
            timeOut=setInterval(function () {

                switch (i%3){
                    case 0:$("#getUV div").text("生成中.");++i;break;
                    case 1:$("#getUV div").text("生成中..");++i;break;
                    case 2:$("#getUV div").text("生成中...");++i;break;
                    case 3:$("#getUV div").text("生成中");++i;break;
                }
            },1000)

        },
        success:function (data) {
            clearInterval(timeOut)
            $("#getUV div").css('width','200px')
            $("#getUV div").text(data.uv)
        }
    })

}

function adminUpdateGood(foodName,price,oldPrice,margin,type,img){
    $.ajax({
        url:"adminUpdateGood",
        type:"POST",
        data:{
            foodName:foodName,
            price:price,
            oldPrice:oldPrice,
            margin:margin,
            type:type,
            img:img
        },
        success:function () {
            getFoodTable(null)
        },
        timeout:10000
    })

}

var pageMsg={pageNow2:1}
function getFoodTable(handle){

    $.ajax({
        url:"foodPageShow",
        type:"POST",
        data:{
            pageNow2:pageMsg.pageNow2,
            handle:handle
        },
        success:function (data) {

            $("#goodsShow table").html("<tr>\n" +
                "<td colspan=\"7\">\n" +
                "</td>\n" +
                "</tr>\n" +
                "<tr>\n" +
                "<th>名称</th><th>单价</th><th>成本</th><th>余量</th>\n" +
                "<th>种类</th><th>图片</th><th>确认</th>\n" +
                "</tr>")

            $.each(data.foods,function(index,e){

                $("#goodsShow table").append("<tr>" +
                    "<td><input value='" + e.foodName + "'/></td><td><input value='" + e.price + "'/></td><td><input value='" + e.oldPrice + "'/></td>" +
                    "<td><input value='" + e.margin + "'/></td><td><input value='" + e.type + "'/></td><td><input value='"+e.img+"'/></td><td>确认</td>" +
                    "</tr>" +
                    "")


            })

            $("#footOfGoodsShow ul li:nth-child(2)").text(data.pageNow+"/"+data.pageCount)

            pageMsg.pageNow2=data.pageNow
        },
        timeout:10000

    })

}