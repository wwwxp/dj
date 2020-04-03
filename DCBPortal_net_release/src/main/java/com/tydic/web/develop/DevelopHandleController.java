package com.tydic.web.develop;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.support.DefaultMultipartHttpServletRequest;

import PluSoft.Utils.JSON;

import com.tydic.bp.common.utils.web.ResponseObj;
import com.tydic.bp.core.controller.BaseController;
import com.tydic.service.develop.DevelopHandleService;
import com.tydic.util.ZipCompressor;
import com.tydic.web.monitor.CustomResourceMonitorController;

@Controller
@RequestMapping(value = "/develop")
public class DevelopHandleController extends BaseController {
	private static Logger log = LoggerFactory
			.getLogger(CustomResourceMonitorController.class);
	
	@Autowired
	private DevelopHandleService developHandleService;

	/**
	 * 查询文件目录树数据
	 * 
	 * @param request
	 * @return
	 */
	@RequestMapping(value = "/listFilesTree", produces = { "application/json;charset=UTF-8" })
	@ResponseBody
	public String listFilesTree(HttpServletRequest request) {
		List resultList = null;
		Map<String, String> params = getParamsMap(request);
		params.put("developTestPath", request.getSession().getServletContext()
				.getRealPath("/"));
		try {
			resultList = developHandleService.listFilesTree(params);
		} catch (Exception e) {
			e.printStackTrace();
			return JSON.Encode(ResponseObj.getResultMap(ResponseObj.ERROR,
					e.getMessage()));
		}
		// System.out.println("resultList"+JSON.Encode(resultList));
		return JSON.Encode(resultList);

	}

	/**
	 * 新建文件或目录
	 * 
	 * @param request
	 * @return
	 */
	@RequestMapping(value = "/createDirectoryOrFile", produces = { "application/json;charset=UTF-8" })
	@ResponseBody
	public String createDirectoryOrFile(HttpServletRequest request) {
		Map resultMap = null;
		Map<String, String> params = getParamsMap(request);
		try {
			String path = (String) params.get("path");
			path = java.net.URLDecoder.decode(path, "UTF-8");
			params.put("path", path);
			resultMap = developHandleService.createDirectoryOrFile(params);
		} catch (Exception e) {
			e.printStackTrace();
			return JSON.Encode(ResponseObj.getResultMap(ResponseObj.ERROR,
					e.getMessage()));
		}
		return JSON.Encode(resultMap);

	}

	/**
	 * 重命名文件或目录
	 * 
	 * @param request
	 * @return
	 */
	@RequestMapping(value = "/renameDirectoryOrFile", produces = { "application/json;charset=UTF-8" })
	@ResponseBody
	public String renameDirectoryOrFile(HttpServletRequest request) {
		Map resultMap = null;
		Map<String, String> params = getParamsMap(request);
		try {
			String path = (String) params.get("path");
			String parentPath = (String) params.get("parentPath");

			path = java.net.URLDecoder.decode(path, "UTF-8");
			parentPath = java.net.URLDecoder.decode(parentPath, "UTF-8");
			params.put("path", path);
			params.put("parentPath", parentPath);
			resultMap = developHandleService.renameDirectoryOrFile(params);
		} catch (Exception e) {
			e.printStackTrace();
			return JSON.Encode(ResponseObj.getResultMap(ResponseObj.ERROR,
					e.getMessage()));
		}
		return JSON.Encode(resultMap);

	}

	/**
	 * 删除文件或目录
	 * 
	 * @param request
	 * @return
	 */
	@RequestMapping(value = "/deleteDirectoryOrFile", produces = { "application/json;charset=UTF-8" })
	@ResponseBody
	public String deleteDirectoryOrFile(HttpServletRequest request) {
		Map resultMap = null;
		Map<String, String> params = getParamsMap(request);

		try {
			String path = (String) params.get("path");
			path = java.net.URLDecoder.decode(path, "UTF-8");
			params.put("path", path);
			resultMap = developHandleService.deleteDirectoryOrFile(params);
		} catch (Exception e) {
			e.printStackTrace();
			return JSON.Encode(ResponseObj.getResultMap(ResponseObj.ERROR,
					e.getMessage()));
		}
		return JSON.Encode(resultMap);

	}

	/**
	 * 打开代码在线编辑页面
	 * 
	 * @param request
	 * @return
	 * @throws UnsupportedEncodingException
	 */
	@RequestMapping(value = "/openDevelopFile", method = RequestMethod.GET)
	public String opendevelopFile(HttpServletRequest request) {
		Map resultMap = null;
		Map<String, String> params = new HashMap();
		try {
			request.setCharacterEncoding("UTF-8");
			String path = (String) request.getParameter("path");
			path = java.net.URLDecoder.decode(path, "UTF-8");
			params.put("path", path);
			resultMap = developHandleService.openDevelopFile(params);
			request.setAttribute("content", (String) resultMap.get("content"));
			request.setAttribute("path",
					java.net.URLEncoder.encode(path, "UTF-8"));

		} catch (Exception e) {
			e.printStackTrace();
			return JSON.Encode(ResponseObj.getResultMap(ResponseObj.ERROR,
					e.getMessage()));
		}
		return "develop/developPage";

	}

	/**
	 * 保存代码编辑信息
	 * 
	 * @param request
	 * @return
	 * @throws UnsupportedEncodingException
	 */
	@RequestMapping(value = "/saveDevelopFile", method = RequestMethod.POST)
	@ResponseBody
	public String saveDevelopFile(HttpServletRequest request) {
		Map resultMap = null;
		Map<String, String> params = getParamsMap(request);
		String path = (String) params.get("path");

		try {
			path = java.net.URLDecoder.decode(path, "UTF-8");
			params.put("path", path);
			resultMap = developHandleService.saveDevelopFile(params);

		} catch (Exception e) {
			e.printStackTrace();
			return JSON.Encode(ResponseObj.getResultMap(ResponseObj.ERROR,
					e.getMessage()));
		}
		return JSON.Encode(resultMap);

	}

	/**
	 * 发布
	 * 
	 * @param request
	 * @return
	 * @throws UnsupportedEncodingException
	 */
	@RequestMapping(value = "/release", method = RequestMethod.POST)
	@ResponseBody
	public String release(HttpServletRequest request) {
		Map resultMap = null;
		Map<String, String> params = getParamsMap(request);
		String path = (String) params.get("path");
		try {
			path = java.net.URLDecoder.decode(path, "UTF-8");
			params.put("path", path);
			resultMap = developHandleService.release(params);

		} catch (Exception e) {
			e.printStackTrace();
			return JSON.Encode(ResponseObj.getResultMap(ResponseObj.ERROR,
					e.getMessage()));
		}
		return JSON.Encode(resultMap);

	}

	/**
	 * 上传文件
	 * 
	 * @param request
	 * @return
	 * @throws UnsupportedEncodingException
	 */
	@RequestMapping(value = "/upload", method = RequestMethod.POST)
	@ResponseBody
	public String uploadFile(DefaultMultipartHttpServletRequest request) {
		Map resultMap = new HashMap();

		try {
			List<File> formFiles = this.getUploadFiles(request);
			Map<String, String> formData = this.getParameterMap(request);
			String path = formData.get("path");
			for (int i = 0; i < formFiles.size(); i++) {
				File file = formFiles.get(i);
				String fileName = file.getName();
				int index = fileName.lastIndexOf(".");
				String suffix = fileName.substring(index + 1);
				String uploadPath = java.net.URLDecoder.decode(path, "UTF-8");
				if (suffix.toLowerCase().equals("zip")) {
					ZipCompressor.unZip(file.getAbsolutePath(), uploadPath);
				} else {
					String tmp = fileName.substring(16);
					String originalFilename = tmp
							.substring(tmp.indexOf("_") + 1);
					FileUtils.moveFile(file, new File(uploadPath
							+ File.separator + originalFilename));
				}
				file.delete();
			}

			resultMap.put("state", "1");
		} catch (Exception e) {
			e.printStackTrace();
			return JSON.Encode(ResponseObj.getResultMap(ResponseObj.ERROR,
					e.getMessage()));
		}
		return JSON.Encode(resultMap);

	}

	public static void main(String[] args) {
		try {
			String a = java.net.URLEncoder.encode("E:\\test\\a\\v", "UTF-8");
			String b = java.net.URLDecoder
					.decode("/dccp_web/develop/openDevelopFile?path=E%3A%5Ctest%5Cjs%5C%E6%96%B0%E5%BB%BA%E6%96%87%E6%9C%AC%E6%96%87%E6%A1%A3.txt",
							"UTF-8");
			System.out.println(a + "," + b);
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

}
