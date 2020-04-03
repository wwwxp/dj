//定义变量， 通常是页面控件和参数
var JsVar = new Object();
//初使化
$(document).ready(function () {
    mini.parse();
    JsVar["fileForm"] = new mini.Form("fileForm");
});

/**
 * 跳转到该页面设值
 * @param action
 * @param data
 */
function onLoadComplete(action,data) {
    JsVar["data"]=data;
    //新建名字固定为“rebalance”的文件夹
    if(JsVar["data"]["newFolderName"]!=undefined){
    	var obj=mini.get("fileName");
    	obj.setValue(JsVar["data"]["newFolderName"]);
    	obj.setAllowInput(false);
    }
}

/**
 * 新增
 * @param e
 */
function addFile(){
    //是新建文件
    var addData = JsVar["fileForm"].getData();
    JsVar["fileForm"].validate();
    if (JsVar["fileForm"].isValid() == false){
        return;
    }
    if(JsVar["data"]["parentNodeName"]==addData["fileName"]){
        showWarnMessageTips("名称不能与父节点相同，请检查！");
    	return;
    }
    
    JsVar["data"]["newFileName"]=addData["fileName"];
    getJsonDataByPost(Globals.baseActionUrl.CONFIGURE_ACTION_CREATE_FILE_URL, JsVar["data"],"配置修改-新建文件夹",
        function(result){
    	if(result.successNum!=undefined && result.errorNum!=undefined){
			if(result.successNum>0 && result.errorNum>0){//有成功有失败  --警告
				showWarnMessageAlter(result.isSuccess,function ok(){
	    			closeWindow({flag:systemVar.SUCCESS,fileName:addData["fileName"]});
	    		});
			}else if(result.successNum>=0 && result.errorNum<=0){//只有有成功  --成功
				/*showMessageAlter(result.isSuccess,function ok(){
	    			closeWindow({flag:systemVar.SUCCESS,fileName:addData["fileName"]});
	    		});*/
				
				closeWindow({flag:systemVar.SUCCESS,fileName:addData["fileName"]});
				showMessageTips(result.isSuccess);
			}else if(result.successNum<=0 && result.errorNum>0){//只有失败  --错误
				showErrorMessageAlter(result.isSuccess,function ok(){
	    			closeWindow({flag:systemVar.SUCCESS,fileName:addData["fileName"]});
	    		});
			}
		}else{
			/*showMessageAlter(result.isSuccess,function ok(){
    			closeWindow({flag:systemVar.SUCCESS,fileName:addData["fileName"]});
    		});*/
			closeWindow({flag:systemVar.SUCCESS,fileName:addData["fileName"]});
			showMessageTips(result.isSuccess);
		}
	    	
        });
}




