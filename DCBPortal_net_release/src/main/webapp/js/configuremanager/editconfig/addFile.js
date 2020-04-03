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
    
    if (data["children"] != null && data["children"].length > 0) {
    	var childrens = [];
    	for (var i=0; i<data["children"].length; i++) {
    		if (data["children"][i]["fileType"] == "F") {
    			childrens.push({
        			fileName:data["children"][i]["fileName"],
        			filePath:data["children"][i]["filePath"]
        		});
    		}
    	}
    	mini.get("copyFilesNames").setData(childrens);
    }
    
    if (data["from"] == busVar.SCRIPT) {
    	$("#copyTr").remove();
    }
    
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
    JsVar["data"]["newFileName"] = addData["fileName"];
    if(JsVar["data"]["from"] == busVar.SCRIPT) {
    	JsVar["data"]["newFileName"] = "bin/" + addData["fileName"];
    }
    JsVar["data"]["copyFilesNames"] = mini.get("copyFilesNames").getValue();
    getJsonDataByPost(Globals.baseActionUrl.CONFIGURE_ACTION_CREATE_FILE_URL, JsVar["data"],"配置修改-新建文件",
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
				showMessageTips(result.isSuccess);
				closeWindow({flag:systemVar.SUCCESS,fileName:addData["fileName"]});
			}
        });
}




