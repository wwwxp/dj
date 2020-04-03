/**
 * Created with IntelliJ IDEA.
 * User: tom
 * Date: 15-6-6
 * Time: 下午1:43
 * To change this template use File | Settings | File Templates.
 */
//菜单数据
var menus = [];
//首页菜单地址
var homePageUrl = Globals.ctx+"/jsp/homepage/homepage.jsp";

//初始化
$(document).ready(function () {
    mini.parse();
    //获取菜单数据
    loadMenu();
    //初始化首页菜单
    initHomePageHtmlMenu();
    //初始化菜单
    initHtmlMenu();

});

//发送ajax请求获取菜单数据
function loadMenu() {
    $.ajax({
        url:  Globals.ctx+"/loginAction.do?method=getPrivilege",
        type: "POST",
        data: {
            showRoot: "false",
            changeMenu: "true"
        },
        cache: false,
        async: false,
        success: function (result) {
            menus = mini.decode(result);
        },
        error: function (jqXHR, textStatus, errorThrown) {
            mini.alert("获取菜单失败!");
        }
    });
}
//初始化菜单
function initHtmlMenu(){
    //转换为菜单显示的数据
    convertMenuData();
    //初始化菜单
    $('#menu').dicMenu({
        data :menus,
        click : function(m) {
            jumpPage(m);
        }
    });
}
function hrefToHomePage(){
    //加载首页
    $("#pageContainer").attr("src", homePageUrl);
    $("#navTipSpan").html("首页");
}
function editPassword(){
    showDialog("修改密码",300,200,Globals.baseJspUrl.USER_JSP_EDIT_PASSWORD_URL,function destroy(action){
        if(action==systemVar.OK){
            showMessageTips("修改密码成功！");
        }
    });
}
function logoff(){
    showConfirmMessageAlter("是否确定注销系统？",function ok(){
        window.location.href = Globals.baseActionUrl.HOME_ACTION_LOGIN_OUT_URL ;
    })
}

//初始化首页菜单
function initHomePageHtmlMenu(){
    $('#hmMenu').dicMenu({
        data :[{id:"",level:"1",pid:"0",name:"首页",url:""}],
        click : function(m) {
            //加载首页
            $("#pageContainer").attr("src", homePageUrl);
            $("#navTipSpan").html("首页");
        }
    });
    //加载首页
    $("#pageContainer").attr("src", homePageUrl);
    $("#navTipSpan").html("首页");
}

//菜单加载页面
function jumpPage(item) {
    var urladdr = item["URL"];
    if (urladdr == null || urladdr == "") {

    } else {
        $("#pageContainer").attr("src", Globals.ctx+urladdr);
        var pathArray = item["PATH"].split("|");
        //得到路径“|系统菜单名称|XXX|XXX
        //去掉“|系统菜单名称”
        pathArray.splice(0, 2);//从第1个位置开始删除，删除2个元素
        $("#navTipSpan").html(pathArray.join("&nbsp;>&nbsp;"));
    }
}

//显示头部，隐藏头部
function menuUpDown(flag){
    //隐藏头部后，内容高度不会自动计算，此处需要程序计算
    var bmpLayout = mini.get("bmpLayout");
    var northRegionHeight=$("#north").height();
    var bmpHeadHeight=$("#bmpHead").height();
    if(flag=="up"){
        $("#bmpHead").hide();
        bmpLayout.updateRegion("north",{height:northRegionHeight-bmpHeadHeight});
        $("#menuUp").hide();
        $("#menuDown").show();
    }else{
        $("#bmpHead").show();
        bmpLayout.updateRegion("north",{height:northRegionHeight+bmpHeadHeight});
        $("#menuUp").show();
        $("#menuDown").hide();
    }
}

//把数据库查询出来的菜单数据，转换为菜单组件需要的菜单数据格式
function convertMenuData(){
    $.each(menus,function(i,menu){
        menu["id"]=menu["PRIVILEGE_ID"];
        menu["name"]=menu["PRIVILEGE_NAME"];
        menu["pid"]=menu["PARENT_PRIVILEGE_ID"];
        menu["level"]=menu["MENU_LEVEL"];
        menu["url"]=menu["URL"];
    });
}