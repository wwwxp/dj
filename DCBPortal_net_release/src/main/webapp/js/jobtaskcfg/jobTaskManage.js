//定义变量， 通常是页面控件和参数
var JsVar = new Object();
//初始化
$(document).ready(function () {
    mini.parse();
    JsVar["datagrid"] = mini.get("datagrid");//取得表格
    JsVar["queryFrom"] = new mini.Form("#queryFrom");//取得查询表单
    //加载表格信息
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
function load(param) {
    datagridLoadPage(JsVar["datagrid"], param, "joblist.queryJobList", Globals.baseActionUrl.JOBTASKAPI_SELECT_JOBLIST_AND_CHECK_JOB_HEALTH);
}

function dateToString(date) {
    return mini.formatDate(date, 'yyyy-MM-dd HH:mm:ss');
}

//详情展示
function onShowRowDetail(e) {
    var row = e.record;
    var grid = e.sender;
    var td = grid.getRowDetailCellEl(row);
    var $detailForm = $("#detailForm")[0];
    $detailForm.style.display = '';
    td.appendChild($detailForm);
    // console.log(row);
    $("#TASK_JOB_CLASS_DETAIL").html(row["TASK_JOB_CLASS"]);
    /*$("#TASK_JOB_PARAMS_DETAIL").html(row["TASK_JOB_PARAMS"]);
    $("#TASK_TYPE_DESC_DETAIL").html(row["TASK_TYPE_DESC"]);
    $("#CRON_EXP_DETAIL").html(row["CRON_EXP"]);
    $("#CRON_DESC_DETAIL").html(row["CRON_DESC"]);
    $("#TASK_EXEC_LAST_TIME_DETAIL").html(dateToString(row["TASK_EXEC_LAST_TIME"]));*/
    $("#CRON_START_TIME_DETAIL").html(dateToString((row["CRON_START_TIME"])));
    $("#CRON_END_TIME_DETAIL").html(dateToString(row["CRON_END_TIME"]));
    /*$("#EXEC_STATUS_DESC_DETAIL").html(row["EXEC_STATUS_DESC"]);
    $("#TASK_STATUS_DESC_DETAIL").html(row["TASK_STATUS_DESC"]);*/
    $("#CRT_DATE_DETAIL").html(dateToString(row["CRT_DATE"]));
    $("#TASK_DESC_DETAIL").html('');
    $("#TASK_DESC_DETAIL").html(row["TASK_DESC"]);
}

//渲染操作按钮
function onActionRenderer(e) {
    var index = e.rowIndex;
    var TASK_STATUS = e.record.TASK_STATUS;
    var EXEC_STATUS = e.record.EXEC_STATUS;
    var TASK_TYPE = e.record.TASK_TYPE;
    var html ="";
    //CASE EXEC_STATUS WHEN '1' THEN '执行中' WHEN '0' THEN '初始'  ELSE '执行完成'
    html += '<a class="Delete_Button" href="javascript:deleteJob(' + index + ')">删除</a>';
    html += '<a class="Delete_Button" href="javascript:edit(' + index + ')">修改</a>';
    //一次性任务
    if (TASK_TYPE == '0') {
        html += '<a class="Delete_Button" href="javascript:execJob(' + index + ')">立即执行</a>';
    } else if (TASK_TYPE == '1' || TASK_TYPE == '2') {
        //自定义任务、循环任务
        if (EXEC_STATUS == '1') {
            //执行状态为执行中，可停止
            html += '<a class="Delete_Button" href="javascript:execStop(' + index + ')">停止</a>';
        } else if (typeof(EXEC_STATUS) == 'undefined' || EXEC_STATUS == '0' || EXEC_STATUS == '2'|| EXEC_STATUS == '3') {
            //执行状态为空、初始以及运行完成时，可选择执行
            html += '<a class="Delete_Button" href="javascript:timerJob(' + index + ')">执行</a>';
        }
    }
    html += '<a class="Delete_Button"  href="javascript:queryLog(' + index + ')">日志</a>';

    return html;
}

function deleteJob(index) {
    var single = [JsVar["datagrid"].getRow(index)];
    showConfirmMessageAlter("确定删除记录？",function ok(){
        getJsonDataByPost(Globals.ctx+"/jobApi/delJobRecord",single,"任务配置-删除任务",
            function(result){
                JsVar["datagrid"].reload();
                showMessageTips("删除任务成功!")
            },"jobTaskBus.deleteTaskRecords","");
    });
}

/**
 * 立即执行
 */
function execJob(index){
    var row  = JsVar["datagrid"].getRow(index);
    var param = {"ID":row["ID"],"TASK_NAME":row["TASK_NAME"],"TASK_TYPE_DESC":row["TASK_TYPE_DESC"]};
    showConfirmMessageAlter("确定立即执行该任务吗？",function ok(){
        getJsonDataByPost(Globals.baseActionUrl.JOBTASKAPI_ACTION_EXEC_URL,param,"任务管理-立即执行",
            function success(result){
                //search();
                showMessageTips("一次性任务执行成功!");
            });
    } );

}

function timerJob(index){
    var row  = JsVar["datagrid"].getRow(index);
    var TASK_TYPE = row["TASK_TYPE"];
    var url =Globals.baseActionUrl.JOBTASKAPI_ACTION_TIMER_URL;
    if(TASK_TYPE == '2' ){
        url = Globals.baseActionUrl.JOBTASKAPI_ACTION_LOOP_URL;
    }
    var param = {"ID":row["ID"],"TASK_NAME":row["TASK_NAME"],"TASK_TYPE_DESC":row["TASK_TYPE_DESC"]};
    showConfirmMessageAlter("确定执行该任务吗？",function ok(){
        getJsonDataByPost(url,param,"任务管理-定时执行",
            function success(result){
                search();
                showMessageTips("周期任务执行成功!");
            });
    });

}

/**
 * 停止
 */
function execStop(index){
  var  row = JsVar["datagrid"].getRow(index);
  var param = {"ID":row["ID"],"TASK_NAME":row["TASK_NAME"],"TASK_TYPE_DESC":row["TASK_TYPE_DESC"]};
    showConfirmMessageAlter("确定停止该任务吗？",function ok(){
        getJsonDataByPost(Globals.baseActionUrl.JOBTASKAPI_ACTION_STOP_URL,param,"任务管理-停止",
            function success(result){
                search();
                showMessageTips("任务停止成功!");
            });
    } );
}


/**
 * 新增定时任务
 */
function add() {
    showAddDialog("任务配置--新增", 600, 500,
        Globals.baseJspUrl.JOBLIST_JSP_ADD_EDIT_URL,
        function destroy(data) {
            if (data == systemVar.SUCCESS) {
                search();
                //showMessageAlter("新增成功");
                showMessageTips("新增成功");
            }
        });
}

//根据 ID 删除任务
function del() {
    var rows = JsVar["datagrid"].getSelecteds();
    if (rows.length > 0) {
        showConfirmMessageAlter("确定删除记录？",function ok(){
            getJsonDataByPost(Globals.ctx+"/jobApi/delJobRecord",rows,"任务配置-删除任务",
                function(result){
                    JsVar["datagrid"].reload();
                    showMessageTips("删除任务成功!")
                },"jobTaskBus.deleteTaskRecords","");
        });
    } else {
        showWarnMessageTips("请选中一条记录!") ;
    }
}

//修改用户
function edit(index) {
    var row;
    //单个操作时，index值不为空也不为undefined；批量操作时，index不传，值为undefined
    if (index != undefined) {
        //单个操作
        row = JsVar["datagrid"].getRow(index);
    } else {
        //批量操作
        var rows = JsVar["datagrid"].getSelecteds();
        if (rows.length == 1) {
            row = rows[0];
        }
        else {
            showWarnMessageTips("请选中一条记录!");
            return;
        }
    }

    showEditDialog("任务配置--修改", 600, 500,
        Globals.baseJspUrl.JOBLIST_JSP_ADD_EDIT_URL,
        function destroy(data) {
            if (data == systemVar.SUCCESS) {
                search();
                //showMessageAlter("新增成功");
                showMessageTips("修改成功");
            }
        },row);
}

/**
 * 查看日志
 * @param index
 */
function queryLog(index) {
    var row = JsVar["datagrid"].getRow(index);

    var selNode={};
    selNode["JOB_ID"] = row["ID"];
    selNode["TASK_NAME"] = row["TASK_NAME"];
    showDialog("日志查看","99%",550,Globals.baseJspUrl.JOBLIST_JSP_QUERY_LOG_URL,
        function destroy(data){
            search();
        },selNode);
}