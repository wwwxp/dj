<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions"%>
<!DOCTYPE html>
<html lang="zh-cn">
<c:set var="ctx" value="${pageContext.request.contextPath}"/>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
	<link type="text/css" rel="stylesheet" href="${ctx}/css/homepage/common.css">
	<link type="text/css" rel="stylesheet" href="${ctx}/css/homepage/iconfont.css">
	<script src="${ctx}/js/common/jquery.min.js" type="text/javascript"></script>
	<script src="${ctx}/js/common/echarts-all.js" type="text/javascript"></script>
	<script language="javascript" type="text/javascript" src="${ctx}/js/homepage/common.js"></script>
	<script language="javascript" type="text/javascript" src="${ctx}/js/homepage/iconfont.js"></script>
	<title>首页</title>
	</style>
</head>

<body>
	<div class="lina-main">
		<!--tab begin-->
		<ul class="tabNav">
			<li class="hover"><a href="#">采预集群</a></li>
			<li><a href="#">批价集群</a></li>
			<li><a href="#">其它集群</a></li>
			<li><a href="#">其它集群</a></li>
		</ul>
		<div class="tabCnt">
			<div class="tabPane hover">
				<!--采预集群 begin-->
				<div class="leftMain">
					<!--主机进程 begin-->
					<div class="host">
						<dl class="blueBg">
							<dt>
								<p>
									<span>55台</span>部署机器
								</p>
								<p>
									<span>220个</span>业务进程
								</p>
							</dt>
							<dd>
								<table width="100%" border="0" cellspacing="0" cellpadding="0"
									class="host-table">
									<tr>
										<th width="40%"><i class="iconfont icon-dian"></i>dcm</th>
										<td width="30%">主机：10</td>
										<td width="30%">进程：20</td>
									</tr>
									<tr>
										<th><i class="iconfont icon-dian"></i>dispatch</th>
										<td>主机：10</td>
										<td>进程：20</td>
									</tr>
									<tr>
										<th><i class="iconfont icon-dian"></i>other</th>
										<td>主机：10</td>
										<td>进程：20</td>
									</tr>
									<tr>
										<th><i class="iconfont icon-dian"></i>dispatch</th>
										<td>主机：10</td>
										<td>进程：20</td>
									</tr>
									<tr>
										<th><i class="iconfont icon-dian"></i>other</th>
										<td>主机：10</td>
										<td>进程：20</td>
									</tr>
								</table>
								<!--分页 begin-->
								<div class="page">
									<a><i class="iconfont icon-zuo201"></i></a> <a><i
										class="iconfont icon-you201"></i></a>
								</div>
							</dd>
						</dl>
						<dl class="greenBg">
							<dt>
								<p>
									<span>12台</span>基础组件
								</p>
								<p>
									<span>110个</span>组件实例
								</p>
							</dt>
							<dd>
								<table width="100%" border="0" cellspacing="0" cellpadding="0"
									class="host-table">
									<tr>
										<th width="40%"><i class="iconfont icon-dian"></i>zk</th>
										<td width="30%">主机：2</td>
										<td width="30%">进程：3</td>
									</tr>
									<tr>
										<th><i class="iconfont icon-dian"></i>jstrom</th>
										<td>主机：5</td>
										<td>进程：200</td>
									</tr>
									<tr>
										<th><i class="iconfont icon-dian"></i>dca</th>
										<td>主机：15</td>
										<td>进程：100</td>
									</tr>
									<tr>
										<th><i class="iconfont icon-dian"></i>jstrom</th>
										<td>主机：10</td>
										<td>进程：20</td>
									</tr>
									<tr>
										<th><i class="iconfont icon-dian"></i>dca</th>
										<td>主机：15</td>
										<td>进程：100</td>
									</tr>
								</table>
								<!--分页 begin-->
								<div class="page">
									<a><i class="iconfont icon-zuo201"></i></a> <a><i
										class="iconfont icon-you201"></i></a>
								</div>
							</dd>
						</dl>
						<div class="clear"></div>
					</div>

					<!--资源占比TOP5 begin-->
					<div class="top5 mt30">
						<h3>
							<a>更多</a>资源占比TOP5
						</h3>
						<ul class="mt30">
							<li><div class="mapchartContainer" id="PieChart1"></div></li>
							<li><div class="mapchartContainer" id="PieChart2"></div></li>
							<li><div class="mapchartContainer" id="PieChart3"></div></li>
							<div class="clear"></div>
						</ul>
					</div>
				</div>
				<div class="rightMain">
					<!--点状图 begin-->
					<div class="Graph">
						<div>
							<select class="gy-sel">
								<option>请选择</option>
								<option>选项一</option>
								<option>选项二</option>
							</select>
						</div>
						图表占位
					</div>

					<!--表格 begin-->
					<div class="tableBox mt30">
						<div class="tablehead">
							<table border="0" cellpadding="0" cellspacing="0" width="100%">
								<tr>
									<th width="25%">服务</th>
									<th width="25%">IP</th>
									<th width="25%">pending</th>
									<th width="25%">进程号</th>
								</tr>
							</table>
						</div>
						<div class="tablebody">
							<table border="0" cellpadding="0" cellspacing="0" width="100%">
								<tr>
									<td width="25%">fmtservice</td>
									<td width="25%">192.168.161.1</td>
									<td width="25%">30000</td>
									<td width="25%">9876</td>
								</tr>
								<tr>
									<td>fmtservice</td>
									<td>192.168.161.1</td>
									<td>30000</td>
									<td>9876</td>
								</tr>
								<tr>
									<td>fmtservice</td>
									<td>192.168.161.1</td>
									<td>30000</td>
									<td>9876</td>
								</tr>
								<tr>
									<td>fmtservice</td>
									<td>192.168.161.1</td>
									<td>30000</td>
									<td>9876</td>
								</tr>
								<tr>
									<td>fmtservice</td>
									<td>192.168.161.1</td>
									<td>30000</td>
									<td>9876</td>
								</tr>
								<tr>
									<td>fmtservice</td>
									<td>192.168.161.1</td>
									<td>30000</td>
									<td>9876</td>
								</tr>
								<tr>
									<td>fmtservice</td>
									<td>192.168.161.1</td>
									<td>30000</td>
									<td>9876</td>
								</tr>
								<tr>
									<td>fmtservice</td>
									<td>192.168.161.1</td>
									<td>30000</td>
									<td>9876</td>
								</tr>
								<tr>
									<td>fmtservice</td>
									<td>192.168.161.1</td>
									<td>30000</td>
									<td>9876</td>
								</tr>
								<tr>
									<td>fmtservice</td>
									<td>192.168.161.1</td>
									<td>30000</td>
									<td>9876</td>
								</tr>
							</table>
						</div>
					</div>
				</div>
				<div class="clear"></div>
			</div>
			<div class="tabPane">22</div>
			<div class="tabPane">33</div>
			<div class="tabPane">44</div>
		</div>
	</div>
</body>
</html>