//定义变量， 通常是页面控件和参数
var JsVar = new Object();
$(document).ready(function() {
	mini.parse();
	// 获取页面表单
	JsVar["busMainClusterForm"] = new mini.Form("#busMainClusterForm");
	JsVar["comClusterGrid"] = mini.get("comClusterGrid");
	JsVar["busClusterGrid"] = mini.get("busClusterGrid");
});

// 父页面调用，新增和修改时会初使化一些参数，例如：data.action：操作类型
function onLoadComplete(action, data) {
	JsVar[systemVar.ACTION] = action;

	//获取配置参数
    propParams = getPropListByKey(cfgVar.tools_dir + "," + cfgVar.env_dir);
    
    getPropList();
    
	if (action == systemVar.EDIT) {
		JsVar["BUS_CLUSTER_ID"] = data["BUS_CLUSTER_ID"];

		loadMainClusterInfo(data);

		// 获取业务集群列表
		initBusinessList(false);
		loadBusClusterInfo(data);

		// 获取组件集群列表
		initComponentList(false);
		loadConClusterInfo(data);
	} else {
		// 获取业务集群列表
		initBusinessList(true);
		// 获取组件集群列表
		initComponentList(true);
	}
}

/**
 * 获取部署真实目录
 */
function getPropList() {
	var propParams = getPropListByKey(cfgVar.buss_dir);
	JsVar["BUS_DIR"] = propParams[cfgVar.buss_dir];
}

/**
 * 获取组件集群列表
 */
function initComponentList(syn) {
	var params = {
		TYPE : "1"
	};

	getJsonDataByPost(Globals.baseActionUrl.FRAME_QUERY_FOR_LIST_URL, params,
			"业务集群新增-获取业务集群列表", function(result) {
				if(result){
					JsVar["comClusterGrid"].setData(result);
				}
			}, "serviceType.queryAllClusterCode", "", syn);
}

/**
 * 渲染是否按IP拆分
 * 
 * @param e
 * @returns
 */
function renderDcfClusterElePersonalConf(e) {
	if (e) {
		var datas = getSysDictData('personal_conf_list');
		if (datas) {
			for (var i = 0; i < datas.length; ++i) {
				if (e.value == datas[i]["code"]) {
					return datas[i]["text"];
				}
			}
		}
	}

	return "";
}

/**
 * 渲染集群名称
 * @param e
 */
function renderClusterName(e) {
	var clusterName = e.record.CLUSTER_NAME;
	var clusterType = e.record.CLUSTER_TYPE;
	return clusterName + "（" + clusterType + "）";
}

/**
 * 
 * 
 * @param e
 * @returns
 */
function renderPath(e) {
	var CLUSTER_TYPE = e.record.CLUSTER_TYPE;
	var CLUSTER_ELE_DEFAULT_PATH = e.record.CLUSTER_ELE_DEFAULT_PATH;

	var path = CLUSTER_ELE_DEFAULT_PATH;
	e.record.CLUSTER_DEPLOY_PATH = CLUSTER_ELE_DEFAULT_PATH;
//	if (CLUSTER_ELE_DEFAULT_PATH && CLUSTER_TYPE) {
//		var path = CLUSTER_ELE_DEFAULT_PATH + "/" + CLUSTER_TYPE;
//		e.record.CLUSTER_DEPLOY_PATH = path;
//
//	} else {
//		e.record.CLUSTER_DEPLOY_PATH = path;
//	}
	
	return path;
}

/**
 * 真实部署目录
 * @param e
 */
function renderRealPath(e) {
	var deployPath = e.record.CLUSTER_ELE_DEFAULT_PATH;
	if (!deployPath.endWith("/")) {
		deployPath += "/";
	}
	return deployPath + JsVar["BUS_DIR"];
}


/**
 * 获取业务集群列表
 */
function initBusinessList(syn) {
	var params = {
		TYPE : busVar.BUSINESS_TYPE
	};
	getJsonDataByPost(Globals.baseActionUrl.FRAME_QUERY_FOR_LIST_URL, params,
			"业务集群新增-获取业务集群列表", function(result) {
				if(result){
					JsVar["busClusterGrid"].setData(result);
				}
			}, "clusterEleDefine.queryClusterEleList", "", syn);
}

// 新增和修改点确认按钮保存
function onSubmit() {
	if (JsVar[systemVar.ACTION] == systemVar.EDIT) {
		update();
	} else {
		add();
	}
}

/**
 * 添加业务集群关联
 * 
 * @returns
 */
function add() {
	// 判断是否有效
	JsVar["busMainClusterForm"].validate();
	if (JsVar["busMainClusterForm"].isValid() == false) {
		return;
	}
	// 新增操作下获取表单的数据
	var busMainForm = JsVar["busMainClusterForm"].getData();
	// 获取业务集群
	var businessList = JsVar["busClusterGrid"].getSelecteds();
	// 获取组件集群
	var componentList = JsVar["comClusterGrid"].getSelecteds();

	var BUSINESS_LIST = new Array();
	for (var i = 0; i < businessList.length; ++i) {
		BUSINESS_LIST.push({
			CLUSTER_ID : businessList[i]["CLUSTER_ID"],
			CLUSTER_PARENT_TYPE : 3,
			CLUSTER_TYPE : businessList[i]["CLUSTER_TYPE"],
			CLUSTER_NAME : businessList[i]["CLUSTER_NAME"],
			TYPE : businessList[i]["CLUSTER_ELE_TYPE"],
			PERSONAL_CONF : businessList[i]["CLUSTER_ELE_PERSONAL_CONF"],
			CLUSTER_DEPLOY_PATH : businessList[i]["CLUSTER_DEPLOY_PATH"]
		});
	}

	var COMPONENT_LIST = new Array();
	for (var i = 0; i < componentList.length; ++i) {
		COMPONENT_LIST.push({
			CLUSTER_ID : componentList[i]["CLUSTER_ID"],
			CLUSTER_TYPE : componentList[i]["CLUSTER_TYPE"],
			CLUSTER_PARENT_TYPE : 1
		});
	}

	var params = {
		BUS_CLUSTER_CODE : busMainForm["BUS_CLUSTER_CODE"],
		BUS_CLUSTER_NAME : busMainForm["BUS_CLUSTER_NAME"],
		BUS_CLUSTER_TYPE : busMainForm["BUS_CLUSTER_TYPE"],
		BUS_CLUSTER_SEQ : busMainForm["BUS_CLUSTER_SEQ"],
		BUSINESS_LIST : BUSINESS_LIST,
		COMPONENT_LIST : COMPONENT_LIST,
		TYPE : 3
	};

	// 校验
	var validate = false;
	var parameter = {
		BUS_CLUSTER_NAME : busMainForm["BUS_CLUSTER_NAME"],
		BUS_CLUSTER_CODE : busMainForm["BUS_CLUSTER_CODE"]
	};
	getJsonDataByPost(Globals.baseActionUrl.FRAME_QUERY_FOR_LIST_URL,
			parameter, "业务集群新增-获取业务集群唯一编码", function(result) {
				if (result && result.length == 0) {
					validate = true;
				}
			}, "busMainCluster.queryMainBusCode", "", false);

	// 验证不通过
	if (validate == false) {
        showWarnMessageTips("集群编码已经存在！");
		return;
	}

	// 添加业务集群信息
	getJsonDataByPost(Globals.baseActionUrl.BUS_MAIN_CLUSTER_ACTION_ADD_URL,
			params, "集群管理-修改业务主集群", function success(result) {
				closeWindow(systemVar.SUCCESS);
			});

}

/**
 * 更新业务集群数据
 * 
 * @returns
 */
function update() {
	// 新增操作下获取表单的数据
	var busMainForm = JsVar["busMainClusterForm"].getData();
	// 获取业务集群
	var businessList = JsVar["busClusterGrid"].getSelecteds();
	// 获取组件集群
	var componentList = JsVar["comClusterGrid"].getSelecteds();

	var BUSINESS_LIST = new Array();
	
	for (var i = 0; i < businessList.length; ++i) {
		BUSINESS_LIST.push({
			BUS_CLUSTER_ID : JsVar["BUS_CLUSTER_ID"],
			CLUSTER_ID : businessList[i]["CLUSTER_ID"],
			CLUSTER_PARENT_TYPE : 3,
			CLUSTER_TYPE : businessList[i]["CLUSTER_TYPE"],
			CLUSTER_NAME : businessList[i]["CLUSTER_NAME"],
			TYPE : businessList[i]["CLUSTER_ELE_TYPE"],
			PERSONAL_CONF : businessList[i]["CLUSTER_ELE_PERSONAL_CONF"],
			CLUSTER_DEPLOY_PATH : businessList[i]["CLUSTER_DEPLOY_PATH"]
		});
	}

	var COMPONENT_LIST = new Array();
	for (var i = 0; i < componentList.length; ++i) {
		COMPONENT_LIST.push({
			BUS_CLUSTER_ID : JsVar["BUS_CLUSTER_ID"],
			CLUSTER_ID : componentList[i]["CLUSTER_ID"],
			CLUSTER_TYPE : componentList[i]["CLUSTER_TYPE"],
			CLUSTER_PARENT_TYPE : 1
		});
	}

	var params = {
		BUS_CLUSTER_ID : JsVar["BUS_CLUSTER_ID"],
		BUS_CLUSTER_CODE : busMainForm["BUS_CLUSTER_CODE"],
		BUS_CLUSTER_NAME : busMainForm["BUS_CLUSTER_NAME"],
		BUS_CLUSTER_TYPE : busMainForm["BUS_CLUSTER_TYPE"],
        BUS_CLUSTER_SEQ : busMainForm["BUS_CLUSTER_SEQ"],
		BUSINESS_LIST : BUSINESS_LIST,
		COMPONENT_LIST : COMPONENT_LIST,
		TYPE : 3
	};

	getJsonDataByPost(Globals.baseActionUrl.BUS_MAIN_CLUSTER_ACTION_EDIT_URL,
			params, "集群管理-修改业务主集群", function success(result) {
				closeWindow(systemVar.SUCCESS);
			});

}

/**
 * 加载主集群数据
 * 
 * @param data
 */
function loadMainClusterInfo(data) {
	var params = {
		BUS_CLUSTER_ID : data["BUS_CLUSTER_ID"]
	};

	getJsonDataByPost(Globals.baseActionUrl.FRAME_QUERY_FOR_OBJECT_URL, params,
			"业务集群修改-修改主集群数据", function(result) {
				if (result) {
					JsVar["busMainClusterForm"].setData(result);
				}
			}, "busMainCluster.queryUpdateMainClusterInfo");
}

/**
 * 加载业务集群数据
 * 
 * @param data
 */
function loadBusClusterInfo(data) {

	var params = {
		CLUSTER_PARENT_TYPE : "3",
		BUS_CLUSTER_ID : JsVar["BUS_CLUSTER_ID"]
	};

	getJsonDataByPost(
			Globals.baseActionUrl.FRAME_QUERY_FOR_LIST_URL,
			params,
			"业务集群修改-获取组件集群数据",
			function(result) {
				if (result) {
					JsVar["busClusterGrid"]
							.findRows(function(row) {
								for (var i = 0; i < result.length; ++i) {
									if (row["CLUSTER_TYPE"] == result[i]["CLUSTER_TYPE"]) {
										var realPath = result[i]["CLUSTER_DEPLOY_PATH"];
//										realPath = realPath.substring(0,
//												realPath.lastIndexOf("/"));

										var newRow = {
											"CLUSTER_ID" : result[i]["CLUSTER_ID"],
											"CLUSTER_NAME" : result[i]["CLUSTER_NAME"],
											"CLUSTER_ELE_DEFAULT_PATH" : realPath
										};

										JsVar["busClusterGrid"].select(row);
										JsVar["busClusterGrid"].updateRow(row,newRow);
										break;
									}
								}

							});
				}
			}, "serviceType.queryBusServiceTypeList");
}

/**
 * 加载组件集群数据
 * 
 * @param data
 */
function loadConClusterInfo(data) {
	var params = {
		CLUSTER_PARENT_TYPE : "1",
		BUS_CLUSTER_ID : JsVar["BUS_CLUSTER_ID"]
	};

	getJsonDataByPost(
			Globals.baseActionUrl.FRAME_QUERY_FOR_LIST_URL,
			params,
			"业务集群新增-获取业务集群列表",
			function(result) {
				if (result) {
					JsVar["comClusterGrid"]
							.findRows(function(row) {
								for (var i = 0; i < result.length; ++i) {
									if (row["CLUSTER_ID"] == result[i]["CLUSTER_ID"]) {
										JsVar["comClusterGrid"].select(row);
										break;
									}
								}

							});
				}
			}, "busRelationClusterList.queryComponentClusterByBusClusterId");

}
