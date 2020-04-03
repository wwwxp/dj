//定义变量， 通常是页面控件和参数
var JsVar =new Object();

var treeList = [];
//初使化
$(document).ready(function () {
    mini.parse();

});

//查询包类型
function loadPackageTypeList(){
	var params = {
			GROUP_CODE:"WEB_BUS_PACKAGE_TYPE"
		};
		getJsonDataByPost(Globals.baseActionUrl.FRAME_QUERY_FOR_LIST_URL, params, "业务配置文件-查询业务配置文件划分",
	        function(result){
			if(result != null && result.length>0){
				var html ="<tr>";
				var isFlag = false;
				$.each(result, function (i, item) {
					if (item.CONFIG_VALUE != null && item.CONFIG_VALUE != '') {
						isFlag = true;
						treeList.push("privilege_tree_"+ (i+1));
						 html += '<td>';
						 html += '<ul id="privilege_tree_'+ (i+1) +'" class="mini-tree"'+
							'style="width: 100%; height: 100%; float: left;"'+
								'showTreeIcon="true" textField="fileName" onDrawNode="nodeRender"'+
								'idField="currId" parentField="parentId" resultAsTree="false"'+
								'showCheckBox="true" checkRecursive="true" expandOnLoad="1"'+
								'allowSelect="false" enableHotTrack="true" autoCheckParent="true"></ul>';

						html += '</td>'

					}
	            });
				html += "</tr>";
				if(isFlag){
					$('#tableDi').append(html);
					 mini.parse();
					 $.each(result, function (i, item) {
							if (item.CONFIG_VALUE != null && item.CONFIG_VALUE != '') {
								//var param = {wegit:wegit,packageType:'ocs',packageTypeName:'ocs'};
								var param = {wegitId:"privilege_tree_"+(i+1),packageType:item.CONFIG_VALUE,packageTypeName:item.CONFIG_NAME,ROLE_ID: JsVar["params"]["ROLE_ID"]};
								initTreeData(param);
							}
			            });
				}
			}
	    },"config.queryConfigList",null,false);

}

//父页面调用，新增和修改时会初使化一些参数，例如：data.action：操作类型
function onLoadComplete(data) {


    JsVar["params"] = {};
    JsVar["params"]["ROLE_ID"] = data["ROLE_ID"];
    JsVar["params"]["ROLE_NAME"] = data["ROLE_NAME"]
    //先去查询已有权限
    var params = {
    		ROLE_ID:JsVar["params"]["ROLE_ID"]
    };
    //获取业务类型
    loadBusList();

    loadPackageTypeList();

    getJsonDataByPost(Globals.baseActionUrl.FRAME_QUERY_FOR_LIST_URL, params, null,
        function(result){
    	for(var kk = 0 ; kk < treeList.length;kk++){
    		var wegit = mini.get(treeList[kk]);
    		if(wegit){
				 var selectedMenuItems = result;
				 var allNodes = wegit.getAllChildNodes();
                if (allNodes === undefined || allNodes.length == 0) {
                	continue;
                }
				 var node;
				 var selectedNode;
				 for (var j = 0, jlen = selectedMenuItems.length; j < jlen; j++) {//对已选择过的菜单进行过滤控制
				     selectedNode = selectedMenuItems[j];
				     var rootNode = wegit.getRootNode();
				     if(selectedNode["FILE_PATH"].indexOf(rootNode.children[0].desc)< 1){
				    	 continue;
				     }
				     for (var i = 0, len = allNodes.length; i < len; i++) {
				         node = allNodes[i];
				         if(node["fileType"]=='D'){
				         	continue;
						 }
				         if (selectedNode["FILE_PATH"] == node["targetPath"]) {
				        	 wegit.checkNode(node);
				        	var parentNodes = wegit.getAncestors(node);
                             parentNodes.forEach(function(pnode){
                                 if(!wegit.isCheckedNode(pnode)){
                                     wegit.checkNode(pnode);
                                 }
                             });

                             break;
				         }
				     }
				 }
    		}

    	}

        },"userCfgFileMapper.queryUserCfgPrivilegeList");
}

/**
 * 初始化节点数据
 */
function initTreeData(params) {
	params.GROUP_CODE = "WEB_BUS_PACKAGE_TYPE";
	params.page_type="SERVICE";
	var wegit = mini.get(params.wegitId);
	var url = Globals.baseActionUrl.BUS_CFGFILE_TREE_QUERY_PROGRAM_URL + "?packageTypeName=" +  encodeURI(encodeURI(params.packageTypeName));
    treeLoad(wegit, null, params, url);

  //将模板节点以及关联子节点删除
	var treeNodeList = wegit.getList();
	for (var m=0; m<treeNodeList.length;m++) {
		var currNode = treeNodeList[m];
		var level = wegit.getLevel(currNode);

		if (currNode["fileType"] == "D" && level == 2){
	    	for (var i=0; i<JsVar["BUS_LIST"].length; i++) {
	    		if (JsVar["BUS_LIST"][i]["CLUSTER_NAME"] == currNode["fileName"]) {

	        			//删除模板节点下的数据
	        			var childrenNodes = wegit.getChildNodes(currNode);
	        			if(childrenNodes && childrenNodes.length >0){
		        			for (var k=0; k<childrenNodes.length; k++) {
		        				var subChildrenNodes = wegit.getChildNodes(childrenNodes[k]);
		        				if (subChildrenNodes != null && subChildrenNodes.length > 0) {
		        					wegit.removeNodes(subChildrenNodes);
		        				}
		        			}
		        			wegit.removeNodes(childrenNodes);
	        			}
	        			wegit.removeNode(currNode);

	        		break;
	    		}
	    	}
	    }
	}
}

/**
 * 获取业务类型，用来选择模板节点
 */
function loadBusList() {
	var params = {
		TYPE:busVar.BUSINESS_TYPE
	};
	//拿到版本和其下业务包
	getJsonDataByPost(Globals.baseActionUrl.FRAME_QUERY_FOR_LIST_URL, params, "业务配置--获取业务类型列表",
		function(result){
			if (result != null && result.length > 0) {
				JsVar["BUS_LIST"] = result;
				//JsVar["BUS_LIST"].push({"CLUSTER_NAME":"default"});
			}
	},"clusterEleDefine.queryClusterEleList",null,false);
}

/**
 * 获取选中节点列表，值获取业务程序列表
 * @returns {Array}
 * @constructor
 */
function GetData() {
	var selNodes = [];
	for(var kk = 0 ; kk < treeList.length;kk++){
		var wegit = mini.get(treeList[kk]);
		 var checkedNodes = wegit.getCheckedNodes();

		    if (checkedNodes != null && checkedNodes.length >0) {
		        for (var i=0; i<checkedNodes.length; i++) {
		            var fileType = checkedNodes[i]["fileType"];
		            if (fileType == "F") {

		            	var BUS_CLUSTER_ID = "";
		            	var CLUSTER_TYPE = "";
		            	var VERSION="";
		            	var PACKAGE_TYPE ="";
		            	wegit.bubbleParent(checkedNodes[i], function(n){
		            		var level = wegit.getLevel(n);
		            		if(level == 2 && n.clusterId){
		            			BUS_CLUSTER_ID = n.clusterId;
		            		}
		            		if(level == 1){
		            			VERSION = n["fileName"];
		            		}
		            		if(level == 0){
		            			PACKAGE_TYPE = n["desc"];
		            		}
		            		if(isEmptyStr(CLUSTER_TYPE)){
			            		for (var i=0; i<JsVar["BUS_LIST"].length; i++) {
				    	    		if (JsVar["BUS_LIST"][i]["CLUSTER_NAME"] == n["fileName"]) {
				    	    			CLUSTER_TYPE = n["fileName"];
				    	    		}
			            		}
		            		}
						});
		                selNodes.push({
		                    PACKAGE_TYPE:PACKAGE_TYPE,
		                    BUS_CLUSTER_ID:BUS_CLUSTER_ID,
		                    CLUSTER_TYPE: CLUSTER_TYPE,
		                    VERSION : VERSION,
		                    FILE_NAME:checkedNodes[i]["fileName"],
		                    ROLE_ID:JsVar["params"]["ROLE_ID"],
		                    FILE_PATH:checkedNodes[i]["targetPath"]
		                });
		            }
		        }
		    }
	}

    return selNodes;
}

/**
 * 图表重新渲染事件
 * @param e
 */
function nodeRender(e) {
    var level = e.node.nodeLevel;
    if (level == "1") {   //业务主集群
        e.iconCls = "tree-node-main-cluster";
    } else if (level == "2") {  //业务集群
        e.iconCls = "tree-node-cluster";
    } else if (level == "3") {  //版本
        e.iconCls = "tree-node-version";
    } else if (level == "4") {  //业务程序
        e.iconCls = "tree-node-program";
    }
}

//查询
function search() {
	for(var i = 0 ; i < treeList.length;i++){
		var wegit = mini.get(treeList[i]);
		var PROGRAM_NAME = mini.get("PROGRAM_NAME").getValue();
	    if (PROGRAM_NAME == "") {
	    	wegit.clearFilter();
	    } else {
	        PROGRAM_NAME = PROGRAM_NAME.toLowerCase();
	        wegit.filter(function (node) {
	            var text = node.fileName ? node.fileName.toLowerCase() : "";
	            if (text.indexOf(PROGRAM_NAME) != -1) {
	                return true;
	            }
	        });
	    }
	}


}
//按enter键时发生
function onKeyEnter(e) {
    search();
}

//新增和修改时提交
function onSubmit() {
    //获取选中用户权限
    var paramsArray = GetData();
    var params = {
        PRIVILEGE_LIST:paramsArray,
        ROLE_ID:JsVar["params"]["ROLE_ID"]
    };

    if (paramsArray == null || paramsArray.length == 0) {
        showConfirmMessageAlter("角色【" + JsVar["params"]["ROLE_NAME"] +"】未指派任何文件，是否确认?", function ok(){
            getJsonDataByPost(Globals.baseActionUrl.BUS_CFGFILE_ADD_PROGRAM_URL, params, "业务指派管理-提交用户指派配置文件列表",
                function success(result){
                    closeWindow(systemVar.SUCCESS);
                });
        });
        return;
    }else {
        getJsonDataByPost(Globals.baseActionUrl.BUS_CFGFILE_ADD_PROGRAM_URL, params, "业务指派管理-提交用户指派配置文件列表",
            function success(result){
                closeWindow(systemVar.SUCCESS);
            });
	}


}

