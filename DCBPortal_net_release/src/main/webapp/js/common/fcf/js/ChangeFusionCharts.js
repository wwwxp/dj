/**
 * Author:Tangdl 
 * Date:09-12-06 
 * Version:1.0 
 * ��json����ת��ƴװ��FusionCharts��ʶ��xml���
 */
function ChangeFcfData() {
}
ChangeFcfData.prototype.params = function (object) {
	//var defaultColor = ["A9F5F2", "006400", "DEB887", "2F4F4F", "556B2F", "B22222", "DAA520", "4B0082", "ADD8E6", "FFA07A"];
	var defaultColor = ["2f7ed8","0d233a","8bbc21","910000","1aadce","492970","f28f43","77a1e5","c42525","a6c96a"];
	var basePath = "js/fcf/charts/" + object.changeType + "/";
	var fcfxml = "<chart rotateYAxisName='1' rotateXAxisName='1' decimals='0' realTimeValuePadding='50'";
	var index = 0;
	if (object != null) {
		var setAtributes = object.setAtributes != undefined ? object.setAtributes : "";
		var datasetAtributes = object.datasetAtributes != undefined ? object.datasetAtributes : "";
		var categoriesAtributes = object.categoriesAtributes != undefined ? object.categoriesAtributes : "";
		var categoryAtributes = object.categoryAtributes != undefined ? object.categoryAtributes : "";
		var allMinValue = 0;
		var allMaxValue = 0;
		var dialValue = 0;
		if (object.changeType == "meter") {
			dialValue = object.dialValue;
			allMinValue = object.minValue != undefined ? object.minValue : 0;
			allMaxValue = object.maxValue != undefined ? object.maxValue : (dialValue * 2);
			fcfxml = fcfxml + " upperLimit='" + allMaxValue + "' lowerLimit='" + allMinValue + "'";
			fcfxml = fcfxml + ((object.showTickMarks != undefined) ? (" showTickMarks='" + object.showTickMarks + "'") : "");
			fcfxml = fcfxml + ((object.cylFillColor != undefined) ? (" cylFillColor='" + object.cylFillColor + "'") : "");
			fcfxml = fcfxml + ((object.showTickValues != undefined) ? (" showTickValues='" + object.showTickValues + "'") : "");
			fcfxml = fcfxml + ((object.showValue != undefined) ? (" showValue='" + object.showValue + "'") : "");
			fcfxml = fcfxml + ((object.bgAlpha != undefined) ? (" bgAlpha='" + object.bgAlpha + "'") : "");
			fcfxml = fcfxml + ((object.showBorder != undefined) ? (" showBorder='" + object.showBorder + "'") : "");
			fcfxml = fcfxml + ((object.placeValuesInside != undefined) ? (" placeValuesInside='" + object.placeValuesInside + "'") : "");
			
		}
		fcfxml = fcfxml + ((object.useRoundEdges != undefined) ? (" useRoundEdges='" + object.useRoundEdges + "'") : "");
		fcfxml = fcfxml + ((object.refreshInterval != undefined) ? (" refreshInterval='" + object.refreshInterval + "'") : "");
		fcfxml = fcfxml + ((object.dataStreamURL != undefined) ? (" dataStreamURL='" + object.dataStreamURL + "'") : "");
		fcfxml = fcfxml + ((object.labelDisplay != undefined) ? (" labelDisplay='" + object.labelDisplay + "'") : "");
		fcfxml = fcfxml + ((object.animation != undefined) ? (" animation='" + object.animation + "'") : "");
		fcfxml = fcfxml + ((object.caption != undefined) ? (" caption='" + object.caption + "'") : "");
		fcfxml = fcfxml + ((object.xAxisName != undefined) ? (" xAxisName='" + object.xAxisName + "'") : "");
		fcfxml = fcfxml + ((object.yAxisName != undefined) ? (" yAxisName='" + object.yAxisName + "'") : "");
		fcfxml = fcfxml + ((object.baseFontSize != undefined) ? (" baseFontSize='" + object.baseFontSize + "'") : " baseFontSize='12'");
		fcfxml = fcfxml + ((object.baseFont != undefined) ? (" baseFont='" + object.baseFont + "'") : "");
		fcfxml = fcfxml + ((object.baseFontColor != undefined) ? (" baseFontColor='" + object.baseFontColor + "'") : "");
		fcfxml = fcfxml + ((object.showValues != undefined) ? (" showValues='" + object.showValues + "'") : "");
		fcfxml = fcfxml + ((object.numberSuffix != undefined) ? (" numberSuffix='" + object.numberSuffix + "'") : "");
		fcfxml = fcfxml + ((object.yAxisMaxValue != undefined) ? (" yAxisMaxValue='" + object.yAxisMaxValue + "'") : "");
		fcfxml = fcfxml + ((object.maxColWidth != undefined) ? (" maxColWidth='" + object.maxColWidth + "'") : "");
		fcfxml = fcfxml + ((object.chartAtributes != undefined) ? object.chartAtributes : "");
		fcfxml = fcfxml + ">";
		//������Զ�����ɫ��ֵ
		if (object.color != undefined) {
			defaultColor = object.color;
		}
		
      	 function ajustColor(index) {
			var len=defaultColor.length;
			return index < len ? defaultColor[index] : defaultColor[index % len];
		}

		
		
		//��ȡ���б���ͻ�ϱ����keyֵ
		var objectDataKey = [];
		if(object.objectData!=undefined){
			if(Object.prototype.toString.apply(object.seriesname) == "[object Array]"){
						var keyIndex = 0;
						for (var params in object.objectData[0]) {
							if (params != object.categoryName) {
								objectDataKey[keyIndex++] = params;
							}
						}
				}
		}
		
		//link
		function setLink(attr, clr) {
			var link = "";
			if (object.link != undefined) {
				link = " link=\"JavaScript: " + object.link + "('" + attr.replace(/\\/gi, "\\\\") + "', '" + clr + "')\" ";
			}	
			return link;	
		}
		
		//����ͼ
		if (object.changeType == "single") {
			for (var d = 0; d < object.objectData.length; d++) {
				var o = object.objectData[d];
				if (object.cloums == undefined) {
					fcfxml = fcfxml + "<set label='" +  o[object.categoryName] + "' value='" + o[object.seriesname] + "' color='" + ajustColor(d) + "'" + setAtributes + setLink(o[object.categoryName], ajustColor(d)) + " />";
				} else {
					fcfxml = fcfxml + "<set  label='" + object.cloums[d] + "' value='" + o[object.seriesname] + "' color='" + ajustColor(d) + "'" + setAtributes + setLink(object.cloums[d], ajustColor(d)) + " />";
				}
			}
		}
		
		//����ͼ
		if (object.changeType == "multi") {
			fcfxml = fcfxml + "<categories" + categoriesAtributes + ">";
				if (object.cloums == undefined) {
						if(object.refreshInterval == undefined){
							for (var d = 0; d < object.objectData.length; d++) {
								var o = object.objectData[d];
								fcfxml = fcfxml + "<category label='" + o[object.categoryName] + "'" + categoryAtributes + " />";
							}
						}
				} else {
					if(object.refreshInterval == undefined){
						for (var i = 0; i < object.cloums.length; i++) {
							fcfxml = fcfxml + "<category label='" + object.cloums[i] + "'" + categoryAtributes + " />";
						}
					}
				}
			fcfxml = fcfxml + "</categories>";
			
			
			if(Object.prototype.toString.apply(object.seriesname) == "[object Array]"){
				for (var sname=0;sname<object.seriesname.length;sname++) {
					fcfxml = fcfxml + "<dataset seriesname='" + object.seriesname[sname] + "'" + datasetAtributes + ">";
					if (object.refreshInterval == undefined) {
						for (var d = 0; d < object.objectData.length; d++) {
							var o = object.objectData[d];
							if (objectDataKey.length > 0) {
								fcfxml = fcfxml + "<set value='" + o[objectDataKey[sname]] + "'" + setAtributes + " />";
							} else {
								fcfxml = fcfxml + "<set value='" + o[sname] + "'" + setAtributes + " />";
							}
						}
					}
					fcfxml = fcfxml + " </dataset>";
				}
			}else{
				for (var sname in object.seriesname) {
					fcfxml = fcfxml + "<dataset seriesname='" + object.seriesname[sname] + "'" + datasetAtributes + ">";
					if (object.refreshInterval == undefined) {
						for (var d = 0; d < object.objectData.length; d++) {
							var o = object.objectData[d];
							if (objectDataKey.length > 0) {
								fcfxml = fcfxml + "<set value='" + o[objectDataKey[sname]] + "'" + setAtributes + " />";
							} else {
								fcfxml = fcfxml + "<set value='" + o[sname] + "'" + setAtributes + " />";
							}
						}
					}
					fcfxml = fcfxml + " </dataset>";
				}
			}
		}
		
		
		//���ͼ
		if (object.changeType == "mixed") {
			fcfxml = fcfxml + "<categories " + categoriesAtributes + ">";
			if (object.cloums == undefined) {
				for (var d = 0; d < object.objectData.length; d++) {
					var o = object.objectData[d];
					fcfxml = fcfxml + "<category label='" + o[object.categoryName] + "'" + categoryAtributes + " />";
				}
			} else {
				for (var i = 0; i < object.cloums.length; i++) {
					fcfxml = fcfxml + "<category label='" + object.cloums[i] + "'" + categoryAtributes + " />";
				}
			}
			fcfxml = fcfxml + "</categories>";
			for (var sname in object.seriesname) {
				if (object.additional != undefined) {
					var additional = object.additional;
					var marker = 0;
					if (Object.prototype.toString.apply(additional) == "[object Array]") {
						for (var p = 0; p < additional.length; p++) {
							if (objectDataKey.length > 0) {
								if (objectDataKey[sname] == additional[p]) {
									fcfxml = fcfxml + "<dataset color='" + ajustColor(index++) + "' seriesname='" + object.seriesname[sname] + "' parentYAxis='S'" + datasetAtributes + ">";
									marker = 1;
								}
							} else {
								if (sname == additional[p]) {
									fcfxml = fcfxml + "<dataset color='" + ajustColor(index++) + "' seriesname='" + object.seriesname[sname] + "' parentYAxis='S'" + datasetAtributes + ">";
									marker = 1;
								}
							}
						}
						if (marker == 0) {
							fcfxml = fcfxml + "<dataset color='" + ajustColor(index++) + "' seriesname='" + object.seriesname[sname] + "'" + datasetAtributes + ">";
						}
					} else {
						for (var params in additional) {
							var parentYAxis = object.parentYAxis != undefined ? object.parentYAxis : "S";
							var nested = additional[params] != "" ? additional[params] : "Line";
							if(objectDataKey.length > 0){
								if(params==objectDataKey[sname]){
									fcfxml = fcfxml + "<dataset color='" + ajustColor(index++) + "' seriesname='" + object.seriesname[sname] + "' renderAs='" + nested + "' parentYAxis='" + parentYAxis + "'" + datasetAtributes + ">";
									marker = 1;
								}
							}
							else {
								if(sname == params){
									fcfxml = fcfxml + "<dataset color='" + ajustColor(index++) + "' seriesname='" + object.seriesname[sname] + "' renderAs='" + nested + "' parentYAxis='" + parentYAxis + "'" + datasetAtributes + ">";
									marker = 1;
								}
							}
						}
						if (marker == 0) {
							fcfxml = fcfxml + "<dataset color='" + ajustColor(index++) + "' seriesname='" + object.seriesname[sname] + "'" + datasetAtributes + ">";
						}
					}
				} else {
					fcfxml = fcfxml + "<dataset color='" + ajustColor(index++) + "' seriesname='" + object.seriesname[sname] + "'" + datasetAtributes + ">";
				}
				for (var d = 0; d < object.objectData.length; d++) {
					var o = object.objectData[d];
					if (objectDataKey.length > 0) {
						fcfxml = fcfxml + "<set value='" + o[objectDataKey[sname]] + "'" + setAtributes + " />";
					} else {
						fcfxml = fcfxml + "<set value='" + o[sname] + "'" + setAtributes + " />";
					}
				}
				fcfxml = fcfxml + " </dataset>";
			}
		}
		//�Ǳ�ͼ
		if (object.changeType == "meter") {
			fcfxml = fcfxml + "<colorRange>";
			if (object.chartType != "Cylinder.swf") {
				var middelValue = allMaxValue * 0.7;
				var thanMiddelValue = allMaxValue * 0.9;
				fcfxml = fcfxml + "<color minValue='0' maxValue='" + middelValue + "'  alpha='0' />";
				fcfxml = fcfxml + "<color minValue='" + middelValue + "' maxValue='" + thanMiddelValue + "' alpha='0' />";
				fcfxml = fcfxml + "<color minValue='" + thanMiddelValue + "' maxValue='" + allMaxValue + "' alpha='0' />";
			}
			fcfxml = fcfxml + "</colorRange>";
			if (object.chartType == "AngularGauge.swf") {
				fcfxml = fcfxml + "<dials>";
				fcfxml = fcfxml + "<dial value='" + dialValue + "' />";
				fcfxml = fcfxml + "</dials>";
			} else {
				fcfxml = fcfxml + "<value>" + dialValue + "</value>";
			}
		}
	}
	if (object.alarmValue != undefined) {
		var tempAlarmName = object.alarmName != undefined ? object.alarmName : "\u8b66\u544a";
		var tempAlarmColor = object.alarmColor != undefined ? object.alarmColor : "8B0000";
		fcfxml = fcfxml + "<trendLines><line startValue='" + object.alarmValue + "' color='" + tempAlarmColor + "' " + "displayvalue='" + tempAlarmName + "' valueOnRight='1' /></trendLines>";
	}
	fcfxml = fcfxml + "</chart>";
	var chartDirectory="";
	if(object.chartDirectory!= undefined){
		chartDirectory=object.chartDirectory;
	}
    //var chart = FusionCharts(object.chartId);
    //if (chart == null) {
	   var chart = new FusionCharts(chartDirectory+"/"+object.chartType, object.chartId != undefined ? object.chartId : "chart", object.width != undefined ? object.width : 550, object.height != undefined ? object.height : 400, "0", "1");
	//}
    chart.setDataXML(fcfxml);
    chart.setTransparent(false);//���fusioncharts����-�˵�����ס������ add by cf 2013-02-26
	chart.render(object.render);
};
var changeData = new ChangeFcfData();

