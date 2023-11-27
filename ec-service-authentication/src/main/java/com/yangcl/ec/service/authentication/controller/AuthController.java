package com.yangcl.ec.service.authentication.controller;

import com.yangcl.ec.common.entity.common.AppEnum;
import com.yangcl.ec.common.entity.common.JsonResult;
import com.yangcl.ec.common.entity.common.LoginAccount;
import com.yangcl.ec.service.authentication.common.AccountFactory;
import com.yangcl.ec.service.authentication.common.AccountRepository;
import com.yangcl.ec.service.authentication.common.JwtUtil;
import com.yangcl.ec.service.authentication.common.MemoryAccountRepositoryImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import sun.rmi.runtime.Log;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * 帐户登录/验证REST服务
 */
@Controller
public class AuthController {

    @Autowired
    private JwtUtil jwtUtil;

    //导入配置-在线用户仓库实现名
    @Value("${account.online.repository}")
    private String accountRepositoryName;

    //导入配置-是否单会话模式
    @Value("${account.online.single}")
    private Boolean accountSingle;

    //导入配置-是否刷新token
    @Value("${account.jwt.refreshExpiration}")
    private Boolean refreshExpirationJwt;

    //导入配置-是否刷新在线帐户过期时间
    @Value("${account.online.refreshExpiration}")
    private Boolean refreshExpirationOnline;

    //导入配置-在线帐户过期时间
    @Value("${account.online.expiration}")
    private Long accountExpiration;

    //在线用户仓库接口
    private AccountRepository accountRepository;

    //初始化方法，概据配置仓库名实例化相应的仓库实现类，有Memory,Redis,MySQL三种实现
    @PostConstruct
    public void  init(){
        accountRepository=AccountFactory.getInstance(accountRepositoryName);
    }

    //线程同步锁
    private Lock lock=new ReentrantLock();


    /**
     * 登录
     * @param loginAccount 帐户
     * @return JsonResult
     */
    @RequestMapping(value = "/auth/account/login",method = RequestMethod.POST)
    @ResponseBody
    public JsonResult<LoginAccount> loginIn(@RequestBody LoginAccount loginAccount){

        //返回包装类型
        JsonResult<LoginAccount> jsonResult=new JsonResult<LoginAccount>();
        jsonResult.setCode(AppEnum.AuthResultCode.LoginSuccess.getCode());
        jsonResult.setMessage(AppEnum.AuthResultCode.LoginSuccess.getMessage());

        //参数校验
        if(loginAccount==null ||
                loginAccount.getAccountId()==null ||
                "".equals(loginAccount.getAccountId()) ||
                loginAccount.getSysName()==null ||
                "".equals(loginAccount.getSysName())){
            jsonResult.setCode(AppEnum.AuthResultCode.InputNull.getCode());
            jsonResult.setMessage(AppEnum.AuthResultCode.InputNull.getMessage());
            return jsonResult;
        }

        //获取登录在线帐户
        LoginAccount onlineAccount=accountRepository.getAccount(loginAccount.getAccountId(),loginAccount.getSysName());
        loginAccount.setToken(jwtUtil.createdToken(loginAccount));//设置token
        loginAccount.setExpirationTime(new Date(System.currentTimeMillis()+accountExpiration*1000));//设置过期时间
        if(onlineAccount!=null){
            //判断是否单会话登录，单会话登录会顶掉之前的会话
            if(accountSingle){
                accountRepository.updateAccount(loginAccount);//更新登录信息
            }else{
                accountRepository.addAccount(loginAccount);//新增登录信息
            }

        }else{
            //未登录过，添加到在线用户
            accountRepository.addAccount(loginAccount);//加入登录信息
        }
        jsonResult.setEntity(loginAccount);
        return jsonResult;
    }

    @RequestMapping(value = "/auth/account/validate",method = RequestMethod.POST)
    @ResponseBody
    public JsonResult<LoginAccount> loginValidate(@RequestBody String token){
        //创建返回包装类型
        JsonResult<LoginAccount> jsonResult = new JsonResult<LoginAccount>();
        //参数校验
        if (token == null || "".equals(token)) {
            jsonResult.setCode(AppEnum.AuthResultCode.InputNull.getCode());
            jsonResult.setMessage(AppEnum.AuthResultCode.InputNull.getMessage());
            return jsonResult;
        }
        //验证token
        LoginAccount accountResult = jwtUtil.getLoginAccountFromToken(token);
        if (accountResult == null) {
            jsonResult.setCode(AppEnum.AuthResultCode.TokenError.getCode());
            jsonResult.setMessage(AppEnum.AuthResultCode.TokenError.getMessage());
            return jsonResult;
        }
        //查找是否在线
        accountResult = accountRepository.getAccount(accountResult.getAccountId(),accountResult.getSysName(), token, accountExpiration);
        if (accountResult == null) {
            jsonResult.setCode(AppEnum.AuthResultCode.AccountNull.getCode());
            jsonResult.setMessage(AppEnum.AuthResultCode.AccountNull.getMessage());
            return jsonResult;
        }
        //验证成功
        jsonResult.setCode(AppEnum.AuthResultCode.ValidateSuccess.getCode());
        jsonResult.setMessage(AppEnum.AuthResultCode.ValidateSuccess.getMessage());
        jsonResult.setEntity(accountResult);
        return jsonResult;
    }

    @RequestMapping(value = "/auth/account/refresh",method = RequestMethod.POST)
    @ResponseBody
    public JsonResult<LoginAccount> refreshAccount(@RequestBody String token){
        //创建返回包装类型
        JsonResult<LoginAccount> jsonResult = new JsonResult<LoginAccount>();

        //采用刷新token模式，防止多线程并发更新问题需要同步锁
        if(this.refreshExpirationJwt){
            lock.lock();
        }
        try {
            //参数校验
            if (token == null || "".equals(token)) {
                jsonResult.setCode(AppEnum.AuthResultCode.InputNull.getCode());
                jsonResult.setMessage(AppEnum.AuthResultCode.InputNull.getMessage());
                return jsonResult;
            }
            //验证token
            LoginAccount accountResult = jwtUtil.getLoginAccountFromToken(token);
            if (accountResult == null) {
                jsonResult.setCode(AppEnum.AuthResultCode.TokenError.getCode());
                jsonResult.setMessage(AppEnum.AuthResultCode.TokenError.getMessage());
                return jsonResult;
            }
            //查找是否在线
            accountResult = accountRepository.getAccount(accountResult.getAccountId(), accountResult.getSysName(), token, accountExpiration);
            if (accountResult == null) {
                jsonResult.setCode(AppEnum.AuthResultCode.AccountNull.getCode());
                jsonResult.setMessage(AppEnum.AuthResultCode.AccountNull.getMessage());
                return jsonResult;
            }
            //刷新token
            if (this.refreshExpirationJwt) {
                accountResult.setToken(jwtUtil.refreshToken(accountResult.getToken()));
            }
            //刷新online
            if (this.refreshExpirationOnline) {
                accountRepository.updateAccount(accountResult, token);
            }
            //验证成功
            jsonResult.setCode(AppEnum.AuthResultCode.ValidateSuccess.getCode());
            jsonResult.setMessage(AppEnum.AuthResultCode.ValidateSuccess.getMessage());
            jsonResult.setEntity(accountResult);
        }finally {
            if(this.refreshExpirationJwt){
                lock.unlock();
            }
        }
        return jsonResult;
    }
}
