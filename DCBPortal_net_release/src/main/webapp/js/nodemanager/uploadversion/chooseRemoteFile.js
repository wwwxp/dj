/**
 * author：wxp
 * time：2019/12/19
 */
var JsVar = new Object();
$(document).ready(function () {
    mini.parse();
	JsVar["dir_tree"] = mini.get("dir_tree");
    JsVar["rootPath"] = null;
    //选中的文件（含路径）
    JsVar["absPath"] = null;
    JsVar["fileHistory"] = new FileHistory();
    document.getElementById("fileList").onclick = clickFile;
});

/**
 * 点击文件/文件夹
 * @param event
 */
function clickFile(event) {
    event = event || window.event;
    var pathInfo = $(event.target).children().text().split("\u0001");
    if(event.target.tagName === 'LI') {
        var $target = $(event.target);
        if(pathInfo[1] === 'D'){
            getFileList(pathInfo[0]);
        }else{
            $(".fileItem").css({backgroundColor:"transparent",color:"red"});
            if(JsVar["absPath"] === pathInfo[0]){
                $target.css({backgroundColor:"transparent",color:"red"});
                JsVar["absPath"] = "";
            }else{
                $target.css({backgroundColor:"green",color:"white"})
                JsVar["absPath"] = pathInfo[0];
            }
        }
    }
}

/**
 * 存储浏览历史
 * @constructor
 */
function FileHistory() {
    this.fileLists = new Array();
    this.uris = new Array();
    this.index = -1;
}

FileHistory.prototype.push = function (fileList,uri) {
    this.index++;
    this.fileLists[this.index] = fileList;
    this.uris[this.index] = uri;
}

FileHistory.prototype.back = function () {
    this.index--;
    if(this.index<0) {
        this.index = 0;
    }
    this.fileLists.length = this.index + 1;
    this.uris.length = this.index + 1;

    $("#fileList").html(this.fileLists[this.index]);
    $("#uri").text(this.uris[this.index]);
}

/**
 * 点击后退键
 */
function dirBack() {
    JsVar["fileHistory"].back();
}

/**
 * 加载目录树、所有文件列表
 */
function loadFileTree() {
    var params = {
        page_type:JsVar["page_type"],
    }
    getJsonDataByPost(Globals.ctx + "/nodeopt/getRemoteFileTree", params, null, function success(data) {
        JsVar["rootPath"]=data["dirTree"][0]["dirPath"];
        JsVar["dir_tree"].setData(data["dirTree"]);
        JsVar["fileList"] = data["fileList"];
        getFileList(null);
    });
    hideLoadMask();
}

/**
 * 获得path对应的文件列表
 * @param path
 */
function getFileList(path) {

    //加载文件列表
    var $fileList = $("#fileList");
    var dirItem = "<li class='dirItem' >" +
        "${fileName}" +
        "<span style='display: none;'>${filePath}\u0001${fileType}</span>" +
        "</li>";
    var fileItem = "<li class='fileItem'>" +
        "${fileName}" +
        "<span style='display: none;'>${filePath}\u0001${fileType}</span>" +
        "</li>";

    $fileList.html("");
    var childFileList = null;
    if(path == null){
        childFileList = JsVar["fileList"][0]["childFileList"];
    }else{
        childFileList = findFileListByPath(path,JsVar["fileList"]);
    }
    if (childFileList != null) {
        for (var i = 0, length = childFileList.length; i < length; ++i) {
            if (childFileList[i]["file"]["fileType"] === 'D') {
                $fileList.prepend(dirItem.replace("${filePath}", childFileList[i]["file"]["targetPath"])
                    .replace("${fileName}", childFileList[i]["file"]["fileName"])
                    .replace("${fileType}", childFileList[i]["file"]["fileType"]));
            } else {
                $fileList.append(fileItem.replace("${filePath}", childFileList[i]["file"]["targetPath"])
                    .replace("${fileName}", childFileList[i]["file"]["fileName"])
                    .replace("${fileType}", childFileList[i]["file"]["fileType"]));
            }
        }
        var curPath = path;
        if (curPath == undefined) {
            curPath = JsVar["fileList"][0]["file"]["targetPath"];
        }
        JsVar["fileHistory"].push($fileList.html(), curPath);
        $("#uri").text(curPath);
    }else{
        showErrorMessageTips("获取子文件列表错误");
    }
}

function findFileListByPath(path,fileList){
    var targetList = null;
    for(var i=0,length=fileList.length;i<length;++i){
        if(fileList[i]["file"]["targetPath"] === path){
            targetList=fileList[i]["childFileList"];
            break;
        }else if(path.startWith(extraPath(fileList[i]["file"]["targetPath"]))){
            return findFileListByPath(path,fileList[i]["childFileList"]);
        }
    }
    return targetList;
}

function extraPath(path) {
    if(path.endWith("/")){
        return path;
    }else{
        return path + "/";
    }
}

/**
 * 点击目录树的节点
 * @param sender
 */
function onClickTreeNode(sender) {
    getFileList(sender.node["dirPath"]);
}

function onLoadComplete(data) {
	JsVar["page_type"] = data;
    loadFileTree();
}

/**
 * 目录树的查找
 */
function searchDirOnTree() {
    var key = mini.get("DIR_NAME").getValue().toLowerCase();

    var dirTree = JsVar["dir_tree"];

    if(key==""){
        dirTree.clearFilter();
    }else{
        dirTree.filter(function (node) {
            if(node["dirName"].toLowerCase() == key){
                dirTree.expandPath(node);
                return true;
            }
        });
    }
}

/**
 * 提交
 */
function onSubmit(){
	if(!isEmptyObject(JsVar["absPath"])){
		JsVar["fileList"]= null;
		var fileInfo = {
            fileRelPath:JsVar["absPath"].replace(JsVar["rootPath"],""),
            value:JsVar["absPath"].substring((JsVar["absPath"].lastIndexOf("/")+1),JsVar["absPath"].length)
        };
		window.Owner.setRemoteFile(fileInfo);
		closeWindow();
	}else{
        showWarnMessageTips("请选择文件（或点击取消）")
    }
}
 
