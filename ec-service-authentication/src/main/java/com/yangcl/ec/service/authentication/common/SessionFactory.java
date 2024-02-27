package com.yangcl.ec.service.authentication.common;

public class SessionFactory {

    //在线帐户仓库接口
    private SessionRepository sessionRepository;

    //根据名称实例化不同的仓库实现，默认用Memory实现
    public static SessionRepository getInstance(String sessionRepositoryName){
        //根据配置创建不同的在线会员仓库实现对象
        if("MemoryAccountRepositoryImpl".equals(sessionRepositoryName)){
            //内存
            return new MemorySessionRepositoryImpl();
        }else{
            return new MemorySessionRepositoryImpl();
        }
    }
}
