/**
 * Created with IntelliJ IDEA.
 * Creater: zhongsixue
 * Date: 16-10-26
 * Time: 上午09:40
 * To change this template use File | Settings | File Templates.
 */

/**
 * 定义变量， 通常是页面控件和参数
 */
var JsVar = new Object();
var netElementData=new Object();
var busTypeData=new Object();
/**
 * 初始化
 */
$(document).ready(function () {
    mini.parse();
    JsVar["numDatagrid"]=mini.get("numDatagrid");
    JsVar["paramForm"]= new mini.Form("#paramForm");
    JsVar["net_element"]= mini.get("net_element");
    //获取网元下拉框值
    getNetElementData();
    //事先获取业务类型下拉框值
    getBusTypeData();
    //获取sp_switch.xml下offline的子节点相关信息
    getOfflineSwitchInfo();
});


/**
 * 获取网元下拉框值
 */
function getNetElementData(){
	getJsonDataByPost(Globals.baseActionUrl.SWITCH_MASTER_STANDBY_UPGRADE_NET_VALUE_ACTION_MANAGE_URL,{},"切离线--查询网元下拉框值",
		function(result){
			if(result.length>0){
				netElementData=result;
				JsVar["net_element"].setData(netElementData);
			}
	});
	
}

/**
 * 获取业务类型下拉框值
 */
function getBusTypeData(){
	var queryParam=new Object();
	queryParam.GROUP_CODE="WEB_SP_SWITCH_BUSINESS";
	getJsonDataByPost(Globals.baseActionUrl.FRAME_QUERY_FOR_LIST_URL,queryParam,"切离线--查询业务类型下拉框值",
		function(result){
			if(result.length>0){
				busTypeData=result;
			}
	}, "config.queryConfigList");
}

/**
 *获取sp_switch.xml相关信息
 */
function getOfflineSwitchInfo(){
	getJsonDataByPost(Globals.baseActionUrl.CUT_OFFLINE_INFO_NUM_NET_ACTION_MANAGE_URL,{},"切离线--查询已存在号段、网元",
		function(result){
		var flag=true;
		//初始信息为号段 
		if(result.numInfo!=null){
			flag=false;
			var numList=result.numInfo;
			JsVar["numDatagrid"].setData(numList);
			for(var i=0;i<numList.length;i++){
				JsVar["numDatagrid"].beginEditRow(JsVar["numDatagrid"].getRow(i));
			}
		}
		//初始信息为网元
		if(result.netInfo!=null){
			flag=false;
			var netList=result.netInfo;
			var netValue=netList.join(",");
			JsVar["net_element"].setValue(netValue);
		}
		if(result.netState==true){
			$("input[name='searchRadio'][value='ele']").attr("checked",true);
		}else if(result.numState==true){
			$("input[name='searchRadio'][value='num']").attr("checked",true); 
		}else{
			$("input[name='searchRadio']").attr("checked",false); 
		}
		
	});
}

/**
 * 表格渲染：状态高亮
 */
function stateRenderer(e){
	var run_pro=e.record.RUN_STATE;
	if(run_pro==null || run_pro<1){
		return "<span class='label label-danger'>&nbsp;未运行&nbsp;</span>";
	}else if(run_pro==1){
		return "<span class='label label-success'>&nbsp;运行中&nbsp;</span>";
	}
}

/**
 * 切离线(cut)/不切离线(notCut)
 */
function submit(type){
	
	//传到后台的值
	var queryParams=new Object();
	queryParams.type=type;
	//判断是否有效
    JsVar["paramForm"].validate();
    if (JsVar["paramForm"].isValid() == false){
        return;
    }
    
    //条件选择:获取要选择的类型
	var radioType=$("input[name='searchRadio']:checked").val();
	var formData=JsVar["paramForm"].getData();
	var numData=JsVar["numDatagrid"].getEditData(true);
	queryParams["radioType"]=radioType;
	if(radioType=="num"){//按号段
		if(numData.length<1){
            showWarnMessageTips("请添加号段信息！");
			return;
		}
		for(var i=0;i<numData.length;i++){
			if(numData[i].startNum=="" || numData[i].endNum==""){
                showWarnMessageTips("请填写开始号段和结束号段！");
		    	return;
			}else if(numData[i].startNum>numData[i].endNum){
                showWarnMessageTips("开始号段不能大于结束号段！");
		    	return;
			}else if(numData[i].busType==""){
                showWarnMessageTips("请选择类型！");
		    	return;
			}
		}
		queryParams["numData"]=numData;
	}else if(radioType=="ele"){//按网元
		if(formData.net_element==""){
            showWarnMessageTips("请选择网元值！");
	    	return;
		}
		
		var netStr=formData.net_element;
		var netArry=netStr.split(",");		
		queryParams["net_element"]=netArry;
	}else{
        showWarnMessageTips("请选择号段或网元条件！");
		return ;
	}
	//提交到后台处理
	getJsonDataByPost(Globals.baseActionUrl.CUT_OFFLINE_CUT_OPT_ACTION_MANAGE_URL,queryParams,"切离线、不切离线操作",
		function(result){
			if (result.rstCode == "0") {
				if(type=="cut"){
                    showMessageTips("修改切离线配置成功！");
				}else if(type=="notCut"){
                    showMessageTips("恢复在线配置成功！");
					getOfflineSwitchInfo();
				}
			}
		}
	);
}

/**
 * 添加行的方法
 */
function addRow(){
	var row = {};
	JsVar["numDatagrid"].addRow(row);
	JsVar["numDatagrid"].beginEditRow(row);
}

/**
 * 操作渲染
 * @returns
 */
function onActionRenderer(e){
    var uid = e.record._uid;
	var html="";
	html+='<div class="icon-remove" title="删除该行" style="cursor:pointer;margin-left:40%;height:16px;width:16px;" onclick="removeRow('+uid+')">';
	return html;
}

/**
 * 删除该行
 */
function removeRow(uid){
	var row = JsVar["numDatagrid"].getRowByUID(uid);
	JsVar["numDatagrid"].removeRow(row);
}