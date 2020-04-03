//定义变量， 通常是页面控件和参数
var JsVar = new Object();

$(document).ready(function () {
    mini.parse();
    //获取页面表单
    JsVar["programForm"] = new mini.Form("#programForm");
    JsVar["PROGRAM_TYPE"] = mini.get("PROGRAM_TYPE");
    JsVar["PROGRAM_CODE"] = mini.get("PROGRAM_CODE");
    JsVar["PROGRAM_NAME"] = mini.get("PROGRAM_NAME");
    JsVar["MULTI_PROCESS"] = mini.get("MULTI_PROCESS");
    
    comboxLoad(JsVar["PROGRAM_TYPE"], {TYPE:'3'}, "serviceType.queryClusterType");
    
});
//父页面调用，新增和修改时会初使化一些参数，例如：data.action：操作类型
function onLoadComplete(action,data) {
	JsVar["DATA"] = data;
    JsVar[systemVar.ACTION] = action;
    if (action == systemVar.EDIT) {
    	 // 初使化表单数据
        initData(data);
    }
}

//新增和修改点确认按钮保存
function submit() {
    if (JsVar[systemVar.ACTION] == systemVar.EDIT) {
        update();
    } else {
        save();
    }
}

 

//点确认新增主机
function save() {
	//判断是否有效
    JsVar["programForm"].validate();
    if (JsVar["programForm"].isValid() == false){
        return;
    }
    
    //插入数据
    var programInfo = JsVar["programForm"].getData();

    var isContinue = true;
    var obj = {
        PROGRAM_CODE : programInfo["PROGRAM_CODE"]
    };
    //校验程序编码是否有重复
    getJsonDataByPost(Globals.baseActionUrl.FRAME_QUERY_FOR_LIST_URL, obj, "业务程序管理-校验程序编码",
        function (result) {
	    	if(result != null && result.length > 0){
                showWarnMessageTips("当前业务程序编码已被使用，请更改！");
	            isContinue = false;
	        }
        }, "programDefine.queryProgramCodeList", null, false);
    
    if (!isContinue) {
    	return;
    }
    
    //判断程序所属组是否为空，如果不为空则判断程序所属组有且最多只能2个
    var programGroup = programInfo["PROGRAM_GROUP"];
    if (programGroup != undefined && programGroup != null && programGroup != '') {
    	var params = {
    		PROGRAM_GROUP:programGroup
    	};
    	//校验程序编码是否有重复
    	getJsonDataByPost(Globals.baseActionUrl.FRAME_QUERY_FOR_LIST_URL, params, "业务程序管理-校验程序所属组",
    	  function (result) {
    	  	if(result != null && result.length > 1){
                showWarnMessageTips("同一所属组有且最多只能包含2个程序！");
    	          isContinue = false;
    	      }
    	  }, "programDefine.queryProgramGroupList", null, false);
    }
    if (isContinue) {
    	//添加程序
        getJsonDataByPost(Globals.baseActionUrl.FRAME_INSERT_OBJECT_URL,[programInfo],"业务程序管理-新增程序",
            function success(result){
                closeWindow(systemVar.SUCCESS);
            },"programDefine.insertProgram");
    }
}

//修改主机
function update() {
	//判断是否有效
    JsVar["programForm"].validate();
    if (JsVar["programForm"].isValid() == false){
        return;
    }
    var programInfo = JsVar["programForm"].getData();

    var isContinue = true;
    var obj = {
    	PROGRAM_CODE_OLD:JsVar["DATA"]["PROGRAM_CODE"],
        PROGRAM_CODE : programInfo["PROGRAM_CODE"]
    };
    getJsonDataByPost(Globals.baseActionUrl.FRAME_QUERY_FOR_LIST_URL, obj, "业务程序管理-查看程序状态",
        function (result) {
	    	if(result != null && result.length > 0){
                showWarnMessageTips("当前业务程序编码已被使用，请更改！");
	            isContinue = false;
	    	}
        }, "programDefine.queryProgramCodeList", null, false);
    if (!isContinue) {
    	return;
    }
    
    //判断程序所属组是否为空，如果不为空则判断程序所属组有且最多只能2个
    var programGroup = programInfo["PROGRAM_GROUP"];
    if (programGroup != undefined && programGroup != null && programGroup != '') {
    	var params = {
    		PROGRAM_CODE:programInfo["PROGRAM_CODE"],
    		PROGRAM_GROUP:programGroup
    	};
    	//校验程序编码是否有重复
    	getJsonDataByPost(Globals.baseActionUrl.FRAME_QUERY_FOR_LIST_URL, params, "业务程序管理-校验程序所属组",
    	  function (result) {
    	  	if(result != null && result.length > 1){
                showWarnMessageTips("同一所属组有且最多只能包含2个程序！");
    	          isContinue = false;
    	      }
    	  }, "programDefine.queryProgramGroupList", null, false);
    }
    
    if (isContinue) {
    	getJsonDataByPost(Globals.baseActionUrl.FRAME_UPDATE_OBJECT_URL, [programInfo], "业务程序管理-修改程序",
                function success(result) {
                    closeWindow(systemVar.SUCCESS);
                }, "programDefine.updateProgram");
    }
}

//编辑初始化数据
function initData(data) {
	var param = new Object();
    param["PROGRAM_CODE"] = data["PROGRAM_CODE"];
    
    getJsonDataByPost(Globals.baseActionUrl.FRAME_QUERY_FOR_OBJECT_URL, param, "",
        function (result) {
            JsVar["programForm"].setData(result);
            JsVar["PROGRAM_TYPE"].setValue(result.PROGRAM_TYPE);
            JsVar["PROGRAM_TYPE"].setText(result.PROGRAM_TYPE);
            JsVar["PROGRAM_NAME"].setEnabled(false);
            JsVar["PROGRAM_TYPE"].setEnabled(false);
            JsVar["PROGRAM_CODE"].setEnabled(false);
            JsVar["MULTI_PROCESS"].setValue(result.MULTI_PROCESS);
        }, "programDefine.queryProgramList");
}
