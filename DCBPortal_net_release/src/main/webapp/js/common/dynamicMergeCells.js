/*
 * 动态合并单元格，只支持行的合并，不支持列合并
 * miniui2.1.5版本已经实现(mergeColumns)，但是由于版本破解问题，暂时用此js实现
 * miniui2.1.4(mergeColumns)有BUG
 * New增加分组合并单元格方法
 * Author:Tangdl
 * Date:2012-12-17
 * 
 * 使用方法：
 	var gridData2=[{"SERVICE_NAME":"[57524]静态表监控_NM2_MID_ITEM_CODE","SERVICE_ID":57524,"COLLECT_ID":3,"_uid":0,"_index":0},
			   {"SERVICE_NAME":"[57524]静态表监控_NM2_MID_ITEM_CODE","SERVICE_ID":57524,"COLLECT_ID":4,"_uid":1,"_index":1},
			   {"SERVICE_NAME":"[57524]静态表监控_NM2_MID_ITEM_CODE2","SERVICE_ID":57524,"COLLECT_ID":5,"_uid":2,"_index":2},
			   {"SERVICE_NAME":"[57524]静态表监控_NM2_MID_ITEM_CODE","SERVICE_ID":57524,"COLLECT_ID":6,"_uid":3,"_index":3},
			   {"SERVICE_NAME":"[57524]静态表监控_NM2_MID_ITEM_CODE","SERVICE_ID":57526,"COLLECT_ID":7,"_uid":4,"_index":4},
			   {"SERVICE_NAME":"[57524]静态表监控_NM2_MID_ITEM_CODE","SERVICE_ID":57526,"COLLECT_ID":7,"_uid":5,"_index":5},
			   {"SERVICE_NAME":"[57524]静态表监控_NM2_MID_ITEM_CODE","SERVICE_ID":57524,"COLLECT_ID":4,"_uid":6,"_index":6}
 	];
 	var mergeCells2="SERVICE_NAME,SERVICE_ID";
 	var mergeCellColumnIndex2="0,1";
 */

 
 //获取合并单元格的列(表格数据，合并的列(Field),合并列的column),分组
 function getMergeCellsOnGroup(gridData,mergeCells,mergeCellColumnIndex){
 	var mergeCellArray=mergeCells.split(",");
 	var mergeCellColumnsArray=mergeCellColumnIndex.split(",");
 	var returnMegeCells=[];
 	for(var i=0;i<mergeCellArray.length;i++){
		var rowIndex=0;
		var rowSpan=1;
	 	for(var x=0;x<gridData.length;x++){
			if(x!=0){
				//分组合并单元格，每列的合并都需要和前面几列进行对比才能确定是否需要合并单元格
				var groupFlag=true;
				for(var z=0;z<(i+1);z++){
					var cell=mergeCellArray[z];
					var currentRowData=gridData[x][cell];
					var upRowData=gridData[x-1][cell];
					//判断是否为日期类型，如果为日期类型需要转换
					if(validateDateType(currentRowData)){
						var tmpCurrentRowData=mini.formatDate(currentRowData,"yyyy-MM-dd HH:mm:ss");
						if(tmpCurrentRowData!=""){
							var currentRowData=tmpCurrentRowData;
						}
						var tmpUpRowData=mini.formatDate(upRowData,"yyyy-MM-dd HH:mm:ss");
						if(tmpUpRowData!=""){
							var upRowData=tmpUpRowData;
						}
					}
					if(currentRowData!=upRowData){
						groupFlag=false;
					}
				}
				if(groupFlag){
					//上一行数据与本行数据相同时只记录第一次相同index
					if(rowIndex==0&&rowSpan==1){
						rowIndex=gridData[x-1]["_index"];
					}
					rowSpan=rowSpan+1;
					//最后一行数据与上一行数据相等，则保存在数组里面
					if(x==(gridData.length-1)){
						returnMegeCells.push(spliceCellObject(rowIndex,rowSpan,mergeCellColumnsArray[i]));
					}
				}else{
					if(rowSpan!=1){
						//上一行数据与本行数据不相同时，则保存在数组里面，且rowSpan=1，rowIndex=0
						returnMegeCells.push(spliceCellObject(rowIndex,rowSpan,mergeCellColumnsArray[i]));
						rowSpan=1;
						rowIndex=0;
					}
				}
			}
	 	}
 	}
	return returnMegeCells;
 }
 
 
 
 //获取合并单元格的列(表格数据，合并的列(Field),合并列的column),不分组
 function getMergeCells(gridData,mergeCells,mergeCellColumnIndex){
 	var mergeCellArray=mergeCells.split(",");
 	var mergeCellColumnsArray=mergeCellColumnIndex.split(",");
 	var returnMegeCells=[];
 	for(var i=0;i<mergeCellArray.length;i++){
 		var cell=mergeCellArray[i];
		var rowIndex=0;
		var rowSpan=1;
	 	for(var x=0;x<gridData.length;x++){
			if(x!=0){
				if(gridData[x][cell]==gridData[x-1][cell]){
					//上一行数据与本行数据相同时只记录第一次相同index
					if(rowIndex==0&&rowSpan==1){
						rowIndex=gridData[x-1]["_index"];
					}
					rowSpan=rowSpan+1;
					//最后一行数据与上一行数据相等，则保存在数组里面
					if(x==(gridData.length-1)){
						returnMegeCells.push(spliceCellObject(rowIndex,rowSpan,mergeCellColumnsArray[i]));
					}
				}else{
					if(rowSpan!=1){
						//上一行数据与本行数据不相同时，则保存在数组里面，且rowSpan=1，rowIndex=0
						returnMegeCells.push(spliceCellObject(rowIndex,rowSpan,mergeCellColumnsArray[i]));
						rowSpan=1;
						rowIndex=0;
					}
				}
			}
	 	}
 	}
	return returnMegeCells;
 }
 
 //拼装mergeCell的对象
 function spliceCellObject(rowIndex,rowSpan,columnIndex){
 	var cellObject=new Object();
	cellObject.rowIndex=parseInt(rowIndex);
	cellObject.rowSpan=parseInt(rowSpan);
	cellObject.columnIndex=parseInt(columnIndex);
	return cellObject;
 }
 
 //判断是否为日期类型
 function validateDateType(content){
 	var currentRowData=mini.parseDate(content);
	if(currentRowData!=null){
		return true;
	}
	return false;
 }