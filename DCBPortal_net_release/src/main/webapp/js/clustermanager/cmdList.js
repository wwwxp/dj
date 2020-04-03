//父页面调用，新增和修改时会初使化一些参数，例如：data.action：操作类型
function onLoadComplete(data) {
	mini.parse();
	comboxLoad(mini.get('#CMD_DESC'), {}, "jobtaskcfg.queryCmdList");
	//任务类型Change事件
    mini.get("CMD_DESC").on("valuechanged", function(e) {
    	onExecTypeChanage(e);
    });
}
 
function onExecTypeChanage(e) {
	 var cmdMsg = e.value;
	 mini.get('#cmdMsg').setValue(cmdMsg);
}
function onSubmit(){
	var cmdMsg = mini.get('#cmdMsg').getValue();
	if(isNull(cmdMsg)){
		showWarnMessageAlter("请选择要执行的命令");
		return;
	}
	closeWindow({flag:systemVar.SUCCESS,cmdMsg:cmdMsg});
}

 