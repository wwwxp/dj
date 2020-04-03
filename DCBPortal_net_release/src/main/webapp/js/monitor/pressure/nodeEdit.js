//定义变量， 通常是页面控件和参数
var JsVar = new Object();
//初使化
$(document).ready(function () {
    mini.parse();
    JsVar["imageTree"]=mini.get("image_url");
    JsVar["nodeForm"] = new mini.Form("nodeForm");
    
    JsVar["fileType"]= new Object();
    JsVar["fileType"]["gif"]='gif';
    JsVar["fileType"]["png"]='png';
    JsVar["fileType"]["jpg"]='jpg';
    JsVar["fileType"]["jpeg"]='jpg';

    getImages();
});



/**
 * 新增和修改提交
 * @param e
 */
function onSubmit(e){
   edit();
}

/**
 * 跳转到该页面设值
 * @param action
 * @param data
 */
function onLoadComplete(data) {
  JsVar["nodeForm"].setData(data);
  
}


/**
 * 修改节点
 */
function edit()
{
    var editData = JsVar["nodeForm"].getData();
    JsVar["nodeForm"].validate();
    if (JsVar["nodeForm"].isValid() == false) {
        return;
    }
    
    editData= mini.clone(editData);
    var imageNode=JsVar["imageTree"].getSelectedNode();
    if(imageNode && imageNode["directory"]){
        showWarnMessageTips("请选择图片！");
    	return;
    }
//    editData["image_url"]=imageNode["path"];
    closeWindow(editData);
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

function getImages(){
	treeLoad(JsVar["imageTree"],"",null,Globals.baseActionUrl.TOPOLOGY_ACTION_GET_IMAGES_URL);
}
/**
 * 取消
 * @param e
 */
function onCancel(e){
    closeWindow();
}


