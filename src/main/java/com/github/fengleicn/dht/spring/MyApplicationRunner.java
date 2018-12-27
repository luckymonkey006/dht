package com.github.fengleicn.dht.spring;

import com.github.fengleicn.dht.modules.StartUp;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

@Component
public class MyApplicationRunner implements ApplicationRunner {
    public static String myIp;
    public static String myPort;

    @Override
    public void run(ApplicationArguments args) throws Exception {
        new StartUp(myIp, Integer.valueOf(myPort)).startUp();
    }
}
