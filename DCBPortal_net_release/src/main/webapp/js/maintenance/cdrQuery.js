 //定义变量， 通常是页面控件和参数
var JsVar = new Object();
var testData= [{CATALOG:'/public/sh_dca/cdr/bak/Voice/NORMAL_FILE_OUTPUT/fileload/bak/*.r',FILES_NUM:202,NUMBERS:2696170},{CATALOG:'/public/sh_dca/cdr/bak/Voice/NORMAL_FILE_OUTPUT/fileload/bak/*.r',FILES_NUM:202,NUMBERS:2696170}];
/**
 * 初始化
 */
$(document).ready(function () {
    mini.parse();
    JsVar["fileGrid"] = mini.get("fileGrid");//取得表格
    JsVar["queryForm"] =  new mini.Form("#queryForm");//取得查询表单
    JsVar["cache"] = 90;
    
    JsVar["voiceArray"]=[];
    JsVar["voiceSplitArray"]=[];
    
    JsVar["dataArray"]=[];
    JsVar["dataSplitArray"]=[];
    
    JsVar["smsArray"]=[];
    JsVar["smsSplitArray"]=[];
    
//    JsVar["voiceArray"] = testData.concat(JsVar["voiceArray"]);
//    
//    JsVar["dataArray"] = testData.concat(JsVar["dataArray"]);
//    JsVar["dataArray"] = testData.concat(JsVar["dataArray"]);
//    
//    JsVar["smsArray"] = testData.concat(JsVar["smsArray"]);
//    JsVar["smsArray"] = testData.concat(JsVar["smsArray"]);
//    JsVar["smsArray"] = testData.concat(JsVar["smsArray"]);
    queryVoice();
    queryData();
    querySms();
    setInterval(queryVoice,10000);
    setInterval(queryData,10000);
    setInterval(querySms,10000);
    
    setTimeout(loadGrid,2000);
    setInterval(loadGrid,10000);
});

//查询
function loadGrid() {
	var formData = JsVar["queryForm"].getData();
	var cdr_type = formData["cdr_type"];
	if(cdr_type == 1){
		 JsVar["fileGrid"].setData(JsVar["voiceArray"]);
	}else if(cdr_type == 2){
		 JsVar["fileGrid"].setData(JsVar["dataArray"]);
	}else if(cdr_type == 3){
		 JsVar["fileGrid"].setData(JsVar["smsArray"]);
	}
}


/**
 * 语音
 */
function queryVoice(){
	
	getJsonDataByGet(Globals.baseActionUrl.MAINTENANCE_HOST_QUERY_CDRLIST,{cdr_type:'1'},"语音",function (result){
		
		if(JsVar["voiceSplitArray"].length == JsVar["cache"]){
			var index = JsVar["voiceSplitArray"][0];
			var voiceArrayLength = JsVar["voiceArray"].length;
			JsVar["voiceArray"] = JsVar["voiceArray"].slice(0,voiceArrayLength-index);
			JsVar["voiceSplitArray"] = JsVar["voiceSplitArray"].slice(1);
			JsVar["voiceArray"] = result.concat(JsVar["voiceArray"]);
			JsVar["voiceSplitArray"].push(result.length);
		}else{
			JsVar["voiceArray"] = result.concat(JsVar["voiceArray"]);
			JsVar["voiceSplitArray"].push(result.length);
		}
	});
	hideLoadMask();
}

/**
 * 数据
 */

function queryData(){
	getJsonDataByGet(Globals.baseActionUrl.MAINTENANCE_HOST_QUERY_CDRLIST,{cdr_type:'2'},"数据",function (result){
		
		if(JsVar["dataSplitArray"].length == JsVar["cache"]){
			var index = JsVar["dataSplitArray"][0];
			var dataArrayLength = JsVar["dataArray"].length;
			JsVar["dataArray"] = JsVar["dataArray"].slice(0,dataArrayLength-index);
			JsVar["dataSplitArray"] = JsVar["dataSplitArray"].slice(1);
			JsVar["dataArray"] = result.concat(JsVar["dataArray"]);
			JsVar["dataSplitArray"].push(result.length);
		}else{
			JsVar["dataArray"] = result.concat(JsVar["dataArray"]);
			JsVar["dataSplitArray"].push(result.length);
		}
	});
	hideLoadMask();
}


/**
 * 短信
 */
function querySms(){
	getJsonDataByGet(Globals.baseActionUrl.MAINTENANCE_HOST_QUERY_CDRLIST,{cdr_type:'3'},"短信",function (result){
		if(JsVar["smsSplitArray"].length == JsVar["cache"]){
			var index = JsVar["smsSplitArray"][0];
			var smsArrayLength = JsVar["smsArray"].length;
			JsVar["smsArray"] = JsVar["smsArray"].slice(0,smsArrayLength-index);
			JsVar["smsSplitArray"] = JsVar["smsSplitArray"].slice(1);
			JsVar["smsArray"] = result.concat(JsVar["smsArray"]);
			JsVar["smsSplitArray"].push(result.length);
		}else{
			JsVar["smsArray"] = result.concat(JsVar["smsArray"]);
			JsVar["smsSplitArray"].push(result.length);
		}
	});
	hideLoadMask();
}

function format(e){
	var files_num = e.record.FILES_NUM;
	
	if(files_num=='total'){
		return "<span style='color:red;'>"+e.record.NUMBERS+"</span>";
	}else{
		return e.record.NUMBERS;
	}
}