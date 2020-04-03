/**
 * Created with IntelliJ IDEA.
 * Creater: zhongsixue
 * Date: 16-8-18
 * Time: 上午10:40
 * To change this template use File | Settings | File Templates.
 */

/**
 * 定义变量， 通常是页面控件和参数
 */
var JsVar=new Object();

/**
 * 初始化
 */
$(document).ready(function () {
    mini.parse();
    JsVar["queryForm"] = new mini.Form("#queryForm");            

});

/**
 * 返回
 */
function back(){
	history.back();
}

/**
 * 日志搜索
 */
function search(){
	JsVar["queryForm"].validate();
	if (JsVar["queryForm"].isValid() == false){
        return;
    }
	$("#queryForm").submit();
}

