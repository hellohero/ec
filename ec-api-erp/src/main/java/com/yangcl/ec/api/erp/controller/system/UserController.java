package com.yangcl.ec.api.erp.controller.system;

import com.yangcl.ec.api.erp.common.AuthPassport;
import com.yangcl.ec.api.erp.service.authentication.AuthService;
import com.yangcl.ec.api.erp.service.erp.UserService;
import com.yangcl.ec.common.entity.erp.domain.Role;
import com.yangcl.ec.common.entity.erp.domain.User;
import com.yangcl.ec.common.entity.common.JsonResult;
import com.yangcl.ec.common.entity.common.LoginAccount;
import com.yangcl.ec.common.entity.erp.dto.UserDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import zipkin.internal.moshi.Json;

import javax.servlet.http.HttpServletRequest;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
//@CrossOrigin(origins = "http://localhost:8080",maxAge = 3600)
public class UserController {

    @Autowired
    private UserService userService;
    @Autowired
    private AuthService authService;

    @RequestMapping(value = "/login",method = RequestMethod.POST)
    public JsonResult<UserDto> userLogin(HttpServletRequest request,@RequestBody User user){
        if(user==null || user.getLoginName()==null || user.getLoginName()==""){
            return new JsonResult<UserDto>("400","帐号不能为空！");
        }
        if(user==null || user.getLoginPwd()==null || user.getLoginPwd()==""){
            return new JsonResult<UserDto>("400","密码不能为空！");
        }

        User result=userService.getByUsernameAndPassword(user.getLoginName(),user.getLoginPwd());


        if(result==null){
            return new JsonResult<UserDto>("400","帐号或密码错误，登录失败！");
        }else{
            //创建DTO对象
            UserDto userDto=new UserDto();
            userDto.setUserId(result.getSysno().toString());
            userDto.setUserName(result.getEmployee().getEmployeeName());
            userDto.setLoginName(result.getLoginName());
            userDto.setMenus(result.getRoles());
            //登录
            LoginAccount loginAccount=new LoginAccount();
            loginAccount.setAccountId(result.getSysno().toString());
            loginAccount.setAccountName(result.getEmployee().getEmployeeName());
            loginAccount.setOtherName("");
            loginAccount.setPassword("");
            loginAccount.setSysName("erp");
            loginAccount.setUsername(result.getLoginName());
            loginAccount.setPermissions(result.getPermissionStringList());
            loginAccount.setLastLoginTime(new Date());
            loginAccount.setLastLoginIP( request.getHeader("X-Real-IP"));

            JsonResult<LoginAccount> loginResult=authService.loginIn(loginAccount);
            if("200".equals(loginResult.getCode())){
                userDto.setToken(loginResult.getEntity().getToken());
                return new JsonResult<UserDto>(loginResult.getCode(),loginResult.getMessage(),loginResult.getEntity().getToken(),userDto);
            }else{
                return new JsonResult<UserDto>(loginResult.getCode(),loginResult.getMessage(),"",userDto);
            }
        }
    }
}
