/**
 * 定义变量， 通常是页面控件和参数
 */
var JsVar = new Object();
/**
 * 初始化
 */
$(document).ready(function () {
    mini.parse();
  //取得主机表格
    JsVar["configGrid"] = mini.get("configGrid");
  //取得查询表单
    JsVar["queryForm"] =  new mini.Form("#queryForm");
});

/**
 * 跳转到该页面设值
 * @param data
 */
function onLoadComplete(data) {
	if(data!=null){
		JsVar["CLUSTER_ID"] = data["CLUSTER_ID"];
		JsVar["CLUSTER_TYPE"] = data["CLUSTER_TYPE"];
	    load(data);
	}
}

/**
 * 查询
 */
function search() {
    var paramsObj = JsVar["queryForm"].getData();
    if (paramsObj != null && paramsObj != undefined) {
    	paramsObj["CLUSTER_ID"] = JsVar["CLUSTER_ID"];
    	paramsObj["CLUSTER_TYPE"] = JsVar["CLUSTER_TYPE"];
    }
    load(paramsObj);
}

/**
 * 加载表格
 * @param param
 */
function load(param){
	getJsonDataByPost(Globals.baseActionUrl.FRAME_QUERY_FOR_LIST_URL, param, "获取一启停批次信息",
		function(result){
			if(result!=null){
				var batchNameArray = [];
				for (var i=0; i<result.length; i++) {
					var isExists = false;
					if (batchNameArray.length > 0) {
						for (var j=0; j<batchNameArray.length; j++) {
							if (result[i]["BATCH_NAME"] == batchNameArray[j]["BATCH_NAME"]) {
								isExists = true;
								break;
							}
						}
					}
					if (!isExists && result[i]["BATCH_NAME"]) {
						var newObj = {
							UID:(new Date()).getTime(),
							BATCH_NAME:result[i]["BATCH_NAME"],
							HOST_IP:"",
							DEPLOY_FILE_TYPE:"",
							CONFIG_PATH:"",
							VERSION:"",
							UPDATE_DATE:""
						};
						batchNameArray.push(newObj);
					}
				}
				
				for (var i=0; i<batchNameArray.length; i++) {
					var childrenArray = [];
					for (var j=0; j<result.length; j++) {
						if (result[j]["BATCH_NAME"] == batchNameArray[i]["BATCH_NAME"] && batchNameArray[i]["BATCH_NAME"] != null) {
							var childrenObj = {
								BATCH_NAME:"",
								UID:(new Date()).getTime(),
								HOST_IP:result[j]["HOST_IP"],
								DEPLOY_FILE_TYPE:result[j]["DEPLOY_FILE_TYPE"],
								CONFIG_PATH:result[j]["CONFIG_PATH"],
								VERSION:result[j]["VERSION"],
								UPDATE_DATE:result[j]["UPDATE_DATE"],
								PARENT_UID:batchNameArray[i]["UID"]
							};
							childrenArray.push(childrenObj);
						}
					}
					batchNameArray[i]["children"] = childrenArray;
				}
				JsVar["configGrid"].setData(batchNameArray);
			}
	}, "startConfig.queryStartConfigList", null, false);
}

/**
 * 删除批次
 * @param e
 * @returns {String}
 */
function onActionRenderer(e) {
	var batchName = e.record.BATCH_NAME;
	var html = "";
	if (batchName != null && batchName != '') {
		html = '<a class="Delete_Button" href="javascript:loadBatch(\'' + batchName + '\')">载入</a>';
		html += '<a class="Delete_Button" href="javascript:delBatch(\'' + batchName + '\')">删除</a>';
	}
	return html;
}

/**
 * 载入批次
 */
function loadBatch(batchName) {
	 closeWindow({"BATCH_NAME": batchName});
}

/**
 * 删除批次
 * @param batchName
 */
function delBatch(batchName) {
    showConfirmMessageAlter("确定删除该批次记录？",function ok(){
    		var delParams = {
    			BATCH_NAME:batchName,
    			CLUSTER_ID:JsVar["CLUSTER_ID"]
    		};
            // 数据汇总保存
            var delObj = new Object();
            delObj["delete|startConfig.delStartConfigByCode"] = [delParams];
            getJsonDataByPost(Globals.baseActionUrl.FRAME_MULTI_OPERATION_URL, [delObj], "一键启停-删除批次",
                function(result){
            		var paramsObj = {
            			CLUSTER_ID:JsVar["CLUSTER_ID"],
            			CLUSTER_TYPE:JsVar["CLUSTER_TYPE"]
            		};
            		load(paramsObj);
                    showMessageTips("删除批次成功!");
                });
    });
}