//全局对象
var JsVar = new Object();

/**
 * 初始化对象
 */
$(function() {
    //加载业务集群
    getclusterList();
    //tab切换
    var $tabNavItem = $('.tabNav').find('a');
    JsVar["BUS_CLUSTER_ID"] = $($tabNavItem[0]).attr("id");

    var $tabPane = $('.tabPane');
    $tabNavItem.each(function (i) {
        $(this).click(function () {
            $(this).parent().addClass('hover').siblings().removeClass('hover');//为tab列表选项增加选中样式
            //$tabPane.eq(i).addClass('hover').siblings().removeClass('hover');//为tab列表对应的内容增加显示隐藏样式
            JsVar["BUS_CLUSTER_ID"] = $(this).attr("id");
            clickTab();
            return false;
        });
    });
    clickTab();
});


/**
 * 点击tab页面时
 */
function clickTab() {
    //清空HTML
    initClearHtml();
    //重新加载HTML
    initDeployViewData();
    //初始化画布
    initCanvas();
}

/**
 * 手动刷新
 */
function handleRefresh() {
    clickTab();
    showMessageTips("刷新成功");
}

/**
 * 自动刷新
 */
var refreshObj = null;
function autoRefresh() {
    var autoTimes = mini.get("autoCom").getValue();
    if (autoTimes == "0") {
        clearInterval(refreshObj);
    } else {
        var refreshTimes = parseInt(autoTimes) * 1000;
        //先移除定时器
        clearInterval(refreshObj);
        //新增定时器
        refreshObj = setInterval(function(){
            //console.log(new Date().format("yyyy-MM-dd hh:mm:ss") + ", refreshTimes: " + refreshTimes);
            clickTab();
        }, refreshTimes);
    }
}

/**
 * 测试画一条线
 * @param context
 */
function createLineForTest(context) {
    context.beginPath();
    context.moveTo(400, 100);
    context.lineTo(400, 400);
    context.moveTo(400, 100.5);
    context.lineTo(600, 600);
    context.closePath();
    context.fillStyle = "#DC143C";
    context.fill();
    context.lineWidth = 1;
    context.strokeStyle = "#DC143C";
    context.stroke();


}

/**
 * 创建画布
 */
function initCanvas() {

    var deployViewCanvas = document.getElementById("deployCanvas");
    //获取画布上下文
    var context = deployViewCanvas.getContext("2d");

    //设置画布大小，画布清空
    var deployViewWidth = $("#deployView").width();
    var deployViewHeight = $("#deployView").height();
    deployViewCanvas.width = (deployViewWidth + 50);
    deployViewCanvas.height = (deployViewHeight + 50);

    //createLineForTest(context);

    if (context) {
        $("ul[ulFlag='line'] li").each(function(index, item){
            var parentTop = $(item).find("div:eq(0)").offset().top + 2;
            parentTop += $(item).find("div:eq(0)").height();

            var parentLeft = $(item).find("div:eq(0)").offset().left;
            parentLeft += $(item).find("div:eq(0)").width()/2;

            var subPositionArray = [];
            var standTop = 0;
            $(item).find("ul li").each(function(subIndex, subItem){
                var liFlag = $(subItem).attr("liFlag");

                var subTop = $(subItem).find("div:eq(0)").offset().top;
                var subLeft = $(subItem).find("div:eq(0)").offset().left + $(subItem).find("div:eq(0)").width()/2;
                //第一次赋予初始化高度
                if (subIndex == 0) {
                    standTop = subTop;
                }
                //只有第一排需要连接
                if (standTop == subTop) {
                    subPositionArray.push({
                        liFlag: liFlag,
                        top: subTop,
                        left: subLeft
                    });
                }
            });

            if (subPositionArray != null && subPositionArray.length > 0) {
                var subLength = subPositionArray.length;
                for (var i = 0; i < subLength; i++) {
                    context.beginPath();
                    context.moveTo(endWithFive(parentLeft), endWithFive(parentTop));

                    //组件程序连线特殊出炉
                    if (subLength == 1 && subPositionArray[i]["liFlag"] == 'comp') {
                        subPositionArray[i]["left"] = parseFloat(subPositionArray[i]["left"]) + 2;
                    }
                    //业务组件连线特殊处理
                    if (subLength == 1 && subPositionArray[i]["liFlag"] == 'bus') {
                        var addLen = 1;
                        if (index == 0) {
                            addLen = 2;
                        }
                        subPositionArray[i]["left"] = parseFloat(subPositionArray[i]["left"]) + addLen;
                    }
                    if (subLength == 3 && subPositionArray[i]["liFlag"] == 'bus') {
                        subPositionArray[i]["left"] = parseFloat(subPositionArray[i]["left"]) + 2;
                    }

                    context.lineTo(endWithFive(subPositionArray[i]["left"]), endWithFive(subPositionArray[i]["top"]));

                    // if (subLength == 3 && subPositionArray[i]["liFlag"] == 'bus') {
                    //     console.log("endWithFive(parentLeft)-->" + endWithFive(parentLeft)
                    //         + ", endWithFive(parentTop) --->" + endWithFive(parentTop)
                    //         +", endWithFive(subPositionArray[i]['left']) --->" + endWithFive(subPositionArray[i]["left"])
                    //         + ", endWithFive(subPositionArray[i]['top']) --->" + endWithFive(subPositionArray[i]["top"]))
                    // }

                    context.closePath();
                    context.setLineDash([3.5, 3.5]);
                    context.fillStyle = "#0f5f5f";
                    context.fill();
                    context.lineWidth = 1;
                    context.strokeStyle = "#0f5f5f";
                    context.stroke();
                }
            }
            //console.log("\n")
        });
    } else {
        showWarnMessageTips("当前浏览器不支持Canvas！");
    }
}

/**
 * 结尾以0.5结束，解决划线毛边问题
 * @param parentLeft
 * @returns {number}
 */
function endWithFive(parentLeft) {
    return (parseInt(parentLeft) + parseFloat(0.5));
}


/**
 * 清空HTML对象数据
 */
function initClearHtml() {
    $("#busView").html("");
    $("#compView").html("");
}

/**
 * 查询业务部署图数据
 */
function initDeployViewData() {
    var params = {
        BUS_CLUSTER_ID : JsVar["BUS_CLUSTER_ID"]
    };
    getJsonDataByPost(Globals.baseActionUrl.DEPLOY_VIEW_QUERY_DATA_URL, params, "部署图-查询业务部署图数据",
        function(retList){
            if (retList != null) {
                var busList = [];
                var compList = [];
                for (var i=0; i<retList.length; i++) {
                    var clusterFlag = retList[i]["clusterFlag"]

                    // var childrenList = retList[i]["subProgramList"];
                    // if (childrenList != null) {
                    //     var cloneData = retList[i]["subProgramList"].clone();
                    //     for(var k=0; k<cloneData.length; k++) {
                    //         // if (retList[i]["clusterType"] == 'dca') {
                    //         //     retList[i]["subProgramList"].push(cloneData[k]);
                    //         //     retList[i]["subProgramList"].push(cloneData[k]);
                    //         // }
                    //
                    //         retList[i]["subProgramList"].push(cloneData[k]);
                    //         retList[i]["subProgramList"].push(cloneData[k]);
                    //         retList[i]["subProgramList"].push(cloneData[k]);
                    //     }
                    // }
                    if (clusterFlag == '3') {
                        busList.push(retList[i]);
                        // busList.push(retList[i]);
                    } else {
                        compList.push(retList[i]);
                        // compList.push(retList[i]);
                    }
                }
                initBusView(busList);

                initCompView(compList);
            }
        }, null, null, false);
}

/**
 * 初始化业务视图
 * @param busList
 */
function initBusView(busList) {
    for (var i=0; i<busList.length; i++) {
        var subProgramList = busList[i]["subProgramList"];
        var clusterName = busList[i]["clusterName"];
        var clusterType = busList[i]["clusterType"];
        var clusterId = busList[i]["clusterId"];
        //主机列表
        var hostList = busList[i]["hostList"] == null ? "无部署主机" : busList[i]["hostList"];
        //部署主机数量
        var hostCount = busList[i]["hostCount"] == null ? 0 : busList[i]["hostCount"];
        //当前运行实例数量
        var instCount = busList[i]["instCount"] == null ? 0 : busList[i]["instCount"];

        //设置程序所占宽度,业务程序一排最多放4个子程序
        // var programWidth = 279;
        var programWidth = 368;
        if (subProgramList == null || subProgramList.length == 0 || subProgramList.length == 1) {
            programWidth = 168;
        } else if (subProgramList.length == 2) {
            programWidth = 187;
        } else if (subProgramList.length <= 8) {
            programWidth = subProgramList.length * 46;
        }

        var tabHtml = "<li style='width: " + programWidth + "px;'>";
        tabHtml += "<div class='first-level blue' style='width: 160px;margin:auto;'>";
        //tabHtml += "<p class='head'>" + clusterName + "（" + clusterType + "）" + "</p>";
        tabHtml += "<p class='head'>" + clusterName + "</p>";
        tabHtml += "<div class='content'>";
        tabHtml += "<p title='部署主机'>主机：<span><a href='javascript:void(0)' title='部署主机有:"+ hostList + "' onclick='showDeployHost(\"" + clusterId + "\", \"3\")'>" + hostCount+ "</a></span></p>";
        tabHtml += "<p title='运行实例'>实例：<span><a href='javascript:void(0)' title='点击查看' onclick='showProgramInst(\"" + clusterId + "\", \"3\")'>" + instCount+ "</a></span></p>";
        tabHtml += "</div>";
        tabHtml += "</div>";
        tabHtml += "<ul class='second-outer clearfix' style='width:" + programWidth + "px'>";
        if (subProgramList != null && subProgramList.length > 0) {
            var subLength = subProgramList.length;
            subProgramList.sort(comparFun);
            for (var j = 0; j < subLength; j++) {
                var programName = $.trim(subProgramList[j]["name"]);
                var instCount = $.trim(subProgramList[j]["instCount"]);

                if (subLength == 1) {
                    tabHtml += "<li style='width:40px;margin-left: 63px;'  liFlag='bus'>";
                } else if (subLength == 2) {
                    tabHtml += "<li style='width:40px;margin-left: 38px;'  liFlag='bus'>";
                } else if (subLength == 3) {
                    tabHtml += "<li style='width:40px;margin-left: 21px;'  liFlag='bus'>";
                } else {
                    tabHtml += "<li style='width:40px;margin-left: 5px;'  liFlag='bus'>";
                }

                if (programName.length >= 10) {
                    tabHtml += "<div class='second-level red' style='width:100%;height: 110px;margin: auto;'>";
                } else {
                    tabHtml += "<div class='second-level red' style='width:100%;height: 70px;margin: auto;'>";
                }
                tabHtml += "<div title='" + programName + "'>" + programName + "<em style='margin: auto'><a href='javascript:void(0)' onclick='showProgramInst(\"" + clusterId + "\", \"3\", \"" + programName + "\")'>" + instCount+ "</a></em></div>";
                tabHtml += "</div>";
                tabHtml += "<li>";
            }
        }
        tabHtml += "</ul>"
        tabHtml += "</li>"
        $("#busView").append(tabHtml);
    }

    //将浮动块删掉
    $("#busView").find("li").each(function(index, item){
        if ($(this).html() == "") {
            $(this).remove();
        }
    });
}

/**
 * 根据名称长度排序
 * @param obj1
 * @param obj2
 * @returns {number}
 */
var comparFun = function(obj1, obj2) {
    var obj1Length = obj1["name"].length;
    var obj2Length = obj2["name"].length;
    return obj1Length - obj2Length;
}

/**
 * 初始化组件视图
 * @param busList
 */
function initCompView(compList) {
    for (var i=0; i<compList.length; i++) {
        var subProgramList = compList[i]["subProgramList"];
        var clusterName = compList[i]["clusterName"];
        var clusterType = compList[i]["clusterType"];
        var clusterId = compList[i]["clusterId"];
        //主机列表
        var hostList = compList[i]["hostList"] == null ? "无部署主机" : compList[i]["hostList"];
        //部署主机数量
        var hostCount = compList[i]["hostCount"] == null ? 0 : compList[i]["hostCount"];
        //当前运行实例数量
        var instCount = compList[i]["instCount"] == null ? 0 : compList[i]["instCount"];

        //判断子元素中是否有比较长的类型，如果有则先计算该类型所需长度
        var isOutRange = false;
        for (var j = 0; j < subProgramList.length; j++) {
            var programName = $.trim(subProgramList[j]["name"]);
            if (programName.length >= 9) {
                isOutRange = true;
                break;
            }
        }

        //设置程序所占宽度
        var isNewLine = false;
        var programWidth = 188;
        if (subProgramList == null || subProgramList.length == 0
            || subProgramList.length < 4 || isOutRange) {
            isNewLine = true;
            programWidth = 125;
        }

        var tabHtml = "";
        tabHtml = "<li style='width: " + programWidth + "px;'>";
        tabHtml += "<div class='first-level green' style='width: 118px;margin:auto;'>";
        //tabHtml += "<p class='head'>" + clusterName + "（" + clusterType + "）" + "</p>";
        tabHtml += "<p class='head'>" + clusterName + "</p>";
        tabHtml += "<div class='content'>";
        tabHtml += "<p title='部署主机'>主机：<span><a href='javascript:void(0)' title='部署主机有:"+ hostList + "' onclick='showDeployHost(\"" + clusterId + "\", \"1\")'>" + hostCount+ "</a></span></p>";
        tabHtml += "<p title='运行实例'>实例：<span><a href='javascript:void(0)' title='点击查看' onclick='showProgramInst(\"" + clusterId + "\", \"1\")'>" + instCount+ "</a></span></p>";
        tabHtml += "</div>";
        tabHtml += "</div>";
        tabHtml += "<ul class='second-outer clearfix'>";
        if (subProgramList != null && subProgramList.length > 0) {
            for (var j = 0; j < subProgramList.length; j++) {
                var programName = $.trim(subProgramList[j]["name"]);
                var instCount = $.trim(subProgramList[j]["instCount"]);
                if (j % 2 == 0) {
                    if (subProgramList.length == 1) {
                        tabHtml += "<li style='width: 40px;' liFlag='comp'>";
                    } else {
                        if (isNewLine) {
                            tabHtml += "<li style='width: 40px;margin-left: 3px;' liFlag='comp'>";
                        } else {
                            tabHtml += "<li style='width: 40px;margin-left: 2px;' liFlag='comp'>";
                        }
                    }
                } else {
                    if (isNewLine) {
                        tabHtml += "<li style='width: 40px;margin-left: 3px;' liFlag='comp'>";
                    } else {
                        tabHtml += "<li style='width: 40px;margin-left: 2px; margin-right: 2px;' liFlag='comp'>";
                    }
                }
                tabHtml += "<div class='second-level yellow' style='width:100%;height: 80px;margin: auto;'>";
                tabHtml += "<div title='" + programName + "'>" + programName + "<br/><em style='margin: auto'><a href='javascript:void(0)' onclick='showProgramInst(\"" + clusterId + "\", \"1\", \"" + programName + "\")'>" + instCount+ "</a></em></div>";
                tabHtml += "</div>";
                tabHtml += "<li>";
            }
        }
        tabHtml += "</ul>";
        tabHtml += "</li>";
        $("#compView").append(tabHtml);
    }
    //将浮动块删掉
    $("#compView").find("li").each(function(index, item){
        if ($(this).html() == "") {
            $(this).remove();
        }
    });
}

/**
 * 加载业务主集群
 */
function getclusterList(){
    getJsonDataByPost(Globals.baseActionUrl.BUS_MAIN_CLUSTER_ACTION_GET_URL, null, "部署图-获取集群",
        function(result){
            if(result.length>0){
                var tabHtml = '';
                $.each(result, function (i, item) {
                    if(i == 0){
                        tabHtml +='<li class="hover"><a id='+item.BUS_CLUSTER_ID+'>'+item.BUS_CLUSTER_NAME+'</a></li>';
                    }else{
                        tabHtml +='<li><a id='+item.BUS_CLUSTER_ID+'>'+item.BUS_CLUSTER_NAME+'</a></li>';
                    }
                });
                $('.tabNav').append(tabHtml);
            }
        },null,null,false);
}

/**
 * 实例列表
 * @param clusterId
 * @param clusterFlag
 * @param programCode
 */
function showProgramInst(clusterId, clusterFlag, programCode) {
    var params = {
        BUS_CLUSTER_ID:JsVar["BUS_CLUSTER_ID"],
        CLUSTER_ID:clusterId,
        CLUSTER_FLAG:clusterFlag,
        SUB_PROGRAM:programCode,
        SHOW_FLAG:"INST"
    };
    showDialog("查看实例列表", "80%", "80%", Globals.baseJspUrl.DEPLOY_VIEW_LIST_URL,
        function destroy(data){

        }, params);
}

/**
 * 部署主机列表
 * @param clusterId
 * @param clusterFlag
 * @param programCode
 */
function showDeployHost(clusterId, clusterFlag, programCode) {
    var params = {
        BUS_CLUSTER_ID:JsVar["BUS_CLUSTER_ID"],
        CLUSTER_ID:clusterId,
        CLUSTER_FLAG:clusterFlag,
        SUB_PROGRAM:programCode,
        SHOW_FLAG:"HOST"
    };
    showDialog("查看主机列表", "80%", "80%", Globals.baseJspUrl.DEPLOY_VIEW_LIST_URL,
        function destroy(data){

        }, params);
}