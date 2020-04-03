//定义变量， 通常是页面控件和参数
	var JsVar = new Object();
	//初使化
	$(document).ready(function () {
	    mini.parse();
	    initDevelopPage();
	    $(window).resize(resizePage);
		
	});

/**
 * 页面尺寸变化时重新渲染页面
 */
function resizePage() {
	var height=document.documentElement.clientHeight;
	JsVar["editor"].setSize("100%", height-10);

};
	
/**
 * 初始化二次开发页面
 */	
function initDevelopPage(){
	     var height=document.documentElement.clientHeight;
	     CodeMirror.commands.senCommand = function (cm) {
	    	  shell();
	    	};
	     JsVar["editor"] = CodeMirror.fromTextArea(document.getElementById("code"), {
	        lineNumbers: true,
			lineWrapping: true,
	        extraKeys: {"Enter": "senCommand"},
	        styleActiveLine: true,
//	        theme:"3024-night",
	        mode: {name: "javascript", globalVars: true},
            gutters: ["CodeMirror-linenumbers", "CodeMirror-foldgutter"]
	      });
	     JsVar["editor"].foldCode(CodeMirror.Pos(0, 0));
	     
	     JsVar["editor"].on("changes", function (Editor, changes) {
	    	 JsVar["isEditting"]=true;
	    	});
	     
	     JsVar["editor"].setSize("100%", height-10);
}

function shell(){
	
	var params = new Object();
	params["host"]=host;
	params["port"]=port;
	params["name"]=name;
	params["password"]=password;
	params["encoding"]=encoding;
	params["shellCommand"]="\r";
	JsVar["editor"].save();
	var lastLineNum=JsVar["editor"].lastLine();
	var lineStr=getLastLineStr();
	params["shellCommand"]=lineStr["text"];
		
	
//	alert(params["shellCommand"]);
	getJsonDataByPost(Globals.baseActionUrl.SHELL_ACTION_SEND_SHELL_URL, params,"",
            function(result){
		var before=JsVar["editor"].getValue();
		JsVar["editor"].setValue(before+result["result"].substring(params["shellCommand"].length+1));
		var lineStr=getLastLineStr();
		JsVar["editor"].setCursor(lineStr["number"],lineStr["text"].length);
//		JsVar["editor"].goDocEnd();
//		JsVar["editor"].apend(result["result"]);
            });
}


function getLastLineStr(){
	var lineStr={text:"",number:1};
	var lastLineNum=JsVar["editor"].lastLine();
	for(var i=lastLineNum;i>0;i--){
		var tmp=JsVar["editor"].getLine(i);
		var index = tmp.indexOf(">");
		if(index<0){
			index = tmp.indexOf("#");
		}
		if(index >-1) {
			lineStr["number"]=i;
			lineStr["text"]=tmp.substring(index+1);
			break;
		}
	
		
	}
	return lineStr;
}