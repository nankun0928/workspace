package com.jt.web.controller;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.jt.common.util.CookieUtils;
import com.jt.common.vo.SysResult;
import com.jt.web.pojo.User;
import com.jt.web.service.UserService;

import redis.clients.jedis.JedisCluster;

@Controller
@RequestMapping("/user")
public class UserController {

	
	@Autowired
	private UserService userService;
	@Autowired
	private JedisCluster jedisCluster;
	
	@RequestMapping("{param}")
	  public String  module(@PathVariable String param){
		  return param;
		  
	  }
	
	//实现用户注册
	@RequestMapping("doRegister")
	@ResponseBody
	public SysResult saveUser(User user){
		
		String username = null;
		
		try {
			username=userService.saveUser(user);
			if(!StringUtils.isEmpty(username)){
				return SysResult.oK(username);
				
			}
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		return SysResult.build(201,"注册失败", user.getUsername());
	}
	
	
	    //用户登陆  http://www.jt.com/service/user/doLogin?r=0.582247581950398
		//通过login.jsp检测登陆的username和password是否正确
		@RequestMapping("/doLogin")
		@ResponseBody
		public SysResult doLogin(String username,String password,
				HttpServletRequest request,HttpServletResponse response){
			//判断用户名和密码是否为null
			if(StringUtils.isEmpty(username) || StringUtils.isEmpty(password)){
				return SysResult.build(201, "用户名密码不能为空");
			}
			//当前输入的用户名是正确的
			try {
				//获取用户的ticket
				String ticket = 
						userService.findUserByUP(username,password);
				//ticket不为空
				if(!StringUtils.isEmpty(ticket)){
					//如果ticket数据不为空 则写入cookie
					//Cookie[] cookies = request.getCookies();
					//Cookie的名称必须为 JT_TICKET
				CookieUtils.setCookie(request, response, "JT_TICKET", ticket);
					return SysResult.oK(ticket);
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
			return SysResult.build(201, "用户登陆失败");	
		}
		//用户的登出操作  //user/logout.html
		//1.cookie中获取ticket信息
		//2.删除redis缓存
		//3.删除cookie信息
		//4.跳转页面到系统欢迎页面
     @RequestMapping("/logout")		
     public String logout(HttpServletRequest request,HttpServletResponse response){
    	 String ticket = CookieUtils.getCookieValue(request,"JT_TICKET");
 		   jedisCluster.del(ticket);
 		CookieUtils.deleteCookie(request, response, "JT_TICKET");
 		
 		//通过重定向的方式返回系统首页  
 		return "redirect:/index.html";
     
     }
	
}
