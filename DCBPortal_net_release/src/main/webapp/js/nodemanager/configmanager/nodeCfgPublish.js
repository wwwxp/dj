//定义变量， 通常是页面控件和参数
var page_type = "SERVICE";

var JsVar = new Object();
var Tree = new Object();
var params = new Object();
var busVersionInfo = new Object();
var busVersionName = new Object();
/**
 * 初始化
 */
$(document).ready(function () {
    mini.parse();
    Tree["fileTree"] = mini.get("fileTree");

    //节点类型
    JsVar["NODE_TYPE"] = mini.get("NODE_TYPE");
    //部署版本
    JsVar["DEP_VERSION"] = mini.get("DEP_VERSION");
    //节点
    JsVar["DEP_NODE"] = mini.get("DEP_NODE");

    //初始化文本栏
    initTextContent();
    // loadPage(null);
    loadNodeTypeCombobox();

    // $(window).resize(resizePage);

});

/**
 * 新进入Tab页签数据
 * @param data
 */
function loadPage(data) {
    mini.parse();
    // JsVar["PACKAGE_TYPE"] = data["CONFIG_VALUE"];
    // JsVar["PACKAGE_TYPE_NAME"] = data["CONFIG_NAME"];

    //获取sftp服务器下的文件
    loadFilesUnderServer(page_type);
    //过滤掉不存在的业务版本
    filterTree();

}


/**
 * 加载主程序类型下拉框
 * */
function loadNodeTypeCombobox() {
    comboxLoad(JsVar["NODE_TYPE"], null, "", Globals.ctx + "/nodeopt/queryNoteTypeConfig");
}


/**
 * 获取ftp服务器下的文件列表
 */
function loadFilesUnderServer(page_type) {
    showLoadMask("加载文件目录");
    var nodeTypeId = JsVar["NODE_TYPE"].getValue();
    var nodeId = JsVar["DEP_NODE"].getValue();
    var version = JsVar["DEP_VERSION"].getValue();
    if (isNull(nodeTypeId) || isNull(version)) {
        showWarnMessageTips("必须选择节点类型、部署版本");
    }
    // showMessageTips("加载目录树");
    var param = {
        "NODE_TYPE_ID": nodeTypeId,
        "NODE_ID": nodeId,
        "VERSION": version
    }
    //查询文件，并加载
    treeLoad(Tree["fileTree"], null, param, Globals.ctx + "/nodeCfgPub/queryNodeDeployCfgFileDir", null);
}

/**
 * 节点渲染 （模板节点渲染）
 * @param e
 */
function distinIsUsed(e) {
    var currNode = e.node;
    //设置图标
    if (currNode["fileType"] == "D") {
        e.iconCls = "mini-tree-expand mini-tree-folder";
        // for (var i = 0; i < JsVar["BUS_LIST"].length; i++) {
        //     if (JsVar["BUS_LIST"][i]["CLUSTER_NAME"] == currNode["fileName"]) {
        //         var parentNode = Tree["fileTree"].getParentNode(currNode);
        //         var firstNode = Tree["fileTree"].getParentNode(parentNode);
        //         if (firstNode["fileName"] == "release") {
        //             e.iconCls = "mini-tree-templet";
        //         }
        //         break;
        //     }
        // }
    }
}

/**
 * 节点过滤
 */
function filterTree() {
    var version_busPack = new Object();
    //拿到版本和其下业务包
    getJsonDataByPost(Globals.baseActionUrl.FRAME_QUERY_FOR_LIST_URL, null, "业务配置--查询版本下的业务包名称",
        function (result) {
            if (result.length > 0) {
                $.each(result, function (i, item) {
                    //数据拼接封装  ocs_v0.0.1-rent   给下面版本号下是否存在该种业务做判断
                    version_busPack[item.NAME + '-' + item.CLUSTER_TYPE] = 1;
                    //数据封装busVersionName[ocs_v0.0.1]="描述信息....."  业务版本号/描述信息封装
                    busVersionName[item.NAME] = "上传时间：" + item.CRT_DATE + "\n" + item.DESCRIPTION;
                    //存name/fileName/version
                    busVersionInfo = result;
                });
            }
        }, "deployTask.queryVersionAndBusPackage", null, false);
}

/**
 * 目录是IP
 */
function isIp(name) {
    //判断是否是主机格式
    var reg = new RegExp("^[0-9]{1,3}.[0-9]{1,3}.[0-9]{1,3}.[0-9]{1,3}", "ig");
    var isIp = reg.test(name) ? true : false;
    return isIp;
}

/**
 * 初始化text
 */
function initTextContent() {
    if (JsVar["editor"]) {
        $("#content").parent().children(".CodeMirror").remove();
    }
    if (JsVar["formatType"] == "xml") {
        var mixedMode = {name: "htmlmixed"};
        JsVar["editor"] = CodeMirror.fromTextArea(document.getElementById("content"), {
            lineNumbers: true,
            lineWrapping: true,
            foldGutter: true,
            extraKeys: {"Ctrl": "autocomplete"},
            styleActiveLine: true,
            mode: mixedMode,
            gutters: ["CodeMirror-linenumbers", "CodeMirror-foldgutter"]
        });
        JsVar["editor"].foldCode(CodeMirror.Pos(0, 0));
    } else if (JsVar["formatType"] == "yaml" || JsVar["formatType"] == "yml") {
        JsVar["editor"] = CodeMirror.fromTextArea(document.getElementById("content"), {
            lineNumbers: true,
            lineWrapping: true,
            foldGutter: true,
            extraKeys: {"Ctrl": "autocomplete"},
            styleActiveLine: true,
            mode: {name: "yaml", globalVars: true},
            gutters: ["CodeMirror-linenumbers", "CodeMirror-foldgutter"]
        });
        JsVar["editor"].foldCode(CodeMirror.Pos(0, 0));
    } else {
        JsVar["editor"] = CodeMirror.fromTextArea(document.getElementById("content"), {
            lineNumbers: true,
            lineWrapping: true,
            foldGutter: true,
            extraKeys: {"Ctrl": "autocomplete"},
            styleActiveLine: true,
            mode: {name: "javascript", globalVars: true},
            gutters: ["CodeMirror-linenumbers", "CodeMirror-foldgutter"]
        });
        JsVar["editor"].foldCode(CodeMirror.Pos(0, 0));
    }

    JsVar["editor"].on("changes", function (Editor, changes) {
        JsVar["isEditting"] = true;
        //if(params["fileName"]=="sp_switch.xml"){
        //	$("#saveFile").css("display", "none");
        //}else{
        $("#saveFile").css("display", "block");
        //}
    });
    $(window).resize(function () {
        resizeEditor();
    });

    resizeEditor();
}

/**
 * 树节点点击事件
 * @param e
 */
function onClickTreeNode(e) {
    var isFile = e.node.file;
    JsVar["cancelChanges"] = false;
    if (JsVar["isEditting"]) {
        showConfirmMessageAlter("文件【" + params["fileName"] + "】未保存，是否继续？", function ok() {
            JsVar["isEditting"] = false;
            onClickTreeNode(e);
        }, function cancel() {
            JsVar["isEditting"] = true;
            Tree["fileTree"].selectNode(JsVar["SEL_NODE"]);
        });
        return;
    }

    //保存选中的节点，后续保存另外文件使用
    JsVar["SEL_NODE"] = e.node;

    //存全局变量
    params["fileName"] = e.node.fileName;
    params["filePath"] = e.node.filePath;
    params["isFile"] = e.node.file;

    if (params["fileName"].lastIndexOf(".xml") > -1) {
        if (JsVar["formatType"] != "xml") {
            JsVar["formatType"] = "xml";
            initTextContent();
        }
    } else if (params["fileName"].lastIndexOf(".yaml") > -1 || params["fileName"].lastIndexOf(".yml") > -1) {
        if (JsVar["formatType"] != "yaml") {
            JsVar["formatType"] = "yaml";
            initTextContent();
        }
    } else if (params["fileName"].lastIndexOf(".properties") > -1) {
        if (JsVar["formatType"] != "properties") {
            JsVar["formatType"] = "properties";
            initTextContent();
        }
    } else {
        if (JsVar["formatType"] != "defaul") {
            JsVar["formatType"] = "defaul";
            initTextContent();
        }
    }
    var parentNode = Tree["fileTree"].getParentNode(e.node);
    params["parentNodeName"] = parentNode.fileName;

    //是文件,获取文件内容
    if (isFile == true) {
        getJsonDataByPost(Globals.ctx + "/nodeCfgPub/getFileContent", e.node, "配置修改-FTP/SFTP获取文件内容",
            function (result) {
                if (result != null) {

                    if (result["REAL_DEPLOY_PATH"] != undefined) {
                        $("#tips").html("当前文件在远程主机初始目录: " + result["REAL_DEPLOY_PATH"]);
                    } else {
                        $("#tips").html("当前文件在远程主机不存在！");
                    }

                    var con = result.fileContent;
                    JsVar["editor"].setValue(con);
                    JsVar["isEditting"] = false;
                    //$("#saveFile").css("display", "none");
                }
            });

    } else {
        JsVar["editor"].setValue('');
        var DESCRIPTION = JsVar["SEL_NODE"]["desc"] == undefined ? "" : JsVar["SEL_NODE"]["desc"];
        if (JsVar["SEL_NODE"]["targetPath"] != undefined) {
            $("#tips").html("当前文件在远程主机初始目录: " + JsVar["SEL_NODE"]["targetPath"]);
        }
        // for(var key in busVersionName){
        // 	if(params["fileName"]==key){
        // 		//如果该节点是版本号目录，则查询显示该版本号的描述信息
        // 		DESCRIPTION=busVersionName[key];
        // 	}
        //
        // }
        DESCRIPTION = "版本描述信息\n" + DESCRIPTION;
        JsVar["editor"].setValue(DESCRIPTION);

        JsVar["isEditting"] = false;
        //$("#saveFile").css("display", "none");
        JsVar["cancelChanges"] = true;
        JsVar["editor"].on('beforeChange', function (cm, change) {
            if (JsVar["cancelChanges"]) {
                change.cancel();
            }
        });

    }
    resizeEditor();
}

function resizeEditor() {
    var height = document.documentElement.clientHeight;
    JsVar["editor"].setSize("100%", height - 165);
}


/**
 * 集群发布
 * @param e
 */
function saveAllFile(e) {
    saveFile(e, true);
}

/**
 * 发布单个
 * @param e
 */
function saveOneFile(e) {
    saveFile(e, false);
}

/**
 * 保存文件
 * @param e
 */
function saveFile(e, isPubAll) {
    //配置文件修改后内容

    var node = Tree["fileTree"].getSelectedNode();
    if (node == null || node.directory == true) {
        return;
    }
    var newContent = JsVar["editor"].getValue() == null ? "" : JsVar["editor"].getValue();
    newContent = newContent.replace(/\+/g, "%2B");
    //newContent = newContent.replace(/\+/g, "%2B").replace(/\&/g, "%26");
    var params = {
        nodeparam: JSON.stringify(node),
        isPublishAll: isPubAll,
        fileContent: newContent
    };
    showConfirmMessageAlter("文件【" + node["fileName"] + "】" + (isPubAll ? "版本配置同步" : "") + "保存，是否继续？", function () {
        getJsonDataByPost(Globals.ctx + "/nodeCfgPub/updateCfgAndPublish", params, "配置修改-业务配置-分发修改后的文件",
            function (result) {
                if (result.success) {
                    showTip(result.success);
                    JsVar["isEditting"] = false;
                } else {
                    showMessageTips("操作失败！");
                }
            });
    });
}

/**
 * 鼠标右键触发事件
 * @param e
 */
function serviceOnBeforeOpen(e) {
    var node = Tree["fileTree"].getSelectedNode();
    if (!node) {//当没有选择节点， 则阻止浏览器的右键菜单
        e.htmlEvent.preventDefault();
        e.cancel = true;
        return;
    }
    var isUsed = node.isUsed;
    if (node) {
        //先清空右边编辑区
        JsVar["editor"].setValue('');

        JsVar["isEditting"] = false;
        //$("#saveFile").css("display", "none");
        JsVar["cancelChanges"] = true;
        JsVar["editor"].on('beforeChange', function (cm, change) {
            if (JsVar["cancelChanges"]) {
                change.cancel();
            }
        });
        //节点是文件夹
        if (node.fileType == "D" && isUsed != true) {//节点是文件夹
            $("#addFile").css("display", "block");
            $("#addFolder").css("display", "block");
            $("#delFile").css("display", "none");
            $("#delFolder").css("display", "block");
            if (Tree["fileTree"].getLevel(node) != 3 || node.fileName != 'other') {
                $("#batchAddFile").css("display", "none");
            } else {
                $("#batchAddFile").css("display", "block");
            }
        } else if (node.fileType == "F") {//节点是文件
            $("#delFile").css("display", "block");
            $("#delFolder").css("display", "none");
            $("#addFile").css("display", "none");
            $("#addFolder").css("display", "none");
            $("#batchAddFile").css("display", "none");
        } else {//阻止右键菜单
            e.htmlEvent.preventDefault();
            e.cancel = true;
            return;
        }
    }
}

/**
 * 同步配置文件夹
 */
function synConfig(event) {
    var curNode = Tree["fileTree"].getSelectedNode();
    var nodeIds = new Array();
    var treeData = Tree["fileTree"].getData();
    for(var i=0,length=treeData.length;i<length;++i){
        nodeIds.push(treeData[i]["nodeId"]);
    }
    var param = {
        NODE_TYPE_ID:curNode["nodeTypeId"],
        VERSION:curNode["version"],
        NODE_ID:curNode["nodeId"],
        cfgParentPath:curNode["filePath"],
        cfgPath:curNode["targetPath"],
        NODE_IDS:nodeIds
    };
    getJsonDataByPost(Globals.ctx + "/nodeCfgPub/synConfig",param,"配置发布-同步配置",function success(data) {
            if(data!=null){
                showTip(data["transInfo"]);
            }else{
                showTip("失败");
            }
    });
}

/**
 * 删除文件、文件夹
 * @param type
 */
function serviceDelFile(type) {
    showWarnMessageTips("当前版本不支持删除操作！");
    return;
    var node = Tree["fileTree"].getSelectedNode();
    var param = new Object();
    param["delFileType"] = type;
    param.fileName = node.fileName;
    param.filePath = node.targetPath;
    param.fileType = node.fileType;
    param.page_type = page_type;
    var file_word = "删除" + param.fileName + "文件";
    var folder_word = "删除" + param.fileName + "文件夹";
    var title = (type == "file") ? file_word : folder_word;
    showConfirmMessageAlter("确认" + title + "?", function ok() {
        getJsonDataByPost(Globals.baseActionUrl.CONFIGURE_ACTION_DELETE_FILE_URL, param, "配置修改-业务配置-分发修改后的文件",
            function (result) {
                showMessageTips(title + "成功！");
                Tree["fileTree"].removeNode(node);
            });
    });
}


/**
 * 页面尺寸变化时重新渲染页面
 */
function resizePage() {
    var height = document.documentElement.clientHeight;
    JsVar["editor"].setSize("100%", height - 71);
};

/**
 * 刷新文件目录树
 */
function refresh() {
    //初始化
    JsVar["editor"].setValue("");
    JsVar["isEditting"] = false;
    Tree["fileTree"].uncheckAllNodes();
    $("#tips").html("");

    Tree["fileTree"].loadData(null);


    mini.get("node_name").setValue("");
    //获取sftp服务器下的文件
    loadFilesUnderServer(page_type);
    //过滤掉不存在的业务版本
    filterTree();
    Tree["fileTree"].expandLevel(3);
}

/**
 * 搜索树节点
 */
function searchTree() {
    var node_name = mini.get("node_name").getValue();
    if (node_name == "") {
        Tree["fileTree"].clearFilter();
    } else {
        node_name = node_name.toLowerCase();
        Tree["fileTree"].filter(function (node) {
            var text = node.fileName ? node.fileName.toLowerCase() : "";
            if (text.indexOf(node_name) != -1) {
                return true;
            }
        });
    }
}

function onNodeTypeChanged(e) {
    var depversionCombo = JsVar["DEP_VERSION"];
    var nodeTypeId = JsVar["NODE_TYPE"].getValue();
    depversionCombo.setValue("");
    JsVar["DEP_NODE"].setValue("");
    JsVar["DEP_NODE"].setData(null);
    depversionCombo.setData(null);
    if (nodeTypeId == null || nodeTypeId == '') {
        return;
    }
    loadDeployVersion();
}

function onDepVersionFocus(e) {
    var nodeTypeId = JsVar["NODE_TYPE"].getValue();
    if (nodeTypeId == null || nodeTypeId == '') {
        return;
    }
    loadDeployVersion()
}

function loadDeployVersion() {
    var depversionCombo = JsVar["DEP_VERSION"];
    var nodeTypeId = JsVar["NODE_TYPE"].getValue();
    if (nodeTypeId == null || nodeTypeId == '') {
        return;
    }
    var pram = {"NODE_TYPE_ID": nodeTypeId};
    comboxLoad(depversionCombo, pram, "", Globals.ctx + "/nodeCfgPub/queryDeployVersion", null);
}

function onDepVersionChanged(e) {
    var depNodeComb = JsVar["DEP_NODE"];
    var nodeTypeId = JsVar["NODE_TYPE"].getValue();
    var version = JsVar["DEP_VERSION"].getValue();
    depNodeComb.setValue("");
    depNodeComb.setData(null);
    if (nodeTypeId == null || nodeTypeId == '') {
        return;
    }
    var pram = {"NODE_TYPE_ID": nodeTypeId, "VERSION": version};
    comboxLoad(depNodeComb, pram, "", Globals.ctx + "/nodeCfgPub/queryNodeDeployInfo", null);
}

/**
 *
 * @param index
 */
function showTip(params) {
    var re1 = new RegExp("\\[成功\\]", "g"); //定义正则表达式
    var re2 = new RegExp("\\[失败\\]", "g"); //定义正则表达式
    var re3 = new RegExp("\\r\\n", "g"); //定义正则表达式
//第一个参数是要替换掉的内容，第二个参数"g"表示替换全部（global）。

    params = params.replace(re1, "<span style='background: green;color: white'>[成功]</span>"); //第一个参数是正则表达式。
    params = params.replace(re2, "<span style='background: red;color: white'>[失败]</span>"); //第一个参数是正则表达式。

    var paramsHtml = "<div style='white-space:pre-wrap;letter-spacing:0.05em;line-height:25px;border-bottom:2px solid #DEE5F4;height:255px;width:99%;overflow:auto;'>" + params + "</div>";
    var options = {
        title: "运行结果",
        width: 800,
        height: 700,
        buttons: ["ok"],
        iconCls: "",
        html: paramsHtml,
        callback: function (action) {

        }
    }
    mini.showMessageBox(options);
}
