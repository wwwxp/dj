 

/**
 * 定义变量， 通常是页面控件和参数
 */
var JsVar = new Object();
 
/**
 * 初始化
 */
$(document).ready(function () {
    mini.parse();
    JsVar["datagrid"]=mini.get("datagrid");
    load();
});

//加载表格 
function load(){
	datagridLoad(JsVar["datagrid"],{},"logLevelCfg.queryList");
}
/**
 * 加载完后，新增一行
 */
function treeLoadEvent(e){
	var row = e.sender.getRow(0);
	if(isEmptyObject(row)){
		addRow(0);
	} 
}

/**
 * 保存发送
 */
function submit(){
 
	if(JsVar["datagrid"].isChanged()){
		var data = JsVar["datagrid"].getChanges();
		var obj = {};
		
		for (var i=0; i<data.length; i++) {
			if (data[i]["PRO_VALUE"] == "" || data[i]["PRO_VALUE"] == undefined
				|| data[i]["PRO_KEY"] == "" || data[i]["PRO_KEY"] == undefined) {
                showWarnMessageTips("参数名和参数值不能为空，请输入！");
				return;
			}
		}
		
		obj["list"] = data;
		//提交到后台处理
		getJsonDataByPost(Globals.baseActionUrl.LOG_LEVEL_CFG_ACTION_UPDATE_URL,obj,"日志级别配置-操作",
			function(result){
				JsVar["datagrid"].reload();
                showMessageTips("配置成功！");
			}
		);
	}else{
        showWarnMessageTips("表格未发生改变");
		return;
	}
}
function sendMsg(){
	var rows = JsVar["datagrid"].getSelecteds();
	if(rows.length<1){
        showWarnMessageTips("请至少选择一条记录");
		return;
	}
	var obj = {};
	obj["checkData"] = rows;
	showDialog("明细",600,450,Globals.baseJspUrl.LOG_LEVEL_JSP_CONFIRM_URL,
	        function destroy(data){
	             
	    },rows);
	
}


/**
 * 操作渲染
 * @returns
 */
function onActionRenderer(e){
    var uid = e.record._uid;
    var rowIndex = e.rowIndex;
	var html="";
	 var html = '<a class="New_Button" href="javascript:addRow(\'' + rowIndex + '\')">新增</a>'
         + ' <a class="Delete_Button" href="javascript:removeRow(\'' + uid + '\')">删除</a>';
	 return html;
}
/**
 * 新增行
 */
function addRow(rowIndex){
	 var row = {};
	 JsVar["datagrid"].addRow(row, rowIndex+1);
	 JsVar["datagrid"].cancelEdit();
	 JsVar["datagrid"].beginEditRow(row);
}

/**
 * 删除该行
 */
function removeRow(uid){
	var row = JsVar["datagrid"].getRowByUID(uid);
	JsVar["datagrid"].removeRow(row);
}