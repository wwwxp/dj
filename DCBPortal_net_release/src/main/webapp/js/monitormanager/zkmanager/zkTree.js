var zTree, rMenu;
var clusterName;
var setting = {
    async: {
        enable: true,
        url: getUrl,
        autoParam: [ "id", "name" ],
        otherParam: {},
        dataFilter: filter
    },
    view: {
        expandSpeed: "fast",
        selectedMulti: false
    },
    data: {
        simpleData: {
            enable: true,
            idKey: "id",
            pIdKey: "pid",
            rootPId: "root"
        }
    },
    callback: {
        onClick: onClick,
        beforeAsync: beforeAsync,  
        onAsyncSuccess: onAsyncSuccess
    }
};


///////////////////////所有节点展开START/////////////////////////////
function beforeAsync() {  
    curAsyncCount++;  
}  
  
function onAsyncSuccess(event, treeId, treeNode, msg) {  
    curAsyncCount--;  
    if (curStatus == "expand") {  
        expandNodes(treeNode.children);  
    } else if (curStatus == "async") {  
        asyncNodes(treeNode.children);  
    }  

    if (curAsyncCount <= 0) {  
        curStatus = "";  
    }  
}  

var curStatus = "init", curAsyncCount = 0, goAsync = false;  
function expandAll() {  
    if (!check()) {  
        return;  
    }  
    var zTree = $.fn.zTree.getZTreeObj("zkTree");  
    if (zTree != null) {
    	var allNodes = zTree.getNodes();
        expandNodes(allNodes);  
        if (!goAsync) {  
            curStatus = "";  
        }
    }
}  
function expandNodes(nodes) {  
    if (!nodes) return;  
    curStatus = "expand";  
    var zTree = $.fn.zTree.getZTreeObj("zkTree");  
    for (var i=0, l=nodes.length; i<l; i++) {  
        zTree.expandNode(nodes[i], true, false, false);//展开节点就会调用后台查询子节点  
        if (nodes[i].isParent && nodes[i].zAsync) {  
            expandNodes(nodes[i].children);//递归  
        } else {  
            goAsync = true;  
        }  
    }  
}  

function check() {  
    if (curAsyncCount > 0) {  
        return false;  
    }  
    return true;  
} 
///////////////////////所有节点展开END/////////////////////////////



function Node(id, pid, name, isParent) {
    this.id = id;
    this.pid = pid;
    this.name = name;
    this.isParent = isParent;
};

function getUrl(treeId, treeNode) {
    return Globals.baseActionUrl.MONITOR_ACTION_ZOOKEEPER_TREE_NODE_URL+"?path=" + treeNode.id+"&clusterName="+clusterName;
}

function filter(treeId, parentNode, responseData) {
    if (!responseData)
        return null;
    var childNodes = [];
    var nodes = responseData;
    var length = nodes.length;
    for (var i = 0; i < length; i++) {
        var childNode = nodes[i];
        childNode.pid = parentNode.id;
        childNode.isParent = childNode.parent;
        childNodes.push(childNode);
    }
    return childNodes;
}

//GET zookeeper node data
function getData(path) {
    clusterName = clusterName;
    $.ajax({
        url: Globals.baseActionUrl.MONITOR_ACTION_ZOOKEEPER_TREE_NODE_DATA_URL,
        type: "get",
        dataType: "json",
        contentType: "application/json;charset=utf-8",
        data: {
            path: path,
            clusterName:clusterName
        },
        success: function (data) {
            $("#content").val(data.data);
        },
        error: function (data) {
           alert("zookeeper获取失败！");
        }
    });
}

function onClick(event, treeId, treeNode, clickFlag) {
    getData(treeNode.id);
}

// show cluster root
function showZKRoot(clusterName, showNodeList, expandAll) {
	clusterName=clusterName;
    var treeNodes = [];
    $.ajax({
        url: Globals.baseActionUrl.MONITOR_ACTION_ZOOKEEPER_TREE_NODE_URL,
        type: "get",
        dataType: "json",
        contentType: "application/json;charset=utf-8",
        data: {
            path: "/",
            clusterName: clusterName
        },
        success: function (data) {
            var nodes = data;
            if (!nodes){
                $("#zkTree").html("zookeeper数据获取失败！");
                return;
            }

            for (var i = 0; i < nodes.length; i++) {
                var item = nodes[i];
                if (showNodeList != null && showNodeList != undefined) {
                	for (var j=0; j<showNodeList.length; j++) {
                		if (item["name"] == showNodeList[j]["nodeName"]) {
                			treeNodes.push(new Node(item.id, item.pid, item.name, item.parent));
                			break;
                		}
                	}
                } else {
                	treeNodes.push(new Node(item.id, item.pid, item.name, item.parent));
                }
            }
            if (nodes.length < 1)
                $("#zkTree").html("zookeeper数据获取失败！");
            else {
                $.fn.zTree.init($("#zkTree"), setting, treeNodes);
                zTree = $.fn.zTree.getZTreeObj("zkTree");
            }
        },
        error: function (data) {
            $("#zkTree").html("zookeeper数据获取失败！");
        }
    });
}

