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
	getJsonDataByPost(Globals.baseActionUrl.FRAME_QUERY_FOR_LIST_URL,{"GROUP_CODE":"WEB_BUS_PACKAGE_TYPE"},"",
			function(result){
				if(result && result.length>0){
					JsVar["PACKAGE_TYPE"].setData(result);
				}
		},"config.queryConfigList");
	
	getJsonDataByPost(Globals.baseActionUrl.FRAME_QUERY_FOR_LIST_URL,{},"",
			function(result){
				if(result && result.length>0){
					JsVar["BUS_CLUSTER_ID"].setData(result);
				}
		},"busMainCluster.queryBusMainClusterListByState");
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
    var version = getVersion(fileName);
    JsVar["FILE_NAME"].setValue(fileName);
    JsVar["version"].setValue(version);
    
    var fileNamePrefix = getFileNamePrefix(fileName);
    JsVar["PACKAGE_TYPE"].setValue(fileNamePrefix);
    
    getJsonDataByPost(Globals.baseActionUrl.FRAME_QUERY_FOR_LIST_URL,{"PACKAGE_TYPE":fileNamePrefix},"",
			function(result){
    			JsVar["BUS_CLUSTER_ID"].setValue("");
				if(result && result.length>0){
					var values = [];
					for(var i = 0 ;i < result.length ;i++){
						values.push(result[i]["BUS_CLUSTER_ID"]);
					}
					var valueStr = values.join(",");
					JsVar["BUS_CLUSTER_ID"].setValue(valueStr);
					JsVar["BUS_CLUSTER_ID"].setEnabled(false);
					
				}else{
					JsVar["BUS_CLUSTER_ID"].setEnabled(true);
				}
		},"ftpFileUpload.queryClusterListByPackageType");
    
    
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

 

//选择文件
function selectFile(){
	$("input[name='uploadType'][value='1']").attr("checked",true); 
	var url = Globals.baseJspUrl.UPLOAD_JSP_CHOOSE_REMOTE_FILE_URL +"?page_type="+ JsVar["FILE_TYPE"];
	showDialog("选择文件",600, 400,url,
	        function success(action){
	            if (action == systemVar.SUCCESS) {
	                //JsVar["classGrid"].reload();
	            }
	        },JsVar["FILE_TYPE"]);
}
function setRemoteFile(obj){
	mini.get('remoteFile').setValue(obj.value);
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

	 if (fileName.lastIndexOf(".tar.gz") > 0 || fileName.lastIndexOf(".zip") > 0) {
		
	 } else{
         showWarnMessageTips("文件格式有问题,请重新选择");
	     return ;
	 }

	 //校验上传tar.gz文件格式
	 if(!validateGzV(fileName)){
		 return ;
	 }
	 
	 // 校验版本号
	/* if(validateVersion(JsVar["version"].getValue())){
		 showWarnMessageAlter("当前版本号已存在,请修改文件名!");
		 return;
	 }
	 
	*/
	 JsVar["uploadForm"].validate();
     if (JsVar["uploadForm"].isValid() == false){
         return;
     }
     //修改操作下获取表单的数据
     var taskInfo = JsVar["uploadForm"].getData();
     taskInfo["FILE_TYPE"] = JsVar["FILE_TYPE"];
     taskInfo["uploadType"] = uploadType;
     taskInfo["fileSuffixType"] = JsVar["fileSuffixType"];
     taskInfo["DESCRIPTION"] = "版本负责人："
    	 					+ taskInfo["PRINCIPAL"]
     						+ "\n"
    	 					+ "升级相关模块："
    	 					+ taskInfo["MODEL"]
    	 					+ "\n"
    	 					+ "本次升级内容："
    	 					+ taskInfo["CONTENT"];
     showConfirmMessageAlter("确认上传文件?",function ok(){
	 	commonFileUpload(Globals.baseActionUrl.FTP_ACTION_FILE_UPLOAD_URL,['uFile'],taskInfo,
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

/**
 * 校验tar.gz文件格式
 * @param fileName  DIC-BIL-DCM-XZ_V2.12.0.0.tar.gz
 * @returns {Boolean}
 */
function validateGzV(fileName) {
    if(fileName.lastIndexOf("_")!=-1){
        var reg = new RegExp("^([\\s\\S]*)[_,-][v,V]\\d{1,2}\\.\\d{1,2}\\.\\d{1,2}.\\d([\\s\\S]*)$", "g");
        if (!reg.test(fileName)){
            showWarnMessageTips("文件格式有问题,请重新选择，正确的文件中必须是文件名_V版本号.小版本号.序列，版本号及序列最多支持两位，例如文件名_V1.0.1.0");
            return false;
        }
    }else{
        showWarnMessageTips("文件格式有问题,请重新选择，正确的文件中必须是文件名_V版本号.小版本号.序列，版本号及序列最多支持两位，例如文件名_V1.0.1.0");
        return false;
    }

	// if(fileName.lastIndexOf("_")!=-1){
		//  var reg = new RegExp("^([\\s\\S]*)[_,-][v,V]\\d{1,2}\\.\\d{1,2}\\.\\d.\\d([\\s\\S]*)$", "g");
      //    if (!reg.test(fileName)){
      //        showWarnMessageTips("文件格式有问题,请重新选择，正确的文件中必须是文件名_V版本号.小版本号.序列，大版本号和小版本号可以是1-2位数，序列必须是1位数，例如文件名_V1.0.1.0");
      //        return false;
      //    }
	 // }else{
      //    showWarnMessageTips("文件格式有问题,请重新选择，正确的文件中必须是文件名_V版本号.小版本号.序列，大版本号和小版本号可以是1-2位数，序列必须是1位数，例如文件名_V1.0.1.0");
	 //         return false;
	 // }
	 
	 return true;
}