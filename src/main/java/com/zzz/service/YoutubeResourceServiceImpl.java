package com.zzz.service;

import com.alibaba.fastjson.JSONObject;
import com.google.api.services.youtube.YouTube;
import com.google.api.services.youtube.model.*;
import com.zzz.model.YoutubeSimpleSource;
import com.zzz.model.YoutubeVideo;
import com.zzz.util.AppUtil;
import com.zzz.util.Auth;
import com.zzz.util.LogUtil;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * @author ZhangJZ
 * @date 10:04 下午  2020/6/1
 * @desc youtube资源获取实现类
 */
@Service
public class YoutubeResourceServiceImpl {

    /**
     * 分页最大返回数
     */
    private static final long NUMBER_OF_VIDEOS_RETURNED = 50;

    /**
     * 频道最大查询限制（实际无限制）
     */
    private static final long CHANNEL_MAX_LIMIT = 200;

    private static final String API_KEY = "AIzaSyABHlrdcSUwpYILt9VDx4okaSTHzSh9XMg";

    /**
     * @desc 获取指定频道下的视频信息
     * 视频信息格式
     * @param channelId 频道id
     * @return 获取到的视频信息
     */
    public String getChannelVideo(String channelId) {
        setProxy();
        YouTube youtube = new YouTube.Builder(Auth.HTTP_TRANSPORT, Auth.JSON_FACTORY, request -> {
        }).setApplicationName("youtube-cmdline-search-sample").build();
        YouTube.Search.List search = null;
        try {
            search = youtube.search().list("id,snippet");
        } catch (IOException e) {
            LogUtil.error(e);
            return e.getMessage();
        }
        String apiKey = API_KEY;
        search.setKey(apiKey);
        // 接口返回数据模型
        search.setType("video");
        // 设置需要接口返回的字段
        search.setFields("items(id/kind,id/videoId,snippet/title,snippet/thumbnails/default/url),nextPageToken,pageInfo,prevPageToken");
        // 返回的最大记录条数
        search.setMaxResults(50L);
        // 设置要查询的channel id
        search.setChannelId(channelId);
        // 安装视频上传时间倒序获取
        search.setOrder("date");
        SearchListResponse searchResponse;
        try {
            searchResponse = search.execute();
        } catch (IOException e) {
            LogUtil.error(e);
            return e.getMessage();
        }
        List<SearchResult> searchResultList = searchResponse.getItems();
        List<SearchResult> allRecord = new ArrayList<>();
        if (searchResultList != null) {
            PageInfo pageInfo = searchResponse.getPageInfo();
            // 根据分页获取全部数据
            while (true) {
                allRecord.addAll(searchResultList);
                if (pageInfo.getTotalResults() <= NUMBER_OF_VIDEOS_RETURNED) {
                    break;
                }
                // 设置分页的参数
                search.setPageToken(searchResponse.getNextPageToken());
                try {
                    searchResponse = search.execute();
                } catch (IOException e) {
                    LogUtil.error(e);
                    return e.getMessage();
                }
                if (searchResponse == null ||
                        AppUtil.isNull(searchResponse.getItems())) {
                    break;
                }
                List<SearchResult> items = searchResponse.getItems();
                if (AppUtil.isNull(items)) {
                    break;
                }
                allRecord.addAll(items);
                if (items.size() < NUMBER_OF_VIDEOS_RETURNED) {
                    break;
                }
            }
            if (AppUtil.isNull(allRecord)) {
                return "当前频道未查询到视频信息";
            }
            // 获取所有的video信息
            List<YoutubeSimpleSource> objects = new ArrayList<>();
            allRecord.forEach(record -> {
                YoutubeSimpleSource source = new YoutubeSimpleSource();
                source.setVideoId(record.getId().getVideoId());
                source.setTitle(record.getSnippet().getTitle());
                source.setThumbnails(record.getSnippet().getThumbnails().getDefault().getUrl());
                objects.add(source);
            });
            return JSONObject.toJSONString(objects);
        }
        return "当前频道未查询到视频信息";
    }

    /**
     * @desc 根据播放列表id获取分页数据
     * @param playlistId 播放列表id
     * @return 查询的数据
     */
    public String getVideoByPlaylistId(String playlistId) {
        if (AppUtil.isNull(playlistId)) {
            return "";
        }
        setProxy();
        YouTube youtube = new YouTube.Builder(Auth.HTTP_TRANSPORT, Auth.JSON_FACTORY, request -> {
        }).setApplicationName("youtube-cmdline-search-sample").build();
        YouTube.PlaylistItems.List search = null;
        try {
            search = youtube.playlistItems().list("id,snippet");
        } catch (IOException e) {
            LogUtil.error(e);
            return "";
        }
        String apiKey = API_KEY;
        search.setKey(apiKey);
        search.setFields("items(snippet/title,snippet/thumbnails,snippet/resourceId/videoId),nextPageToken,pageInfo,prevPageToken");
        search.setMaxResults(NUMBER_OF_VIDEOS_RETURNED);
        search.setPlaylistId(playlistId);
        PlaylistItemListResponse searchResponse;
        try {
            searchResponse = search.execute();
        } catch (IOException e) {
            LogUtil.error(e);
            return "";
        }
        List<PlaylistItem> searchResultList = searchResponse.getItems();
        List<PlaylistItem> allRecord = new ArrayList<>();
        if (searchResultList != null) {
            PageInfo pageInfo = searchResponse.getPageInfo();
            if (pageInfo.getTotalResults() <= NUMBER_OF_VIDEOS_RETURNED) {
                // 添加第一页数据
                allRecord.addAll(searchResultList);
            } else {
                // 获取多页数据
                allRecord.addAll(searchResultList);
                while (true) {
                    search.setPageToken(searchResponse.getNextPageToken());
                    try {
                        searchResponse = search.execute();
                    } catch (IOException e) {
                        LogUtil.error(e);
                        return e.getMessage();
                    }
                    if (searchResponse == null ||
                            AppUtil.isNull(searchResponse.getItems())) {
                        break;
                    }
                    List<PlaylistItem> items = searchResponse.getItems();
                    if (AppUtil.isNull(items)) {
                        break;
                    }
                    allRecord.addAll(items);
                    if (items.size() < NUMBER_OF_VIDEOS_RETURNED ||
                            allRecord.size() > CHANNEL_MAX_LIMIT) {
                        break;
                    }
                }
            }
        }
        if (AppUtil.isNull(allRecord)) {
            return "未查询到相关信息";
        }
        // 获取所有的video id
        List<YoutubeSimpleSource> objects = new ArrayList<>();
        allRecord.forEach(record -> {
            YoutubeSimpleSource source = new YoutubeSimpleSource();
            source.setVideoId(record.getSnippet().getResourceId().getVideoId());
            source.setTitle(record.getSnippet().getTitle());
            source.setThumbnails(record.getSnippet().getThumbnails().getDefault().getUrl());
            objects.add(source);
        });
        return JSONObject.toJSONString(objects);
    }

    /**
     * @desc 根据video id列表获取视频详细信息
     * @param videoIds 视频列表
     * @return 视频详细信息
     */
    public String getVideoDetailByVideoId(List<String> videoIds) {
        if (AppUtil.isNull(videoIds)) {
            return "";
        }
        setProxy();
        YouTube youtubeVideo = new YouTube.Builder(Auth.HTTP_TRANSPORT, Auth.JSON_FACTORY, request -> {
        }).setApplicationName("youtube-cmdline-search-sample").build();
        YouTube.Videos.List search;
        try {
            search = youtubeVideo.videos().list("id,snippet");
        } catch (IOException e) {
            LogUtil.error(e);
            return "";
        }
        search.setFields("items(id,snippet/publishedAt,snippet/title,snippet/description,snippet/tags,snippet/channelId,snippet/thumbnails)");
        String apiKey = API_KEY;
        search.setKey(apiKey);
        search.setMaxResults(NUMBER_OF_VIDEOS_RETURNED);
        StringBuilder videoIdStr = new StringBuilder();
        if (videoIds.size()  > 1) {
            for (int i = 0; i < videoIds.size(); i++) {
                videoIdStr.append(videoIds.get(i));
                if (i != videoIds.size() - 1) {
                    videoIdStr.append(",");
                }
            }
        } else {
            videoIdStr.append(videoIds.get(0));
        }
        search.setId(videoIdStr.toString());
        VideoListResponse response;
        try {
            response = search.execute();
        } catch (IOException e) {
            LogUtil.error(e);
            return "";
        }
        List<Video> items = response.getItems();
        if (AppUtil.isNull(items)) {
            return null;
        }
        List<YoutubeVideo> resultList = new ArrayList<>();
        for (Video item : items) {
            List<String> tags = item.getSnippet().getTags();
            StringBuilder stringBuilder = new StringBuilder();
            if (AppUtil.isNotNull(tags)) {
                for (String tag : tags) {
                    stringBuilder.append(tag).append(",");
                }
            }
            String description = item.getSnippet().getDescription();
            if (description == null) {
                description = "";
            }
            YoutubeVideo newVideo = YoutubeVideo.builder()
                    .videoId(item.getId())
                    .keywords(stringBuilder.toString())
                    .mainBody(description.substring(0, Math.min(4990, description.length())))
                    .title(item.getSnippet().getTitle())
                    .thumbnail(item.getSnippet().getThumbnails().getDefault().getUrl())
                    .releaseTime(new Date(item.getSnippet().getPublishedAt().getValue()))
                    .crawlerTime(new Date())
                    .build();
            resultList.add(newVideo);
        }
        return JSONObject.toJSONString(resultList);
    }

    /**
     * @desc 设置代理（根据你的VPN本地代理设置）
     */
    private void setProxy() {
        String host = "127.0.0.1";
        String port = "1087";
        System.setProperty("http.proxyHost", host);
        System.setProperty("http.proxyPort", port);
        System.setProperty("https.proxyHost", host);
        System.setProperty("https.proxyPort", port);
    }

}
