package com.zzz.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Date;

/**
 * @author ZhangJZ
 * @date 10:41 下午  2020/6/3
 * @desc
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class YoutubeVideo implements Serializable {

    private static final long serialVersionUID = 2786663273000366753L;

    private String videoId;

    private String keywords;

    private String mainBody;

    private String title;

    private String thumbnail;

    private Date releaseTime;

    private Date crawlerTime;

}
