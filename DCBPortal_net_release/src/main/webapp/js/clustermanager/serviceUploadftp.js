/**
 * 定义变量， 通常是页面控件和参数
 */
var JsVar = new Object();


//初始化
$(document).ready(function () {
    mini.parse();
    //获取页面表单
    JsVar["queryForm"] = new mini.Form("#queryForm");
    //表格
    JsVar["uploadGrid"] = mini.get("uploadGrid");
    //1:组件2：业务
    JsVar["FILE_TYPE"] = "2";
    //业务包类型下拉控件
    JsVar["PACKAGE_TYPE"] = mini.get("PACKAGE_TYPE");
    //初使化下拉查询数据
    initCombox();
    //查询版本服务器
    getFtpServer();
    //加载表格
    load({});
});
/**
 * 加载包类型的数据
 */
function initCombox(){
	getJsonDataByPost(Globals.baseActionUrl.FRAME_QUERY_FOR_LIST_URL,{"GROUP_CODE":"WEB_BUS_PACKAGE_TYPE"},"",
			function(result){
				if(result.length>0){
					result.unshift({"CONFIG_NAME":"全部","CONFIG_VALUE":""});
					JsVar["PACKAGE_TYPE"].setData(result);
					JsVar["PACKAGE_TYPE"].select(0);
				}
		},"config.queryConfigList");
}
/**
 * 查询
 */
function search() {
	var paramsObj = JsVar["queryForm"].getData(); 
	load(paramsObj);
}

/**
 * 渲染所属业务集群
 * @param e
 * @returns
 */
function onRenderBusCluster(e) {
    if(e && e.value){
    	var busCluster = e.value;
    	 var busClusterList = busCluster.split(",");
         var elements = new Array();
         for(var i=0;i<busClusterList.length;i++){
        	 elements.push("<span class='label label-success' >"+busClusterList[i]+"</span>  ");
         }
         if(elements.length != 0){
        	 return "<span style='word-wrap:break-word;word-break: break-all;white-space: normal;'>"+elements.join(" ")+"&nbsp;</span>";
         } else {
        	 return "<span class='label label-danger'>&nbsp;未分配&nbsp;</span>";
         }
    }
    return "<span class='label label-danger'>&nbsp;未分配&nbsp;</span>";
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
 * 合并数据
 * @param e
 */
function loadUploadGridData(e) {
	var gridData = JsVar["uploadGrid"].getData();
	var mergeCells2="PACKAGE_TYPE_NAME";
 	var mergeCellColumnIndex2="1";
	var mergeData = getMergeCellsOnGroup(gridData, mergeCells2, mergeCellColumnIndex2);
	JsVar["uploadGrid"].mergeCells(mergeData);
}

/**
 * 加载表格
 * @param params
 */
function load(obj){
	obj["FILE_TYPE"] = JsVar["FILE_TYPE"];
	datagridLoadPage(JsVar["uploadGrid"],obj ,"ftpFileUpload.queryFileInfoList");
}

/**
 * 渲染操作单元格
 * @param e
 */ 
function onActionRenderer(e){
	var index = e.rowIndex;
    var html = '<a class="Delete_Button" href="javascript:del(' + index + ')">删除  </a>';
    /*if(e.record.STATE == 1){
    html += '<a class="Delete_Button" href="javascript:invalid(' + index + ')">失效</a>';
    }else{
    	html += '<a class="Delete_Button" href="javascript:active(' + index + ')">有效</a>';
    }*/
   // html += '<a class="Delete_Button" href="javascript:fileContent(' + index + ')">查看文件</a>';
    return html;
}

/**
 * 删除文件信息
 */
function del(index){
	//新建一个集合,用于存放被勾选的id
	var paramsArray = new Array();
	var row = JsVar["uploadGrid"].getRow(index);
	var obj = {};
	obj["ID"] = row["ID"];
	obj["FILE_NAME"] = row["FILE_NAME"];
	obj["NAME"] = row["NAME"];
	obj["VERSION"] = row["VERSION"];
	obj["PACKAGE_TYPE"] = row["PACKAGE_TYPE"];
	paramsArray.push(obj);
	showConfirmMessageAlter("确认删除记录?",function ok(){
		getJsonDataByPost(Globals.baseActionUrl.FTP_ACTION_DELETE_SERVICE_URL,paramsArray,"业务程序框架版本管理-删除文件信息",
				function(result){
			JsVar["uploadGrid"].reload();
			showMessageTips("删除成功!");
		});
	});
}

/**
 *生效 
 */
function active(index){
	var row = JsVar["uploadGrid"].getRow(index);
	getJsonDataByPost(Globals.baseActionUrl.FRAME_UPDATE_OBJECT_URL,[{ID:row["ID"]}],"业务程序框架版本管理-生效",
		function(result){
		showMessageTips("操作成功！");
		JsVar["uploadGrid"].reload();	
	},"ftpFileUpload.active");
}

/**
 *失效 
 */
function invalid(index){
	var row = JsVar["uploadGrid"].getRow(index);
	
	getJsonDataByPost(Globals.baseActionUrl.FRAME_QUERY_FOR_OBJECT_URL,{ID:row["ID"]},"业务程序框架版本管理-查询是否可失效",
				function(result){
			if(result["total"] == 0){
				showConfirmMessageAlter("确定失效当前版本？",function(){
					getJsonDataByPost(Globals.baseActionUrl.FRAME_UPDATE_OBJECT_URL,[{ID:row["ID"]}],"业务程序框架版本管理-失效版本",
						function(result){
						showMessageTips("操作成功！");
						JsVar["uploadGrid"].reload();	
					},"ftpFileUpload.inactive");
				});
			}else{
                showWarnMessageTips("当前版本有正在运行程序，无法进行失效操作！");
			}
		},"ftpFileUpload.checkActive");
}

/**
 *版本状态渲染 
 */
function stateRenderer(e){
	var html = "";
	if(e.record.STATE == 1){
		html+="<span class='label label-success' style='margin-left:10px;'>有效</span>";
	}else{
		html+="<span class='label label-danger' style='margin-left:10px;'>失效</span>";
	}
	return html;
}

/**
 * 上传文件弹框
 */
function upload(){
	showAddDialog("业务程序版本管理-文件上传",730,455,Globals.baseJspUrl.UPLOAD_JSP_ADD_SERVICE_URL,
	        function destroy(data){
	            if (data == systemVar.SUCCESS) {
	            	JsVar["uploadGrid"].reload();
                    showMessageTips("文件上传成功!");
	            }
	    },{FILE_TYPE:JsVar["FILE_TYPE"]});
}

/**
 * 查看文件
 */
function fileContent(index){
	var rowInfo = JsVar["uploadGrid"].getRow(index);
	showDialog("查看文件名",430,250,Globals.baseJspUrl.UPLOAD_JSP_FILE_CONTENT_SERVICE_URL,
	        function destroy(data){
	    },rowInfo);
}

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
	showDialog("查看详情",730,450,Globals.baseJspUrl.UPLOAD_JSP_VIEW_SERVICE_URL,
	        function destroy(data){
	    },rowInfo);
}