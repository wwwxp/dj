//定义变量， 通常是页面控件和参数
var JsVar = new Object();
var  ipList = [];

//初使化
$(document).ready(function () {
    mini.parse();
    JsVar["fileForm"] = new mini.Form("fileForm");
});

/**
 * 获取主机IP下对应的端口
 * @param ip
 * @returns {*}
 */
function getPorts(ip){
    var ports = JsVar["portsMap"][ip];
    return ports;
}

/**
 * 初始化页面数据
 * @param data
 */
function initData(data){
    var ips = data["ipList"];
    JsVar["portsMap"] = data["portsMap"]; // 后台数据
    var params = JsVar["data"]["nodesData"]; // 主页面传过来的数据
    for(var j= 0;j<params.length;j++) {
        var ip = params[j]["ip"];
        var portList = params[j]["port"];
        var isRepeatPort = JsVar["portsMap"][ip]; //结果是一个数组
        if (isRepeatPort == undefined || isRepeatPort == null) {
            continue;
        }
        for(var i = 0;i<isRepeatPort.length;i++){
            var info = isRepeatPort[i];  //info = {text:"7333"}
            for(var k = 0;k<portList.length;k++){
                var p = portList[k];
                if(portList[k] == info["text"]){
                    isRepeatPort.remove(info);
                    i = i-1; //port每移除一个size就减少一个
                }
            }
        }
    }

    ipList = ips;
    var html ="";
    for(var i = 0 ;i < ips.length;i++){
        var ip = ips[i];
        var temPort = JsVar["portsMap"][ips[i]];
        if(temPort == undefined || temPort == null || temPort.length == 0){ //移除port为空的ip即整行空数据
            continue;
        }
        html +='<tr>'
        html +='<td style="width: 5%;text-align: center;">';
        html +=' <div id ="chkHost_'+ips[i]+'" name="chkHost" class="mini-checkbox" tooltip="勾选对应IP所有端口" onclick="selectAllHost(\'' + ips[i] + '\')"></div>';
        html +='</td>';
        html +='<td style="width: 30%;text-align: left;">';
        html +=' <div id ="file_host_ip_'+ips[i]+'" name="file_host_ip" class="mini-checkbox" onclick="changeStatus('+i+')" text="'+ips[i] +'"></div>';
        html +='</td>';
        html +='<td style="width: 65%;">';
        html +=' <div id="redisFileName_'+ips[i]+'" name="redisFileName" class="mini-checkboxlist" repeatLayout="flow" enabled="false" data="getPorts('+'\''+ips[i] +'\'' +')"/>';
        html +='</td>';
        html +='</tr>';
    }
    html += '<tr>';
    html += '<td colspan="2" id="copyFile" style="text-align:right;"><span class="fred" >*</span>复制文件：</td>';
    html += '<td>';
    html += '   <input width="100%" id="copyFilesNames" name="copyFilesNames"class="mini-combobox" required="true" multiSelect="true" popupHeight="140px"textField="fileName" valueField="fileName"/>';
    html += '</td>';
    html += '</tr>';

    $('#fileForm').append(html);
    mini.parse();
    //获取模板文件列表
    loadDefaultFile();
}

/**
 * 选中所有的主机以及端口
 * @param ip
 * @constructor
 */
function selectAllHost(ip) {
    var checkAllHostObj = mini.get("chkHost_" + ip);
    if(checkAllHostObj.checked) {
        mini.get("file_host_ip_"+ ip).setChecked(true);
        mini.get("redisFileName_" + ip).setEnabled(true);
        mini.get("redisFileName_" + ip).selectAll();
    } else {
        mini.get("file_host_ip_"+ ip).setChecked(false);
        mini.get("redisFileName_" + ip).setEnabled(false);
        mini.get("redisFileName_" + ip).deselectAll();
    }
}

/**
 * 触发checkbox的点击事件
 * @param n
 */
function changeStatus(n) {
    var ip = mini.get("file_host_ip_"+ ipList[n]);
    var ports =  mini.get("redisFileName_"+ ipList[n]);
    if(ip.checked) {
        if(ports.enabled == false){
            ports.setEnabled(true);
        }
    }else{
        if(ports.getSelecteds() != null && ports.getSelecteds().length>0){
            ip.setChecked(true);
        }else{
            ip.checked = false;
            ports.setEnabled(false);
        }
    }
}

/**
 * 跳转到该页面设值
 * @param action
 * @param data
 */
function onLoadComplete(action,data) {
    JsVar["data"] = data;
    //获取部署主机列表信息
    loadDeployHostId();
    JsVar["copyFiles"] = mini.get("copyFilesNames");
}

/**
 * 获取该分类已部署主机IP
 */
function loadDeployHostId(){
    var params = new Object();
    params["clusterCode"] = JsVar["data"]["CLUSTER_CODE"];
    getJsonDataByPost(Globals.baseActionUrl.CONFIGURE_ACTION_QUERY_ZK_REDIS_NODES_URL, params,"",
        function(result){
            if(result != null){
                initData(result);
            }
        },"",null,false);
}

/**
 * 获取default下的文件值
 */
function loadDefaultFile(){
    getJsonDataByPost(Globals.baseActionUrl.CONFIGURE_ACTION_LOAD_DEFAULT_FILE_URL, JsVar["data"],"配置修改-新建实例--获取可选文件",
        function(result){
            if(result!=null && result.length>0 ){
                //给可选文件下拉框注值
                JsVar["copyFiles"].setData(result);
            }
        });
}

/**
 * 支持批量添加redis文件
 */
function addBatchFile() {
    var addData = JsVar["fileForm"].getData();
    JsVar["fileForm"].validate();
    if (JsVar["fileForm"].isValid() == false){
        return;
    }
    if(JsVar["data"]["fileName"] == addData["fileName"]){
        showWarnMessageTips("实例名称不合法，请检查！");
        return;
    }

    var list = [];
    for (var i=0; i<ipList.length;i++) {
        var file_host_ip = mini.get("#file_host_ip"+"_"+ ipList[i]); //注意id的拼接
        if (file_host_ip == null || file_host_ip == undefined) {
            continue;
        }
        if(file_host_ip.checked){
            var hostIp = file_host_ip.getText();  //获取勾选的ip值
        }
        //获取选中的port集合
        var hostPortList = mini.get("redisFileName"+"_"+ ipList[i]).getSelecteds(); //注意id的拼接
        for (var j=0;j<hostPortList.length;j++) {
            var portMap = hostPortList[j];
            var port = portMap["text"];
            list.push({
                    "hostIp":hostIp,
                    "port":port,
                    "SEL_FILE_NAME":hostIp + "_" + port
                    });
        }
    }

    JsVar["data"]["copyFilesNames"] = addData["copyFilesNames"];
    JsVar["data"]["listData"] = list;
    if (list.length == 0) {
        showWarnMessageTips("至少要选中一个IP和对应端口！");
        return;
    }
    //JsVar["data"]["SEL_FILE_NAME"] = list[0]["SEL_FILE_NAME"];

    getJsonDataByPost(Globals.baseActionUrl.CONFIGURE_ACTION_BATCH_CREATE_COPY_REDIS_FILE_URL, JsVar["data"],"配置修改-新建文件夹",
        function(result){
            if (result != null && result["RST_MSG"] != null) {
                if (result["RST_CODE"] == '0') {
                    showWarnMessageAlter(result["RST_MSG"],function ok(){
                        closeWindow({flag:systemVar.SUCCESS, fileName:JsVar["data"]["SEL_FILE_NAME"]});
                    });
                } else if (result["RST_CODE"] == '1') {
                    showErrorMessageAlter(result["RST_MSG"],function ok(){
                        closeWindow({flag:systemVar.SUCCESS, fileName:JsVar["data"]["SEL_FILE_NAME"]});
                    });
                } else {
                    showMessageAlter(result["RST_MSG"],function ok(){
                        closeWindow({flag:systemVar.SUCCESS, fileName:JsVar["data"]["SEL_FILE_NAME"]});
                    });
                }
            } else {
                showMessageAlter("创建实例失败",function ok(){
                    closeWindow({flag:systemVar.SUCCESS, fileName:JsVar["data"]["SEL_FILE_NAME"]});
                });
            }
        });
}
