/**
 * @author 王贤朋
 */
var JsVar = new Object();
$(document).ready(function () {
    mini.parse();
    JsVar["clusterName"] =  mini.get("clusterName");
    JsVar["clusterDesc"] =  mini.get("clusterDesc");
    JsVar["clusterCode"] =  mini.get("clusterCode");

    JsVar["allType"] = new Array();   //记录类型的NODE_TYPE_ID
    addNodeType();
    loadNodeTypeCombobox(mini.get("NODE_TYPE_0"));
});

//一行节点类型的索引
var typeIndex= -1;
//标签的索引
var tagIndex = -1;
var propPrefix = {
    typeId:"NODE_TYPE_",
    aliasId:"ALIAS_",
    typeTdId:"nodeTypeTd_",
    aliasTdId:"aliasTd_",
    nodesClass:"nodes_",
    nodesTrClass:"nodesTr_",
    delBtnId:"_tag"
};

//滚动条修饰
function niceScroll(selector,autoHidden) {
    $(selector).niceScroll({
        cursorcolor:"#ccc",
        cursoropacitymax: 1, //改变不透明度非常光标处于活动状态（scrollabar“可见”状态），范围从1到0
        touchbehavior: false, //使光标拖动滚动像在台式电脑触摸设备
        cursorwidth: "10px", //像素光标的宽度
        cursorborder: "0", // 游标边框css定义
        cursorborderradius: "10px",//以像素为光标边界半径
        autohidemode: autoHidden //是否隐藏滚动条
    });
}

//指定滚动条的垂直偏移量
function niceScrollToPosition(selector,top) {
    var scroll = $(selector).getNiceScroll(0);
    if(scroll){
        scroll.resize();
        scroll.doScrollTop(top);
    }
}

/**
 * 程序类型下拉框的加载
 */
function loadNodeTypeCombobox(combobox,param){

    comboxLoad( combobox,param,"nodeClusterManager.queryDeployedNodeType","","",false);
}

function ClusterRow(nodeTypeId,tagIndex){
    this.nodeTypeId = nodeTypeId;
    this.tagIndex = tagIndex;
}

/**
 * 点击节点类型时，显示出节点
 */
function loadNodesCheckBox(e) {
    var nodeTypeId = e.value;
    var params={
        NODE_TYPE_ID:nodeTypeId
    }

    var asyn = true;

    if(e.asyn!=undefined){
        asyn = e.asyn;
    }

    var curTagIndex = -1;
    if(JsVar["allType"][typeIndex]!=undefined){
        //第二次的选择
        curTagIndex = JsVar["allType"][typeIndex]["tagIndex"];
        JsVar["allType"][typeIndex]["nodeTypeId"] = nodeTypeId;
    } else {
        // 第一次的选择，把NODE_TYPE_ID进行存储
        JsVar["allType"][typeIndex] = new ClusterRow(nodeTypeId, tagIndex);
        curTagIndex = tagIndex;
    }

    //显示程序类型对应的节点
    getJsonDataByPost(Globals.ctx + "/nodeClusterManager/queryNodesByNodeType", params, "集群配置管理-获取节点列表",
        function success(result){
            if (result != null) {
                var nodesTr =
                    "<tr class='${nodesTrClass} nodesTr'>" +
                    "   <td></td>" +
                    "   <td colspan='5'>" +
                    "       <table></table>" +
                    "   </td>" +
                    "</tr>"

                var nodesContentPart1 =
                    "<tr class='${nodesClass} nodes'>" +
                    "     <td>" +
                    "         <div>" +
                    "               <input type='checkbox' class='nodeId' value='${nodeId}'/>" +
                    "               <span class='nodePathInfo' onclick='selectNode(this)'>${nodePathInfo}</span>" +
                    "         </div></td>" ;
                var nodesContentPart2 =
                    "         <td><div>" +
                    "               <input type='checkbox' class='nodeId' value='${nodeId2}'/>" +
                    "               <span class='nodePathInfo' onclick='selectNode(this)'>${nodePathInfo2}</span>" +
                    "         </div>" ;
                var nodesContentPart3=
                    "     </td>" +
                    "</tr>";

                //删除“旧的节点列表”
                var nodesTrClass = propPrefix.nodesTrClass+curTagIndex;
                var nodesClass=propPrefix.nodesClass+curTagIndex;
                $("." + nodesTrClass).remove();

                $(".formTable8").append(nodesTr.replace("${nodesTrClass}",nodesTrClass));

                for (var i=0,length=result.length;i<length;i+=2) {

                    if(length%2!=0 && i==length-1){
                        $("."+nodesTrClass+" table").append(
                            (nodesContentPart1 + nodesContentPart3).replace("${nodesClass}",nodesClass)
                                .replace("${nodeId}",result[i]["NODE_ID"])
                                .replace("${nodePathInfo}",result[i]["NODE_PATH_INFO"])
                        );
                    }else{
                        $("."+nodesTrClass+" table").append(
                            (nodesContentPart1 + nodesContentPart2 + nodesContentPart3).replace("${nodesClass}",nodesClass)
                                .replace("${nodeId}",result[i]["NODE_ID"])
                                .replace("${nodePathInfo}",result[i]["NODE_PATH_INFO"])

                                .replace("${nodeId2}",result[i+1]["NODE_ID"])
                                .replace("${nodePathInfo2}",result[i+1]["NODE_PATH_INFO"])
                        );

                    }

                }

                //第二行选择节点之后，显示第一行的删除键
                if(typeIndex==1){
                    $("#"+JsVar["allType"][0]["tagIndex"]+propPrefix.delBtnId).css("display","inline");
                }

                niceScrollToPosition(".mini-fit",10000);
            } else {
                showErrorMessageTips("获取程序节点失败，请检查!");
            }
        },null,null,asyn);
}

//选择节点程序
function selectNode(e) {
    var checkBox = $(e).parent().children(0);
    var checked = checkBox.prop("checked");
    if(checked){
        checkBox.prop("checked",false);
    }else{
        checkBox.prop("checked",true);
    }
}

//添加一行程序类型
function addNodeType(){
    //判断当前行是否选择程序类型
    var curTypeId = propPrefix.typeId + tagIndex;
    var curNodeType= mini.get(curTypeId);
    if(curNodeType && curNodeType.getValue() == ''){
        showWarnMessageTips("请先输入当前的节点类型");
        return ;
    }

    //添加新行时，当前行禁用
    var lastType = JsVar["allType"][typeIndex];
    lastType && mini.get(propPrefix.typeId +lastType["tagIndex"]).disable();

    tagIndex ++ ;

    //组件的添加
    var items = addItem();

    typeIndex++;

    //滑动条到底部
    niceScrollToPosition(".mini-fit",10000);

    return items;
}

/**
 * 添加组件
 */
function addItem() {
    //添加一行
    var nodeTypeTdId=propPrefix.typeTdId+tagIndex;
    var aliasTdId = propPrefix.aliasTdId+tagIndex;
    var delBtnId = tagIndex+propPrefix.delBtnId;
    var trContent = $("#nodeTypeModule").html()
        .replace("nodeTypeTd",nodeTypeTdId)
        .replace("aliasTd",aliasTdId)
        .replace("tagIndexSpan",delBtnId);
    $(".formTable8").append("<tr class='nodeTypeTr'>" + trContent +"</tr>");

    if(typeIndex==-1){
        $("#"+delBtnId).before("<span  class='label label-success'  onclick='addNodeType(this)'></span>");
        $("#"+delBtnId).css("display","none");
    }
    //添加miniUI组件
    var combobox = createCombobox(propPrefix.typeId+tagIndex);
    var textBox = createTextBox(propPrefix.aliasId+tagIndex);
    combobox.render(document.getElementById(nodeTypeTdId));
    textBox.render(document.getElementById(aliasTdId));

    //加载程序类型的下拉框
    var nodeTypeParam = null;
    var allType = JsVar["allType"];
    if (allType.length <= 0) {
        nodeTypeParam={NODE_TYPE_IDS: null};
    } else {
        var nodeTypeIds = new Array();
        for(var i=0;i<allType.length;++i){
            nodeTypeIds.push(allType[i]["nodeTypeId"]);
        }
        nodeTypeParam={NODE_TYPE_IDS: nodeTypeIds};
    }
    loadNodeTypeCombobox(combobox, nodeTypeParam);
    var items = {
        nodeTypeItem:combobox,
        aliasItem:textBox
    };
    return items;
}

//删除一行
function delNodeType(self) {
    var curTagIndex = parseInt(self.id);

    //删除空行时
    var curNodeType= mini.get(propPrefix.typeId + curTagIndex);
    if(curNodeType.getValue() == ''){
        $(self).parent().parent().remove();
        tagIndex -- ;
        typeIndex -- ;
        var lastTagIndex = JsVar["allType"][JsVar["allType"].length-1]["tagIndex"];
        mini.get(propPrefix.typeId+lastTagIndex).enable();

        return;
    }

    //删除行
    $(self).parent().parent().remove();

    //删除节点列表
    $("."+propPrefix.nodesTrClass+curTagIndex).remove();

    //删除记录
    var allType = JsVar["allType"];
    var curTypeIndex = -1;
    for(var i=0;i<allType.length;++i){
        if(allType[i]["tagIndex"] == curTagIndex){
            curTypeIndex = i;
        }
    }
    for(var i=curTypeIndex;i<allType.length-1;++i){
        //修改存储的值
        allType[i]=allType[i+1];

    }
    allType.removeAt(allType.length-1);
    typeIndex -- ;

    //删除的后续操作
    if(curTypeIndex == 0){
        $("#"+allType[0]["tagIndex"]+propPrefix.delBtnId).before("<span  class='label label-success'  onclick='addNodeType(this)'></span>");
    }
    if(allType.length == 1){
        $("#"+allType[0]["tagIndex"]+propPrefix.delBtnId).css("display","none");
    }

    //启动最后一行
    allType[typeIndex] && mini.get(propPrefix.typeId+allType[typeIndex]["tagIndex"]).enable();
}

//创建程序类型下拉框
function createCombobox(id) {
    var combobox = new mini.ComboBox();
    combobox.set({
        width: "100%",
        name: "NODE_TYPE",
        id: id,
        allowInput: "true",
        valueField :"NODE_TYPE_ID",
        textField:"NODE_TYPE_NAME",
        required:"true",
        onvaluechanged:"loadNodesCheckBox",
        emptyText:"请选择节点类型"
    });

    return combobox;
}

//创建类型别名输入框
function createTextBox(id) {
    var textBox = new mini.TextBox();
    textBox.set({
        width:"100%",
        name:"alias",
        id:id,
        emptyText:"节点类型别名"
    });

    return textBox;
}

/**
 * 集群编码的校验
 */
function checkClusterCode(code) {
    var reg = new RegExp("^[\\w-$]+$");
    return reg.test(code);
}

/**
 * 保存成一个集群
 */
function saveCluster(action) {
    var param=new Object();

    param["NODE_CLUSTER_NAME"]=JsVar["clusterName"].getValue();
    param["NODE_CLUSTER_CODE"]=JsVar["clusterCode"].getValue();
    param["NODE_CLUSTER_DESC"]=JsVar["clusterDesc"].getValue();

    if(!param["NODE_CLUSTER_NAME"] && !param["NODE_CLUSTER_CODE"]){
        showWarnMessageTips("集群名称和集群编码不能为空！");
        return;
    }

    if(!checkClusterCode(param["NODE_CLUSTER_CODE"])){
        showWarnMessageTips("集群编码只能由字母、数字、$、-、下划线组成！");
        return;
    }

    // var nodes = document.getElementsByClassName("nodeId");
    var nodes = $(".nodeId");
    var moreThanOne = false;
    for(var i=0;i<nodes.length;++i){
            if(nodes[i].checked){
                moreThanOne = true;
            }
    }
    if(!moreThanOne){
        showWarnMessageTips("创建集群时至少需要包含一个节点！");
        return;
    }

    var nodeTypes=new Array();     //JsVar["allType"];
    var allType = JsVar["allType"];
    for(var index=0;index<allType.length;++index){
        var tagIndex = allType[index]["tagIndex"];
        var $nodeIds = $("."+propPrefix.nodesClass+tagIndex+" .nodeId");
        var nodeIds = new Array();
        var nodeTypeHaveNode = false;

        //添加该类型的节点
        for(var i=0;i<$nodeIds.length;++i){
            if($nodeIds[i].checked){
                nodeIds.push({NODE_ID:$nodeIds[i].value});
                nodeTypeHaveNode = true;
            }
        }
        if(!nodeTypeHaveNode){
            continue;
        }
        nodeTypes.push({
            NODE_TYPE_ID:allType[index]["nodeTypeId"],
            NODE_TYPE_ALIAS:mini.get(propPrefix.aliasId+tagIndex).getValue(),
            NODE_IDS:nodeIds
        })
    }

    param["NODE_TYPES"] = nodeTypes;

    showLoadMask();
    if (action == "add") {
        getJsonDataByPost(Globals.ctx + "/nodeClusterManager/addCluster", param, "集群管理-新增集群-集群信息插入", function success(result) {
            if (result != null && result != undefined) {
                if (result["rstCode"] == "1") {
                    closeWindow(systemVar.SUCCESS);
                } else {
                    showWarnMessageTips(result["rstMsg"]);
                }
            }
        });
    }else{
        param["ID"] = mini.get("clusterId").getValue();
        getJsonDataByPost(Globals.ctx + "/nodeClusterManager/updateCluster", param, "集群管理-修改集群-集群信息更新", function success(result) {
            if (result != null && result != undefined) {
                if (result["rstCode"] == "1") {
                    closeWindow(systemVar.SUCCESS);
                } else {
                    showWarnMessageTips(result["rstMsg"]);
                }
            }
        });
    }
}

//调用showAddDialog/showEditDialog时会执行
function onLoadComplete(action,data) {

    JsVar[systemVar.ACTION] = action;

    if (action == systemVar.EDIT){
        findClusterById(data);
        niceScroll(".mini-fit",false);
    }else{
        niceScroll(".mini-fit",true);
    }
}

function findClusterById(data) {
    showLoadMask();
    getJsonDataByPost(Globals.ctx + "/nodeClusterManager/findClusterById", data, "集群管理-根据Id查询集群",function success(result) {
        if(result!=null && result!=undefined){
            JsVar["clusterName"].setValue(result["NODE_CLUSTER_NAME"]);
            JsVar["clusterCode"].setValue(result["NODE_CLUSTER_CODE"]);
            JsVar["clusterDesc"].setValue(result["NODE_CLUSTER_DESC"]);
            mini.get("clusterId").setValue(result["ID"]);
            var nodeTypeList = result["nodeTypes"];
            for (var i = 0,length=nodeTypeList.length; i < length; ++i) {

                if (i == 0) {
                    //设置第一行属性
                    var nodeType0 = mini.get("NODE_TYPE_0");
                    loadNodeTypeCombobox(nodeType0);
                    var nodeTypeId = nodeTypeList[i]["NODE_TYPE_ID"];
                    nodeType0.setValue(nodeTypeId);
                    mini.get("ALIAS_0").setValue(nodeTypeList[i]["NODE_TYPE_ALIAS"]);

                    //添加节点
                    loadNodesCheckBox({value:nodeTypeId,asyn:false});
                    //选中节点
                    var checkedNodes = nodeTypeList[i]["nodes"];
                    nodesChecked(checkedNodes);
                }
                else {
                    //添加一行
                    var items = addNodeType();
                    var nodeTypeId = nodeTypeList[i]["NODE_TYPE_ID"];
                    items["nodeTypeItem"].setValue(nodeTypeId);
                    items["aliasItem"].setValue(nodeTypeList[i]["NODE_TYPE_ALIAS"]);

                    //添加节点
                    loadNodesCheckBox({value: nodeTypeId, asyn: false});
                    //选中节点
                    var checkedNodes = nodeTypeList[i]["nodes"];
                    nodesChecked(checkedNodes);
                }

            }
            //滑动条到底部
            niceScrollToPosition(".mini-fit",0);
        }
    });
}

/**
 * 程序单选框的选择
 */
function nodesChecked(checkedNodes) {
    var $nodeIds = $(".nodeId");
    var nodeId1 = null;
    var nodeId2 = null;
    for(var i=0,length1=$nodeIds.length;i<length1;++i){
        nodeId1 = $nodeIds[i].value;
        for(var j=0,length2=checkedNodes.length;j<length2;++j){
            nodeId2 = checkedNodes[j]["NODE_ID"];

            if(nodeId1 == nodeId2){
                $nodeIds[i].checked=true;
            }
        }
    }
}

/**
 * 新增和修改提交
 * @param e
 */
function onSubmit(e){
    if(JsVar[systemVar.ACTION] == systemVar.EDIT){
        saveCluster("update");
        return;
    }
    saveCluster("add");
}

/**
 * 取消
 */
function onCancel() {
    closeWindow();
}