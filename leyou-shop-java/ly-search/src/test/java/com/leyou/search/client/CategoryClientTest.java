package com.leyou.search.client;

import com.leyou.item.pojo.Brand;
import com.leyou.item.pojo.Category;
import com.netflix.discovery.converters.Auto;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.*;

@RunWith(SpringRunner.class)
@SpringBootTest
public class CategoryClientTest {
    @Autowired
    private CategoryClient categoryClient;
    @Autowired
    private BrandClient brandClient;
    @Test
    public void queryCategoryByIds(){
//        List<Category> categories = categoryClient.queryCategoryListByIds(Arrays.asList(1L, 2L, 3L));
//        Assert.assertEquals(3,categories.size());
//        for (Category category : categories) {
//            System.out.println("category = " + category);
//        }
        List<Brand> brands = brandClient.queryBrandByIds(Arrays.asList(1L, 2L, 3L));
        for (Brand brand : brands) {
            System.out.println("brand = " + brand);
        }
    }
}