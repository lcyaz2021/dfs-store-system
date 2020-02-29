package com.leyou.item.service;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.leyou.common.dto.CartDTO;
import com.leyou.common.enums.ExceptionEnums;
import com.leyou.common.exception.LyException;
import com.leyou.common.vo.PageResult;
import com.leyou.item.mapper.SkuMapper;
import com.leyou.item.mapper.SpuDetailMapper;
import com.leyou.item.mapper.SpuMapper;
import com.leyou.item.mapper.StockMapper;
import com.leyou.item.pojo.*;
import org.apache.commons.lang.StringUtils;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import tk.mybatis.mapper.entity.Example;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class GoodsService {
    @Autowired
    private SpuMapper spuMapper;
    @Autowired
    private SpuDetailMapper spuDetailMapper;
    @Autowired
    private CategoryService categoryService;
    @Autowired
    private BrandService brandService;
    @Autowired
    private SkuMapper skuMapper;
    @Autowired
    private StockMapper stockMapper;
    @Autowired
    private AmqpTemplate amqpTemplate;
    public PageResult<Spu> querySpuByPage(Integer page, Integer rows, Boolean saleable, String key) {
        // 分页
        PageHelper.startPage(page,rows);
        // 搜索字段过滤
        Example example = new Example(Spu.class);
        Example.Criteria criteria = example.createCriteria();
        if(StringUtils.isNotBlank(key)){
            criteria.andLike("title", "%" + key + "%");
        }
        // 上下架过滤
        if(saleable != null){
            criteria.andEqualTo("saleable",saleable);
        }
        // 默认排序 按商品更新时间
        example.setOrderByClause("last_update_time DESC");
        // 查询
        List<Spu> spus = spuMapper.selectByExample(example);
        if(CollectionUtils.isEmpty(spus)){
            throw new LyException(ExceptionEnums.GOODS_NOT_FOUND);
        }
        // 解析分类和品牌的名称
        loadCategoryAndBrandName(spus);
        // 解析分页结果
        PageInfo<Spu> info = new PageInfo<>(spus);
        // 返回包装过后的结果
        return new PageResult<>(info.getTotal(),spus);
    }

    private void loadCategoryAndBrandName(List<Spu> spus) {
        for (Spu spu : spus) {
            // 处理分类名称
            List<String> names = categoryService.queryByIds(Arrays.asList(spu.getCid1(), spu.getCid2(), spu.getCid3()))
                    .stream().map(Category::getName).collect(Collectors.toList());
            spu.setCname(StringUtils.join(names,"/"));
            // 处理品牌名称
            spu.setBname(brandService.queryById(spu.getBrandId()).getName());
        }
    }
    @Transactional
    public void saveGoods(Spu spu) {
        // 新增spu
        spu.setId(null);
        spu.setCreateTime(new Date());
        spu.setLastUpdateTime(spu.getCreateTime());
        spu.setSaleable(true);
        spu.setValid(false);
        int count = spuMapper.insert(spu);
        if(count != 1){
            throw new LyException(ExceptionEnums.GOODS_SAVE_ERROR);
        }
        // 新增detail
        SpuDetail detail = spu.getSpuDetail();
        detail.setSpuId(spu.getId());
        count = spuDetailMapper.insert(detail);
        // 定义库存集合
        List<Stock> stockList = new ArrayList<>();
        if(count != 1){
            throw new LyException(ExceptionEnums.GOODS_SAVE_ERROR);
        }
        // 新增sku
        List<Sku> skus = spu.getSkus();
        for (Sku sku : skus) {
            sku.setCreateTime(new Date());
            sku.setLastUpdateTime(sku.getCreateTime());
            sku.setSpuId(spu.getId());
            count = skuMapper.insert(sku);
            if(count != 1){
                throw new LyException(ExceptionEnums.GOODS_SAVE_ERROR);
            }
            // 新增库存
            Stock stock = new Stock();
            stock.setSkuId(sku.getId());
//            System.out.println(stock.getSkuId());
            stock.setStock(sku.getStock());
            stockList.add(stock);
        }
        // 批量新增库存
        count = stockMapper.insertList(stockList);
        // 校验
        if(count != stockList.size()){
            throw new LyException(ExceptionEnums.GOODS_SAVE_ERROR);
        }

        // 发送mq消息
        amqpTemplate.convertAndSend("item.insert",spu.getId());
    }


    public SpuDetail queryDetailById(Long spuId) {
        SpuDetail spuDetail = spuDetailMapper.selectByPrimaryKey(spuId);
        if (spuDetail == null) {
            throw new LyException(ExceptionEnums.SPU_DETAIL_NOT_FOUND);
        }
        return spuDetail;
    }

    public List<Sku> querySkusBySpuId(Long spuId) {
        // 查询sku
        Sku sku = new Sku();
        sku.setSpuId(spuId);
        List<Sku> skuList = skuMapper.select(sku);
        if (CollectionUtils.isEmpty(skuList)) {
            throw new LyException(ExceptionEnums.GOODS_SKU_NOT_FOUND);
        }
        // 查询库存
        for (Sku s : skuList) {
            Stock stock = stockMapper.selectByPrimaryKey(s.getId());
            if (stock == null) {
                throw new LyException(ExceptionEnums.GOODS_STOCK_NOT_FOUND);
            }
            s.setStock(stock.getStock());
        }
        return skuList;
    }
    @Transactional
    public void updateGoods(Spu spu) {
        if (spu.getId() == null) {
            throw new LyException(ExceptionEnums.GOODS_ID_CANNOT_BE_NULL);
        }
        Sku sku = new Sku();
        sku.setSpuId(spu.getId());
        // 查询sku
        List<Sku> skuList = skuMapper.select(sku);
        if (!CollectionUtils.isEmpty(skuList)) {
            // 删除sku
            skuMapper.delete(sku);
            // 删除stock
            List<Long> ids = skuList.stream().map(Sku::getId).collect(Collectors.toList());
            stockMapper.deleteByIdList(ids);
        }
        // 修改spu
        spu.setValid(null);
        spu.setSaleable(null);
        spu.setLastUpdateTime(new Date());
        spu.setCreateTime(null);

        int count = spuMapper.updateByPrimaryKeySelective(spu);
        if (count != 1) {
            throw new LyException(ExceptionEnums.GOODS_UPDATE_ERROR);
        }
        // 修改detail
        count = spuDetailMapper.updateByPrimaryKeySelective(spu.getSpuDetail());
        if (count != 1) {
            throw new LyException(ExceptionEnums.GOODS_UPDATE_ERROR);
        }
        // 新增sku和stock
        saveSkuAndStock(spu);
        // 发送mq消息
        amqpTemplate.convertAndSend("item.update",spu.getId());
    }
    private void saveSkuAndStock(Spu spu) {
        List<Stock> stockList = new ArrayList<>();
        List<Sku> skus = spu.getSkus();
        for (Sku sku : skus) {
            sku.setSpuId(spu.getId());
            sku.setCreateTime(new Date());
            sku.setLastUpdateTime(sku.getCreateTime());
            int count = skuMapper.insert(sku);
            if (count != 1) {
                throw new LyException(ExceptionEnums.GOODS_SAVE_ERROR);
            }
            // 保存库存信息
            Stock stock = new Stock();
            // 只有sku保存成功了, 才能获取sku的id, 批量新增不会产生id
            stock.setSkuId(sku.getId());
            stock.setStock(sku.getStock());
            stockList.add(stock);
        }
        // 批量新增库存
        // 这里有个坑, 有两个InsertListMapper,insertList方法限制不一样, 可以点进去看看
        int count = stockMapper.insertList(stockList);
        if (count != stockList.size()) {
            throw new LyException(ExceptionEnums.GOODS_SAVE_ERROR);
        }
    }

    public Spu querySpuById(Long id) {
        // 查询spu
        Spu spu = spuMapper.selectByPrimaryKey(id);
        if(spu == null){
            throw new LyException(ExceptionEnums.GOODS_NOT_FOUND);
        }
        // 查询sku
        spu.setSkus(querySkusBySpuId(id));
        // 查询detail
        spu.setSpuDetail(queryDetailById(id));
        return spu;
    }

    /**
     * 购物车中商品列表
     */
    public List<Sku> querySkuByIds(List<Long> ids) {
        // 查询 sku
        List<Sku> skus = skuMapper.selectByIdList(ids);
        if (CollectionUtils.isEmpty(skus)) {
            throw new LyException(ExceptionEnums.GOODS_SKU_NOT_FOUND);
        }
        // 填充库存
        loadStockInSku(ids, skus);
        return skus;
    }
    /**
     * 根据 sku 列表查询库存
     */
    private void loadStockInSku(List<Long> ids, List<Sku> skus) {
        // 查询库存
        List<Stock> stockList = stockMapper.selectByIdList(ids);
        if (CollectionUtils.isEmpty(stockList)) {
            throw new LyException(ExceptionEnums.GOODS_STOCK_NOT_FOUND);
        }
        // 将库存转为 map，key 是 skuId，值是库存
        Map<Long, Integer> stockMap = stockList.stream()
                .collect(Collectors.toMap(stock -> stock.getSkuId(), stock -> stock.getStock()));
        // 保存库存到 sku
        for (Sku sku : skus) {
            sku.setStock(stockMap.get(sku.getId()));
        }
    }
    public void decreaseStock(List<CartDTO> carts) {
        for (CartDTO cart : carts) {
            int count = stockMapper.decreaseStock(cart.getSkuId(), cart.getNum());
            if (count != 1) {
                throw new LyException(ExceptionEnums.STOCK_NOT_ENOUGH);
            }
        }
    }
}
