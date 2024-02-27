package com.yangcl.ec.api.erp.common;

import com.netflix.ribbon.proxy.annotation.Http;
import com.yangcl.ec.api.erp.service.authentication.AuthService;
import com.yangcl.ec.common.entity.common.JsonResult;
import com.yangcl.ec.common.entity.common.LoginAccount;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;
import sun.rmi.runtime.Log;

import javax.servlet.*;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Order(1)
@WebFilter(filterName = "ValidateTokenFilter")
public class ValidateTokenFilter implements Filter {

    private static List<String> removes=new ArrayList<String>();

    @Autowired
    private AuthService authService;

    public void init(FilterConfig filterConfig) throws ServletException {
        removes.add("info");
        removes.add("login");
    }

    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        HttpServletRequest request=(HttpServletRequest)servletRequest;
        HttpServletResponse response=(HttpServletResponse)servletResponse;
        //充许跨域，*为不限
        response.setHeader("Access-Control-Allow-Origin", "*");
        //预简请求响应，设置充许的头和方法
        if("OPTIONS".equals(request.getMethod())){
            response.setHeader("Access-Control-Allow-Headers", "Authorization,Origin, X-Requested-With, Content-Type, Accept");
            response.setHeader("Access-Control-Allow-Methods","POST,GET,OPTIONS");
            response.setHeader("Access-Control-Max-Age","3600");
            response.setContentType("application/json;charset=UTF-8");
            PrintWriter out=response.getWriter();
            out.write("{\"code\":\"444\",\"message\":\"预检请求响应\"}");
            out.close();
            filterChain.doFilter(request,response);
            return;
        }

        String url=request.getRequestURI().substring(request.getContextPath().length());
        if(url.startsWith("/") && url.length()>1){
            url=url.substring(1);
        }

        if(isInclude(url)){
            filterChain.doFilter(request,response);
            return;
        }else{
            String token=request.getHeader("Authorization");
            JsonResult<LoginAccount> loginResult=authService.loginValidate(token==null?"error":token);
            if("200".equals(loginResult.getCode())){
                loginResult=authService.refreshAccount(loginResult.getEntity().getToken());
                response.setHeader("Authorization",loginResult.getEntity().getToken());
                filterChain.doFilter(request,response);
                return;
            }
            else{
                response.setContentType("application/json;charset=UTF-8");
                PrintWriter out=response.getWriter();
                out.write("{\"code\":\""+loginResult.getCode()+"\",\"message\":\""+loginResult.getMessage()+"\"}");
                out.close();
                filterChain.doFilter(request,response);
                return;
            }
        }
    }

    private boolean isInclude(String url){
        for(String remove:removes){
            if(remove.equals(url)){
                return true;
            }
        }
        return false;
    }

    public void destroy() {
    }
}
