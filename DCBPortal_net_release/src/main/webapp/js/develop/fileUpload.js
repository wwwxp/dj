//定义变量， 通常是页面控件和参数
var JsVar = new Object();
//初使化
$(document).ready(function () {
    mini.parse();
//    JsVar["fileForm"] = new mini.Form("fileForm");
});



/**
 * 新增和修改提交
 * @param e
 */
function onSubmit(e){
	uploadFile();
}

/**
 * 跳转到该页面设值
 * @param action
 * @param data
 */
function onLoadComplete(data) {
	JsVar["selectNode"]=data;
}

/**
 * 新增文件或目录
 */
function uploadFile(){
	commonFileUpload(Globals.baseActionUrl.DEVELOP_ACTION_UPLOAD_URL,
			['fileName'],
			{path:JsVar["selectNode"]["path"]},
            function (result){
				if(result["state"] == 1){
                	closeWindow(systemVar.SUCCESS);
                }
			});
//	
//    $.ajaxFileUpload
//    (
//        {
//            url: Globals.baseActionUrl.DEVELOP_ACTION_UPLOAD_URL, //用于文件上传的服务器端请求地址
//            secureuri: false, //是否需要安全协议，一般设置为false
//            fileElementId: ['fileName'], //文件上传域的ID
//            data:{path:JsVar["selectNode"]["path"]},
//            dataType: 'json', //返回值类型 一般设置为json
//            success: function (data, status)  //服务器成功响应处理函数
//            {
//              if(data){
//                    if(data.error){
//                        showErrorMessageAlter(data.error);
//                        return;
//                    }
//	                if(data["state"] == 1){
//	                	closeWindow(systemVar.SUCCESS);
//	                }
//              }
//            },
//            error: function (data, status, e)//服务器响应失败处理函数
//            {
//                alert(e);
//            }
//        }
//    )
 }
    


/**
 * 取消
 * @param e
 */
function onCancel(e){
    closeWindow();
}


