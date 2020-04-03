/**
 * Created with IntelliJ IDEA.
 * Creater: zhongsixue
 * Date: 16-10-17
 * Time: 上午11:14
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
    JsVar["runningProgramGrid"] = mini.get("runningProgramGrid");
    //取得查询表单
    JsVar["queryForm"] = new mini.Form("#queryForm");
    //加载tab页
    //loadingClusterTab();
});

/**
 * 加载业务主集群
 */
function loadingClusterTab() {
	getJsonDataByPost(Globals.baseActionUrl.FRAME_QUERY_FOR_LIST_URL, null, "集群划分-查询所有业务主集群",
        function(result){
		var tab_str="";
		if(result.length>0){
			var tabs=mini.get("#switchTabs");
			$.each(result, function (i, item) {
				var tab = {
					title:item.BUS_CLUSTER_NAME,
					id:item.BUS_CLUSTER_ID,
					code:item.BUS_CLUSTER_CODE,
					dataField:item.ID, 
					showCloseButton: false
				};
				tabs.addTab(tab);
            });
			//给第一个tab加上active动作
			tabs.setActiveIndex(0);
		}
    },"busMainCluster.queryBusMainClusterList");
}

/**
 * tab页切换函数：根据集群id加载对应表格
 * @param item
 */
function loadPage(item){
	JsVar["BUS_CLUSTER_ID"] = item["BUS_CLUSTER_ID"];
	JsVar["BUS_CLUSTER_CODE"] = item["BUS_CLUSTER_CODE"];
	JsVar["BUS_CLUSTER_NAME"] = item["BUS_CLUSTER_NAME"];
	//该版本切换使用的配置文件
	JsVar["SWITCH_CONFIG_FILE"] = item["SWITCH_CONFIG_FILE"];
	//该版本切换对应的
	JsVar["SWITCH_CLUSTER_TYPE"] = item["SWITCH_CLUSTER_TYPE"];
	//程序状态初始化为1（正在运行）
	var params = {
		BUS_CLUSTER_ID:JsVar["BUS_CLUSTER_ID"],
		RUN_STATE:1
	};
	loadRunningProGrid(params);
}

/**
 * 模糊查询
 */
function likeSearch(){
	var paramsObj = JsVar["queryForm"].getData();
	paramsObj.BUS_CLUSTER_ID = JsVar["BUS_CLUSTER_ID"];
	//程序状态初始化为1（正在运行）
	paramsObj.RUN_STATE=1;
	loadRunningProGrid(paramsObj);
}

/**
 * 加载运行程序表格，参数：CLUSTER_ID,RUN_STATE
 */
function loadRunningProGrid(gridParam){
	//加载表格信息
	datagridLoadPage(JsVar["runningProgramGrid"], gridParam, "taskProgram.queryRunningProgramList");
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
 * 表格渲染：操作
 */
function optionRenderer(e){
	var index = e.rowIndex;
	var rownum = e.record.ROWNUM;
	var ACTION_FLAG = e.record.ACTION_FLAG;
	var html= '';
	if(rownum <2 ){
		if(ACTION_FLAG == '2' || ACTION_FLAG==null){
			html += '<a class="Delete_Button" id="row_0_'+index+'" href="javascript:greyUpgrade(' + index + ')">灰度升级</a>';
		} else if (ACTION_FLAG == '0'){
			html += '<a class="Delete_Button" id="row_3_'+index+'" href="javascript:rollback(' + index + ',\'row1back\')">回退</a>';
		}
	} else {
		if(ACTION_FLAG == '1'){
			html += '<a class="Delete_Button" id="row_2_'+index+'" href="javascript:rollback(' + index + ')">回退</a>';
		}else{
			html += '<a class="Delete_Button" id="row_3_'+index+'" href="javascript:updateGrayConfig(' + index + ')">灰度配置修改</a>';
			html += '<a class="Delete_Button" id="row_1_'+index+'" href="javascript:officialLaunch(' + index + ')">正式发布</a>';
		}
	}
 
	return html;
}
/**
 * 表格渲染：操作
 */
function messageRenderer(e){
	var rownum = e.record.ROWNUM;
	var ACTION_FLAG = e.record.ACTION_FLAG;
	
	if(rownum <2 ){
		 if(ACTION_FLAG =='1'){
			return '<span class="label label-danger">'+ '&nbsp;无业务&nbsp;</span>';
		 }else{
			 return '<span class="label label-success">'+ '&nbsp;正常运行&nbsp;</span>';
		 }
	}else{
		if(ACTION_FLAG == '1'){
			return  '<span class="label label-success">'+ '&nbsp;正常运行&nbsp;</span>';
		}else if(ACTION_FLAG == '2'){
			return  '<span class="label label-danger">'+ '&nbsp;无业务&emsp;&nbsp;</span>';
		}else{
			return  '<span class="label label-warning">'+ '&nbsp;试运行中&nbsp;</span>';
		}
	}
}

/**
 * 灰度配置修改
 */
function updateGrayConfig(index) {
	var rowInfo = JsVar["runningProgramGrid"].getRow(index);
	var gridParam=new Object();
	//程序状态初始化为1（正在运行）
	gridParam.RUN_STATE=1;
	//父页面传参--不需要展示的program
	//当前程序对应的集群ID,主机和Jstorm集群ID区分
	gridParam.CLUSTER_ID = rowInfo["CLUSTER_ID"];
	gridParam.ID = rowInfo.ID;
	gridParam.CLUSTER_TYPE = rowInfo.CLUSTER_TYPE;
	gridParam.PROGRAM_CODE_1 = rowInfo.PROGRAM_CODE;
	gridParam.PROGRAM_GROUP = rowInfo.PROGRAM_GROUP;
	//包类型
	gridParam.PACKAGE_TYPE = rowInfo.PACKAGE_TYPE;
	//业务集群ID
	gridParam.BUS_CLUSTER_ID = JsVar["BUS_CLUSTER_ID"];
	//版本切换配置文件
	gridParam.SWITCH_CONFIG_FILE = JsVar["SWITCH_CONFIG_FILE"];
	//版本切换集群类型
	gridParam.SWITCH_CLUSTER_TYPE = JsVar["SWITCH_CLUSTER_TYPE"];
	
	showDialog("主备切换--灰度配置文件修改",700,550,Globals.baseJspUrl.SWITCH_PROGRAM_UPGRADE_CONFIG_JSP_MANAGE_URL,
	    function destroy(data){
			var params = {
				BUS_CLUSTER_ID:JsVar["BUS_CLUSTER_ID"],
				RUN_STATE:"1"
			};
			loadRunningProGrid(params);
	},gridParam);
}

/**
 *  灰度升级Gray released in computer terms
 */
function greyUpgrade(index){
	var rowInfo = JsVar["runningProgramGrid"].getRow(index);
	var gridParam=new Object();
	//程序状态初始化为1（正在运行）
	gridParam.RUN_STATE=1;
	//父页面传参--不需要展示的program
	gridParam.CLUSTER_ID = rowInfo.CLUSTER_ID;
	//程序ID
	gridParam.ID = rowInfo.ID;
	//集群类型
	gridParam.CLUSTER_TYPE = rowInfo.CLUSTER_TYPE;
	//程序编码
	gridParam.PROGRAM_CODE = null;
	//集群编码
	gridParam.CLUSTER_CODE = rowInfo.CLUSTER_CODE;
	//业务包名称
	gridParam.PACKAGE_NAME = rowInfo.PACKAGE_NAME;
	//所属组
	gridParam.PROGRAM_GROUP = rowInfo.PROGRAM_GROUP;
	//业务集群ID
	gridParam.BUS_CLUSTER_ID = JsVar["BUS_CLUSTER_ID"];
	//包类型
	gridParam.PACKAGE_TYPE = rowInfo.PACKAGE_TYPE;
	//版本切换配置文件
	gridParam.SWITCH_CONFIG_FILE = JsVar["SWITCH_CONFIG_FILE"];
	//版本切换集群类型
	gridParam.SWITCH_CLUSTER_TYPE = JsVar["SWITCH_CLUSTER_TYPE"];
	
	var flag=false;
	getJsonDataByPost(Globals.baseActionUrl.FRAME_QUERY_FOR_LIST_URL,gridParam,"主备切换-灰度升级--查询备用Topology",
		function(result){
			if(result.length>0){
				flag=true;
			}
	},"taskProgram.queryRunningProgramList",null,false);
	
	if(!flag){
        showWarnMessageTips("没有正在运行的备用Topology，无法进行灰度升级操作。");
		return;
	}
	showDialog("主备切换--灰度升级",700,550,Globals.baseJspUrl.SWITCH_PROGRAM_UPGRADE_JSP_MANAGE_URL,
	    function destroy(data){
			var params = {
				BUS_CLUSTER_ID:JsVar["BUS_CLUSTER_ID"],
				RUN_STATE:"1"
			};
			loadRunningProGrid(params);
	},gridParam);
}

/**
 * 正式发布
 */
function officialLaunch(index){
	var rowInfo = JsVar["runningProgramGrid"].getRow(index);
	var gridParam=new Object();
	//程序状态初始化为1（正在运行）
	gridParam.RUN_STATE=1;
	//父页面传参--不需要展示的program
	gridParam.CLUSTER_ID = rowInfo.CLUSTER_ID;
	gridParam.ID = rowInfo.ID;
	//集群类型
	gridParam.CLUSTER_TYPE = rowInfo.CLUSTER_TYPE;
	//程序编码
	gridParam.PROGRAM_CODE = null;
	//集群编码
	gridParam.CLUSTER_CODE = rowInfo.CLUSTER_CODE;
	//业务包名称
	gridParam.PACKAGE_NAME = rowInfo.PACKAGE_NAME;
	//所属组
	gridParam.PROGRAM_GROUP = rowInfo.PROGRAM_GROUP;
	//业务集群ID
	gridParam.BUS_CLUSTER_ID = JsVar["BUS_CLUSTER_ID"];
	//包类型
	gridParam.PACKAGE_TYPE = rowInfo.PACKAGE_TYPE;
	//版本切换配置文件
	gridParam.SWITCH_CONFIG_FILE = JsVar["SWITCH_CONFIG_FILE"];
	//版本切换集群类型
	gridParam.SWITCH_CLUSTER_TYPE = JsVar["SWITCH_CLUSTER_TYPE"];
	var flag=false;
	getJsonDataByPost(Globals.baseActionUrl.FRAME_QUERY_FOR_LIST_URL,gridParam,"主备切换-正式发布--查询备用Topology",
			function(result){
				if(result.length>0){
					flag=true;
				}
	},"taskProgram.queryRunningProgramList",null,false);
	
	if(!flag){
        showWarnMessageTips("没有正在运行的备用Topology，无法进行正式发布操作。");
		return;
	}
	showConfirmMessageAlter("确定开始正式发布？",function ok(){
		getJsonDataByPost(Globals.baseActionUrl.SWITCH_MASTER_STANDBY_OPT_LAUNCH_ACTION_MANAGE_URL,gridParam,"主备切换-灰度升级",
			function(result){
				if (result.rstCode == "0") {
                    showMessageTips("修改正式发布配置文件成功！");
					var params = {
						BUS_CLUSTER_ID:JsVar["BUS_CLUSTER_ID"],
						RUN_STATE:"1"
					};
					loadRunningProGrid(params);
				}
			});
	});
}

/**
 * 回退
 */
function rollback(index, actionFlag){
	var rowInfo = JsVar["runningProgramGrid"].getRow(index);
	var gridParam=new Object();
	//程序状态初始化为1（正在运行）
	gridParam.RUN_STATE=1;
	//父页面传参--不需要展示的program
	gridParam.CLUSTER_ID = rowInfo.CLUSTER_ID;
	gridParam.ID = rowInfo.ID;
	//程序类型
	gridParam.CLUSTER_TYPE = rowInfo.CLUSTER_TYPE;
	//程序所属组
	gridParam.PROGRAM_GROUP = rowInfo.PROGRAM_GROUP;
	//业务主集群ID
	gridParam.BUS_CLUSTER_ID = JsVar["BUS_CLUSTER_ID"];
	//包类型
	gridParam.PACKAGE_TYPE = rowInfo.PACKAGE_TYPE;
	//版本切换配置文件
	gridParam.SWITCH_CONFIG_FILE = JsVar["SWITCH_CONFIG_FILE"];
	//版本切换集群类型
	gridParam.SWITCH_CLUSTER_TYPE = JsVar["SWITCH_CLUSTER_TYPE"];
	
	gridParam.actionFlag = actionFlag;
	var flag=false;
	getJsonDataByPost(Globals.baseActionUrl.FRAME_QUERY_FOR_LIST_URL,gridParam,"回退--查询备用Topology",
			function(result){
				if(result.length>0){
					flag=true;
				}
	},"taskProgram.queryRunningProgramList",null,false);
	
	if(!flag){
        showWarnMessageTips("没有正在运行的备用Topology，无法进行回退操作。");
		return;
	}
	gridParam.PROGRAM_CODE_1=rowInfo.PROGRAM_CODE;
	showDialog("主备切换--回退",700,400,Globals.baseJspUrl.SWITCH_PROGRAM_ROLLBACK_JSP_MANAGE_URL,
	    function destroy(data){
			var params = {
				BUS_CLUSTER_ID:JsVar["BUS_CLUSTER_ID"],
				RUN_STATE:"1"
			};
			loadRunningProGrid(params);
	},gridParam);
}