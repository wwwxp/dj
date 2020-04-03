//定义变量， 通常是页面控件和参数
var JsVar = new Object();
//初始化
$(document).ready(function () {
    mini.parse();
    JsVar["resultCodeGrid"] = mini.get("resultCodeGrid");//取得主机表格
    JsVar["queryFrom"] =  new mini.Form("#queryFrom");//取得查询表单
    //加载主机表格信息
    search();
});

//查询
function search() {
    var paramsObj = JsVar["queryFrom"].getData();
    load(paramsObj);
}
//重新加载表格
function refresh() {
    JsVar["queryFrom"].reset();
    load(null);
}
//加载表格
function load(param){
    datagridLoadPage(JsVar["resultCodeGrid"],param,"monitorMapper.queryResultCode","","anotherDataSource");
}

//渲染操作按钮
function onActionRenderer(e) {
	
    var index = e.rowIndex;
    var html="";
    var html = '<a class="Delete_Button" href="javascript:editResultCode(' + index + ')">修改</a>';
     html += '<a class="Delete_Button" href="javascript:deleteResultCode(' + index + ')">删除</a>';
    return html;
}


/**
 * 新增结果码
 */
function addResultCode() {
    showAddDialog("新增结果码",600,350,Globals.baseJspUrl.MONITOR_ACTION_RESULTCODE_MANAGE_JSP,
        function destroy(data){
            if (data == systemVar.SUCCESS) {
                JsVar["resultCodeGrid"].reload();
                showMessageTips("新增成功");
            }
    });
}

/**
 * 修改结果码
 * @param index
 */
function editResultCode(index) {
	var row = JsVar["resultCodeGrid"].getRow(index);
    showEditDialog("修改结果码",600,350,Globals.baseJspUrl.MONITOR_ACTION_RESULTCODE_MANAGE_JSP,
        function destroy(data){
            if (data == systemVar.SUCCESS) {
                JsVar["resultCodeGrid"].reload();
                showMessageTips("修改成功!");
            }
    },row);
}

//删除结果码
function deleteResultCode(index) {
	 var ids = new Array();
	 if(index != null ){
		row = JsVar["resultCodeGrid"].getRow(index);
		ids.push({OCS_RESULT_CODE:row["OCS_RESULT_CODE"],OCP_RESULT_CODE:row["OCP_RESULT_CODE"]});
	}else{
		 var rows = JsVar["resultCodeGrid"].getSelecteds();
		    if (rows.length > 0) {
		    	for(var i=0 ; i<rows.length;i++){
		    		ids.push({OCS_RESULT_CODE:rows[i]["OCS_RESULT_CODE"],OCP_RESULT_CODE:rows[i]["OCP_RESULT_CODE"]});
		    	}
		    }
		    else {
                showWarnMessageTips("请选中一条记录!") ;
		        return;
		    }
	}
   
    
   
    showConfirmMessageAlter("确定删除记录？",function ok(){
    
    	getJsonDataByPost(Globals.baseActionUrl.FRAME_DELETE_OBJECT_URL,ids,"结果码管理-删除结果码",
                function(result){
                    JsVar["resultCodeGrid"].reload();
                    showMessageTips("删除结果码成功!");
                },"monitorMapper.deleteResultCode","anotherDataSource");
    	 
    })
}