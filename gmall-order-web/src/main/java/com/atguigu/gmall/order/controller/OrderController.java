package com.atguigu.gmall.order.controller;




import com.alibaba.dubbo.config.annotation.Reference;
import com.alibaba.fastjson.JSON;
import com.atguigu.gmall.bean.*;
import com.atguigu.gmall.config.LoginRequire;
import com.atguigu.gmall.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.util.*;

@Controller
public class OrderController {

   @Reference
    private UserService userService;

   @Reference
   private OrderService orderService;

   @Reference
   private CartService cartService;


   @Reference
   private  ManageService manageService;

   @Reference
   private PaymentService paymentService;

     //结算
    // http://localhost:8081/trade?userId=1
    @RequestMapping("trade")
    @LoginRequire
    public String trade(HttpServletRequest request) {
        // 获取userId
        String userId = (String) request.getAttribute("userId");
        List<UserAddress> userAddressesList = userService.findUserAddressByUserId(userId);

        // 必须获取购物车选中的数据
        List<CartInfo> cartInfoList =cartService.getCartCheckedList(userId);
       //存放订单详情  将订单中
        ArrayList<OrderDetail> detailArrayList = new ArrayList<>();
        for (CartInfo cartInfo : cartInfoList) {
            OrderDetail orderDetail = new OrderDetail();

            orderDetail.setOrderId(cartInfo.getId());
            orderDetail.setSkuName(cartInfo.getSkuName());
            orderDetail.setImgUrl(cartInfo.getImgUrl());
            orderDetail.setSkuNum(cartInfo.getSkuNum());
            orderDetail.setOrderPrice(cartInfo.getCartPrice());

            detailArrayList.add(orderDetail);
        }
        OrderInfo orderInfo = new OrderInfo();
        orderInfo.setOrderDetailList(detailArrayList);
        orderInfo.sumTotalAmount();

        // 保存数据
        request.setAttribute("detailArrayList",detailArrayList);
        request.setAttribute("userAddressesList",userAddressesList);
        request.setAttribute("totalAmount",orderInfo.getTotalAmount());

        //为了防止表单的重复提交
        String tradeNo = orderService.getTradeNo(userId);
        request.setAttribute("tradeNo",tradeNo);
        return  "trade";
    }
    // http://trade.gmall.com/submitOrder
    // @RequestBody 将json --- > JavaObject
        @RequestMapping("submitOrder")
        @LoginRequire
       public  String submitOrder(OrderInfo orderInfo, HttpServletRequest request){
                /*
        1.  将数据添加到数据库表中! cartInfo ,orderDetail
        2.  确定后台如何接收前台传递过来的数据！
         */
            // 调用服务层！保存
            // 订单的总金额，订单的状态，用户Id，第三方交易编号，创建时间，过期时间，进程状态也没有！
            String userId = (String) request.getAttribute("userId");
            orderInfo.setUserId(userId);
             // 生成订单编号
            String outTradeNo="ATGUIGU"+System.currentTimeMillis()+""+new Random().nextInt(1000);
            orderInfo.setOutTradeNo(outTradeNo);
            // 防止表单重复提交
            // 获取页面提交的流水号
            String tradeNo = request.getParameter("tradeNo");
            // 调用比较方法
            boolean result=orderService.checkTradeNo(tradeNo,userId);
              if(!result){
                  request.setAttribute("errMsg","请勿重复提交订单！");
                  return "tradeFail";
              }
            // 删除缓存的流水号
            orderService.delTradeNo(userId);

              //验证库存
            List<OrderDetail> orderDetailList = orderInfo.getOrderDetailList();
            for (OrderDetail orderDetail : orderDetailList) {
                //验证库存  返回是true 代表有库存 false 则没有
           boolean flag = orderService.checkStock(orderDetail.getSkuNum(),orderDetail.getSkuId());
            if(!flag){
                request.setAttribute("errMsg",orderDetail.getSkuName()+"库存不足！");
                return "tradeFail";
            }
                // 验证价格：orderDetail.getOrderPrice()== skuInfo.price
                SkuInfo skuInfo = manageService.getSkuInfo(orderDetail.getSkuId());
                int res =orderDetail.getOrderPrice().compareTo(skuInfo.getPrice());
                if(0 !=res){
                    request.setAttribute("errMsg",orderDetail.getSkuName()+"商品价格有变动，请重新下单！");
                    // 加载最新价格到缓存！
                    cartService.loadCartCache(userId);
                    return "tradeFail";
                }
            }

            //返回订单的id
            String orderId = orderService.saveOrderInfo(orderInfo);

            // 准备发送延迟队列  订单15秒之后就会自动把订单状态改为 关闭
            paymentService.closeOrderInfo(outTradeNo,15);
            // 重定向到支付模块！
            return "redirect://payment.gmall.com/index?orderId="+orderId;
        }


    // http://trade.gmall.com/orderSplit?orderId=xxx&wareSkuMap=xxx
    @RequestMapping("orderSplit")
    @ResponseBody
    public String orderSplit(HttpServletRequest request){
        String orderId = request.getParameter("orderId");
        // [{"wareId":"1","skuIds":["2","10"]},{"wareId":"2","skuIds":["3"]}]
        String wareSkuMap = request.getParameter("wareSkuMap");
        // 调用服务层的拆单方法
        List<OrderInfo> subOrderInfoList = orderService.orderSplit(orderId,wareSkuMap);

        // 声明一个存储map 的集合
        ArrayList<Map> mapArrayList = new ArrayList<>();
        for (OrderInfo orderInfo : subOrderInfoList) {
            Map map = orderService.initWareOrder(orderInfo);
            mapArrayList.add(map);
        }
       return JSON.toJSONString(mapArrayList);
    }


}
