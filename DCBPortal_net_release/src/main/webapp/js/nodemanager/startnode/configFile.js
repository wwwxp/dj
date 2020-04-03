//定义变量， 通常是页面控件和参数
var JsVar = new Object();
var versionInfo=new Object;
//初使化
$(document).ready(function () {
    mini.parse();

    JsVar["fileTree"]=mini.get("fileTree");
    JsVar["node_name"]=mini.get("node_name");
    JsVar["fileContent"]=mini.get("fileContent");

    intFileTextArea();
});

function onLoadComplete(data) {
    versionInfo["NODE_ID"]=data["NODE_ID"];
    versionInfo["VERSION"]=data["VERSION"];
    loadFileTree(versionInfo);
}

//树的加载
function loadFileTree(param) {

    treeLoad(JsVar["fileTree"], null, param, Globals.ctx+"/startNode/loadFileTree");
}

//树的查找
function searchTree() {
    var nodeName=JsVar["node_name"].getValue().trim().toLowerCase();
    if(nodeName==""){
        JsVar["fileTree"].clearFilter();
    }else{
        JsVar["fileTree"].filter(function (node) {
            if(node.fileName.indexOf(nodeName)!=-1){
                return true;
            }
        })

    }

}

//对文件内容显示区域的样式进行美化
function intFileTextArea() {
    JsVar["editor"] = CodeMirror.fromTextArea(document.getElementById("fileContent"), {
        lineNumbers: true,
        lineWrapping: true,
        foldGutter: true,
        extraKeys: {"Ctrl": "autocomplete"},
        styleActiveLine: true,
        mode:  {name: "javascript", globalVars: true},
        gutters: ["CodeMirror-linenumbers", "CodeMirror-foldgutter"]
    });
    JsVar["editor"].foldCode(CodeMirror.Pos(0, 0));

    var height=document.documentElement.clientHeight;
    JsVar["editor"].setSize("100%", "100%");
}

//根据选中的节点，加载文件内容
function getFileContent() {
    var node=JsVar["fileTree"].getSelectedNode();
    var parentNodes=JsVar["fileTree"].getAncestors(node);


    var relative_file_path="";

    for (var index=0;index<parentNodes.length;++index) {

        if (!parentNodes[index]["fileName"]) {
            break;
        }

        relative_file_path += parentNodes[index]["fileName"];
        relative_file_path += "/";
    }
    relative_file_path += node["fileName"];
    var nodeId = JsVar["fileTree"].getRootNode()["children"][0]["nodeId"];
    var param={

        NODE_ID:nodeId,
        RELATIVE_FILE_PATH:relative_file_path

    };
    if(JsVar["fileTree"].isLeaf(node)){

        getJsonDataByPost(Globals.ctx+"/startNode/getFileContent",param,"启停程序-配置文件内容加载",
            function success(result) {
                if(result["success"]) {
                    JsVar["editor"].setValue(result["content"]);

                    document.getElementById("tips").innerText="所在目录："+result["hostIp"]+" -> "+result["filePath"];
                }else{
                    showWarnMessageTips(result["content"]);
                }

        });
    }
}
