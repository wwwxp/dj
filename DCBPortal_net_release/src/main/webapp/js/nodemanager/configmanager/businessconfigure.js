//定义变量， 通常是页面控件和参数
var page_type="SERVICE";

var JsVar = new Object();
var Tree = new Object();
var params=new Object();
var busVersionInfo=new Object();
var busVersionName=new Object();
/**
 * 初始化
 */
$(document).ready(function () {
    mini.parse();
    Tree["fileTree"]=mini.get("fileTree");
    //初始化文本栏
    initTextContent();
	loadPage(null);
    $(window).resize(resizePage);
});

/**
 * 新进入Tab页签数据
 * @param data
 */
function loadPage(data) {
	mini.parse();
	// JsVar["PACKAGE_TYPE"] = data["CONFIG_VALUE"];
	// JsVar["PACKAGE_TYPE_NAME"] = data["CONFIG_NAME"];
	//获取业务类型
	loadBusList();
	//获取sftp服务器下的文件
    loadFilesUnderServer(page_type);
    //过滤掉不存在的业务版本
    filterTree();
}

/**
 * 获取业务类型，用来选择模板节点
 */
function loadBusList() {
	var params = {
		TYPE:busVar.BUSINESS_TYPE
	};
	//拿到版本和其下业务包
	getJsonDataByPost(Globals.baseActionUrl.FRAME_QUERY_FOR_LIST_URL, params, "业务配置--获取业务类型列表",
		function(result){
			if (result != null && result.length > 0) {
				JsVar["BUS_LIST"] = result;
				//JsVar["BUS_LIST"].push({"CLUSTER_NAME":"default"});
			}
	},"clusterEleDefine.queryClusterEleList",null,false);
}

/**
 * 获取ftp服务器下的文件列表
 */
function loadFilesUnderServer(page_type){
	params["page_type"]=page_type;
	params["bus_package_type"] = JsVar["PACKAGE_TYPE"];
	//查询文件，并加载
	treeLoad(Tree["fileTree"], null, params, Globals.ctx+"/nodecfg/query");
	return;
	//展开所有节点
	//Tree["fileTree"].expandAll();
	
	//将模板节点以及关联子节点删除
	var treeList = Tree["fileTree"].getList();
	for (var m=0; m<treeList.length;m++) {
		var currNode = treeList[m];
		if (currNode["fileType"] == "D"){
	    	for (var i=0; i<JsVar["BUS_LIST"].length; i++) {
	    		if (JsVar["BUS_LIST"][i]["CLUSTER_NAME"] == currNode["fileName"]) {
	    			var parentNode = Tree["fileTree"].getParentNode(currNode);
	        		var firstNode = Tree["fileTree"].getParentNode(parentNode);
	        		if (firstNode["fileName"] == "release") {
	        			//删除模板节点下的数据(分IP模板节点删除)
	        			var childrenNodes = Tree["fileTree"].getChildNodes(currNode);
	        			if(childrenNodes && childrenNodes.length >0){
		        			for (var k=0; k<childrenNodes.length; k++) {
		        				var subChildrenNodes = Tree["fileTree"].getChildNodes(childrenNodes[k]);
		        				if (subChildrenNodes != null && subChildrenNodes.length > 0) {
		        					Tree["fileTree"].removeNodes(subChildrenNodes);
		        				}
		        			}
		        			Tree["fileTree"].removeNodes(childrenNodes);
	        			}
	        			Tree["fileTree"].removeNode(currNode);
	        		}
	        		break;
	    		}
	    	}
	    }
	}
}

/**
 * 节点渲染 （模板节点渲染）
 * @param e
 */
function distinIsUsed(e){
	var currNode=e.node;
	//设置图标
    if (currNode["fileType"] == "D"){
    	e.iconCls = "mini-tree-expand mini-tree-folder";
    	for (var i=0; i<JsVar["BUS_LIST"].length; i++) {
    		if (JsVar["BUS_LIST"][i]["CLUSTER_NAME"] == currNode["fileName"]) {
    			var parentNode = Tree["fileTree"].getParentNode(currNode);
        		var firstNode = Tree["fileTree"].getParentNode(parentNode);
        		if (firstNode["fileName"] == "release") {
        			e.iconCls = "mini-tree-templet";
        		}
        		break;
    		}
    	}
    }
}

/**
 * 节点过滤
 */
function filterTree(){
	var version_busPack = new Object();
	//拿到版本和其下业务包
	getJsonDataByPost(Globals.baseActionUrl.FRAME_QUERY_FOR_LIST_URL,null,"业务配置--查询版本下的业务包名称",
		function(result){
			if(result.length>0){
				$.each(result,function(i,item){
					//数据拼接封装  ocs_v0.0.1-rent   给下面版本号下是否存在该种业务做判断
					version_busPack[item.NAME+'-'+item.CLUSTER_TYPE] = 1;
					//数据封装busVersionName[ocs_v0.0.1]="描述信息....."  业务版本号/描述信息封装
					busVersionName[item.NAME]="上传时间："+item.CRT_DATE+"\n"+item.DESCRIPTION;
					//存name/fileName/version
					busVersionInfo=result;
				});
			}
	},"deployTask.queryVersionAndBusPackage",null,false);
	
//	//拿到树根节点(空的)
//	var rootNode = Tree["fileTree"].getRootNode();
//	
//	//二级节点(release)
//	var releaseNodes=Tree["fileTree"].getChildNodes(rootNode);
//	
//	//版本节点
//	var secondNodes=Tree["fileTree"].getChildNodes(releaseNodes[0]);
//	
//	//需要移除的节点集合
//	var removeNodes = [];
//	if(secondNodes!=undefined){
//		for(var i=0;i<secondNodes.length;i++){
//			var secondNode=secondNodes[i];
//			var secondNodeName=secondNode.fileName;
//			//三级节点(IP或者billing等)
//			var thirdNodes=Tree["fileTree"].getChildNodes(secondNode);
//			if(thirdNodes!=undefined){
//				for(var j=0;j<thirdNodes.length;j++){
//					var thirdNode=thirdNodes[j];
//					var thirdNodeName=thirdNode.fileName;
//					//判断是否是IP
//					if(isIp(thirdNodeName)){//是（192.168.161.25）
//						//billing等
//						var fourNodes=Tree["fileTree"].getChildNodes(thirdNode);
//						//ip下面有多少个业务类是需要移除的
//						var removeTag = 0;
//						if(fourNodes!=undefined){
//							for(var k=0;k<fourNodes.length;k++){
//								var fourNode=fourNodes[k];
//								var fourNodeName=fourNode.fileName;
//								//不存在  ocs_v0.0.1-billing  说明ocs_v0.0.1下没有billing业务包，不显示
//								if(!version_busPack[secondNodeName+'-'+fourNodeName]){
//									removeNodes.push(fourNode);
//									removeTag++;
//								}
//							}
//							
//						}
//						//判断Ip目录下是否还有子节点（ip下所有的业务类与需要移除的业务类个数相同）
//						if(removeTag == fourNodes.length){
//							removeNodes.push(thirdNode);
//						}
//					}else{//是（billing等）
//						if(!version_busPack[secondNodeName+'-'+thirdNodeName]){
//							removeNodes.push(thirdNode);
//						}
//					}
//				}
//			}
//		}
//	}
//	//循环移除不需要的节点
//	for(var i =0 ;i<removeNodes.length;i++){
//		Tree["fileTree"].removeNode(removeNodes[i]);
//	}
}

/**
 * 目录是IP
 */
function isIp(name){
	//判断是否是主机格式
	var reg=new RegExp("^[0-9]{1,3}.[0-9]{1,3}.[0-9]{1,3}.[0-9]{1,3}","ig");
	var isIp=reg.test(name)?true:false;
	return isIp;
}

/**
 * 初始化text
 */	
function initTextContent(){
	if(JsVar["editor"]){
		$("#content").parent().children(".CodeMirror").remove();
	}
	if(JsVar["formatType"] == "xml"){
		var mixedMode = {name: "htmlmixed"};
		JsVar["editor"] = CodeMirror.fromTextArea(document.getElementById("content"), {
			lineNumbers: true,
            lineWrapping: true,
            foldGutter: true,
			extraKeys: {"Ctrl": "autocomplete"},
			styleActiveLine: true,
			mode: mixedMode,
            gutters: ["CodeMirror-linenumbers", "CodeMirror-foldgutter"]
		});
        JsVar["editor"].foldCode(CodeMirror.Pos(0, 0));
	}else if(JsVar["formatType"] == "yaml" ||JsVar["formatType"] == "yml"){
		JsVar["editor"] = CodeMirror.fromTextArea(document.getElementById("content"), {
			lineNumbers: true,
            lineWrapping: true,
            foldGutter: true,
			extraKeys: {"Ctrl": "autocomplete"},
			styleActiveLine: true,
			mode:  {name: "yaml", globalVars: true},
            gutters: ["CodeMirror-linenumbers", "CodeMirror-foldgutter"]
		});
        JsVar["editor"].foldCode(CodeMirror.Pos(0, 0));
	}else{
		JsVar["editor"] = CodeMirror.fromTextArea(document.getElementById("content"), {
			lineNumbers: true,
            lineWrapping: true,
            foldGutter: true,
			extraKeys: {"Ctrl": "autocomplete"},
			styleActiveLine: true,
			mode:  {name: "javascript", globalVars: true},
            gutters: ["CodeMirror-linenumbers", "CodeMirror-foldgutter"]
		});
        JsVar["editor"].foldCode(CodeMirror.Pos(0, 0));
	}

	JsVar["editor"].on("changes", function (Editor, changes) {
		JsVar["isEditting"]=true;
		//if(params["fileName"]=="sp_switch.xml"){
		//	$("#saveFile").css("display", "none");
		//}else{
			$("#saveFile").css("display", "block");
		//}
	});

	var height=document.documentElement.clientHeight;
	JsVar["editor"].setSize("100%", height-71);
}

/**
 * 树节点点击事件
 * @param e
 */
function onClickTreeNode(e){
	var isFile=e.node.file;
	JsVar["cancelChanges"] = false;
	if(JsVar["isEditting"]){
		showConfirmMessageAlter("文件【"+params["fileName"]+"】未保存，是否继续？",function ok(){
			JsVar["isEditting"] = false;
			onClickTreeNode(e);
		}, function cancel() {
			JsVar["isEditting"] = true;
			Tree["fileTree"].selectNode(JsVar["SEL_NODE"]);
		});
		return;
	}
	
	//保存选中的节点，后续保存另外文件使用
	JsVar["SEL_NODE"] = e.node;
	
	//存全局变量
	params["fileName"]=e.node.fileName;
	params["filePath"]=e.node.filePath;
	params["isFile"]=e.node.file;
	
	if(params["fileName"].lastIndexOf(".xml") > -1){
		if(JsVar["formatType"] != "xml"){
			JsVar["formatType"]="xml";
			initTextContent();
		}
	}else if(params["fileName"].lastIndexOf(".yaml") > -1 ||params["fileName"].lastIndexOf(".yml") > -1 ){
		if(JsVar["formatType"] != "yaml"){
			JsVar["formatType"]="yaml";
			initTextContent();
		}
	}else if(params["fileName"].lastIndexOf(".properties") > -1){
		if(JsVar["formatType"] != "properties"){
			JsVar["formatType"]="properties";
			initTextContent();
		}
	}else {
		if(JsVar["formatType"] != "defaul" ){
			JsVar["formatType"]="defaul";
			initTextContent();
		}
	}
	var parentNode=Tree["fileTree"].getParentNode(e.node);
	params["parentNodeName"]=parentNode.fileName;

	//是文件,获取文件内容
	if(isFile==true){
		getJsonDataByPost(Globals.ctx+"/nodecfg/getFileContent",params,"配置修改-FTP/SFTP获取文件内容",
			function(result){
				if(result!=null){
					
					if (result["REAL_DEPLOY_PATH"] != undefined) {
						$("#tips").html("当前文件在远程主机初始目录: " + result["REAL_DEPLOY_PATH"]);
					} else {
						$("#tips").html("当前文件在远程主机不存在！");
					}
					
					var con=result.fileContent;
					JsVar["editor"].setValue(con);
					JsVar["isEditting"] = false;	
					//$("#saveFile").css("display", "none");
				}
		});
		
	}else{
		JsVar["editor"].setValue('');
		var DESCRIPTION=JsVar["SEL_NODE"]["desc"]==undefined?"":JsVar["SEL_NODE"]["desc"];
		if(JsVar["SEL_NODE"]["targetPath"]!=undefined){
			$("#tips").html("当前文件在远程主机初始目录: " + JsVar["SEL_NODE"]["targetPath"]);
		}
		// for(var key in busVersionName){
		// 	if(params["fileName"]==key){
		// 		//如果该节点是版本号目录，则查询显示该版本号的描述信息
		// 		DESCRIPTION=busVersionName[key];
		// 	}
		//
		// }
		DESCRIPTION = "版本描述信息\n" + DESCRIPTION;
		JsVar["editor"].setValue(DESCRIPTION);
		
		JsVar["isEditting"] = false;
		//$("#saveFile").css("display", "none");
		JsVar["cancelChanges"] = true;
		JsVar["editor"].on('beforeChange',function(cm,change) {
			if(JsVar["cancelChanges"]){
				change.cancel();
			}
  		});
  		
	}
}

/**
 * 保存文件
 * @param e
 */
function saveFile(e){
	//配置文件修改后内容

	var node = Tree["fileTree"].getSelectedNode();
    var newContent = JsVar["editor"].getValue()==null ? "" : JsVar["editor"].getValue();
    newContent = newContent.replace(/\+/g, "%2B");
    //newContent = newContent.replace(/\+/g, "%2B").replace(/\&/g, "%26");
	var params = {
		nodeparam:JSON.stringify(node),
		fileContent:newContent
	};

	//保存和分发
	getJsonDataByPost(Globals.ctx+"/nodecfg/updateCfgAndPublish",params,"配置修改-业务配置-分发修改后的文件",
		function(result){
				if(result.success){
					showTip(result.success);
					JsVar["isEditting"]=false;
				}else {
					showMessageTips("操作失败！");
				}
			// if(result.successNum!=undefined && result.errorNum!=undefined){
			// 	if(result.successNum>0 && result.errorNum>0){//有成功有失败  --警告
			// 		JsVar["isEditting"]=false;
            //         showWarnMessageAlter(result.isSuccess)
			// 	}else if(result.successNum>0 && result.errorNum<=0){//只有有成功  --成功
			// 		JsVar["isEditting"]=false;
            //         showMessageAlter(result.isSuccess);
			// 	}else if(result.successNum<=0 && result.errorNum>0){//只有失败  --错误
			// 		showErrorMessageAlter(result.isSuccess);
			// 	}
			// }else{
            //     showMessageAlter(result.isSuccess);
			// }
	});
}

/**
 * 保存文件
 * @param e
 */
function saveFile_BAK(e){
	//配置文件修改后内容
	params["fileContent"]=JsVar["editor"].getValue();
	var node = Tree["fileTree"].getSelectedNode();
	var nodeParam = JSON.stringify(node);
	params["nodeparam"] = nodeParam;
	// var topPath="";
	// var serviceObject=new Object();
	// serviceObject["TYPE"]=3;//初始值3，业务配置
	// var personal_conf;
	
	// //查询业务配置  找到对应的service_type_code
	// getJsonDataByPost(Globals.baseActionUrl.FRAME_QUERY_FOR_LIST_URL,serviceObject,"业务配置--查询业务类型",
	// 	function(result){
	// 	if(result.length>0){
	// 		$.each(result,function(i,item){
	// 			//遍历节点
	// 			Tree["fileTree"].bubbleParent(node, function(n){
	// 				if(n.fileName == item.CLUSTER_CODE){
	// 					params["CLUSTER_ID"] = item.CLUSTER_ID;
	// 					params["CLUSTER_CODE"] = item.CLUSTER_CODE;
	// 					params["CLUSTER_TYPE"] = item.CLUSTER_TYPE;
	// 					personal_conf = item.PERSONAL_CONF;
	//
	// 					//判断父节点是否是主机格式
	// 					var currParentNode = Tree["fileTree"].getParentNode(node);
	// 					var grantParentNode = Tree["fileTree"].getParentNode(currParentNode);
	// 					var reg = new RegExp("^[0-9]{1,3}.[0-9]{1,3}.[0-9]{1,3}.[0-9]{1,3}","ig");
	// 					params["HOST_IP"] = reg.test(grantParentNode.fileName) ? grantParentNode.fileName:null;
	// 					return false;
	// 				}
	// 			});
	// 		});
	// 	}
	// },"serviceType.queryPersonalConfByCode",null,false);
	//
	//
	// serviceObject.FILE_TYPE=2;
	// //查询上传包数据表  找到对应的上传的fileName(上传的包名)
	// if(busVersionInfo.length>0){
	// 	$.each(busVersionInfo,function(i,item){
	// 		//遍历节点
	// 		Tree["fileTree"].bubbleParent(node,function(n){
	// 			if(n.fileName == item.NAME){
	// 				params["packageFileName"] = item.FILE_NAME;
	// 				params["VERSION"] = item.VERSION;
	// 				return false;
	// 			}
	// 		});
	// 	});
	// }
	//
	// var temp=0;
	// //拼接路径
	// Tree["fileTree"].bubbleParent(node, function(n){
	// 	if(n.fileName == params["CLUSTER_TYPE"]){
	// 		topPath = n.fileName + "/"+topPath;
	// 		temp=1;
	// 	}else{
	// 		if(temp==0 && n.fileName!=params["fileName"]){
	// 			topPath =n.fileName + "/"+topPath;
	// 		}
	// 	}
	// });
	// params["topPath"]=topPath;
	//
	// if(params["CLUSTER_TYPE"] == 'billing' || params["CLUSTER_TYPE"] == 'rent'){
	// 	var parent_node = Tree["fileTree"].getParentNode(node);
	// 	if(parent_node.fileName=="rebalance"){
	// 		params["is_doRebalance"] = "nimbus";//查询nimbus主机，一起保存文件
	// 	}
	//
	// }
	//保存和分发
	getJsonDataByPost(Globals.ctx+"/nodecfg/updateCfgAndPublish",params,"配置修改-业务配置-分发修改后的文件",
		function(result){
			if(result.successNum!=undefined && result.errorNum!=undefined){
				if(result.success){
					showMessageTips(result.success);
				}else {
					showMessageTips("操作失败！");
				}
				return;
				if(result.successNum>0 && result.errorNum>0){//有成功有失败  --警告
					JsVar["isEditting"]=false;
                    showWarnMessageTips(result.isSuccess);
				}else if(result.successNum>0 && result.errorNum<=0){//只有有成功  --成功
					JsVar["isEditting"]=false;
                    showMessageTips(result.isSuccess);
				}else if(result.successNum<=0 && result.errorNum>0){//只有失败  --错误
					showErrorMessageAlter(result.isSuccess);
				}
			}else{
                showMessageTips(result.isSuccess);
			}
	});
}

/**
 * 鼠标右键触发事件
 * @param e
 */
function serviceOnBeforeOpen(e){
	var node = Tree["fileTree"].getSelectedNode();
	if (!node) {//当没有选择节点， 则阻止浏览器的右键菜单
		e.htmlEvent.preventDefault();
		e.cancel = true;
		return;
	}
	var isUsed=node.isUsed;
	if(node){
		//先清空右边编辑区
		JsVar["editor"].setValue('');
		
		JsVar["isEditting"] = false;	
		//$("#saveFile").css("display", "none");
		JsVar["cancelChanges"] = true;
		JsVar["editor"].on('beforeChange',function(cm,change) {
			if(JsVar["cancelChanges"]){
				change.cancel();
			}
	     });
		//节点是文件夹
		if(node.fileType=="D" && isUsed!=true){//节点是文件夹
			$("#addFile").css("display", "block");
			$("#addFolder").css("display", "block");
			$("#delFile").css("display", "none");
			$("#delFolder").css("display", "block");
			if(Tree["fileTree"].getLevel(node)!=3 || node.fileName != 'other'){
				$("#batchAddFile").css("display", "none");
			}else{
				$("#batchAddFile").css("display", "block");
			}
		}else if(node.fileType=="F"){//节点是文件
			$("#delFile").css("display", "block");
			$("#delFolder").css("display", "none");
			$("#addFile").css("display", "none");
			$("#addFolder").css("display", "none");
			$("#batchAddFile").css("display", "none");
		}else{//阻止右键菜单
			e.htmlEvent.preventDefault();
			e.cancel = true;
			return;
		}
	}
}

/**
 * 删除文件、文件夹
 * @param type
 */
function serviceDelFile(type){
	showWarnMessageTips("当前版本不支持删除操作！");
	return;
	var node = Tree["fileTree"].getSelectedNode();
	var param = new Object();
	param["delFileType"]=type;
	param.fileName=node.fileName;
	param.filePath=node.targetPath;
	param.fileType=node.fileType;
	param.page_type=page_type;
	var file_word="删除"+param.fileName+"文件";
	var folder_word="删除"+param.fileName+"文件夹";
	var title=(type=="file")?file_word:folder_word;
	showConfirmMessageAlter("确认"+title+"?", function ok(){
		getJsonDataByPost(Globals.baseActionUrl.CONFIGURE_ACTION_DELETE_FILE_URL,param,"配置修改-业务配置-分发修改后的文件",
			function(result){
				showMessageTips(title+"成功！");
				Tree["fileTree"].removeNode(node);
		});
	});
}


/**
 * 删除文件、文件夹
 * @param type
 */
function delBatchFile(){
	var idsStr = Tree["fileTree"].getValue(true);
	var ids = idsStr.split(",");
	if(ids[0].length > 0){
		var obj = [];
		for(var i = 0 ; i < ids.length ;i++){
			var node = Tree["fileTree"].getNode(ids[i]);
			var param = new Object();
			param.fileName=node.fileName;
			param.filePath=node.targetPath;
			param.fileType=node.fileType; 
			param.currId=ids[i]; 
			param.page_type=page_type;
			if(node.fileType =='F'){
				obj.push(param);
			}
		}
		if(obj.length >0){
			var data = {"list":obj,"page_type":page_type,"flag":"batch"};
			showConfirmMessageAlter("确认批量删除选中的文件吗?", function ok(){
				getJsonDataByPost(Globals.baseActionUrl.CONFIGURE_ACTION_DELETE_FILE_URL,data,"配置修改-业务配置-分发修改后的文件",
					function(result){
						showMessageTips("批量删除成功！");
						for(var i = 0 ; i < obj.length ;i++){
							var dnode = Tree["fileTree"].getNode(obj[i]["currId"]);
							Tree["fileTree"].removeNode(dnode);
						}
				});
			});
		}else{
			showMessageTips("请选中文件！");
		}
		
		
	}else{
		showMessageTips("请选中节点！");
	}
	
	 
	
}
/**
 * 批量新增
 */
function addBatchFolder(){
	var node = Tree["fileTree"].getSelectedNode();
}
function S4() {
    return (((1+Math.random())*0x10000)|0).toString(16).substring(1);
}
function guid() {
    return (S4()+S4()+"-"+S4()+"-"+S4()+"-"+S4()+"-"+S4()+S4()+S4());
}


/**
 *  新建文件,文件夹
 * @param type
 */
function serviceAddFile(type){
	var node = Tree["fileTree"].getSelectedNode();
	var parent_node=Tree["fileTree"].getParentNode(node);
	var param = new Object();
	param["newFileType"]=type;
	param.fileName=node.fileName;
	param.filePath=node.targetPath;
	param.fileType=node.fileType;
	param.page_type=page_type;
	param.children = node.children;
	if(type == 'file'){
		showAddDialog("新建配置文件",450, 300,Globals.baseJspUrl.CONFIGURE_JSP_CREATE_FILE_URL,
			function destroy(data){
				if(data.flag==systemVar.SUCCESS){
					 var node = Tree["fileTree"].getSelectedNode();
			         var newNode = {};
			         newNode["fileName"] = data.fileName;
			         newNode["fileType"] = "F";
			         newNode["currId"] = guid();
			         
			         newNode["filePath"] = node.targetPath;
			         newNode["targetPath"] =newNode["filePath"] +"/"+newNode["fileName"];
			         newNode["directory"] = false;
			         newNode["file"] = true;
			         Tree["fileTree"].addNode(newNode, "add", node);
			         Tree["fileTree"].selectNode(newNode);
				}
		},param);
	}else if(type == 'folder'){
		showAddDialog("新建配置目录",400,150,Globals.baseJspUrl.CONFIGURE_JSP_CREATE_FOLDER_URL,
				function destroy(data){
			if(data.flag==systemVar.SUCCESS){
				var node = Tree["fileTree"].getSelectedNode();
		         var newNode = {};
		         newNode["fileName"] = data.fileName;
		         newNode["currId"] = guid();
		         newNode["fileType"] = "D";
		         newNode["filePath"] = node.targetPath;
		         newNode["targetPath"] =newNode["filePath"] +"/"+newNode["fileName"];
		         newNode["directory"] = true;
		         newNode["file"] = false;
		         Tree["fileTree"].addNode(newNode, "add", node);
				//treeLoad(Tree["fileTree"],"",params,Globals.baseActionUrl.CONFIGURE_ACTION_FILE_TREE_URL);
			}
		},param);
	}else if(type == 'batch'){
		param.filePath=node.filePath;
		param.targetPath = node.targetPath;
		showAddDialog("批量新增",450, 300,Globals.baseJspUrl.CONFIGURE_JSP_CREATE_BATCH_FILE_URL,
				function destroy(data){
					if(data.flag==systemVar.SUCCESS){
						var node = Tree["fileTree"].getSelectedNode();
						var latnids = data.LATNS.split(',');
						var latnnames =  data.LATN_NAMES.split(',')
						var childNodes = Tree["fileTree"].getChildNodes(node);
						for(var i = 0 ; i < latnids.length;i++){
							 var newNode = {};
					         newNode["fileName"] = latnnames[i];
					         newNode["currId"] = guid();
					         newNode["fileType"] = "D";
					         newNode["filePath"] = node.targetPath;
					         newNode["targetPath"] =newNode["filePath"] +"/"+latnids[i];
					         newNode["directory"] = true;
					         newNode["file"] = false;
					         var isCreateDir = false;
					         var dirNode ;
					         if(childNodes && childNodes.length > 0){
						         for(var k = 0 ; k < childNodes.length;k++){
						        	 var tmpNode = childNodes[k];
						        	 if(tmpNode["fileName"] == latnnames[i] && tmpNode["fileType"]=="D"){
						        		 isCreateDir = true;
						        		 dirNode = childNodes[k];
						        		 break;
						        	 }
						         }
					         }
					         if(!isCreateDir){ 
					        	 Tree["fileTree"].addNode(newNode, "add", node);
					        	 dirNode = newNode;
					        	
					         } 
					         
					         var copynames = data.FILE_NAMES.split(',');
					         if(copynames && copynames.length >0 && copynames[0]){
						         for(var j = 0 ; j < copynames.length;j++){
						        	 var newfileNode = {};
						        	 var prefix = copynames[j].substring(0, copynames[j].lastIndexOf("."));
									 var suffix = copynames[j].substring(copynames[j].lastIndexOf("."),copynames[j].length);
						        	 var fname = prefix +"_"+ latnids[i] + suffix
									 newfileNode["fileName"] = fname;
						        	 newfileNode["fileType"] = "F";
						        	 newfileNode["currId"] = guid();
						        	 newfileNode["filePath"] = dirNode.targetPath;
						        	 newfileNode["targetPath"] = dirNode.targetPath +"/"+fname;
						        	 newfileNode["directory"] = false;
							         newfileNode["file"] = true;
							         Tree["fileTree"].addNode(newfileNode, "add", dirNode);
						         }
						      }
						}
				         Tree["fileTree"].selectNode(dirNode);
				         showMessageTips("批量新增文件成功！");
					}
			},param);
	}
}

/**
 * 页面尺寸变化时重新渲染页面
 */
function resizePage() {
	var height=document.documentElement.clientHeight;
	JsVar["editor"].setSize("100%", height-71);
};

/**
 * 刷新文件目录树
 */
function refresh(){
	mini.get("node_name").setValue("");
	//获取sftp服务器下的文件
    loadFilesUnderServer(page_type);
    //过滤掉不存在的业务版本
    filterTree();
}

/**
 * 搜索树节点
 */
function searchTree() {
    var node_name = mini.get("node_name").getValue();
    if (node_name == "") {
    	Tree["fileTree"].clearFilter();
    }else {
    	node_name = node_name.toLowerCase();
        Tree["fileTree"].filter(function (node) {
            var text = node.fileName ? node.fileName.toLowerCase() : "";
            if (text.indexOf(node_name) != -1) {
                return true;
            }
        });
    }
}

/**
 *
 * @param index
 */
function showTip(params){
	var paramsHtml="<textarea style='letter-spacing:0.05em;line-height:25px;border-bottom:2px solid #DEE5F4;height:255px;width:99%;overflow:auto;'>"+params+"</textarea>";
	var options={
		title: "运行结果",
		width:800,
		height:700,
		buttons: ["ok"],
		iconCls: "",
		html: paramsHtml,
		callback: function(action){

		}
	}
	mini.showMessageBox(options);
}
