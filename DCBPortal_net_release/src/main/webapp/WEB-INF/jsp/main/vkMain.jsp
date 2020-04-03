<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
    <!--公共的css与js -->
    <%@ include file="/public/common/common.jsp"%>
    <!-- VK Style -->
    <link rel="stylesheet" type="text/css" href="${ctx}/css/vk_style/css/common.css"/>
    <script type="text/javascript" src="${ctx}/js/main/vkMain.js"></script>
    <title>DCBP云计费平台</title>
</head>

<body>
<!--顶部 begin-->
<div class="top">
		<span style="width:120px;">
			<a id="hideOrShowA" href="javascript:hideOrShowMenu()" title="隐藏菜单"><img id="hideOrShowIco" src="${ctx}/css/vk_style/images/top_ico5.png"/></a>
            <a href="javascript:editPassword()" title="改密码"><img src="${ctx}/css/vk_style/images/top_ico3.png"/></a>
			<a href="javascript:logoff()" title="注销"><img src="${ctx}/css/vk_style/images/top_ico2.png"/></a>
		</span>用户：<em class="cBlue">${sessionScope.userMap.EMPEE_NAME}</em>，您好，欢迎您！
</div>
<!--头部+导航 begin-->
<div class="head" style="background-image: url('${ctx}/css/vk_style/images/logo.gif');background-repeat:no-repeat">
    <div class="menu" id="menuHead">
        <div class="clear"></div>
    </div>
</div>
<div class="tab_bq">
    <span>&nbsp;&nbsp;当前位置：</span> <span id="navTipSpan"></span>
</div>
</ul>
<!--二三级导航 begin-->
<div id="subMenuDiv" style="width: 0px;height: 0px;"></div>
<div id="pageContainerDiv" class="mini-fit">
    <iframe frameborder="0" style="width: 100%;height: 100%;" id="pageContainer"></iframe>
</div>
</body>
</html>
