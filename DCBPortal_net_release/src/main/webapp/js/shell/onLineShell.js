 //定义变量， 通常是页面控件和参数
var JsVar = new Object();
/**
 * 初始化
 */
$(document).ready(function () {
    mini.parse();
    JsVar["shellTree"] = mini.get("shellTree");//取得任务表格
    JsVar["shellData"]=[{id:0,parentId:-1,text:"主机列表"}];
    loadShellTree();
 
    
});



/**
 * 加载文件目录树
 */
function loadShellTree(){
	JsVar["shellTree"].loadList(JsVar["shellData"],"id","parentId");
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
 * 生成树节点之前进行自定树节点设置
 * @param e
 */
function onDrawNode(e){
	
}

/**
 * 点击目录树触发事件
 */
function onClickTreeNode(){
	var node = JsVar["shellTree"].getSelectedNode();
	if(node["id"] != 0){
		openShellTab(node);
	}
	
	
}

/**
 * 打开tab页面
 * @param node
 */
function openShellTab(node) {
    var tabs = mini.get("mainShellPage");
    var tabsData= tabs.getTabs();
   
   
    for(var i=0;i<tabsData.length;i++){
    	var tabData=tabsData[i];
    	if(tabData["id"] == node["id"]){
    		tabs.activeTab(tabData);
    		return;
    	}
    }

}

/**
 * 鼠标右键触发事件
 * @param e
 */
function onBeforeOpen(e){
	var node = JsVar["shellTree"].getSelectedNode();
    if (!node) {//当没有选择节点， 则阻止浏览器的右键菜单
        e.htmlEvent.preventDefault();
        e.cancel = true;
        return;
    }
}



/**
 * 新建主机连接
 * @param type
 */
function newHostLink(){
	 showDialog("新建主机连接",650,150,Globals.baseJspUrl.SHELL_ACTION_ADD_HOST_LINK_JSP,
		        function  destroy(data){
		        if(data){
		        	    var index=JsVar["shellData"].length;
		        	    var tabs = mini.get("mainShellPage");
		        	    
//		        	    var tabsData= tabs.getTabs();
//		        	    for(var i=0;i<tabsData.length;i++){
//		        	    	var tabData=tabsData[i];
//		        	    	if(tabData["id"] == node["id"]){
//		        	    		tabs.activeTab(tabData);
//		        	    		return;
//		        	    	}
//		        	    }
		        	    var url=Globals.baseJspUrl.SHELL_ACTION_OPEN_HOST_LINK_JSP+"?host="+data["host"]+"&port="+data["port"]+"&name="+data["name"]+"&password="+encrypt(data["password"]);
		        	    var tab = {id:index,title: data["host"],showCloseButton:true,url:url};
		        	    tab = tabs.addTab(tab);
		        	    tabs.activeTab(tab);
		        	   
		        	    JsVar["shellData"].push({id:index,parentId:0,text:data["host"]});
		        	    loadShellTree();
		        }
		    });
	
}





/**
 * tab页面关闭前触发事件 
 * @param e
 */
function beforecloseTab(e){
	var targetIFrame;
	var tab=e.tab;
	JsVar["closingTab"]=tab;
	
	JsVar["shellData"].splice(tab["id"],1);
	loadShellTree();
	
	getJsonDataByPost(Globals.baseActionUrl.SHELL_ACTION_REMOVE_HOST_LINK_URL, {host:tab["title"]},"在线shell-关闭主机连接",
            function(result){
                showMessageTips("关闭主机连接成功!");
            });
	
	
	
	//取消关闭（当前mini UI版本有bug，不支持）
	e.cancel = true;
	
}


/**
 * 切换tab页时,选中激活的树节点
 * @param e
 */
function activechangedTab(e){

}

