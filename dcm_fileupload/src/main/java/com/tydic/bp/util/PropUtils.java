package com.tydic.bp.util;

import org.springframework.context.EnvironmentAware;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

@Component
public class PropUtils implements EnvironmentAware {

    private static Environment environment;

    @Override
    public void setEnvironment(Environment environment) {
        if (PropUtils.environment == null) {
            PropUtils.environment = environment;
        }
    }

    public static String getProperty(String key){
        return environment.getProperty(key);
    }
}
