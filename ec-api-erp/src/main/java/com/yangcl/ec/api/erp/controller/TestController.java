package com.yangcl.ec.api.erp.controller;

import com.yangcl.ec.api.erp.service.authentication.AuthService;
import com.yangcl.ec.common.entity.common.JsonResult;
import com.yangcl.ec.common.entity.common.LoginAccount;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import sun.rmi.runtime.Log;

import java.util.ArrayList;
import java.util.Date;

@Controller
public class TestController {

    @Autowired
    private AuthService authService;

    @GetMapping(value = "/test/login")
    @ResponseBody
    public String testLogin(){
        return "Login Page";
    }

    @GetMapping(value = "/test/index")
    @ResponseBody
    public String testIndex(){
        return "Index Page";
    }

    @GetMapping(value = "/test/error")
    @ResponseBody
    public String testError(){
        return "Error Page";
    }

    @GetMapping(value = "/test")
    @ResponseBody
    public String test(){
        LoginAccount loginAccount=new LoginAccount();
        loginAccount.setAccountId("1");
        loginAccount.setUsername("admin");
        loginAccount.setSysName("erp");
        loginAccount.setAccountName("accountName");
        loginAccount.setOtherName("");
        loginAccount.setPassword("");
        loginAccount.setLastLoginTime(new Date());
        loginAccount.setPermissions(new ArrayList<String>());

        JsonResult<LoginAccount> loginAccountJsonResult=authService.loginIn(loginAccount);
        JsonResult<LoginAccount> result=authService.loginValidate(loginAccountJsonResult.getEntity().getToken());
        return "";
    }
}
