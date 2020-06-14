package com.zzz.controller;

import com.zzz.service.YoutubeResourceServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;

/**
 * @author ZhangJZ
 * @date 10:07 下午  2020/6/1
 * @desc youtube Controller
 */
@RestController(value = "/youtube")
public class YoutubeResourceController {

    /**
     * 默认使用的channel id
     */
    private static final String DEFAULT_CHANNEL_ID = "UCXuqSBlHAE6Xw-yeJA0Tunw";

    /**
     * 默认使用的playlist id
     */
    private static final String DEFAULT_PLAYLIST_ID = "RDCMUCoC47do520os_4DBMEFGg4A";

    /**
     * 默认使用的video id
     */
    private static final String DEFAULT_VIDEO_ID = "0gNY0KZ2nyY";

    @Autowired
    private YoutubeResourceServiceImpl youtubeResourceService;

    @GetMapping("/getChannelVideo.json")
    public String getChannelVideo(String channelId) {
        if (StringUtils.isEmpty(channelId)) {
            channelId = DEFAULT_CHANNEL_ID;
        }
        return youtubeResourceService.getChannelVideo(channelId);
    }

    @GetMapping("/getVideoByPlaylistId.json")
    public String getVideoByPlaylistId(String playlistId) {
        if (StringUtils.isEmpty(playlistId)) {
            playlistId = DEFAULT_PLAYLIST_ID;
        }
        return youtubeResourceService.getVideoByPlaylistId(playlistId);
    }

    @GetMapping("/getVideoDetailByVideoId.json")
    public String getVideoDetailByVideoId(String videoId) {
        if (StringUtils.isEmpty(videoId)) {
            videoId = DEFAULT_VIDEO_ID;
        }
        List<String> list = new ArrayList<>();
        list.add(videoId);
        return youtubeResourceService.getVideoDetailByVideoId(list);
    }

}
