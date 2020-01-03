package com.atguigu.gmall.item.Controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.alibaba.fastjson.JSON;
import com.atguigu.gmall.bean.SkuInfo;
import com.atguigu.gmall.bean.SkuSaleAttrValue;
import com.atguigu.gmall.bean.SpuSaleAttr;
import com.atguigu.gmall.service.ManageService;
import org.apache.log4j.net.SocketNode;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;

@Controller
public class ItemController {

        @Reference
        private ManageService manageService;

        @RequestMapping("{skuId}.html")
        public String skuInfoPage(@PathVariable("skuId") String  skuId, Model model){
            // 存储基本的skuInfo信息
            SkuInfo skuInfo = manageService.getSkuInfo(skuId);
            model.addAttribute("skuInfo",skuInfo);

            // 存储 spu，sku数据   查询 出来所有的 销售属性以及 被选择的 销售属性
            List<SpuSaleAttr> spuSaleAttrList =manageService.getSpuSaleAttrListCheckBySku(skuInfo);
            model.addAttribute("spuSaleAttrList",spuSaleAttrList);

            // 查询 销售属性值与skuId 组合的数据集合
            List<SkuSaleAttrValue> skuSaleAttrValueListBySpu =  manageService.getSkuSaleAttrValueListBySpu(skuInfo.getSpuId());

            HashMap<String, String> map = new HashMap<>();
            String key="";
            if (skuSaleAttrValueListBySpu!=null && skuSaleAttrValueListBySpu.size()>0) {
                for (int i = 0; i < skuSaleAttrValueListBySpu.size(); i++) {
                    SkuSaleAttrValue skuSaleAttrValue = skuSaleAttrValueListBySpu.get(i);
                    // {"125|128":"40","124|129":"39","123|127":"38","123|126":"37"}
                    //  key =125|128  value =40
                    //  map.put(key,value);  map---->json
                    // 对应的拼接规则：1.   如果skuId 与下一个skuId 不一致的时候，则停止拼接。
                    // 2.  当循环到集合末尾的时候，停止拼接 map.put(key,value) 清空key
                    // 第一次循环 key = 123  第二次 key = 123|126  第三次循环 map.put(key,value) 清空key

                    if (key.length() > 0) {
                        key += "|";
                    }
                    key += skuSaleAttrValue.getSaleAttrValueId();
                    if ((i + 1) == skuSaleAttrValueListBySpu.size()
                            || !skuSaleAttrValue.getSkuId().equals(skuSaleAttrValueListBySpu.get(i + 1).getSkuId())) {
                        map.put(key, skuSaleAttrValue.getSkuId());
                        key = "";
                    }
                }
            }
            String valuesSkuJson = JSON.toJSONString(map);
            model.addAttribute("valuesSkuJson",valuesSkuJson);
            return "item";
        }

}
