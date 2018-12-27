package com.github.fengleicn.dht.starter;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

@Component
public class MyApplicationRunner implements ApplicationRunner {
    public static String myIp;
    public static String myPort;

    @Override
    public void run(ApplicationArguments args) throws Exception {
        new TaskManager(myIp, Integer.valueOf(myPort)).run();
    }
}
