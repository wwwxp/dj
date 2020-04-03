/**
*文件上传类
*/

//附件表格
var attachDg;
var swfu;
function createSwfUpload(uploadSwfPath,uploadBtnRender,btnImageUrl,btnText,btnTextStyle,fileSize,fileTypes,fileTypesDesc,fileUploadLimit,uploadUrl,attachDg){
attachDg=attachDg;
swfu=new SWFUpload({
					upload_url: uploadUrl,
					
					// File Upload Settings
					file_size_limit : fileUploadLimit+" MB",	// 1000MB
					file_types : fileTypes,//设置可上传的类型
					file_types_description : fileTypesDesc,
					file_upload_limit : fileUploadLimit,
									
					file_queue_error_handler : fileQueueError,//选择文件后出错
					file_dialog_complete_handler : fileDialogComplete,//选择好文件后提交
					file_queued_handler : fileQueued,
					upload_progress_handler : uploadProgress,
					upload_error_handler : uploadError,
					upload_success_handler : uploadSuccess,
					upload_complete_handler : uploadComplete,
	
					// Button Settings
					button_image_url : btnImageUrl,
					button_placeholder_id : uploadBtnRender,
					button_width: 60,
					button_height: 24,
					button_text : btnText,
					button_text_style : btnTextStyle,
					button_text_top_padding: 8,
					button_text_left_padding: 0,
					button_window_mode: SWFUpload.WINDOW_MODE.TRANSPARENT,
					button_cursor: SWFUpload.CURSOR.HAND,
					// Flash Settings
					flash_url : uploadSwfPath,
					// Debug Settings
					debug: false  //是否显示调试窗口
				});
}


//渲染状态
function statusRenderer(e){
	return e.record["STATUS"];
}

//渲染附件表格的删除按钮
function renderDelButton(e){
 	return '<a class="Delete_Button" href="javascript:deleteAttach(\'' + e.record.ATTACH_ID + '\')">删除</a>';
}

//删除附件
function deleteAttach(attachId){
	attachDg.removeRow(attachDg.getSelected(),false);
	swfu.cancelUpload(attachId,false);
}

//判断浏览器的类型
function getBrowserType() {  
   var browser = {};  
   var userAgent = navigator.userAgent.toLowerCase();  
   var s;  
   (s = userAgent.match(/msie ([\d.]+)/))  
           ? browser.ie = s[1]  
           : (s = userAgent.match(/firefox\/([\d.]+)/))  
                   ? browser.firefox = s[1]  
                   : (s = userAgent.match(/chrome\/([\d.]+)/))  
                            ? browser.chrome = s[1]  
                            : (s = userAgent.match(/opera.([\d.]+)/))  
                                    ? browser.opera = s[1]  
                                    : (s = userAgent  
                                            .match(/version\/([\d.]+).*safari/))  
                                            ? browser.safari = s[1]  
                                            : 0;  
    var oType = "";  
    if (browser.ie) {  
    	oType = "ie";
    } else if (browser.firefox) {  
    	oType = "firefox";
    } else if (browser.chrome) {  
    	oType = "chrome";
    } else if (browser.opera) {  
    	oType = "opera";
    } else if (browser.safari) {  
    	oType = "safari"; 
    } else {  
    	oType = '未知浏览器';  
    }  
    return oType;  
} 


function fileQueueError(file, errorCode, message) {
	try {
		var imageName = "<font color='red'>文件上传错误</font>";
		var errorName = "";
		if (errorCode === SWFUpload.errorCode_QUEUE_LIMIT_EXCEEDED) {
			errorName = "You have attempted to queue too many files.";
		}

		if (errorName !== "") {
			alert(errorName);
			return;
		}
		
		switch (errorCode) {
		case SWFUpload.QUEUE_ERROR.ZERO_BYTE_FILE:
			imageName = "<font color='red'>文件大小为0</font>";
			break;
		case SWFUpload.QUEUE_ERROR.FILE_EXCEEDS_SIZE_LIMIT:
			imageName = "<font color='red'>文件大小超过限制</font>";
			break;
		case SWFUpload.QUEUE_ERROR.ZERO_BYTE_FILE:
		case SWFUpload.QUEUE_ERROR.INVALID_FILETYPE:
		default:
			alert(message);
			break;
		}
		addReadyFileInfo(file.id,file.name,file.size,file.type,imageName,"无法上传");

	} catch (ex) {
		alert(ex);
		//this.debug(ex);
	}
}

/**
 * 当文件选择对话框关闭消失时，如果选择的文件成功加入上传队列，
 * 那么针对每个成功加入的文件都会触发一次该事件（N个文件成功加入队列，就触发N次此事件）。
 * @param {} file
 * id : string,			    // SWFUpload控制的文件的id,通过指定该id可启动此文件的上传、退出上传等
 * index : number,			// 文件在选定文件队列（包括出错、退出、排队的文件）中的索引，getFile可使用此索引
 * name : string,			// 文件名，不包括文件的路径。
 * size : number,			// 文件字节数
 * type : string,			// 客户端操作系统设置的文件类型
 * creationdate : Date,		// 文件的创建时间
 * modificationdate : Date,	// 文件的最后修改时间
 * filestatus : number		// 文件的当前状态，对应的状态代码可查看SWFUpload.FILE_STATUS }
 */
//上传附件
function uploadFile(){
	swfu.startUpload(); 
}
 
function fileQueued(file){
	var sameFileFlag=0;
	var attahData=attachDg.getData();
	//判断是否有重复文件名
	for(var i=0;i<attahData.length;i++){
		if(attahData[i]["ATTACH_NAME"]==file.name){
			sameFileFlag=1;
			break;
		}
	}
	//重复文件名不允许上传
	if(sameFileFlag==1){
		swfu.cancelUpload(file.id,false);
		return ;
	}
	addReadyFileInfo(file.id,file.name,file.size,file.type,"成功加载到上传队列","");
}
function fileDialogComplete(numFilesSelected, numFilesQueued) {
	try {
		if (numFilesQueued > 0) {
			//document.getElementById('btnCancel').disabled = "";
			//this.startUpload();
		}
	} catch (ex) {
		alert(ex);
		//this.debug(ex);
	}
}

//上传中
function uploadProgress(file, bytesLoaded) {

	try {
	//	var percent = Math.ceil((bytesLoaded / file.size) * 100);

	//	var progress = new FileProgress(file,  this.customSettings.upload_target);
	//	progress.setProgress(percent);
	//	if (percent === 100) {
	//		progress.setStatus("");//正在创建缩略图...
	//		progress.toggleCancel(false, this);
	//	} else {
	//		progress.setStatus("正在上传...");
	//		addFileInfo(file.id,"正在上传...");
	//		progress.toggleCancel(true, this);
	//	}
	} catch (ex) {
		alert(ex);
		//this.debug(ex);
	}
}
//上传成功
function uploadSuccess(file, serverData) {
	try {
		//var progress = new FileProgress(file);
		addFileInfo(file.id,"文件上传完成");
		var attachData=attachDg.getData();
		for(var i=0;i<attachData.length;i++){
			if(attachData[i]["ATTACH_ID"]==file.id){
				attachData[i]["ATTACH_ALIAS_NAME"]=serverData;
				break;
			}
		}
		attachDg.setData(attachData);
//		addFileId(file.id,serverData);
//		alert(serverData);
	} catch (ex) {
		alert(ex);
		//this.debug(ex);
	}
}
//添加文件标识
function addFileId(fileId,id){
	alert(fileId+"-------"+id);
	var row = document.getElementById(fileId);
	row.cells[4].innerHTML = id;
//	alert(row.cells[4].innerHTML);
}
//添加文件信息
function addFileInfo(fileId,message){
	var attahData=attachDg.getData();
	for(var i=0;i<attahData.length;i++){
		if(attahData[i]["ATTACH_ID"]==fileId){
			attahData[i]["STATUS"]= "<font color='green'>"+message+"</font>";
			break;
		}
	}
	attachDg.setData(attahData);
}
//选择文件后显示
function addReadyFileInfo(fileid,fileName,size,type,message,status){
	var attachObject=new Object();
	attachObject["ATTACH_NAME"]=fileName;
	attachObject["ATTACH_ID"]=fileid;
	attachObject["ATTACH_MESSAGE"]=message;
	attachObject["ATTACH_SIZE"]=size;
	attachObject["ATTACH_TYPE"]=type;
	if(status==null||status==""){
		status=message;
	}else{
		if(message!=null&&message!=""){
			status+="("+message+")";
		}
	}
	attachObject["STATUS"]=status;
	attachDg.addRow(attachObject,attachDg.getData().length);
}

function cancelUpload(){
	var infoTable = document.getElementById("infoTable");
	var rows = infoTable.rows;
	var ids = new Array();
	var row;
	if(rows==null){
		return false;
	}
	for(var i=0;i<rows.length;i++){
		ids[i] = rows[i].id;
	}	
	for(var i=0;i<ids.length;i++){
		deleteFile(ids[i]);
	}	
}

function deleteFile(fileId){
	//用表格显示
	var infoTable = document.getElementById("infoTable");
	var row = document.getElementById(fileId);
	var filePath = row.cells[4].innerHTML;
	//删除上传成功的文件
	$.ajax({
		type : 'post',
		url : "DeleteFileServlet",
		data : 'filePath='+filePath,
		success : function(data) { // 判断是否成功
			//处理被删除的节点
			infoTable.deleteRow(row.rowIndex);
			swfu.cancelUpload(fileId,false);
		},
		error:function(data){
			addFileInfo(fileId,"<font color='red'>删除文件夹出错</a>");
		}
	});
	
}

function uploadComplete(file) {
	try {
		/*  I want the next upload to continue automatically so I'll call startUpload here */
		if (this.getStats().files_queued > 0) {
			this.startUpload();
		} else {
			//var progress = new FileProgress(file,  this.customSettings.upload_target);
			//progress.setComplete();
			//progress.setStatus("<font color='red'>所有文件上传完毕!</b></font>");
			//progress.toggleCancel(false);
		}
	} catch (ex) {
		alert(ex);
		//this.debug(ex);
	}
}

function uploadError(file, errorCode, message) {
	var imageName =  "<font color='red'>文件上传出错!</font>";
	var progress;
	try {
		switch (errorCode) {
		case SWFUpload.UPLOAD_ERROR.FILE_CANCELLED:
			try {
				progress = new FileProgress(file,  this.customSettings.upload_target);
				progress.setCancelled();
				progress.setStatus("<font color='red'>取消上传!</font>");
				progress.toggleCancel(false);
			}
			catch (ex1) {
				alert(ex1);
				//this.debug(ex1);
			}
			break;
		case SWFUpload.UPLOAD_ERROR.UPLOAD_STOPPED:
			try {
				progress = new FileProgress(file,  this.customSettings.upload_target);
				progress.setCancelled();
				progress.setStatus("<font color='red'>停止上传!</font>");
				progress.toggleCancel(true);
			}
			catch (ex2) {
				alert(ex2);
				//this.debug(ex2);
			}
		case SWFUpload.UPLOAD_ERROR.UPLOAD_LIMIT_EXCEEDED:
			imageName = "<font color='red'>文件大小超过限制!</font>";
			break;
		default:
			alert(message);
			break;
		}
		addFileInfo(file.id,imageName);
	} catch (ex3) {
		alert(ex);
		//this.debug(ex3);
	}

}


function addImage(src) {
	var newImg = document.createElement("img");
	newImg.style.margin = "5px";

	document.getElementById("thumbnails").appendChild(newImg);
	if (newImg.filters) {
		try {
			newImg.filters.item("DXImageTransform.Microsoft.Alpha").opacity = 0;
		} catch (e) {
			// If it is not set initially, the browser will throw an error.  This will set it if it is not set yet.
			newImg.style.filter = 'progid:DXImageTransform.Microsoft.Alpha(opacity=' + 0 + ')';
		}
	} else {
		newImg.style.opacity = 0;
	}

	newImg.onload = function () {
		fadeIn(newImg, 0);
	};
	newImg.src = src;
}

function fadeIn(element, opacity) {
	var reduceOpacityBy = 5;
	var rate = 30;	// 15 fps


	if (opacity < 100) {
		opacity += reduceOpacityBy;
		if (opacity > 100) {
			opacity = 100;
		}

		if (element.filters) {
			try {
				element.filters.item("DXImageTransform.Microsoft.Alpha").opacity = opacity;
			} catch (e) {
				// If it is not set initially, the browser will throw an error.  This will set it if it is not set yet.
				element.style.filter = 'progid:DXImageTransform.Microsoft.Alpha(opacity=' + opacity + ')';
			}
		} else {
			element.style.opacity = opacity / 100;
		}
	}

	if (opacity < 100) {
		setTimeout(function () {
			fadeIn(element, opacity);
		}, rate);
	}
}



/* ******************************************
 *	FileProgress Object
 *	Control object for displaying file info
 * ****************************************** */
/**
 * 此方法目前没用到，显示的层已经隐藏了。如果需要的话，只需要把divFileProgressContainer
 * 这个层的display设置为显示就行
 */
function FileProgress(file, targetID) {
	this.fileProgressID = "divFileProgress";

	this.fileProgressWrapper = document.getElementById(this.fileProgressID);
	if (!this.fileProgressWrapper) {
		this.fileProgressWrapper = document.createElement("div");
		this.fileProgressWrapper.className = "progressWrapper";
		this.fileProgressWrapper.id = this.fileProgressID;

		this.fileProgressElement = document.createElement("div");
		this.fileProgressElement.className = "progressContainer";

		var progressCancel = document.createElement("a");
		progressCancel.className = "progressCancel";
		progressCancel.href = "#";
		progressCancel.style.visibility = "hidden";
		progressCancel.appendChild(document.createTextNode(" "));

		var progressText = document.createElement("div");
		progressText.className = "progressName";
		progressText.appendChild(document.createTextNode("上传文件: "+file.name));

		var progressBar = document.createElement("div");
		progressBar.className = "progressBarInProgress";

		var progressStatus = document.createElement("div");
		progressStatus.className = "progressBarStatus";
		progressStatus.innerHTML = "&nbsp;";

		this.fileProgressElement.appendChild(progressCancel);
		this.fileProgressElement.appendChild(progressText);
		this.fileProgressElement.appendChild(progressStatus);
		this.fileProgressElement.appendChild(progressBar);

		this.fileProgressWrapper.appendChild(this.fileProgressElement);
		document.getElementById(targetID).style.height = "75px";
		document.getElementById(targetID).appendChild(this.fileProgressWrapper);
		fadeIn(this.fileProgressWrapper, 0);

	} else {
		this.fileProgressElement = this.fileProgressWrapper.firstChild;
		this.fileProgressElement.childNodes[1].firstChild.nodeValue = "上传文件: "+file.name;
	}

	this.height = this.fileProgressWrapper.offsetHeight;

}
FileProgress.prototype.setProgress = function (percentage) {
	this.fileProgressElement.className = "progressContainer green";
	this.fileProgressElement.childNodes[3].className = "progressBarInProgress";
	this.fileProgressElement.childNodes[3].style.width = percentage + "%";
};
FileProgress.prototype.setComplete = function () {
	this.fileProgressElement.className = "progressContainer blue";
	this.fileProgressElement.childNodes[3].className = "progressBarComplete";
	this.fileProgressElement.childNodes[3].style.width = "";

};
FileProgress.prototype.setError = function () {
	this.fileProgressElement.className = "progressContainer red";
	this.fileProgressElement.childNodes[3].className = "progressBarError";
	this.fileProgressElement.childNodes[3].style.width = "";

};
FileProgress.prototype.setCancelled = function () {
	this.fileProgressElement.className = "progressContainer";
	this.fileProgressElement.childNodes[3].className = "progressBarError";
	this.fileProgressElement.childNodes[3].style.width = "";

};
FileProgress.prototype.setStatus = function (status) {
	this.fileProgressElement.childNodes[2].innerHTML = status;
};

FileProgress.prototype.toggleCancel = function (show, swfuploadInstance) {
	this.fileProgressElement.childNodes[0].style.visibility = show ? "visible" : "hidden";
	if (swfuploadInstance) {
		var fileID = this.fileProgressID;
		this.fileProgressElement.childNodes[0].onclick = function () {
			swfuploadInstance.cancelUpload(fileID);
			return false;
		};
	}
};