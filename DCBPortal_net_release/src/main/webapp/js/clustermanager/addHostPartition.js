/**
 * Created with IntelliJ IDEA.
 * Creater: zhongsixue
 * Date: 16-7-19
 * Time: 下午16:17
 * To change this template use File | Settings | File Templates.
 */

/**
 * 定义变量， 通常是页面控件和参数
 */
var param=new Object();

/**
 * 跳转到该页面设值
 * @param action
 * @param data
 */
function onLoadComplete(action,data) {

	//集群ID
	param["CLUSTER_ID"] = data["CLUSTER_ID"];
	//集群类型
	param["CLUSTER_TYPE"] = data["CLUSTER_TYPE"];
	//集群编码
	param["CLUSTER_CODE"] = data["CLUSTER_CODE"];
	//是否运行在Jstorm中
	param["JSTORM_RUN"] = data["JSTORM_RUN"];
	
	//运行在Jstorm中的组件
	if(param["JSTORM_RUN"] == busVar.STATE_ACTIVE){
		//业务主集群ID
		param["BUS_CLUSTER_ID"] = data["BUS_CLUSTER_ID"];
		
		$("#hostNameDiv").remove();
		
		//根据Jstorm集群获取主机列表
		queryHostDiv("host.queryHostInfoForAddBillingAndRent");
	}else{
		loadHostList();
	}
	//加载右键
    loadingRightClick();
    //若无主机,则不显示全选页面
	chooseSelectAll();
}



/**
 * 用户选择事件
 * @param e
 */
function sshUserChange(e){
	queryHostDiv("deployHome.queryHostNotInThisDeploy");//加载div
}

/**
 * 加载用户
 */
function loadHostList(){
	var ssh_user = mini.get("SSH_USER");
	var params ={
		CLUSTER_ID:param["CLUSTER_ID"]
	};
	getJsonDataByPost(Globals.baseActionUrl.FRAME_QUERY_FOR_LIST_URL, params, "部署图-查询该部署分类下的所有用户",
	    function(result){
	        var list = result;
			if (list != null && list.length > 0) {
				ssh_user.setData(list);
			} else{
				comboxLoad(ssh_user, params, "deployHome.queryShellUser", null, null, false);
			}
			list = ssh_user.getData();
			if (list != null && list.length > 0) {
				mini.get("SSH_USER").select(0);
			}
	    }, "deployHome.queryShellUserDeploy", null, false);
}

/**
 * 获取所有的Jstorm集群列表
 */
function loadJstormList() {
	var params = {
		BUS_CLUSTER_ID:param["BUS_CLUSTER_ID"],
		CLUSTER_TYPE: busVar.JSTORM
	};
	comboxLoad(mini.get("JSTORM_CLUSTER_ID"), params, "serviceType.queryJstormClusterList", null, null, false);
	var jstormList = mini.get("JSTORM_CLUSTER_ID").getData();
	if (jstormList != null && jstormList.length > 0) {
		mini.get("JSTORM_CLUSTER_ID").select(0);
	}
}

/**
 * 加载Jstorm集群列表
 * @param e
 */
function loadJstormClusterList(e){
	queryHostDiv("host.queryHostInfoForAddBillingAndRent");
}

/**
 * 查询所哟
 * @param execKey
 */
function queryHostDiv(execKey){
	//var jstormClusterID = mini.get("JSTORM_CLUSTER_ID").getValue();
	var params = {
		CLUSTER_ID:param["CLUSTER_ID"],
		CLUSTER_TYPE:param["CLUSTER_TYPE"],
		//JSTORM_CLUSTER_ID:jstormClusterID,
		SSH_USER:mini.get("SSH_USER").getValue(),
		BUS_CLUSTER_ID:param["BUS_CLUSTER_ID"]
	};
	
	//每次执行本方法之前,都将div中的内容清楚,使代码重用性高
	$("#hostFitDiv").html("");
	//查询不是该类型的所有主机
	getJsonDataByPost(Globals.baseActionUrl.FRAME_QUERY_FOR_LIST_URL, params, "部署图-查询非该部署分类下的所有主机",
	    function(result){
			var str = "";
			$.each(result, function (i, item) {
				var tips = "主机名称：" + item.HOST_NAME
					   + "\n主机信息：" + item.HOST_IP + "(" + item.SSH_USER + ")";
	        	str+='<ul class="ul_host_common" title="'+tips+'" id="'+(new Date()).getTime()+'" data-HOST_ID="'+item.HOST_ID+'" style="background-color:#5cb85c;">'
	        		/*+'	<li style="height:60%;">'
	        		+'		<image src="'+Globals.ctx+'/images/deployHost/module_0.png" class="ul_host_img"/>'
	        		+'	</li>'*/
	        		+'	<li class="ul_host_li" style="height:20%;margin-top:6px;">'
	        		+'		<input type="checkbox" id="'+item.HOST_ID+'" data-HOST_IP="'+item.HOST_IP+'" name="ck_host"'
	        		+'			style="font-size:12px;" value="'+item.HOST_ID+'" />'
	        		+'		<label style="cursor:pointer;float:right;width:135px;text-align:left;margin-left:2px;display:block;white-space:nowrap; overflow:hidden; text-overflow:ellipsis;" '
        			+'			for="'+item.HOST_ID+'">' + item.HOST_IP + "(" + item.SSH_USER + ")</label>"
	        		+'	</li>'
	        		/*+'	<li class="ul_host_li" style="height:20%;text-align:center;">'
	        		+'	(' + item.SSH_USER + ')&nbsp;&nbsp;&nbsp;&nbsp;'
	        		+'	</li>'*/
	        		+'</ul>';
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
	    }, execKey, null, false);
	
	//当业务类型为billing时选择所有的主机
	if (param["RUN_JSTORM"] == "billing") {
		selectAll();
	}
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
 * 选择划分主机
 */
function chooseDeploy(){
	var obj = document.getElementsByName('ck_host');
	var host_arry = new Array();
	//取到对象数组后，循环检测它是不是被选中
	for(var i=0; i<obj.length; i++){
		if(obj[i].checked){
			var hostInfo = {
				HOST_ID:obj[i].value,
				CLUSTER_TYPE:param["CLUSTER_TYPE"],
				CLUSTER_ID:param["CLUSTER_ID"],
				NAME:$(obj[i]).data("HOST_IP") + "_" + param["CLUSTER_TYPE"]
			};
			//将对象放入数组
			host_arry.push(hostInfo);
		};
	}
	
	if(host_arry.length>0){
		//运行在Jstorm中的业务程序，添加主机划分信息(现在其实和其他非运行Jstorm逻辑一样)
		if(param["JSTORM_RUN"] == busVar.STATE_ACTIVE){
			//将选中的主机添加进数据库
			var params = {
				REF_CLUSTER_ID:mini.get("JSTORM_CLUSTER_ID").getValue(),
				REF_CLUSTER_TYPE: busVar.JSTORM,
				CLUSTER_ID:param["CLUSTER_ID"],
				HOST_LIST:host_arry
			};
			getJsonDataByPost(Globals.baseActionUrl.DEPLOY_BUSINESS_HOST_URL, params, "集群划分-批量添加Jstorm主机",
				function(result){
					closeWindow(systemVar.SUCCESS);
                    showMessageTips("添加主机成功!");
			});
		}else{
			//将选中的主机添加进数据库
			getJsonDataByPost(Globals.baseActionUrl.FRAME_INSERT_OBJECT_URL, host_arry, "集群划分-批量添加主机",
				function(result){
					closeWindow(systemVar.SUCCESS);
                    showMessageTips("添加主机成功!");
			},"deployHome.insertChosenHost", null, false);
		};
	}else{
		//没有选中主机
        showWarnMessageTips("请选择主机！");
	};
}

//判断是否该显示全选按钮
function chooseSelectAll(){
	var array = document.getElementsByName("ck_host");
	if(array.length < 1){
		$("#selectLi").attr("style","visibility:hidden");
	}
}

/**
 * 选择所有主机
 */
function selectAll(){
	var array = document.getElementsByName("ck_host");
	for(var i=0;i<array.length;i++){
		array[i].checked = true;
	}
}

/**
 * 反选所有主机
 */
function selectNone(){
	var array = document.getElementsByName("ck_host");
	for(var i=0;i<array.length;i++){
		array[i].checked = false;
	}
}