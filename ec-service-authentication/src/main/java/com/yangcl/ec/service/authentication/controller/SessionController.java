package com.yangcl.ec.service.authentication.controller;

import com.yangcl.ec.common.entity.common.AppEnum;
import com.yangcl.ec.common.entity.common.JsonResult;
import com.yangcl.ec.common.entity.common.LoginAccount;
import com.yangcl.ec.common.entity.common.TokenSession;
import com.yangcl.ec.common.entity.common.appenum.TokenSessionStatus;
import com.yangcl.ec.service.authentication.common.*;
import jdk.nashorn.internal.parser.Token;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.annotation.PostConstruct;
import javax.xml.ws.RequestWrapper;
import java.util.Date;

@Controller
public class SessionController {

    @Autowired
    private JwtUtil jwtUtil;

    //导入配置-是否单会话模式
    @Value("${account.online.single}")
    private Boolean accountSingle;

    //导入配置-在线帐户过期时间
    @Value("${account.online.expiration}")
    private Long accountExpiration;

    //导入配置-在线用户仓库实现名
    @Value("${account.online.repository}")
    private String accountRepositoryName;

    //导入配置-是否刷新token
    @Value("${account.jwt.refreshExpiration}")
    private Boolean refreshExpiration;

    //在线用户仓库接口
    private AccountRepository accountRepository;

    private SessionRepository sessionRepository;

    //初始化方法，概据配置仓库名实例化相应的仓库实现类，有Memory,Redis,MySQL三种实现
    @PostConstruct
    public void  init(){
        accountRepository= AccountFactory.getInstance(accountRepositoryName);
        sessionRepository= SessionFactory.getInstance(accountRepositoryName);
    }


    @PostMapping(value = "/auth/session/created")
    @ResponseBody
    public String createdSession(@RequestBody TokenSession session){
        //创建token
        session.setToken(jwtUtil.createdToken(session));
        //设置帐户过期时间
        session.setExpirationTime(new Date(System.currentTimeMillis()+accountExpiration*1000));
        //设置会话状态为正常
        session.setSessionStatus(TokenSessionStatus.Normal.toString());
        //判断帐户是否在线
        if(sessionRepository.getSession(session.getSessionId(),session.getSystem())!=null){
            //如果帐户在线
            if(accountSingle){
                //如果单会话模式，更新会话
                sessionRepository.updateSession(session);
            }else{
                //如果多会话模式，新增会话
                sessionRepository.addSession(session);
            }
        }else{
            //不在线，新增会话
            sessionRepository.addSession(session);
        }
        return session.getToken();
    }

    @PostMapping(value = "/auth/session/validate")
    @ResponseBody
    public TokenSession sessionValidate(@RequestBody String token){
        TokenSession tokenSession=jwtUtil.getSessionFromToken(token);
        TokenSession onlineSession=sessionRepository.getSession(tokenSession==null?"":tokenSession.getSessionId(),
                tokenSession==null?"":tokenSession.getSystem(),token);
        if(onlineSession!=null){
            //验证token有效期是否小于3分钟与配置是否刷新token，则生成刷新token
            Long timeMillis=jwtUtil.getExpirationDateFromToken(token).getTime()-System.currentTimeMillis();
            if(timeMillis>0 && timeMillis<180000000 && refreshExpiration && onlineSession.getSessionStatus()==TokenSessionStatus.Normal.toString()){
                //更新旧会话状态为将过期
                onlineSession.setSessionStatus(TokenSessionStatus.Overdue.toString());
                sessionRepository.updateSession(onlineSession);
                //加入新会话状态为正常
                onlineSession.setToken(jwtUtil.refreshToken(token));
                onlineSession.setSessionStatus(TokenSessionStatus.Normal.toString());
                sessionRepository.addSession(onlineSession);//加入新的会话
                onlineSession.setRefreshToken(onlineSession.getToken());
            }else{
                onlineSession.setRefreshToken(null);
            }
        }
        return onlineSession;
    }

    @GetMapping(value = "/test")
    @ResponseBody
    public String test(){

        TokenSession session=new TokenSession();
        session.setSessionId("1");
        session.setSystem("erp");
        String token=this.createdSession(session);
        return token;
    }

    @GetMapping(value = "/test2")

    public String test2(@RequestParam String token){
        TokenSession result=this.sessionValidate(token);


        result=this.sessionValidate(token);



        result=this.sessionValidate(token);

        return "";
    }
}
