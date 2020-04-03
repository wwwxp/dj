<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
    <!--公共的css与js -->
    <%@ include file="/public/common/common.jsp"%>
    <!-- tydic Style -->
    <link rel="stylesheet" type="text/css" href="${ctx}/css/tydic_style/base.css"/>
    <script type="text/javascript" src="${ctx}/css/tydic_style/tydic.js"></script>
    <script type="text/javascript" src="${ctx}/js/main/tydicMain.js"></script>
    <title>BP_MINIUI前台管理</title>
</head>
<body>
<div class="mini-fit" style="overflow-y:hidden;">
    <div id="bmpLayout" class="mini-layout" style="width:100%;height:100%;" borderStyle="border:0px">
        <div id="north" region="north" height="88" showSplit="false" showHeader="false" style="border:0px;" splitSize="2">
            <!-- 头部-->
            <div id="bmpHead" style="width: 100%;height: 58px;">
                <div class="head-bg">
                    <div class="logo"></div>
                    <div class="logo-right">
                        <div class="logo-test">欢迎您： <span id="empeename" style="color: Red; font-family: Tahoma">${sessionScope.userMap.EMPEE_NAME}</span> | <a href="javascript:hrefToHomePage()">首页</a> | <a href="javascript:editPassword()">改密码</a> | <a href="javascript:logoff()">注销</a>&nbsp;&nbsp;&nbsp;</div>
                    </div>
                </div>
            </div>
            <!-- 头部END-->

            <!-- 菜单 -->
            <div id="bmpMenu" class="menu-bg">
                <table width="100%" border="0" cellspacing="0" cellpadding="0">
                    <tr>
                        <!--<td id="fmenuTd" width="55px">-->
                            <!-- 收藏夹菜单 -->
                            <!-- JS中动态产生 -->
                        <!--</td>-->
                        <td id="hmMenuTd" width="55px">
                            <!-- 首页菜单 -->
                            <div class="menu" id="hmMenu"></div>
                        </td>
                        <td>
                            <!-- 权限菜单 -->
                            <div class="menu" id="menu"></div>
                        </td>
                        <td width="20px;" valign="top">
                            <div class="menu-up-down">
                                <ul>
                                    <li class="menu-up" id="menuUp" onclick="menuUpDown('up')"></li>
                                    <li class="menu-down" id="menuDown" style="display: none;" onclick="menuUpDown('down')"></li>
                                </ul>
                            </div>
                        </td>
                    </tr>
                </table>
            </div>
            <!-- 菜单End -->
        </div>

        <div title="center" region="center"  showSplit="false" showHeader="false" splitSize="2" style="border:0px;">
            <!-- 内容区域-->
            <table width="100%" cellspacing="0" cellpadding="0" class="treeBg3">
                <colgroup>
                    <col style="height: 24px;" width="30px;"/>
                    <col style="width: 65px;"/>
                    <col/>
                    <col/>
                </colgroup>
                <tr>
                    <td class="ico5"></td>
                    <td>当前位置：</td>
                    <td id="navTipSpan" colspan="2"></td>
                </tr>
            </table>
            <div class="mini-fit frameStyle" style="overflow-y:hidden;">
                <!-- 添加此行代码，兼容性更好，但是在js中需要做控制，隐藏工作台角色时，需要增加或加减高度 -->
                <div id="mainIFrameDiv" class="mini-fit" style="overflow-y:hidden;">
                    <!-- iframe界面，加载内容 -->
                    <iframe id="pageContainer" name="pageContainer" frameborder="0"
                            style="padding: 0px;padding-top: 3px;margin:0px; width: 100%; height: 100%;"></iframe>
                </div>
            </div>
            <!-- 内容区域End -->
        </div>
    </div>
</div>

<!-- 底部 -->
<table width="100%" border="0" cellspacing="0" cellpadding="0" class="bottom">
    <tr>
        <td align="center">深圳天源迪科信息技术股份有限公司</td>
    </tr>
</table>
<!-- 底部end -->

</body>
</html>
