//定义变量， 通常是页面控件和参数
//var TreeObj = new Object();
var page_type="PLATFORM";

var JsVar = new Object();

/**
 * 初始化
 */
$(document).ready(function () {
    mini.parse();
});

/**
 * 跳转到该页面设值
 * @param data
 */
function onLoadComplete(data) {
	JsVar["DATA"] = data;
	//查询当前配置文件信息
//	var params = {
//		INST_ID:JsVar["DATA"]["INST_ID"],
//		HOST_ID:JsVar["DATA"]["HOST_ID"]
//	};
//	getJsonDataByPost(Globals.baseActionUrl.FRAME_QUERY_FOR_LIST_URL,params,"启停管理-查看配置文件",
//			function(result){
//			if(result != null && result.length > 0){
//				var content = result[0]["FILE_CONTENT"];
//				initFileType();
//				initTextContent();
//				JsVar["editor"].setValue(content);	
//			}
//	}, "instConfig.queryInstConfigById");
	initFileType();
	initTextContent();
	getJsonDataByPost(Globals.baseActionUrl.CONFIGURE_ACTION_SHOW_CONFIG_FILE_CONTENT_URL,data,"配置修改-FTP/SFTP获取文件内容",
			function(result){
		if(result!=null){
			var con=result.fileContent;
			JsVar["editor"].setValue(con);	
		}
	});
}

/**
 * 获取文件类型
 */
function initFileType() {
	if(JsVar["DATA"]["filePath"].lastIndexOf(".xml") > -1){
		if(JsVar["formatType"] != "xml"){
			JsVar["formatType"]="xml";
		}
	}else if(JsVar["DATA"]["filePath"].lastIndexOf(".yaml") > -1){
		if(JsVar["formatType"] != "yaml"){
			JsVar["formatType"]="yaml";
		}
	}else if(JsVar["DATA"]["filePath"].lastIndexOf(".properties") > -1){
		if(JsVar["formatType"] != "properties"){
			JsVar["formatType"]="properties";
		}
	}else {
		if(JsVar["DATA"]["filePath"] != "defaul" ){
			JsVar["formatType"]="defaul";
		}
	}
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
        JsVar["editor"].foldCode(CodeMirror.Pos(0, 0));
	}else if(JsVar["formatType"] == "yaml"){
		JsVar["editor"] = CodeMirror.fromTextArea(document.getElementById("content"), {
			lineNumbers: true,
            lineWrapping: true,
			extraKeys: {"Ctrl": "autocomplete","Ctrl-Q": function(cm){ cm.foldCode(cm.getCursor()); }},
			styleActiveLine: true,
			mode:  {name: "yaml", globalVars: true},
			gutters: ["CodeMirror-linenumbers", "CodeMirror-foldgutter"]
		});
        JsVar["editor"].foldCode(CodeMirror.Pos(0, 0));
	}else if(JsVar["formatType"] == "properties"){
		JsVar["editor"] = CodeMirror.fromTextArea(document.getElementById("content"), {
			lineNumbers: true,
            lineWrapping: true,
			extraKeys: {"Ctrl": "autocomplete","Ctrl-Q": function(cm){ cm.foldCode(cm.getCursor()); }},
			styleActiveLine: true,
			mode:  {name: "properties", globalVars: true},
			gutters: ["CodeMirror-linenumbers", "CodeMirror-foldgutter"]
		});
        JsVar["editor"].foldCode(CodeMirror.Pos(0, 0));
	}else{
		JsVar["editor"] = CodeMirror.fromTextArea(document.getElementById("content"), {
			lineNumbers: true,
            lineWrapping: true,
			extraKeys: {"Ctrl": "autocomplete","Ctrl-Q": function(cm){ cm.foldCode(cm.getCursor()); }},
			styleActiveLine: true,
			mode:  {name: "javascript", globalVars: true},
			gutters: ["CodeMirror-linenumbers", "CodeMirror-foldgutter"]
		});
        JsVar["editor"].foldCode(CodeMirror.Pos(0, 0));
	}
	JsVar["editor"].on("changes", function (Editor, changes) {
		JsVar["isEditting"]=true;
		$("#saveFile").css("display", "block");
	});

	var height=document.documentElement.clientHeight;
	JsVar["editor"].setSize("100%", height-50);
}
