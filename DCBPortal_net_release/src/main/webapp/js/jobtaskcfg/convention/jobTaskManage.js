/**
 * Created with IntelliJ IDEA.
 * Creater: zhongsixue
 * Date: 16-10-18
 * Time: 下午17：06
 * To change this template use File | Settings | File Templates.
 */
/**
 * 定义变量， 通常是页面控件和参数
 */
var JsVar = new Object();


/**
 * 初始化
 */
$(document).ready(function () {
    mini.parse();
 	//表格获取
    JsVar["datagrid"] = mini.get("datagrid");
    search();
     
});

  

/**
 * 查询
 */
function search() {
	loaddatagrid({TYPE_MODE:"1"});
}

/**
 * 加载常规任务表格
 */
function loaddatagrid(paramsObj){
	//加载表格信息
	datagridLoadPage(JsVar["datagrid"],paramsObj,"jobtaskcfg.queryTaskList");
}

/**
 * 状态渲染
 */
function stateRenderer(e){
	var STATE = e.record.STATE;
	if(STATE==1){
		return "有效";
	}else{
		return "无效";
	}
}

function execTypeRenderer(e){
	var EXEC_TYPE = e.record.EXEC_TYPE;
	if(EXEC_TYPE == 'cmd'){
		return "命令模式";
	}else{
		return "数据库模式";
	}
}
function isalarmRenderer(e){
	var IS_ALARM = e.record.IS_ALARM;
	if(IS_ALARM == 'yes'){
		return "是";
	}else{
		return "否";
	} 
}

function paramsRenderer(e){
	var EXEC_TYPE = e.record.EXEC_TYPE;
	if(EXEC_TYPE == 'cmd'){
		return e.record.CMD_NAME;
	}else{
		return e.record.TASK_CONTENT;
	}
}

/**
 * 操作渲染
 */
function optionRenderer(e){
	var index = e.rowIndex;
	var html="";
	html += '<a class="Delete_Button" href="javascript:immediately(' + index + ')">立刻执行</a>';
    html += '<a class="Delete_Button" href="javascript:edit(' + index + ')">修改</a>';
    html += '<a class="Delete_Button" href="javascript:del(' + index + ')">删除</a>';
    html += '<a class="Delete_Button" href="javascript:queryLog(' + index + ')">日志</a>';
    return html;
}
/**
 * 立刻执行
 * @param index
 */
function immediately(index){
	var row = JsVar["datagrid"].getRow(index);
	getJsonDataByPost(Globals.baseActionUrl.JOBTASKCFG_ACTION_EXEC_URL,[{ID:row["ID"]}],"常规任务管理-执行"+row["TASK_NAME"],
	        function success(result){
                showMessageTips("任务执行成功");
	       });
}
/**
 * 新增常规任务
 */
function add() {
    showAddDialog("常规任务配置--新增",500,350,Globals.baseJspUrl.JOBTASKCFG_JSP_ADD_EDIT_URL,
        function destroy(data){
            if (data == systemVar.SUCCESS) {
                JsVar["datagrid"].reload();
                showMessageTips("新增成功");
            }
    });
}

/**
 * 修改常规任务
 * @param index
 */
function edit(index) {
	var row = JsVar["datagrid"].getRow(index);
	showEditDialog("常规任务配置--修改",500,350,Globals.baseJspUrl.JOBTASKCFG_JSP_ADD_EDIT_URL,
	        function destroy(data){
	            if (data == systemVar.SUCCESS) {
	                JsVar["datagrid"].reload();
                    showMessageTips("修改成功!");
	            }
	    },row);
}

/**
 * 删除常规任务
 * @param index
 */
function del(index) {
	var row = JsVar["datagrid"].getRow(index);
	var  ids = new Array();
	ids.push({ID:row["ID"]});
	showConfirmMessageAlter("确定删除记录?",function ok(){
		getJsonDataByPost(Globals.baseActionUrl.FRAME_DELETE_OBJECT_URL,ids,"常规任务配置--删除",
            function(result){
				JsVar["datagrid"].reload();
                showMessageTips("删除常规任务成功!");
        },"jobtaskcfg.delTask");
	});
}

/**
 * 查看日志
 * @param index
 */
function queryLog(index) {
	var row = JsVar["datagrid"].getRow(index);
	showDialog("日志查看",1000,600,Globals.baseJspUrl.JOBTIMERTASKCFG_JSP_QUERYLOG_CRON_URL,
	        function destroy(data){
				JsVar["datagrid"].reload();
	    },row);
}
