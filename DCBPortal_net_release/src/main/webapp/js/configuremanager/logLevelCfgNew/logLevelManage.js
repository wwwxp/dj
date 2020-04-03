//定义变量， 通常是页面控件和参数
var JsVar = new Object();
//初始化
$(document).ready(function () {
    mini.parse();
    //日志级别展示表格
    JsVar["logLevelGrid"] = mini.get("logLevelGrid");
    //取得查询表单
    JsVar["queryForm"] =  new mini.Form("#queryForm");
    //加载主机表格信息
    search();
});

//查询
function search() {
    var paramsObj = JsVar["queryForm"].getData();
    load(paramsObj);
}

//加载表格
function load(param){
    datagridLoadPage(JsVar["logLevelGrid"], param, "logLevelCfg.queryList");
}

//渲染操作按钮
function onActionRenderer(e) {
	var index = e.rowIndex;
    var html = '<a class="Delete_Button" href="javascript:edit(' + index + ')">修改</a>';
     	html += '<a class="Delete_Button" href="javascript:del(' + index + ')">删除</a>';
    return html;
}

/**
 * 新增日志级别
 */
function add() {
    showAddDialog("日志级别管理-新增日志级别",780, 200,Globals.baseJspUrl.LOG_LEVEL_JSP_ADD_EDIT_URL,
        function destroy(data){
            if (data == systemVar.SUCCESS) {
                JsVar["logLevelGrid"].reload();
                showMessageTips("新增日志级别成功");
            }
    });
}

/**
 * 修改日志级别
 * @param index
 */
function edit(index) {
	var row;
    //单个操作时，index值不为空也不为undefined；批量操作时，index不传，值为undefined
    if(index!=undefined){
        //单个操作
		row = JsVar["logLevelGrid"].getRow(index);
	}else{
        //批量操作
		var rows = JsVar["logLevelGrid"].getSelecteds();
	    if (rows.length == 1) {
		    row = rows[0];
	    } else {
            showWarnMessageTips("请选中一条记录!") ;
	        return;
	    }
	}
	showEditDialog("修改日志级别信息",780, 200,Globals.baseJspUrl.LOG_LEVEL_JSP_ADD_EDIT_URL,
	        function destroy(data){
	            if (data == systemVar.SUCCESS) {
	                JsVar["logLevelGrid"].reload();
	                showMessageTips("修改日志级别成功!");
	            }
	    },row);
}

/**
 * 删除日志级别配置
 * @param index
 */
function del(index) {
    var row = JsVar["logLevelGrid"].getRow(index);
    if (!row) {
    	row = JsVar["logLevelGrid"].getSelected();
    }
    if (!row) {
        showWarnMessageTips("请选中一条记录!") ;
    }
    showConfirmMessageAlter("确定删除记录？",function ok(){
    	getJsonDataByPost(Globals.baseActionUrl.LOG_LEVEL_CFG_ACTION_NEW_DEL_URL, row, "日志级别管理-删除日志级别",
            function(result){
                JsVar["logLevelGrid"].reload();
                showMessageTips("删除日志级别成功!");
            });
    });
}

/**
 * 发送消息
 */
function sendMsg() {
	var logLevelList = JsVar["logLevelGrid"].getSelecteds();
	if (logLevelList == null || logLevelList.length == 0) {
        showWarnMessageTips("请选中要发送的记录!");
		return;
	}
	
	//重新处理一下，主要是用来显示发送了那些参数
	var data = [];
	for (var i=0; i<logLevelList.length; i++) {
		data.push({
			PRO_ID:logLevelList[i]["PRO_ID"],
			PRO_KEY:logLevelList[i]["PRO_KEY"],
			PRO_VALUE:logLevelList[i]["PRO_VALUE"],
			PRO_DESC:logLevelList[i]["PRO_DESC"]
		});
	}
	
	var params ={
		checkData:data
	};
	
	//提交到后台处理
	getJsonDataByPost(Globals.baseActionUrl.LOG_LEVEL_CFG_ACTION_SENDMSG_URL, params, "日志级别配置-操作",
		function(result){
		    closeWindow(systemVar.SUCCESS);
		    showMessageTips("发送成功！");
		}
	);
}
