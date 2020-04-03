/**
 * Created with IntelliJ IDEA.
 * Creater: zhongsixue
 * Date: 16-8-30
 * Time: 上午09:30
 * To change this template use File | Settings | File Templates.
 */

/**
 * 定义变量， 通常是页面控件和参数
 */
var JsVar=new Object();
var DataObj=new Object();
var GlobalParams=new Object();
/**
 * 初始化
 */
$(document).ready(function () {
    mini.parse();
    JsVar["dirsGrid"] = mini.get("dirsGrid");
    JsVar["fileGrid"] = mini.get("fileGrid");
    //获取前台传过来的参数
    getForwardParams();
    //加载表格
    loadGridInfo();
});

/**
 * 获取跳转页面get请求参数,并保存到JsVar中 
 */
function getForwardParams(){
    // var forwardParamString = window.location.search;
	// var queryArray = forwardParamString.split("=");
	DataObj["clusterName"] = getQueryString('clusterName');
	DataObj["host"] = getQueryString('host');
	DataObj["name"] = getQueryString('file');
}

/**
 * 表格加载
 */
function loadGridInfo(){
	getJsonDataByPost(Globals.baseActionUrl.MONITOR_ACTION_SUMMARY_CLUSTER_LOG_FILE_URL, DataObj, "",
		function(result){
		//先给全局变量赋值
		DataObj["port"]=result.port;
		DataObj["parent"]=result.parent;
		
		//加载表格
		JsVar["dirsGrid"].setData(result.dirs);	
		JsVar["fileGrid"].setData(result.files);
		//标签里的内容
		$("#logDir").html(result.parent);
		$("#logHost").html("["+result.host+"]");
		
	},null,null,false);
	
}

/**
 * 列渲染，dir名称
 * @param e
 */
function dirsRenderer(e){
	return  '<a class="Delete_Button" href="javascript:dirsScan(\''+e.record.fileName+'\')">'+e.record.fileName+'</a>';
}

/**
 * 目录点击：查看dir
 */
function dirsScan(fileName){
	DataObj["dir"]=DataObj["parent"]+"/"+fileName;
	loadGridInfo();
}

/**
 * 列渲染，dir/file时间格式
 * @param e
 */
function timeRederer(e){
	var time=e.record.modifyTime;
	//截取时间
	var year=time.substring(0,4);
	var month=time.substring(4,6);
	var day=time.substring(6,8);
	var hour=time.substring(8,10);
	var minute=time.substring(10,12);
	var second=time.substring(12,14);
	//拼接时间
	var time_date=year+"-"+month+"-"+day+" "+hour+":"+minute+":"+second;
	return time_date;
}

/**
 * 列渲染，file名称
 */
function filesRenderer(e){
	var html='<a class="Delete_Button" href="'
		+Globals.baseActionUrl.MONITOR_ACTION_SUMMARY_NIMBUS_LOG_URL
		+'?clusterName='+DataObj["clusterName"]+'&host='+DataObj["host"]
		+'&port='+DataObj["port"]+'&dir='+DataObj["parent"]+'&file='+e.record.fileName+'">'+e.record.fileName+'</a>';
	return  html;
}

/**
 * 列渲染，file操作按钮（文件下载）
 */
function oprateRenderer(e){
	var html='<a class="Delete_Button" style="cursor:pointer;" title="最大下载:10MB" '
		+'href="'+Globals.baseActionUrl.MONITOR_ACTION_SUMMARY_DOWNLOAD_LOG_URL
		+'?host='+DataObj["host"]+'&port='+DataObj["port"]+'&dir='+DataObj["parent"]+'&file='+e.record.fileName+'">下载文件</a>';
	return html;
}

function fileSizeRenderer(e){
	var size_byte=e.record.size;
	var format_size;
	if(size_byte>=(1024*1024*1024)){
		format_size=(size_byte/(1024*1024*1024)).toFixed(2)+"GB";
	}else if(size_byte>=(1024*1024)){
		format_size=(size_byte/(1024*1024)).toFixed(2)+"MB";
	}else {
		format_size=(size_byte/1024).toFixed(2)+"KB";
	}
	return format_size;
}

/**
 * 返回
 */
function back(){
	history.back();
}
