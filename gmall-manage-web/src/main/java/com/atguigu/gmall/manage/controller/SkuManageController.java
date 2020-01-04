package com.atguigu.gmall.manage.controller;


import com.alibaba.dubbo.config.annotation.Reference;
import com.atguigu.gmall.bean.*;
import com.atguigu.gmall.service.ListService;
import com.atguigu.gmall.service.ManageService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@CrossOrigin
public class SkuManageController {

    @Reference
    private ManageService manageService;

    @Reference
    private  ListService listService;


  //  http://localhost:8082/spuImageList?spuId=58
     //图片加载功能

    @RequestMapping("spuImageList")
    public  List<SpuImage> spuImageList(SpuImage spuImage){

        return   manageService.getSpuImageList(spuImage);
    }

    // http://localhost:8082/spuSaleAttrList?spuId=58

    //销售属性
    @RequestMapping("spuSaleAttrList")
    public List<SpuSaleAttr> getspuSaleAttrList(SpuSaleAttr spuSaleAttr){
        List<SpuSaleAttr> spuSaleAttrList = manageService.getSpuSaleAttrList(spuSaleAttr);
        return spuSaleAttrList;
    }


    @RequestMapping("saveSkuInfo")
    public String saveSkuInfo(@RequestBody SkuInfo skuInfo){
        manageService.saveSkuInfo(skuInfo);

        return "OK";
    }


    /**
     *   根据传入的skuId 将skuInfo 拷贝到skuLsInfo  留下想要的属性 为了前端在首页显示使用
     *   传入       上架到 Kibana上 以便在前台查询能够使用倒排索引！
     * @param skuId
     */
   @RequestMapping(value = "onSale",method = RequestMethod.GET)
   @ResponseBody
    public  void onSale(String skuId){
     //查出 skuinfo
       SkuInfo skuInfo = manageService.getSkuInfo(skuId);

       SkuLsInfo skuLsInfo = new SkuLsInfo();

       // 属性拷贝
       BeanUtils.copyProperties(skuInfo,skuLsInfo);

       //传到 kibana
       listService.saveSkuInfo(skuLsInfo);
   }


}
