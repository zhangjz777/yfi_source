package com.zzz.util;


import com.alibaba.fastjson.JSONObject;
import org.apache.http.HttpEntity;
import org.apache.http.HttpHost;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicHeader;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class HttpClientUtil {

    /**
     * 连接超时时间
     */
    private static int CONNECT_TIME_OUT = 15 * 1000;
    /**
     * 读取超时时间40秒
     */
    private static int READ_TIME_OUT = 30 * 1000;


    public static String doGet(String url, boolean useProxy) {
        CloseableHttpClient httpClient = null;
        CloseableHttpResponse response = null;
        String result = "";
        try {
            httpClient = HttpClients.custom().build();
            // 创建httpGet远程连接实例
            HttpGet httpGet = new HttpGet(url);
            // 设置配置请求参数
            // 连接主机服务超时时间
            RequestConfig requestConfig = RequestConfig.custom().setConnectTimeout(60000)
                    // 请求超时时间
                    .setConnectionRequestTimeout(60000)
                    // 数据读取超时时间
                    .setSocketTimeout(60000)
                    .build();
            // 为httpGet实例设置配置
            httpGet.setConfig(requestConfig);
            // 执行get请求得到返回对象
            response = httpClient.execute(httpGet);
            // 通过返回对象获取返回数据
            HttpEntity entity = response.getEntity();
            // 通过EntityUtils中的toString方法将结果转换为字符串
            result = EntityUtils.toString(entity);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            // 关闭资源
            if (null != response) {
                try {
                    response.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (null != httpClient) {
                try {
                    httpClient.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return result;
    }


    /**
     * 发送post 请求（json格式）
     *
     * @param reqURL
     * @param jsonDataStr
     * @return result
     */
    public static String postJson(String reqURL, String jsonDataStr) {
        CloseableHttpClient httpClient = null;
        HttpPost httpPost = null;
        String responseResult = null;
        CloseableHttpResponse response = null;
        try {
            HttpHost proxy = new HttpHost("127.0.0.1", 64562, "http");
            CredentialsProvider provider = new BasicCredentialsProvider();
            provider.setCredentials(new AuthScope(proxy), new UsernamePasswordCredentials("Wei.liu@automax-singapore.com", "hema6666!"));
            httpClient = HttpClients.custom().setDefaultCredentialsProvider(provider).build();
            httpPost = new HttpPost(reqURL);
            RequestConfig requestConfig = RequestConfig.custom()
                    // 连接一个url的连接等待时间
                    .setConnectTimeout(CONNECT_TIME_OUT)
                    .setConnectionRequestTimeout(CONNECT_TIME_OUT)
                    // 读取数据超时
                    .setSocketTimeout(READ_TIME_OUT)
                    .setProxy(proxy)
                    .build();
            httpPost.setConfig(requestConfig);
            httpPost.setHeader(HTTP.CONTENT_TYPE, "application/json");
            StringEntity entity = new StringEntity(jsonDataStr, "UTF-8");
            entity.setContentEncoding(new BasicHeader(HTTP.CONTENT_ENCODING, "UTF-8"));
            entity.setContentType("application/json");
            httpPost.setEntity(entity);
            response = httpClient.execute(httpPost);
            HttpEntity httpEntity = response.getEntity();
            if (httpEntity != null) {
                responseResult = EntityUtils.toString(httpEntity, "UTF-8");
                EntityUtils.consume(httpEntity);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            closeHttp(response, httpClient);
        }
        return responseResult;
    }

    public static String postJsonNotProxy(String reqURL, String jsonDataStr, String token) {
        CloseableHttpClient httpClient = null;
        HttpPost httpPost = null;
        String responseResult = null;
        CloseableHttpResponse response = null;
        try {
            httpClient = HttpClients.custom().build();
            httpPost = new HttpPost(reqURL);
            RequestConfig requestConfig = RequestConfig.custom()
                    // 连接一个url的连接等待时间
                    .setConnectTimeout(CONNECT_TIME_OUT)
                    .setConnectionRequestTimeout(CONNECT_TIME_OUT)
                    // 读取数据超时
                    .setSocketTimeout(READ_TIME_OUT)
                    .build();
            httpPost.setConfig(requestConfig);
            httpPost.setHeader(HTTP.CONTENT_TYPE, "application/json");
            httpPost.setHeader("Authorization", token);
            StringEntity entity = new StringEntity(jsonDataStr, "UTF-8");
            entity.setContentEncoding(new BasicHeader(HTTP.CONTENT_ENCODING, "UTF-8"));
            entity.setContentType("application/json");
            httpPost.setEntity(entity);
            response = httpClient.execute(httpPost);
            HttpEntity httpEntity = response.getEntity();
            if (httpEntity != null) {
                responseResult = EntityUtils.toString(httpEntity, "UTF-8");
                EntityUtils.consume(httpEntity);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            closeHttp(response, httpClient);
        }
        return responseResult;
    }

    public static String doPostForm(String httpUrl, Map param) {
        HttpURLConnection connection = null;
        InputStream is = null;
        OutputStream os = null;
        BufferedReader br = null;
        String result = null;
        try {
            URL url = new URL(httpUrl);
            // 通过远程url连接对象打开连接
            String host = "127.0.0.1";
            String port = "1087";
            connection = (HttpURLConnection) url.openConnection(new Proxy(Proxy.Type.HTTP, new InetSocketAddress(host, Integer.parseInt(port))));
            // 不需要设置代理，直接请求即可
//            connection = (HttpURLConnection) url.openConnection();
            // 设置连接请求方式
            assert connection != null;
            connection.setRequestMethod("POST");
            // 设置连接主机服务器超时时间：15000毫秒
            connection.setConnectTimeout(15000);
            // 设置读取主机服务器返回数据超时时间：60000毫秒
            connection.setReadTimeout(60000);
            // 默认值为：false，当向远程服务器传送数据/写数据时，需要设置为true
            connection.setDoOutput(true);
            // 默认值为：true，当前向远程服务读取数据时，设置为true，该参数可有可无
            connection.setDoInput(true);
            // 设置传入参数的格式:请求参数应该是 name1=value1&name2=value2 的形式。
            connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
            // 通过连接对象获取一个输出流
            os = connection.getOutputStream();
            // 通过输出流对象将参数写出去/传输出去,它是通过字节数组写出的(form表单形式的参数实质也是key,value值的拼接，类似于get请求参数的拼接)
            os.write(createLinkString(param).getBytes());
            // 通过连接对象获取一个输入流，向远程读取
            if (connection.getResponseCode() == 200) {
                is = connection.getInputStream();
                // 对输入流对象进行包装:charset根据工作项目组的要求来设置
                br = new BufferedReader(new InputStreamReader(is, "UTF-8"));
                StringBuilder sbf = new StringBuilder();
                String temp;
                // 循环遍历一行一行读取数据
                while ((temp = br.readLine()) != null) {
                    sbf.append(temp);
                    sbf.append("\r\n");
                }
                result = sbf.toString();
            }
        } catch (IOException e) {
            LogUtil.error(e);
        } finally {
            // 关闭资源
            if (null != br) {
                try {
                    br.close();
                } catch (IOException e) {
                    LogUtil.error(e);
                }
            }
            if (null != os) {
                try {
                    os.close();
                } catch (IOException e) {
                    LogUtil.error(e);
                }
            }
            if (null != is) {
                try {
                    is.close();
                } catch (IOException e) {
                    LogUtil.error(e);
                }
            }
            // 断开与远程地址url的连接
            assert connection != null;
            connection.disconnect();
        }
        return result;
    }

    private static String createLinkString(Map<String, String> params) {
        List<String> keys = new ArrayList<>(params.keySet());
        Collections.sort(keys);
        StringBuilder prestr = new StringBuilder();
        for (int i = 0; i < keys.size(); i++) {
            String key = keys.get(i);
            String value = params.get(key);
            // 拼接时，不包括最后一个&字符
            if (i == keys.size() - 1) {
                prestr.append(key).append("=").append(value);
            } else {
                prestr.append(key).append("=").append(value).append("&");
            }
        }
        return prestr.toString();
    }

    private static void closeHttp(CloseableHttpResponse response, CloseableHttpClient httpClient) {
        try {
            if (response != null) {
                response.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            if (httpClient != null) {
                httpClient.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
