/**
 * Created with IntelliJ IDEA.
 * Creater: zhongsixue
 * Date: 16-10-18
 * Time: 下午16:22
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
    //取得程序表格 
    JsVar["runningProgramGrid"] = mini.get("runningProgramGrid");
});
/**
 * 父页面调用
 * @param data
 */
function onLoadComplete(data) {
	var obj = data;
	obj["PROGRAM_CODE"] = data["PROGRAM_CODE_1"];
	JsVar["DATA"] = data;
	//加载运行中程序表格信息
    loadProgramGrid(obj);
}

/**
 * 加载运行中程序表格信息
 */
function loadProgramGrid(gridParam){
	datagridLoad(JsVar["runningProgramGrid"],gridParam,"taskProgram.queryRunningProgramList");
}

/**
 * 表格渲染：状态高亮
 */
function stateRenderer(e){
	var run_pro=e.record.RUN_STATE;
	if(run_pro==null || run_pro<1){
		return "<span class='label label-danger'>&nbsp;未运行&nbsp;</span>";
	}else if(run_pro==1){
		return "<span class='label label-success'>&nbsp;运行中&nbsp;</span>";
	}
}

function launch(){
    var dataList = JsVar["runningProgramGrid"].getData();
    if (!dataList) {
        showWarnMessageTips("没有可升级的版本，请检查！");
    	return;
    }
    var programData = dataList[0];
    
    programData["PROGRAM_GROUP"] = JsVar["DATA"]["PROGRAM_GROUP"];
	getJsonDataByPost(Globals.baseActionUrl.SWITCH_ABM_MASTER_STANDBY_OPT_LAUNCH_ACTION_MANAGE_URL,programData,"主备切换-灰度升级",
		function(result){
			if (result.rstCode == "0") {
                showMessageTips("正式发布成功！");
				closeWindow();
			}
		});
}

/**
 * 关闭
 */
function close(){
	closeWindow();
}