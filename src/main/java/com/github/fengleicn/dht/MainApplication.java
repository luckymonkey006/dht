package com.github.fengleicn.dht;


import com.alibaba.fastjson.JSON;
import com.github.fengleicn.dht.starter.TaskManager;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class MainApplication {
    public static void main(String[] args) throws Exception {
        CloseableHttpClient closeableHttpClient = HttpClients.createDefault();
        HttpPost httpPost = new HttpPost("http://pv.sohu.com/cityjson?ie=utf-8");
        HttpResponse httpResponse = closeableHttpClient.execute(httpPost);
        String resPespString = EntityUtils.toString(httpResponse.getEntity());
        String respJson = resPespString.split(" = ")[1];
        respJson = respJson.substring(0, respJson.length() - 1);
        String myIp = JSON.parseObject(respJson).getString("cip");
        String myPort = "6883";
        LoggerFactory.getLogger(MainApplication.class).info("My ip address is " + myIp + " port is " + myPort + '.');
        new TaskManager(myIp, Integer.valueOf(myPort)).run();
        SpringApplication.run(MainApplication.class);
    }
}
