package cn.itsource.aigou.service;


import cn.itsource.aigou.domain.Brand;
import cn.itsource.aigou.query.BrandQuery;
import cn.itsource.basic.util.PageList;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest
public class BrandServiceTest {
//在测试的时候，需要加上spring主角，因为是从配置中心去获取数据源的
    @Autowired
    private IBrandService brandService;

    @Test
    /*测试查询列表的*/
    public void test(){
        brandService.list().forEach(e -> System.out.println(e));
    }

    @Test
    /*测试条件查询的*/
    public void test2(){
        BrandQuery query = new BrandQuery();
        query.setKeyword("代");

        PageList<Brand> brandPageList = brandService.queryPage(query);
        System.out.println(brandPageList.getTotal());

        brandPageList.getRows().forEach(e-> System.out.println(e));
    }
}