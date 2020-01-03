package com.atguigu.gmall.service;

import com.atguigu.gmall.bean.UserAddress;
import com.atguigu.gmall.bean.UserInfo;


import java.util.List;
// 业务层接口
public interface UserService {
    /**
     * 查询所有数据
     * @return
     */
    List<UserInfo> findAll();



    /**
     * 根据用户Id 查询用户地址列表
     * @param userId
     * @return
     */
    List<UserAddress> findUserAddressByUserId(String userId);
}
