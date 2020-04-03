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
		$(item).dblclick(function(e) {
			changeOperatorByDbClick(index);
		});
	});
	
	//第一个页签刷新
	changeOperator(0, Globals.ctx+'/jsp/clustermanager/businesscluster/clusterBusTabs?index=0');
});

/**
 * 双击刷新iframe页签
 * @param index
 */
function changeOperatorByDbClick(index) {
	$("#frameDiv>iframe").each(function(i, item) {
		if (index == i) {
			$(item).css("display", "block");
			$(item).attr("src", $(item).attr("src"));
		} else {
			$(item).css("display", "none");
		}
	});
}

/**
 * 配置文件修改Tab不需要刷新
 */
function changeOperator(index, url) {
	$("#frameDiv>iframe").each(function(i, item) {
		if (index == i) {
			$(item).css("display", "block");
			if (!$(item).attr("isRefresh")) {
				$(item).attr("isRefresh", "1");
				$(item).attr("src", url);
			}
		} else {
			$(item).css("display", "none");
		}
	});
}

