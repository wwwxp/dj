/**
 * Created with IntelliJ IDEA.
 * Creater: zhongsixue
 * Date: 17-2-7
 * Time: 下午16:17
 * To change this template use File | Settings | File Templates.
 */

/**
 * 定义变量， 通常是页面控件和参数
 */
var JsVar=new Object();
var param=new Object();

//部署进度说明需要的全局变量
var index=0;
var sumbitButton;

//获取Redis已使用的文件列表
var lastFileData = [];

//每台主机配置文件保存数组
var deployTypeFileArray = [];

/**
 * 跳转到该页面设值
 * @param data
 */
function onLoadComplete(data) {
    mini.parse();
    //按钮对象
    sumbitButton = mini.get("sumbitButton");
    //取得主机表格
    JsVar["configGrid"] = mini.get("configGrid");
    //集群编码
    param["CLUSTER_CODE"] = data["CLUSTER_CODE"];
    //集群ID
    param["CLUSTER_ID"] = data["CLUSTER_ID"];
    //集群名称
    param["CLUSTER_NAME"] = data["CLUSTER_NAME"];
    //集群类型
    param["CLUSTER_TYPE"] = data["CLUSTER_TYPE"];
    //鼠标右键单台主机启停参数
    param["HOST_ID"] = data["HOST_ID"];
    //默认启动版本列表加载
    initStartVersion();
    //加载右键
    loadingRightClick();
    //查询出文件列表
    queryFileData();
    //拼接html 填充主机列表
    queryHostDivForStart(param);
    //查询实例
    queryInstConfigList();
}

/**
 * 默认启动版本加载
 */
function initStartVersion() {
    var versionList = getStartVersionList(param["CLUSTER_ID"], param["CLUSTER_TYPE"]);
    mini.get("defaultDeployVersion").setData(versionList);
    if (versionList != null && versionList.length > 1) {
        mini.get("defaultDeployVersion").setValue(versionList[1]["VERSION"]);
    }
}

/**
 * 查询实例
 */
function queryInstConfigList() {
    var params = {
        CLUSTER_ID:param["CLUSTER_ID"],
        CLUSTER_TYPE:param["CLUSTER_TYPE"]
    };
    datagridLoadPage(mini.get("configGrid"), params, "instConfig.queryInstConfigInfo");
}

/**
 * 渲染操作按钮
 * @param e
 * @returns {String}
 */
function onActionRenderer(e) {
    var rowIndex=e.rowIndex;
    var html = '<a class="Delete_Button" href="javascript:showConfigContent(' + rowIndex +')">配置文件</a>';
    html += '<a class="Delete_Button" href="javascript:checkHost('+rowIndex+')">状态检查</a>';
    html += '<a class="Delete_Button" href="javascript:deleteInst('+rowIndex+')">删除</a>';
    return html;
}

/**
 * 查看配置
 */
function showConfigContent(index) {
    var rowData=JsVar["configGrid"].getRow(index);
    var hostId = rowData.HOST_ID;
    var filePath = rowData.FILE_PATH;
    var instId = rowData.INST_ID;
    var fileName = rowData.FILE_NAME;
    var clusterType = rowData.CLUSTER_TYPE;
    var deployFileType = rowData.DEPLOY_FILE_TYPE;
    if((clusterType==busVar.DCA && deployFileType!=busVar.REDIS)
        || clusterType==busVar.MONITOR
        || clusterType==busVar.DMDB
        || clusterType==busVar.DCLOG
        || clusterType==busVar.M2DB){//多个配置文件
        var params = {
            INST_ID:instId,
            HOST_ID:hostId,
            filePath:filePath
        };
        showDialog("查看配置文件",780,"80%",Globals.baseJspUrl.HOST_JSP_SHOW_MUTIL_CONFIG_CONTENT_URL,
            function destroy(data){
            }, params);
    }else{
        var params = {
            INST_ID:instId,
            HOST_ID:hostId,
            filePath:filePath,
            fileName:fileName
        };
        showDialog("查看配置文件",780,"80%",Globals.baseJspUrl.HOST_JSP_SHOW_CONFIG_CONTENT_URL,
            function destroy(data){
            }, params);
    }
}

/**
 * 检查
 */
function checkHost(index){
    var rowData=JsVar["configGrid"].getRow(index);
    if(rowData==null || rowData==undefined){
        showWarnMessageAlter("请选择一条记录");
        return;
    }
    var state=rowData.STATUS;

    var updateParams=new Object();
    updateParams["INST_ID"]=rowData.INST_ID;
    updateParams["HOST_ID"]=rowData.HOST_ID;
    updateParams["DEPLOY_TYPE"]=rowData.DEPLOY_TYPE;
    updateParams["CLUSTER_TYPE"]=rowData.CLUSTER_TYPE;

    updateParams["DEPLOY_FILE_TYPE"] = rowData.DEPLOY_FILE_TYPE;
    //updateParams["CLUSTER_CODE"]=rowData.DEPLOY_TYPE;
    updateParams["SOFT_LINK_PATH"]=rowData.SOFT_LINK_PATH;
    updateParams["VERSION"]=rowData.VERSION;

    //1.远程主机检查,返回真实状态
    getJsonDataByPost(Globals.baseActionUrl.HOSTSTART_ACTION_CHECK_PROCESS_STATE_URL,rowData,"instconfig--状态检查",
        function(result){
            var processInfo=(result.processPort==undefined?"":result.processPort);
            //2.真实状态与数据库的做对比
            if(state==1){//数据库:运行
                if(result.processState==1){//远程主机：运行
                    if(rowData.DEPLOY_FILE_TYPE == "m2db"){
                        showMessageAlter(rowData.DEPLOY_FILE_TYPE+"程序正在运行，实例名为【"+processInfo+"】");
                    }else{
                        showMessageAlter(rowData.DEPLOY_FILE_TYPE+"程序正在运行，进程号为【"+processInfo+"】");
                    }

                }else{//远程主机：未运行
                    //3.状态不正确，提示更新数据库
                    updateParams["STATUS"]='0';
                    showConfirmMessageAlter(rowData.DEPLOY_FILE_TYPE+"程序未运行，"+processInfo+"是否同步数据库?", function ok(){
                        synchronizeDatabase(updateParams);
                    });
                }
            }else{//数据库:未运行
                if(result.processState==1){//远程主机：运行
                    //3.状态不正确，提示更新数据库
                    updateParams["STATUS"]='1';
                    showConfirmMessageAlter(rowData.DEPLOY_FILE_TYPE+"程序正在运行，进程号为【"+processInfo+"】，是否同步数据库?", function ok(){
                        synchronizeDatabase(updateParams);
                    });
                }else{//远程主机：未运行
                    showWarnMessageAlter(rowData.DEPLOY_FILE_TYPE+"程序未运行!"+processInfo+"");
                }
            }
        });
}

/**
 * 检查状态，数据库同步方法
 */
function synchronizeDatabase(params){
    getJsonDataByPost(Globals.baseActionUrl.HOSTSTART_ACTION_UPDATE_PROCESS_STATE_URL,params,"instconfig--状态检查--同步数据库状态",
        function(result){
            if(result!="error"){
                showMessageAlter("同步数据库成功！");
                //刷新
                queryInstConfigList();
            }else{
                showErrorMessageAlter("同步数据库失败:"+result);
            }
        });
}

/**
 * 删除已停止实例
 * @param index
 */
function deleteInst(index){
    var rowData=JsVar["configGrid"].getRow(index);
    if(rowData==null || rowData==undefined){
        showWarnMessageAlter("请选择一条记录");
        return;
    }
    var RUN_STATE=rowData.STATUS;
    if(RUN_STATE == 1){
        showWarnMessageAlter("该程序正在运行中，不可删除！");
        return;
    }

    var msg="确定删除记录？";
    if(rowData.DEPLOY_TYPE==busVar.DMDB){
        msg="确定删除记录及对应远程主机上的目录？";
    }
    showConfirmMessageAlter(msg,function ok(){
        getJsonDataByPost(Globals.baseActionUrl.INST_CONFIG_TASK_ACTION_DELETE_INFOMATION_URL,rowData,"instconfig--删除已停止的实例信息",
            function(result){
                if(result.Success!=undefined){
                    showMessageAlter("删除成功！");
                }else if(result.error!=undefined){
                    showErrorMessageAlter("删除失败！请检查！");
                }

                queryInstConfigList();
            });
    });
}

/**
 * 状态渲染
 * @param e
 * @returns {String}
 */
function statusRenderer(e) {
    var RUN_STATE=e.record.STATUS;
    var html= "";
    if(RUN_STATE == 0 || RUN_STATE == null){//未启用
        return "<span class='label label-danger' style='letter-spacing:0.2em;'>&nbsp;停止&nbsp;</span>";
    }else if(RUN_STATE == 1){//已启用， 运行中
        return "<span class='label label-success' style='letter-spacing:0.2em;'>&nbsp;运行&nbsp;</span>";
    }
}


/**
 * 点击后可能出现滚动条需要重新渲染页面
 */
function clickPanelBtn() {
    setTimeout(function(){
        mini.parse();
    }, 500);
}

/**
 * 勾选实例
 * @param e
 */
function selectGridRow(e) {

    var selRows = JsVar["configGrid"].getSelecteds();

    //判断当前行是否勾选中，如果没有勾选中则当前行删除
    $("#paramsInfo:eq(0)>tr").each(function(index, item){
        var isExists = false;
        var instId = $(item).data("INST_ID");
        for (var i=0; i<selRows.length; i++) {
            if (selRows[i]["INST_ID"] == instId) {
                isExists = true;
                break;
            }
        }
        if (instId && !isExists) {
            $(item).remove();
        }
    });

    //判断勾选中的行是否有创建，如果没有创建则创建勾选行
    if (selRows != null) {
        for (var i=0; i<selRows.length; i++) {
            var selRow = selRows[i];
            //判断选中实例是否已经创建
            var isCreate= false;
            $("#paramsInfo:eq(0)>tr").each(function(index, item){
                var instId = $(item).data("INST_ID");
                if (selRow["INST_ID"] == instId) {
                    isCreate = true;
                    return false;
                }
            });
            if (!isCreate && selRow["STATUS"] != busVar.STATE_ACTIVE) {
                addConfigRow(null, selRow["HOST_ID"], selRow["HOST_IP"], selRow["SSH_USER"], true, selRow, selRow["INST_ID"]);
            }
        }
    }
}

/**
 * 单台主机启停，不刷新所有表格数据
 */
function singleChange(obj){
    //获取主机ID
    var hostId = $(obj).val();
    //获取主机IP
    var hostIp = $(obj).data("HOST_IP");
    //获取主机用户
    var sshUser = $(obj).data("SSH_USER");
    //获取主机是否选中
    var checked = $(obj).attr("checked");
    var str = "";
    if (checked || checked == "checked") {
        var timeSeq = (new Date()).getTime();
        str+='<tr style="padding-top:10px;" time="' + timeSeq + '" id="tr_'+hostIp+'" data-HOST_IP="'+hostIp+'" >'
            +'<th><span>主机：</span></th>'
            +'<td class="host_ips" id="host_ips">'
            +'  <input rowType="ip" class="mini-textbox" allowInput="false" '
            +'	    name="ip_'+timeSeq+'" id="ip_'+timeSeq+'" value="'+hostIp + '(' + sshUser + ')'+'" style="width:100%;">'
            +'</td>'

            +'<th><span class="fred">*</span>启动模式：</th>'
            +'<td>'
            +'  <input class="mini-combobox" name="deployType_'+timeSeq+'" id="deployType_'+timeSeq+'" popupWidth="100%" '
            +'	    onvaluechanged="changeDeploy(\'deployType_'+timeSeq+'\', '+timeSeq+')" '
            +'	    data="getSysDictData(\'dmdb_deploy_type\')" textField="text" valueField="code" required="true" '
            +'	    showNullItem="false" style="width:100%;"/>'
            +'</td> '

            +'<th><span class="fred">*</span>配置文件：</th>'
            +'<td>'
            +'  <input class="mini-treeselect" id="file_'+timeSeq+'" onvaluechanged="valueChange" popupHeight="220px" popupWidth="100%" '
            +'	    valueField="relativePath" textField="fileName" allowInput="false" parentField="parentRelativePath" showFolderCheckBox="false" showCheckBox="true" multiSelect="true" '
            +'	    name="file_'+timeSeq+'" style="width:100%;"/>'
            +'</td>'

            +'<th><span class="fred">*</span>启动版本：</th>'
            +'<td>'
            +'  <input class="mini-combobox" id="version_'+timeSeq+'" popupWidth="100%" '
            +'	    valueField="VERSION" textField="VERSION" allowInput="false"  data="getStartVersionData(\''+param["CLUSTER_ID"]+'\', \''+param["CLUSTER_TYPE"]+'\', \''+hostId+'\')" '
            +'	    name="version_'+timeSeq+'" style="width:100%;"/>'
            +'</td>'

            +'<td>'
            +'  <div style="height:100%;line-height:30px;">'
            +'      <div style="text-align:right;width:45%;cursor:pointer;float:left;margin:2px 2px 2px 0px;" title="添加" id="addRow_' + timeSeq + '" name="addRow_'+timeSeq+'" href="javascript:void(0)" onclick="addNextRow(this, \''+timeSeq+'\', \''+hostIp+'\', \''+hostId+'\', \''+sshUser+'\')"><img src="../../../images/deployHost/add_row.png" /></div>'
            +'      <div style="text-align:left;width:45%;cursor:pointer;float:left;margin:2px 2px 2px 5px;" title="删除" id="removeRow_' + timeSeq + '" name="removeRow_'+timeSeq+'" href="javascript:void(0)" onclick="delCurrentRow(this, \''+timeSeq+'\', \''+hostIp+'\', \''+hostId+'\')"><img src="../../../images/deployHost/del_row.png" /></div>'
            +'  </div>'
            +'</td>'

            +'<td> '
            +'	<input class="mini-hidden" name="hostId_'+timeSeq+'" id="hostId_'+timeSeq+'" value="'+hostId+'"/></td> '
            +'</tr>';
        $("#paramsInfo").append(str);
        mini.parse();

        //获取最新的文件使用数据
        getLastFileData();

        //必须深克隆一个数组，不然个主机数据初始数据不能保持一致
        var tempData = deepClone(JsVar["fileData"]);

        //获取配置文件控件ID
        var tid="file_"+timeSeq;

        //设置版本初始化,默认选中最新版本启动
        var versionData = mini.get("version_" + timeSeq).getData();
        if (versionData != null && versionData.length > 0) {
            var lastVersion = versionData[0]["VERSION"];
            mini.get("version_" + timeSeq).setValue(lastVersion);

            //设置版本为当前共用选择版本
            var deployVersion = mini.get("defaultDeployVersion").getValue();
            if (deployVersion != null && deployVersion != '' && typeof(deployVersion) != 'undefined') {
                mini.get("version_" + timeSeq).setValue(deployVersion);
            }
        }

        //配置文件
        var rootData = [];
        for (var j=0; j<tempData.length; j++) {
            tempData[j]["HOST_ID"] = hostId;
            tempData[j]["INDEX"] = timeSeq;

            //获取到最根节点
            if ((tempData[j]["parentId"] == null || tempData[j]["parentId"] == "")
                && (tempData[j]["parentRelativePath"] == null || tempData[j]["parentRelativePath"] == "")) {
                rootData.add(tempData[j]);
            }
        }

        //获取根节点下级节点
        var excludeFileArray = [];
        for (var i=0; i<rootData.length; i++) {
            for (var j=0; j<tempData.length; j++) {
                if (rootData[i]["id"] == tempData[j]["parentId"]
                    && tempData[j]["fileName"].indexOf("_") != -1
                    && tempData[j]["fileName"].indexOf(hostIp+"_") == -1) {
                    var excludeFile = tempData.splice(j, 1);
                    excludeFileArray.add(excludeFile[0]);
                    j--;
                }
            }
        }

        //根据主机IP过滤配置文件
        for (var i=0; i<excludeFileArray.length; i++) {
            for (var j=0; j<tempData.length; j++) {
                if (tempData[j]["parentId"] == excludeFileArray[i]["id"]) {
                    tempData.splice(j, 1);
                    j--;
                }
            }
        }

        //设置配置文件
        mini.get(tid).tree.loadList(tempData, "relativePath", "parentRelativePath");

        //将当前主机配置文件数据保存，用来在配置文件Change事件中作为数据源使用
        var isExists = false;
        for (var j=0; j<deployTypeFileArray.length; j++) {
            if (deployTypeFileArray[j]["ID"] == tid) {
                deployTypeFileArray[j]["DATA"] = tempData;
                isExists = true;
                break;
            }
        }
        if (!isExists) {
            deployTypeFileArray.push({
                ID:tid,
                DATA:tempData
            });
        }

        //渲染下拉树控件图标
        mini.get(tid).tree.on("drawNode", function(e) {
            var node = e.node;
            //设置图标
            if (node["fileType"] == "D"){
                e.iconCls = "mini-tree-expand mini-tree-folder";
            }
            //dcam/dcas/loadredis查看目录下的配置文件
            if ((node["rootName"] == "instance_pattern" || node["rootName"] == "route_pattern" || node["rootName"] == "main_pattern") && node.parentId != null && node.parentId != "") {
                var hostId = node["HOST_ID"];
                var instPath = node["relativePath"];
                var fileName = node["fileName"];
                e.nodeHtml += '&nbsp;&nbsp;<img src="'+Globals.ctx + '/images/goto.gif" '
                    +  ' style="cursor:pointer;" title="查看配置文件" onclick="showMutilConfig(\''+param["CLUSTER_ID"]+'\', \''+param["CLUSTER_TYPE"]+'\', \''+hostId+'\', \''+instPath+'\', \''+fileName+'\')" />';

                e.nodeHtml += '&nbsp;&nbsp;<img src="'+Globals.ctx + '/images/comlog.png" '
                    +  ' style="cursor:pointer;" title="查看最近一次操作日志" onclick="showStartOperationLog(\''+param["CLUSTER_ID"]+'\', \''+param["CLUSTER_TYPE"]+'\', \''+hostId+'\', \''+instPath+'\', \''+instPath+'\', \''+timeSeq+'\')" />';
            }
            //判断是否使用
            if (node["IS_USED"] == "true") {
                e.nodeHtml += '&nbsp;<img src="'+Globals.ctx + '/images/ok.png" style="cursor:pointer;" title="配置文件已经被使用，不能再被选择" />';
            }
        });
    } else {
        //主机不选中删除对应的数据行
        $("#paramsTable>tbody:eq(0)>tr").each(function(index, item){
            var trId = $(item).attr("id");
            if (trId == "tr_" + hostIp) {
                $(item).remove();
            }
        });
    }
}

/**
 * 在当前行下面添加下一行
 * @param obj
 * @param timeseq
 */
function addNextRow(obj, timeseq, hostIp, hostId, sshUser) {
    var trObj = $("tr[time='"+timeseq+"']");
    addConfigRow(trObj, hostId, hostIp, sshUser, false, null);
}

/**
 * 添加一行记录
 * @param currentRow 当前行对象
 * @param hostId 主机ID
 * @param hostIp 主机IP
 * @param isFull 是否需要填充值
 * @param fullObj 填充值对象
 * @param instId 实例ID
 * @returns
 */
function addConfigRow(currentRow, hostId, hostIp, sshUser, isFull, fullObj, instId) {
    var timeSeq = (new Date()).getTime();
    var str = "";
    var timeSeq = (new Date()).getTime();
    str+='<tr style="padding-top:10px;" time="' + timeSeq + '" id="tr_'+hostIp+'" data-HOST_IP="'+hostIp+'" data-INST_ID="'+instId+'" >'
        +'<th><span>主机：</span></th>'
        +'<td class="host_ips" id="host_ips">'
        +'  <input rowType="ip" class="mini-textbox" allowInput="false" '
        +'	    name="ip_'+timeSeq+'" id="ip_'+timeSeq+'" value="'+hostIp + '(' + sshUser + ')'+'" style="width:100%;">'
        +'</td>'

        +'<th><span class="fred">*</span>启动模式：</th>'
        +'<td>'
        +'  <input class="mini-combobox" name="deployType_'+timeSeq+'" id="deployType_'+timeSeq+'" popupWidth="100%" '
        +'	    onvaluechanged="changeDeploy(\'deployType_'+timeSeq+'\', '+timeSeq+')" '
        +'	    data="getSysDictData(\'dmdb_deploy_type\')" textField="text" valueField="code" required="true" '
        +'	    showNullItem="false" style="width:100%;"/>'
        +'</td> '

        +'<th><span class="fred">*</span>配置文件：</th>'
        +'<td>'
        +'  <input class="mini-treeselect" id="file_'+timeSeq+'" onvaluechanged="valueChange" popupHeight="220px" popupWidth="100%" '
        +'	    valueField="relativePath" textField="fileName" allowInput="false" parentField="parentRelativePath" showFolderCheckBox="false" showCheckBox="true" multiSelect="true" '
        +'	    name="file_'+timeSeq+'" style="width:100%;"/>'
        +'</td>'

        +'<th><span class="fred">*</span>启动版本：</th>'
        +'<td>'
        +'  <input class="mini-combobox" id="version_'+timeSeq+'" popupWidth="100%" '
        +'	    valueField="VERSION" textField="VERSION" allowInput="false"  data="getStartVersionData(\''+param["CLUSTER_ID"]+'\', \''+param["CLUSTER_TYPE"]+'\', \''+hostId+'\')" '
        +'	    name="version_'+timeSeq+'" style="width:100%;"/>'
        +'</td>'

        +'<td>'
        +'  <div style="height:100%;line-height:30px;">'
        +'      <div style="text-align:right;width:45%;cursor:pointer;float:left;margin:2px 2px 2px 0px;" title="添加" id="addRow_' + timeSeq + '" name="addRow_'+timeSeq+'" href="javascript:void(0)" onclick="addNextRow(this, \''+timeSeq+'\', \''+hostIp+'\', \''+hostId+'\', \''+sshUser+'\')"><img src="../../../images/deployHost/add_row.png" /></div>'
        +'      <div style="text-align:left;width:45%;cursor:pointer;float:left;margin:2px 2px 2px 5px;" title="删除" id="removeRow_' + timeSeq + '" name="removeRow_'+timeSeq+'" href="javascript:void(0)" onclick="delCurrentRow(this, \''+timeSeq+'\', \''+hostIp+'\', \''+hostId+'\')"><img src="../../../images/deployHost/del_row.png" /></div>'
        +'  </div>'
        +'</td>'

        +'<td> '
        +'	<input class="mini-hidden" name="hostId_'+timeSeq+'" id="hostId_'+timeSeq+'" value="'+hostId+'"/>'
        +'</td> '
        +'</tr>';
    if (currentRow == null) {
        $("#paramsInfo").append(str);
    } else {
        $(currentRow).after(str);
    }
    mini.parse();

    //获取最新的文件使用数据
    getLastFileData();

    //必须深克隆一个数组，不然个主机数据初始数据不能保持一致
    var tempData = deepClone(JsVar["fileData"]);

    //获取配置文件控件ID
    var tid="file_"+timeSeq;

    //设置版本初始化,默认选中最新版本启动
    var versionData = mini.get("version_" + timeSeq).getData();
    if (versionData != null && versionData.length > 0) {
        var lastVersion = versionData[0]["VERSION"];
        mini.get("version_" + timeSeq).setValue(lastVersion);

        //设置版本为当前共用选择版本
        var deployVersion = mini.get("defaultDeployVersion").getValue();
        if (deployVersion != null && deployVersion != '' && typeof(deployVersion) != 'undefined') {
            mini.get("version_" + timeSeq).setValue(deployVersion);
        }
    }

    //配置文件
    var rootData = [];
    for (var j=0; j<tempData.length; j++) {
        tempData[j]["HOST_ID"] = hostId;
        tempData[j]["INDEX"] = timeSeq;

        //获取到最根节点
        if ((tempData[j]["parentId"] == null || tempData[j]["parentId"] == "")
            && (tempData[j]["parentRelativePath"] == null || tempData[j]["parentRelativePath"] == "")) {
            rootData.add(tempData[j]);
        }
    }

    //获取根节点下级节点
    var excludeFileArray = [];
    for (var i=0; i<rootData.length; i++) {
        for (var j=0; j<tempData.length; j++) {
            if (rootData[i]["id"] == tempData[j]["parentId"]
                && tempData[j]["fileName"].indexOf("_") != -1
                && tempData[j]["fileName"].indexOf(hostIp+"_") == -1) {
                var excludeFile = tempData.splice(j, 1);
                excludeFileArray.add(excludeFile[0]);
                j--;
            }
        }
    }

    //根据主机IP过滤配置文件
    for (var i=0; i<excludeFileArray.length; i++) {
        for (var j=0; j<tempData.length; j++) {
            if (tempData[j]["parentId"] == excludeFileArray[i]["id"]) {
                tempData.splice(j, 1);
                j--;
            }
        }
    }

    //设置配置文件
    mini.get(tid).tree.loadList(tempData, "relativePath", "parentRelativePath");

    //将当前主机配置文件数据保存，用来在配置文件Change事件中作为数据源使用
    var isExists = false;
    for (var j=0; j<deployTypeFileArray.length; j++) {
        if (deployTypeFileArray[j]["ID"] == tid) {
            deployTypeFileArray[j]["DATA"] = tempData;
            isExists = true;
            break;
        }
    }
    if (!isExists) {
        deployTypeFileArray.push({
            ID:tid,
            DATA:tempData
        });
    }

    //渲染下拉树控件图标
    mini.get(tid).tree.on("drawNode", function(e) {
        var node = e.node;
        //设置图标
        if (node["fileType"] == "D"){
            e.iconCls = "mini-tree-expand mini-tree-folder";
        }
        //dcam/dcas/loadredis查看目录下的配置文件
        if ((node["rootName"] == "instance_pattern" || node["rootName"] == "route_pattern" || node["rootName"] == "main_pattern") && node.parentId != null && node.parentId != "") {
            var hostId = node["HOST_ID"];
            var instPath = node["relativePath"];
            var fileName = node["fileName"];
            e.nodeHtml += '&nbsp;&nbsp;<img src="'+Globals.ctx + '/images/goto.gif" '
                +  ' style="cursor:pointer;" title="查看配置文件" onclick="showMutilConfig(\''+param["CLUSTER_ID"]+'\', \''+param["CLUSTER_TYPE"]+'\', \''+hostId+'\', \''+instPath+'\', \''+fileName+'\')" />';

            e.nodeHtml += '&nbsp;&nbsp;<img src="'+Globals.ctx + '/images/comlog.png" '
                +  ' style="cursor:pointer;" title="查看最近一次操作日志" onclick="showStartOperationLog(\''+param["CLUSTER_ID"]+'\', \''+param["CLUSTER_TYPE"]+'\', \''+hostId+'\', \''+instPath+'\', \''+instPath+'\', \''+timeSeq+'\')" />';
        }
        //判断是否使用
        if (node["IS_USED"] == "true") {
            e.nodeHtml += '&nbsp;<img src="'+Globals.ctx + '/images/ok.png" style="cursor:pointer;" title="配置文件已经被使用，不能再被选择" />';
        }
    });

    //是否需要填充数据， 当重载数据时需要填充数据
    if (isFull) {
        //设置选中行数据
        mini.get("deployType_" + timeSeq).setValue(fullObj["DEPLOY_FILE_TYPE"]);
        //触发部署类型Change事件
        changeDeploy("deployType_" + timeSeq, timeSeq);
        mini.get("file_" + timeSeq).setValue(fullObj["CONFIG_PATH"]);
        var currentNode = mini.get("file_" + timeSeq).tree.getNode(fullObj["CONFIG_PATH"]);
        mini.get("file_" + timeSeq).tree.selectNode(currentNode);
        mini.get("version_" + timeSeq).setValue(fullObj["VERSION"]);

        //判断主机是否被选中，如果没选中在选中主机
        $("#paramsTable>tbody:eq(0)>tr").each(function(index, item){
            var trId = $(item).attr("id");
            if (trId.indexOf(hostIp) != -1) {
                $("#hostFitDiv input[id='"+hostId+"']").attr("checked", true);
                return false;
            }
        });
    }
}


/**
 * 删除当前行
 * @param obj
 * @param timeseq
 */
function delCurrentRow(obj, timeseq, hostIp, hostId) {
    var trObj = $("tr[time='"+timeseq+"']");
    if (trObj != null) {
        //判断INST_ID是否存在
        var instId = $(trObj).data("INST_ID");
        if (instId) {
            var configList = JsVar["configGrid"].getSelecteds();
            for (var i=0; i<configList.length; i++) {
                if (configList[i]["INST_ID"] == instId) {
                    JsVar["configGrid"].deselect(configList[i]);
                    break;
                }
            }
        }

        //删除行对象
        trObj.remove();

        //判断当前主机是否只要最后一行，如果为最后一行将复选框不要勾选
        var isExist = false;
        $("#paramsTable>tbody:eq(0)>tr").each(function(index, item){
            var trId = $(item).attr("id");
            if (trId.indexOf(hostIp) != -1) {
                isExist = true;
            }
        });
        if (!isExist) {
            $("#hostFitDiv input[id='"+hostId+"']").attr("checked", false);
        }
    }
}

/**
 * 保存当前配置
 */
function addOperator() {
    var forms = new mini.Form("#paramsTable");
    if (forms.isValid() == false){
        return;
    }

    var rowData = [];
    $("#paramsInfo>tr").each(function(index, item){
        var time = $(item).attr("time");

        var hostIp = $(this).data("HOST_IP");
        var deployType = mini.get("deployType_"+time).getValue();
        var file = mini.get("file_"+time).getValue();
        var version = mini.get("version_"+time).getValue();
        var hostId = mini.get("hostId_"+time).getValue();

        var singleParams = {
            HOST_IP:hostIp,
            DEPLOY_FILE_TYPE:deployType,
            CONFIG_PATH:file,
            VERSION:version,
            HOST_ID:hostId
        };

        rowData.add(singleParams);
    });

    //配置校验
    if (rowData != null && rowData.length > 0) {
        for (var i=0; i<rowData.length; i++) {
            var deployFileType = rowData[i]["DEPLOY_FILE_TYPE"];
            var configPath = rowData[i]["CONFIG_PATH"];
            var version = rowData[i]["VERSION"];
            var hostId = rowData[i]["HOST_ID"];
            var hostIp = rowData[i]["HOST_IP"];

            if (deployFileType == "" || configPath == "" || version == "") {
                showWarnMessageAlter("启动模式、配置文件、版本均不能为空，请选择！");
                return;
            }

            for (var j=0; j<rowData.length; j++) {
                var nextDeployFileType = rowData[j]["DEPLOY_FILE_TYPE"];
                var nextConfigPath = rowData[j]["CONFIG_PATH"];
                var nextHostIp = rowData[j]["HOST_IP"];
                if (i != j && (deployFileType == nextDeployFileType && configPath == nextConfigPath)) {
                    var configFile = nextConfigPath.substr(nextConfigPath.indexOf("/")+1);
                    showWarnMessageAlter("相同启动模式不能选择同一配置文件,<br/>主机IP【"+hostIp + ", " + nextHostIp +"】,启动模式为【"+nextDeployFileType+"】， 配置文件【"+configFile+"】,请选择！");
                    return;
                }
            }
        }
    }

    //配置数据
    var params = {
        CLUSTER_ID:param["CLUSTER_ID"],
        CLUSTER_TYPE:param["CLUSTER_TYPE"],
        CLUSTER_CODE:param["CLUSTER_CODE"],
        CLUSTER_NAME:param["CLUSTER_NAME"],
        HOST_LIST:rowData
    };

    mini.prompt("<span class='fred'>*</span>批次号:", "启动配置项保存", function(action, value) {
        if (action == "ok") {
            if (value == null || value.trim() == "") {
                showWarnMessageAlter("批次名称不能为空， 请输入批次名称！");
            } else if (value.length > 15){
                showWarnMessageAlter("批次名称过程， 批次名称长度不得超过15个汉字！");
            } else {
                params["BATCH_NAME"] = value;
                //保存数据
                getJsonDataByPost(Globals.baseActionUrl.HOST_START_ACTION_INIT_CONFIG_URL, params, "组件启动-保存配置项数据",
                    function success(result){
                        if (result != null && result.RST_CODE == "1") {
                            showMessageAlter(result.RST_MSG);
                        } else {
                            showErrorMessageAlter(result.RST_MSG);
                        }
                    },null,null,false);
            }
        }
    });
}

/**
 * 重载上次数据
 */
function reloadConfig() {
    showDialog("载入批次","80%","90%",Globals.baseJspUrl.INST_CONFIG_JSP_START_ONCE_URL,
        function destroy(data){
            if (data != null && data["BATCH_NAME"] != null) {
                var batchName = data["BATCH_NAME"];

                //根据批次名称查询配置文件信息
                var params = {
                    CLUSTER_ID:param["CLUSTER_ID"],
                    BATCH_NAME:batchName
                };
                getJsonDataByPost(Globals.baseActionUrl.HOST_START_ACTION_LOAD_CONFIG_URL, params, "",
                    function success(result){
                        if (result != null && result != '') {
                            //清空所有节点数据
                            selectNone();

                            //加载重载数据
                            for (var i=0; i<result.length; i++) {
                                var hostId = result[i]["HOST_ID"];
                                var hostIp = result[i]["HOST_IP"];
                                var sshUser = result[i]["SSH_USER"];
                                var hostCheck = $("#hostFitDiv input[id='"+hostIp+"']").attr("checked");
                                if (hostCheck == false || hostCheck == undefined) {
                                    $("#hostFitDiv input[id='"+hostIp+"']").attr("checked", true);
                                }
                                addConfigRow(null, hostId, hostIp, sshUser, true, result[i]);
                            }
                        } else {
                            showWarnMessageAlter("没有保存配置数据！");
                        }
                    },null, null, false);
            }
        }, param, {allowDrag:false});
}

/**
 * 刷新数据
 */
function refreshData() {
    //获取最新的文件使用数据
    getLastFileData();

    $("#paramsTable>tbody:eq(0)>tr").each(function(index, item){
        var timeSeq = $(item).attr("time");
        var tid = "file_" + timeSeq;

        //克隆获取配置文件列表
        var tempData = deepClone(JsVar["fileData"]);
        //获取当前行主机ID
        var hostId = mini.get("hostId_" + timeSeq).getValue();

        //配置文件
        var rootData = [];
        for (var j=0; j<tempData.length; j++) {
            tempData[j]["HOST_ID"] = hostId;
            tempData[j]["INDEX"] = timeSeq;

            //获取到最根节点
            if ((tempData[j]["parentId"] == null || tempData[j]["parentId"] == "")
                && (tempData[j]["parentRelativePath"] == null || tempData[j]["parentRelativePath"] == "")) {
                rootData.add(tempData[j]);
            }
        }

        //获取根节点下级节点
        var excludeFileArray = [];
        var hostIp = $(this).data("HOST_IP");
        for (var i=0; i<rootData.length; i++) {
            for (var j=0; j<tempData.length; j++) {
                if (rootData[i]["id"] == tempData[j]["parentId"]
                    && tempData[j]["fileName"].indexOf("_") != -1
                    && tempData[j]["fileName"].indexOf(hostIp+"_") == -1) {
                    var excludeFile = tempData.splice(j, 1);
                    excludeFileArray.add(excludeFile[0]);
                    j--;
                }
            }
        }

        //根据主机IP过滤配置文件
        for (var i=0; i<excludeFileArray.length; i++) {
            for (var j=0; j<tempData.length; j++) {
                if (tempData[j]["parentId"] == excludeFileArray[i]["id"]) {
                    tempData.splice(j, 1);
                    j--;
                }
            }
        }

        //设置配置文件下拉框数据
        mini.get(tid).tree.loadList(tempData, "relativePath", "parentRelativePath");

        //将当前主机配置文件数据保存，用来在配置文件Change事件中作为数据源使用
        var isExists = false;
        for (var j=0; j<deployTypeFileArray.length; j++) {
            if (deployTypeFileArray[j]["ID"] == tid) {
                deployTypeFileArray[j]["DATA"] = tempData;
                isExists = true;
                break;
            }
        }
        if (!isExists) {
            deployTypeFileArray.push({
                ID:tid,
                DATA:tempData
            });
        }
        //触发部署类型Change事件
        changeDeploy("deployType_" + timeSeq, timeSeq);
    });
}

/**
 * 选择启动模式
 * @param id
 */
function changeDeploy(id, index) {
    //获取当前行配置文件列表，不能直接使用JsVar["fileData"]
    var file_id="file_"+index;

    //获取当前行配置文件列表
    var tempData = [];
    for (var i=0; i<deployTypeFileArray.length; i++) {
        if (deployTypeFileArray[i]["ID"] == file_id) {
            tempData = deployTypeFileArray[i]["DATA"];
        }
    }

    //根据部署类型过滤配置文件
    var currentDeployArray = [];
    var deployType = mini.get(id).getValue();

    if (deployType == "sync_pattern" || deployType == "mgr_pattern" || deployType == "watcher_pattern" || deployType == "movesync_pattern") {
        deployType = "instance_pattern";
	}

    for (var i=0; i<tempData.length; i++) {
        if (tempData[i]["rootName"] == deployType) {
            currentDeployArray.push(tempData[i]);
        }
    }
    //配置文件设置数据
    mini.get(file_id).tree.loadList(currentDeployArray, "relativePath", "parentRelativePath");

    //清空文件选择
    mini.get(file_id).setValue("");
}

/**
 * 获取最新的下拉框数据(dmdb拿文件列表)
 */
function getLastFileData() {
    var fileParams = {
        DEPLOY_TYPE:busVar.DCA,
        CLUSTER_ID:param["CLUSTER_ID"]
    };
    getJsonDataByPost(Globals.baseActionUrl.FRAME_QUERY_FOR_LIST_URL, fileParams, "组件启停-查询组件集群实例",
        function success(result){
            //实例最新使用文件列表
            lastFileData = result;
            if (result != null && result.length > 0 && JsVar["fileData"].length > 0) {
                for (var i=0; i<JsVar["fileData"].length; i++) {
                    for (var j=0; j<result.length; j++) {
                        if (JsVar["fileData"][i]["relativePath"] == JsVar["fileData"][i]["rootName"] + "/" + result[j]["INST_PATH"]
                            && JsVar["fileData"][i]["rootName"] == result[j]["DEPLOY_FILE_TYPE"]) {
                            JsVar["fileData"][i]["IS_USED"] = "true";
                            break;
                        }
                    }
                }
            }
        },"instConfig.queryInstConfigList",null,false);
}

/**
 * 节点选择事件
 * @param e
 */
function valueChange(e) {

    var selNodeList = e.source.tree.getCheckedNodes();
    if (selNodeList != null && selNodeList.length > 0) {
        var totalFileName = [];
        for (var i=0; i<selNodeList.length; i++) {
            var nodeUsed = selNodeList[i]["IS_USED"];
            //选中节点为已经使用或者为非叶子节点不能被选中
            if (nodeUsed == "true" || selNodeList[i]["parentRelativePath"] == null || selNodeList[i]["parentRelativePath"] == "") {
                e.source.tree.disableNode(selNodeList[i]);
                e.source.tree.uncheckNode(selNodeList[i]);
            } else {
                totalFileName.push(selNodeList[i]["fileName"]);
            }
        }
        mini.get(e.source.id).setText(totalFileName.join(","));
    }

//	var isUsed = e.source.tree.getSelectedNode().IS_USED;
//	//选中节点为已经使用或者为非叶子节点不能被选中
//	if (isUsed == "true"
//		|| e.source.tree.getSelectedNode().parentRelativePath == null) {
//		mini.get(e.source.id).setText("");
//		mini.get(e.source.id).setValue("");
//	}
}

/**
 * 点击提交
 */
var textValue="";
var startTimes = 0;
function onSubmit(){
    $("#deployTextarea").html("");
    var params=new Object();
    if (new mini.Form("#paramsTable").isValid() == false){
        return;
    }

    //获取所有的选中主机配置数据航
    var params = [];
    $("#paramsTable>tbody:eq(0)>tr").each(function(index, item){
        var time = $(item).attr("time");
        var ip = mini.get("ip_"+time).getValue();
        var deployType = mini.get("deployType_"+time).getValue();
        var version = mini.get("version_"+time).getValue();
        var host = mini.get("hostId_"+time).getValue();
        var fileStr = mini.get("file_"+time).getValue();

        if (fileStr != null) {
            var fileList = fileStr.split(",");
            for (var i=0; i<fileList.length; i++) {
                var file = fileList[i];
                var singleParams = {
                    HOST_IP:ip,
                    DEPLOY_TYPE:deployType,
                    FILE:file,
                    VERSION:version,
                    HOST_ID:host,
                    CLUSTER_TYPE:param["CLUSTER_TYPE"],
                    CLUSTER_ID:param["CLUSTER_ID"],
                    CLUSTER_CODE:param["CLUSTER_CODE"],
                    CLUSTER_NAME:param["CLUSTER_NAME"]
                };
                params.push(singleParams);
            }
        } else {
            var singleParams = {
                HOST_IP:ip,
                DEPLOY_TYPE:deployType,
                FILE:"",
                VERSION:version,
                HOST_ID:host,
                CLUSTER_TYPE:param["CLUSTER_TYPE"],
                CLUSTER_ID:param["CLUSTER_ID"],
                CLUSTER_CODE:param["CLUSTER_CODE"],
                CLUSTER_NAME:param["CLUSTER_NAME"]
            };
            params.push(singleParams);
        }
    });

    //被选中主机的个数
    var checkedHostLength = $("input[name='ck_host']:checked").length;
    if (params.length == 0 || checkedHostLength == 0) {
        showWarnMessageAlter("请选择需要启停主机！");
        return;
    }

    //参数校验
    for (var i=0; i<params.length; i++) {
        if (params[i]["DEPLOY_TYPE"] == "" || params[i]["FILE"] == "" || params[i]["VERSION"] == "") {
            showWarnMessageAlter("配置文件、版本均不能为空，请选择！");
            return;
        }
    }

    textValue='';
    sumbitButton.setText("正在启动");
    sumbitButton.setEnabled(false);
    startTimes = (new Date()).getTime();
    textValue += "启动开始时间: " + ((new Date(startTimes)).format("yyyy-MM-dd hh:mm:ss")) + "<br/>";
    postAjaxForDeploy(params);
}

/**
 * 启动Dmdb
 * @param params
 */
function postAjaxForDeploy(params){
    textValue +="主机正在启动...<br/>";
    $("#deployTextarea").html(textValue);

    //将滚动条自动滚动到最下面
    var deployDiv = document.getElementById("mainDiv");
    deployDiv.scrollTop = Math.max(0, deployDiv.scrollHeight - deployDiv.offsetHeight);

    getJsonDataByPost(Globals.baseActionUrl.DMDB_TASK_ACTION_RUN_INFOMATION_URL,params,"启停管理--启动dmdb",
        function(result){
            if(result && result["RST_STR"] != ""){
                textValue+=result["RST_STR"];
                textValue=textValue.replaceAll("\n","<br/>");
                var endTimes = (new Date()).getTime();
                textValue += "运行结束时间: " + ((new Date(endTimes)).format("yyyy-MM-dd hh:mm:ss")) + ", 本次运行"+params.length+"个实例, 总耗时: " + ((endTimes - startTimes)/1000).toFixed(2) + "秒";
                $("#deployTextarea").html(textValue);

                //将滚动条自动滚动到最下面
                deployDiv.scrollTop = Math.max(0, deployDiv.scrollHeight - deployDiv.offsetHeight);

                //刷新数据
                refreshData();
                //更新实例数据
                queryInstConfigList();

                sumbitButton.setText("启动");
                sumbitButton.setEnabled(true);

                var rstCode = result["RST_CODE"];
                if (rstCode == busVar.FAILED) {
                    showErrorMessageAlter("启动失败");
                } else {
                    showMessageAlter("启动成功");
                    //当关闭窗口时,用于判断是否重新加载
                    param["submit_state"] =1;
                }
                //高亮检索“失败”“成功”关键字
                heightLightKeyWord();
                mini.parse();
            }
        });
}

/**
 * 组件状态检查
 */
function showStatus() {
    showClusterStatus(param);
}

/**
 * 高亮检索关键字
 */
function heightLightKeyWord(){
    $("#deployTextarea").textSearch("success,successful",{textReduction:false,divStr:",",markCss: "font-weight:bold;color:#429C39;letter-spacing:.1em;",nullReport:false,caseIgnore:true});
    $("#deployTextarea").textSearch("failed,error",{textReduction:false,divStr:",",markCss: "font-weight:bold;color:#CF5130;letter-spacing:.1em;",nullReport:false,caseIgnore:true});
}

/**
 * 根据state判断是否部署过主机,如果部署过,关闭窗口后要刷新
 */
function close(){
    closeWindow();
}

/**
 * 获取配置文件列表
 */
function queryFileData() {
    getJsonDataByPost(Globals.baseActionUrl.DEPLOY_TASK_ACTION_GET_CONFIG_LIST_URL, param, "启停管理--获取启停配置文件",
        function(result) {
            if (result != null && result.length > 0) {
                JsVar["fileData"] = result;
                for (var i=0; i<JsVar["fileData"].length; i++) {
                    if (JsVar["fileData"][i]["relativePath"] == "") {
                        JsVar["fileData"][i]["relativePath"] = null;
                    }
                    if (JsVar["fileData"][i]["parentRelativePath"] == "") {
                        JsVar["fileData"][i]["parentRelativePath"] = null;
                    }
                    if ((JsVar["fileData"][i]["rootName"] == "main_pattern"
                        || JsVar["fileData"][i]["rootName"] == "route_pattern"
                        || JsVar["fileData"][i]["rootName"] == "instance_pattern") && JsVar["fileData"][i]["fileType"] == "F") {
                        JsVar["fileData"].splice(i, 1);
                        i--;
                    }
                }
            }
        }, null, null, false);
}
