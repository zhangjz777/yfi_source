package com.zzz.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Date;

/**
 * @author ZhangJZ
 * @date 9:51 下午  2020/6/5
 * @desc
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class FacebookVideo implements Serializable {

    private static final long serialVersionUID = -7339014683535104342L;

    private String videoId;

    /**
     * 简介
     */
    private String description;

    /**
     * 标题
     */
    private String title;

    /**
     * 缩略图
     */
    private String thumbnail;

    /**
     * 视频上传时间
     */
    private Date releaseTime;

    /**
     * channel id
     */
    private String channelId;

}
