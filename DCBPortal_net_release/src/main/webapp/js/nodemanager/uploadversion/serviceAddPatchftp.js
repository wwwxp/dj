/**
 * 新增修改集群弹框
 */

//定义变量， 通常是页面控件和参数
var JsVar = new Object();
var fileupload;     //文件上传对象\
var file;//选择的文件对象，主要给change对象使用，
$(document).ready(function () {
    mini.parse();
    //获取页面表单
    JsVar["uploadForm"] = new mini.Form("#uploadForm");
    JsVar["FILE_NAME"] = mini.get("FILE_NAME");
    //业务包类型下拉控件
    JsVar["PACKAGE_TYPE"] = mini.get("PACKAGE_TYPE");
    //集群下拉控件
    JsVar["BUS_CLUSTER_ID"] = mini.get("BUS_CLUSTER_ID");

    //部署版本
    JsVar["DEP_VERSION"] = mini.get("DEP_VERSION");

    JsVar["version"] = mini.get("VERSION");
    fileupload = $('#uFile');
    fileupload.change(function(){
    	setFormEl(this);
    });
    initCombox();
    $("input[name='uploadType']").click(function(){
 	   var type = $(this).val();
 	   var fileName;
 	   if(type =='1'){
 		    fileName =  $.trim(mini.get("remoteFile").getValue());
 	   }else{
 		    fileName = $.trim(fileupload.val());
 	   }
 	   if(fileName && fileName != ''){
 		   var obj = new Object();
 		   obj.value=  fileName;
 		   setFormEl(obj);
 	   }else{
 		   JsVar["FILE_NAME"].setValue("");
 		   JsVar["version"].setValue("");
 	   }
  });
    
});
/**
 * 查询业务包类型数据
 */
function initCombox(){
	getJsonDataByPost(Globals.ctx+"/nodeopt/queryNoteTypeConfig",null,"",
			function(result){
				if(result && result.length>0){
					JsVar["PACKAGE_TYPE"].setData(result);
				}
		});
}


function onNodeTypeChanged(e) {
    var depversionCombo = JsVar["DEP_VERSION"];
    var nodeTypeId = JsVar["PACKAGE_TYPE"].getValue();
    depversionCombo.setValue("");
    depversionCombo.setData(null);
    if(nodeTypeId==null||nodeTypeId==''){
        return;
    }
    loadDeployVersion();
}


function loadDeployVersion() {
    var depversionCombo = JsVar["DEP_VERSION"];
    var nodeTypeId = JsVar["PACKAGE_TYPE"].getValue();
    if(nodeTypeId==null||nodeTypeId==''){
        return;
    }
    var pram = {"NODE_TYPE_ID": nodeTypeId};
    comboxLoad(depversionCombo, pram, "", Globals.ctx + "/nodeCfgPub/queryNodeTypeVersionList", null);
}


/**
 * 给表单设值
 * @param obj
 */
function setFormEl(obj){
	 //输出选中结果
	var fileFullName = obj.value;
    var fileName = fileFullName.substring((fileFullName.lastIndexOf("\\")+1),fileFullName.length);
    var version;
    // 获取文件类型
    if(fileName.indexOf(".tar.gz")>0){
    	JsVar["fileSuffixType"] = ".tar.gz";
    }else{
    	JsVar["fileSuffixType"] = ".zip";
    }
    JsVar["FILE_NAME"].setValue(fileName);

    // var fileNamePrefix = getFileNamePrefix(fileName);
    // JsVar["PACKAGE_TYPE"].setValue(fileNamePrefix);

    // getJsonDataByPost(Globals.baseActionUrl.FRAME_QUERY_FOR_LIST_URL,{"PACKAGE_TYPE":fileNamePrefix},"",
	// 		function(result){
    // 			JsVar["BUS_CLUSTER_ID"].setValue("");
	// 			if(result && result.length>0){
	// 				var values = [];
	// 				for(var i = 0 ;i < result.length ;i++){
	// 					values.push(result[i]["BUS_CLUSTER_ID"]);
	// 				}
	// 				var valueStr = values.join(",");
	// 				JsVar["BUS_CLUSTER_ID"].setValue(valueStr);
	// 				JsVar["BUS_CLUSTER_ID"].setEnabled(false);
	//
	// 			}else{
	// 				JsVar["BUS_CLUSTER_ID"].setEnabled(true);
	// 			}
	// 	},"ftpFileUpload.queryClusterListByPackageType");


}
/**
 * 得到版本号
 */
function getVersion(param){
	var fileName = param.toLowerCase();
	var v = "_v";
	if(fileName.lastIndexOf("-v") > 0){
		v = "-v"; 
	}
	  // 版本号截取
    if(fileName.indexOf(".tar.gz")>0){
        return fileName.substring((fileName.lastIndexOf(v)+2),fileName.indexOf(".tar.gz"));
    }else{
    	return fileName.substring((fileName.lastIndexOf(v)+2),fileName.indexOf(".zip"));
    }
}
/**
 * 得到文件名前缀
 */
function getFileNamePrefix(fileName){
	var v = "_v";
	if(fileName.lastIndexOf("-v") > 0){
		v = "-v"; 
	}
	  // 版本号截取
    if(fileName.indexOf(".tar.gz")>0){
        return fileName.substring(0,fileName.toLowerCase().lastIndexOf(v));
    }else{
    	return fileName.substring(0,fileName.toLowerCase().lastIndexOf(v));
    }
}


//父页面调用，新增和修改时会初使化一些参数，例如：data.action：操作类型
function onLoadComplete(action,data) {
    JsVar["FILE_TYPE"] = data["FILE_TYPE"];
}

//选择本地文件
function selectLocalFile() {
    $("input[name='uploadType'][value='0']").attr("checked",true);
    mini.get("remoteFile").setValue("");
}

//选择文件
function selectFile(){
	$("input[name='uploadType'][value='1']").attr("checked",true);
    var url = Globals.ctx + "/jsp/nodemanager/uploadversion/chooseRemoteFile" +"?page_type="+ JsVar["FILE_TYPE"];
	showDialog("选择文件", 600, 400,url,
	        function success(action){
	            if (action == systemVar.SUCCESS) {
	                //JsVar["classGrid"].reload();
	            }
	        },JsVar["FILE_TYPE"]);
}
function setRemoteFile(obj){
	mini.get('remoteFile').setValue(obj.value);
    JsVar["fileRelPath"] = obj["fileRelPath"];
	setFormEl(obj);
}

/**
 * 开始上传文件
 */
function fileUpload(){
	var uploadType = $("input[name='uploadType']:checked").val();
	var fileName = $.trim(fileupload.val());
	if(uploadType == '1'){
		fileName = mini.get("remoteFile").getValue(); 
	}
	if(fileName == ""){
        showWarnMessageTips("请选择文件");
        return ;
    }

	 if (fileName.lastIndexOf(".tar.gz") > 0 || fileName.lastIndexOf(".zip") > 0||fileName.lastIndexOf(".war") > 0) {
		
	 } else{
         showWarnMessageTips("文件格式有问题,请重新选择");
	     return ;
	 }

	 JsVar["uploadForm"].validate();
     if (JsVar["uploadForm"].isValid() == false){
         return;
     }
     //修改操作下获取表单的数据
     var taskInfo = JsVar["uploadForm"].getData();
    if(uploadType === '1'){
        taskInfo["fileRelPath"] = JsVar["fileRelPath"];
    }
     taskInfo["uploadType"] = uploadType;
     taskInfo["fileName"] = fileName;
     taskInfo["NODE_TYPE_CFG_ID"] = taskInfo["PACKAGE_TYPE"];
     taskInfo["DESCRIPTION"] = "版本负责人："
    	 					+ taskInfo["PRINCIPAL"]
     						+ "\n"
    	 					+ "升级相关模块："
    	 					+ taskInfo["MODEL"]
    	 					+ "\n"
    	 					+ "本次升级内容："
    	 					+ taskInfo["DESC"];
     showConfirmMessageAlter("确认上传文件?",function ok(){
	 	// commonFileUpload(Globals.baseActionUrl.FTP_ACTION_FILE_UPLOAD_URL,['uFile'],taskInfo,
	 	commonFileUpload(Globals.ctx+"/nodeopt/updateVersionPatchPkg",['uFile'],taskInfo,
	 			function (result){
	 				if (result != null && result != undefined && result != ""){
                        closeWindow(systemVar.SUCCESS);
					}else {
                        showWarnMessageTips("上传失败");
						return false;
					}
	 			},false,"正在上传文件,请稍等...", "业务版本包上传", 700, 450);
     });
	 
}