//定义变量， 通常是页面控件和参数
var JsVar = new Object();
//初使化
$(document).ready(function () {
    mini.parse();
    JsVar["imageTree"] = mini.get("image_url");
    JsVar["fromNode"] = mini.get("fromNode");
    JsVar["toNode"] = mini.get("toNode");
    JsVar["nodeForm"] = new mini.Form("nodeForm");


    getImages();
});



/**
 * 新增和修改提交
 * @param e
 */
function onSubmit(e){
   save();
}

/**
 * 跳转到该页面设值
 * @param action
 * @param data
 */
function onLoadComplete(data) {
	JsVar["allnodes"]=data["allnodes"];
//	JsVar["xy_location"]=data["xy_location"];
	JsVar["fromNode"].setData(data["allnodes"]);
	JsVar["toNode"].setData(data["allnodes"]);
	JsVar["nodeForm"].setData(data["xy_location"]);
  
}


/**
 * 保存节点
 */
function save()
{
    var addData = JsVar["nodeForm"].getData();
    
    for(var i=0;i<JsVar["allnodes"].length;i++){
		if(addData["id"] ==JsVar["allnodes"][i]["_id"] ){
			showErrorMessageAlter("节点id已存在，请输入其它值！");
			return;
		}
	}
    JsVar["nodeForm"].validate();
    if (JsVar["nodeForm"].isValid() == false) {
        return;
    }
    
    addData= mini.clone(addData);
    var imageNode=JsVar["imageTree"].getSelectedNode();
    if(imageNode && imageNode["directory"]){
        showWarnMessageTips("请选择图片！");
    	return;
    }
//    editData["image_url"]=imageNode["path"];
    closeWindow(addData);
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


