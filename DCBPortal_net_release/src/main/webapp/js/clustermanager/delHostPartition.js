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
		
	//根据Jstorm集群获取主机列表
	queryHostDiv("deployHome.queryPartitionHostList");

	//加载右键
    loadingRightClick();
    //若无主机,则不显示全选页面
	chooseSelectAll();
}

/**
 * 查询所哟
 * @param execKey
 */
function queryHostDiv(execKey){
	var params = {
		CLUSTER_ID:param["CLUSTER_ID"],
		CLUSTER_TYPE:param["CLUSTER_TYPE"],
		BUS_CLUSTER_ID:param["BUS_CLUSTER_ID"]
	};
	
	//每次执行本方法之前,都将div中的内容清楚,使代码重用性高
	$("#hostFitDiv").html("");
	//查询不是该类型的所有主机
	getJsonDataByPost(Globals.baseActionUrl.FRAME_QUERY_FOR_LIST_URL, params, "部署图-查询非该部署分类下的所有主机",
	    function(result){
			var str = "";
			$.each(result, function (i, item) {
				var tips = "主&nbsp;&nbsp;机：" + item.HOST_IP + "(" + item.SSH_USER + ")";
	        	str+='<ul class="ul_host_common" title="'+tips+'" id="'+(new Date()).getTime()+'" data-HOST_ID="'+item.HOST_ID+'" style="background-color:#5cb85c;">'
	        		+'	<li class="ul_host_li" style="height:20%;margin-top:6px;">'
	        		+'		<input type="checkbox" id="'+item.HOST_ID+'" data-HOST_IP="'+item.HOST_IP+'" name="ck_host"'
	        		+'			style="font-size:12px;" value="'+item.HOST_ID+'" />'
	        		+'		<label style="cursor:pointer;float:right;width:135px;text-align:left;margin-left:2px;display:block;white-space:nowrap; overflow:hidden; text-overflow:ellipsis;" '
        			+'			for="'+item.HOST_ID+'">' + item.HOST_IP + "(" + item.SSH_USER + ")</label>"
	        		+'	</li>'
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
	
	//默认选中所有主机
	selectAll();
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
				HOST_IP:$(obj[i]).data("HOST_IP"),
				NAME:$(obj[i]).data("HOST_IP") + "_" + param["CLUSTER_TYPE"]
			};
			//将对象放入数组
			host_arry.push(hostInfo);
		};
	}
	
	if(host_arry.length>0){
		//将选中的主机添加进数据库
		var params = {
			CLUSTER_ID:param["CLUSTER_ID"],
			HOST_LIST:host_arry
		};
		showConfirmMessageAlter("是否确定将选中主机从集群中移除？", function ok(){
			getJsonDataByPost(Globals.baseActionUrl.DEPLOY_DEL_BATCH_HOST_URL, params, "集群划分-批量删除主机列表",
				function(result){
					closeWindow(systemVar.SUCCESS);
                    showMessageTips("批量移除主机成功!");
			});
		});
	}else{
		//没有选中主机
        showWarnMessageTips("请选择要删除的主机！");
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