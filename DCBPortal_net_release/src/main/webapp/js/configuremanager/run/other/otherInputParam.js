/**
 * 定义变量， 通常是页面控件和参数
 */
var JsVar = new Object();

/**
 * 初始化
 */
$(document).ready(function () {
	 JsVar["paramFrom"] = new mini.Form("#paramFrom");
    
});
//父页面调用，新增和修改时会初使化一些参数，例如：data.action：操作类型
function onLoadComplete(data) {
	JsVar["obj"] = data["obj"];
}
function onSubmit(){
	 //判断是否有效
    JsVar["paramFrom"].validate();
    if (JsVar["paramFrom"].isValid() == false){
        return;
    }
    var paramInfo = JsVar["paramFrom"].getData();
    var INPUT_PARAM = paramInfo["INPUT_PARAM"];
    paramInfo["INPUT_PARAM"] = INPUT_PARAM;
    paramInfo["IS_PARAM"] = JsVar["IS_PARAM"];
    closeWindow({flag:"success",params:paramInfo});
    
    //paramInfo["list"] = JsVar["obj"];
	//for(var i = 0 ; i <JsVar["obj"].length ;i++ ){
	//	JsVar["obj"][i]["INPUT_PARAM"]= INPUT_PARAM;
	//}
   // parent.postAjaxParam(paramInfo);
  //  postAjaxParam(paramInfo);
	
}

/**
 * 提交
 */
function postAjaxParam(obj){
	showConfirmMessageAlter("确定启动运行该程序吗？",function ok(){
		getJsonDataByPost(Globals.baseActionUrl.OTHER_RUN_ACTION_MANAGE_URL,obj,"启停管理-运行other周边程序",
	        function(result){
				if(result["flag"]=='error'){
					  
				    var opt={"allowResize":"Boolean","allowDrag":"Boolean","showMaxButton":"Boolean"};
					showDialog("程序执行结果",500,300,Globals.baseJspUrl.SERVICE_PROGRAM_JSP_RESULT_URL,
				        function destroy(data){
			    			//JsVar["programGrid"].reload();
				    },result,opt);
				}else{
					 
					closeWindow(systemVar.SUCCESS);
				 
					//JsVar["programGrid"].reload();
					//showMessageAlter("运行成功!");
				}
	        });
	});
}