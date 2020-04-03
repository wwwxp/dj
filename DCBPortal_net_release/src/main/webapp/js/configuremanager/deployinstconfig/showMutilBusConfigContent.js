//定义变量， 通常是页面控件和参数

var JsVar = new Object();

/**
 * 跳转到该页面设值
 * @param data
 */
function onLoadComplete(data) {
	mini.parse();
    JsVar["DATA"] = data;
    JsVar["configFileCombo"] = mini.get("copyFilesNames");
    //初始化文本域
    initTextContent();
    initCombox();
    //loadConfigFile();
    initFileType();
}

/**
 * 加载配置文件
 * @param params
 */
function loadConfigFile() {
	JsVar["DATA"]["CONFIG_FILE"] = JsVar["configFileCombo"].getValue();
    getJsonDataByPost(Globals.baseActionUrl.BUS_PROGRAM_FILE_CONTENT_ACTION_MANAGE_URL, JsVar["DATA"], "业务状态管理-获取业务程序的配置文件",
        function success(result) {
            if(result!=null){
                var con=result.fileContent;
                JsVar["editor"].setValue(con);
            }
        });
}

/**
 * combo值变化触发
 * @param e
 */
function loadContent(e){
	var fileName = e.selected.fileName;
	JsVar["DATA"]["CONFIG_FILE"] = fileName;
	getJsonDataByPost(Globals.baseActionUrl.BUS_PROGRAM_FILE_CONTENT_ACTION_MANAGE_URL,JsVar["DATA"],"业务状态管理-获取业务程序的配置文件",
		function(result){
			if(result!=null){
				var con=result.fileContent;
				JsVar["editor"].setValue(con);	
			}
	});
	
}

/**
 * 构造下拉框数据
 * @param obj
 */
function initCombox(){
    var fileNames = JsVar["DATA"]["CONFIG_FILE"].split(",");
    var fileArray =[];
    for(var i = 0 ; i < fileNames.length; i++){
        var param = {"fileName":fileNames[i],"fileName":fileNames[i]};
        fileArray.push(param);
    }
    JsVar["configFileCombo"].setData(fileArray);
    JsVar["configFileCombo"].select(0);
}



/**
 * 获取文件类型
 */
function initFileType() {
    if(JsVar["DATA"]["CONFIG_FILE"].lastIndexOf(".xml") > -1){
        if(JsVar["formatType"] != "xml"){
            JsVar["formatType"]="xml";
        }
    }else if(JsVar["DATA"]["CONFIG_FILE"].lastIndexOf(".yaml") > -1){
        if(JsVar["formatType"] != "yaml"){
            JsVar["formatType"]="yaml";
        }
    }else if(JsVar["DATA"]["CONFIG_FILE"].lastIndexOf(".properties") > -1){
        if(JsVar["formatType"] != "properties"){
            JsVar["formatType"]="properties";
        }
    }else {
        if(JsVar["DATA"]["CONFIG_FILE"] != "defaul" ){
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
    }else if(JsVar["formatType"] == "yaml"){
        JsVar["editor"] = CodeMirror.fromTextArea(document.getElementById("content"), {
            lineNumbers: true,
            extraKeys: {"Ctrl": "autocomplete","Ctrl-Q": function(cm){ cm.foldCode(cm.getCursor()); }},
            styleActiveLine: true,
            lineWrapping: true,
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
    JsVar["editor"].on("changes", function (Editor, changes) {
        JsVar["isEditting"]=true;
        $("#saveFile").css("display", "block");
    });

    var height=document.documentElement.clientHeight;
    JsVar["editor"].setSize("100%", height-71);
}
