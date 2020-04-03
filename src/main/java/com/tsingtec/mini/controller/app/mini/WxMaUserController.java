package com.tsingtec.mini.controller.app.mini;

import cn.binarywang.wx.miniapp.api.WxMaService;
import cn.binarywang.wx.miniapp.bean.WxMaJscode2SessionResult;
import cn.binarywang.wx.miniapp.bean.WxMaPhoneNumberInfo;
import cn.binarywang.wx.miniapp.bean.WxMaUserInfo;
import com.tsingtec.mini.config.wx.WxMaConfiguration;
import com.tsingtec.mini.entity.app.WxMaUser;
import com.tsingtec.mini.service.WxMaUserService;
import com.tsingtec.mini.utils.DataResult;
import com.tsingtec.mini.vo.req.app.mini.WxLoginReqVO;
import com.tsingtec.mini.vo.resp.app.mini.InformationRespVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import me.chanjar.weixin.common.error.WxErrorException;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * 微信小程序用户接口
 *
 * @author <a href="https://github.com/binarywang">Binary Wang</a>
 */
@Slf4j
@RestController
@Api(tags = "小程序接口")
@RequestMapping("/wx")
public class WxMaUserController {

    @Autowired
    private WxMaUserService wxMaUserService;

    /**
     * 授权 获取身份信息
     * @param wxLoginVo
     * @return
     */
    @PostMapping("/auth")
    @ApiOperation(value = "用户授权接口")
    public DataResult<InformationRespVO> sign(@RequestBody WxLoginReqVO wxLoginVo){
        final WxMaService wxService = WxMaConfiguration.getMaService();
        DataResult result = DataResult.success();
        InformationRespVO informationRespVO = new InformationRespVO();
        result.setData(informationRespVO);
        log.info("登录信息为:{}",wxLoginVo);
        String code = wxLoginVo.getCode();
        if(StringUtils.isBlank(code)){
            result.setCode(-1);
            result.setMsg("授权信息不全,请重新进行授权");
            return result;
        }
        try {
            WxMaJscode2SessionResult session = wxService.getUserService().getSessionInfo(code);
            if (!wxService.getUserService().checkUserInfo(session.getSessionKey(), wxLoginVo.getRawData(), wxLoginVo.getSignature())) {
                result.setCode(-1);
                result.setMsg("user check failed");
                return result;
            }
            // 解密用户信息
            WxMaUserInfo userInfo = wxService.getUserService().getUserInfo(session.getSessionKey(), wxLoginVo.getEncryptedData(), wxLoginVo.getIv());
            log.info(userInfo.toString());
            WxMaUser wxMaUser = wxMaUserService.findByOpenId(userInfo.getOpenId());
            if(null == wxMaUser){
                wxMaUser = new WxMaUser();
            }
            BeanUtils.copyProperties(userInfo,wxMaUser);
            wxMaUserService.insert(wxMaUser);

        }catch (WxErrorException e) {
            result.setCode(-1);
            result.setMsg("授权失败,请稍后再试!");
            return result;
        }
        return result;
    }

    /**
     * 登录接口
     */
    @GetMapping("/login")
    @ApiOperation(value = "用户登录接口")
    public DataResult<InformationRespVO> login(String code) {
        InformationRespVO informationRespVO = new InformationRespVO();
        DataResult result = DataResult.success();
        result.setData(informationRespVO);

        if (StringUtils.isBlank(code)) {
            result.setCode(-1);
            result.setMsg("授权信息不全,请重新进行授权");
            return result;
        }
        final WxMaService wxService = WxMaConfiguration.getMaService();
        try {
            WxMaJscode2SessionResult session = wxService.getUserService().getSessionInfo(code);
            log.info(session.getSessionKey());
            log.info(session.getOpenid());
            log.info(session.getUnionid());
            //TODO 可以增加自己的逻辑，关联业务相关数据
            WxMaUser wxMaUser = wxMaUserService.findByOpenId(session.getOpenid());
            if(null != wxMaUser){
                wxMaUser.setUnionId(session.getUnionid());
                wxMaUserService.update(wxMaUser);
                result.setCode(0);
                result.setMsg("登录成功");
            }else{
                result.setCode(-1);
                result.setMsg("登录成功");
            }
        } catch (WxErrorException e) {
            log.error(e.getMessage(), e);
            result.setCode(-1);
            result.setMsg("登录失败");
        }
        return result;
    }




    /**
     * <pre>
     * 获取用户信息接口
     * </pre>
     */
    @GetMapping("/info")
    public DataResult info(String sessionKey,
                       String signature, String rawData, String encryptedData, String iv) {
        DataResult result = DataResult.success();
        final WxMaService wxService = WxMaConfiguration.getMaService();

        // 用户信息校验
        if (!wxService.getUserService().checkUserInfo(sessionKey, rawData, signature)) {
            result.setCode(-1);
            result.setMsg("登录失败");
        }
        // 解密用户信息
        WxMaUserInfo userInfo = wxService.getUserService().getUserInfo(sessionKey, encryptedData, iv);

        return result;
    }

    /**
     * <pre>
     * 获取用户绑定手机号信息
     * </pre>
     */
    @GetMapping("/phone")
    public DataResult phone(String sessionKey, String signature,
                        String rawData, String encryptedData, String iv) {
        DataResult result = DataResult.success();
        final WxMaService wxService = WxMaConfiguration.getMaService();
        // 用户信息校验
        if (!wxService.getUserService().checkUserInfo(sessionKey, rawData, signature)) {
            return result;
        }

        // 解密
        WxMaPhoneNumberInfo phoneNoInfo = wxService.getUserService().getPhoneNoInfo(sessionKey, encryptedData, iv);

        return result;
    }

}