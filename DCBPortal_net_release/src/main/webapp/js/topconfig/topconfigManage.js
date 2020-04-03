 //定义变量， 通常是页面控件和参数
var JsVar = new Object();
/**
 * 初始化
 */
$(document).ready(function () {
    mini.parse();
    JsVar["fileTree"] = mini.get("fileTree");//取得文件树
    loadFilesTree();
    initTextContent();
    $(window).resize(resizePage);
    
});


/**
 * 页面尺寸变化时重新渲染页面
 */
function resizePage() {
	var height=document.documentElement.clientHeight;
	JsVar["editor"].setSize("100%", height-40);

};
	
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
//	        theme:"3024-night",
	        mode: {name: "javascript", globalVars: true},
            gutters: ["CodeMirror-linenumbers", "CodeMirror-foldgutter"]
	      });
	     JsVar["editor"].foldCode(CodeMirror.Pos(0, 0));
	     JsVar["editor"].on("changes", function (Editor, changes) {
	    	 JsVar["isEditting"]=true;
	    	 $("#saveFile").css("display", "block");
	    	});
	     
	     JsVar["editor"].setSize("100%", height-40);
	    
}



/**
 * 加载文件目录树
 */
function loadFilesTree(){
	treeLoad(JsVar["fileTree"],"",null,Globals.baseActionUrl.TOPCONFIG_ACTION_FILE_TREE_URL);
	  
	//fitler();
}

/**
 * 刷新文件目录树
 */
function refresh(){
	mini.get("node_name").setValue("");
	loadFilesTree();
}

/**
 * 搜索树节点
 */
function searchTree() {
    var node_name = mini.get("node_name").getValue();
   
    if (node_name == "") {
    	JsVar["fileTree"].clearFilter();
    }
    else {
    	node_name = node_name.toLowerCase();
        JsVar["fileTree"].filter(function (node) {
            var text = node.name ? node.name.toLowerCase() : "";
            if (text.indexOf(node_name) != -1) {
                return true;
            }
        });
    }
  
}
function fitler(){
	JsVar["fileTree"].filter(function (node) {
		if (node.directory == true) {
			 if(node.name.indexOf("conf") != -1){
				 return true;
			 }
		}else{
			var nodes = JsVar["fileTree"].getAncestors(node);
			for(var i = 0 ; i < nodes.length ; i++){
				if(nodes[i].name.indexOf("conf") != -1){
					return true;
				}
			}
		}
		
    /*	if (node.directory == true) {
        	var childs = JsVar["fileTree"].getAllChildNodes(node);
        	if(childs.length<1){
        		return false;
        	}else{
        		return true;
        	}
        }else{
        	return true;
        } */
    });
}



/**
 * 点击目录树触发事件
 */
function onClickTreeNode(){
	var node = JsVar["fileTree"].getSelectedNode();
	if(node.directory == false){
		getJsonDataByPost(Globals.baseActionUrl.TOPCONFIG_ACTION_OPEN_FILE_URL,null,null,
		function (result){
			 JsVar["editor"].setValue(result.content);
		})
		
	}else{
		 JsVar["editor"].setValue('');
		 $("#saveFile").css("display", "none");
	}
	
}



/**
 * 是否正在编辑中
 * @returns
 */
function isEditing(){
	return JsVar["isEditting"];
	
}

/**
 * 保存页面编辑内容
 */
function saveFilePage(){
	var node = JsVar["fileTree"].getSelectedNode();
	 var topPath="";
	 var flag;
	 JsVar["fileTree"].bubbleParent(node,function(n){
		 if(n.name =='config'){
				return false;
			 }
		 if(node.name != n.name){
			//得到从top目录的路径：例如topology/topology_v1.93/conf/，相当于每个主机的位置的相对目录
			 if(n.name =='topology'){
				 flag= 'topology';
				 topPath =n.name + "/"+topPath + "conf/";
			 } 
			 else if(n.name =='np'){//得到从top目录的路径：例如np/np_v1.93/conf/，相当于每个主机的位置的相对目录
				 flag = 'np';
				 topPath =n.name +"/"+ topPath + "conf/";
			 }else if(n.name =='prepayment'){//得到从top目录的路径：例如prepayment/np_v1.93/conf/，相当于每个主机的位置的相对目录
				 flag = 'prepayment';
				 topPath =n.name +"/"+ topPath + "conf/";
			 }
			 else if(n.name =='hb'){//得到从top目录的路径：例如hb/np_v1.93/conf/，相当于每个主机的位置的相对目录
				 flag = 'hb';
				 topPath =n.name +"/"+ topPath + "conf/";
			 } else if(n.name =='acct'){//得到从top目录的路径：例如hb/np_v1.93/conf/，相当于每个主机的位置的相对目录
				 flag = 'acct';
				 topPath =n.name +"/"+ topPath + "conf/";
			 }else{
				 topPath =n.name + "/"+topPath;
			 }
			 
		 }
		
	 });
	var path = node["path"];
	var content = JsVar["editor"].getValue();
	 showConfirmMessageAlter("确定保存吗？",function ok(){
		 getJsonDataByPost(Globals.baseActionUrl.TOPCONFIG_ACTION_VALIDATE_JSON_URL,{filePage:content,topFileName:node.name},"",
				function (result){
				  if(result && result.error){
                      showWarnMessageTips(result.error);
				    return
				  }
				  var obj = {path:path,filePage:content,topPath:topPath,topFileName:node.name,flag:flag};
				  
				  if(flag == "topology"){
					  	//保存文件时，选择需要同步的主机
					    showDialog("同步主机信息",600,300,Globals.baseJspUrl.CHOOSE_HOST_INFO_JSP_MANAGE_URL,
						        function destroy(data){
					    		//JsVar["taskGrid"].reload();
						    },obj);
				  }else{
					  getJsonDataByPost(Globals.baseActionUrl.TOPCONFIG_ACTION_SAVE_FILE_URL,obj,"配置管理-保存配置",function (){
							JsVar["isEditting"]=false;
                          showMessageTips("保存成功！");
							 
						})  
				  }
				  
			    }) ;
	 });
	

}
/**
 * 生成树节点之前进行自定树节点设置
 * @param e
 */
function onDrawNode(e){
	var node = e.node;
	//过滤空文件
}

function treeLoadEvent(e){
	var result=e.result;
	/*for(var i=0;i<result.length;i++){
		if(result[i]["name"]=='rate_proxy.xml' 
		|| result[i]["name"] =='netproxy_cfg.xml'){
			JsVar["fileTree"].removeNode(result[i]);
			
		}
	}*/
}
/**
 * 获取未被保存的页面内容
 * @returns
 */
function getUnsaveFilePage(){
	return JsVar["editor"].getValue();
}

/**
 * 鼠标右键触发事件
 * @param e
 */
function onBeforeOpen(e){
	var node = JsVar["fileTree"].getSelectedNode();
    if (!node) {//当没有选择节点， 则阻止浏览器的右键菜单
        e.htmlEvent.preventDefault();
        e.cancel = true;
        return;
    }
//    var rootNode=JsVar["fileTree"].getRootNode();
//    var rootPath=rootNode["children"][0]["path"];
    var regExp = new RegExp("^"+templatePath);
     
    $("#newFile").css("display", "none");
    $("#newDirectory").css("display", "none");
    $("#uploadFile").css("display", "none");
    $("#release").css("display", "none");
    $("#delete").css("display", "none");
    $("#rename").css("display", "none");
    $("#newTopology").css("display", "none");
    
    
    if(regExp.test(node["path"]))return;
    
    $("#delete").css("display", "block");
    $("#rename").css("display", "block");
	if (node.directory == true) {
		$("#newFile").css("display", "block");
		$("#newDirectory").css("display", "block");
		$("#release").css("display", "block");
		$("#newTopology").css("display", "block");
		$("#uploadFile").css("display", "block");
	} 
}

/**
 * 新建文件或目录
 * @param type
 */
function newFile(type){
	 var node = JsVar["fileTree"].getSelectedNode();
	 var params=mini.clone(node);
	 params["type"]=type;
	 
	 if (type == 'directory') {
		 showAddDialog("新增目录",400,150,Globals.baseJspUrl.DEVELOP_ACTION_PRESSURE_ADD_EDIT_FILE_JSP,
			        function  destroy(data){
			        if (data == systemVar.SUCCESS) {
			        	loadFilesTree();
			        	JsVar["fileTree"].selectNode(node["id"]);
			    		JsVar["fileTree"].expandNode(node["id"]);
                        showMessageTips("新增目录成功!");
			        }
			    },params);
	 }else if(type == 'file'){
		 showAddDialog("新增文件",400,150,Globals.baseJspUrl.DEVELOP_ACTION_PRESSURE_ADD_EDIT_FILE_JSP,
			        function  destroy(data){
			        if (data == systemVar.SUCCESS) {
			        	loadFilesTree();
			        	JsVar["fileTree"].selectNode(node["id"]);
			    		JsVar["fileTree"].expandNode(node["id"]);
                        showMessageTips("新增文件成功!");
			        }
			    },params);
	 }else if(type == 'topology'){
		 showAddDialog("新建topology",400,150,Globals.baseJspUrl.DEVELOP_ACTION_PRESSURE_ADD_EDIT_FILE_JSP,
			        function  destroy(data){
			        if (data == systemVar.SUCCESS) {
			        	loadFilesTree();
			        	JsVar["fileTree"].selectNode(node["id"]);
			    		JsVar["fileTree"].expandNode(node["id"]);
                        showMessageTips("新增topology成功!");
			        }
			    },params);
	 }
	
	
}

/**
 * 重命名文件或目录
 */
function renameFile(){
	var node = JsVar["fileTree"].getSelectedNode();
	var parentNode=JsVar["fileTree"].getParentNode(node);
	var tabs = mini.get("mainDevelopPage");
	var tabsData= JsVar["fileTree"]["openTabs"];
	var params=mini.clone(node);
	 
	 if (node.directory == true) {
		 params["type"]='directory';
		 var childNodes=JsVar["fileTree"].getAllChildNodes(node);
		 if(tabsData){
			 for(var i=0;i<tabsData.length;i++){
				 for(var j=0;j<childNodes.length;j++){
					 if(tabsData[i]["nodeId"]==childNodes[j]["id"]){
                         showWarnMessageTips("当前目录下的文件【"+tabsData[i]["title"]+"】已打开，不能进行重命名！");
						 return;
					 }
				 }
				 
			 } 
		 }
		 
		 showEditDialog("重命名目录",400,150,Globals.baseJspUrl.DEVELOP_ACTION_PRESSURE_ADD_EDIT_FILE_JSP,
			        function  destroy(data){
			        if (data == systemVar.SUCCESS) {
			        	loadFilesTree();
			        	JsVar["fileTree"].selectNode(parentNode["id"]);
			    		JsVar["fileTree"].expandNode(parentNode["id"]);
                        showMessageTips("重命名目录成功!");
			        }
			    },params);
	 }else if(node.directory == false){
		 params["type"]='file';
		 if(tabsData){
			 for(var i=0;i<tabsData.length;i++){
					if(params["id"] == tabsData[i]["nodeId"]){
                        showWarnMessageTips("【"+params["name"]+"】已打开，不能进行重命名！");
						return;
					}
				}
		 }
		 showEditDialog("重命名文件",400,150,Globals.baseJspUrl.DEVELOP_ACTION_PRESSURE_ADD_EDIT_FILE_JSP,
			        function  destroy(data){
			        if (data == systemVar.SUCCESS) {
			        	loadFilesTree();
			        	JsVar["fileTree"].selectNode(parentNode["id"]);
			    		JsVar["fileTree"].expandNode(parentNode["id"]);
                        showMessageTips("重命名文件成功!");
			        }
			    },params);
	 }
}

/**
 * 删除文件或目录
 */
function deleteFile(){
	var message="";
	var message_file="是否删除该文件？";
	var message_directory="是否删除该目录？（该目录下的所有文件及目录将会被删除）";
	var node = JsVar["fileTree"].getSelectedNode();
	var parentNode=JsVar["fileTree"].getParentNode(node);
	var params=mini.clone(node);
	var tabsData= JsVar["fileTree"]["openTabs"];
	if(node.directory == true){
		message=message_directory;
		 var childNodes=JsVar["fileTree"].getAllChildNodes(node);
		 if(tabsData){
			 for(var i=0;i<tabsData.length;i++){
				 for(var j=0;j<childNodes.length;j++){
					 if(tabsData[i]["nodeId"]==childNodes[j]["id"]){
                         showWarnMessageTips("当前目录下的文件【"+tabsData[i]["title"]+"】已打开，不能进行删除操作！");
						 return;
					 }
				 }
				 
			 }
		 }
	}else{
		message=message_file;
		if(tabsData){
			for(var i=0;i<tabsData.length;i++){
				if(params["id"] == tabsData[i]["nodeId"]){
                    showWarnMessageTips("【"+params["name"]+"】已打开，不能进行删除操作！");
					return;
				}
			}
		}
	}
	
	
	
	showConfirmMessageAlter(message ,function (){
		getJsonDataByPost(Globals.baseActionUrl.DEVELOP_ACTION_DELETE_DIRECTORY_OR_FILE_URL,{path:params["path"]},"二次开发-删除目录或文件",function (){
			loadFilesTree(); 
			JsVar["fileTree"].selectNode(parentNode["id"]);
    		JsVar["fileTree"].expandNode(parentNode["id"]);
            showMessageTips("删除成功！");
		})
	});
}

/**
 * 上传文件
 */
function uploadFile(){
	var node = JsVar["fileTree"].getSelectedNode();
	if(!node.directory){
        showMessageTips("请选择目录上传文件！");
		return ;
	}
	
	showDialog("上传文件",400,150,Globals.baseJspUrl.DEVELOP_ACTION_FILE_UPLOAD_JSP,
	        function  destroy(data){
	        if (data == systemVar.SUCCESS) {
	        	loadFilesTree(); 
	        	JsVar["fileTree"].selectNode(node["id"]);
	    		JsVar["fileTree"].expandNode(node["id"]);
                showMessageTips("上传文件成功!");
	        }
	    },node);
}

/**
 * 发布
 */
function releaseFile(){
	
	var node = JsVar["fileTree"].getSelectedNode();
	if(!validateReleaseFile(node)){
		showErrorMessageAlter("发布版本文件校验失败，请检查后再发布！");
		return;
	}
	showDialog("发布","400","150",Globals.baseJspUrl.DEVELOP_ACTION_DEVELOP_RELEASE_HANDLE_JSP,
			function  destroy(data){
			 if (data == systemVar.SUCCESS) {
                 showMessageTips("发布成功!");
			 }
	},node);
}