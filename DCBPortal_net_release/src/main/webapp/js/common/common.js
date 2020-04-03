
/**
 * 判断是否为空对象
 * @param o 对象
 */
function isEmptyObject(o){
    for(var n in o){
        return false;
    }
    return true;
}
/**
 * 判断是否为空字符串
 * @param str 字符串
 */
function isNotEmptyStr(str){ 
	if(str && $.trim(str)){
		return true;
	}else{
		return false;
	}
}
/**
 * 判断是否为空字符串
 * @param str 字符串
 */
function isEmptyStr(str){ 
	if(str && $.trim(str)){
		return false;
	}else{
		return true;
	}
}

/**
 * 判断字符串是否为空
 * */
function isNull(str) {
    if(str==undefined || str==null || str.length == 0)
        return true;
    return false;
}

/**
 * 通过传的日期和天数计算出距离的日期
 * @param date  传入的日期
 * @param num  天数
 * @returns {string}
 */
function addByTransDate(date, num) {
    if(date instanceof Date){
        date = mini.formatDate(date,"yyyy-MM-dd");
    }

    var translateDate = "", dateString = "", monthString = "", dayString = "";
    translateDate = date.replace("-", "/").replace("-", "/"); ;

    var newDate = new Date(translateDate);
    newDate = newDate.valueOf();
    newDate = newDate + num * 24 * 60 * 60 * 1000;  //备注 如果是往前计算日期则为减号 否则为加号
    newDate = new Date(newDate);

    //如果月份长度少于2，则前加 0 补位
    if ((newDate.getMonth() + 1).toString().length == 1) {
        monthString = 0 + "" + (newDate.getMonth() + 1).toString();
    } else {
        monthString = (newDate.getMonth() + 1).toString();
    }

    //如果天数长度少于2，则前加 0 补位
    if (newDate.getDate().toString().length == 1) {

        dayString = 0 + "" + newDate.getDate().toString();
    } else {

        dayString = newDate.getDate().toString();
    }

    dateString = newDate.getFullYear() + "-" + monthString + "-" + dayString;
    return dateString;

}
/**
 * 全量替换字符串
 * @param str1  要替换的旧文本
 * @param num  要替换的新文本
 *  例： "aabc".replaceAll("a","s") = ssbc
 */
String.prototype.replaceAll = function(str1, str2){
	return this.replace(new RegExp(str1, "gm"), str2);
};

String.prototype.LTrim = function()  
{  
	return this.replace(/(^\s*)/g, "");  
}

String.prototype.RTrim = function()  
{  
	return this.replace(/(\s*$)/g, "");  
}

/**
* 对Date的扩展，将 Date 转化为指定格式的String   
* 月(M)、日(d)、小时(h)、分(m)、秒(s)、季度(q) 可以用 1-2 个占位符，   
* 年(y)可以用 1-4 个占位符，毫秒(S)只能用 1 个占位符(是 1-3 位的数字)   
* 例子：   
* (new Date()).Format("yyyy-MM-dd hh:mm:ss.S") ==> 2006-07-02 08:09:04.423   
* (new Date()).Format("yyyy-M-d h:m:s.S")      ==> 2006-7-2 8:9:4.18  
 */ 

Date.prototype.format = function(fmt) { //author: meizz   
	var o = {
		"M+" : this.getMonth() + 1, //月份   
		"d+" : this.getDate(), //日   
		"h+" : this.getHours(), //小时   
		"m+" : this.getMinutes(), //分   
		"s+" : this.getSeconds(), //秒   
		"q+" : Math.floor((this.getMonth() + 3) / 3), //季度   
		"S" : this.getMilliseconds()
	//毫秒   
	};
	if (/(y+)/.test(fmt))
		fmt = fmt.replace(RegExp.$1, (this.getFullYear() + "")
				.substr(4 - RegExp.$1.length));
	for ( var k in o)
		if (new RegExp("(" + k + ")").test(fmt))
			fmt = fmt.replace(RegExp.$1, (RegExp.$1.length == 1) ? (o[k])
					: (("00" + o[k]).substr(("" + o[k]).length)));
	return fmt;
}

/**
 * RSA加密
 * @param text
 * @returns
 */
function encrypt(text){
	// maxDigits:
	// Change this to accommodate your largest number size. Use setMaxDigits()
	// to change it!
	//
	// In general, if you're working with numbers of size N bits, you'll need 2*N
	// bits of storage. Each digit holds 16 bits. So, a 1024-bit key will need
	//
	// 1024 * 2 / 16 = 128 digits of storage.
	//
	setMaxDigits(131); 
    var encrypt_key = new RSAKeyPair("10001", '', Globals["modulus"],1024);  
    var encrypt= encryptedString(encrypt_key, text,RSAAPP["PKCS1Padding"]); //不支持汉字  
    
    return encrypt;
}

/**
 * 数组去重
 */
Array.prototype.unique = function()
{
	this.sort();
	var re=[this[0]];
	for(var i = 1; i < this.length; i++)
	{
		if( this[i] !== re[re.length-1])
		{
			re.push(this[i]);
		}
	}
	return re;
}

/**
 * 获取链接中的参数 
 * @param name
 * @returns
 */
function getUrlParam(name){

	var reg = new RegExp("(^|&)"+ name +"=([^&]*)(&|$)"); 
	var r = window.location.search.substr(1).match(reg); 
	if (r!=null) return unescape(r[2]); return null; 
} 

/**
 * 深度拷贝
 * @param obj
 * @returns {Array}
 */
//深度克隆
function deepClone(obj){
    var result,oClass=isClass(obj);
    //确定result的类型
    if(oClass==="Object"){
        result={};
    }else if(oClass==="Array"){
        result=[];
    }else{
        return obj;
    }
    for(key in obj){
        var copy=obj[key];
        if(isClass(copy)=="Object"){
            result[key]=arguments.callee(copy);//递归调用
        }else if(isClass(copy)=="Array"){
            result[key]=arguments.callee(copy);
        }else{
            result[key]=obj[key];
        }
    }
    return result;
}

//返回传递给他的任意对象的类
function isClass(o){
    if(o===null) return "Null";
    if(o===undefined) return "Undefined";
    return Object.prototype.toString.call(o).slice(8,-1);
}

String.prototype.startWith=function(str){    
  var reg=new RegExp("^"+str);    
  return reg.test(this);       
}

String.prototype.endWith=function(str){    
  var reg=new RegExp(str+"$");    
  return reg.test(this);       
}

/**
 * 获取配置参数
 */
function getPropListByKey(key){
	var keyValue = {};
	var params = {
		PROPERTIES_KEY:key
	};
	getJsonDataByPost(Globals.baseActionUrl.SERVICE_TYPE_ACTION_GET_PROP_LIST_URL, params, "",
		function success(result){
		if (result != null) {
			keyValue = result;
		}
	 }, "", "", false);
	return keyValue;
}

/**
 * 渲染公共方法，用于从字典查
 * @param e
 * @returns
 */
function render(e,type,textField,valueField){
    textField = textField || "text";
    valueField = valueField || "code";

    var value = e.value;
    var array = getSysDictData(type);
    if(array){
        for(var i=0;i<array.length;++i){
            var record = array[i];
            if(record[valueField] == value){
                return record[textField];
            }
        }
    }
    return value;
}

/**
 * 数字格式化
 * @param num
 * @param precision
 * @param separator
 * @returns {*}
 */
function formatNumber(num, precision, separator) {
    var parts;
    // 判断是否为数字
    if (!isNaN(parseFloat(num)) && isFinite(num)) {
        // 把类似 .5, 5. 之类的数据转化成0.5, 5, 为数据精度处理做准, 至于为什么
        // 不在判断中直接写 if (!isNaN(num = parseFloat(num)) && isFinite(num))
        // 是因为parseFloat有一个奇怪的精度问题, 比如 parseFloat(12312312.1234567119)
        // 的值变成了 12312312.123456713
        num = Number(num);
        // 处理小数点位数
        num = (typeof precision !== 'undefined' ? num.toFixed(precision) : num).toString();
        // 分离数字的小数部分和整数部分
        parts = num.split('.');
        // 整数部分加[separator]分隔, 借用一个著名的正则表达式
        parts[0] = parts[0].toString().replace(/(\d)(?=(\d{3})+(?!\d))/g, '$1' + (separator || ','));

        return parts.join('.');
    }
    return NaN;
}
/**
 * 采用正则表达式获取地址栏参数
 * abc.html?id=123&url=http://www.maidq.com
 * @param name
 * GetQueryString("id") == 123
 */
function getQueryString(name) {

    var reg = new RegExp("(^|&)" + name + "=([^&]*)(&|$)", "i");
    var r = window.location.search.substr(1).match(reg);
    if (r != null)
        return decodeURIComponent(r[2]);
    return null;
}