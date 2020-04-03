/**
 * Created with IntelliJ IDEA.
 * User: tom
 * Date: 16-3-5
 * Time: 上午11:30
 * To change this template use File | Settings | File Templates.
 */
// by zhangxixnu 2010-06-21  welcome to visit my personal website http://www.zhangxinxu.com/
// textSearch.js v1.0 文字，关键字的页面纯客户端搜索
// 2010-06-23 修复多字母检索标签破碎的问题
// 2010-06-29 修复页面注释显示的问题
// 2013-05-07 修复继续搜素关键字包含之前搜索关键字没有结果的问题
// 不论何种情况，务必保留作者署名。


(function ($) {
    $.fn.textSearch = function (str, options) {
        var defaults = {
            divFlag: true,//是否识别多个字符串
            divStr: " ",//分字符串标识
            textReduction:true,//是否还原文字
            markClass: "",//标记类
            markColor: "red",//标记颜色
            nullReport: true,
            caseIgnore: false,//是否忽略大小写
            callback: function () {
                return false;
            }
        };
        var sets = $.extend({}, defaults, options || {}), clStr;
        if (sets.markClass) {
            clStr = "class='" + sets.markClass + "'";
        }else if(sets.markCss){
        	clStr = "style='" + sets.markCss + "'";
        } else {
            clStr = "style='font-weight:bold;color:" + sets.markColor + ";'";
        }
        
        //对前一次高亮处理的文字还原
        if(sets.textReduction){
            $("span[rel='mark']").each(function () {
                var text = document.createTextNode($(this).text());
                $(this).replaceWith($(text));
            });
        }

        //字符串正则表达式关键字转化
        $.regTrim = function (s) {
            var imp = /[\^\.\\\|\(\)\*\+\-\$\[\]\?]/g;
            var imp_c = {};
            imp_c["^"] = "\\^";
            imp_c["."] = "\\.";
            imp_c["\\"] = "\\\\";
            imp_c["|"] = "\\|";
            imp_c["("] = "\\(";
            imp_c[")"] = "\\)";
            imp_c["*"] = "\\*";
            imp_c["+"] = "\\+";
            imp_c["-"] = "\\-";
            imp_c["$"] = "\\$";
            imp_c["["] = "\\[";
            imp_c["]"] = "\\]";
            imp_c["?"] = "\\?";
            s = s.replace(imp, function (o) {
                return imp_c[o];
            });
            return s;
        };
        $(this).each(function () {
            var t = $(this);
            str = $.trim(str);
            if (str === "") {
                //alert("关键字为空");
                return false;
            } else {
                //将关键字push到数组之中
                var arr = [];
                if (sets.divFlag) {
                    arr = str.split(sets.divStr);
                } else {
                    arr.push(str);
                }
            }
            var v_html = t.html();
            //删除注释
            v_html = v_html.replace(/<!--(?:.*)\-->/g, "");

            //将HTML代码支离为HTML片段和文字片段，其中文字片段用于正则替换处理，而HTML片段置之不理
            var tags = /[^<>]+|<(\/?)([A-Za-z]+)([^<>]*)>/g;
            var a = v_html.match(tags), test = 0;
            var reg_flags="g";
            if(sets.caseIgnore){
            	reg_flags="ig";
            }
            $.each(a, function (i, c) {
                if (!/<(?:.|\s)*?>/.test(c)) {//非标签
                    //开始执行替换
                    $.each(arr, function (index, con) {
                        if (con === "") {
                            return;
                        }
                        var reg = new RegExp($.regTrim(con), reg_flags);
                        if (reg.test(c)) {
                            //正则替换
                        	if(sets.caseIgnore){
                        		var match_array=c.match(reg);
                        		//临时数组
                        		var noRepeatArray=new Array();
                        		
                        		var is_exist=false;
                        		for(var i = 0; i < match_array.length; i++){ //遍历当前数组
                        			for(var j=0;j<noRepeatArray.length;j++){
                        				if(match_array[i]==noRepeatArray[j]){
                        					is_exist=true;
                        					break;
                        				}
                        			}
                        			if(!is_exist){
                        				noRepeatArray.push(match_array[i]); //把当前数组的当前项push到临时数组里面
                        			}
                        		}
                        		
                        		for(var i = 0; i < noRepeatArray.length; i++){
                        			c = c.replace(new RegExp(noRepeatArray[i],'g'), "♂" + noRepeatArray[i] + "♀");
                        		}
                        	}else{
                        		c = c.replace(reg, "♂" + con + "♀");
                        	}
                        	test = 1;
                        }
                    });
                    c = c.replace(/♂/g, "<span rel='mark' " + clStr + ">").replace(/♀/g, "</span>");
                    a[i] = c;
                }
            });
            //将支离数组重新组成字符串
            var new_html = a.join("");

            $(this).html(new_html);

            if (test === 0 && sets.nullReport) {
                alert("没有搜索结果");
                return false;
            }
            //执行回调函数
            sets.callback();
        });
    };
})(jQuery);


