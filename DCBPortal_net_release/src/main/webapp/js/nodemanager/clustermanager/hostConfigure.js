var nowurl = document.location.href;
nowurl = nowurl.substring(0, nowurl.indexOf("html"));
var JsVar = new Object();
$(document).ready(function () {
    mini.parse();
    JsVar["hostGrid"] = mini.get("hostGrid");
    JsVar["hostIP"] = mini.get("hostIP");
    JsVar["hostName"] = mini.get("hostName");
    //initTableHead("hostGrid","DSF_HOST_MANAGE_Q",true);
    hostConfigureSearch();
});

/**
 * 王启帆
 * 列表查询（与模糊查询，共一条SQL语句）
 * 模糊查询
 */
function hostConfigureSearch(){
    var params = {
        "service_name": "DSF_HOST_CONFIGURE_PAGE_Q",
        "HOST_IP": JsVar["hostIP"].getValue(),
        "HOST_NAME": JsVar["hostName"].getValue()
    }
    var url = nowurl + 'ControlServlet.do?serviceName=HostConfigureServlet&methodName=hostConfigureQuery';
    datagridLoadPage(JsVar["hostGrid"], params, null, url);
}

/**
 * 王启帆
 * 添加主机配置
 * 添加一行记录，打开添加弹窗页面
 */
function addHostAndLatn() {
    showAddDialog("主机配置 ","45%","65%",Globals.ctx + "/jsp/nodemanager/clustermanager/clusteraddOne",
        function callback(data){
            if (data != undefined && data.success == systemVar.SUCCESS) {
                showMessageTips("主机新增成功！");
                hostConfigureSearch();
            }else if(data == systemVar.CANCEL || data == systemVar.CLOSE){

            }else{
                showErrorMessageTips(data.errormsg);
                return false;
            }
        });
}

/**
 * 王启帆
 * 修改主机配置
 * 打开修改弹窗页面
 */
function updateHost() {
    var rows = JsVar["hostGrid"].getSelecteds();
    if(rows.length>1){
        showWarnMessageTips("一次只能修改一条数据！请勿选择多条！");
        return;
    }
    if(1!=rows.length){
        showWarnMessageTips("请先选择一条数据进行修改");
        return;
    }
    var param ={
        HOST_ID:rows[0].HOST_ID,
        HOST_IP:rows[0].HOST_IP,
        HOST_NAME:rows[0].HOST_NAME,
        ENV:rows[0].ENV,
        LATN_ID:rows[0].LATN_ID
    };
    showEditDialog("修改主机 ","45%","65%",nowurl + "html/hostConfigure/hostConfigureAU.html",
        function callback(data){
            if (data != undefined && data.success == systemVar.SUCCESS) {
                showMessageTips("修改成功！");
                hostConfigureSearch();
            }else if(data == systemVar.CANCEL || data == systemVar.CLOSE){

            }else{
                showErrorMessageTips(data.errormsg);
                return false;
            }
        },param);
}

/**
 * 王启帆
 * 删除主机和及其以下所有的本地网配置
 */
function removeHostAndLatn() {
    var rows = JsVar["hostGrid"].getSelecteds();
    var ids="";
    if (rows.length > 0) {
        for(var i =0;i<rows.length;i++){
            ids = ids + rows[i]["HOST_ID"] + ",";
        }
        var IDS=ids.substring(0,ids.length-1);
        showConfirmMessageAlter("确认删除主机和及其以下的所有本地网信息?", function ok(){
            var url = nowurl + 'ControlServlet.do?serviceName=HostConfigureServlet&methodName=removeHostAndLatn';
            var removeParam = {
                HOST_IDS : IDS
            }
            getJsonDataByPost(url, removeParam, "删除主机和及其以下的所有本地网信息", function(data){
                if ("1"==data.recode) {
                    showMessageTips("删除成功!");
                }else{
                    showErrorMessageTips("删除失败!");
                }
                //刷新页面
                hostConfigureSearch();
            });
        });
    }else{
        showWarnMessageTips("至少请选择一条记录!");
    }
}