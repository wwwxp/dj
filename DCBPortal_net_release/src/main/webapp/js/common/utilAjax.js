/**
 * 通过post方式获取json数据
 * @param url   地址 格式："/userAction.do?method=insertEmpee"
 * @param data   data 参数 json格式  {id：1，name:"adc"} 或 [{id：1，name:"adc"}]
 * @param logName 模块名  例如：“权限管理-增加权限”
 * @param callback  回调函数，返回成功后回调方法
 * @param execKey  execKey
 * @param dbKey  dbKey   可选项
 * @param async     是否异步，默认异步 可选项
 */
function getJsonDataByPost(url,data,logName,callback,execKey,dbKey,async,msg,isShowLoadMask) {
    var obj = new Object();
    if(data){
        obj["params"] = mini.encode(data);
    }
    if(logName){
        obj["logName"] = logName;
    }
    if(execKey){
        obj["execKey"] = execKey;
    }
    if(dbKey){
        obj["dbKey"] = dbKey;
    }
    getJsonData(url, obj, callback, "POST", async,msg,isShowLoadMask);
}

/**
 * @param url   地址 格式："/userAction.do?method=insertEmpee"
 * @param data   data 参数 json格式  {id：1，name:"adc"} 或 [{id：1，name:"adc"}]
 * @param logName 模块名  例如：“权限管理-增加权限”
 * @param callback  回调函数  返回成功后回调方法
 * @param execKey  execKey
 * @param dbKey  dbKey  可选项
 * @param async     是否异步，默认异步  可选项
 *  @param msg       遮罩显示信息
 */
function getJsonDataByGet(url,data,logName,callback,execKey,dbKey,async,msg) {
    var obj = new Object();
    obj["params"] = mini.encode(data);
    if(execKey){
        obj["execKey"] = execKey;
    }
    if(dbKey){
        obj["dbKey"] = dbKey;
    }
    showLoadMask();
    getJsonData(url, obj, callback, "GET", async,msg);
}

/**
 * 获取json数据
 * @param url   地址
 * @param callback  回调函数
 * @param method    请求类型 GET OR POST
 * @param data      请求数据
 * @param async     是否异步，默认异步
 * @param msg       遮罩显示信息
 *
 */
function getJsonData(url, data, callback, method, async,msg,isShowLoadMask) {
    if(async == undefined){
        async = true;
    }
    if(isShowLoadMask == undefined || isShowLoadMask){
    	showLoadMask(msg);
    } 
     
    $.ajax({
        url: url,
        type: method,
        data: data,
        cache: false,
        dataType: "text",
        async: async,
        success: function (result,  status, xhr) {
        	//先用text文本接收， 再转换成json
            result = mini.decode(result);
            hideLoadMask();
            if(result){
                if(result.error){
                    showErrorMessageAlter(result.error);
                    return;
                }
                if(result.timeout){
                	showErrorMessageAlter("登录超时,请重新登陆",function(){
                		top.window.location.href =  result.timeout;
                		 return;
                	});
                    //alert("登录超时,请重新登陆");
                }
                //菜单没权限
                if (result.noPermission) {
                	$(document.head).html("");
                	$(document.body).html(result.noPermission);
                }
                callback(result);
            }else{
                callback();
            }
        },
        error: function (result, textStatus, errorThrown) {
        	//先用text文本接收， 再转换成json
            result = mini.decode(result);
            hideLoadMask();
            if(result.status=="0"){
                return ;
            }
            showErrorMessageAlter("系统异常!");
        }
    });
}

/**
 * 文件上传，考虑到文件上传用的功能页面较少，
 * 目前没有把/js/common/ajaxfileupload.js加入到common.jsp中
 * 因为若需使用该方法请在对应的页面中引入该js文件
 * @param url 处理上传文件的地址
 * @param fileElementIds  文件列表，数组型，对应<input /> 标签的id
 * @param param 除文件列表的其它普通表单数据
 * @param callback 回调方法
 * @param isclean  上传文件后是否清空选中文件
 * @param logName 日志名称
 */
function commonFileUpload(url,fileElementIds,param,callback,isclean,msg,logName, alterWidth, alterHeight){
	showLoadMask(msg);
	if(isclean == undefined || isclean == null){
		isclean=true;
	}
	if (param != null && logName != null && logName!= undefined) {
		param["logName"] = logName;
	}
	$.ajaxFileUpload
    (
        {
            url: url, //用于文件上传的服务器端请求地址
            secureuri: false, //是否需要安全协议，一般设置为false
            fileElementId: fileElementIds, //文件上传域的ID
            data:param,
            isclean:isclean,
            dataType: 'text', //返回值类型 一般设置为json
            success: function (result,  status, xhr) {
                //先用text文本接收， 再转换成json
                try {
                    result = $.parseJSON(result.replace(/<.*?>/ig, ""));
                }catch(e){
                    //console.log(result+e);
                }
                // result = mini.decode(result);
                hideLoadMask();
                if(result){
                    if(result.error){
                        showErrorMessageAlter(result.error);
                        return;
                    }
                    if(result.timeout){
                        showErrorMessageAlter("登录超时,请重新登陆",function(){
                            top.window.location.href =  result.timeout;
                            return;
                        });
                        //alert("登录超时,请重新登陆");
                    }
                    //菜单没权限
                    if (result.noPermission) {
                        $(document.head).html("");
                        $(document.body).html(result.noPermission);
                    }
                    callback(result);
                }else{
                    callback();
                }
            },
            error: function (result, textStatus, errorThrown) {
                //先用text文本接收， 再转换成json
                hideLoadMask();
                try {
                    result = $.parseJSON(result.replace(/<.*?>/ig, ""));
                }catch(e){
                    //console.log(result+e);
                }
                // result = mini.decode(result);
                if(result.status=="0"){
                    return ;
                }
                showErrorMessageAlter("系统异常!");
            }
        }
    );
}