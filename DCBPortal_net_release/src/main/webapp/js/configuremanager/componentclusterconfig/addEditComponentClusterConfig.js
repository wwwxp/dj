//定义变量， 通常是页面控件和参数
var JsVar = new Object();
var hostIP;
var sshUser;

var propParams = new Object();
$(document).ready(function () {
    mini.parse();
    //获取页面表单
    JsVar["serviceTypeForm"] = new mini.Form("#serviceTypeForm");
    //获取配置项Grid对象
    JsVar["configGrid"] = mini.get("configGrid");
    //获取配置参数
    propParams = getPropListByKey(cfgVar.tools_dir + "," + cfgVar.env_dir);
    //加载集群类型
    loadClusterTYpe();
});

/**
 * 加载集群类型
 */
function loadClusterTYpe() {
	var params = {
		TYPE: busVar.COMPONENT_TYPE
	};
	comboxLoad(mini.get("CLUSTER_TYPE"), params, "clusterEleDefine.queryClusterEleList");
}

//父页面调用，新增和修改时会初使化一些参数，例如：data.action：操作类型
function onLoadComplete(action,data) {
    JsVar[systemVar.ACTION] = action;
    JsVar["DO_CFG"] = systemVar.ADD;
    if (action == systemVar.EDIT) {
    	JsVar["DO_CFG"] = systemVar.EDIT;
    	initData(data);
    	
    	//加载组件参数
        loadComponentsParamsList();
    }
}

//新增和修改点确认按钮保存
function onSubmit() {
    if (JsVar[systemVar.ACTION] == systemVar.EDIT) {
        update();
    } else {
        save();
    }
}

/**
 * 组件类型变更
 * @param e
 */
function changeClusterType(e) {
	var clusterType = mini.get("CLUSTER_TYPE").getValue();
	//是否需要配置M2DB实例
	if (clusterType == busVar.M2DB) {
		$("#m2dbTr").css("display", "");
		mini.get("M2DB_INSTANCE").setRequired(true);
	} else {
		$("#m2dbTr").css("display", "none");
	}
	mini.parse();
	
	//加载组件参数
    loadComponentsParamsList();
}

/**
 * 切换组件类型
 * @param e
 */
function changeDeployPath(e) {
	var clusterDeployPath = "";
	var deployPath = mini.get("DEPLOY_PATH").getValue();
	if (!deployPath.startWith("/")) {
		clusterDeployPath += "/";
	}
	clusterDeployPath += deployPath;
	
	 
	JsVar["CLUSTER_DEPLOY_PATH"] = clusterDeployPath;
	var clusterType = mini.get("CLUSTER_TYPE").getValue();
	//获取真实目录信息
	var toolsDir = propParams[cfgVar.tools_dir];
	if (!propParams[cfgVar.tools_dir].endWith("/")) {
		toolsDir = propParams[cfgVar.tools_dir] + "/";
	}
	var envDir = propParams[cfgVar.env_dir];
	if (!propParams[cfgVar.env_dir].endWith("/")) {
		envDir = propParams[cfgVar.env_dir] + "/";
	}
	if (!clusterDeployPath.endWith("/")) {
		clusterDeployPath += "/" + toolsDir +  envDir + "版本号/" + clusterType;
	}else{
		clusterDeployPath += toolsDir + envDir + "版本号/" + clusterType;
	}
    mini.get("CLUSTER_DEPLOY_PATH").setValue(clusterDeployPath);
}

//点确认新增主机
function save() {
    //判断是否有效
    JsVar["serviceTypeForm"].validate();
    if (JsVar["serviceTypeForm"].isValid() == false){
        return;
    }
    
    //新增操作下获取表单的数据 
    var serviceTypeData = JsVar["serviceTypeForm"].getData();
    serviceTypeData["CLUSTER_DEPLOY_PATH"] = JsVar["CLUSTER_DEPLOY_PATH"];
    
    //获取组件参数
    var paramsList = getComponentsParams();
    serviceTypeData["PARAMS_LIST"] = paramsList;
    
    getJsonDataByPost(Globals.baseActionUrl.SERVICE_TYPE_ACTION_ADD_URL, serviceTypeData, "集群管理-新增集群",
        function success(result){
            closeWindow(systemVar.SUCCESS);
        });
}

//修改主机
function update() {
    //判断是否有效
    JsVar["serviceTypeForm"].validate();
    if (JsVar["serviceTypeForm"].isValid() == false){
        return;
    }
    //修改操作下获取表单的数据
    var serviceTypeData = JsVar["serviceTypeForm"].getData();
    serviceTypeData["CLUSTER_ID"] = JsVar["CLUSTER_ID"];
    serviceTypeData["CLUSTER_DEPLOY_PATH"] = JsVar["CLUSTER_DEPLOY_PATH"];
    
    //获取组件参数
    var paramsList = getComponentsParams();
    serviceTypeData["PARAMS_LIST"] = paramsList;
    
    getJsonDataByPost(Globals.baseActionUrl.SERVICE_TYPE_ACTION_EDIT_URL, serviceTypeData, "集群管理-修改集群信息",
        function success(result){
            closeWindow(systemVar.SUCCESS);
        });
}

//编辑初始化数据
function initData(data) {
	JsVar["CLUSTER_ID"] = data["CLUSTER_ID"];
	JsVar["serviceTypeForm"].setData(data);
	
	//如果集群没有部署划分，则可以修改所有东西，否则下面3项不能修改
	if (data["DEPLOY_COUNT"] != null && data["DEPLOY_COUNT"] > 0) {
		mini.get("CLUSTER_CODE").setEnabled(false);
		mini.get("CLUSTER_TYPE").setEnabled(false);
		mini.get("CLUSTER_DEPLOY_PATH").setEnabled(false);
		mini.get("DEPLOY_PATH").setEnabled(false);
		
		if (data["CLUSTER_TYPE"] == busVar.M2DB) {
			mini.get("M2DB_INSTANCE").setEnabled(false);
		}
	}
	
	if (data["CLUSTER_TYPE"] == busVar.M2DB) {
		$("#m2dbTr").css("display", "");
		mini.get("M2DB_INSTANCE").setRequired(true);
		mini.parse();
	}
	 
	mini.get("DEPLOY_PATH").setValue(data["CLUSTER_DEPLOY_PATH"]);
	changeDeployPath();
}


/**
 * 查询  加载表格
 * @param param
 */
function loadComponentsParamsList() {
	var params = {
		DO_CFG:JsVar["DO_CFG"],
		CLUSTER_ID:JsVar["CLUSTER_ID"],
		CLUSTER_TYPE:mini.get("CLUSTER_TYPE").getValue()
	};
    datagridLoad(JsVar["configGrid"], params, null, Globals.baseActionUrl.SERVICE_TYPE_ACTION_GET_PARAMS_URL);
}

/**
 * 查询组件默认参数
 */
function defaultParmas() {
	JsVar["DO_CFG"] = systemVar.ADD;
    loadComponentsParamsList();
}

/**
 * 还原组件初始化参数
 */
function backParams() {
	JsVar["DO_CFG"] = systemVar.EDIT;
	loadComponentsParamsList();
}

/**
 * onRenderPassword函数中input标签元素值每次改变时，触发此函数
 * 
 * 作用：getGridData函数获取的值是原来datagrid的值，并不包含新渲染的input框中的值，
 * 		此函数便是为了更新getGridData函数获取的值。
 * 
 * 步骤：1.获取选中行对象
 * 		 2.将oldRow克隆成新对象newRow
 * 		 3.将当前input标签值赋给newRow。
 * 		 4.再将当前行更新成newRow的值
 * 这样getGridData函数获取的值就是文本框中最新的值。
 * 
 * @param e 当前input标签对象
 */
function valueChange(e){
	var oldRow = JsVar["configGrid"].getSelected();
	var newRow=mini.clone(oldRow);
	if(newRow){
		newRow["CFG_VALUE"]=e.value;
		JsVar["configGrid"].updateRow(oldRow,newRow);
	}
}

/**
 * 列渲染（给密码行中的input标签改成密码框）
 * @param e 当前列对象
 * @returns 给每列添加的Input标签
 */
function onRenderParamValue(e) {
	//获取当行记录
	var record = e.record;
	var	value = e.value == null ? "" : e.value;
	
	var cellHtml = e.cellHtml;
	e.value = "";//清空数据，不然页面会出现冲突
	//必填项加标注
	if (e.field == 'CFG_NAME' && record.CFG_IS_REQUIRED == '1') {
		cellHtml = "<span class=\"fred\">*</span>" + cellHtml;
	} else if (e.field == 'CFG_VALUE') {
		//当行记录的PARAM_NAME是password时（即是密码那一行）
		if (record.CFG_IS_PASSWD == "1"){
			//给当列添加个input标签 （密码框）
			//注明：此处在添加input标签时，如果使用mini-password或者mini-textbox，页面不能解析。所以这里直接使用type，下同
			cellHtml = "<input type='password' style='width:100%' onchange='valueChange(this)' value='"+value+"' maxlength='2000'/>";
		} else {
			//即非密码行
			//给当行添加个input标签（文本框）
			cellHtml = "<input type='text' style='width:100%' onchange='valueChange(this)' value='"+value+"' maxlength='2000'/>";
		}
	}
	e.cellHtml = cellHtml;
}


/**
 * 点确认保存参数
 */
function getComponentsParams() {
	//参数列表
    var addArray = [];
    
//    if (JsVar["DO_CFG"] == systemVar.EDIT) {
//    	if (JsVar["configGrid"].isChanged()) {
//    		addArray = getGridData();
//    	}
//    } else {
//    	addArray = getGridData();
//    }
    
    addArray = getGridData();
    
    //密码RSA加密
    for(var i=0;i<addArray.length;i++){
    	if(addArray[i]["CFG_IS_PASSWD"] == '1'){
    	    var enc_pwd = encrypt(addArray[i]["CFG_VALUE"]);
    	    addArray[i]["CFG_VALUE"]=enc_pwd;
    	}
    }
    return addArray;
}

/**
 * 获取表格数据
 * @returns {Array}
 */
function getGridData() {
    var data = JsVar["configGrid"].getData();
    var arr = new Array();
    
    var tipStr = "";
    for (var i = 0; i < data.length; i++) {
        var obj = new Object();
        obj["CLUSTER_TYPE"] = mini.get("CLUSTER_TYPE").getValue();
        obj["CFG_TYPE"] = data[i]["CFG_TYPE"];
        obj["CFG_NAME"] = data[i]["CFG_NAME"];
        obj["CFG_CODE"] = data[i]["CFG_CODE"];
        obj["CFG_IS_PASSWD"] = data[i]["CFG_IS_PASSWD"];
        if (data[i]["CFG_VALUE"]) {
            obj["CFG_VALUE"] = data[i]["CFG_VALUE"];
        } else if(data[i]["CFG_IS_REQUIRED"] == 1) {
        	//必填项
        	tipStr += data[i]["CFG_NAME"] + ",";
        } else {
            obj["CFG_VALUE"] = '';
        }
        arr.push(obj);
    }
    
    //校验不通过
    if(tipStr.length > 0) {
    	showErrorMessageAlter("以下为必填项：" + tipStr.substring(0,tipStr.length-1));
    	return ;
    } else {
    	return arr;
    }
}

/**
 * 校验编码
 * @param e
 */
function onEnglishAndNumberValidation(e) {
    if (e.isValid) {
        if (isEnglishAndNumber(e.value) == false) {
            e.errorText = "集群编码只能包含数字和字母！";
            e.isValid = false;
        }
    }
}

/**
 * 英文或者数字
 * @param v
 * @returns {Boolean}
 */
function isEnglishAndNumber(v) {
    var re = new RegExp("^[0-9a-zA-Z\_]*$");
    if (re.test(v)) return true;
    return false;
}





