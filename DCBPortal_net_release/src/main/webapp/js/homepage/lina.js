$(function(){
	
	var currTab = '';
	var pagesize = 5; 
	var bussCurrIndex=1;
	var assCurrIndex=1;
	//加载业务集群
	getclusterList();
	//tab切换
	var $tabNavItem = $('.tabNav').find('a');
	currTab = $($tabNavItem[0]).attr("id");
	var $tabPane = $('.tabPane');
	$tabNavItem.each(function(i){
		$(this).click(function(){
			$(this).parent().addClass('hover').siblings().removeClass('hover');//为tab列表选项增加选中样式
			//$tabPane.eq(i).addClass('hover').siblings().removeClass('hover');//为tab列表对应的内容增加显示隐藏样式
			currTab = $(this).attr("id");
			clickTab();
			return false;
		});
	});
	clickTab();
	/**
	 * 点击tab页面时
	 */
	function clickTab(){
		//业务
		bussHostCount();
		bussProgramCount();
		bussProgramAndHostList();
		//组件
		assHostCount();
		assProgramCount();
		assProgramAndHostList();
		//获取集群的topo列表
		getTopoList();
	}
	
	/**
	 * 业务集群的部署主机个数
	 */
	function bussHostCount(){
		var param = {BUS_CLUSTER_ID:currTab,CLUSTER_PARENT_TYPE:'3'};
		getJsonDataByPost(Globals.baseActionUrl.FRAME_QUERY_FOR_OBJECT_URL, param, "",
		        function(result){
				 $('#buss_host_count').html(result.HOSTCOUNT+"台");
		    },"homeindex.queryHostCount");
	}
	
	/**
	 * 业务集群的进程个数
	 */
	function bussProgramCount(){
		var param = {BUS_CLUSTER_ID:currTab};
		getJsonDataByPost(Globals.baseActionUrl.FRAME_QUERY_FOR_OBJECT_URL, param, "",
		        function(result){
					$('#buss_program_count').html(result.PROGRAMCOUNT+"个");
		    },"homeindex.querybussProgramCount");
	}
	
	/**
	 * 组件部署主机个数
	 */
	function assHostCount(){
		var param = {BUS_CLUSTER_ID:currTab,CLUSTER_PARENT_TYPE:'1'};
		getJsonDataByPost(Globals.baseActionUrl.FRAME_QUERY_FOR_OBJECT_URL, param, "",
		        function(result){
				 $('#ass_host_count').html(result.HOSTCOUNT+"台");
		    },"homeindex.queryHostCount");
	}
	
	/**
	 * 组件的进程个数
	 */
	function assProgramCount(){
		var param = {BUS_CLUSTER_ID:currTab};
		getJsonDataByPost(Globals.baseActionUrl.FRAME_QUERY_FOR_OBJECT_URL, param, "",
		        function(result){
					$('#ass_program_count').html(result.PROGRAMCOUNT+"个");
		    },"homeindex.queryassProgramCount");
	}
	
	
	/**
	 * 查询业务集群的分类列表
	 */
	function bussProgramAndHostList(){
		var param = {BUS_CLUSTER_ID:currTab};
		getJsonDataByPost(Globals.baseActionUrl.FRAME_QUERY_FOR_LIST_URL, param, "",
		        function(result){
				   if(result && result.length >0){
					   $('#buss_table').empty();
					    if(result.length > pagesize){
					    	$('#buss_page').show();
					    	var $pageli = $('#buss_page').find('a');
					     
					    	$pageli.each(function(i){
					    		$(this).click(function(){
					    			var pageid = $(this).attr("id"); 
					    			clickbusspage(pageid,result);
					    			return false;
					    		});
					    	});
					    }else{
					    	$('#buss_page').hide();
					    }
					    clickbusspage(null,result);
				   } 
				 
		    },"homeindex.querybussProgramAndHost");
	}
	
	/**
	 * 查询组件集群的分类列表
	 */
	function assProgramAndHostList(){
		var param = {BUS_CLUSTER_ID:currTab};
		getJsonDataByPost(Globals.baseActionUrl.FRAME_QUERY_FOR_LIST_URL, param, "",
		        function(result){
				   if(result && result.length >0){
					   $('#ass_table').empty();
					    if(result.length > pagesize){
					    	$('#ass_page').show();
					    	var $pageli = $('#ass_page').find('a');
					     
					    	$pageli.each(function(i){
					    		$(this).click(function(){
					    			var pageid = $(this).attr("id"); 
					    			clickasspage(pageid,result);
					    			return false;
					    		});
					    	});
					    }else{
					    	$('#ass_page').hide();
					    }
					    clickasspage(null,result);
				   } 
				 
		    },"homeindex.queryassProgramAndHost");
	}
	/**
	 * 点分页时确发业务
	 */
	function clickbusspage(pageid,result){
		$('#buss_table').empty();
		var tableHtml = '';
		var pagecount = Math.floor(result.length/pagesize)+1;
		 
		if(pageid == 'buss_right'){
			var index = (bussCurrIndex *pagesize)-1;
			//点下一页先判断 还有没有数据
			 if(result.length > index ){ 
				 for(var i = index ; i < index+pagesize ; i++){
					 if(result[i]){
					   tableHtml += '<tr>';
					   tableHtml += '<th width="40%"><i class="iconfont icon-dian"></i>'+result[i]["CLUSTER_NAME"]+'</th>';
					   tableHtml += '<td width="30%">主机：'+result[i]["HOSTCOUNT"]+'</td>';
					   tableHtml += '<td width="30%">进程：'+result[i]["PROGRAMCOUNT"]+'</td>';
					   tableHtml += '</tr>';
					 }else{
						 break;
					 }
				  }
			 }
		}else{
			var index = (bussCurrIndex *pagesize);
			//点下一页先判断 还有没有数据
			 if(result.length > index || pagecount ==1){ 
				 for(var i = index-pagesize ; i < index ; i++){
					 if(result[i]){
					   tableHtml += '<tr>';
					   tableHtml += '<th width="40%"><i class="iconfont icon-dian"></i>'+result[i]["CLUSTER_NAME"]+'</th>';
					   tableHtml += '<td width="30%">主机：'+result[i]["HOSTCOUNT"]+'</td>';
					   tableHtml += '<td width="30%">进程：'+result[i]["PROGRAMCOUNT"]+'</td>';
					   tableHtml += '</tr>';
					 }else{
						 break;
					 }
				  }
			 } 
		}
	 
		
		  
		   $('#buss_table').append(tableHtml);
	}
	
	
	/**
	 * 组件点分页时确发业务
	 */
	function clickasspage(pageid,result){
		$('#ass_table').empty();
		var tableHtml = '';
		var pagecount = Math.floor(result.length/pagesize)+1;
		 
		if(pageid == 'ass_right'){
			var index = (assCurrIndex *pagesize)-1;
			//点下一页先判断 还有没有数据
			 if(result.length > index ){ 
				 for(var i = index ; i < index+pagesize ; i++){
					 if(result[i]){
					   tableHtml += '<tr>';
					   tableHtml += '<th width="40%"><i class="iconfont icon-dian"></i>'+result[i]["CLUSTER_NAME"]+'</th>';
					   tableHtml += '<td width="30%">主机：'+result[i]["HOSTCOUNT"]+'</td>';
					   tableHtml += '<td width="30%">进程：'+result[i]["PROGRAMCOUNT"]+'</td>';
					   tableHtml += '</tr>';
					 }else{
						 break;
					 }
				  }
			 }
		}else{
			var index = (bussCurrIndex *pagesize);
			//点下一页先判断 还有没有数据
			 if(result.length > index || pagecount ==1){ 
				 for(var i = index-pagesize ; i < index ; i++){
					 if(result[i]){
					   tableHtml += '<tr>';
					   tableHtml += '<th width="40%"><i class="iconfont icon-dian"></i>'+result[i]["CLUSTER_NAME"]+'</th>';
					   tableHtml += '<td width="30%">主机：'+result[i]["HOSTCOUNT"]+'</td>';
					   tableHtml += '<td width="30%">进程：'+result[i]["PROGRAMCOUNT"]+'</td>';
					   tableHtml += '</tr>';
					 }else{
						 break;
					 }
				  }
			 } 
		}
	 
		
		  
		   $('#ass_table').append(tableHtml);
	}
	
	function getclusterList(){
		getJsonDataByPost(Globals.baseActionUrl.FRAME_QUERY_FOR_LIST_URL, null, "集群划分-查询所有业务主集群",
		        function(result){
				if(result.length>0){
					 var tabHtml = '';
					$.each(result, function (i, item) {
						if(i == 0){
							tabHtml +='<li class="hover"><a id='+item.BUS_CLUSTER_ID+'>'+item.BUS_CLUSTER_NAME+'</a></li>';
						}else{
							tabHtml +='<li><a id='+item.BUS_CLUSTER_ID+'>'+item.BUS_CLUSTER_NAME+'</a></li>';
						}
		            }); 
					console.log(tabHtml);
					$('.tabNav').append(tabHtml);
				}
		    },"busMainCluster.queryBusMainClusterListByState",null,false);
	}
	
	function getTopoList(){
		var param = {BUS_CLUSTER_ID:currTab};
		getJsonDataByPost(Globals.baseActionUrl.FRAME_QUERY_FOR_LIST_URL, param, "",
		        function(result){
					if(result && result.length > 0 ){
						var optionHtml = "";
						for(var i = 0 ; i < result.length;i++){
							optionHtml += '<option value="'+result[i]["CLUSTER_CODE"]+'">'+ result[i]["PROGRAM_NAME"]+'</option>'
						}
					
						$('#topoList').empty();
						$('#topoList').append(optionHtml);
						var param = {name:result[0]["PROGRAM_NAME"],cluster:result[0]["CLUSTER_CODE"]};
					    initTopoGraph(param);
					}
					    
		    },"homeindex.queryTopoList");
	}
	$("#topoList").change(function(){
	    var value = $(this).val();
	    var text = $(this).find("option:selected").text();
	    var param = {name:text,cluster:value};
	    initTopoGraph(param);
	  });
	
	function initTopoGraph(param){
		var clusterUrl = Globals.ctx + "/api/v2/cluster/"+param["cluster"]+"/topology/summary?clusterName="+param["cluster"];
	    //http://localhost:8088/DCBPortal_net_release//api/v2/cluster/JSTORM1/topology/summary?clusterName=JSTORM1
		var isFlag = true;
		var id;
		getJsonDataByPost(clusterUrl,"","", function (data) {
	         if(data && data.topologies){
	        	   for(var i = 0;i< data.topologies.length;i++){
	        		   if(data.topologies[i]["name"] == param["name"]){
	        			   id = data.topologies[i]["id"];
	        			   isFlag = false;
	        			   break;
	        		   }
	        	   }
	         } 
	         
	    },"","",false);

        if(isFlag){
        	return;
        }
		var url = Globals.ctx + "/api/v2/cluster/"+param["cluster"]+"/topology/"+id+"/graph";
		//var url = "http://192.168.161.205:8026/DCBPortal/api/v2/cluster/JSTORM1/topology/t_voi-1.0.0.1-12-1527492464/graph";	
		getJsonDataByPost(url,"","", function (data) {
		        if (data.error){
		            $('#topology-graph').hide();
		            //$('#topology-graph-tips').html("<p class='text-muted'>" + data.error + "</p>");
		            return;
		        }else{
		        	 $('#topology-graph').empty();
		            data = data.graph;
		        }

		        tableData = new VisTable().newData(data);

		        var visStyle = new VisNetWork();
		        var visData = visStyle.newData(data);
		        var options = visStyle.newOptions({depth: 3, breadth:0});
		       
		        var container = document.getElementById('topology-graph');
		        // 初始化节点图宽高,不固定
		        container.setAttribute("style", "width:"+$(container).width()+"px;height:250px; float:left;");

		        // initialize your network!
		        var network = new vis.Network(container, visData, options);
		        network.view.canvas.options.width=$(container).width()-5;
		       network.on("click", function (params) {
		    	   if(params["nodes"][0]){
			    	   clickBolt({CLUSTER_CODE:param["cluster"],SERVICE_NAME:param["name"],TASK_NAME:params["nodes"][0]});

		    	   }
		        });

		        //do the hash , after draw the graph
		       // var hash = window.location.hash;
		        //$(hash).tab('show');
		    },"","",false);

	}
	//点击bolt事件触发
	function clickBolt(params){
		getJsonDataByPost(Globals.baseActionUrl.HOMEINDEX_QUERY_BOLT_URL,params,"", 
			function (result) {
			     if(result && result.length >0){
			    	 var tableHtml="";
			    	 $('#boltTable').empty();
			    	 for(var i = 0 ;i < result.length;i++){
			    		 tableHtml += '<tr>';
			    		 tableHtml += '<td class="lh40">' +result[i]["TASK_NAME"]+ '</td>';
			    		 tableHtml += '<td class="lh40">' +result[i]["HOST_IP"]+ '</td>';
			    		 tableHtml += '<td class="lh40">' +result[i]["PENDING_SIZE"]+ '</td>';
			    		 tableHtml += '<td class="lh40">' +result[i]["C_PRO_ID"]+ '</td>';
			    		 tableHtml += '</tr>';
			    	 }
			    	 $('#boltTable').append(tableHtml);
			     }
			
			
		},"","",false);
		
	}
	
	
	
	//饼图1-CPU
	setPieChart1('PieChart1', 'CPU', [
        { value: 48, name: '192.168.1.1' },
		{ value: 25, name: '192.168.1.2' },
		{ value: 12, name: '192.168.1.3' },
        { value: 15, name: '192.168.1.4' }
    ]);
	
	//饼图2-内存
	setPieChart2('PieChart2', '内存', [
        { value: 25, name: '192.168.1.1' },
		{ value: 12, name: '192.168.1.2' },
		{ value: 15, name: '192.168.1.3' },
        { value: 48, name: '192.168.1.4' }
    ]);
	
	//饼图3-磁盘
	setPieChart3('PieChart3', '磁盘', [
        { value: 12, name: '192.168.1.1' },
		{ value: 15, name: '192.168.1.2' },
		{ value: 48, name: '192.168.1.3' },
        { value: 25, name: '192.168.1.4' }
    ]);
})

//设置饼图1
var setPieChart1 = function(id, title, dataVal) {
    var legends = [];
    for (var i = 0; i < dataVal.length; i++) {
        legends.push(dataVal[i].name);
    }
    var options = {
		title : {
			text: 'CPU',
			x:'center',
		},
        color: ['#ffc000', '#5b9bd5', '#ed7d31', '#a5a5a5'],
        tooltip: {
            trigger: 'item',
            formatter: "{a} <br/>{b}: {c} ({d}%)"
        },
        series: [{
            name: title,
            type: 'pie',
            selectedMode: 'single',
            radius: ['0%', '70%'],
            // roseType: 'radius',
            labelLine: {
                normal: {
                    show: true,
                    length: 10,
                    length2: 10
                }
            },
            data: dataVal
        }]
    };
    var tempChart = echarts.init(document.getElementById(id));
    tempChart.showLoading();
    setTimeout(function() {
        tempChart.hideLoading();
        tempChart.setOption(options);
    }, 500);
};

//设置饼图2
var setPieChart2 = function(id, title, dataVal) {
    var legends = [];
    for (var i = 0; i < dataVal.length; i++) {
        legends.push(dataVal[i].name);
    }
    var options = {
		title : {
			text: '内存',
			x:'center'
		},
        color: ['#ffc000', '#5b9bd5', '#ed7d31', '#a5a5a5'],
        tooltip: {
            trigger: 'item',
            formatter: "{a} <br/>{b}: {c} ({d}%)"
        },
        series: [{
            name: title,
            type: 'pie',
            selectedMode: 'single',
            radius: ['0%', '70%'],
            // roseType: 'radius',
            labelLine: {
                normal: {
                    show: true,
                    length: 10,
                    length2: 10
                }
            },
            data: dataVal
        }]
    };
    var tempChart = echarts.init(document.getElementById(id));
    tempChart.showLoading();
    setTimeout(function() {
        tempChart.hideLoading();
        tempChart.setOption(options);
    }, 500);
};

//设置饼图3
var setPieChart3 = function(id, title, dataVal) {
    var legends = [];
    for (var i = 0; i < dataVal.length; i++) {
        legends.push(dataVal[i].name);
    }
    var options = {
		title : {
			text: '磁盘',
			x:'center'
		},
        color: ['#ffc000', '#5b9bd5', '#ed7d31', '#a5a5a5'],
        tooltip: {
            trigger: 'item',
            formatter: "{a} <br/>{b}: {c} ({d}%)"
        },
        series: [{
            name: title,
            type: 'pie',
            selectedMode: 'single',
            radius: ['0%', '70%'],
            // roseType: 'radius',
            labelLine: {
                normal: {
                    show: true,
                    length: 10,
                    length2: 10
                }
            },
            data: dataVal
        }]
    };
    var tempChart = echarts.init(document.getElementById(id));
    tempChart.showLoading();
    setTimeout(function() {
        tempChart.hideLoading();
        tempChart.setOption(options);
    }, 500);
};
