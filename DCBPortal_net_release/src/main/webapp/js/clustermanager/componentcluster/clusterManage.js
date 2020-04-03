/**
 * Created with IntelliJ IDEA.
 * Creater: yuanhao
 * Date: 16-7-19
 * Time: 上午10:00
 * To change this template use File | Settings | File Templates.
 */
$(document).ready(function () {
	var len = $("#clusterProDiv tr td").length;
	$("#clusterProDiv tr td").each(function(index, item){
		$(item).click(function(e) {
			$("#clusterProDiv td").removeClass("hover");
			$("#clusterProDiv td").removeClass("nobg");
			$("#clusterProDiv td").removeClass("bg");
			$("#clusterProDiv td").removeClass("jt2");
			//获取当前对象下标
			var currIndex = $("#clusterProDiv td").index($(this));
			if (currIndex > 0) {
				$("#clusterProDiv td:eq("+(currIndex-1)+")").addClass("jt2");
			}
			if (currIndex == (len - 1)) {
				$(this).addClass("nobg bg");
			} else {
				$(this).addClass("hover");
				$("#clusterProDiv td:last").addClass("nobg");
			}
		});
	});
});

/**
 * 配置文件修改Tab不需要刷新
 */
var isRefresh = false;
function changeOperator(index, url) {
	if (index == 3) {
		$("#elePages").css("display", "none");
		$("#filesPages").css("display", "block");
		if (!isRefresh) {
			$("#filesPages").attr("src", url);
			isRefresh = true;
		}
	} else {
		$("#elePages").css("display", "block");
		$("#elePages").attr("src", url);
		$("#filesPages").css("display", "none");
	}
}

