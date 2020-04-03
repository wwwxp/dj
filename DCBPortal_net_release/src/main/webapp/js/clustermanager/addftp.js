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
    JsVar["version"] = mini.get("VERSION");
    fileupload = $('#uFile');
    fileupload.change(function(){
    	setFormEl(this)
        
    });
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
 * 给表单设值 
 * @param obj
 */
function setFormEl(obj){
	 //输出选中结果
    var fileFullName = obj.value;
    var fileName = fileFullName.substring((fileFullName.lastIndexOf("\\")+1),fileFullName.length);
    // 版本号截取
    var version = fileName.substring((fileName.toLowerCase().indexOf("_v")+2),fileName.indexOf(".zip"));
    JsVar["FILE_NAME"].setValue(fileName);
    JsVar["version"].setValue(version);
}

//父页面调用，新增和修改时会初使化一些参数，例如：data.action：操作类型
function onLoadComplete(action,data) {
    JsVar["FILE_TYPE"] = data["FILE_TYPE"];
}

/**
 * 校验版本号
 * @param version
 */
function validateVersion(version){
	// 字符串转换为整型
	var currentVersionToNum = parseInt(version.replace(/\./g,""));
	var flag = false;
	getJsonDataByPost(Globals.baseActionUrl.FRAME_QUERY_FOR_LIST_URL,{FILE_TYPE:JsVar["FILE_TYPE"]},"",
			function (result){
		if(result != null && result.length > 0){
			JsVar["lastVersion"] = result[0]["VERSION"];
			var lastVersionToNum = parseInt(result[0]["VERSION"].replace(/\./g,""));
			if(currentVersionToNum <= lastVersionToNum){
				flag = true ;
			}
		}else{
			// 传递后台一个字符串,不然该值为undefined
			JsVar["lastVersion"] = "none";
		}
	},"ftpFileUpload.queryVersion","",false);
	
	return flag;
}

//选择文件
function selectFile(){
	$("input[name='uploadType'][value='1']").attr("checked",true); 
	var url = Globals.baseJspUrl.UPLOAD_JSP_CHOOSE_REMOTE_FILE_URL +"?page_type="+ JsVar["FILE_TYPE"];
	showDialog("选择文件",600,400,url,
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

	 if (fileName.lastIndexOf(".")!=-1) {
		 var fileType = (fileName.substring(fileName.lastIndexOf(".")+1,fileName.length)).toLowerCase();
	     if(fileType != 'zip'){
             showWarnMessageTips("文件类型必须为zip文件,请重新选择");
		     return ;
	     }
	 }else{
         showWarnMessageTips("文件格式有问题,请重新选择");
	        return ;
	 }

	 //校验上传zip文件格式
	 if(!validateZipV(fileName)){
             return ;
	 }
	 
	 // 校验版本号
	// if(validateVersion(JsVar["version"].getValue())){
	//	 showWarnMessageAlter("当前版本号必须是最新版本号!");
	//	 return;
	 //}
	 
	 JsVar["uploadForm"].validate();
     if (JsVar["uploadForm"].isValid() == false){
         return;
     }
   //修改操作下获取表单的数据
     var taskInfo = JsVar["uploadForm"].getData();
     taskInfo["uploadType"] = uploadType;
     taskInfo["FILE_TYPE"] = JsVar["FILE_TYPE"];
     showConfirmMessageAlter("确认上传文件?",function ok(){
	 	commonFileUpload(Globals.baseActionUrl.FTP_ACTION_FILE_UPLOAD_URL,['uFile'],taskInfo,
	 			function (result){
	 				closeWindow(systemVar.SUCCESS);
	 			},false,"正在上传文件,请稍等...",'框架版本管理--版本上传');
     });
	 
}
