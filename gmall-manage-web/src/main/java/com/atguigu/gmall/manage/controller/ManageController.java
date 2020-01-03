package com.atguigu.gmall.manage.controller;


import com.alibaba.dubbo.config.annotation.Reference;
import com.atguigu.gmall.bean.*;
import com.atguigu.gmall.service.ManageService;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;

@CrossOrigin
@Controller
public class ManageController {

    @Reference
    private ManageService manageService;

    @RequestMapping("getCatalog1")
    @ResponseBody
    public List<BaseCatalog1> getCatalog1(){
        return manageService.getCatalog1();
    }

    @RequestMapping("getCatalog2")
    @ResponseBody
    public List<BaseCatalog2> getCatalog2(BaseCatalog2 baseCatalog2){
        return manageService.getCatalog2(baseCatalog2);
    }

    @RequestMapping("getCatalog3")
    @ResponseBody
    public List<BaseCatalog3> getCatalog3(String catalog2Id){
        return manageService.getCatalog3(catalog2Id);
    }


    //在保存sku的时候也会执行此方法
    //http://localhost:8082/attrInfoList?catalog3Id=61
    @RequestMapping("attrInfoList")
    @ResponseBody
    public List<BaseAttrInfo> attrInfoList(String catalog3Id){
        return manageService.getAttrList(catalog3Id);
    }

    @RequestMapping("saveAttrInfo")
    @ResponseBody
    public void  saveAttrInfo(@RequestBody BaseAttrInfo baseAttrInfo){
        manageService.saveAttrInfo(baseAttrInfo);
    }

//    http://localhost:8082/getAttrValueList?attrId=23
//     @RequestMapping("getAttrValueList")
//    public  List<BaseAttrValue> getAttrValueList(String attrId){
//        //select * from baseAttrValue where attrId =?
//        return   manageService.getAttrValueList(attrId);
//
//     }

     @RequestMapping("getAttrValueList")
     @ResponseBody
     public  List<BaseAttrValue> getAttrValueList(String attrId){
       //先通过attrId查询平台属性
      // 返回平台属性的平台属性值集合
       //先查询 是否有这个属性  根据attrId查询一下是否有这个属性
        BaseAttrInfo baseAttrInfo =  manageService.getAttrInfo(attrId);
           if(baseAttrInfo == null) {
               return  null;
           }
         return   manageService.getAttrValueList(attrId);
     }





}
