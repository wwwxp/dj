//定义变量， 通常是页面控件和参数
var page_type="SERVICE";

var JsVar = new Object();
var Tree = new Object();
var params=new Object();
var CodeAndIp=new Object();
/**
 * 初始化
 */
$(document).ready(function () {
    mini.parse();
    Tree["fileTree"]=mini.get("fileTree");
    //获取sftp服务器下的文件
    loadFilesUnderServer(page_type);
    initTextContent();
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
	params["page_type"]=page_type;
	//参数GROUP_CODE：写死
	JsVar["GROUP_CODE"]="WEB_FTP_CONFIG";
	getJsonDataByPost(Globals.baseActionUrl.FRAME_QUERY_FOR_LIST_URL,JsVar,"配置修改-FTP/SFTP根目录查询",
			function(result){
		if(result.length>0){
			$.each(result,function(i,item){
				params[item.CONFIG_NAME]=item.CONFIG_VALUE;						
			});
			//查询文件，并加载
			treeLoad(Tree["fileTree"],"",params,Globals.baseActionUrl.CONFIGURE_ACTION_FILE_TREE_URL);
		}
	},"config.queryConfigList");
}

/**
 * 初始化text
 */	
function initTextContent(){
	var height=document.documentElement.clientHeight;
	JsVar["editor"] = CodeMirror.fromTextArea(document.getElementById("content"), {
		lineNumbers: true,
        lineWrapping: true,
		extraKeys: {"Ctrl": "autocomplete"},
		styleActiveLine: true,
		mode: {name: "javascript", globalVars: true},
        gutters: ["CodeMirror-linenumbers", "CodeMirror-foldgutter"]
	});
    JsVar["editor"].foldCode(CodeMirror.Pos(0, 0));
	JsVar["editor"].on("changes", function (Editor, changes) {
		JsVar["isEditting"]=true;
		$("#saveFile").css("display", "block");
	});

	JsVar["editor"].setSize("100%", height-41);
}

/**
 * 树节点点击事件
 * @param e
 */
function onClickTreeNode(e){
	var isFile=e.node.file;
	//存全局变量
	params["fileName"]=e.node.fileName;
	params["filePath"]=e.node.filePath;
	var parentNode=Tree["fileTree"].getParentNode(e.node);
	params["parentNodeName"]=parentNode.fileName;

	//是文件,获取文件内容
	if(isFile==true){
		getJsonDataByPost(Globals.baseActionUrl.CONFIGURE_ACTION_FILE_CONTENT_URL,params,"配置修改-FTP/SFTP获取文件内容",
				function(result){
			if(result!=null){
				var con=result.fileContent;
				JsVar["editor"].setValue(con);			
			}
		});
	}else{
		JsVar["editor"].setValue('');
		$("#saveFile").css("display", "none");
	}
}


/**
 * 业务配置：新建文件或文件夹
 */
function serviceNewFile(type){
	var node = Tree["fileTree"].getSelectedNode();
	var parent_node=Tree["fileTree"].getParentNode(node);
	var param = new Object();
	param["newFileType"]=type;
	param["sftpInfo"]=params;
	param.fileName=node.fileName;
	param.filePath=node.filePath;
	param.fileType=node.fileType;

	if(type == 'file'){//新建文件
		//只能新建文件
		showAddDialog("新建文件",400,150,Globals.baseJspUrl.CONFIGURE_JSP_CREATE_FILE_URL,
				function destroy(data){
			if(data=="success"){
                showMessageTips("新建文件成功!");
				treeLoad(Tree["fileTree"],"",params,Globals.baseActionUrl.CONFIGURE_ACTION_FILE_TREE_URL);
//				Tree["fileTree"].selectNode(parent_node);
//				Tree["fileTree"].expandNode(parent_node);
			}else if(data=="fail"){
				showErrorMessageAlter("新建文件失败!");
			}
		},param);
	}else if(type == 'folder'){//新建文件夹
		//新建的只能是rebalance文件夹
		param.newFolderName="rebalance";
		showAddDialog("新建文件夹",400,150,Globals.baseJspUrl.CONFIGURE_JSP_CREATE_FILE_URL,
				function destroy(data){
			if(data=="success"){
                showMessageTips("新建文件夹成功!");
				treeLoad(Tree["fileTree"],"",params,Globals.baseActionUrl.CONFIGURE_ACTION_FILE_TREE_URL);
//				Tree["fileTree"].selectNode(parent_node);
//				Tree["fileTree"].expandNode(parent_node);
			}else if(data=="fail"){
				showErrorMessageAlter("新建文件夹失败!");
			}
		},param);
	}
}


/**
 *业务配置：鼠标右键触发事件
 * @param e
 */
function serviceOnBeforeOpen(e){
	var node = Tree["fileTree"].getSelectedNode();
	var parent_name;
	if (!node) {//当没有选择节点， 则阻止浏览器的右键菜单
		e.htmlEvent.preventDefault();
		e.cancel = true;
		return;
	}
	if(node){
		//先清空右边编辑区
		JsVar["editor"].setValue('');
		$("#saveFile").css("display", "none");
		var parNode=Tree["fileTree"].getParentNode(node);
		var grandNode=Tree["fileTree"].getParentNode(parNode);
		//节点是文件夹rocketmq
		if( node.file==false && parNode.fileName=="topology"){
			$("#newFolder").css("display", "block");
			$("#newFile").css("display", "none");
		}else if(node.file==false && node.fileName=="rebalance" && grandNode.fileName=="topology"){//节点是rebalance文件,父类是topology
			$("#newFile").css("display", "block");
			$("#newFolder").css("display", "none");
		}else{
			e.htmlEvent.preventDefault();
			e.cancel = true;
			return;
		}
	}
}

/**
* 业务：保存修改后的文件（覆盖原文件，serviceconfigure中的方法）
*/
function saveFile(e){
	
	var FILE_CODE;
	params["new_content"]=JsVar["editor"].getValue();
	
	var node = Tree["fileTree"].getSelectedNode();
	var topPath="";
	var child_Nodes=new Array();
	var serviceObject=new Object();
	serviceObject["PLATFORM_BUSINESS"]=1;//初始值1，业务类型
	var personal_conf;
	//查询业务配置  所有类型
	getJsonDataByPost(Globals.baseActionUrl.FRAME_QUERY_FOR_LIST_URL,serviceObject,"业务配置--查询所有类型",
		function(result){
		if(result.length>0){
			$.each(result,function(i,item){
				//遍历节点
				Tree["fileTree"].bubbleParent(node,function(n){
					if(n.fileName==item.CODE){
						params["SERVICE_TYPE_CODE"]=item.CODE;
						personal_conf=item.PERSONAL_CONF;
						//获取该节点的子节点
						child_Nodes=Tree["fileTree"].getChildNodes(n);
					}
				});
			})
			
			//遍历根节点
			Tree["fileTree"].bubbleParent(node,function(n){
				if(n.fileName =='release'){
					return false;
				}
				if(node.fileName != n.fileName){
					//得到从top目录的路径：例如topology/topology_v1.93/conf/，相当于每个主机的位置的相对目录
					if(n.fileName =='topology'){
						topPath =n.fileName + "/"+topPath ;
						params["SERVICE_TYPE_CODE"]='topology';
					}else if(n.fileName ==params["SERVICE_TYPE_CODE"]){//得到从top目录的路径：例如np/np_v1.93/conf/，相当于每个主机的位置的相对目录
						topPath =n.fileName +"/"+ topPath ;
					}else{
						var temp=0;
						//类型为1
						if(personal_conf=="1"){
							for(var i=0;i<child_Nodes.length;i++){
								//去掉主机IP目录
								if(child_Nodes[i].fileName==n.fileName){
									temp=1;
								}
							}
						}
						if(temp==0){
							topPath =n.fileName + "/"+topPath;
							FILE_CODE=n.fileName;
						}
					}
				}
			});
			
		}
	},"serviceType.queryPersonalConfByCode",null,false);
	
	//保存和分发
	params["topPath"]=topPath;	
	params["FILE_CODE"]=FILE_CODE;
	//往主机上分发文件
	getJsonDataByPost(Globals.baseActionUrl.CONFIGURE_ACTION_FILE_SAVE_URL,params,"配置修改-业务配置-分发修改后的文件",
			function(result){
		if(result.isSuccess=="true"){
            showMessageTips("文件保存成功!");
		}else{
			showErrorMessageAlter("文件保存失败!");
		}
	});
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
	//查询文件，并加载
	treeLoad(Tree["fileTree"],"",params,Globals.baseActionUrl.CONFIGURE_ACTION_FILE_TREE_URL);
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