package com.zzz.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * @author ZhangJZ
 * @date 10:56 下午  2020/6/1
 * @desc youtube
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class YoutubeSimpleSource implements Serializable {

    private static final long serialVersionUID = 6517527650841059623L;

    private String videoId;

    private String title;

    private String thumbnails;
}
