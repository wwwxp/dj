/**
 * Created with IntelliJ IDEA.
 * User: tom
 * Date: 15-6-5
 * Time: 下午4:07
 * To change this template use File | Settings | File Templates.
 */
//定义变量， 通常是页面控件和参数
var JsVar = new Object();
//初始化
$(document).ready(function () {
    mini.parse();
    //登陆异常提示异常信息
    if ($("#logErrorTip").val().trim() != "") {
        mini.alert($("#logErrorTip").val());
        $("#logErrorTip").val("");
    }
    JsVar["isSubmit"] =false;
    /**
     * 回车键会触发keydown 和click（应该click按扭在form表单中）两个事件，
     * 造成两个调用onLoginClick(),因此需要通过JsVar["isSubmit"]判断，当其中一个调用了
     * onLoginClick()后不再调用此方法，避免多次提交问题
     */
    $(window).unbind("keydown").bind("keydown",function(e){ 
        if(!e){
            e = window.event;
        }
        if(e.keyCode== 13 || e.which== 13 ){
        	if(!JsVar["isSubmit"]){
        		 onLoginClick();
        		 JsVar["isSubmit"]=true;
        	}
           
        }
    });
    
    $("#login_btn").unbind("click").bind("click",function(e){
    	if(!JsVar["isSubmit"]){
   		  onLoginClick();
   		 JsVar["isSubmit"]=true;
    	}
    });
    
    //密码自动填充问题
    $("#replacePassWord").focus(function(){ 
    	$(this).hide(); 
    	$("#passWord").val("").show().css("backgroundColor","#fff").focus(); 
    }); 
    $("#passWord").blur(function(){ 
    	$(this).show().css("backgroundColor","#fff"); 
    	$("#replacePassWord").hide(); 
    }); 
});

//触发登录事件
function onLoginClick() {
	
	//$(".login_btn").attr("disabled",true);
    var userNameValue = $("#userName").val();
    var passWordValue = $("#passWord").val();
    
    if (userNameValue.trim() == "" && passWordValue.trim() == "") {
        mini.alert("请输入用户名和密码！");
        return false;
    }
    $("#passWord").val(encrypt(passWordValue));
    $("#loginForm").submit();
}
/**
 * 重置
 */
function resetLoginForm(){
    $("#userName").attr("value","");
    $("#passWord").attr("value","");
}