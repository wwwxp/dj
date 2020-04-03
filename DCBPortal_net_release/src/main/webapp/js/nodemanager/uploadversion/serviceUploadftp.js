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
    JsVar["CODE"] = mini.get("CODE");
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
	getJsonDataByPost(Globals.ctx+"/nodeopt/queryNoteTypeConfig",null,"",
			function(result){
				if(result.length>0){
					result.unshift({"TYPE_INFO":"全部","CODE":""});
					JsVar["CODE"].setData(result);
					JsVar["CODE"].select(0);
				}
		});
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
	var mergeCells2="NAME,CODE";
 	var mergeCellColumnIndex2="1,2";
	var mergeData = getMergeCellsOnGroup(gridData, mergeCells2, mergeCellColumnIndex2);
	JsVar["uploadGrid"].mergeCells(mergeData);
}

/**
 * 加载表格
 * @param params
 */
function load(obj){
	obj["FILE_TYPE"] = JsVar["FILE_TYPE"];
	datagridLoadPage(JsVar["uploadGrid"],obj ,null,Globals.ctx+"/nodeopt/queryNodeTypeVersionDetail");
}

/**
 * 运行状态高亮显示
 * @param e
 * @returns
 */
function isFullVersionRenderer(e){
    var run_state=e.record.IS_FULL_VERSION;
    if(run_state == 1){
        return "<span class='label label-success'>全量</span>";
    }else if(run_state == 0){
        return "<span class='label label-danger'>增量</span>";
    }
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
	var row = JsVar["uploadGrid"].getRow(index);
	var obj = {};
	obj["ID"] = row["ID"];
	obj["FILE_NAME"] = row["FILE_NAME"];
	obj["NAME"] = row["NAME"];
	obj["VERSION"] = row["VERSION"];
	obj["CODE"] = row["CODE"];
	showConfirmMessageAlter("确认删除记录?<br>业务类型:"+obj["NAME"]+"<br>业务编码:"+obj["CODE"]+"<br>版本:"+obj["VERSION"],function ok(){
		getJsonDataByPost(Globals.ctx+"/nodeopt/deleteVersion",obj,"业务程序框架版本管理-删除文件信息",
				function(result){
			JsVar["uploadGrid"].reload();
			if(result.success){
				showTip(result.success)
			}else {
				showMessageTips("操作失败!");
			}
		});
	});
}


/**
 *
 * @param index
 */
function showTip(params) {
	var re1 = new RegExp("成功","g"); //定义正则表达式
	var re2 = new RegExp("失败","g"); //定义正则表达式
	var re3 = new RegExp("异常","g"); //定义正则表达式
	// var re3 = new RegExp("\\r\\n","g"); //定义正则表达式
//第一个参数是要替换掉的内容，第二个参数"g"表示替换全部（global）。

	params = params.replace(re1, "<span style='background: green;color: white'>[成功]</span>"); //第一个参数是正则表达式。
	params = params.replace(re2,"<span style='background: red;color: white'>[失败]</span>"); //第一个参数是正则表达式。
	params = params.replace(re3,"<span style='background: red;color: white'>[异常]</span>"); //第一个参数是正则表达式。

	var paramsHtml = "<div style='white-space:pre-wrap;letter-spacing:0.05em;line-height:25px;border-bottom:2px solid #DEE5F4;height:255px;width:99%;overflow:auto;'>" + params + "</div>";
	var options = {
		title: "运行结果",
		width: 800,
		height: 700,
		buttons: ["ok"],
		iconCls: "",
		html: paramsHtml,
		callback: function (action) {

		}
	}
	mini.showMessageBox(options);
}

/**
 * 上传文件弹框
 */
function upload(){
	showAddDialog("业务程序版本管理-文件上传",800,600,Globals.ctx+"/jsp/nodemanager/uploadversion/serviceAddftp",
	        function destroy(data){
	            if (data == systemVar.SUCCESS) {
	            	JsVar["uploadGrid"].reload();
                    showMessageTips("文件上传成功!");
	            }
	    },{FILE_TYPE:JsVar["FILE_TYPE"]});
}
/**
 * 上传文件弹框
 */
function uploadPatch(){
    showAddDialog("业务程序版本管理-版本补丁上传",800,600,Globals.ctx+"/jsp/nodemanager/uploadversion/serviceAddPatchftp",
        function destroy(data){
            if (data == systemVar.SUCCESS) {
                JsVar["uploadGrid"].reload();
                showMessageTips("文件上传成功!");
            }
        },{FILE_TYPE:JsVar["FILE_TYPE"]});
}
