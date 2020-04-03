package com.tydic.bp.util;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

@Component
public class IOCUtils implements ApplicationContextAware {

    private static ApplicationContext applicationContext;

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        if (IOCUtils.applicationContext == null) {
            IOCUtils.applicationContext = applicationContext;
        }
    }

    public static ApplicationContext getApplicationContext() {
        return applicationContext;
    }

    public static Object getBean(String id){
        return applicationContext.getBean(id);
    }

    public static <T> T getBean(Class<T> clas){
        return applicationContext.getBean(clas);
    }

    public static <T> T getBean(String name,Class<T> clas){
        return applicationContext.getBean(name,clas);
    }
}
