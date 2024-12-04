package io.github.jsuper1980;

import org.junit.jupiter.api.Test;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

public class SQLGeneratorTest {
    @Test
    void test1() {
        ObjectNode json = new ObjectMapper().createObjectNode();

        json.put("main", "select (@税负计算()) as 税负 from (@应纳税额(行业代码=123)) s1, (@销售额(行业代码=123)) s2");
        json.put("税负计算", "s1.ynse / s2.xse");
        json.put("应纳税额", "select ROUND(SUM(SB.YBTSE), 2) YNSE from SJCK_TY.HXZG_SB_SBXX SB where SB.DJXH in (@行业纳税人(行业代码=#{行业代码}))");
        json.put("销售额", "select ROUND(SUM(SB.JSYJ), 2) XSE from SJCK_TY.HXZG_SB_SBXX SB where SB.DJXH in (@行业纳税人(行业代码=#{行业代码}))");
        json.put("行业纳税人", "SELECT DJ.DJXH FROM SJCK_TY.HXZG_DJ_NSRXX DJ WHERE DJ.HY_DM = '#{行业代码}'");

        SQLResult res = SQLGenerator.generateSQL(json);

        System.out.println(res.getResultSQL());
    }
}
