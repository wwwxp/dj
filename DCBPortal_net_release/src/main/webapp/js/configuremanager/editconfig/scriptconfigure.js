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
    //业务类型
    loadBusList();
    //获取sftp服务器下的文件
    loadFilesUnderServer(page_type);
    //过滤掉不存在的业务版本
    filterTree();
    //初始化文本栏
    initTextContent();
    $(window).resize(resizePage);
});

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
			}
	},"clusterEleDefine.queryClusterEleList",null,false);
}

/**
 * 空方法，这个方法是有动态生成绑定的tabload事件需要的，空实现就可以了
 * @param data
 */
function loadPage(data) {

}

/**
 * 获取ftp服务器下的文件列表
 */
function loadFilesUnderServer(page_type){
	params["page_type"]=page_type;
	params["GROUP_CODE"]="WEB_BUS_PACKAGE_TYPE";
	//查询文件，并加载
	treeLoad(Tree["fileTree"], "", params, Globals.baseActionUrl.CONFIGURE_ACTION_SCRIPT_FILE_TREE_URL);
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
			extraKeys: {"Ctrl": "autocomplete"},
			styleActiveLine: true,
			mode: mixedMode,
            gutters: ["CodeMirror-linenumbers", "CodeMirror-foldgutter"]
		});
	} else if(JsVar["formatType"] == "yaml"){
		JsVar["editor"] = CodeMirror.fromTextArea(document.getElementById("content"), {
			lineNumbers: true,
            lineWrapping: true,
			extraKeys: {"Ctrl": "autocomplete"},
			styleActiveLine: true,
			mode:  {name: "yaml", globalVars: true},
            gutters: ["CodeMirror-linenumbers", "CodeMirror-foldgutter"]
		});
	} else if(JsVar["formatType"] == "shell"){
		JsVar["editor"] = CodeMirror.fromTextArea(document.getElementById("content"), {
			lineNumbers: true,
            lineWrapping: true,
			extraKeys: {"Ctrl": "autocomplete"},
			styleActiveLine: true,
			mode:  {name: "shell", globalVars: true},
            gutters: ["CodeMirror-linenumbers", "CodeMirror-foldgutter"]
		});
	} else{
		JsVar["editor"] = CodeMirror.fromTextArea(document.getElementById("content"), {
			lineNumbers: true,
            lineWrapping: true,
			extraKeys: {"Ctrl": "autocomplete"},
			styleActiveLine: true,
			mode:  {name: "javascript", globalVars: true},
            gutters: ["CodeMirror-linenumbers", "CodeMirror-foldgutter"]
		});
	}
    JsVar["editor"].foldCode(CodeMirror.Pos(0, 0));
	JsVar["editor"].on("changes", function (Editor, changes) {
		JsVar["isEditting"]=true;
		//if(params["fileName"]=="sp_switch.xml"){
		//	$("#saveFile").css("display", "none");
		//}else{
			$("#saveFile").css("display", "block");
		//}
	});

	var height=document.documentElement.clientHeight;
	JsVar["editor"].setSize("100%", height-41);
}

/**
 * 树节点点击事件
 * @param e
 */
function onClickTreeNode(e){
	var isFile=e.node.file;
	JsVar["cancelChanges"] = false;
	if(JsVar["isEditting"]){
		showConfirmMessageAlter("文件【"+params["fileName"]+"】未保存，是否继续？",function(){
			JsVar["isEditting"] = false;
			onClickTreeNode(e);
		});
		return;
	}
	
	//存全局变量
	params["fileName"]=e.node.fileName;
	params["filePath"]=e.node.filePath;
	params["isFile"]=e.node.file;
	
	if(params["fileName"].lastIndexOf(".xml") > -1){
		if(JsVar["formatType"] != "xml"){
			JsVar["formatType"]="xml";
			initTextContent();
		}
	} else if(params["fileName"].lastIndexOf(".yaml") > -1){
		if(JsVar["formatType"] != "yaml"){
			JsVar["formatType"]="yaml";
			initTextContent();
		}
	} else if(params["fileName"].lastIndexOf(".sh") > -1){
		if(JsVar["formatType"] != "sh"){
			JsVar["formatType"]="shell";
			initTextContent();
		}
	} else if(params["fileName"].lastIndexOf(".properties") > -1){
		if(JsVar["formatType"] != "properties"){
			JsVar["formatType"]="properties";
			initTextContent();
		}
	} else {
		if(JsVar["formatType"] != "defaul" ){
			JsVar["formatType"]="defaul";
			initTextContent();
		}
	}
	var parentNode=Tree["fileTree"].getParentNode(e.node);
	params["parentNodeName"]=parentNode.fileName;

	//是文件,获取文件内容
	if(isFile==true){
		getJsonDataByPost(Globals.baseActionUrl.CONFIGURE_ACTION_FILE_CONTENT_URL,params,"配置修改-FTP/SFTP获取文件内容",
				function(result){
			if(result!=null){
				var con=result.fileContent;
				JsVar["editor"].setValue(con);
				JsVar["isEditting"] = false;	
				$("#saveFile").css("display", "none");		
			}
		});
		
	}else{
		JsVar["editor"].setValue('');
		var DESCRIPTION="";
		for(var key in busVersionName){
			if(params["fileName"]==key){
				//如果该节点是版本号目录，则查询显示该版本号的描述信息
				DESCRIPTION=busVersionName[key];
			}

		}
		JsVar["editor"].setValue(DESCRIPTION);
		
		JsVar["isEditting"] = false;
		$("#saveFile").css("display", "none");
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
	
	//业务包类型
	var packageType = node["desc"];
	if(!packageType){
		packageType = "";
	}
    var newContent = JsVar["editor"].getValue()==null ? "" : JsVar["editor"].getValue();
    newContent = newContent.replace(/\+/g, "%2B");
	var params = {
		FILE_NAME:node["fileName"],
		FILE_PATH:node["filePath"],
		CLUSTER_TYPE:node["clusterType"],
		NEW_CONTENT:newContent,
		PACKAGE_TYPE:packageType
	};

	//保存和分发
	getJsonDataByPost(Globals.baseActionUrl.CONFIGURE_ACTION_SCRIPT_DISTRIBUTE_URL,params,"配置修改-业务配置-分发修改后的脚本文件",
		function(result){
			if(result.successNum!=undefined && result.errorNum!=undefined){
				if(result.successNum>0 && result.errorNum>0){//有成功有失败  --警告
					JsVar["isEditting"]=false;
                    showWarnMessageAlter(result.isSuccess);
				}else if(result.successNum>0 && result.errorNum<=0){//只有有成功  --成功
					JsVar["isEditting"]=false;
                    showMessageAlter(result.isSuccess);
				}else if(result.successNum<=0 && result.errorNum>0){//只有失败  --错误
					showErrorMessageAlter(result.isSuccess);
				}
			}else{
                showMessageAlter(result.isSuccess);
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
	if(node){
		//先清空右边编辑区
		JsVar["editor"].setValue('');
		
		JsVar["isEditting"] = false;	
		$("#saveFile").css("display", "none");
		JsVar["cancelChanges"] = true;
		JsVar["editor"].on('beforeChange',function(cm,change) {
			if(JsVar["cancelChanges"]){
				change.cancel();
			}
	     });
		//节点是文件夹
		if(node.fileType=="D"){//节点是文件夹
			var isCurrNode = false;
			for (var i=0; i<JsVar["BUS_LIST"].length; i++) {
				if (JsVar["BUS_LIST"][i]["CLUSTER_NAME"] == node["fileName"]){
					isCurrNode = true;
					break;
				}
			}
			if (isCurrNode) {
				$("#delFile").css("display", "none");
				$("#delFolder").css("display", "none");
				$("#addFile").css("display", "block");
				$("#addFolder").css("display", "none");
			} else {
				e.htmlEvent.preventDefault();
				e.cancel = true;
			}
			
		}else if(node.fileType=="F"){//节点是文件
			$("#delFile").css("display", "block");
			$("#delFolder").css("display", "none");
			$("#addFile").css("display", "none");
			$("#addFolder").css("display", "none");
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
	var node = Tree["fileTree"].getSelectedNode();
	var param = new Object();
	param["delFileType"]=type;
	param.fileName=node.fileName;
	param.filePath=node.filePath;
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
 *  新建文件,文件夹
 * @param type
 */
function serviceAddFile(type){
	var node = Tree["fileTree"].getSelectedNode();
	var parent_node=Tree["fileTree"].getParentNode(node);
	var param = new Object();
	param["newFileType"]=type;
	param.fileName=node.fileName;
	param.filePath=node.filePath;
	param.fileType=node.fileType;
	param.page_type=page_type;
	param.children = node.children;
	param.from="script";
	if(type == 'file'){
		showAddDialog("新建配置文件",450, 300,Globals.baseJspUrl.CONFIGURE_JSP_CREATE_FILE_URL,
			function destroy(data){
				if(data.flag==systemVar.SUCCESS){
					 var node = Tree["fileTree"].getSelectedNode();
			         var newNode = {};
			         newNode["fileName"] = data.fileName;
			         newNode["fileType"] = "F";
			         newNode["filePath"] = node.filePath +"/"+param.fileName + "/bin";
			         newNode["directory"] = false;
			         newNode["file"] = true;
			         Tree["fileTree"].addNode(newNode, "add", node);
				}
		},param);
	}else if(type == 'folder'){
		showAddDialog("新建配置目录",400,150,Globals.baseJspUrl.CONFIGURE_JSP_CREATE_FOLDER_URL,
				function destroy(data){
			if(data.flag==systemVar.SUCCESS){
				var node = Tree["fileTree"].getSelectedNode();
		         var newNode = {};
		         newNode["fileName"] = data.fileName;
		         newNode["fileType"] = "D";
		         newNode["filePath"] = node.filePath +"/"+param.fileName;
		         newNode["directory"] = true;
		         newNode["file"] = false;
		         Tree["fileTree"].addNode(newNode, "add", node);
				//treeLoad(Tree["fileTree"],"",params,Globals.baseActionUrl.CONFIGURE_ACTION_FILE_TREE_URL);
			}
		},param);
	}
}

/**
 * 页面尺寸变化时重新渲染页面
 */
function resizePage() {
	var height=document.documentElement.clientHeight;
	JsVar["editor"].setSize("100%", height-41);
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
