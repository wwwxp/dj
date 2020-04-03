/**
 * 定义变量， 通常是页面控件和参数
 */
var JsVar = new Object();


//初始化
$(document).ready(function () {
    mini.parse();
    JsVar["uploadGrid"] = mini.get("uploadGrid");
    JsVar["FILE_TYPE"] = "1";
    //加载版本服务器数据
    getFtpServer();
    //加载表格
    load();
});

/**
 * 加载表格
 * @param params
 */
function load(){
	datagridLoadPage(JsVar["uploadGrid"],{FILE_TYPE:JsVar["FILE_TYPE"]},"ftpFileUpload.queryFileInfo");
}

/**
 * 渲染操作单元格
 * @param e
 */
function onActionRenderer(e){
	var index = e.rowIndex;
    var html = '<a class="Delete_Button" style="margin-right:0" href="javascript:del(' + index + ')">删除</a>';
    return html;
}

/**
 * 获取ftp版本服务器地址
 */
function getFtpServer(){
	var ftpParam = {"GROUP_CODE":"WEB_FTP_CONFIG"};
	getJsonDataByPost(Globals.baseActionUrl.FRAME_QUERY_FOR_LIST_URL,ftpParam,"",
			function(result){
		      if(result && result.length >0){
		    	  for(var i = 0 ; i < result.length ;i++){
		    		  if(result[i]["CONFIG_NAME"]=="FTP_IP"){
		    			  $('#ftpHostName').html(result[i]["CONFIG_VALUE"]);
		    		  }
		    		  if(result[i]["CONFIG_NAME"]=="FTP_TYPE"){
		    			  $('#ftpTypeName').html(result[i]["CONFIG_VALUE"]);
		    		  }
		    	  }
		      }
		      
	},"config.queryConfigList");
}

/**
 * 删除文件信息(一条)
 */
function del(index){
	//新建一个集合,用于存放被勾选的id
	var versions = new Array();
	var row = JsVar["uploadGrid"].getRow(index);
	var isExistRunPro=true;
	//查询删除的版本是否已启动
	getJsonDataByPost(Globals.baseActionUrl.FRAME_QUERY_FOR_OBJECT_URL,row,"组件版本管理-查询版本运行状况",
			function(result){
		if(result.RUN_COUNT<=0){
			isExistRunPro=false;
		}
	},"instConfig.queryInstConfigCount","",false);
	
	if(!isExistRunPro){
		//删除
		showConfirmMessageAlter("确认删除记录?",function ok(){
			
			getJsonDataByPost(Globals.baseActionUrl.FTP_ACTION_DELETE_PLATFORM_BY_VERSION_URL,row,"组件版本管理-删除版本文件信息",
					function(result){
                        showMessageTips(result.message);
				JsVar["uploadGrid"].reload();
			});
		});
	}else{
        showWarnMessageTips("该版本有主机正在运行，请先停止后在进行删除操作！");
	}
}

/**
 * 上传文件弹框
 */
function upload(){
	showAddDialog("文件上传",730,450,Globals.baseJspUrl.UPLOAD_JSP_ADD_URL,
	        function destroy(data){
	            if (data == systemVar.SUCCESS) {
	            	JsVar["uploadGrid"].reload();
                    showMessageTips("文件上传成功!");
	            }
	    },{FILE_TYPE:JsVar["FILE_TYPE"]});
}

/**
 * 版本回退
 */
/*function back(){
	var params = {FILE_TYPE:JsVar["FILE_TYPE"]};
	// 查询上个版本,作遮罩层显示用
	getJsonDataByPost(Globals.baseActionUrl.FRAME_QUERY_FOR_LIST_URL,params,"",
			function(result){
		params["lastVersion"] = result[1]["VERSION"];
	},"ftpFileUpload.queryVersion");
	showConfirmMessageAlter("确认版本回退?",function ok(){
		getJsonDataByPost(Globals.baseActionUrl.FTP_ACTION_BACK_VERSION_URL,params,"组件版本管理-版本回退",
				function(result){
			JsVar["uploadGrid"].reload();
			showMessageAlter("已回退到" + params["lastVersion"] + "版本");
		});
	});
}*/

/**
 * 文件名查看详情超链接渲染
 * @param e
 * @returns
 */
function fileRenderer(e){
	return '<a class="Delete_Button" style="margin-right:0" href="javascript:fileView(' + e.rowIndex + ')">' +  e.value  +  '</a>';
}

function fileView(index){
	var rowInfo = JsVar["uploadGrid"].getRow(index);
	showDialog("查看详情",730,450,Globals.baseJspUrl.UPLOAD_JSP_VIEW_URL,
	        function destroy(data){
	    },rowInfo);
}