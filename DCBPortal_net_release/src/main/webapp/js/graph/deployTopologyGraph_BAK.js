//部署Topology
$(document).ready(function(){
	mini.parse();
	
	initWarp();

	//创建场景对象
	var sceneObj = getSceneObj();
	
	//创建应用服务器集群
	initApplicationServer(sceneObj);
	
	//创建组件集群服务器
	initComponentsServer(sceneObj);
});

/**
 * 重写JTopo换行
 */
function initWarp() {
	// 文本换行,以空格为换行标识
	CanvasRenderingContext2D.prototype.wrapText = function(str, x, y) {
		var textArray = str.split('\n');
		var r = y + 7;
		if (textArray == undefined || textArray == null)
			return false;

		var rowCnt = textArray.length;
		var i = 0, imax = rowCnt, maxLength = 0;
		maxText = textArray[0];
		for (; i < imax; i++) {
			var nowText = textArray[i], textLength = nowText.length;
			if (textLength >= maxLength) {
				maxLength = textLength;
				maxText = nowText;
			}
		}
		var maxWidth = this.measureText(maxText).width;
		var lineHeight = this.measureText("元").width;
		x -= lineHeight * 2;
		for ( var j = 0; j < textArray.length; j++) {
			var words = textArray[j];
			//this.fillText(words, -(maxWidth / 2), y - textArray.length * lineHeight / 100 - r);
			this.fillText(words, x, y);
			y += lineHeight;
		}
	};
}

/**
 * 获取画布对象
 */
function getSceneObj() {
	var canvasObj = $("#canvas")[0];
	var stageObj = new JTopo.Stage(canvasObj);
	stageObj.mode = "drag";
	var sceneObj = new JTopo.Scene(stageObj);
	sceneObj.mode = "drag";
	return sceneObj;
}

/**
 * 创建应用服务器集群
 */
function initApplicationServer(sceneObj) {
	var containerWidth = 1260;
	var containerHeight = 220;
	
	var containerObj = new JTopo.Container("应用服务器集群");
	containerObj.name = "appContainer";
	containerObj.textPosition = "Top_Center";
	containerObj.textOffsetY = 30;
	containerObj.textOffsetX = 20;
	containerObj.font = "18px Consolas Bold";
	containerObj.fontColor = "0,0,0";
	//设置标题位置
	containerObj.textPosition = 'Top_Left';
	//设置背景透明度
	//containerObj.alpha = 0.1;
	//设置位置，分别为距离左边、距离上面、长度、高度
	containerObj.setBound(20, 10, containerWidth, containerHeight);
	//设置圆角
	containerObj.borderRadius = 10;
	containerObj.borderColor = "240,240,240";
	containerObj.borderWidth = 1;
	containerObj.fillColor = "255,255,255";
	
	var startX = 40;
	var startY = 50;
	var totalServer = 5;
	//平均单个主机占用长度
	var totalPadding = (totalServer * 10) + 40;
	var singleServerWidth = Math.ceil((containerWidth-totalPadding)/totalServer);
	
	for (var i=0; i<totalServer; i++) {
		//添加节点对象
		var childContainer = new JTopo.Container();
		//设置节点位置
		childContainer.setBound(startX, startY, singleServerWidth, 160);
		childContainer.alpha = 1;
		childContainer.fillColor = "251,251,251";
		childContainer.borderWidth = 1;
		childContainer.borderColor = "240,240,240";
		childContainer.textPosition = "Middle_Left";
		sceneObj.add(childContainer);
		
		//设置容器中对象
		var containerEle = new JTopo.Node("采集服务器组\n(2台, java/c++)");
		var containerX = startX + singleServerWidth - 60;
		var containerY = containerHeight/2-10;
		containerEle.setBound(containerX, containerY, 120, 120);
		containerEle.textPosition = "Middle_Left";
		containerEle.fontColor = "127,127,127";
		containerEle.setImage(Globals.ctx +  '/images/jtopo/leftshow/host.png', true);
		
		var nameArr = containerEle.text.split('\n');
		if(nameArr!=null && nameArr.length > 1){
			containerEle.paintText = function(a){
				a.beginPath();
				a.font = this.font;
				a.fillStyle = "rgba(" + this.fontColor + ", " + this.alpha + ")";
				a.wrapText(this.text, -this.width, -this.height);
				a.closePath();
			};
		}
		startX += (singleServerWidth + 10);
		sceneObj.add(containerEle);
		
	}
	sceneObj.add(containerObj);
	
	//在该容器最下方中间位置添加一个隐藏节点，用来连线
	var appLinkNode = new JTopo.Node();
	appLinkNode.name = "appLinkNode";
	appLinkNode.setBound((containerWidth/2) + 20, (containerHeight + 10), 2, 2);
	sceneObj.add(appLinkNode);
}

/**
 * 创建组件集群服务器
 */
function initComponentsServer(sceneObj) {
	var containerWidth = 1260;
	var containerHeight = 220;
	
	var containerObj = new JTopo.Container("组件服务器集群");
	containerObj.textOffsetY = 30;
	containerObj.textOffsetX = 20;
	containerObj.font = "18px Consolas";
	containerObj.fontColor = "0,0,0";
	containerObj.name = "comContainer";
	containerObj.textPosition = 'Top_Left';
	containerObj.alpha = 0.5;
	containerObj.setBound(20, 260, containerWidth, containerHeight);
	//设置圆角
	containerObj.borderRadius = 10;
	containerObj.borderColor = "130,122,0";
	containerObj.borderWidth = 1;
	containerObj.fillColor = "253,253,253";
	sceneObj.add(containerObj);
	
	var startX = 40;
	var startY = 300;
	var totalServer = 9;
	//平均单个主机占用长度
	var totalPadding = (totalServer * 10) + 40;
	var singleServerWidth = Math.ceil((containerWidth-totalPadding)/totalServer);
	
	for (var i=0; i<totalServer; i++) {
		//添加节点对象
		var childContainer = new JTopo.Container();
		//设置节点位置
		childContainer.setBound(startX, startY, singleServerWidth, 160);
		childContainer.alpha = 1;
		childContainer.fillColor = "0,166,80";
		childContainer.borderWidth = 1;
		childContainer.borderColor = "130,122,0";
		childContainer.textPosition = "Middle_Left";
		sceneObj.add(childContainer);
		
		//设置容器中对象
		var containerEle = new JTopo.Node("DMDB服务器组\n        (2台)");
		var containerX = startX + singleServerWidth - 60;
		var containerY = startY + containerHeight/2 - 60;
		containerEle.setBound(containerX, containerY, 120, 120);
		containerEle.textPosition = "Top_Center";
		containerEle.setImage(Globals.ctx +  '/images/jtopo/leftshow/host.png', true);
		var nameArr = containerEle.text.split('\n');
		if(nameArr!=null && nameArr.length > 1){
			containerEle.paintText = function(a){
				a.beginPath();
				a.font = this.font;
				a.fillStyle = "rgba(" + this.fontColor + ", " + this.alpha + ")";
				a.wrapText(this.text, -this.width, -this.height);
				a.closePath();
			};
		}
		startX += (singleServerWidth + 10);
		sceneObj.add(containerEle);
	}
	sceneObj.add(containerObj);
	
	//在该容器最下方中间位置添加一个隐藏节点，用来连线
	var appLinkNode = new JTopo.Node();
	appLinkNode.setBound((containerWidth/2) + 20, startY - 42, 2, 2);
	sceneObj.add(appLinkNode);
	
	var nodeList = sceneObj.getDisplayedElements();
	for (var i=0; i<nodeList.length; i++) {
		if (nodeList[i].name == "appLinkNode") {
			var link = new JTopo.Link(appLinkNode, nodeList[i]);
			link.arrowsRadius = 5;
			link.bundleGap = 0;
			sceneObj.add(link);
			
			var link2 = new JTopo.Link(nodeList[i], appLinkNode);
			link2.arrowsRadius = 5;
			link2.bundleGap = 0;
			sceneObj.add(link2);
			break;
		}
	}
}


















