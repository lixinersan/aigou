package cn.itsource.aigou.service.impl;

import cn.itsource.aigou.RedisClient;
import cn.itsource.aigou.StaticPageClient;
import cn.itsource.aigou.domain.ProductType;
import cn.itsource.aigou.mapper.ProductTypeMapper;
import cn.itsource.aigou.service.IProductTypeService;
import cn.itsource.basic.util.AjaxResult;
import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * <p>
 * 商品目录 服务实现类
 * </p>
 *
 * @author æ
 * @since 2019-07-31
 */
@Service
public class ProductTypeServiceImpl extends ServiceImpl<ProductTypeMapper, ProductType> implements IProductTypeService {

    //由于依赖的openfeign，会创建接口的动态代理对象交给spring管理
    @Autowired
    private RedisClient redisClient;

    @Autowired
    private StaticPageClient staticPageClient;

//
    @Override
    public List<ProductType> loadTypeTree() {
        //从redis中获取数据
        AjaxResult productTypes = redisClient.get("productTypes");
        String restObj = (String) productTypes.getRestObj();
        List<ProductType> productType = JSON.parseArray(restObj, ProductType.class);
        //判断是不是有值
        if (productType==null||productType.size()<=0){
//            没有就查数据库，在放到redis中
            productType = loop();
            redisClient.set("productTypes", JSON.toJSONString(productType));
        }
        return productType;
        //递归方式实现
        //return recursive(0L);

    }
/*
 * 生成主页面
 *
 * 先根据product.type.vm生成一个product.type.vm.html
 *
 * 再根据home.vm生成主页面
* */
    @Override
    public void genHomePage() {
        //第一步 ： 生成product.type.vm.html
        //准备map
        Map<String, Object> map = new HashMap<>();

        String templatePath = "E:\\Mycarm\\aigou-parent\\aigou-product-parent\\aigou-product-service\\src\\main\\resources\\template\\product.type.vm";
        String targetPath = "E:\\Mycarm\\aigou-parent\\aigou-product-parent\\aigou-product-service\\src\\main\\resources\\template\\product.type.vm.html";
        //model 就是数据   List 存放所有的商品类型
        List<ProductType> productTypes = loadTypeTree();

        map.put("model", productTypes);
        map.put("templatePath",templatePath);
        map.put("targetPath",targetPath);
        staticPageClient.getStaticPage(map);


        //第二步 ： 生成home.html
/*        map = new HashMap<>();
        templatePath = "E:\\Mycarm\\aigou-parent\\aigou-product-parent\\aigou-product-service\\src\\main\\resources\\template\\home.vm";
        targetPath = "E:\\Mycarm\\aigou-web-front\\aigou-web-home\\home.html";

        //model 中要有一个数据是staticRoot
        Map<String,String> model = new HashMap<>();
        model.put("staticRoot","E:\\Mycarm\\aigou-parent\\aigou-product-parent\\aigou-product-service\\src\\main\\resources\\");
        map.put("model",model);
        map.put("templatePath",templatePath);
        map.put("targetPath",targetPath);
        staticPageClient.genStaticPage(map);*/
    }

    /**
     * 循环方式
     * @return
     */
    private List<ProductType> loop() {
        List<ProductType> productTypes = baseMapper.selectList(null);
        //定义一个List存放一级菜单
        List<ProductType> list = new ArrayList<>();
        //定义一个Map存放所有的ProductType，key是id，value是类型对象
        Map<Long,ProductType> map = new HashMap<>();
        for (ProductType pt : productTypes) {
            map.put(pt.getId(),pt);
        }
        //循环
        for (ProductType productType : productTypes) {
            if(productType.getPid()==0){
                list.add(productType);
            }else{
                ProductType parent = map.get(productType.getPid());
                parent.getChildren().add(productType);
            }
        }
        return list;
    }

    /**
     * 递归方式实现加载类型树
     * 缺点：
     * （1）性能很低，要发送多次sql
     * （2）递归的深度可能会导致栈溢出
     *
     * @return
     */
    private List<ProductType> recursive(Long id) {
        //查询所有一级菜单
        List<ProductType> parents = baseMapper.selectList(new QueryWrapper<ProductType>().eq("pid", id));
        for (ProductType parent : parents) {
            //取到所有的子
            List<ProductType> children = recursive(parent.getId());
            if(!children.isEmpty()){
                parent.setChildren(children);
            }
        }
        return parents;
    }
}
