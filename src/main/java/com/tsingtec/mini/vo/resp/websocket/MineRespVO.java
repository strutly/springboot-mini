package com.tsingtec.mini.vo.resp.websocket;

import lombok.Data;

import java.util.Date;

/**
 * @Author lj
 * @Date 2020/6/6 17:08
 * @Version 1.0
 */
@Data
public class MineRespVO {
    private Integer id;//用户id
    private String username;//用户名
    private String avatar;//头像
    private String sign;//签名
    private String status="online"; //在线状态 online：在线、hide：隐身
    private Integer unRead=0;

    private Date historyTime;

    private String content;

    private String type;

    private String name;

    public String getName(){
        return username;
    }
}
