package com.github.fengleicn.dht;


import com.alibaba.fastjson.JSON;
import com.github.fengleicn.dht.modules.StartUp;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

public class MainApplication {
    public static void main(String[] args) throws Exception {
        System.out.println("[INFO] running...");
        String myIp;
        String myPort;
        CloseableHttpClient closeableHttpClient = HttpClients.createDefault();
        HttpPost httpPost = new HttpPost("http://pv.sohu.com/cityjson?ie=utf-8");
        HttpResponse httpResponse = closeableHttpClient.execute(httpPost);
        String resPespString = EntityUtils.toString(httpResponse.getEntity());
        String respJson = resPespString.split(" = ")[1];
        respJson = respJson.substring(0, respJson.length() - 1);
        myIp = JSON.parseObject(respJson).getString("cip");
        myPort = "6883";
        new StartUp(myIp, Integer.valueOf(myPort)).startUp();
    }
}
