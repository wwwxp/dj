
jQuery.extend({
	

    createUploadIframe: function(id, uri)
	{
			//create frame
            var frameId = 'jUploadFrame' + id;
            
            if(window.ActiveXObject) {
                //var io = document.createElement('<iframe id="' + frameId + '" name="' + frameId + '" />');
            	if(jQuery.browser.version=="9.0" || jQuery.browser.version=="10.0"){  
                    var io = document.createElement('iframe');  
                    io.id = frameId;  
                    io.name = frameId;  
                }else if(jQuery.browser.version=="6.0" || jQuery.browser.version=="7.0" || jQuery.browser.version=="8.0"){  
                    var io = document.createElement('<iframe id="' + frameId + '" name="' + frameId + '" />');  
	                if(typeof uri== 'boolean'){
	                    io.src = 'javascript:false';
	                }
	                else if(typeof uri== 'string'){
	                    io.src = uri;
	                }
                }
            }else {
                var io = document.createElement('iframe');
                io.id = frameId;
                io.name = frameId;
            }
            io.style.position = 'absolute';
            io.style.top = '-1000px';
            io.style.left = '-1000px';

            document.body.appendChild(io);

            return io;   
    },
    createUploadForm: function(id, fileElementId, data)
	{
		//create form	
		var formId = 'jUploadForm' + id;
		
/*************添加批量上传多个文件支持 fileId生成需要累加，因此先注释掉 start************** by niejianwen *********************/
//		var fileId = 'jUploadFile' + id;
/*************添加批量上传多个文件支持 fileId生成需要累加，因此先注释掉 end***************** by niejianwen ******************/
		
		var form = jQuery('<form  action="" method="POST" name="' + formId + '" id="' + formId + '" enctype="multipart/form-data"></form>');	
		if(data)
		{
			for(var i in data)
			{
				jQuery('<input type="hidden" name="' + i + '" value="' + data[i] + '" />').appendTo(form);
			}			
		}	
		
/*************添加批量上传多个文件支持 start***************** by niejianwen *****************************************/
		
		 //修改前代码 start------- 
//		var oldElement = jQuery('#' + fileElementId);
//		var newElement = jQuery(oldElement).clone();
//		jQuery(oldElement).attr('id', fileId);
//		jQuery(oldElement).before(newElement);
//		jQuery(oldElement).appendTo(form);
		//修改前代码 end -------  
		
		
		for(var i = 0;i< fileElementId.length;i++){  
			  var fileId = 'jUploadFile' + (id+i);
		      var oldElement = jQuery('#' + fileElementId[i]);    
		      var newElement = jQuery(oldElement).clone(); 
		      jQuery(oldElement).attr('id', fileId);    
		      jQuery(oldElement).before(newElement);    
		      jQuery(oldElement).appendTo(form);    
		}  
		//修改后代码 end-------
/*************添加批量上传多个文件支持 end***************** by niejianwen *****************************************/
		
		//set attributes
		jQuery(form).css('position', 'absolute');
		jQuery(form).css('top', '-1200px');
		jQuery(form).css('left', '-1200px');
		jQuery(form).appendTo('body');		
		return form;
    },

    ajaxFileUpload: function(s) {
        // TODO introduce global settings, allowing the client to modify them for all requests, not only timeout		
        s = jQuery.extend({}, jQuery.ajaxSettings, s);
        var id = new Date().getTime()        
		var form = jQuery.createUploadForm(id, s.fileElementId, (typeof(s.data)=='undefined'?false:s.data));
		var io = jQuery.createUploadIframe(id, s.secureuri);
		var frameId = 'jUploadFrame' + id;
		var formId = 'jUploadForm' + id;		
        // Watch for a new set of requests
        if ( s.global && ! jQuery.active++ )
		{
			jQuery.event.trigger( "ajaxStart" );
		}            
        var requestDone = false;
        // Create the request object
        var xml = {}   
        if ( s.global )
            jQuery.event.trigger("ajaxSend", [xml, s]);
        // Wait for a response to come back
        var uploadCallback = function(isTimeout)
		{			
			var io = document.getElementById(frameId);
            try 
			{				
				if(io.contentWindow)
				{
					 xml.responseText = io.contentWindow.document.body?io.contentWindow.document.body.innerHTML:null;
                	 xml.responseXML = io.contentWindow.document.XMLDocument?io.contentWindow.document.XMLDocument:io.contentWindow.document;
					 
				}else if(io.contentDocument)
				{
					 xml.responseText = io.contentDocument.document.body?io.contentDocument.document.body.innerHTML:null;
                	xml.responseXML = io.contentDocument.document.XMLDocument?io.contentDocument.document.XMLDocument:io.contentDocument.document;
				}						
            }catch(e)
			{
				jQuery.handleError(s, xml, null, e);
			}
            if ( xml || isTimeout == "timeout") 
			{				
                requestDone = true;
                var status;
                try {
                    status = isTimeout != "timeout" ? "success" : "error";
                    // Make sure that the request was successful or notmodified
                    if ( status != "error" )
					{
                        // process the data (runs the xml through httpData regardless of callback)
                        var data = jQuery.uploadHttpData( xml, s.dataType );    
                        // If a local callback was specified, fire it and pass it the data
                        if ( s.success )
                            s.success( data, status );
    
                        // Fire the global callback
                        if( s.global )
                            jQuery.event.trigger( "ajaxSuccess", [xml, s] );
                    } else
                        jQuery.handleError(s, xml, status);
                } catch(e) 
				{
                    status = "error";
                    jQuery.handleError(s, xml, status, e);
                }

                // The request was completed
                if( s.global )
                    jQuery.event.trigger( "ajaxComplete", [xml, s] );

                // Handle the global AJAX counter
                if ( s.global && ! --jQuery.active )
                    jQuery.event.trigger( "ajaxStop" );

                // Process result
                if ( s.complete )
                    s.complete(xml, status);

                jQuery(io).unbind()

                setTimeout(function()
									{	try 
										{
											jQuery(io).remove();
											jQuery(form).remove();	
											
										} catch(e) 
										{
											jQuery.handleError(s, xml, null, e);
										}									

									}, 100)

                xml = null

            }
        }
        // Timeout checker
        if ( s.timeout > 0 ) 
		{
            setTimeout(function(){
                // Check to see if the request is still happening
                if( !requestDone ) uploadCallback( "timeout" );
            }, s.timeout);
        }
        try 
		{

			var form = jQuery('#' + formId);
			jQuery(form).attr('action', s.url);
			jQuery(form).attr('method', 'POST');
			jQuery(form).attr('target', frameId);
            if(form.encoding)
			{
				jQuery(form).attr('encoding', 'multipart/form-data');      			
            }
            else
			{	
				jQuery(form).attr('enctype', 'multipart/form-data');			
            }			
            jQuery(form).submit();
            
            
            /*************添加上传完表单是否清空文件 start***************** by niejianwen *****************************************/
            if(!s.isclean){
                var files=jQuery(form).find(":file");
                for(var i=0;i<files.length;i++){
                	var fileName = jQuery(files[i]).attr("name");
                	 
                	var oldElement = jQuery("#"+fileName);
                	if(oldElement){
                		jQuery(oldElement).before(files[i]);    
           		        jQuery(oldElement).appendTo(form);
           		        jQuery(oldElement).remove();
           		        jQuery(files[i]).attr('id', fileName);
                	}
                }
            }
            /*************添加上传完表单是否清空文件 start******************* by niejianwen  ***************************************/

        } catch(e) 
		{			
            jQuery.handleError(s, xml, null, e);
        }
		
        jQuery('#' + frameId).load(uploadCallback	);
        return {abort: function () {}};	

    },

    uploadHttpData: function( r, type ) {
        var data = !type;
        data = type == "xml" || data ? r.responseXML : r.responseText;
        // ifthe type is "script", eval it in global context
        if( type == "script" ){
         jQuery.globalEval( data );
        }
            
        // Get the JavaScript object, ifJSON is used.
        if( type == "json" ){
        	data = r.responseText;  
            var start = data.indexOf(">");  
            if(start != -1) {  
              var end = data.indexOf("<", start + 1);  
              if(end != -1) {  
                data = data.substring(start + 1, end);  
               }  
            }  
            eval( "data = " + data);  
        }
            
        // evaluate scripts within html
        if ( type == "html" )
            jQuery("<div>").html(data).evalScripts();

        return data;
    },
    
    /*************添加handleError方法 start***************** by niejianwen *****************************************/
    
    /**
     * handlerError只在jquery-1.4.2之前的版本中存在，
     * jquery-1.6 和1.7中都没有这个函数了，因此在1.4.2中将这个函数复制到了ajaxFileUpload.js中
     */
    handleError: function( s, xhr, status, e ) 		{
    	// If a local callback was specified, fire it
    			if ( s.error ) {
    				s.error.call( s.context || s, xhr, status, e );
    			}

    			// Fire the global callback
    			if ( s.global ) {
    				(s.context ? jQuery(s.context) : jQuery.event).trigger( "ajaxError", [xhr, s, e] );
    			}
    		}
});


/**
* validateZipV校验文件版本格式  必须含有v1.0.1类似格式
*/
function validateZipV(fileName) {
	 if(fileName.lastIndexOf("_")!=-1){
		 var reg = new RegExp("^[_][v,V]\\d{1,2}\\.\\d\\.\\d$", "g"); 
		 
		 var tmpVersion = fileName.substring(fileName.lastIndexOf("_"),fileName.lastIndexOf("."));
        if (!reg.test(tmpVersion)){
            showWarnMessageTips("文件格式有问题,请重新选择，正确的文件中必须是文件名_v版本号.小版本号.序列，大版本号可以是1-2位数，小版本和序列必须是1位数，例如文件名_v1.0.1");
            return false;
        }
	 }else{
         showWarnMessageTips("文件格式有问题,请重新选择，正确的文件中必须是文件名_v版本号.小版本号.序列，大版本号可以是1-2位数，小版本和序列必须是1位数，例如文件名_v1.0.1");
	         return false;
	 }
	 
	 return true;
}
/*************添加validateZipV方法 end***************** by yewenjie *****************************************/

