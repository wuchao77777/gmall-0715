package com.atguigu.gmall.manage.Service;

import com.alibaba.dubbo.config.annotation.Service;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.atguigu.gmall.bean.*;
import com.atguigu.gmall.config.RedisUtil;
import com.atguigu.gmall.manage.manage.constant.ManageConst;
import com.atguigu.gmall.manage.mapper.*;
import com.atguigu.gmall.service.ManageService;
import org.redisson.Redisson;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import redis.clients.jedis.Jedis;

import javax.annotation.Resource;
import javax.persistence.Column;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.RecursiveTask;
import java.util.concurrent.TimeUnit;

@Service
public class ManageServiceImpl implements ManageService{

    @Autowired
    BaseAttrInfoMapper baseAttrInfoMapper;

    @Autowired
    BaseAttrValueMapper baseAttrValueMapper;

    @Autowired
    BaseCatalog1Mapper baseCatalog1Mapper;

    @Autowired
    BaseCatalog2Mapper baseCatalog2Mapper;

    @Autowired
    BaseCatalog3Mapper baseCatalog3Mapper;

    @Autowired
    SpuInfoMapper  spuInfoMapper;

    @Resource // 默认按照name，如果没有name 找type
    private BaseSaleAttrMapper baseSaleAttrMapper;


    @Autowired
    private SpuImageMapper spuImageMapper;

    @Autowired
    private SpuSaleAttrMapper spuSaleAttrMapper;

    @Autowired
    private SpuSaleAttrValueMapper spuSaleAttrValueMapper;

  @Autowired
   private  SkuImageMapper skuImageMapper;

    @Autowired
    private  SkuInfoMapper skuInfoMapper;

    @Autowired
    private  SkuAttrValueMapper skuAttrValueMapper;

    @Autowired
    private  SkuSaleAttrValueMapper skuSaleAttrValueMapper;

  @Autowired
    private   RedisUtil redisUtil;



    @Override
    public List<BaseCatalog1> getCatalog1() {
        // select * from basecatalog1 ;
        List<BaseCatalog1> baseCatalog1List = baseCatalog1Mapper.selectAll();
        return baseCatalog1List;
    }

    @Override
    public List<BaseCatalog2> getCatalog2(BaseCatalog2 baseCatalog2) {
        // select * from basecatalog2 where catalog1Id = ?
     return baseCatalog2Mapper.select(baseCatalog2);
    }

    @Override
    public List<BaseCatalog3> getCatalog3(String catalog2Id) {
        BaseCatalog3 baseCatalog3=new BaseCatalog3();
        baseCatalog3.setCatalog2Id(catalog2Id);
        List<BaseCatalog3> baseCatalog3List = baseCatalog3Mapper.select(baseCatalog3);
        return baseCatalog3List;
    }

    @Override
    public List<BaseAttrInfo> getAttrList(String catalog3Id) {
//        BaseAttrInfo baseAttrInfo = new BaseAttrInfo();
//        baseAttrInfo.setCatalog3Id(catalog3Id);
//        List<BaseAttrInfo> baseAttrInfoList = baseAttrInfoMapper.select(baseAttrInfo);
        List<BaseAttrInfo> baseAttrInfos = baseAttrInfoMapper.selectBaseAttrInfoListByCatalog3Id(Long.parseLong(catalog3Id));
        return baseAttrInfos;
    }

    @Transactional
    @Override
    public void saveAttrInfo(BaseAttrInfo baseAttrInfo) {

        //判断是否是修改
        // 保存| 修改
        if(baseAttrInfo.getId() !=null && baseAttrInfo.getId().length()>0){
            // 修改：
            baseAttrInfoMapper.updateByPrimaryKeySelective(baseAttrInfo);
        }else {
            // 直接保存平台属性
            baseAttrInfoMapper.insertSelective(baseAttrInfo);
        }

        // baseAttrValue 修改：
        // 先将原有的数据删除，然后再新增！
        //清空值条件为attrid = BaseAttrValue.id
        BaseAttrValue baseAttrValue1 = new BaseAttrValue();

        baseAttrValue1.setAttrId(baseAttrInfo.getId());
        // delete from baseAttrValue where attrId = baseAttrInfo.getId();
        baseAttrValueMapper.delete(baseAttrValue1);

        // 保存平台属性值
        List<BaseAttrValue> attrValueList = baseAttrInfo.getAttrValueList();

        // 判断集合不为空！
        // 先判断对象不为空！然后再判断集合长度！
        if (attrValueList!=null && attrValueList.size()>0) {
            for (BaseAttrValue baseAttrValue : attrValueList) {
                // 平台属性值Id主键自增，平台属性Id baseAttrValue.attrId = baseAttrInfo.id
                //   private String valueName;
                //    private String attrId;
                baseAttrValue.setAttrId(baseAttrInfo.getId());
                baseAttrValueMapper.insertSelective(baseAttrValue);
            }
        }
    }

    @Override
    public List<BaseAttrValue> getAttrValueList(String attrId) {
        // select * from baseAttrValue where attrId = ?
        BaseAttrValue baseAttrValue = new BaseAttrValue();
        baseAttrValue.setAttrId(attrId);
        List<BaseAttrValue> select = baseAttrValueMapper.select(baseAttrValue);
        return  select;
    }

    @Override
    public BaseAttrInfo   getAttrInfo(String attrId) {
        //为attrid = BaseAttrValue.id//  select * from baseAttrInfo where id = attrId
        BaseAttrInfo baseAttrInfo = baseAttrInfoMapper.selectByPrimaryKey(attrId);
        // 查询平台属性值集合
       baseAttrInfo.setAttrValueList(getAttrValueList(attrId));
        return baseAttrInfo;
    }

    @Override
    public List<SpuInfo> getSpuInfoList(SpuInfo spuInfo) {
      return   spuInfoMapper.select(spuInfo);
    }


    @Override
    public List<BaseSaleAttr> getBaseSaleAttrList() {

        return baseSaleAttrMapper.selectAll();
    }



    @Transactional
    @Override
    public void saveSpuInfo(SpuInfo spuInfo) {
        /*
            spuInfo
            spuImage
            spuSaleAttr
            spuSaleAttrValue
         */
        spuInfoMapper.insert(spuInfo);
        // spuImage
        List<SpuImage> spuImageList = spuInfo.getSpuImageList();
        if(spuImageList !=null && spuImageList.size()>0){
            for (SpuImage spuImage : spuImageList) {
                spuImage.setSpuId(spuInfo.getId());
                spuImageMapper.insertSelective(spuImage);
            }
        }
      // 保存 销售属性
        List<SpuSaleAttr> spuSaleAttrList = spuInfo.getSpuSaleAttrList();
        if(spuSaleAttrList !=null && spuSaleAttrList.size()>0) {
            for (SpuSaleAttr spuSaleAttr : spuSaleAttrList) {
                spuSaleAttr.setSpuId(spuInfo.getId());
                spuSaleAttrMapper.insertSelective(spuSaleAttr);

                // 销售属性值
                List<SpuSaleAttrValue> spuSaleAttrValueList = spuSaleAttr.getSpuSaleAttrValueList();
                if (spuSaleAttrValueList != null && spuSaleAttrValueList.size() > 0) {
                    for (SpuSaleAttrValue spuSaleAttrValue : spuSaleAttrValueList) {
                        spuSaleAttrValue.setSpuId(spuInfo.getId());
                        spuSaleAttrValueMapper.insertSelective(spuSaleAttrValue);
                    }
                }
            }
        }
    }


    @Override
    public List<SpuImage> getSpuImageList(SpuImage spuImage) {
        return spuImageMapper.select(spuImage);
    }


    @Transactional
    @Override
    public List<SpuSaleAttr> getSpuSaleAttrList(SpuSaleAttr spuSaleAttr) {
        String spuId = spuSaleAttr.getSpuId();
        return  spuSaleAttrMapper.selectSpuSaleAttrList(Long.parseLong(spuId));
    }

    @Override
    public void saveSkuInfo(SkuInfo skuInfo) {
//
//        skuInfo:
//        skuImage:
//        skuSaleAttrValue:
//        skuAttrValue:
        /**
         * 判断是修改还是新增sku
         */
        if (skuInfo.getId()==null || skuInfo.getId().length()==0){
            skuInfoMapper.insert(skuInfo);
        }else {
            skuInfoMapper.updateByPrimaryKeySelective(skuInfo);
        }
        //        sku_img,
        SkuImage skuImage = new SkuImage();
        skuImage.setSkuId(skuInfo.getId());
        skuImageMapper.delete(skuImage);

        List<SkuImage> skuImageList = skuInfo.getSkuImageList();
        if(skuImageList != null && skuImageList.size()>0){
            for (SkuImage image : skuImageList) {
                   /* "" 区别 null*/
                     if(image.getId() != null && image.getId().length()==0){
                         image.setId(null);
                     }
                image.setSkuId(skuInfo.getId());
                skuImageMapper.insertSelective(image);
            }
        }

   //     sku_attr_value,  先删除在增加
        SkuAttrValue skuAttrValue = new SkuAttrValue();
        skuAttrValue.setSkuId(skuInfo.getId());
        skuAttrValueMapper.delete(skuAttrValue);



        // 插入数据
        List<SkuAttrValue> skuAttrValueList = skuInfo.getSkuAttrValueList();
        if (skuAttrValueList!=null && skuAttrValueList.size()>0){
            for (SkuAttrValue attrValue : skuAttrValueList) {
                if (attrValue.getId()!=null && attrValue.getId().length()==0){
                    attrValue.setId(null);
                }
                // skuId
                attrValue.setSkuId(skuInfo.getId());
                skuAttrValueMapper.insertSelective(attrValue);
            }
        }
//        sku_sale_attr_value,
        SkuSaleAttrValue skuSaleAttrValue = new SkuSaleAttrValue();
        skuSaleAttrValue.setSkuId(skuInfo.getId());
        skuSaleAttrValueMapper.delete(skuSaleAttrValue);
//      插入数据
        List<SkuSaleAttrValue> skuSaleAttrValueList = skuInfo.getSkuSaleAttrValueList();
        if (skuSaleAttrValueList!=null && skuSaleAttrValueList.size()>0){
            for (SkuSaleAttrValue saleAttrValue : skuSaleAttrValueList) {
                if (saleAttrValue.getId()!=null && saleAttrValue.getId().length()==0){
                    saleAttrValue.setId(null);
                }
                // skuId
                saleAttrValue.setSkuId(skuInfo.getId());
                skuSaleAttrValueMapper.insertSelective(saleAttrValue);
            }
        }
    }


    /**
     *  根据sku 查 skuinfo 信息
     * @param skuId
     * @return
     */
    @Override
    public SkuInfo getSkuInfo(String skuId) {
        // redisson 解决分布式锁、
        return getSkuInfoRedisson(skuId);
    // 使用redis--set 命令做分布式锁！
        //return getSkuInfoRedist(skuId);
    }

    private SkuInfo getSkuInfoRedisson(String skuId) {
        SkuInfo skuInfo =null;
        RLock lock =null;
        Jedis jedis =null;

        try {
            jedis = redisUtil.getJedis();
            String userKey = ManageConst.SKUKEY_PREFIX+skuId+ManageConst.SKUKEY_SUFFIX;

            if(jedis.exists(userKey)){
                // 获取缓存中的数据
                String userJson  = jedis.get(userKey);
                if(!StringUtils.isEmpty(userJson)){
                    skuInfo= JSON.parseObject(userJson, SkuInfo.class);
                  return  skuInfo;
                }
            }else {
                //从数据库中获取数据   加redisson 锁
                Config config = new Config();
                config.useSingleServer().setAddress("redis://192.168.206.226:6379");
                RedissonClient redissonClient = Redisson.create(config);
                lock = redissonClient.getLock("mylock");

                lock.lock(10, TimeUnit.SECONDS);
                // 从数据库查询数据
                skuInfo = getSkuInfoDB(skuId);
                String  skuInfostr = JSON.toJSONString(skuInfo);
                //放入jedis中
                jedis.setex(userKey,ManageConst.SKUKEY_TIMEOUT,skuInfostr);

                return skuInfo;
            }

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (jedis!=null){
                jedis.close();
            }
            if (lock!=null){
                lock.unlock();
            }
        }
        return getSkuInfoDB(skuId);
    }

    private SkuInfo getSkuInfoRedist(String skuId) {
        SkuInfo skuInfo = null;
        Jedis jedis =null;
        try {
             jedis = redisUtil.getJedis();
            String skuInfoKey = ManageConst.SKUKEY_PREFIX+skuId+ManageConst.SKUKEY_SUFFIX;

            String skuJson = jedis.get(skuInfoKey);
            if (skuJson==null || skuJson.length()==0){
                // 没有数据 ,需要加锁！取出完数据，还要放入缓存中，下次直接从缓存中取得即可！
                String skuLockKey=ManageConst.SKUKEY_PREFIX+skuId+ManageConst.SKULOCK_SUFFIX;
                // 生成锁
//                String lockKey   = jedis.setex(skuLockKey, ManageConst.SKULOCK_EXPIRE_PX, "OK");
                String token = UUID.randomUUID().toString().replaceAll("-", "");
               //skuLockKey ，token         token  是vules
                String lockKey =  jedis.set(skuLockKey,token,"NX","PX",ManageConst.SKULOCK_EXPIRE_PX);
                     if("OK".equals(lockKey)){
                         System.out.println("获取锁！");
                         skuInfo = getSkuInfoDB(skuId);
                         // 将是数据放入缓存
                         // 将对象转换成字符串
                         String skuRedisStr = JSON.toJSONString(skuInfo);
                         jedis.setex(skuInfoKey,ManageConst.SKUKEY_TIMEOUT,skuRedisStr);
                         /**
                          *  如果出现 在设置锁后 的业务太复杂导致 锁 失效， 业务完成时 把其他本来不是自己的锁给解了
                          *  就使用lua 脚本解决  赋值   匹配 锁的 key 和value 如果一致 进行解锁
                          */
                         String script ="if redis.call('get', KEYS[1]) == ARGV[1] then return redis.call('del', KEYS[1]) else return 0 end";
                         jedis.eval(script, Collections.singletonList(skuLockKey),Collections.singletonList(token));

                         return skuInfo;
                     }else{
                         System.out.println("等待");
                         // 等待
                         Thread.sleep(1000);
                         // 自旋
                         return getSkuInfo(skuId);
                     }
            }else {
                // 缓冲里有数据
              skuInfo  = JSON.parseObject(skuJson, SkuInfo.class);
              return skuInfo;
            }
        } catch (Exception e) {
              e.printStackTrace();
        }finally {
            if(jedis!=null){
                jedis.close();
            }
        }
        // 从数据库返回数据
        return getSkuInfoDB(skuId);
    }

    /*
     从数据库中查找 skuInfo
   */
    private SkuInfo getSkuInfoDB(String skuId) {
        SkuInfo skuInfo = skuInfoMapper.selectByPrimaryKey(skuId);
        SkuImage skuImage = new SkuImage();
        skuImage.setSkuId(skuId);
        //获取图片
        List<SkuImage> skuImageList = skuImageMapper.select(skuImage);
        if(skuImageList !=null || skuImageList.size()>0){
            skuInfo.setSkuImageList(skuImageList);
        }
    //查询平台属性  在详情页面放入
        SkuAttrValue skuAttrValue = new SkuAttrValue();
        skuAttrValue.setSkuId(skuId);
        List<SkuAttrValue> skuAttrValues = skuAttrValueMapper.select(skuAttrValue);
        skuInfo.setSkuAttrValueList(skuAttrValues);
        return skuInfo;
    }

    @Override
    public List<SpuSaleAttr> getSpuSaleAttrListCheckBySku(SkuInfo skuInfo) {
        String skuId = skuInfo.getId();
        String spuId = skuInfo.getSpuId();
      return    spuSaleAttrMapper.selectSpuSaleAttrListCheckBySku(skuId,spuId);


    }

    @Override
    public List<SkuSaleAttrValue> getSkuSaleAttrValueListBySpu(String spuId) {
        return skuSaleAttrValueMapper.selectSkuSaleAttrValueListBySpu(spuId);
    }
}
