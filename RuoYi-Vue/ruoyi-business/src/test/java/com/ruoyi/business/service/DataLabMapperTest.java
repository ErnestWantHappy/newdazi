package com.ruoyi.business.service;
import org.junit.jupiter.api.Test;
import org.apache.ibatis.builder.xml.XMLMapperBuilder;
import org.apache.ibatis.session.Configuration;
import static org.junit.jupiter.api.Assertions.*;
class DataLabMapperTest {
    @Test void mapperStatementsParseAndMatchInterfaces() throws Exception {
        Configuration config=new Configuration();
        for (String name : new String[]{"DataLabMapper","PythonPracticeMapper"}) {
            String path="mapper/business/"+name+".xml";
            try (java.io.InputStream in=getClass().getClassLoader().getResourceAsStream(path)) {
                new XMLMapperBuilder(in,config,path,config.getSqlFragments()).parse();
            }
            for (java.lang.reflect.Method method : Class.forName("com.ruoyi.business.mapper."+name).getMethods()) {
                assertTrue(config.hasStatement("com.ruoyi.business.mapper."+name+"."+method.getName()),method.getName());
            }
        }
    }
}
