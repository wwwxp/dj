<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ page language="java" import="java.util.Map,java.math.BigInteger" %>
<%@ page import="com.tydic.bp.core.utils.properties.SystemProperty" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions"%>
<%
  ServletContext cxt=request.getSession().getServletContext();
  Map<String,BigInteger> RSAMap=(Map<String,BigInteger>)cxt.getAttribute("RSAMap");
  String modulus=RSAMap.get("modulus").toString(16);
  request.setAttribute("modulus", modulus);
  request.setAttribute("webLatnId", SystemProperty.getContextProperty("latnId"));
%>
<c:set var="ctx" value="${pageContext.request.contextPath}"/>
<c:set var="sessionId" value="${pageContext.session.id}"/>
<c:set var="modulus" value="${requestScope.modulus}"/>
<c:set var="webLatnId" value="${requestScope.webLatnId}"/>
<link href="${ctx}/css/common.css" rel="stylesheet" type="text/css" />
<!-- 基本变量的设置 -->
<script type="text/javascript">
    //公共变量
    var Globals = {
        ctx: '${ctx}',
        sessionId: '${sessionId}',
        modulus:'${modulus}',
        webLatnId:'${webLatnId}'
    }
</script>
<script src="${ctx}/js/common/theamCfg.js" type="text/javascript"></script>
<script src="${ctx}/js/common/scripts/boot.js" type="text/javascript"></script>
<script src="${ctx}/js/common/systemConstant.js" type="text/javascript"></script>
<script src="${ctx}/js/common/utilAjax.js" type="text/javascript"></script>
<script src="${ctx}/js/common/utilMiniui.js" type="text/javascript"></script>
<script src="${ctx}/js/common/config.js" type="text/javascript"></script>
<script src="${ctx}/js/common/validatebox.js" type="text/javascript"></script>
<script src="${ctx}/js/common/dataDict.js" type="text/javascript"></script>
<script src="${ctx}/js/common/common.js" type="text/javascript"></script>
<script src="${ctx}/js/common/echarts-all.js" type="text/javascript"></script>
<script src="${ctx}/js/common/rsa/Barrett.js" type="text/javascript"></script>
<script src="${ctx}/js/common/rsa/BigInt.js" type="text/javascript"></script>
<script src="${ctx}/js/common/rsa/RSA.js" type="text/javascript"></script>
<script src="${ctx}/js/common/textSearchHighlight.js" type="text/javascript"></script>


<!-- 提示插件 -->
<link href="${ctx}/css/toastr/toastr.css" rel="stylesheet" type="text/css" />
<script src="${ctx}/js/common/toastr/toastr.js" type="text/javascript"></script>

