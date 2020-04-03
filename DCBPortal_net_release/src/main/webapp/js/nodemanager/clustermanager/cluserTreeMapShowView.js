var img = new Image();
img.src = "../../../images/deployHost/module_1.png";

var dom = document.getElementById("container");
var myChart = echarts.init(dom);
var app = {};
option = null;
innit();

//myChart.showLoading();
function innit() {
    var id = geturl("id");
    if (id != undefined) {
        showView(id);
    }
}

function geturl(name) {
    var reg = new RegExp("[^\?&]?" + encodeURI(name) + "=[^&]+");
    var arr = window.parent.document.getElementById("myiframe").contentWindow.location.search.match(reg);
    if (arr != null) {
        return decodeURI(arr[0].substring(arr[0].search("=") + 1));
    }
    return "";
}

function showView(id) {
    var params = {"CLUSTER_ID": id + ""};
    getJsonDataByPost(Globals.ctx + "/nodeClusterManager/queryClusterView", params, "部署图-查询业务部署情况",
        function (result) {
            if (result) {
                myData = result;
            }
        }, "", null, false);


    putCLUSType(myData);
    if (myData && myData.children) {
        $.each(myData.children, function (index, dvalue) {
            putType(dvalue);
            if (dvalue.children) {
                $.each(dvalue.children, function (index, ddvalue) {
                    putType(ddvalue)
                });
            }
        });
    }


    console.log(JSON.stringify(myData));
    putData(myData);

}

function putData(data) {
    //myChart.hideLoading();
    option = {
        tooltip: {
            trigger: 'item',
            triggerOn: 'mousemove'
        },
        series:
            [{
                type: 'tree',
                initialTreeDepth: -1,
                data: [data],
                top: '1%',
                left: '20%',
                bottom: '1%',
                right: '40%',
                roam: true,
                // rootLocation: {x: '10%', y: '60%'}, // 根节点位置  {x: 'center',y: 10}
                // nodePadding: 10, //智能定义全局最小节点间距，不能定义层级节点间距，有点搓。
                symbol: 'image://../../../images/deployHost/module_1.png',
                lineStyle: {
                    color: "#ccc",
                    width: 1.3,
                    curveness: 0.5
                },
                label: {
                    normal: {
                        position: 'left',
                        verticalAlign: 'middle',
                        align: 'right',
                        fontSize: 15
                    }
                },

                leaves: {
                    label: {
                        normal: {
                            position: 'right',
                            verticalAlign: 'middle',
                            align: 'left'
                        }
                    }
                },

                expandAndCollapse: true,
                animationDuration: 100,
                animationDurationUpdate: 200
            }]

    };
    myChart.setOption(option, true);
    // myChart.setOption({series: [{data: data}]});
};
if (option && typeof option === "object") {
    var elesArr = Array.from(new Set(myChart._chartsViews[0]._data._graphicEls));
    var height = 350; // 这里限制最小高
    var currentHeight = 20 * (elesArr.length - 1) || 20; // 每项10px
    var newHeight = Math.max(currentHeight, height);
    dom.style.height = newHeight + 'px';
    myChart.resize();
}
myChart.on('click', function (params) {
    if (params.componentType === 'series') {
        // 点击到了 series 上
        if (!params.value) {
            // 点击的节点有子分支（可点开）
            var elesArr = Array.from(new Set(myChart._chartsViews[0]._data._graphicEls));
            var height = 350; // 这里限制最小高
            var currentHeight = 20 * (elesArr.length - 1) || 20; // 每项10px
            var newHeight = Math.max(currentHeight, height);
            dom.style.height = newHeight + 'px';
            myChart.resize();
        }
    }
});

function putType(obj) {
    if (obj) {
        var circleColor = '#69c';

        //文字描述的颜色
        var color = "gray";
        obj.symbol = 'image://../../../images/deployHost/module_1.png';
        if (obj.name.indexOf("正在运行状态") != -1) {
            color = "#5cb85c";
            obj.symbol = 'image://../../../images/deployHost/module_run_1.png';
        } else if (obj.name.indexOf("未运行状态") != -1) {
            color = "gray";
            obj.symbol = 'image://../../../images/deployHost/module_run_0.png'
        } else if (obj.name.indexOf("待部署状态") != -1) {
            color = "gray";
            obj.symbol = 'image://../../../images/deployHost/setting.png'
        }
        obj.symbol = 'circle';//使用image有bug，必须合并再展开才能展示图片
        var isNode = obj["c_type"] === 'NODE';
        if (isNode) {
            circleColor = color;
        }
        obj.symbolSize = [25, 25];
        obj.label = {
            normal: {
                color: color
            }
        };
        obj.itemStyle = {
            normal: {
                label: {
                    show: true,
                    textStyle: {
                        align: 'center',
                        verticalAlign: 'middle'
                    }
                },
                color: circleColor,
                borderWidth: 1,
                borderColor: '#000000'
            }
        };
    }
}

function putCLUSType(obj) {
    if (obj) {
        obj.symbol = 'roundRect';
        obj.symbolSize = [25, 25];
        obj.label = {
            color: "blue"
        };
        obj.itemStyle = {
            normal: {
                label: {
                    show: true
                },
                color: 'blue',
                borderWidth: 1,
                borderColor: '#000000'
            }
        };
    }
}