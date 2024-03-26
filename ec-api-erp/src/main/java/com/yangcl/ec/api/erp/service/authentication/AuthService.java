package com.yangcl.ec.api.erp.service.authentication;

import com.yangcl.ec.common.entity.common.JsonResult;
import com.yangcl.ec.common.entity.common.LoginAccount;
import org.springframework.cloud.netflix.feign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import java.util.Map;
@FeignClient(value = "ec-service-authentication",fallback = AuthServiceHystrix.class)
public interface AuthService {
    @PostMapping(value = "/auth/account/login")
    public JsonResult<LoginAccount> loginIn(LoginAccount loginAccount);
    @PostMapping(value = "/auth/account/validate")
    public JsonResult<LoginAccount> loginValidate(String token);
    @PostMapping(value = "/auth/account/refresh")
    public JsonResult<LoginAccount> refreshAccount(String token);
}
