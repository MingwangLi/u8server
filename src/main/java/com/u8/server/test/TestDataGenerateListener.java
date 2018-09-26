package com.u8.server.test;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

/**
 * Created by ant on 2016/9/21.
 */
public class TestDataGenerateListener implements ServletContextListener{
    @Override
    public void contextInitialized(ServletContextEvent sce) {

        TestApi.runTestData();

    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {

    }
}
