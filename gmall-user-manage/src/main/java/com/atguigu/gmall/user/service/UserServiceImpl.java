package com.atguigu.gmall.user.service;

import com.alibaba.dubbo.config.annotation.Service;
import com.alibaba.fastjson.JSON;
import com.atguigu.gmall.bean.UserAddress;
import com.atguigu.gmall.bean.UserInfo;
import com.atguigu.gmall.config.RedisUtil;
import com.atguigu.gmall.service.UserService;
import com.atguigu.gmall.user.mapper.UserAddressMapper;
import com.atguigu.gmall.user.mapper.UserInfoMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.DigestUtils;
import redis.clients.jedis.Jedis;


import java.util.List;

@Service
public class UserServiceImpl implements UserService{



    @Autowired
    private UserInfoMapper userInfoMapper;
    @Autowired
    private com.atguigu.gmall.user.mapper.UserAddressMapper UserAddressMapper;

    @Autowired
    private RedisUtil redisUtil;

    public String userKey_prefix="user:";
    public String userinfoKey_suffix=":info";
    public int userKey_timeOut=60*60*24;


    @Override
    public List<UserInfo> findAll() {

        return  userInfoMapper.selectAll();
    }

    @Override
    public List<UserAddress> findUserAddressByUserId(String userId) {
        UserAddress userAddress = new UserAddress();
        userAddress.setId(userId);
        return  UserAddressMapper.select(userAddress);
    }

    @Override
    public UserInfo login(UserInfo userInfo) {
        // select * from userInfo where loginName = ? and passwd = ?
        // 要对密码进行加密
        String passwd = userInfo.getPasswd();
        String newPasswd  = DigestUtils.md5DigestAsHex(passwd.getBytes());
        userInfo.setPasswd(newPasswd);
        UserInfo info = userInfoMapper.selectOne(userInfo);

        if(info !=null){
            // 获得到redis ,将用户存储到redis中
            Jedis jedis = redisUtil.getJedis();
            jedis.setex(userKey_prefix+info.getId()+userinfoKey_suffix,userKey_timeOut, JSON.toJSONString(info));
            jedis.close();
            return info;
        }
          return  null;
    }

    @Override
    public UserInfo verfiy(String userId) {
        Jedis jedis = redisUtil.getJedis();
        // 获取缓存中的数据
        String userinfo = jedis.get(userKey_prefix + userId + userinfoKey_suffix);
        if(userinfo !=null){
            // userJson 转换成对象
            UserInfo userInfo = JSON.parseObject(userinfo, UserInfo.class);
            return userInfo;
        }
        return null;
    }




}
