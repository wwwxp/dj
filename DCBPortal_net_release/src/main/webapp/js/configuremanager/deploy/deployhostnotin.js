
/**
 * 定义变量， 通常是页面控件和参数
 */
var param=new Object();
//部署文件路径变量
var info = new Object();
var sumbitButton;
var paramData = new Object();

/**
 * 跳转到该页面设值
 * @param data
 */
function onLoadComplete(data) {
    paramData = data;
	//集群ID
	param["CLUSTER_ID"] = data["CLUSTER_ID"];
	//集群编码
	param["CLUSTER_CODE"] = data["CLUSTER_CODE"];
	//集群类型
	param["CLUSTER_TYPE"] = data["CLUSTER_TYPE"];
	//主机ID，当选择单个主机部署的时候该参数不为空
	param["HOST_ID"] = data["HOST_ID"];
	//按钮对象
	sumbitButton = mini.get("sumbitButton");
	//加载右键
    loadingRightClick();
	//初始化可部署主机列表
	queryHostDiv();
}

//初使化主机列表的div
function queryHostDiv(){
	//每次执行本方法之前,都将div中的内容清楚,使代码重用性高
	$("#hostFitDiv").html("");
	//查询该类型所有主机
	getJsonDataByPost(Globals.baseActionUrl.FRAME_QUERY_FOR_LIST_URL,param,"部署管理-初始化部署主机列表",
	    function(result){
		    var str="";
			$.each(result, function (i, item) {
				var tips = "主机名称：" + item.HOST_NAME
						+ "\n主机信息：" + item.HOST_IP + "(" + item.SSH_USER + ")"
						+ "\n最近部署版本: " + (item.VERSION == undefined ? "未部署" : item.VERSION)
				        + "\n最近部署时间: " + (isNull(item.DEPLOY_DATE) ? "" : item.DEPLOY_DATE);
				if (item.STATE == busVar.STATE_ACTIVE) {
					str+='<ul class="ul_host_common" title="' + tips + '" id="'+(new Date()).getTime()+'" data-HOST_ID="'+item.HOST_ID+'" style="background-color:#5cb85c;">'
	        		/*+'	<li style="height:60%;">'
	        		+'		<image src="'+Globals.ctx+'/images/deployHost/module_'+item.STATE+'.png" class="ul_host_img"/>'
	        		+'	</li>'*/
	        		+'	<li style="height:20%;margin-top:6px;">'
	        		+'		<input type="checkbox" data-ID="'+item.ID+'" data-service="'+item.HOST_IP + '(' +  item.SSH_USER+ ')'+'" id="'+item.HOST_ID+'" name="ck_host" checked=checked onClick="checkedchanged()" '
	        		+'			style="font-size:12px;" value="'+item.HOST_ID+'_'+item.STATE+'" />'
	        		+'		<label style="cursor:pointer;float:right;width:135px;text-align:left;margin-left:2px;display:block;white-space:nowrap; overflow:hidden; text-overflow:ellipsis;" '
        			+'			for="'+item.HOST_ID+'">' + item.HOST_IP + "(" + item.SSH_USER + ")</label>"
	        		+'	</li>'
	        		+'</ul>';
				} else {
					str+='<ul class="ul_host_common" title="' + tips + '" id="'+(new Date()).getTime()+'" data-HOST_ID="'+item.HOST_ID+'" style="background-color:gray;">'
	        		/*+'	<li style="height:60%;">'
	        		+'		<image src="'+Globals.ctx+'/images/deployHost/module_'+item.STATE+'.png" class="ul_host_img"/>'
	        		+'	</li>'*/
	        		+'	<li style="height:20%;margin-top:6px;">'
	        		+'		<input type="checkbox" data-ID="'+item.ID+'" data-service="'+item.HOST_IP + '(' +  item.SSH_USER+ ')'+'" id="'+item.HOST_ID+'" name="ck_host" checked=checked onClick="checkedchanged()" '
	        		+'			style="font-size:12px;" value="'+item.HOST_ID+'_'+item.STATE+'" />'
	        		+'		<label style="cursor:pointer;float:right;width:135px;text-align:left;margin-left:2px;display:block;white-space:nowrap; overflow:hidden; text-overflow:ellipsis;" '
        			+'			for="'+item.HOST_ID+'">' + item.HOST_IP + "(" + item.SSH_USER + ")</label>"
	        		+'	</li>'
	        		+'</ul>';
				}
	        	
	        });
			$("#hostFitDiv").append(str);
			//主机上面添加右键功能
			$("#hostFitDiv .ul_host_common").hover(function(e) {
				var id = $(this).attr("id");
				var hostId = $(this).data("HOST_ID");
				//给类加上菜单
				var array = new Array();
				array.push({header: '右击菜单'});
				array.push({text: '主机详情', action: function(e){
					e.preventDefault();
					scanHostInfo(hostId);
				}});
				array.push({text: '终端操作', action: function(e){
					e.preventDefault();
					operatorTerminal(hostId);
				}});
				context.attach("#"+id, array);
			}, function(e) {
				mini.parse();
			});
			checkedchanged();
	    },"deployHome.queryDeployHostList");
}

/**
 * 加载右键菜单 
 */
function loadingRightClick(){
	context.init({preventDoubleContext: false});
	context.settings({compress: true});
}

/**
 * 查看分类中的主机详情
 */
function scanHostInfo(hostId){
	var params = {
		HOST_ID:hostId
	};
	getJsonDataByPost(Globals.baseActionUrl.FRAME_QUERY_FOR_LIST_URL, params, "主机管理-查询详细信息",
			   function (result){
		param = result[0];
		showDialog("详细信息",600,400,Globals.baseJspUrl.HOST_JSP_DETAIL_URL,
				function destroy(data){
			
		 },param);
	   },"host.queryHostList");
}

/**
 * 终端操作
 */
function operatorTerminal(hostId) {
	//获取主机ID
	var hostStr = "'" + hostId + "'";
	$("#termialHost").val(hostStr);
	$("#termialForm").attr("action", Globals.baseActionUrl.HOST_ACTION_TERMINAL_URL);
	$("#termialForm").submit();
}

/**
 * 选择主机
 */
function checkedchanged() {
	var obj = $("input[name='ck_host']");
	$("#paramsInfo").html("");
	var str="";
	//被选中的个数
	//取到对象数组后，我们来循环检测它是不是被选中
	for(var i=0; i<obj.length; i++){
		if($(obj[i]).attr("checked")){
			var hostIpUser = $(obj[i]).data("service");
			var hostId = $(obj[i]).attr("id");
			//var hostId = $(obj[i]).attr("id").split[0];
			//var hostIp = $(obj[i]).attr("id").split[1];
			str+='<tr style="padding-top:10px;" id="tr_'+hostId+'">'
				+'<th><span>主机：</span></th>'
				+'<td>'
				+'	<input rowType="ip" class="mini-textbox" allowInput="false" '
				+'		id="ip_' + i + '" name="ip_' + i + '" value="'+hostIpUser+'" style="width:100%;"> '
				+'</td>'
				
				+'<th><span class="fred">*</span>部署版本：</th>'
				+'<td>'
				+'	<input class="mini-combobox" id="version_' + i + '" name="version_'+i+'" '
				+' 		textField="VERSION" valueField="VERSION" required="true" '
				+' 		showNullItem="false" style="width:100%;"/>'
				+'</td>'
				+'</tr>';
		}
	}
	$("#paramsInfo").append(str);
	mini.parse();
	
	var versionData = getVersionData();
	for(var i=0; i<obj.length; i++){
		if($(obj[i]).attr("checked")){
			mini.get("version_" + i).setData(versionData);
			//默认选中最新版本
			if (versionData != null && versionData.length) {
				mini.get("version_" + i).setValue(versionData[0]["VERSION"]);
			}
		}
	}
}

/**
 * 根据主机ID获取版本信息
 * 根据主机ID和组件类型查询当前主机可部署的版本列表
 * @param hostId
 */
function getVersionData() {
	var versionData = [];
	var params = {
		CLUSTER_TYPE:param["CLUSTER_TYPE"]    //集群类型
	};
	getJsonDataByPost(Globals.baseActionUrl.FRAME_QUERY_FOR_LIST_URL,param,"部署管理-查询当前组件可部署版本",
			function(result){
				if(result != null){
					versionData = result;
				}
	},"deployHome.queryDeployVersionById", null, false);
	return versionData;
}

/**
 * 获取部署文件路径并存放在全局变量中
 */
function getDeployPath(){
	getJsonDataByPost(Globals.baseActionUrl.FRAME_QUERY_FOR_OBJECT_URL,param,"",
			function(result){
				if(result != null){
					info["TARGET_SH_PATH"] = result["TARGET_SH_PATH"];
					info["SOURCE_SH_FILE"] = result["SOURCE_SH_FILE"];
				}
	},"serviceType.queryServiceTypeInfo", null, false);
}

/**
 * 部署版本
 */
function chooseDeploy() {
	var obj = $("input[name='ck_host']");
	
	//获取部署文件存放路径
	//getDeployPath();
	
	//获取所有选中的主机
	var hostArray = [];
	var isDeploy = false;
	for (var i=0; i<obj.length; i++) {
		if($(obj[i]).attr("checked")){
			var hostIpUser = $(obj[i]).data("service");
			var hostId = $(obj[i]).attr("id");
			var id = $(obj[i]).data("ID");
			
			var hostInfo = {
				ID:id,
				HOST_ID:hostId,
				HOST_IP_USER:hostIpUser,
				CLUSTER_ID:param["CLUSTER_ID"],
				CLUSTER_CODE:param["CLUSTER_CODE"],
				CLUSTER_TYPE:param["CLUSTER_TYPE"],
				VERSION:mini.get("version_"+i).getValue(),
				STATE:$(obj[i]).val().split("_")[1]
			};
			if (hostInfo.STATE == "1") {
				isDeploy = true;
			}
			hostArray.push(hostInfo);
		}
	}
	
	//判断当前主机是否已部署版本
	if (hostArray != null && hostArray.length > 0) {
		for (var i=0; i<hostArray.length; i++) {
			if (hostArray[i]["VERSION"] == null || hostArray[i]["VERSION"] == "" || hostArray[i]["VERSION"] == undefined) {
                showWarnMessageTips("请选择部署版本！");
				return;
			}
		}
		if (isDeploy) {
			showConfirmMessageAlter("选中已部署主机，确定覆盖部署环境吗？",function ok(){
				sumbit(hostArray);
			});
		} else {
			showConfirmMessageAlter("确定部署环境吗？",function ok(){
				sumbit(hostArray);
			});
		}
	} else {
        showWarnMessageTips("请选择需要部署的主机！");
	}
}

/**
 * 部署主机
 */
var index=0;
var textValue="";
var startTimes = 0;
function sumbit(hostArray){
	index = 0;
	textValue='';
	sumbitButton.setText("正在部署");
	sumbitButton.setEnabled(false);
	startTimes = (new Date()).getTime();
	textValue += "部署开始时间: " + ((new Date(startTimes)).format("yyyy-MM-dd hh:mm:ss")) + "<br/>";
	postAjax(hostArray);
	
}

/**
 * 主机部署， 一次部署一台主机
 * @param host_arry
 */
function postAjax(host_arry){
	var obj = host_arry[index];
	textValue +="主机【"+obj["HOST_IP_USER"]+"】，正在部署...<br/>";
	$("#deployTextarea").html(textValue);
	
	//高亮检索“失败”“成功”关键字
	heightLightKeyWord();
	
	//将滚动条自动滚动到最下面
	var deployDiv = document.getElementById("mainDiv");
	deployDiv.scrollTop = Math.max(0, deployDiv.scrollHeight - deployDiv.offsetHeight);
	
	getJsonDataByPost(Globals.baseActionUrl.CLUSTER_ACTION_DEPLOY_URL,[obj],"部署管理-基本类和框架类主机部署",
			function(result){
				index++;
				textValue=$("#deployTextarea").html()+result.success;
				textValue=textValue.replaceAll("\n","<br/>");
	        	
		        if(host_arry.length==index){
		        	var lowerValue = textValue.toLocaleLowerCase();
		        	var endTimes = (new Date()).getTime();
		        	textValue += "部署结束时间: " + ((new Date(endTimes)).format("yyyy-MM-dd hh:mm:ss")) + ", 本次部署"+index+"台主机, 总耗时: " + Math.floor((endTimes - startTimes)/1000) + "秒";
		        	$("#deployTextarea").html(textValue);
		        	
		        	//将滚动条自动滚动到最下面
		        	deployDiv.scrollTop = Math.max(0, deployDiv.scrollHeight - deployDiv.offsetHeight);
		        	
		        	//高亮检索“失败”“成功”关键字
		        	heightLightKeyWord();
		        	
		        	queryHostDiv();
					
		        	sumbitButton.setText("部署");
					sumbitButton.setEnabled(true);
					if(lowerValue.indexOf("error") >-1 || lowerValue.indexOf("failed")>-1) {
		        		showErrorMessageAlter("部署失败");
		        	}else{
                        showMessageTips("部署成功");
		        	}
		        }else{
		        	postAjax(host_arry);
		        }
		    },null,null,true,null,false);
}

/**
* 高亮检索关键字
*/
function heightLightKeyWord(){
	$("#deployTextarea").textSearch("success,successful",{textReduction:false,divStr:",",markCss: "font-weight:bold;color:#429C39;letter-spacing:.1em;",nullReport:false,caseIgnore:true});
	$("#deployTextarea").textSearch("error,fail,failed",{textReduction:false,divStr:",",markCss: "font-weight:bold;color:#CF5130;letter-spacing:.1em;",nullReport:false,caseIgnore:true});
}

/**
 * 根据state判断是否部署过主机,如果部署过,关闭窗口后要刷新
 */
function close(){
    closeWindow(paramData);
}

/**
 * 选中所有主机
 */
function selectAll(){
	var array = document.getElementsByName("ck_host");
	for(var i=0;i<array.length;i++){
		if(!($(array[i]).attr("sum_run"))){
			array[i].checked = true;
		}
	}
	checkedchanged();
}

/**
 * 取消选中所有主机
 */
function selectNone(){
	var array = document.getElementsByName("ck_host");
	for(var i=0;i<array.length;i++){
		array[i].checked = false;
	}
	checkedchanged();
}

/**
 * 点击事件:查看部署（运行/停止）详情
 */
function processScanEvent(){
	var html_paragraph=$("#deployTextarea").html();
	if(html_paragraph==""){
        showWarnMessageTips("当前不存在部署进度详情，请部署后再进行此操作！");
		return;
	}
	showDialog("查看部署日志",700,450,Globals.baseJspUrl.HOST_JSP_PROCESS_SCAN_LOG_CONTENT_URL,
			function destroy(data){
			
		}, {"textValue":html_paragraph});
}