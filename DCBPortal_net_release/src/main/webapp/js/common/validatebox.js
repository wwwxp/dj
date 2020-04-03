/*自定义vtype*/
/**
 * 固定电话：例如0755-1234567
 */
mini.VTypes["telephoneErrorText"] = "电话格式错误";
mini.VTypes["telephone"] = function (v) {
    if(v){
        var reg = /^((\d{7,8})|(\d{4}|\d{3})-(\d{7,8})|(\d{4}|\d{3})-(\d{7,8})-(\d{4}|\d{3}|\d{2}|\d{1})|(\d{7,8})-(\d{4}|\d{3}|\d{2}|\d{1}))$/;
        if (reg.test(v)) {
            return true;
        }
        return false;
    }
    return true;
}
/**
 * 手机号码：例如13818881888
 */
mini.VTypes["mobileErrorText"] = "手机号格式错误";
mini.VTypes["mobile"] = function (v) {
    if(v){
        var reg = /^1[3|4|5|8|9]\d{9}$/;
        if (reg.test(v)) {
            return true;
        }
        return false;
    }
    return true;
}
//联系电话验证  例如13818881888 、 0755-8888888
mini.VTypes["contactMobilePhoneErrorText"] = "电话格式错误";
mini.VTypes["contactMobilePhone"] = function (v) {
    if(v){
        var reg = /((\d{11})|^((\d{7,8})|(\d{4}|\d{3})-(\d{7,8})|(\d{4}|\d{3})-(\d{7,8})-(\d{4}|\d{3}|\d{2}|\d{1})|(\d{7,8})-(\d{4}|\d{3}|\d{2}|\d{1}))$)/;
        if (reg.test(v)) {
            return true;
        }
        return false;
    }
    return true;
}
//IP地址验证
mini.VTypes["IPErrorText"]="IP地址格式错误";
mini.VTypes["IP"]=function (v){
	var reg= /^([0-9]|[1-9]\d|1\d\d|2[0-4]\d|25[0-5])\.([0-9]|[1-9]\d|1\d\d|2[0-4]\d|25[0-5])\.([0-9]|[1-9]\d|1\d\d|2[0-4]\d|25[0-5])\.([0-9]|[1-9]\d|1\d\d|2[0-4]\d|25[0-5])$/;
	if(reg.test(v)){
		return true;
	}
	return false;
}
//[0-9a-zA-Z]{4,}

//密码复杂度
mini.VTypes["PWDCHECKErrorText"]="密码必须包括数字,英文字母大小写";
mini.VTypes["PWDCHECK"]=function (v){
	var reg= /[A-Z]+/;
	var reg1= /[a-z]+/;
	var reg2= /[0-9]+/;
	if(reg.test(v)&& reg1.test(v)&&reg2.test(v)){
		 
		return true;
	}
	return false;
}
