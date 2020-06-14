package com.zzz.service;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.gargoylesoftware.htmlunit.BrowserVersion;
import com.gargoylesoftware.htmlunit.ProxyConfig;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.zzz.model.FacebookVideo;
import com.zzz.util.AppUtil;
import com.zzz.util.DateUtil;
import com.zzz.util.HttpClientUtil;
import com.zzz.util.LogUtil;
import org.apache.commons.lang3.RandomUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.regexp.RE;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * @author ZhangJZ
 * @date 10:20 下午  2020/6/4
 * @desc facebook资源获取实现类
 */
@Service
public class FacebookResourceServiceImpl {


    /**
     * @desc 获取首页数据
     * @param channelId channelId
     * @return 关键数据节点信息
     */
    public String getVideoByChannelId(String channelId) {
        try {
            Thread.sleep(RandomUtils.nextLong(3, 8) * 1000);
            // 构造WebClient 模拟Chrome 浏览器
            WebClient webClient = new WebClient(BrowserVersion.CHROME);
            // 支持JavaScript
            webClient.getOptions().setJavaScriptEnabled(true);
            webClient.getOptions().setCssEnabled(false);
            webClient.getOptions().setActiveXNative(false);
            webClient.getOptions().setCssEnabled(false);
            webClient.getOptions().setThrowExceptionOnScriptError(false);
            webClient.getOptions().setThrowExceptionOnFailingStatusCode(false);
            webClient.getOptions().setTimeout(15000);
            String host = "127.0.0.1";
            String port = "1087";
            System.setProperty("http.proxyHost", host);
            System.setProperty("http.proxyPort", port);
            System.setProperty("https.proxyHost", host);
            System.setProperty("https.proxyPort", port);
            ProxyConfig proxyConfig = new ProxyConfig();
            proxyConfig.setProxyHost(host);
            proxyConfig.setProxyPort(Integer.parseInt(port));
            // 设置WebClient的代理
            webClient.getOptions().setProxyConfig(proxyConfig);
            // 拼接你要访问的视频首页地址
            HtmlPage rootPage = webClient.getPage("https://www.facebook.com/watch/" + channelId + "/");
            //设置一个运行JavaScript的时间
            webClient.waitForBackgroundJavaScript(5000);
            String html = rootPage.asXml();
            Document document = Jsoup.parse(html);
            if (document == null) {
                LogUtil.error("获取facebook页面失败.channelId:" + channelId);
                return null;
            }
            Elements scripts = document.getElementsByTag("script");
            if (AppUtil.isNull(scripts)) {
                LogUtil.error("获取facebook页面失败.channelId:" + channelId);
                return null;
            }
            String sourceStr = null;
            for (Element script : scripts) {
                // 只截取script节点的前面一部分，节省匹配时间
                String scriptHtml = script.html().substring(0, Math.min(script.html().length(), 200));
                // 前几条视频所在的节点，可以使用浏览器分析dom找到
                if (scriptHtml.contains("bigPipe.onPageletArrive({bootloadable:{\"CometVideoHomeSottoCatalogNonSubscriberUpsellSection")) {
                    sourceStr = script.html();
                    break;
                }
            }
            executeHomePageData(sourceStr);
            return sourceStr;
        } catch (Exception e) {
            LogUtil.error(e);
        }
        return null;
    }

    /**
     * @desc 处理首页的数据
     * @param sourceStr 首页的节点数据
     * @return 最终获取的视频信息
     */
    private String executeHomePageData(String sourceStr) {
        if (AppUtil.isNull(sourceStr)) {
            return "获取首页数据为空";
        }
        try {
            // 获取首页数据
            int sourceStart = sourceStr.indexOf("section_components:{edges:") + 26;
            int sourceEnd = sourceStr.indexOf(",page_info:{has_next_page");
            String jsonData = sourceStr.substring(sourceStart, sourceEnd);
            JSONArray jsonArray = JSONArray.parseArray(jsonData);
            if (AppUtil.isNull(jsonArray)) {
                LogUtil.error("解析首页数据失败.");
                return "";
            }
            // 尝试通过获取的数据保存视频
            List<FacebookVideo> facebookVideoList = saveVideoBySourceData(jsonArray);
            if (AppUtil.isNull(facebookVideoList)) {
                return "";
            }
            List<FacebookVideo> resultList = new ArrayList<>(facebookVideoList);
            // 尝试通过分页获取全部信息
            int cursorStart = sourceStr.indexOf("end_cursor") + 12;
            String cursorStr = sourceStr.substring(cursorStart, cursorStart + 120);
            int sectionIdStart = sourceStr.indexOf("WWW_PLAYLIST_VIDEO_LIST") - 21;
            String sectionId = sourceStr.substring(sectionIdStart - 48, sectionIdStart);
            Map<String, Object> map = new HashMap<>();
            // 构造接口请求参数，每次请求一百条数据
            String var = "{\"count\":100,\"cursor\":\"cursorStr\",\"scale\":1,\"sectionID\":\"sourceSectionId\",\"useDefaultActor\":false}";
            String var1 = var.replace("cursorStr", cursorStr);
            var1 = var1.replace("sourceSectionId", sectionId);
            map.put("variables", var1);
            map.put("doc_id", "2651465051639629");
            // 主要在此请求中的代理设置（参考方法实现）
            String dataJson = HttpClientUtil.doPostForm("https://www.facebook.com/api/graphql/", map);
            if (AppUtil.isNull(dataJson)) {
                LogUtil.error("通过Facebook-API获取数据失败.mapInfo:[" + JSONObject.toJSONString(map) + "]");
                return JSONObject.toJSONString(resultList);
            }
            while (true) {
                // 防止被反扒机制限制，使用自动随机睡眠一定时间后再请求接口
                Thread.sleep(RandomUtils.nextLong(3, 8) * 1000);
                JSONObject jsonObject = JSONObject.parseObject(dataJson);
                JSONArray dataArray = jsonObject.getJSONObject("data").getJSONObject("node")
                        .getJSONObject("section_components").getJSONArray("edges");
                // 保存视频信息
                List<FacebookVideo> videoList = saveVideoBySourceData(dataArray);
                if (AppUtil.isNull(videoList)) {
                    break;
                } else {
                    resultList.addAll(videoList);
                }
                JSONObject pageInfo = jsonObject.getJSONObject("data").getJSONObject("node")
                        .getJSONObject("section_components").getJSONObject("page_info");
                if (!pageInfo.getBoolean("has_next_page")) {
                    break;
                }
                // 下一次请求需要上一次请求的一个参数
                String var2 = var.replace("cursorStr", pageInfo.getString("end_cursor"));
                var2 = var2.replace("sourceSectionId", sectionId);
                map.put("variables", var2);
                dataJson = HttpClientUtil.doPostForm("https://www.facebook.com/api/graphql/", map);
            }
            return JSONObject.toJSONString(resultList);
        } catch (Exception e) {
            LogUtil.error(e);
        }
        return "";
    }

    /**
     * @desc 根据json 数组解析需要的数据
     * @param jsonArray json数组
     * @return 构造好的数据
     */
    private List<FacebookVideo> saveVideoBySourceData(JSONArray jsonArray) {
        List<FacebookVideo> arrayList = new ArrayList<>();
        try {
            for (Object o : jsonArray) {
                JSONObject data = (JSONObject) o;
                JSONArray attachments = data.getJSONObject("node").getJSONObject("feed_unit").getJSONArray("attachments");
                if (AppUtil.isNull(attachments)) {
                    continue;
                }
                JSONObject attachment = (JSONObject) attachments.get(0);
                FacebookVideo video = new FacebookVideo();
                video.setVideoId(attachment.getJSONObject("media").getString("id"));
                String title = attachment.getJSONObject("media").getString("name");
                String mainBody = attachment.getJSONObject("media").getJSONObject("savable_description").getString("text");
                if (AppUtil.isNull(title) && AppUtil.isNull(mainBody)) {
                    continue;
                }
                String thumbnail = attachment.getJSONObject("media").getJSONObject("image").getString("uri");
                Long publishTime = attachment.getJSONObject("media").getLong("publish_time");
                if (publishTime == null) {
                    continue;
                }
                video.setThumbnail(thumbnail);
                video.setDescription(mainBody);
                video.setReleaseTime(new Date(publishTime * 1000));
                arrayList.add(video);
            }
        } catch (Exception e) {
            LogUtil.error(e);
        }
        return arrayList;
    }

    /**
     * @desc 根据video id获取视频详情
     * @param videoId 视频id（注意 此video组成实际为 频道 id + video id）
     * @return 视频详情
     */
    public String getVideoDetailByVideoId(String videoId) {
        if (AppUtil.isNull(videoId)) {
            return null;
        }
        try {
            // 注意多条记录请求时的反爬
//            Thread.sleep(RandomUtils.nextLong(3, 8) * 1000);
            // 构造WebClient 模拟Chrome 浏览器
            WebClient webClient = new WebClient(BrowserVersion.CHROME);
            // 支持JavaScript
            webClient.getOptions().setJavaScriptEnabled(true);
            webClient.getOptions().setCssEnabled(false);
            webClient.getOptions().setActiveXNative(false);
            webClient.getOptions().setCssEnabled(false);
            webClient.getOptions().setThrowExceptionOnScriptError(false);
            webClient.getOptions().setThrowExceptionOnFailingStatusCode(false);
            webClient.getOptions().setTimeout(15000);
            String host = "127.0.0.1";
            String port = "1087";
            System.setProperty("http.proxyHost", host);
            System.setProperty("http.proxyPort", port);
            System.setProperty("https.proxyHost", host);
            System.setProperty("https.proxyPort", port);
            ProxyConfig proxyConfig = new ProxyConfig();
            proxyConfig.setProxyHost(host);
            proxyConfig.setProxyPort(Integer.parseInt(port));
            webClient.getOptions().setProxyConfig(proxyConfig);
            HtmlPage rootPage = webClient.getPage("https://www.facebook.com/" + videoId + "/");
            webClient.waitForBackgroundJavaScript(5000);
            String html = rootPage.asXml();
            Document document = Jsoup.parse(html);
            if (document == null) {
                return null;
            }
            // 从document中解析缩略图地址
            Elements imageEle = document.getElementsByAttributeValue("name", "twitter:image");
            if (AppUtil.isNull(imageEle)) {
                return null;
            }
            String thumbnail = imageEle.get(0).attr("content");
            // 解析需要的信息(此数据包含作者信息，但缺少简介,因此需要额外获取)
            Elements jsonEle = document.getElementsByAttributeValue("type", "application/ld+json");
            if (AppUtil.isNull(jsonEle)) {
                return null;
            }
            String cdata = jsonEle.get(0).childNodes().get(0).toString();
            JSONObject sourceJson = JSONObject.parseObject(StringUtils.substringBetween(cdata, "//<![CDATA[", "//]]>"));
            // 上传时间
            String uploadDate = sourceJson.getString("uploadDate");
            if (AppUtil.isNull(uploadDate)) {
                if (jsonEle.size() < 2) {
                    return null;
                }
                sourceJson = JSONObject.parseObject(jsonEle.get(1).text());
                uploadDate = sourceJson.getString("uploadDate");
                if (AppUtil.isNull(uploadDate)) {
                    return null;
                }
            }
            String title = StringUtils.substringBefore(sourceJson.getString("name"), "|");
            String description = sourceJson.getString("description");
            String channelUrl = sourceJson.getJSONObject("publisher").getString("url");
            String channelId = channelUrl.split("/")[3];
            FacebookVideo video = FacebookVideo.builder()
                    .videoId(videoId)
                    .channelId(channelId)
                    .description(description)
                    .releaseTime(DateUtil.parseDate(uploadDate, DateUtil.YYYY_MM_DD_T_HH_MM_SS_ZZ))
                    .thumbnail(thumbnail)
                    .build();
            return JSONObject.toJSONString(video);
        } catch (Exception e) {
            LogUtil.error(e);
        }
        return null;
    }

}
