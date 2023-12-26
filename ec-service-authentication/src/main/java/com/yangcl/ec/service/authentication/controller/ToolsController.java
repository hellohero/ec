package com.yangcl.ec.service.authentication.controller;

import com.yangcl.ec.common.entity.common.LoginAccount;
import com.yangcl.ec.common.entity.common.TokenSession;
import com.yangcl.ec.service.authentication.common.AccountFactory;
import com.yangcl.ec.service.authentication.common.AccountRepository;
import com.yangcl.ec.service.authentication.common.SessionFactory;
import com.yangcl.ec.service.authentication.common.SessionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import javax.annotation.PostConstruct;
import java.util.List;

@Controller
public class ToolsController {

    //导入配置-在线用户仓库实现名
    @Value("${account.online.repository}")
    private String accountRepositoryName;

    private AccountRepository accountRepository;

    private SessionRepository sessionRepository;

    //初始化方法，概据配置仓库名实例化相应的仓库实现类，有Memory,Redis,MySQL三种实现
    @PostConstruct
    public void  init(){
        accountRepository= AccountFactory.getInstance(accountRepositoryName);
        sessionRepository= SessionFactory.getInstance(accountRepositoryName);
    }


    @GetMapping(value = "/tools/accounts")
    public String accountList(Model model){
        List<LoginAccount> accounts=accountRepository.getAccounts();
        model.addAttribute("accounts",accounts);
        return "accounts";
    }

    @GetMapping(value = "/tools/account/remove")
    public String removeAccount(@RequestParam String accountId,@RequestParam String sysName,@RequestParam String token){
        accountRepository.removeAccount(accountId,sysName,token);
        return "redirect:/tools/accounts";
    }

    @GetMapping(value = "/tools/sessions")
    public String sessionList(Model model){
        List<TokenSession> sessions=sessionRepository.getSessions();
        model.addAttribute("sessions",sessions);
        return "sessionList";
    }
}
