package com.notebook.cloud.util;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

@Component
public class PropertyReader {

    @Autowired
    private Environment env;

    private static Environment envProxy = null;

    @PostConstruct
    public void init()
    {
        envProxy = env;
    }

    public static String getProperty(String key)
    {
        String value = null;
        if (null != envProxy){
            value = envProxy.getProperty(key);
        }
        return value;
    }

    public static void main(String[] args) {
        System.out.println(getProperty("project.path"));
    }

}
