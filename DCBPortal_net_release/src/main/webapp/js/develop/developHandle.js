 //定义变量， 通常是页面控件和参数
var JsVar = new Object();
/**
 * 初始化
 */
$(document).ready(function () {
    mini.parse();
    JsVar["fileTree"] = mini.get("fileTree");//取得任务表格
    //初始文件类型图标
    JsVar["fileType"]= new Object();
    JsVar["fileType"]["css"]='css';
    JsVar["fileType"]["html"]='html';
    JsVar["fileType"]["htm"]='html';
    JsVar["fileType"]["jsp"]='html';
    JsVar["fileType"]["js"]='js';
    JsVar["fileType"]["txt"]='txt';
    JsVar["fileType"]["xml"]='xml';
    JsVar["fileType"]["zip"]='zip';
    JsVar["fileType"]["java"]='java';
    JsVar["fileType"]["jar"]='java';
    JsVar["fileType"]["sql"]='sql';
    JsVar["fileType"]["gif"]='gif';
    JsVar["fileType"]["png"]='png';
    JsVar["fileType"]["jpg"]='jpg';
    JsVar["fileType"]["jpeg"]='jpg';
    JsVar["fileType"]["ico"]='ico';
    JsVar["fileType"]["db"]='db';

    loadFilesTree();
    
});



/**
 * 加载文件目录树
 */
function loadFilesTree(){
	treeLoad(JsVar["fileTree"],"",null,Globals.baseActionUrl.DEVELOP_ACTION_LIST_FILES_TREE_URL);
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

/**
 * 操作帮助信息
 */
function help(){
	showDialog("帮助信息","800","550",Globals.baseJspUrl.DEVELOP_ACTION_DEVELOP_HELP_INFOMATION_JSP,function(){
		 
	 },{});
		
}

/**
 * 生成树节点之前进行自定树节点设置
 * @param e
 */
function onDrawNode(e){
	var node = e.node;
	//修改默认的父子节点图标
    if (node.directory == true) {
        e.iconCls = "mini-tree-folder";
    } else {
    	var name = node["name"];
    	var index=node["name"].lastIndexOf(".");
    	var typeName=name.substring(index+1,name.length);
    	typeName=typeName.toLowerCase();
    	if(typeName != ""){
    		if(JsVar["fileType"][typeName] != undefined ){
    			e.iconCls = "mini-tree-leaf-"+JsVar["fileType"][typeName];
    		}else{
    			e.iconCls = "mini-tree-leaf";
    		}
    	}else{
    		e.iconCls = "mini-tree-leaf";
    	}
    	
    }
}

/**
 * 点击目录树触发事件
 */
function onClickTreeNode(){
	var node = JsVar["fileTree"].getSelectedNode();
	if(node.directory == false){
		 var index = node["name"].lastIndexOf(".");
		    var suffix=node["name"].substring(index+1).toLowerCase();
		    
		    if(   suffix == "txt" 
		    	||suffix == "js" 
		    	||suffix == "sh"
		    	||suffix == "html"
	    		||suffix == "css" 
    			|| suffix == "conf" 
				|| suffix == "topo" 
				|| suffix == "java" 
				|| suffix == "jsp"
				|| suffix == "xml"
				|| suffix == "properties"
					){
		    	openDevelopTab(node);
		    }
		
	}
	
}

/**
 * 打开tab页面
 * @param node
 */
function openDevelopTab(node) {
    var tabs = mini.get("mainDevelopPage");
    var tabsData= tabs.getTabs();
   
   
    for(var i=0;i<tabsData.length;i++){
    	var tabData=tabsData[i];
//    	if(tabData["title"] == node["name"] && tabData["nodeId"] != node["id"]){
//    		tabs.activeTab(tabData);
//    		showWarnMessageAlter("已有同名文件被打开，请关闭同名文件后再打开该文件！");
//    		return;
//    	}else
    		
    	if(tabData["nodeId"] == node["id"]){
    		tabs.activeTab(tabData);
    		return;
    	}
    }
    
    if(tabsData.length>9){
        showWarnMessageTips("最多可以打开10个窗口！");
    	return;
    }
   
    var url=Globals.baseJspUrl.DEVELOP_ACTION_DEVELOP_PAGE_JSP+"?path="+encodeURI(node["path"]);
   
    //add tab
    var tab = {nodeId:node["id"],title: node["name"],showCloseButton:true,url:url};
    tab = tabs.addTab(tab);
    var tabsData= tabs.getTabs();
    JsVar["fileTree"]["openTabs"]=tabsData;
    //tab body
//    var el = tabs.getTabBodyEl(tab);
//    el.innerHTML = node["path"];
    //active tab
    
    tabs.activeTab(tab);
   

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

/**
 * 发布版本文件校验
 * @param node
 * @returns {Boolean}
 */
function validateReleaseFile(node){
	var directory = {};
	var num=0;
	var nodesArray=JsVar["fileTree"].getChildNodes(node);
	
	for(var i=0;i<nodesArray.length;i++){
		var nodeName=nodesArray[i]["name"];
		if(nodeName == 'bin' ){
			num++;
			directory["bin"]=nodesArray[i];
		}else if(nodeName == 'conf'){
			num++;
			directory["conf"]=nodesArray[i];
		}else if(nodeName == 'external'){
			num++;
			directory["external"]=nodesArray[i];
		}else if(nodeName == 'extend'){
			num++;
			directory["extend"]=nodesArray[i];
		}else if(nodeName == 'lib'){
			num++;
			directory["lib"]=nodesArray[i];
		}else if(nodeName == 'view'){
			num++;
			directory["view"]=nodesArray[i];
		}
	}
	
	if(num != 6 ) return false;
	
	
	var binValidate=false;
	var binArray=JsVar["fileTree"].getChildNodes(directory["bin"]);
	for(var i=0;i<binArray.length;i++){
		 var binNodeName=binArray[i]["name"];
		  if(binNodeName == 'storm.js'){
			  binValidate=true;
			  break;
		  }
	}
	if(!binValidate) return false;
	
	
	var confValidate=false;
	var confArray=JsVar["fileTree"].getChildNodes(directory["conf"]);
	for(var i=0;i<confArray.length;i++){
		 var confNodeName=confArray[i]["name"];
//		  var regExp=/.conf$/;
//		  if(regExp.exec(confNodeName)){
//			  confValidate=true;
//			  break;
//		  }
		 if((node["name"]+".conf") == confNodeName){
			 confValidate=true;
			 break;
		 }
	}
	
	if(!confValidate) return false;
	var libValidate=false;
	var libArray=JsVar["fileTree"].getChildNodes(directory["lib"]);
	for(var i=0;i<libArray.length;i++){
		 var libNodeName=libArray[i]["name"];
		
		  if(libNodeName ==  'DCBP.jar'){
			  libValidate=true;
			  break;
		  }
	}
	if(!libValidate) return false;
	
	return true;

}
/**
 * tab页面关闭前触发事件 
 * @param e
 */
function beforecloseTab(e){
	var targetIFrame;
	var iframes=document.getElementsByTagName("iframe");
	var tab=e.tab;
	JsVar["closingTab"]=tab;
	var index=0;
	for(var i=0;i<iframes.length;i++){
		if(iframes[i]["src"].indexOf(tab["url"])>0){
			targetIFrame=iframes[i];
			index=i;
			break;
		}
	}
	
	//tab页面关闭时，激活当前tab页的后一个tab页，如当前为最后一个节点，则激活前一个tab页，同时在文件树中选中激活的tab页树节点
	var tmpTab;
	var tabs=e.sender.tabs;
	if(index==iframes.length-1 ){
		 tmpTab=tabs[index-1];
	}else{
		 tmpTab=tabs[index+1];
	}
	
	if(tmpTab != undefined ){
		var mainDevelopPage = mini.get("mainDevelopPage");
		mainDevelopPage.activeTab(tmpTab);
		
		JsVar["fileTree"].selectNode(tmpTab["nodeId"]);
		JsVar["fileTree"].expandPath(tmpTab["nodeId"]);
	}
	
	
	
	//判断是否正在编辑中
    var isEditting=targetIFrame.contentWindow.isEditing();
    if(isEditting){
    	JsVar["unsaveFilePage"] =targetIFrame.contentWindow.getUnsaveFilePage();
    	JsVar["unsaveFilePath"] =targetIFrame.contentWindow.path;
		showConfirmMessageAlter("<span style='text-align:center;color:red;'>当前文档未保存！</span><br/>【确认】保存并关闭当前文档，【取消】不保存并关闭当前文档" ,
				saveFilePage);
	       
	}
    
	//取消关闭（当前mini UI版本有bug，不支持）
	e.cancel = true;
	
}


/**
 * 切换tab页时,选中激活的树节点
 * @param e
 */
function activechangedTab(e){
//	alert("activechangedTab");
	var tab=e.tab;
	if(tab == undefined ) return;
	JsVar["fileTree"].selectNode(tab["nodeId"]);
	JsVar["fileTree"].expandPath(tab["nodeId"]);
}

/**
 * 保存页面编辑数据
 */
function saveFilePage(){
	getJsonDataByPost(Globals.baseActionUrl.DEVELOP_ACTION_SAVE_DEVELOP_FILE_URL,{path:JsVar["unsaveFilePath"],filePage:JsVar["unsaveFilePage"]},"二次开发-保存代码",function (){
        showMessageTips("保存成功！");
	})
}