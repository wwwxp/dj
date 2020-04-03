//定义变量， 通常是页面控件和参数
var JsVar = new Object();
//初始化
$(document).ready(function () {
    mini.parse();
    JsVar["userGrid"] = mini.get("user_datagrid");//取得查询表格
    JsVar["queryFrom"] =  new mini.Form("#queryFrom");//取得查询表单
    //加载用户表格信息
    search();
});

//查询
function search() {
    var paramsObj = JsVar["queryFrom"].getData();
    load(paramsObj);
}
//重新加载表格
function refresh() {
    JsVar["queryFrom"].reset();
    load(null);
}
//加载表格
function load(param){
    datagridLoadPage(JsVar["userGrid"],param,null,Globals.baseActionUrl.USER_ROLE_CONFIG_ACTION_QUERY);
}

//渲染操作按钮
function onRenderer(e) {
    var empee_id = e.record.EMPEE_ID;
    return '<a class="Delete_Button" href="javascript:userRoleEdit(\'' + empee_id + '\')">分配角色</a>';
}

/**
 * 渲染所属角色
 * @param e
 * @returns
 */
function onRenderRole(e) {
    if(e && e.value){
    	var busRole = e.value;
    	 var busRoleList = busRole.split(",");
         var elements = new Array();
         for(var i=0;i<busRoleList.length;i++){
        	 elements.push("<span class='label label-success' >"+busRoleList[i]+"</span>  ");
         }
         if(elements.length != 0){
        	 return "<span style='word-wrap:break-word;word-break: break-all;white-space: normal;'>"+elements.join(" ")+"&nbsp;</span>";
         } else {
        	 return "<span class='label label-danger'>&nbsp;未分配&nbsp;</span>";
         }
    }
    return "<span class='label label-danger'>&nbsp;未分配&nbsp;</span>";
}


//分配角色
function userRoleEdit(empee_id) {
    showDialog("分配角色",480,500,Globals.baseJspUrl.USER_ROLE_JSP_DISPATCH_ROLE_URL,
        function destroy(data){
            if (data == systemVar.SUCCESS) {
                JsVar["userGrid"].reload();
                showMessageTips("分配角色成功!");
            }
    },empee_id);
}




