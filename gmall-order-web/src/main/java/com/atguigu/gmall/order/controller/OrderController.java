package com.atguigu.gmall.order.controller;




import com.alibaba.dubbo.config.annotation.Reference;
import com.atguigu.gmall.bean.UserAddress;
import com.atguigu.gmall.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class OrderController {

   @Reference
    private UserService userService;

    // http://localhost:8081/trade?userId=1
    @RequestMapping("trade")
    public List<UserAddress> trade(String userId){
        return userService.findUserAddressByUserId(userId);
    }



}
