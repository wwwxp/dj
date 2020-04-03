//定义变量， 通常是页面控件和参数
var JsVar = new Object();
//jstorm中的节点
var nodes;
//jstrom 中的连接
var link;
//扩展节点
var extendNode;
//扩展连接
var extendLink;
//bolt节点相关信息
var bcomInputStr=[];
//spout节点相关信息
var scomOutStr=[];
//连接线相关信息
var linkStr=[];
//bolt和spout节点的x坐标，y坐标信息,以及扩展节点信息
var topology;
var canvas ;			
var stage ;
var scene ;
//定时器
var timer;
//是否是调试编辑模式
var isdebug=false;
/**
 * 页面初始化加载
 */
$(document).ready(function(){
	 mini.parse();
	 JsVar["urlRootPath"]="/images/topology/custom";
	canvas = document.getElementById('canvas');
	$(window).resize(resizeCanvas);
	resizeCanvas();
	 stage = new JTopo.Stage(canvas);
	 scene = new JTopo.Scene(stage);
	 $("#contextmenu a").click(function(){
         var text = $(this).text();
         if(text == '添加节点'){
        	 addExtendNode();
         }
         $("#contextmenu").hide();
         $("#nodeContextmenu").hide();
         JsVar["nodeContextmenuOpen"] = false;
	 });
	 
	 $("#nodeContextmenu a").click(function(){
         var text = $(this).text();
         if(text == '删除节点'){
        	 deleteExtendNode();
         }
         $("#nodeContextmenu").hide();
         JsVar["nodeContextmenuOpen"] = false;
         $("#contextmenu").hide();
	 });
	 
	 

//   延迟加载，避免提示组件sweet-alert未完成初始化
	 setTimeout(init,100);
//	 初始化页面显示拓扑图
	 timer=setInterval(init,5000);

});

/**
 * 暂停数据刷新
 * （为了可以重新排列拓扑图的布局，需要先停止数据刷新）
 */
function suspendRefresh(){
	removeAllEvenListener();
	isdebug=true;
	clearInterval(timer);
	debugMode();
	dbclickEvenListener();
    showMessageTips("启动成功！当前页面数据将不会再更新，可进行视图编辑操作");
}

/**
 * 启动数据刷新
 */
function startRefresh(){
	 isdebug=false;
	 removeAllEvenListener();
	if(timer != undefined){
	 clearInterval(timer);
	}
	 timer=setInterval(init,5000);
    showMessageTips("启动成功！编辑视图已停用，当前页面数据将会定时刷新");
}

/**
 * 初始化页面数据
 */
function init(){
	//显示工具栏
	//showJTopoToobar(stage);
	var canvas = $('#canvas');
	if(canvas.length > 0){
		canvas.css({
			'background-color': '#FFFFFF'
			
		});
	}
	
	getJsonDataByPost(graph_url,[],"定制拓扑图-获取各节点数据",function (result){
	       	if(!isdebug){
	       		bcomInputStr=[];
	       	    scomOutStr=[];
	       	    linkStr=[];
//            	alert(JSON.stringify(result));
        		var resultNodes=result.graph.nodes;
            	for(var i=0;i<resultNodes.length;i++){
            		if(resultNodes[i].hidden != true){
            			if(resultNodes[i].spout){
            				scomOutStr.push(resultNodes[i]);
                			
                		}else{
                			bcomInputStr.push(resultNodes[i]);
                		}
            		}
            		
            	}
            	var resultLink=result.graph.edges;
            	for(var i=0;i<resultLink.length;i++){
            		if(resultLink[i].hidden != true){
            			linkStr.push(resultLink[i]);
            		}
            	}
             getJsonDataByPost(Globals.baseActionUrl.TOPOLOGY_ACTION_GET_URL,{"clusterName":clusterName,"topologyName":topologyName},"定制拓扑图-获取节点定制信息",function (result){
            	topology=result;
             	display();
             },null,null,false);
            	
            	
        	}
	},null,null,false);
	
}
/**
 * 初始化拓扑图
 */
function display(){
	//清空所有缓存节点信息和场景中的所有元素
	 scene.clear();
	 nodes=[];
	 link=[];
	 extendNode=[];
	 extendLink=[];
	
	//解析spout信息
	for(var i=0;i<scomOutStr.length;i++){
		var spout=scomOutStr[i];
		var spoutNode=createNode(spout["id"],random_x(),random_y());
		nodes.push(spoutNode);
		
		
	}
	
	//解析bolt信息
	for(var i=0;i<bcomInputStr.length;i++){
		var bolt=bcomInputStr[i];
		var boltNode=createCircleNode(bolt["id"],random_x(),random_y());
		nodes.push(boltNode);
	}
	
	//解析扩展节点信息
	if(topology != undefined && topology != "" && topologyName.indexOf(topology["topologyName"]) == 0){
		var extendNodes=topology["extendNodes"];
		if(extendNodes != undefined && extendNodes != null && extendNodes != ""){
			for(var i=0;i<extendNodes.length;i++){
				var exNode=createNode(extendNodes[i].id,extendNodes[i].location_x,extendNodes[i].location_y);
				extendNode.push(exNode);
			}
			
		}
		
	}
	
	//解析连接线信息
	for(var i=0;i<linkStr.length;i++){
		var tmp=linkStr[i];
		var boltLink=createLink(getNode(tmp["from"]),getNode(tmp["to"]),(tmp["value"].toFixed(2))+"");
		link.push(boltLink);
		
		
	}
	
	//解析扩展节点连接信息
	if(topology != undefined && topology != "" && topologyName.indexOf(topology["topologyName"]) == 0){
		var extendLinks=topology["extendLinks"];
		if(extendLinks != undefined && extendLinks != null && extendLinks != ""){
			for(var i=0;i<extendLinks.length;i++){
				var exLink=createLink(getNode(extendLinks[i].from_id),getNode(extendLinks[i].to_id),"");
				extendLink.push(exLink);
			}
			
		}
		
	}
	
	//更新显示位置
	if(topology != undefined && topology != "" && topologyName.indexOf(topology["topologyName"]) == 0){
		var nodeLocation=topology["node"];
		for(var i=0; i<nodeLocation.length;i++){
			var location=nodeLocation[i];
			for(var j=0 ; j<nodes.length;j++){
				if(nodes[j]._id == location["id"]){
					nodes[j].x=location["location_x"];
					nodes[j].y=location["location_y"];
					break;
				}
			}
			
			
		}
	}
	
	
	//显示所有jstorm节点
	for(var i=0;i<nodes.length;i++){
		scene.add(nodes[i]);
	}
	
	//显示所有扩展节点
    if(extendNode != undefined && extendNode.length != undefined && extendNode.length>0 ){
    	for(var i=0;i<extendNode.length;i++){
    		scene.add(extendNode[i]);
    	}
	}
	
	
	//显示所有jstorm连接线
	for(var i=0;i<link.length;i++){
		scene.add(link[i]);
	}
	
	//显示所有扩展连接线
	if(extendLink != undefined && extendLink.length != undefined && extendLink.length>0 ){
		for(var i=0;i<extendLink.length;i++){
			scene.add(extendLink[i]);
		}
	}
	
	
}


/**
 * 创建圆形节点
 * @param nodeName 节点显示名称
 * @param location_x  x坐标
 * @param location_y  y坐标
 * @returns {___anonymous1932_1941}
 */
function createCircleNode(id,location_x,location_y){
	
	var imageUrl=getImageUrlById(id);
	var topoNodeType=getTopoNodeTypeById(id);
	var circleNode;
	if(imageUrl == "" || imageUrl == "/"){
		circleNode= new JTopo.CircleNode();
	}else{
		circleNode=new JTopo.Node();
	}
	 
	//设置节点ID
	circleNode._id=id;
	circleNode.text = getAliasById(id); // 文字
	circleNode.font = '16px 微软雅黑'; // 字体
	circleNode.jtopoType = 'node'; // 设置节点属性（自定义）
	circleNode.setLocation(location_x, location_y);// 位置 
	circleNode.dragable=false; //设置节点是否可以拖动
	
	
	imageUrl=Globals.ctx+imageUrl;
	
	if(imageUrl == ""){
		circleNode.radius=25;//  半径
		circleNode.textPosition = 'Middle_Center';// 文字居中
	}else{
		circleNode.textPosition = 'Bottom_Center';// 文字下居中
		circleNode.setImage(imageUrl, true);
		circleNode.image_url=imageUrl;
		circleNode.topoNodeType=topoNodeType;
//		node.alpha =0.7;
		circleNode.fontColor ="#000000";
	}
	return circleNode;
}


/**
 * 创建矩形节点
 * @param nodeName 节点显示名称
 * @param location_x x坐标
 * @param location_y y坐标
 * @returns {___anonymous2428_2431}
 */
function  createNode(id,location_x,location_y){
	var node = new JTopo.Node();
	//设置节点ID
	node._id=id;
	node.text = getAliasById(id); // 文字
	var topoNodeType=getTopoNodeTypeById(id);
	node.font = '16px 微软雅黑'; // 字体
	node.dragable=false; //设置节点是否可以拖动
	node.setLocation(location_x, location_y);// 位置
	node.jtopoType = 'node'; // 设置节点属性（自定义）
	var imageUrl=getImageUrlById(id);
	imageUrl=Globals.ctx+imageUrl;
	
	if(imageUrl == ""){
		node.textPosition = 'Middle_Center';// 文字居中
		node.setSize(100,30);// 尺寸
		node.borderRadius = 5; // 圆角
		node.borderWidth = 2; // 边框的宽度
		node.borderColor = '0,0,0'; //边框颜色 
	}else{
		node.textPosition = 'Bottom_Center';// 文字下居中
		node.setImage(imageUrl, true);
		node.image_url=imageUrl;
		node.topoNodeType=topoNodeType;
//		node.alpha =0.7;
		node.fontColor ="#000000";
	}

	return node;
}

/**
 * 创建动态节点
 */
function createDynamicNode(id, location_x, location_y){
    var node = new JTopo.Node();
  //设置节点ID
	node._id=id;
	node.text = getAliasById(id); // 文字
	node.font = '16px 微软雅黑'; // 字体
	node.dragable=false; //设置节点是否可以拖动
	node.jtopoType = 'node'; // 设置节点属性（自定义）
    var imageUrl=getImageUrlById(id);
	node.percent = 0.1;
    node.beginDegree = 0;
    node.width = node.height = 70;
    node.setLocation(location_x, location_y);
    node.textPosition = "Bottom_Center";
    node.fontColor ="#000000";
    node.paint = function(g){
        g.save();
        g.beginPath();
        g.moveTo(0,0);
        g.fillStyle = 'rgba(0,255,0,' + this.alpha + ')';
        g.arc(0, 0, this.width/2, this.beginDegree, this.beginDegree + 2*Math.PI*this.percent);
        g.fill();                
        g.closePath();
        g.beginPath();
        g.fillStyle = 'rgba(0,0,255,' + this.alpha + ')';
        g.moveTo(0,0);
        g.arc(0, 0, this.width/2-8, this.beginDegree, this.beginDegree + 2*Math.PI);                
        g.fill();
        g.closePath(); 
        g.restore();                            
        this.paintText(g);
    };
   
    JTopo.Animate.stepByStep(node, {
        percent: 1
    }, 2, true).start();    
	
    return node;
}
/**
 * 创建连接线
 * @param fromNode 线起始节点
 * @param toNode 线结束节点
 * @param text 线显示名称
 * @returns {___anonymous2955_2958}
 */
function createLink(fromNode,toNode,text){
	 var link = new JTopo.Link(fromNode, toNode, text);        
     link.lineWidth = 4; // 线宽
     link.arrowsRadius=8; //箭头大小
     link.textOffsetY = 0; // 文本偏移量
     link.strokeColor = '169,169,169';
     link.fontColor ="220,20,60";
     link.font = '15px 微软雅黑';
     link.jtopoType = 'link'; // 设置连线属性（自定义）
//   link.dashedPattern = 4;
//	 link.alpha =0.6;
     return link;
}

/**
 * 随机产生x坐标值
 * @return
 */
function random_x(){
	return Math.round(Math.random()*800);
}

/**
 * 随机产生y坐标值
 * @return
 */
function random_y(){
	return Math.round(Math.random()*500);
}


/**
 * 根据节点名称取得节点
 * @param nodeName 节点名称
 * @returns  返回节点
 */
function getNode(id){
	for (var i=0;i<nodes.length;i++){
		var node=nodes[i];
		if(node._id==id){
			return node;
		}
	}
	
	for (var i=0;i<extendNode.length;i++){
		var node=extendNode[i];
		if(node._id==id){
			return node;
		}
	}
}


/**
 * 保存当前页面所有节点x和y坐标值及其它自定信息
 */
function saveNodeLocation(){
	var str="";
	var data=new Object();
	data["clusterName"]=clusterName;
	var index = topologyName.lastIndexOf("-");
	if(index>0){
		data["topologyName"]=topologyName.substring(0,index);
	}else{
		data["topologyName"]=topologyName;
	}
	
	
	var nodesLocation=[]
	for (var i=0;i<nodes.length;i++){
		var node=nodes[i];
		var nodeLocation=new Object();
		nodeLocation["id"]=node._id;
		nodeLocation["node_name"]=removeXYValue(node.text);
		nodeLocation["location_x"]=node.x;
		nodeLocation["location_y"]=node.y;
		nodeLocation["topoNodeType"]=node.topoNodeType;
		var image_url=node.image_url== null  ?"":node.image_url;
		image_url=image_url.replace(Globals.ctx,"");
		
		if(image_url.replaceAll("/","") == JsVar["urlRootPath"].replaceAll("/","")){
			nodeLocation["image_url"]="";
		}else{
			nodeLocation["image_url"]=image_url;
		}
		
		nodesLocation.push(nodeLocation);
//		str+=","+node.text+"("+node.x+","+node.y+")";
	}
	data["node"]=nodesLocation;
	
	//扩展节点信息
	var exNodes=[]
	for (var i=0;i<extendNode.length;i++){
		var node=extendNode[i];
		var exNode=new Object();
		exNode["id"]=node._id;
		exNode["node_name"]=removeXYValue(node.text);
		exNode["location_x"]=node.x;
		exNode["location_y"]=node.y;
		var image_url=node.image_url== null ?"":node.image_url;
		image_url=image_url.replaceAll(Globals.ctx,"");
		exNode["image_url"]=image_url;
		exNodes.push(exNode);
	}
	data["extendNodes"]=exNodes;
	
	//扩展节点连接信息
	var exLinks=[]
	for (var i=0;i<extendLink.length;i++){
		var link=extendLink[i];
		var exLink=new Object();
		var from=link.nodeA;
		var to =link.nodeZ;
		exLink["from_id"]=from._id;
		exLink["to_id"]=to._id;
		exLinks.push(exLink);
	}
	 data["extendLinks"]=exLinks;
	
	 getJsonDataByPost(Globals.baseActionUrl.TOPOLOGY_ACTION_SAVE_URL,{param:JSON.stringify(data)},"定制拓扑图-获取节点定制信息",function (result){

         showMessageTips("当前显示视图已成功保存!");
      },null,null,false);
	 
	
}


/**
 * 根据ID获取名称
 * @param id
 */
function getAliasById(id){
	var alias=id;
	
	//更新显示位置
	if(topology != undefined && topology != "" && topologyName.indexOf(topology["topologyName"]) == 0){
		var nodeLocation=topology["node"];
		for(var i=0; i<nodeLocation.length;i++){
			var location=nodeLocation[i];
			if(location["id"]==id){
				alias=location["node_name"]
				return alias;
			}
			
			
		}
		
		var exNodes=topology["extendNodes"];
		if(exNodes != undefined && exNodes != null && exNodes != ""){
			for(var i=0;i<exNodes.length;i++){
				var info=exNodes[i];
				if(info["id"]==id){
					alias=info["node_name"]== undefined ?id:info["node_name"];
					return alias;
				}
			}
			
		}
	}
	return alias;
}


/**
 * 根据ID获取图片url
 * @param id
 */
function getImageUrlById(id){
	var url="";
	
	if(topology != undefined && topology != "" && topologyName.indexOf(topology["topologyName"]) == 0){
		var nodeLocation=topology["node"];
		for(var i=0; i<nodeLocation.length;i++){
			var location=nodeLocation[i];
			if(location["id"]==id){
				url=location["image_url"]== undefined ?"":location["image_url"];
				return url;
			}
			
			
		}
		var exNodes=topology["extendNodes"];
		if(exNodes != undefined && exNodes != null && exNodes != ""){
			for(var i=0;i<exNodes.length;i++){
				var info=exNodes[i];
				if(info["id"]==id){
					url=info["image_url"]== undefined ?"":info["image_url"];
					return url;
				}
			}
			
		}
		
	}
	return url;
}

/**
 * 根据ID获取节点类型
 * @param id
 * @returns {String}
 */
function getTopoNodeTypeById(id){
	var url="";
	
	if(topology != undefined && topology != "" && topologyName.indexOf(topology["topologyName"]) == 0){
		var nodeLocation=topology["node"];
		for(var i=0; i<nodeLocation.length;i++){
			var location=nodeLocation[i];
			if(location["id"]==id){
				url=location["topoNodeType"]== undefined ?"":location["topoNodeType"];
				return url;
			}
			
			
		}
		var exNodes=topology["extendNodes"];
		if(exNodes != undefined && exNodes != null && exNodes != ""){
			for(var i=0;i<exNodes.length;i++){
				var info=exNodes[i];
				if(info["id"]==id){
					url=info["topoNodeType"]== undefined ?"":info["topoNodeType"];
					return url;
				}
			}
			
		}
		
	}
	return url;
}


/**
 * 渲染页面
 */
function resizeCanvas() {
	  var context=canvas.getContext("2d");
	  var width=$(window).get(0).innerWidth;
	  var height=$(window).get(0).innerHeight
      $("#canvas").attr("width", width-10);
      $("#canvas").attr("height",height-50 );
      context.fillRect(0, 0, width-10, height-50);

};

/**
 * 调试模式，用于显示所有节点的所在坐标，辅助布局视图
 */
function debugMode(){
	//调试用，确定图形位置
//	var displayedNodes=scene.getDisplayedNodes();
	
     //jstorm中的节点 
    if(nodes != undefined && nodes != "" && nodes.length!=undefined && nodes.length>0){
    	for(var i=0;i<nodes.length;i++){
    	 nodes[i].dragable=true;//设置节点可拖动
       	 nodes[i].mouseover(function(event){
       		this.text=removeXYValue(this.text)+'('+this.x+','+this.y+')';
            });
       
       	 nodes[i].mousemove(function(event){
       		this.text=removeXYValue(this.text)+'('+this.x+','+this.y+')';
            });
       	 
       	nodes[i].addEventListener('mouseup', function(event){
       			
       	   $("#contextmenu").hide();
		   $("#nodeContextmenu").hide();
		   JsVar["nodeContextmenuOpen"] = false;
		   
           });
        };
    }
     
     //扩展节点
     if(extendNode != undefined && extendNode != "" && extendNode.length!=undefined && extendNode.length>0){
    	   for(var i=0;i<extendNode.length;i++){
    		   extendNode[i].dragable=true;//设置节点可拖动
    		   extendNode[i].mouseover(function(event){
    			   this.text=removeXYValue(this.text)+'('+this.x+','+this.y+')';
    	         });
    		   extendNode[i].addEventListener('mouseup', function(event){
    			   $("#contextmenu").hide();
    			   $("#nodeContextmenu").hide();
    			   JsVar["nodeContextmenuOpen"] = false;
    			   JsVar["currentNode"] = this;
    			   if(event.button == 2){// 右键
    					// 当前位置弹出菜单（div）
    					$("#nodeContextmenu").css({
    						top: event.pageY,
    						left: event.pageX
    					}).show();	
    				}
    			   JsVar["nodeContextmenuOpen"] = true;
               });
    		   extendNode[i].mousemove(function(event){
    			   this.text=removeXYValue(this.text)+'('+this.x+','+this.y+')';
    	         });
    	     }
     }
     
     stage.addEventListener('mouseup', function(event){
    	 if(event.button == 2){// 右键
    		 if(!JsVar["nodeContextmenuOpen"]){
      		    $("#contextmenu").hide();
  			    $("#nodeContextmenu").hide();
				// 当前位置弹出菜单（div）
				$("#contextmenu").css({
					top: event.pageY,
					left: event.pageX
				}).show();	
  				JsVar["xy_location"] = new Object();
  				JsVar["xy_location"].location_x=event.pageX;
  				JsVar["xy_location"].location_y=event.pageY;
      	 }
    	 }else{
    		   $("#contextmenu").hide();
			   $("#nodeContextmenu").hide();
			   JsVar["nodeContextmenuOpen"] = false;
    	 }
    	 
    	
		});
	 stage.click(function(event){
			if(event.button == 0){
				// 关闭弹出菜单（div）
				$("#contextmenu").hide();
			}
		});
  
}

/**
 * 鼠标双击监听（用于修改节点显示名称和节点显示图片）
 */
function dbclickEvenListener(){

	scene.dbclick(function(event){
		if(event.target == null || event.target.jtopoType == 'link') return;
		removeNodesEvenListener();
		var e = event.target;
		
//		var editHtml= 
//
//			"<div>\n" +
//			"\t\t<table style=\"table-layout: fixed;\">\n" + 
//			"\t\t\t<tr>\n" + 
//			"\t\t\t\t<th width=\"150\">节点名称：</th>\n" + 
//			"\t\t\t\t<td><input id=\"node_name\" type=\"text\" /></td>\n" + 
//			"\t\t\t</tr>\n" + 
//			"      <tr>\n" + 
//			"        <th width=\"150\">图片URL：</th>\n" + 
//			"        <td>\n" + 
//			"         <input id=\"image_url\" type=\"text\" />"+
//			"         </td>\n" + 
//			"      </tr>\n" + 
//			"      <tr>\n" + 
//			"        <th width=\"150\">&nbsp;&nbsp;&nbsp;&nbsp;X坐标：</th>\n" + 
//			"        <td><input id=\"location_x\" type=\"text\" /></td>\n" + 
//			"      </tr>\n" + 
//			"\n" + 
//			"      <tr>\n" + 
//			"        <th width=\"150\">&nbsp;&nbsp;&nbsp;&nbsp;Y坐标：</th>\n" + 
//			"        <td><input id=\"location_y\" type=\"text\" /></td>\n" + 
//			"      </tr>\n" + 
//			"\n" + 
//			"    </table>\n" + 
//			"  </div>\n" + 
//			"\n" + 
//			"";
//
//		var state=swal({
//			title: "<small>编辑节点</small>",
//			text: editHtml,
//			showCancelButton: true,
//			closeOnConfirm: false,
//			type:"input",
//			html: true
//		},function(isConfirm){
////			启动节点事件监听
//			 debugMode();
////			 判断是否点确定
//			 if(!isConfirm) return;
//			 var x=$("#location_x").val();
//			 var y=$("#location_y").val()
//			 var reg = /^\d+$/; 
//			 if(reg.exec(x) && reg.exec(y)){
//				var image_url=$("#image_url").val();
//			    e.x= Number(x);
//				e.y= Number(y);
//				e.text=$("#node_name").val();
//				if(image_url.indexOf("/") == 0){
//					image_url=image_url.substring(1,image_url.length);
//				}
//				var imageUrl_tmp=Globals.ctx+"/images/topology/"+image_url;
//				e.image_url=imageUrl_tmp.replaceAll("/+","/");
//				e.setImage(e.image_url);
//				 swal("暂存成功", " 需点 \"保存当前显示视图\" 按钮永久保存", "success");
//			 }else{
//				 swal("修改无效", "X坐标或者Y坐标存在非数字字符", "error");
//				 return;
//			 }
//			
//			
//		});
		
		openNodeEdit(e);
//		$("#node_name").val(removeXYValue(e.text));
//		$("#location_x").val(e.x);
//		$("#location_y").val(e.y);
////		alert("e.image_url:"+e.image_url);
//		var image_url=e.image_url == undefined ? "":e.image_url ;
//		var index=image_url.indexOf("/images/topology");
//		$("#image_url").val(removeXYValue(image_url == "" ? "":image_url.substring(index+17,image_url.length)));
//		
//		//由于组件弹窗时type="input"时会默认显示输入框，但跟我们的业务不符合，因此隐藏掉
//		$("fieldset > input").css("display","none");
		
//		alert(JSON.stringify($("fieldset > input").attr("tabindex")));
	});
}

/**
 * 移除页面的所有监听事件
 */
function removeAllEvenListener(){
	 scene.removeAllEventListener();
	 stage.removeAllEventListener();
	 
	 if(nodes!= undefined &&nodes.length != undefined && nodes.length>0){
		 for(var i=0;i<nodes.length;i++){
			 nodes[i].removeAllEventListener();
		 }
	 }
	 
	 if(extendNode!= undefined &&extendNode.length != undefined && extendNode.length>0){
		 for(var i=0;i<extendNode.length;i++){
			 extendNode[i].removeAllEventListener();
		 }
	 }
	 
	 
}

/**
 * 移除所有节点的监听事件
 */
function removeNodesEvenListener(){
	 for(var i=0;i<nodes.length;i++){
		 nodes[i].removeAllEventListener();
	 }
	 
	 for(var i=0;i<extendNode.length;i++){
		 extendNode[i].removeAllEventListener();
	 }
}

/**
 * 过滤节点显示的坐标值
 * @param text
 * @returns {String}
 */
function removeXYValue(text){
     var tmp="";
	 var index=text.lastIndexOf("(");
	 if(index>0){
//		 过滤
		 var coordinate=text.substring(index,text.length);
		 coordinate=coordinate.replace("(","").replace(")","").replace(/-/g,"");
		 var reg = /^\d+,\d+$/; 
		 if(reg.exec(coordinate)){
			 tmp= text.substring(0,index);
		 }else{
			 tmp=text;
		 }
	 }else{
		 tmp= text;
	 }
	 return tmp;
}

/**
 * 编辑节点
 * @param e
 */
function openNodeEdit(e){
	var params={};
	params["id"] =e["_id"];
	params["node_name"] =removeXYValue(e.text);
	params["location_x"] =e["x"];
	params["location_y"] =e["y"];
	params["topoNodeType"] =e["topoNodeType"];
	var image_url=e["image_url"] == undefined ? "":e["image_url"] ;
	var index=image_url.indexOf(JsVar["urlRootPath"]);
	params["image_url"] = removeXYValue((image_url == "" || index <0) ? "":image_url.substring(index+JsVar["urlRootPath"].length,image_url.length));
	
	showDialog("节点编辑","800","300",Globals.baseJspUrl.MONITOR_ACTION_TOPOLOGY_NODE_EDIT_JSP,function(result){
		if(result && result.id){
	//		e["_id"]=result["id"];
			e["text"]=result["node_name"];
			e["x"]=Number(result["location_x"]);
			e["y"]=Number(result["location_y"]);
			e["topoNodeType"]=result["topoNodeType"];
			var url=(Globals.ctx+JsVar["urlRootPath"]+"/"+result["image_url"]).replaceAll("//","/");
			e["image_url"]=url;
			e.setImage(url);
            showMessageTips("暂存成功需点 \"保存当前显示视图\" 按钮永久保存");
		}
			
		},params);
}


/**
 * 增加拓展节点
 */
function addExtendNode(){
	var params = {};
	var allnodes = nodes.concat(extendNode);
	params["xy_location"] = JsVar["xy_location"]
	params["allnodes"] = allnodes;
	
	for(var i = 0;i<allnodes.length;i++){
		allnodes[i]["text"]=removeXYValue(allnodes[i]["text"]);
	}
	showDialog("新增节点","800","300",Globals.baseJspUrl.MONITOR_ACTION_TOPOLOGY_EXTEND_NODE_JSP,function(result){
		if(result && result.id){
			var id = result["id"];
			var node_name = result["node_name"];
			var location_x = Number(result["location_x"]);
			var location_y = Number(result["location_y"]);
			var fromNode = result["fromNode"];
			var toNode = result["toNode"];
			var topoNodeType = result["topoNodeType"];
			var image_url=(Globals.ctx+JsVar["urlRootPath"]+"/"+result["image_url"]).replaceAll("//","/");
			
			if(result["image_url"] == undefined || result["image_url"] == "" || result["image_url"] == "/"){
				circleNode= new JTopo.CircleNode();
			}else{
				circleNode=new JTopo.Node();
			}
			
			
			//设置节点ID
			circleNode._id=id;
			circleNode.text = node_name; // 文字
			circleNode.font = '16px 微软雅黑'; // 字体
			circleNode.jtopoType = 'node'; // 设置节点属性（自定义）
			circleNode.setLocation(location_x, location_y);// 位置 
			circleNode.dragable=true; //设置节点是否可以拖动
			
			if(image_url == ""){
				circleNode.radius=25;//  半径
				circleNode.textPosition = 'Middle_Center';// 文字居中
			}else{
				circleNode.textPosition = 'Bottom_Center';// 文字下居中
				circleNode.setImage(image_url, true);
				circleNode.image_url=image_url;
				circleNode.topoNodeType=topoNodeType;
				circleNode.fontColor ="#000000";
			}
			
			extendNode.push(circleNode);
			scene.add(circleNode);
			
			circleNode.mouseover(function(event){
  			   this.text=removeXYValue(this.text)+'('+this.x+','+this.y+')';
  	         });
			circleNode.addEventListener('mouseup', function(event){
 			   $("#contextmenu").hide();
 			   $("#nodeContextmenu").hide();
 			   JsVar["nodeContextmenuOpen"] = false;
 			   JsVar["currentNode"] = this;
 			   if(event.button == 2){// 右键
 					// 当前位置弹出菜单（div）
 					$("#nodeContextmenu").css({
 						top: event.pageY,
 						left: event.pageX
 					}).show();	
 				}
 			   JsVar["nodeContextmenuOpen"] = true;
            });
			if(fromNode != null && fromNode != ''){
				var link = createLink(getNode(fromNode),circleNode,"");
				extendLink.push(link);
				scene.add(link);
			}
			if(toNode != null && toNode != ''){
				var link = createLink(circleNode,getNode(toNode),"");
				extendLink.push(link);
				scene.add(link);
			}
            showMessageTips("暂存成功需点 \"保存当前显示视图\" 按钮永久保存");
		}
			
		},params);
}

/**
 * 删除拓展节点
 */
function deleteExtendNode(){
	showConfirmMessageAlter("是否删除当前节点？", function(){
		var id = JsVar["currentNode"]["_id"];
		for(var i=0;i<extendLink.length;i++){
			if((extendLink[i]["nodeA"] &&extendLink[i]["nodeA"]["_id"] == id) || (extendLink[i]["nodeZ"] && extendLink[i]["nodeZ"]["_id"] == id)  ){
				scene.remove(extendLink[i]);
				extendLink.splice(i,1);
				if(i<extendLink.length-1){
					i--;
				}
				
			}
		}
		
		for(var i=0;i<extendNode.length;i++){
			if(extendNode[i]["_id"] == id  ){
				scene.remove(extendNode[i]);
				extendNode.splice(i,1);
				if(i<extendLink.length-1){
					i--;
				}
			}
		}
		
		
		JsVar["currentNode"]=null;
	},function (){
		JsVar["currentNode"]=null;
	});
	
	
}