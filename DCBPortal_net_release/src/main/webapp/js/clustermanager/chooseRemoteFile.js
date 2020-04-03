/**
 * 新增修改集群弹框
 */

//定义变量， 通常是页面控件和参数
var JsVar = new Object();
 
$(document).ready(function () {
    mini.parse();
    
    JsVar["rbl"] = mini.get("rbl");
    JsVar["rbl"].on("valuechanged", function (e) {
        JsVar["value"] = this.getValue();
    });
    
});
function onLoadComplete(data) {
	JsVar["page_type"] = data;
	 
	var url = Globals.ctx + "/uploadFTP/queryRemoteFile?page_type="+JsVar["page_type"];
	 getJsonDataByPost(url,null,null,
	            function success(result){
		 			JsVar["rbl"].setData(result);
	            });
}

function onSubmit(){
	//var filePath = mini.get('rbl').getValue;
	//var fileName = mini.get('rbl').getText();
	if(!isEmptyObject(JsVar["value"])){
		JsVar["rbl"]= null;
		window.Owner.setRemoteFile(JsVar);
		closeWindow();
	}
	
}
 
