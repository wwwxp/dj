//定义变量， 通常是页面控件和参数
var JsVar = new Object();
//初始化
$(document).ready(function () {
    mini.parse();
    //集群展示表格
    JsVar["busClusterGrid"] = mini.get("busClusterGrid");
    //取得查询表单
    JsVar["queryForm"] =  new mini.Form("#queryForm");
    
    //加载主机表格信息
    search();
});

//查询
function search() {
    var paramsObj = JsVar["queryForm"].getData();
    load(paramsObj);
}

//重新加载表格
function refresh() {
    JsVar["queryForm"].reset();
    load(null);
}

//加载表格
function load(param){
    datagridLoadPage(JsVar["busClusterGrid"], param, "busMainCluster.queryBusMainClusterList");
}

//渲染操作按钮
function onActionRenderer(e) {
	var index = e.rowIndex;
    var html = '<a class="Delete_Button" href="javascript:edit(' + index + ')">修改</a>';
     	html += '<a class="Delete_Button" href="javascript:del(' + index + ')">删除</a>';
    return html;
}

//渲染主机名称单元格成超链接
function onRenderClusterName(e){
	var index=e.rowIndex; 
	return '<a class="Delete_Button" href="javascript:clusterDetail('+index+')">'+e.record.CLUSTER_NAME+'</a>';
}

//详细信息
function clusterDetail(index){
	var row = JsVar["busClusterGrid"].getRow(index);
	showDialog("详细信息",600,400,Globals.baseJspUrl.BUS_MAIN_CLUSTER_JSP_DETAIL_URL,
	        function destroy(data){
	            
	    },row);
}
	
//新增集群小类
function add() {
    //var paramsObj = JsVar["busClusterGrid"].getData();
	// var params = {
     //    BUS_CLUSTER_ID: paramsObj["BUS_CLUSTER_ID"];
	// };
    showAddDialog("集群管理-新增业务主集群", "98%", "98%",Globals.baseJspUrl.BUS_MAIN_CLUSTER_JSP_ADD_EDIT_URL,
        function destroy(data){
            if (data == systemVar.SUCCESS) {
                JsVar["busClusterGrid"].reload();
                showMessageTips("新增集群成功");
            }
    }	);
}

/**
 * 修改集群
 * @param index
 */
function edit(index) {
	var row;
    //单个操作时，index值不为空也不为undefined；批量操作时，index不传，值为undefined
    if(index!=undefined){
        //单个操作
		row = JsVar["busClusterGrid"].getRow(index);
	}else{
        //批量操作
		var rows = JsVar["busClusterGrid"].getSelecteds();
	    if (rows.length == 1) {
		    row = rows[0];
	    } else {
            showWarnMessageTips("请选中一条记录!") ;
	        return;
	    }
	}
	showEditDialog("修改集群信息","98%", "98%",Globals.baseJspUrl.BUS_MAIN_CLUSTER_JSP_ADD_EDIT_URL,
	        function destroy(data){
	            if (data == systemVar.SUCCESS) {
	                JsVar["busClusterGrid"].reload();
	                showMessageTips("修改业务主集群成功!");
	            }
	    },row);
}

//删除主机 
function del(index) {
  //创建一个集合,用于存放被勾选的ID
	var ids = new Array();
    //单个操作时，index值不为空也不为undefined；批量操作时，index不传，值为undefined
    if(index!=undefined){
        //单个操作
		var row = JsVar["busClusterGrid"].getRow(index);
		ids.push({BUS_CLUSTER_ID:row["BUS_CLUSTER_ID"]});
	}else{
		//得到被勾选的行对象
		var rows = JsVar["busClusterGrid"].getSelecteds();
	    if (rows.length > 0) {
	        for (var i = 0; i < rows.length; i++) {
	        	//此处缺少一个判断,集群是否能够删除
				ids.push({BUS_CLUSTER_ID:rows[i]["BUS_CLUSTER_ID"]});
	        }
	    } else {
            showWarnMessageTips("请选中一条记录!") ;
	        return;
	    }
	}
    
    showConfirmMessageAlter("确定删除记录？",function ok(){
    	getJsonDataByPost(Globals.baseActionUrl.BUS_MAIN_CLUSTER_ACTION_DELETE_URL, ids, "集群管理-删除集群",
                function(result){
                    JsVar["busClusterGrid"].reload();
                    showMessageTips("删除集群成功!");
                });
    	 
    });
}

function onStatusRender(e) {
    var status = e.record.BUS_CLUSTER_STATE;
    if(status == "1" ){
        return "有效";
    } else {
        return "无效";
    }
}

/**
 * 渲染所属业务集群
 * @param e
 * @returns
 */
function onRenderBusCluster(e) {
    if(e && e.value){
    	var busCluster = e.value;
    	 var busClusterList = busCluster.split(",");
         var elements = new Array();
         for(var i=0;i<busClusterList.length;i++){
        	 elements.push("<span class='label label-success' >"+busClusterList[i]+"</span>  ");
         }
         if(elements.length != 0){
        	 return "<span style='word-wrap:break-word;word-break: break-all;white-space: normal;'>"+elements.join(" ")+"&nbsp;</span>";
         } else {
        	 return "<span class='label label-danger'>&nbsp;未分配&nbsp;</span>";
         }
    }
    return "<span class='label label-danger'>&nbsp;未分配&nbsp;</span>";
}
