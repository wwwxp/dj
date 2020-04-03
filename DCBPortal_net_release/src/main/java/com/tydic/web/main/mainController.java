package com.tydic.web.main;

import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import com.tydic.bp.core.controller.BaseController;

/**
 * Created with IntelliJ IDEA.
 * User: zhuwei
 * Date: 15-8-27
 * Time: 上午9:50
 * To change this template use File | Settings | File Templates.
 */
@Controller
@RequestMapping("/main")
public class mainController extends BaseController {
	private static Logger log = LoggerFactory.getLogger(mainController.class);
	
	@RequestMapping("/dic")
	public String dicMain(HttpServletRequest request){
		log.info("登陆到主页面");
		return "/main/dicMain";
	}
	
	@RequestMapping("/vk")
	public String vkMain(HttpServletRequest request){
		log.info("登陆到主页面");
		return "/main/vkMain";
	}
}
