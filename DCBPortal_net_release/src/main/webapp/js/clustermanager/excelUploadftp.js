/**
 * 新增修改集群弹框
 */

//定义变量， 通常是页面控件和参数
var JsVar = new Object();
var fileupload;     //文件上传对象\
var file;//选择的文件对象，主要给change对象使用，
$(document).ready(function () {
    mini.parse();
    //获取页面表单
    JsVar["uploadForm"] = new mini.Form("#uploadForm");
    //  JsVar["FILE_NAME"] = mini.get("FILE_NAME");
    fileupload = $('#uFile');
    fileupload.change(function(){
        setFormEl(this);
    });
    $("input[name='uploadType']").click(function(){
        var type = $(this).val();
        var fileName;
        if(type =='1'){
            fileName =  $.trim(mini.get("remoteFile").getValue());
        }else{
            fileName = $.trim(fileupload.val());
        }
        if(fileName && fileName != ''){
            var obj = new Object();
            obj.value=  fileName;
            setFormEl(obj);
        }else{
            // JsVar["FILE_NAME"].setValue("");
        }
    });
});
/**
 * 给表单设值
 * @param obj
 */
function setFormEl(obj){
    //输出选中结果
    var fileFullName = obj.value;
    var fileName = fileFullName.substring((fileFullName.lastIndexOf("\\")+1),fileFullName.length);
    //JsVar["FILE_NAME"].setValue(fileName);
}

//父页面调用，新增和修改时会初使化一些参数，例如：data.action：操作类型
function onLoadComplete(action,data) {
    JsVar["FILE_TYPE"] = data["FILE_TYPE"];
}

//选择文件
function selectFile(){
    $("input[name='uploadType'][value='1']").attr("checked",true);
    var url = Globals.baseJspUrl.UPLOAD_JSP_CHOOSE_REMOTE_FILE_URL +"?page_type="+ JsVar["FILE_TYPE"];
    showDialog("选择文件",500,250,url,
        function success(action){
            if (action == systemVar.SUCCESS) {
                //JsVar["classGrid"].reload();
            }
        },JsVar["FILE_TYPE"]);
}

/**
 * 开始上传文件
 */
function fileUpload(){
    var uploadType = $("input[name='uploadType']:checked").val();
    var fileName = $.trim(fileupload.val());
    if(uploadType == '1'){
        fileName = mini.get("remoteFile").getValue();
    }
    if(fileName == ""){
        showWarnMessageTips("请选择文件");
        return ;
    }

    if (fileName.lastIndexOf(".")!=-1) {
        var fileType = (fileName.substring(fileName.lastIndexOf(".")+1,fileName.length)).toLowerCase();
        if(fileType != 'xlsx'){
            showWarnMessageTips("文件类型必须为xlsx文件,请重新选择");
            return ;
        }
    }else{
        showWarnMessageTips("文件格式有问题,请重新选择");
        return ;
    }

    // if(!validateZipV(fileName)){
    //     return ;
    // }


    JsVar["uploadForm"].validate();
    if (JsVar["uploadForm"].isValid() == false){
        return;
    }
    //修改操作下获取表单的数据
    var taskInfo = JsVar["uploadForm"].getData();
    showConfirmMessageAlter("确认导入文件?", function ok() {
        commonFileUpload(Globals.baseActionUrl.HOST_ACTION_EXCEL_URL, ['uFile'], taskInfo,
            function (result) {
                if (result != null && result["retCode"] == 0) {
                    showErrorMessageAlter("文件导入失败，请检查文件数据是否正确!");
                } else {
                    if (result != null && result["ERROR_RESULT"].length > 0) {
                        var alterMsg = "<textarea style='height:240px;width:380px;text-align:center;'>批量数据导入完成！" + "成功：" + result["SUCESS_RESULT"].length + "条，" + "失败：" + result["ERROR_RESULT"].length + "条！";
                        var errorMsg = "\n导入失败主机信息如下（主机已存在或者数据格式校验不通过）：";
                        for (var i = 0; i < result["ERROR_RESULT"].length; i++) {
                            var hostIP = result["ERROR_RESULT"][i]["HOST_IP"];
                            var hostName = result["ERROR_RESULT"][i]["HOST_NAME"];
                            var hostFail = result["ERROR_RESULT"][i]["FAIL_MSG"];
                            errorMsg += "\n【主机IP: " + hostIP + "， 主机名称: " + hostName + "， 失败原因: " + hostFail + "】";
                        }
                        alterMsg += errorMsg;
                        alterMsg += "</textarea>";
                        showWarnMessageAlter(alterMsg);
                    } else {
                        var alterMsg = "批量数据导入完成！" + "成功：" + result["SUCESS_RESULT"].length + "条，" + "失败：" + result["ERROR_RESULT"].length + "条！";
                        showMessageAlter(alterMsg);
                    }
                    closeWindow(systemVar.SUCCESS);
                }

            }, false, "正在上传文件,请稍等...", 'excel文件导入');
    });

}
