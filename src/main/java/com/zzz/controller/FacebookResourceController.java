package com.zzz.controller;

import com.zzz.service.FacebookResourceServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;

/**
 * @author ZhangJZ
 * @date 10:30 下午  2020/6/5
 * @desc facebook controller
 */
@RestController
public class FacebookResourceController {

    /**
     * 默认使用的channel id
     */
    private static final String DEFAULT_CHANNEL_ID = "cnliziqi";

    /**
     * 默认使用的video id
     */
    private static final String DEFAULT_VIDEO_ID = "cnliziqi/videos/3080176115398147";

    @Autowired
    private FacebookResourceServiceImpl facebookResourceService;

    @GetMapping("/facebook/getChannelVideo.json")
    public String getChannelVideo(String channelId) {
        if (StringUtils.isEmpty(channelId)) {
            channelId = DEFAULT_CHANNEL_ID;
        }
        return facebookResourceService.getVideoByChannelId(channelId);
    }

    @GetMapping("/facebook/getVideoDetailByVideoId.json")
    public String getVideoDetailByVideoId(String videoId) {
        if (StringUtils.isEmpty(videoId)) {
            videoId = DEFAULT_VIDEO_ID;
        }
        return facebookResourceService.getVideoDetailByVideoId(videoId);
    }

}
