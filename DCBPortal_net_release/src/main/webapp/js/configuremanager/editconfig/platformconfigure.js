//定义变量， 通常是页面控件和参数
//var TreeObj = new Object();
var page_type="PLATFORM";

var JsVar = new Object();
var Tree = new Object();
var params=new Object();
var CodeAndIp=new Object();
var isUsedFlag=true;
/**
 * 初始化
 */
$(document).ready(function () {
    mini.parse();
    //取得文件树
    Tree["fileTree"] = mini.get("fileTree");
    //获取sftp服务器下的文件
    loadFilesUnderServer(page_type);
    //初始化文本域
    initTextContent();
    //窗口自适应
    $(window).resize(resizePage);
  
});

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
	var params = {
		page_type:page_type
	};
	//查询文件，并加载
	treeLoad(Tree["fileTree"], "", params, Globals.baseActionUrl.CONFIGURE_ACTION_FILE_TREE_URL);
}

/**
 * 已使用的文件加（isUsed）属性 
 * @param e
 */
function distinIsUsed(e){
	//当前节点
	var currNode=e.node;
	var is_used=currNode.isUsed;
	var fileType=currNode.fileType;
	
	//设置图标
    if (currNode["fileType"] == "D"){
    	e.iconCls = "mini-tree-expand mini-tree-folder";
    }
    
    if (currNode["fileType"] == "D" && currNode["fileName"] == "cfg_templet") {
    	e.iconCls = "mini-tree-templet";
    }
    
	if(is_used==true && fileType=="F"){//文件
		e.nodeHtml += '&nbsp;<img src='+Globals.ctx + '/images/ok.png />';
	} else if(is_used==true && fileType=="D"){//目录
		var childArray=currNode.children;
		for(var i=0;i<childArray.length;i++){
			childArray[i].isUsed=true;
		}
	}
	//遍历所有节点的子节点，如果存在
	var allChildUsed=true;
	var childArray=currNode.children;
	if(childArray!=null && childArray.length>0){
		for(var i=0;i<childArray.length;i++){
			if(childArray[i].isUsed!=true){
				allChildUsed=false;
				break;
			}
		}
		if(allChildUsed){
			currNode.isUsed=true;
			e.nodeHtml += '&nbsp;<img src='+Globals.ctx + '/images/ok.png />';
		}
	}	
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
			extraKeys: {"Ctrl": "autocomplete","Ctrl-Q": function(cm){ cm.foldCode(cm.getCursor()); }},
			styleActiveLine: true,
            foldGutter: true,
			mode: mixedMode,
			gutters: ["CodeMirror-linenumbers", "CodeMirror-foldgutter"]
		});
        JsVar["editor"].foldCode(CodeMirror.Pos(0, 0));
	}else if(JsVar["formatType"] == "yaml"){
		JsVar["editor"] = CodeMirror.fromTextArea(document.getElementById("content"), {
			lineNumbers: true,
            lineWrapping: true,
			extraKeys: {"Ctrl": "autocomplete","Ctrl-Q": function(cm){ cm.foldCode(cm.getCursor()); }},
			styleActiveLine: true,
			mode:  {name: "yaml", globalVars: true},
            foldGutter: true,
			gutters: ["CodeMirror-linenumbers", "CodeMirror-foldgutter"]
		});
        JsVar["editor"].foldCode(CodeMirror.Pos(0, 0));
	}else if(JsVar["formatType"] == "properties"){
		JsVar["editor"] = CodeMirror.fromTextArea(document.getElementById("content"), {
			lineNumbers: true,
			extraKeys: {"Ctrl": "autocomplete","Ctrl-Q": function(cm){ cm.foldCode(cm.getCursor()); }},
			styleActiveLine: true,
			mode:  {name: "properties", globalVars: true},
			gutters: ["CodeMirror-linenumbers", "CodeMirror-foldgutter"]
		});
	}else{
		JsVar["editor"] = CodeMirror.fromTextArea(document.getElementById("content"), {
			lineNumbers: true,
			extraKeys: {"Ctrl": "autocomplete","Ctrl-Q": function(cm){ cm.foldCode(cm.getCursor()); }},
			styleActiveLine: true,
            foldGutter: true,
			mode:  {name: "javascript", globalVars: true},
			gutters: ["CodeMirror-linenumbers", "CodeMirror-foldgutter"]
		});
        JsVar["editor"].foldCode(CodeMirror.Pos(0, 0));
	}
	
	JsVar["editor"].on("changes", function (Editor, changes) {
		JsVar["isEditting"]=true;
		$("#saveFile").css("display", "block");
		if(isUsedFlag){
			mini.get("saveFileButton").setEnabled(false);
		}else{
			mini.get("saveFileButton").setEnabled(true);
		}		
	});

	var height=document.documentElement.clientHeight;
	JsVar["editor"].setSize("100%", height-71);
}

/**
 * 页面尺寸变化时重新渲染页面
 */
function resizePage() {
	var height=document.documentElement.clientHeight;
	JsVar["editor"].setSize("100%", height-71);
};


/**
 * 树节点点击事件
 * @param e
 */
function onClickTreeNode(e){
	var isFile=e.node.file;
	
	JsVar["cancelChanges"] = false;
	if(JsVar["isEditting"]){
		showConfirmMessageAlter("文件【"+e.node["fileName"]+"】未保存，是否继续？",function ok(){
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
	
	JsVar["selectedNode"] = e.node;
	if(JsVar["selectedNode"]["fileName"].lastIndexOf(".xml") > -1){
		if(JsVar["formatType"] != "xml"){
			JsVar["formatType"]="xml";
			initTextContent();
		}
	}else if(JsVar["selectedNode"]["fileName"].lastIndexOf(".yaml") > -1){
		if(JsVar["formatType"] != "yaml"){
			JsVar["formatType"]="yaml";
			initTextContent();
		}
	}else if(JsVar["selectedNode"]["fileName"].lastIndexOf(".properties") > -1){
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
	
	//是文件,获取文件内容
	if(isFile==true){
		var params = {
			fileName : JsVar["selectedNode"]["fileName"],
			filePath : JsVar["selectedNode"]["filePath"],
			clusterId: JsVar["selectedNode"]["clusterId"]
		};
		getJsonDataByPost(Globals.baseActionUrl.CONFIGURE_ACTION_FILE_CONTENT_URL, params, "配置修改-FTP/SFTP获取文件内容",
				function(result){
			if(result!=null){
				
				if (result["REAL_DEPLOY_PATH"] != undefined) {
					$("#tips").html("当前文件在远程主机初始目录: " + result["REAL_DEPLOY_PATH"]);
				} else {
					$("#tips").html("当前文件无需分发到远程主机！");
				}
				
				var con=result.fileContent;
				JsVar["editor"].setValue(con);	
				JsVar["isEditting"] = false;
				$("#saveFile").css("display", "none");
				if(e.node.isUsed){
					isUsedFlag=true;
				}else{
					isUsedFlag=false;
				}
			}
		});
	}else{
		if(JsVar["selectedNode"]["desc"]){
			JsVar["editor"].setValue(JsVar["selectedNode"]["desc"]);
		}else{
			JsVar["editor"].setValue("");
		}
		
		JsVar["isEditting"] = false;
		JsVar["cancelChanges"] = true;
		JsVar["editor"].on('beforeChange',function(cm,change) {
			if(JsVar["cancelChanges"]){
				change.cancel();
			}
  		});
		$("#saveFile").css("display", "none");
	}
	
}

/**
 * 平台：保存并且分发修改后的文件（覆盖原文件）
 */
function saveAndDisFile(e){
	var parentNode = Tree["fileTree"].getParentNode(JsVar["selectedNode"]);
	var param = {};
	//param["new_content"]=JsVar["editor"].getValue();
    var newContent = JsVar["editor"].getValue()==null ? "" : JsVar["editor"].getValue();
    newContent = newContent.replace(/\+/g, "%2B");
    param["new_content"] = newContent;

	param["CLUSTER_TYPE"] = JsVar["selectedNode"]["clusterType"];
	param["CLUSTER_ID"] = JsVar["selectedNode"]["clusterId"];
	param["clusterCode"] = JsVar["selectedNode"]["clusterCode"];
	param["isCluster"] = JsVar["selectedNode"]["isCluster"];
	param["filePath"] = JsVar["selectedNode"]["filePath"];
	param["fileName"] = JsVar["selectedNode"]["fileName"];
	param["targetPath"] = JsVar["selectedNode"]["targetPath"];
	param["parentNodeName"] = parentNode["fileName"];

	//往主机上分发文件
	getJsonDataByPost(Globals.baseActionUrl.CONFIGURE_ACTION_FILE_DISTRIBUTE_URL,param,"配置修改-平台配置-分发修改后的文件",
		function(result){
		if(result.isSuccess){
			JsVar["isEditting"]=false;
		}
		if(result.successNum!=undefined && result.errorNum!=undefined){
			if(result.successNum>0 && result.errorNum>0){//有成功有失败  --警告
				showWarnMessageAlter(result.isSuccess);
			}else if(result.successNum>=0 && result.errorNum<=0){//只有有成功  --成功
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
 * 平台配置：鼠标右键触发事件
 * @param e
 */
function onBeforeOpen(e){
	var node = Tree["fileTree"].getSelectedNode();
	var parent_node=Tree["fileTree"].getParentNode(node);
	var parent_parent_node=Tree["fileTree"].getParentNode(parent_node);
	var grand_parent_node=Tree["fileTree"].getParentNode(parent_parent_node);
	
	if (!node) {//当没有选择节点， 则阻止浏览器的右键菜单
		e.htmlEvent.preventDefault();
		e.cancel = true;
		return;
	}
	if(node["clusterCode"] && node["clusterCode"] != "" && node["clusterCode"] != busVar.CLUSTER_DEFAULT){
		//先清空右边编辑区
		JsVar["editor"].setValue('');
		JsVar["isEditting"] = false;
		$("#saveFile").css("display", "none");
        $("#deleteFolderExDefault").css("display", "none");
        $("#deleteBatch").css("display", "none");
		JsVar["cancelChanges"] = true;
		
		if(node.fileType=='D'){//节点是文件夹
			    $("#renameFile").css("display", "none");
			    //集群根节点可以新建实例
				if(node["clusterRoot"]==true && 
						(node["clusterType"] == busVar["FASTDFS"])) {//fastdfs/dmdb
					$("#newFile").css("display", "none");
					$("#newFolder").css("display", "block");
					$("#deleteFile").css("display", "none");
					$("#deleteFolder").css("display", "none");
				//集群根节点的一级子目录可以新建实例
				}else if(parent_node["clusterRoot"]==true 
						 &&(node["clusterType"] == busVar["DCA"] 
				          || node["clusterType"] == busVar["DMDB"]
				          || node["clusterType"] == busVar["ROCKETMQ"])){
					$("#newFile").css("display", "none");
					$("#newFolder").css("display", "block");
					$("#deleteFile").css("display", "none");
					$("#deleteFolder").css("display", "none");
					if (node["fileName"] == "sentinel") {
						$("#deleteFolderExDefault").css("display", "block");
					}

					//redis节点支持多选删除
					if (node["clusterType"] == "dca" && node["fileType"] == "D" && node["fileName"] == "redis") {
						$("#deleteBatch").css("display", "block");
					}
				}else if(node["clusterRoot"] == false){
					$("#newFile").css("display", "block");
					$("#newFolder").css("display", "none");
					$("#deleteFile").css("display", "none");
					$("#deleteFolder").css("display", "block");
				}else{
					e.htmlEvent.preventDefault();
					e.cancel = true;
					return;
				}
		}else if(node.fileType=='F'){//节点是文件
				 $("#renameFile").css("display", "block");
				//集群下面的文件可以进行删除
				if(node["clusterRoot"]== false ) {
					$("#newFile").css("display", "none");
					$("#newFolder").css("display", "none");
					$("#deleteFile").css("display", "block");
					$("#deleteFolder").css("display", "none");
				}
		}
	}else{
		e.htmlEvent.preventDefault();
		e.cancel = true;
		return;
	}
}

/**
 * 平台配置：新建文件/文件夹
 * @param type
 */
function newFile(type){

	var node = Tree["fileTree"].getSelectedNode();
	
	//配置文件展示中要展示集群编码，集群名称，参数需要调整只保留编码
	var fileName = node.fileName;
	if(fileName && fileName.indexOf("(") != -1){
		node.fileName = fileName.substring(fileName.indexOf("(")+1,fileName.lastIndexOf(")"));
	}
	
	var param = new Object();
	param["newFileType"]=type;
	param["fileName"]=node.fileName;
	param["filePath"]=node.filePath;
	param["page_type"]=page_type;
	param["hostIpDir"]=node.fileName;
	param["CLUSTER_TYPE"]=node["clusterType"];
	param["CLUSTER_CODE"]=node["clusterCode"];
	param["CLUSTER_ID"]=node["clusterId"];
	param["isCluster"] = node["isCluster"];
	param["targetPath"] =node["targetPath"];
	if(type == 'file'){
		var parentNode = Tree["fileTree"].getParentNode(node);
		if (parentNode != null) {
			var childrenList = parentNode["children"];
			for (var i=0; i<childrenList.length; i++) {
				var fileName = childrenList[i]["fileName"];
				if (fileName == busVar.DEFAULT) {
					param["children"] = childrenList[i]["children"];
					break;
				}
			}
		}
		showAddDialog("新建配置文件",450,300,Globals.baseJspUrl.CONFIGURE_JSP_CREATE_FILE_URL,
			function destroy(data){
				if(data.flag==systemVar.SUCCESS){
					var node = Tree["fileTree"].getSelectedNode();
					treeLoad(Tree["fileTree"],"",{page_type:page_type},Globals.baseActionUrl.CONFIGURE_ACTION_FILE_TREE_URL);
					
					var finalNode = Tree["fileTree"].findNodes(function(newNode) {
						if (newNode.targetPath == node.targetPath + "/" + data["fileName"]) {
							//展开当前节点目录
							Tree["fileTree"].expandPath(newNode);
							//选中节点
							Tree["fileTree"].selectNode(newNode);
							//滚动条滚到节点
							Tree["fileTree"].scrollIntoView(newNode);
							return true;
						}
					});
				}
		},param);
	}else if(type == 'folder'){
        var url = Globals.baseJspUrl.CONFIGURE_JSP_CREATE_COPY_FOLDER_URL
		var width = 500;
        var height = 400;
        if (param["CLUSTER_TYPE"] == busVar.DCA
            && param["fileName"] == busVar.REDIS
            && param["hostIpDir"] == busVar.REDIS
			&& Globals.webLatnId == "sx") {
        	url = Globals.baseJspUrl.CONFIGURE_JSP_ADD_COPY_FOLDER_URL;
            width = "60%";
            height = "60%";

            var nodesArray = Tree["fileTree"].getChildNodes(node);
            var list = [];
            for(var i = 0;i< nodesArray.length; i++){
            	var nodeName = nodesArray[i]["fileName"];
            	if(nodeName == "default"){
                    continue;
                }
                var ip = nodeName.split("_")[0]; //IP
                var port = nodeName.split("_")[1]; //ip对应的端口port
                if (list == null || list.length == 0) {
                    list.push({
                        "ip":ip,
                        "port":[port]
                    });
				} else {
                	var isExists = false;
                	for (var j=0;j<list.length;j++) {
                        if (list[j]["ip"] == ip) {
                            list[j]["port"].push(port);
                            isExists = true;
                            break;
						}
					}
					if (!isExists) {
                        list.push({
                            "ip":ip,
                            "port":[port]
                        });
					}
				}
			}
            param["nodesData"] = list;
        }
		//调页面
		showAddDialog("新建实例", width, height, url, function destroy(data){
			if(data.flag==systemVar.SUCCESS){
				var node = Tree["fileTree"].getSelectedNode();
				
				treeLoad(Tree["fileTree"],"",{page_type:page_type},Globals.baseActionUrl.CONFIGURE_ACTION_FILE_TREE_URL);
				
				var finalNode = Tree["fileTree"].findNodes(function(newNode) {
					if (newNode.targetPath == node.targetPath + "/" + data["fileName"]) {
						//展开当前节点目录
						Tree["fileTree"].expandPath(newNode);
						//选中节点
						Tree["fileTree"].selectNode(newNode);
						//滚动条滚到节点
						Tree["fileTree"].scrollIntoView(newNode);
						return true;
					}
				});
			}
		},param);
	}
}

/**
 * 重命名文件
 */
function renameFile(){
	var node = Tree["fileTree"].getSelectedNode();
	var parent_node=Tree["fileTree"].getParentNode(node);
	var parent_parent_node=Tree["fileTree"].getParentNode(parent_node);
	var grandFaNode=Tree["fileTree"].getParentNode(parent_parent_node);
	
	var param = new Object();
	param["fileName"]=node["fileName"];
	param["filePath"]=node["filePath"];
	param["CLUSTER_TYPE"]=node["clusterType"];
	param["CLUSTER_CODE"]=node["clusterCode"];
	param["CLUSTER_ID"]=node["clusterId"];
	param["isCluster"] = node["isCluster"];
	param["targetPath"] =node["targetPath"];
	
	showEditDialog("重命名文件", 400, 150, Globals.baseJspUrl.CONFIGURE_JSP_RENAME_FILE_URL,
			function destroy(data){
		if(data.flag==systemVar.SUCCESS){
			var subNode = Tree["fileTree"].getSelectedNode();
			var node = Tree["fileTree"].getParentNode(subNode);
			
			treeLoad(Tree["fileTree"],"",{page_type:page_type},Globals.baseActionUrl.CONFIGURE_ACTION_FILE_TREE_URL);
			
			var finalNode = Tree["fileTree"].findNodes(function(newNode) {
				if (newNode.targetPath == node.targetPath + "/" + data["fileName"]) {
					//展开当前节点目录
					Tree["fileTree"].expandPath(newNode);
					//选中节点
					Tree["fileTree"].selectNode(newNode);
					//滚动条滚到节点
					Tree["fileTree"].scrollIntoView(newNode);
					return true;
				}
			});

		}
	},param);
}

/**
 * 多选删除节点
 */
function deleteBatchNode(nodeType) {
	var nodeList = [];
    var currRedisNode = Tree["fileTree"].getSelectedNode();
    var delFileList =[];
    var fileNameList = [];
    var delChildrenList = [];
    if (currRedisNode["clusterType"] == "dca" && currRedisNode["fileName"] == "redis") {
        var childrenList = currRedisNode["children"];
        if (childrenList && childrenList.length> 0) {
        	for (var i=0; i<childrenList.length; i++) {
                if (childrenList[i]["checked"] || childrenList[i]["children"][0]["checked"]) {
                    delFileList.push({
                        CLUSTER_ID:currRedisNode["clusterId"],
                        CLUSTER_CODE:currRedisNode["clusterCode"],
                        CLUSTER_TYPE:currRedisNode["clusterType"],
                        targetPath:childrenList[i]["targetPath"],
						fileName:childrenList[i]["fileName"],
						filePath:childrenList[i]["filePath"]
					});
                    fileNameList.push(childrenList[i]["fileName"]);
                    delChildrenList.push(childrenList[i]);
				}
			}
		}
		if (delFileList != null && delFileList.length > 0) {
        	var params = {
                REDIS_INST_LIST:delFileList
			};
            //showConfirmMessageAlter("确定删除选中节点,选中节点有:[" +  fileNameList.join(",") + "]?",function ok(){
            showConfirmMessageAlter("确定删除Redis目录下选中实例?",function ok(){
                getJsonDataByPost(Globals.baseActionUrl.CONFIGURE_ACTION_DELETE_REDIS_BATCH_FILE_URL, params, "配置修改-平台配置-删除Redis节点集群目录",
                    function(result){
                        if(result["optFlag"] != null && result["optFlag"] == "0"){
                        	showMessageAlter(result["isSuccess"], function ok() {
                        		for (var i=0; i<delChildrenList.length; i++) {
                                    Tree["fileTree"].removeNode(delChildrenList[i]);
								}
							});
                        } else {
                        	var succList = result["succList"];
                            showErrorMessageAlter(result["isSuccess"], function ok() {
                                for (var i=0; i<delChildrenList.length; i++) {
                                	if (succList != null && succList.length > 0) {
                                		for (var j=0; j<succList.length; j++) {
                                			if (delChildrenList[i]["fileName"] == succList[j]) {
                                                Tree["fileTree"].removeNode(delChildrenList[i]);
                                                break;
											}
										}
									}
                                }
                            });
						}
                    });
            });
		} else {
        	showWarnMessageTips("请勾选需要删除的节点!")
		}
    } else {
        showWarnMessageTips("请勾选需要删除的节点!");
    }
}

/**
 * 删除文件/文件夹
 */
function deleteFile(type){
	var node = Tree["fileTree"].getSelectedNode();
	var parent_node=Tree["fileTree"].getParentNode(node);
	var param = new Object();
	param["CLUSTER_TYPE"]=node["clusterType"];
	param["CLUSTER_CODE"]=node["clusterCode"];
	param["CLUSTER_ID"]=node["clusterId"];
	param["isCluster"] = node["isCluster"];
	param["targetPath"] =node["targetPath"];
	param.fileName=node["fileName"];
	param.filePath=node["filePath"];
	param.deleteType=type;
	param.page_type=page_type;
	
	
	var deleteType=type;
	if(type=="file"){
		deleteType="文件";
		param.hostIpDir=parent_node.fileName;
	}else if(type=="folder"){
		deleteType="实例";
		param.hostIpDir=node.fileName;
	}
	param.deleteTypeName=deleteType;
	showConfirmMessageAlter("确定删除该"+deleteType+"?",function ok(){
		getJsonDataByPost(Globals.baseActionUrl.CONFIGURE_ACTION_DELETE_FILE_URL,param,"配置修改-平台配置-删除"+deleteType,
				function(result){
			if(result.isSuccess!=null && result.isSuccess!=""){
				showMessageAlter(result.isSuccess,function ok(){
					Tree["fileTree"].removeNode(node);
				});
			}
		});
	});
}

/**
 * 清空当前目录下初default目录的所有实例
 */
function deleteFileExDefault(type) {
    var node = Tree["fileTree"].getSelectedNode();
    var param = new Object();
    param["CLUSTER_TYPE"]=node["clusterType"];
    param["CLUSTER_CODE"]=node["clusterCode"];
    param["CLUSTER_ID"]=node["clusterId"];
    param["isCluster"] = node["isCluster"];
    param["targetPath"] =node["targetPath"];
    param.fileName=node["fileName"];
    param.filePath=node["filePath"];
    showConfirmMessageAlter("确定删除选中目录下的所有非默认实例？",function ok(){
        getJsonDataByPost(Globals.baseActionUrl.CONFIGURE_ACTION_DELETE_ALL_FILE_URL,param,"配置修改-平台配置-清空sentinel目录实例",
            function(result){
                if(result != null && result.optFlag == 0){
                    showMessageAlter(result.isSuccess,function ok(){
                        Tree["fileTree"].removeNode(node);
                    });
                } else {
                    showWarnMessageAlter("删除失败，请检查!");
				}
            });
    });
}

/**
 * 刷新文件目录树
 */
function refresh(){
	mini.get("node_name").setValue("");
	//查询文件，并加载
	treeLoad(Tree["fileTree"],"",{page_type:page_type},Globals.baseActionUrl.CONFIGURE_ACTION_FILE_TREE_URL);
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
