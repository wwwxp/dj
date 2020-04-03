/**
 * 定义变量， 通常是页面控件和参数
 */
var JsVar = new Object();

/**
 * 初始化
 */
$(document).ready(function() {
	mini.parse();
	// 表格获取
	JsVar["datagrid"] = mini.get("datagrid");

});

function onLoadComplete(data) {
	JsVar["data"] = data;
	var CLUSTER_NAME = data.CLUSTER_NAME;
	$('#node_name_label').html(data.NODE_NAME);
	if(CLUSTER_NAME == 'dcbilling'){
		$('#clustern_name_label').html("批价中心（" +CLUSTER_NAME +"）");
	}else{
		$('#clustern_name_label').html("采集中心（" +CLUSTER_NAME +"）");
	}
	loaddatagrid(JsVar["data"])
}

/**
 * 加载定时任务表格
 */
function loaddatagrid(paramsObj) {

	// 加载表格信息
	datagridLoadPage(JsVar["datagrid"], paramsObj,
			"jobtaskcfg.queryExpandStrategyLogList");
}
function queryl(){
	// 加载表格信息
	datagridLoadPage(JsVar["datagrid"], JsVar["data"],
			"jobtaskcfg.queryExpandStrategyLogList");
}

function execTypeRenderer(e) {
	var EXEC_TYPE = e.record.EXEC_TYPE;
	if (EXEC_TYPE == 'cmd') {
		return "命令模式";
	} else {
		return "数据库模式";
	}
}
function onStatusRenderer(e) {
	var EXEC_RESULT = e.record.EXEC_RESULT;
	if (EXEC_RESULT == '0') {
		return "<span class='label label-danger' style='letter-spacing:0.2em;'>&nbsp;失败&nbsp;</span>";
	} else {
        return "<span class='label label-success' style='letter-spacing:0.2em;'>&nbsp;成功&nbsp;</span>";
	}
}
function paramsRenderer(e) {
	var EXEC_TYPE = e.record.EXEC_TYPE;
	if (EXEC_TYPE == 'cmd') {
		return e.record.CMD_CONTENT;
	} else {
		return e.record.TASK_CONTENT;
	}
}

/**
 * 状态渲染
 */
function taskTypeRenderer(e) {
	var TASK_TYPE = e.record.TASK_TYPE;
	if (TASK_TYPE == 2) {
		return "循环调度";
	} else if (TASK_TYPE == 3) {
		return "自定义调度";
	} else {
		return "常规任务";
	}
}

/**
 * 触发值 显示HOST_NORM_MSG
 */
function onRuleMsgRenderer(e) {
	var data = e.record;
	var index = e.rowIndex;
	// var html = '<a class="Delete_Button" href="javascript:showHostNormMsg('
	// 		+ index + ')">' + data.RULE_MSG + '</a>';
	// return html;
	return data.RULE_MSG;
}

/**
 * 显示HOST_NORM_MSG
 */
function showHostNormMsg(e) {
	// 查询数据
	var htmlMsg = '';

	var index = parseInt(e);
	var row = JsVar["datagrid"].getRow(index);
	// 拼接数据
	var msg = row.HOST_NORM_MSG;

	var MsgJsonObj = "";
	try {
        MsgJsonObj = JSON.parse(msg);
	} catch(err) {
        MsgJsonObj = msg;
	}


	htmlMsg = "<textarea style='width:750px;height:280px;font-size:12px;' readonly='readonly'>"
			+ getHostNormMsgFormatStr(MsgJsonObj) + "</textarea>";

	mini.showMessageBox({
		showHeader : false,
		minWidth : 800,
		minHeight : 400,
		title : "日志",
		buttons : [ "ok" ],
		message : htmlMsg,
		iconCls : "",
		callback : null
	});
}
/**
 * 
 */
function getHostNormMsgFormatStr(obj) {
	// 结构 obj 为数组，然后属性为 code，result，result 为数组里面有其他属性和 批次号数组

	var formatStr = "";
	// 结果数组
	for (var i = 0; i < obj.length; i++) {
		if ("200" == obj[i].code) {
			resultAry = obj[i].result;
			//formatStr += "\n   --------------------------------------------------------------";
			for (var j = 0; j < resultAry.length; j++) {

				//formatStr += "\n   " + "批次号" + JSON.stringify(resultAry[j].batchNo);

				formatStr += "主机：" + resultAry[j].ip + " CPU使用率（%）："
						+ resultAry[j].cpuRateSum + " 磁盘使用率（%）："
						+ resultAry[j].diskTotalRate + " 内存使用率（%）："
						+ resultAry[j].memoryRateSum + " 网络输出（M）："
						+ resultAry[j].networkOutRateSum + " 网络输入（M）："
						+ resultAry[j].networkInRateSum +"\n"

				// formatStr += "\n
				// --------------------------------------------------------------";
			}
		}
		// formatStr +=
		// "\n=============================END==================================\n";
	}
	return formatStr;
}