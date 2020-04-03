/**
 * Created with IntelliJ IDEA.
 * User: tom
 * Date: 15-5-15
 * Time: 下午2:26
 * To change this template use File | Settings | File Templates.
 */
//菜单数据
var menus = [];
//首页菜单地址
var homePageUrl = Globals.ctx+"/jsp/homepage/homepage";

//初始化
$(document).ready(function () {
    mini.parse();
    //获取菜单数据
    loadMenu();
    //加载主菜单
    loadTopMenu();

});

/**
 * 发送ajax请求获取菜单数据
 */
function loadMenu() {
    $.ajax({
        url:  Globals.ctx+"/login/getPrivilege",
        type: "POST",
        data: {
            showRoot: "false",
            changeMenu: "true"
        },
        cache: false,
        async: false,
        success: function (result) {
            menus = result;
        },
        error: function (jqXHR, textStatus, errorThrown) {
            mini.alert("获取菜单失败!");
        }
    });
}

/**
 * 加载渲染所有菜单
 */
function loadTopMenu() {
    //渲染顶部右边菜单
    var topMenuHtmls = '<dl id="menu_-1" tar="-1"> <dt> <img src="' +  Globals.ctx+'/css/vk_style/images/menu_home.png" /> </dt> <dd>首页</dd> </dl>';
    var subMenu = "";
    $
        .each(
        menus,
        function (i, mo) {
            if (mo["PARENT_PRIVILEGE_ID"] == "1") {
                //加载主菜单
                topMenuHtmls += '<dl tar="' + i + '" path="'+mo["PATH"]+'" url="'+mo["URL"]+'" id="menu_' 
                    + mo["PRIVILEGE_ID"] + '"> <dt> <img src="' +  Globals.ctx + '/css/vk_style/images/' + mo["IMAGE"] + '" /> </dt> <dd>'
                    + mo["PRIVILEGE_NAME"]
                    + '</dd> </dl>';
                subMenu += '<div class="menu_second" id="second_menu' + i + '">';
                //加载二级菜单
                $
                    .each(
                    menus,
                    function (i2, mo2) {
                        if (mo2["PARENT_PRIVILEGE_ID"] == mo["PRIVILEGE_ID"]) {
                            subMenu += '<ul>';
                            subMenu += '<li>';
                            subMenu += '<a href="javascript:void(0);" class="a_h3">'
                                + mo2["PRIVILEGE_NAME"]
                                + '</a>';
                            subMenu += '</li>';
                            //加载三级菜单
                            subMenu += '<li>';
                            $
                                .each(
                                menus,
                                function (i3, mo3) {
                                    if (mo3["PARENT_PRIVILEGE_ID"] == mo2["PRIVILEGE_ID"]) {
                                        var subMenuArray = new Array();
                                        subMenu += '<a id="' + mo3["PRIVILEGE_ID"] + '">'
                                            + mo3["PRIVILEGE_NAME"]
                                            + '</a>';
                                        //增加点击事件
                                        $(
                                            "#"
                                                + mo3["PRIVILEGE_ID"])
                                            .die(
                                                "click")
                                            .live(
                                            "click",
                                            function (event) {
                                                jumpPage(mo3);
                                            });
                                        //递归遍历查找四级菜单（四级菜单也显示为三级）
                                        findSubMenu(
                                            mo3["PRIVILEGE_ID"],
                                            subMenuArray);
                                        $
                                            .each(
                                            subMenuArray,
                                            function (i4, mo4) {
                                                subMenu += '<a id="' + mo4["PRIVILEGE_ID"] + '">'
                                                    + mo4["PRIVILEGE_NAME"]
                                                    + '</a>';
                                                //增加点击事件
                                                $(
                                                    "#"
                                                        + mo4["PRIVILEGE_ID"])
                                                    .die(
                                                        "click")
                                                    .live(
                                                    "click",
                                                    function (event) {
                                                        jumpPage(mo4);
                                                    });
                                            });
                                    }
                                });
                            subMenu += '</li>'
                            subMenu += '</ul>'
                        }

                    });
                subMenu += '</div>'
            }
        });
    //渲染主菜单与子菜单
    $("#menuHead").html(topMenuHtmls);
    $("#subMenuDiv").append(subMenu);
    //增加监听事件
    blinkEvent();

}

/**
 * 查找二级菜单
 * @param menuId
 * @param subMenuArray
 */
function findSubMenu(menuId, subMenuArray) {
    for (var i = 0; i < menus.length; i++) {
        var mo = menus[i];
        if (mo["PARENT_PRIVILEGE_ID"] == menuId) {
            subMenuArray.push(mo);
            if (!isLeaf(mo["PRIVILEGE_ID"])) {
                findSubMenu(mo["PRIVILEGE_ID"], subMenuArray);
            }
        }
    }
}

/**
 * 查找二级菜单是否还有子菜单
 * @param menuId
 * @returns {Boolean}
 */
function isLeaf(menuId) {
    var leaf = true;
    for (var i = 0; i < menus.length; i++) {
        var mo = menus[i];
        if (mo["PARENT_PRIVILEGE_ID"] == menuId) {
            leaf = false;
            break;
        }
    }
    return leaf;
}

/**
 * 二、三级菜单的展示和展示位置
 * @param firstMenuElement
 * @param secondMenuDiv
 */
function secondMenuShow(firstMenuElement, secondMenuDiv) {
    var firstMenuLeft = firstMenuElement.offset().left;
    var windowWidth = $(window).width();
    var secondMenuWidth = secondMenuDiv.width();
    var secondMenuTop = firstMenuElement.offset().top
        + firstMenuElement.height();
    var secondMenuLeft = firstMenuLeft;
    var secondMenuRight = firstMenuLeft + secondMenuWidth;
    if (secondMenuLeft < 0) {
        secondMenuLeft = 0;
    }
    if (secondMenuRight + 24 >= windowWidth) {
        secondMenuLeft = windowWidth - secondMenuWidth - 24;
        secondMenuRight = windowWidth - 24;
    }
    secondMenuDiv.css({
        top: secondMenuTop,
        left: secondMenuLeft,
        width: secondMenuWidth
    });

    secondMenuDiv.show();
}

/**
 * 菜单事件监听
 */
function blinkEvent() {
    //增加事件
    $('#menuHead').find('dl').click(// 鼠标移入移出样式
        function () {
            //清除Class样式
            $('#menuHead dl').each(function (i) {
                $("#" + this.id).removeClass("hover");
            })
            //增加当前点击的样式
            $("#" + this.id).addClass("hover");
            //加载子菜单
            var tar = $(this).attr('tar');
            $('div[class="menu_second"]').hide();
            //加载子菜单
            if (tar != "-1") {
            	//获取一级菜单的url 
            	var url = $(this).attr('url'); 
            	//如果一级菜单的url 不为空，且二级菜单也不空，则显示二级菜单
            	//如果一级菜单的url 不 为空，二级菜单为空，则点一级菜单转到相应的jsp
            	if(isNotEmptyStr(url)){
            		 var sub = $('#second_menu' + tar);
            		if(isNotEmptyStr(sub.html())){
            			secondMenuShow($(this), $('#second_menu' + tar));
            		}else{
            			var path = $(this).attr('path');
            			var obj = new Object();
            			obj["URL"] = url;
            			obj["PATH"] = path;
            			jumpPage(obj);
            		}
            	}
                
            } else {
                //加载首页
                $("#pageContainer").attr("src", homePageUrl);
                $("#navTipSpan").html("首页");
            }
        });
    // 鼠标移入main主页面后隐藏2、3级菜单
    $('#pageContainerDiv').mouseenter(function () {
        //鼠标移出
        $('div[class="menu_second"]').hide();
    });

    //默认点击首页，第一个顶部主菜单
    $('#menuHead dl').each(function (i) {
        if (i == "0") {
            $("#" + this.id).trigger("click");
        }
    });
}

/**
 * 菜单加载页面
 * @param item
 */
function jumpPage(item) {
    var urladdr = item["URL"];
    if (urladdr == null || urladdr == "") {
        //mini.alert("该菜单项未配置菜单地址！");
    } else {
        $("#pageContainer").attr("src", Globals.ctx+urladdr);
        var pathArray = item["PATH"].split("|");
        //得到路径“|金融支付|XXX|XXX
        //去掉“|金融支付”
        pathArray.splice(0, 2);//从第1个位置开始删除，删除2个元素
        $("#navTipSpan").html(pathArray.join("&nbsp;>&nbsp;"));
    }
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
    });
}

/**
 * 显示或者隐藏菜单
 */
function hideOrShowMenu(){
    var pageContainerH = $("#pageContainerDiv").height();
    var headH = $(".head").height();
    if($(".head").is(":hidden")){
        $(".head").show();    //如果元素为隐藏,则将它显现
        $("#pageContainerDiv").height(pageContainerH-headH);
        $(".top").css("border-bottom","none");
        $("#hideOrShowIco").attr("src",Globals.ctx+"/css/vk_style/images/top_ico5.png");
        $("#hideOrShowA").attr("title","隐藏菜单");
    }else{
        $(".head").hide();     //如果元素为显现,则将其隐藏
        $("#pageContainerDiv").height(pageContainerH+headH);
        $(".top").css("border-bottom","1px solid #ccc");
        $("#hideOrShowIco").attr("src",Globals.ctx+"/css/vk_style/images/top_ico4.png");
        $("#hideOrShowA").attr("title","显示菜单");
    }
}