<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"%>
<%@ include file="/public/common/common.jsp"%>
<!DOCTYPE html>
<html lang="zh-cn">
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
	<link type="text/css" rel="stylesheet" href="${ctx}/css/homepage/lina.css">
	<link type="text/css" rel="stylesheet" href="${ctx}/css/homepage/iconfont.css">
	<link href="${ctx}/assets/css/vis.min.css" rel="stylesheet"/>
	<script src="${ctx}/assets/js/vis.min.js"></script>
	<script src="${ctx}/assets/js/vue.min.js"></script>
	<script src="${ctx}/assets/js/storm.js"></script>
	<script language="javascript" type="text/javascript" src="${ctx}/js/homepage/lina.js"></script>
	<script language="javascript" type="text/javascript" src="${ctx}/js/homepage/iconfont.js"></script>
	<title>首页</title>
	</style>
</head>
<body>
<div class="lina-main">
    <!--tab begin-->
    <ul class="tabNav">
        
    </ul>
    <div class="tabCnt">
        <div class="tabPane hover">
        	<!--采预集群 begin-->
            <div class="leftMain">
            	<!--主机进程 begin-->
                <div class="host">
                	<dl class="blueBg"> 
                    	<dt>
                        	<p><span id="buss_host_count">55台</span>部署机器</p>
                            <p><span id="buss_program_count">220个</span>业务进程</p>
                        </dt>
                        <dd>
                        	<table id="buss_table" width="100%" border="0" cellspacing="0" cellpadding="0" class="host-table">
                              <tr>
                                <th width="40%"><i class="iconfont icon-dian"></i>dcm</th>
                                <td width="30%">主机：10</td>
                                <td width="30%">进程：20</td>
                              </tr>
                              <tr>
                                <th width="40%"><i class="iconfont icon-dian"></i>dispatch</th>
                                <td width="40%">主机：10</td>
                                <td width="40%">进程：20</td>
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
                            <div class="page" id="buss_page">
                            	<a id="buss_left"><i class="iconfont icon-zuo201"></i></a>
                                <a id="buss_right"><i class="iconfont icon-you201"></i></a>
                            </div>
                        </dd>
                    </dl>
                    <dl class="greenBg">
                    	<dt>
                        	<p><span id="ass_host_count">12台</span>基础组件</p>
                            <p><span id="ass_program_count">110个</span>组件实例</p>
                        </dt>
                        <dd>
                        	<table id="ass_table" width="100%" border="0" cellspacing="0" cellpadding="0" class="host-table">
                              <tr>
                                <th width="40%"><i class="iconfont icon-dian"></i>zk</th>
                                <td width="30%">主机：2</td>
                                <td width="30%">进程：3</td>
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
                            <div class="page" id="ass_page">
                            	<a id="ass_left"><i class="iconfont icon-zuo201"></i></a>
                                <a id="ass_right"><i class="iconfont icon-you201"></i></a>
                            </div>
                            
                        </dd>
                    </dl>
                    <div class="clear"></div>
                </div>
                
                <!--资源占比TOP5 begin-->
                <div class="top5 mt30">
                	<h3><a>更多</a>资源占比TOP5</h3>
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
                	<select class="gy-sel" id="topoList">
                       <!--  <option>请选择</option>
                        <option>选项一</option>
                        <option>选项二</option> -->
                    </select>
                    </div>
                     <div id="topology-graph"></div>	
<!--                      <p id="topology-graph-tips" class="text-muted">无数据， 请检查.</p> -->
                     
                </div>
                
                <!--表格 begin-->
                <div class="tableBox-lina mt30" style="text-align:center;"> 
                    <div class="tablehead-lina">
                    <table border="0" cellpadding="0" cellspacing="0" width="100%"> 
                        <tr> 
                            <th width="25%" >服务</th>
                            <th width="25%">IP</th>
                            <th width="25%">pending</th>
                            <th width="25%">进程号</th>
                        </tr> 
                    </table> 
                    </div> 
                    <div class="tablebody-lina" style="text-align:center;"> 
                    <table id="boltTable" border="0" cellpadding="0" cellspacing="0" width="100%"> 
                        <tr>
                            <td class="lh40">fmtservice</td>
                            <td class="lh40" >192.168.161.1</td>
                            <td class="lh40" >30000</td>
                            <td class="lh40">9876</td>
                        </tr> 
                       <tr>
                            <td class="lh40">fmtservice</td>
                            <td class="lh40" >192.168.161.1</td>
                            <td class="lh40" >30000</td>
                            <td class="lh40">9876</td>
                        </tr> 
                        <tr>
                            <td class="lh40">fmtservice</td>
                            <td class="lh40" >192.168.161.1</td>
                            <td class="lh40" >30000</td>
                            <td class="lh40">9876</td>
                        </tr> 
                        <tr>
                            <td class="lh40">fmtservice</td>
                            <td class="lh40" >192.168.161.1</td>
                            <td class="lh40" >30000</td>
                            <td class="lh40">9876</td>
                        </tr> 
                         <tr>
                            <td class="lh40">fmtservice</td>
                            <td class="lh40" >192.168.161.1</td>
                            <td class="lh40" >30000</td>
                            <td class="lh40">9876</td>
                        </tr> 
                        <tr>
                            <td class="lh40">fmtservice</td>
                            <td class="lh40" >192.168.161.1</td>
                            <td class="lh40" >30000</td>
                            <td class="lh40">9876</td>
                        </tr> 
                         <tr>
                            <td class="lh40">fmtservice</td>
                            <td class="lh40" >192.168.161.1</td>
                            <td class="lh40" >30000</td>
                            <td class="lh40">9876</td>
                        </tr>  
                        <tr>
                            <td class="lh40">fmtservice</td>
                            <td class="lh40" >192.168.161.1</td>
                            <td class="lh40" >30000</td>
                            <td class="lh40">9876</td>
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