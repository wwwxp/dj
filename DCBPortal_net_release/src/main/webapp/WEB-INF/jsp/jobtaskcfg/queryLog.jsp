<%@ page language="java" contentType="text/html; charset=UTF-8"
         pageEncoding="UTF-8" %>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
    <%@ include file="/public/common/common.jsp" %>
    <script src="${ctx}/js/jobtaskcfg/queryLog.js"
            type="text/javascript"></script>
    <title>任务日志查看</title>
</head>
<body>
<div class="mini-fit p5">
    <div class="search2" style="border: 0px;padding:0px;">
        <div style="float: left">
            <span>任务ID：<label id="job_id_label"></label>，任务名称：<label id="job_name_label"></label></span>
        </div>
        <div style="text-align: right">
            <a class="mini-button mini-button-green"  onclick="query()" plain="false">刷新</a>
        </div>
    </div>
    <div class="mini-fit" style="margin-top: 2px;">
        <div id="datagrid" class="mini-datagrid"
             style="width: 100%; height: 100%;" idField="ID" allowResize="false"
             multiSelect="false">
            <div property="columns">
                <div type="indexcolumn" width="3">序号</div>


                <div field="EXEC_RESULT_STATUS_DESC" headerAlign="center" align="center"
                     renderer="onStatusRenderer"
                     width="4">执行结果
                </div>

                <div field="EXEC_MESSAGE" headerAlign="center" align="left"
                     width="35">任务结果描述
                </div>
                <div field="EXEC_TIME" headerAlign="center" align="left"
                     width="4">执行耗时(秒)
                </div>
                <div field="CRT_DATE" headerAlign="center" align="center"
                     dateFormat="yyyy-MM-dd HH:mm:ss" width="7">执行开始时间
                </div>
                <div field="CRT_DATE" headerAlign="center" align="center"
                     dateFormat="yyyy-MM-dd HH:mm:ss" width="7">执行结束时间
                </div>

            </div>
        </div>
    </div>
</div>
</body>
</html>
