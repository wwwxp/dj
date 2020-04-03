//定义变量， 通常是页面控件和参数
var JsVar = new Object();
var hostIP;
var sshUser;
$(document).ready(function () {
    mini.parse();
    //获取页面表单
    JsVar["hostFrom"] = new mini.Form("#hostFrom");
    JsVar["SSH_PASSWD"] = mini.get("SSH_PASSWD");
    JsVar["SSH＿CHECK_PASSWD"] = mini.get("SSH＿CHECK_PASSWD");
    hostIP = mini.get("HOST_IP");
    sshUser = mini.get("SSH_USER");
});


//父页面调用，新增和修改时会初使化一些参数，例如：data.action：操作类型
function onLoadComplete(action,data) {
    JsVar[systemVar.ACTION] = action;
    if (action == systemVar.EDIT) {
    	 // 初使化表单数据
    	hostIP.setEnabled(false);
    	sshUser.setEnabled(false);
        initData(data);
    }
}

//新增和修改点确认按钮保存
function onSubmit() {
    if (JsVar[systemVar.ACTION] == systemVar.EDIT) {
        update();
    } else {
        save();
    }
}

//点确认新增主机
function save() {
	if(JsVar["SSH_PASSWD"].getValue() != JsVar["SSH＿CHECK_PASSWD"].getValue()){
		showWarnMessageAlter("输入的密码不一致，请重新输入",function(){
			JsVar["SSH_PASSWD"].setValue('');
			JsVar["SSH＿CHECK_PASSWD"].setValue('');
		});
		return;
	}
	
    //判断是否有效
    JsVar["hostFrom"].validate();
    if (JsVar["hostFrom"].isValid() == false){
        return;
    }
    //新增操作下获取表单的数据 
    var hostInfo = JsVar["hostFrom"].getData();
    getJsonDataByPost(Globals.baseActionUrl.HOST_ACTION_ADD_URL,hostInfo,"主机管理-新增主机",
        function success(result){
            toastr.success("添加成功");
            closeWindow(systemVar.SUCCESS);
        });
}

//修改主机
function update() {
	if(JsVar["SSH_PASSWD"].getValue() != JsVar["SSH＿CHECK_PASSWD"].getValue()){
		showWarnMessageAlter("输入的密码不一致，请重新输入",function(){
			JsVar["SSH_PASSWD"].setValue('');
			JsVar["SSH＿CHECK_PASSWD"].setValue('');
		});
		return;
	}
	
    //判断是否有效
    JsVar["hostFrom"].validate();
    if (JsVar["hostFrom"].isValid() == false){
        return;
    }
    //修改操作下获取表单的数据
    var taskInfo = JsVar["hostFrom"].getData();
    taskInfo["HOST_ID"] = JsVar["HOST_ID"];
    getJsonDataByPost(Globals.baseActionUrl.HOST_ACTION_EDIT_URL,taskInfo,"主机管理-修改主机",
        function success(result){
            toastr.success("修改成功");
            closeWindow();
        });
}

//编辑初始化数据
function initData(data) {
	var param = new Object();
	param["HOST_ID"] = data["HOST_ID"];
	getJsonDataByPost(Globals.baseActionUrl.HOST_ACTION_QUERY_INFO_URL,param,"",
        function success(result){
			JsVar["HOST_ID"] = data["HOST_ID"];
			JsVar["hostFrom"].setData(data);
			JsVar["SSH＿CHECK_PASSWD"].setValue(result["SSH_PASSWD"]);
			JsVar["SSH_PASSWD"].setValue(result["SSH_PASSWD"]);
        }
	);
}
