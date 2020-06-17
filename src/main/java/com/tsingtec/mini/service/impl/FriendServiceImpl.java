package com.tsingtec.mini.service.impl;

import com.google.common.collect.Lists;
import com.tsingtec.mini.entity.mp.MpUser;
import com.tsingtec.mini.entity.websocket.Friend;
import com.tsingtec.mini.repository.ChatIdRepository;
import com.tsingtec.mini.repository.FriendRepository;
import com.tsingtec.mini.service.FriendService;
import com.tsingtec.mini.utils.BeanMapper;
import com.tsingtec.mini.vo.req.websocket.SignReqVO;
import com.tsingtec.mini.vo.resp.websocket.FriendRespVO;
import com.tsingtec.mini.vo.resp.websocket.MineRespVO;
import com.tsingtec.mini.vo.resp.websocket.ToRespVO;
import com.vip.vjtools.vjkit.mapper.JsonMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @Author lj
 * @Date 2020/3/29 13:54
 * @Version 1.0
 */
@Service
public class FriendServiceImpl implements FriendService {

    private final JsonMapper mapper = JsonMapper.nonEmptyMapper();
    @Autowired
    private FriendRepository friendRepository;

    @Autowired
    private ChatIdRepository chatIdRepository;

    @Override
    public Friend checkByUidAndType(Integer uid, String type) {
        return friendRepository.findByUidAndType(uid,type);
    }

    @Override
    public MineRespVO getByUidAndType(MpUser mpUser, String type) {
        Friend friend = checkByUidAndType(mpUser.getId(),type);
        if(null==friend){
            friend = new Friend();
            friend.setAvatar(mpUser.getHeadImgUrl());
            friend.setType(type);
            friend.setUid(mpUser.getId());
            friend.setUsername(mpUser.getName());
            friendRepository.save(friend);
        }
        return BeanMapper.map(friend,MineRespVO.class);
    }

    @Override
    public List<FriendRespVO> getByUid(Integer uid) {

        List<Integer> fids = chatIdRepository.getFidsByUid("#"+uid+"#","%#"+uid+"#%");
        List<Friend> friends = friendRepository.findAllById(fids);
        /**
         * 只设置一个分组
         */
        FriendRespVO friendRespVO = new FriendRespVO();
        if(friends.size()>0){
            friendRespVO.setList(BeanMapper.mapList(friends,MineRespVO.class));
        }
        return Lists.newArrayList(friendRespVO);
    }

    @Override
    public void update(SignReqVO signReqVO) {
        Friend friend = friendRepository.getOne(signReqVO.getId());
        friend.setSign(signReqVO.getSign());
        friendRepository.save(friend);
    }

    @Override
    public ToRespVO get(Integer id) {
        Friend friend = friendRepository.getOne(id);
        return mapper.fromJson(mapper.toJson(friend),ToRespVO.class);
    }

}
