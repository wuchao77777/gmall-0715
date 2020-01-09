package com.atguigu.gmall.service;

import com.atguigu.gmall.bean.*;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface ManageService {
    /**
     * 表示查询一级分类数据
     * @return
     */
    public List<BaseCatalog1> getCatalog1();
    /**
     * 通过二级分类对象来查询数据
     * @param baseCatalog2
     * @return
     */
    public List<BaseCatalog2> getCatalog2(BaseCatalog2 baseCatalog2);

    /**
     *  通过三级分类属性来查询
     * @param catalog2Id
     * @return
     */
    public List<BaseCatalog3> getCatalog3(String catalog2Id);

    /**
     * 通过三级分类Id 查询
     * @param catalog3Id
     * @return
     */
    public List<BaseAttrInfo> getAttrList(String catalog3Id);

    /**
     * 保存平台属性，平台属性值
     * @param baseAttrInfo
     */
    void saveAttrInfo(BaseAttrInfo baseAttrInfo);


    /**
     * 根据attrId 回显数据
     * @param attrId
     * @return
     */
    List<BaseAttrValue> getAttrValueList(String attrId);



    /**
     * 通过attrId 查询baseAttrInfo
     * @param attrId
     * @return
     */
    BaseAttrInfo getAttrInfo(String attrId);


    /**
     *  加载所有的商品列表
     * @param spuInfo
     * @return
     */
    List<SpuInfo> getSpuInfoList(SpuInfo spuInfo);


    /**
     *  当创建SPU  商品 的时候  保存 所有！
     * @param spuInfo
     */
    public void saveSpuInfo(SpuInfo spuInfo);

    /**  查询所有的基本属性
     *
     * @return
     */
    List<BaseSaleAttr> getBaseSaleAttrList();

    /**
     *  根据传入的   spuImage 中的 spu _id 查询出该spu的所有图片
     * @param spuImage
     * @return
     */
    List<SpuImage> getSpuImageList(SpuImage spuImage);

    /**
     * 通过后台的 spuid 获取所有的销售属性
     * @param spuSaleAttr
     * @return
     */
    List<SpuSaleAttr> getSpuSaleAttrList(SpuSaleAttr spuSaleAttr);

    /**
     * 保存sku
     * @param skuInfo
     */
    void saveSkuInfo(SkuInfo skuInfo);


    /**
     *
     */
    SkuInfo getSkuInfo(String skuId);

    /**
     * 根据查询
     * @param skuInfo
     * @return
     */
    List<SpuSaleAttr> getSpuSaleAttrListCheckBySku(SkuInfo skuInfo);

    /**
     *  根据  spuid 查询出 改spu 对应的  // {"125|128":"40","124|129":"39","123|127":"38","123|126":"37"}
     *     125  128 是同一个sku 所有的销售属性值的ID   通过俩确定一个sku  相当于字典
     * @param spuId
     * @return
     */
    List<SkuSaleAttrValue> getSkuSaleAttrValueListBySpu(String spuId);


    /**
     * 根据 平台属性值的ID  查询出平台属性的名字 以及 平台属性值的名字
     * @param attrValueIdList
     * @return
     */
    List<BaseAttrInfo> getAttrList( List<String> attrValueIdList);
}
