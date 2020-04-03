var JsVar = new Object();

//父页面调用，新增和修改时会初使化一些参数，例如：data.action：操作类型
function onLoadComplete(data) {
	//初始化编辑器
    initTextContent();
	//拿文件内容
    viewDetails(data);
    
    $(window).resize(resizePage);
}
/**
 * 点击详情，触发详情展示操作
 */
function viewDetails(data) {
	data = mini.clone(data);
	getJsonDataByPost(Globals.baseActionUrl.COMMON_TOPOLOGY_SCAN_CONFIG_FILE_TASK_ACTION_MANAGE_URL,data,null,
	    function(result){
		var txt=result.fileContent;
			if(isEmptyStr(txt)){
				JsVar["editor"].setValue("没有找到定义，请检查");
	        	return;			
			}
	 		JsVar["editor"].setValue(txt);
    });
}
  

/**
 * 初始化text
 */	
function initTextContent(){
	
	if(JsVar["editor"]){
		$("#content").parent().children(".CodeMirror").remove();
	}
	if(JsVar["formatType"] == "xml"){
		var mixedMode = {name: "htmlmixed"};
		JsVar["editor"] = CodeMirror.fromTextArea(document.getElementById("content"), {
			lineNumbers: true,
            lineWrapping: true,
			extraKeys: {"Ctrl": "autocomplete","Ctrl-Q": function(cm){ cm.foldCode(cm.getCursor()); }},
			styleActiveLine: true,
			mode: mixedMode,
			gutters: ["CodeMirror-linenumbers", "CodeMirror-foldgutter"]
		});
	}else if(JsVar["formatType"] == "yaml"){
		JsVar["editor"] = CodeMirror.fromTextArea(document.getElementById("content"), {
			lineNumbers: true,
            lineWrapping: true,
			extraKeys: {"Ctrl": "autocomplete","Ctrl-Q": function(cm){ cm.foldCode(cm.getCursor()); }},
			styleActiveLine: true,
			mode:  {name: "yaml", globalVars: true},
			gutters: ["CodeMirror-linenumbers", "CodeMirror-foldgutter"]
		});
	}else if(JsVar["formatType"] == "properties"){
		JsVar["editor"] = CodeMirror.fromTextArea(document.getElementById("content"), {
			lineNumbers: true,
            lineWrapping: true,
			extraKeys: {"Ctrl": "autocomplete","Ctrl-Q": function(cm){ cm.foldCode(cm.getCursor()); }},
			styleActiveLine: true,
			mode:  {name: "properties", globalVars: true},
			gutters: ["CodeMirror-linenumbers", "CodeMirror-foldgutter"]
		});
	}else{
		JsVar["editor"] = CodeMirror.fromTextArea(document.getElementById("content"), {
			lineNumbers: true,
            lineWrapping: true,
			extraKeys: {"Ctrl": "autocomplete","Ctrl-Q": function(cm){ cm.foldCode(cm.getCursor()); }},
			styleActiveLine: true,
			mode:  {name: "javascript", globalVars: true},
			gutters: ["CodeMirror-linenumbers", "CodeMirror-foldgutter"]
		});
	}
    JsVar["editor"].foldCode(CodeMirror.Pos(0, 0));
	var height=document.documentElement.clientHeight;
	JsVar["editor"].setSize("100%", height-41);
}

/**
 * 页面尺寸变化时重新渲染页面
 */
function resizePage() {
	var height=document.documentElement.clientHeight;
	JsVar["editor"].setSize("100%", height-41);
};
