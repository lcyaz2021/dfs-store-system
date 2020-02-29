package com.leyou.page.service;

import com.leyou.item.pojo.*;
import com.leyou.page.client.BrandClient;
import com.leyou.page.client.CategoryClient;
import com.leyou.page.client.GoodsClient;
import com.leyou.page.client.SpecificationClient;
import jdk.nashorn.internal.runtime.options.Options;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.io.File;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class PageService {
    @Autowired
    private BrandClient brandClient;

    @Autowired
    private CategoryClient categoryClient;

    @Autowired
    private GoodsClient goodsClient;

    @Autowired
    private SpecificationClient specificationClient;
    @Autowired
    private TemplateEngine templateEngine;

    public Map<String, Object> loadModel(Long spuId) {
        Map<String,Object> model = new HashMap<>();
        // 查询spu
        Spu spu = goodsClient.querySpuById(spuId);
//        System.out.println("spu = " + spu);
        // 查询skus
        List<Sku> skus = spu.getSkus();
//        System.out.println("skus = " + skus);
        // 查询详情
        SpuDetail detail = spu.getSpuDetail();
//        System.out.println("detail = " + detail);
        // 查询brand
        Brand brand = brandClient.queryBrandById(spu.getBrandId());
//        System.out.println("brand = " + brand);
        // 查询商品分类
        List<Category> categories = categoryClient.queryCategoryListByIds(
                Arrays.asList(spu.getCid1(), spu.getCid2(), spu.getCid3()));
//        System.out.println("categories = " + categories);
        // 查询规格参数
        List<SpecGroup> specs = specificationClient.querySpecsByCid(spu.getCid3());

        model.put("title",spu.getTitle());
        model.put("subTitle",spu.getSubTitle());
        model.put("skus",skus);
        model.put("detail",detail);
        model.put("brand",brand);
        model.put("categories",categories);
        model.put("specs",specs);
        return model;
    }
    public void createHtml(Long spuId) {
        // 上下文
        Context context = new Context();
        context.setVariables(loadModel(spuId));
        // 输出流
        File dest = getDestFile(spuId);
        if (dest.exists()) {
            dest.delete();
        }
        try {
            PrintWriter writer = new PrintWriter(dest, "UTF-8");
            templateEngine.process("item", context, writer);
        } catch (Exception e) {
            System.out.println("静态页输出异常！");
        }
    }
    private File getDestFile(Long spuId) {
        System.out.println("spuId = " + spuId);
        return new File("D:\\JavaWork\\static\\", spuId + ".html");
    }
    public void deleteHtml(Long spuId) {
        File dest = getDestFile(spuId);
        if (dest.exists()) {
            dest.delete();
        }
    }
}
