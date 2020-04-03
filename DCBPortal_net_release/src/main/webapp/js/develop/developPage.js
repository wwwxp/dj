//定义变量， 通常是页面控件和参数
	var JsVar = new Object();
	//初使化
	$(document).ready(function () {
	    mini.parse();
	    initDevelopPage();
	    $(window).resize(resizePage);
	    var regExp = new RegExp("^"+templatePath);
	    if(!regExp.test(path)){
	    	$("#saveFile").css("display", "block")
	    }
	    
	});

/**
 * 页面尺寸变化时重新渲染页面
 */
function resizePage() {
	var height=document.documentElement.clientHeight;
	JsVar["editor"].setSize("100%", height-40);

};
	
/**
 * 初始化二次开发页面
 */	
function initDevelopPage(){
	     var height=document.documentElement.clientHeight;
	     JsVar["editor"] = CodeMirror.fromTextArea(document.getElementById("code"), {
	        lineNumbers: true,
            lineWrapping: true,
	        extraKeys: {"Ctrl": "autocomplete"},
	        styleActiveLine: true,
//	        theme:"3024-night",
	        mode: {name: "javascript", globalVars: true},
            gutters: ["CodeMirror-linenumbers", "CodeMirror-foldgutter"]
	      });
	     JsVar["editor"].foldCode(CodeMirror.Pos(0, 0));
	     JsVar["editor"].on("changes", function (Editor, changes) {
	    	 JsVar["isEditting"]=true;
	    	});
	     
	     JsVar["editor"].setSize("100%", height-40);
}

/**
 * 是否正在编辑中
 * @returns
 */
function isEditing(){
	return JsVar["isEditting"];
	
}

/**
 * 保存页面编辑内容
 */
function saveFilePage(){
	getJsonDataByPost(Globals.baseActionUrl.DEVELOP_ACTION_SAVE_DEVELOP_FILE_URL,{path:path,filePage:JsVar["editor"].getValue()},"二次开发-保存代码",function (){
		JsVar["isEditting"]=false;
        showMessageTips("保存成功！");
		 
	}) 

}
/**
 * 获取未被保存的页面内容
 * @returns
 */
function getUnsaveFilePage(){
	return JsVar["editor"].getValue();
}