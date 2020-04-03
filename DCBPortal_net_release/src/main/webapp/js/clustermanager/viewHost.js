//父页面调用，新增和修改时会初使化一些参数，例如：data.action：操作类型
function onLoadComplete(data) {
    viewDetails(data);
}
/**
 * 点击详情，触发详情展示操作
 */
function viewDetails(data) {
	data = mini.clone(data);
	
	deserializerJsonViewForm("viewForm",data);
	$("#CRT_DATE").html(mini.formatDate(data.CRT_DATE,"yyyy-MM-dd HH:mm:ss"));
}

 