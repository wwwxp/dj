/**
 * Created with IntelliJ IDEA.
 * Creater: zhongsixue
 * Date: 16-10-17
 * Time: 下午15:20
 * To change this template use File | Settings | File Templates.
 */

/**
 * 定义变量， 通常是页面控件和参数
 */
var JsVar = new Object();
var netElementData=new Object();
var busTypeData=new Object();
/**
 * 初始化
 */
$(document).ready(function () {
    mini.parse();
    //取得程序表格 
    JsVar["runningProgramGrid"] = mini.get("runningProgramGrid");
    JsVar["numDatagrid"]=mini.get("numDatagrid");
    JsVar["paramForm"]= new mini.Form("#paramForm");
    JsVar["net_element"]= mini.get("net_element");
    //事先获取业务类型下拉框值
    getBusTypeData();
    
});
/**
 * 父页面调用
 * @param data
 */
function onLoadComplete(data) {
	JsVar["DATA"] = data;
	var PROGRAM_CODE=data.PROGRAM_CODE;
	//加载运行中程序表格信息
    loadProgramGrid(data);
    //加载sp_switch.xml已有号段信息
    getSpSwitchInfo(PROGRAM_CODE);
    //获取网元下拉框值
    getNetElementData();
}

/**
 * 加载运行中程序表格信息
 */
function loadProgramGrid(gridParam){
	gridParam["PROGRAM_CODE"] = null;
	datagridLoad(JsVar["runningProgramGrid"], gridParam, "taskProgram.queryRunningProgramList");
}

/**
 * 获取网元下拉框值
 */
function getNetElementData(){
	var params = {
		CLUSTER_ID:JsVar["DATA"]["CLUSTER_ID"],
		CLUSTER_CODE:JsVar["DATA"]["CLUSTER_CODE"],
		PACKAGE_NAME:JsVar["DATA"]["PACKAGE_NAME"],
		//程序所属组 
		PROGRAM_GROUP:JsVar["DATA"]["PROGRAM_GROUP"],
		//业务包类型
		PACKAGE_TYPE:JsVar["DATA"]["PACKAGE_TYPE"],
		//版本切换使用配置文件
		SWITCH_CONFIG_FILE:JsVar["DATA"]["SWITCH_CONFIG_FILE"]
	};
	getJsonDataByPost(Globals.baseActionUrl.SWITCH_MASTER_STANDBY_UPGRADE_NET_VALUE_ACTION_MANAGE_URL, params, "灰度升级--查询网元下拉框值",
		function(result){
			if(result.length>0){
				netElementData=result;
				JsVar["net_element"].setData(netElementData);
			}
	});
	
}

/**
 * 获取业务类型下拉框值
 */
function getBusTypeData(){
	var queryParam=new Object();
	queryParam.GROUP_CODE="WEB_SP_SWITCH_BUSINESS";
	getJsonDataByPost(Globals.baseActionUrl.FRAME_QUERY_FOR_LIST_URL,queryParam,"灰度升级--查询业务类型下拉框值",
		function(result){
			if(result.length>0){
				busTypeData=result;
			}
	},"config.queryConfigList");
}

/**
 *获取sp_switch.xml相关信息
 */
function getSpSwitchInfo(PROGRAM_CODE){
	var params = {
		PROGRAM_CODE:PROGRAM_CODE,
		PROGRAM_GROUP:JsVar["DATA"]["PROGRAM_GROUP"],
		PACKAGE_TYPE:JsVar["DATA"]["PACKAGE_TYPE"],
		//业务包类型
		PACKAGE_TYPE:JsVar["DATA"]["PACKAGE_TYPE"],
		//版本切换使用配置文件
		SWITCH_CONFIG_FILE:JsVar["DATA"]["SWITCH_CONFIG_FILE"]
	};
	getJsonDataByPost(Globals.baseActionUrl.SWITCH_MASTER_STANDBY_UPGRADE_NUM_INFO_ACTION_MANAGE_URL, params, "灰度升级--查询已存在号段",
		function(result){
		//初始信息为网元
		if(result.netInfo!=null){
			var netList=result.netInfo;
			var netStr=netList.join(",");
			JsVar["net_element"].setValue(netStr);
			$("input[name='searchRadio'][value='ele']").attr("checked",true); 
		}else if(result.numInfo!=null){//初始信息为号段 
			var numList=result.numInfo;
			JsVar["numDatagrid"].setData(numList);
			for(var i=0;i<numList.length;i++){
				JsVar["numDatagrid"].beginEditRow(JsVar["numDatagrid"].getRow(i));
			}
			$("input[name='searchRadio'][value='num']").attr("checked",true); 
		}else{
			$("input[name='searchRadio'][value='num']").attr("checked",true); 
		}
	});
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

/**
 * 提交到后台
 */
function submitUpgrade(){
	//传到后台的值
	var upgradeParams=new Object();
	//判断是否有效
    JsVar["paramForm"].validate();
    if (JsVar["paramForm"].isValid() == false){
        return;
    }
    //top选择：取值
    //var programData = JsVar["runningProgramGrid"].getSelected();
    //if(programData==null){
    //	showWarnMessageAlter("请选中一条TOP信息！");
    //	return;
    //}
    
    var dataList = JsVar["runningProgramGrid"].getData();
    if (!dataList) {
        showWarnMessageTips("没有可升级的版本，请检查！");
    	return;
    }
    upgradeParams = dataList[0];
    //条件选择:获取要选择的类型
	var radioType=$("input[name='searchRadio']:checked").val();
	var formData=JsVar["paramForm"].getData();
	var numData=JsVar["numDatagrid"].getEditData(true);
	upgradeParams["radioType"]=radioType;
	if(radioType=="num"){//按号段
		if(numData.length<1){
            showWarnMessageTips("请添加号段信息！");
			return;
		}
		for(var i=0;i<numData.length;i++){
			if(numData[i].startNum=="" || numData[i].endNum==""){
                showWarnMessageTips("请填写开始号段和结束号段！");
		    	return;
			}else if(numData[i].startNum>numData[i].endNum){
                showWarnMessageTips("开始号段不能大于结束号段！");
		    	return;
			}else if(numData[i].busType==""){
                showWarnMessageTips("请选择类型！");
		    	return;
			}
		}
		upgradeParams["numData"]=numData;
	}else if(radioType=="ele"){//按网元
		if(formData.net_element==""){
            showWarnMessageTips("请选择网元！");
	    	return;
		}
		var netStr=formData.net_element;
		var netArry=netStr.split(",");		
		upgradeParams["net_element"]=netArry;
	}else{
        showWarnMessageTips("请选择号段或网元！");
		return ;
	}
	
	//灰度升级所属Group
	upgradeParams["PROGRAM_GROUP"] = JsVar["DATA"]["PROGRAM_GROUP"];
	
	//业务主集群ID
	upgradeParams["BUS_CLUSTER_ID"] = JsVar["DATA"]["BUS_CLUSTER_ID"];
	
	//灰度升级配置文件
	upgradeParams["SWITCH_CONFIG_FILE"] = JsVar["DATA"]["SWITCH_CONFIG_FILE"];
	
	//版本切换集群类型
	upgradeParams["SWITCH_CLUSTER_TYPE"] = JsVar["DATA"]["SWITCH_CLUSTER_TYPE"];
	
	//提交到后台处理
	getJsonDataByPost(Globals.baseActionUrl.SWITCH_MASTER_STANDBY_OPT_UPGRADE_ACTION_MANAGE_URL,upgradeParams,"主备切换-灰度升级",
		function(result){
			if (result.rstCode == "0") {
                showMessageTips("修改灰度升级配置成功！");
				closeWindow();
			}
		}
	);
}

/**
 * 添加行的方法
 */
function addRow(){
	var row = {};
	JsVar["numDatagrid"].addRow(row);
	JsVar["numDatagrid"].beginEditRow(row);
}

/**
 * 业务类型渲染
 */
function busTypeRenderer(){
	
}

/**
 * 操作渲染
 * @returns
 */
function onActionRenderer(e){
    var uid = e.record._uid;
	var html="";
	html+='<div class="icon-remove" title="删除该行" style="cursor:pointer;margin-left:15px;height:16px;width:16px;" onclick="removeRow('+uid+')">';
	return html;
}

/**
 * 删除该行
 */
function removeRow(uid){
	var row = JsVar["numDatagrid"].getRowByUID(uid);
	JsVar["numDatagrid"].removeRow(row);
}

/**
 * 关闭
 */
function close(){
	closeWindow();
}