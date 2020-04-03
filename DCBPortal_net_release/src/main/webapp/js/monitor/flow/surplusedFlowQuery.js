//定义变量， 通常是页面控件和参数
var JsVar = new Object();
/**
 * 初始化
 */
$(document).ready(function () {
    mini.parse();
    JsVar["queryForm"] =  new mini.Form("#queryForm");
    JsVar["flowGrid"] = mini.get("flowGrid");//取得任务表格
});

function search(){
	
	JsVar["queryForm"].validate();
    if (JsVar["queryForm"].isValid() == false){
        return;
    }
	var params=JsVar["queryForm"].getData();
	datagridLoadPage(JsVar["flowGrid"],params,"",Globals.baseActionUrl.MONITOR_ACTION_FLOW_TRANSFER_QUERY_URL);
	
	
	 
}

function onload(e){
	var data=e.data;
	if(data.length<1){
		var params=JsVar["queryForm"].getData();
        showWarnMessageTips("未查到【设备号："+params["SERVNO"]+"】相关信息");
	}
	
}

/**
 * 数字格式化
 * @param e
 * @returns {String}
 */
function numberFormat(e){
	var formatNumber;
	var value = e.value+"";
	var values = value.split(".");
	var iInt;
	var iFra;
	if(values.length>1){
		iInt = values[0];
		iFra = values[1];
	}else{
		iInt = values[0];
	}
	var splits= Math.ceil(iInt.length/3);
	var firstSplit = (iInt.length%3) == 0 ? 3 : iInt.length%3;
	var splitsArray=[];
	splitsArray.push(iInt.substring(0,firstSplit));
	for(var i = 0;i< splits-1;i++){
		splitsArray.push(iInt.substring(firstSplit+i*3,firstSplit+(i+1)*3));
	}
	formatNumber= splitsArray.join(",")+(iFra == undefined ?"":iFra);
	return formatNumber+" KB";
	 
}

