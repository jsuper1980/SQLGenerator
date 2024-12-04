package io.github.jsuper1980;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import com.fasterxml.jackson.databind.JsonNode;

/**
 * 结构化 SQL 生成器
 * 
 * 移植自: 程序员鱼皮 https://gitee.com/yinxiaozhi/sql-generator
 * 授权: Apache License Version 2.0
 */
public class SQLGenerator {
    private static final String FIELD_MAIN = "main";
    private static final String FIELD_SQL = "sql";
    private static final String FIELD_PARAMS = "params";

    /**
     * 生成SQL的入口函数
     *
     * @param json JSON输入数据
     * @return 包含生成的resultSQL和invokeTree（根节点的第一个子节点）的对象
     */
    public static SQLResult generateSQL(JsonNode json) {
        if (!json.has(FIELD_MAIN)) {
            return null;
        }
        JsonNode mainNode = json.get(FIELD_MAIN);
        String sql = getSql(mainNode);
        if (sql == null) {
            return null;
        }
        SQLInvokeTreeNode rootInvokeTreeNode = new SQLInvokeTreeNode(FIELD_MAIN, sql, getParams(mainNode));
        String resultSQL = generateSQL(FIELD_MAIN, json, getParams(mainNode), rootInvokeTreeNode);
        return new SQLResult(resultSQL, rootInvokeTreeNode.getChildren().size() > 0 ? rootInvokeTreeNode.getChildren().get(0) : null);
    }

    /**
     * 递归生成SQL
     *
     * @param key 当前节点的键
     * @param context 整个JSON上下文节点
     * @param params 参数映射
     * @param invokeTreeNode 当前调用树节点
     * @return 生成的SQL字符串
     */
    private static String generateSQL(String key, JsonNode context, Map<String, String> params, SQLInvokeTreeNode invokeTreeNode) {
        JsonNode currentNode = context.get(key);
        if (currentNode == null) {
            return "";
        }
        SQLInvokeTreeNode childInvokeTreeNode = null;
        if (invokeTreeNode != null) {
            childInvokeTreeNode = new SQLInvokeTreeNode(key, getSql(currentNode), params);
            invokeTreeNode.getChildren().add(childInvokeTreeNode);
        }
        String result = replaceParams(currentNode, context, params, childInvokeTreeNode);
        String resultSQL = replaceSubSql(result, context, childInvokeTreeNode);
        if (childInvokeTreeNode != null) {
            childInvokeTreeNode.setResultSQL(resultSQL);
        }
        return resultSQL;
    }

    /**
     * 参数替换（params）
     *
     * @param currentNode 当前节点
     * @param context 整个JSON上下文节点
     * @param params 动态参数
     * @param invokeTreeNode 当前调用树节点
     * @return 替换参数后的SQL字符串
     */
    private static String replaceParams(JsonNode currentNode, JsonNode context, Map<String, String> params, SQLInvokeTreeNode invokeTreeNode) {
        if (currentNode == null) {
            return "";
        }
        String sql = getSql(currentNode);
        if (sql == null) {
            return "";
        }
        // 动态、静态参数结合，且优先用动态参数
        // 实现用静态参数配置默认值
        Map<String, String> combinedParams = new HashMap<>();
        Map<String, String> currentParams = getParams(currentNode);
        combinedParams.putAll(currentParams);
        if (params != null) {
            combinedParams.putAll(params);
        }

        if (invokeTreeNode != null) {
            invokeTreeNode.setParams(combinedParams);
        }
        // 无需替换
        if (combinedParams.isEmpty()) {
            return sql;
        }

        String result = sql;
        for (Map.Entry<String, String> entry : combinedParams.entrySet()) {
            String replacedKey = "#\\{" + entry.getKey() + "\\}";
            result = result.replaceAll(replacedKey, entry.getValue());
        }
        return result;
    }

    /**
     * 替换子SQL（@xxx）
     *
     * @param sql SQL字符串
     * @param context 整个JSON上下文节点
     * @param invokeTreeNode 当前调用树节点
     * @return 替换子SQL后的SQL字符串
     */
    private static String replaceSubSql(String sql, JsonNode context, SQLInvokeTreeNode invokeTreeNode) {
        if (sql == null) {
            return "";
        }
        String result = sql;
        String[] regExpMatchArray = matchSubQuery(result);
        // 依次替换
        while (regExpMatchArray != null && regExpMatchArray.length > 2) {
            String subKey = regExpMatchArray[1];
            JsonNode replacementNode = context.get(subKey);
            // 没有可替换的节点
            if (replacementNode == null) {
                throw new IllegalArgumentException(subKey + " 不存在");
            }
            // 获取要传递的动态参数
            Map<String, String> params = new HashMap<>();
            String paramsStr = regExpMatchArray[2];
            if (paramsStr != null) {
                paramsStr = paramsStr.trim();
                List<String> singleParamsStrArray = List.of(paramsStr.split(","));
                for (String singleParamsStr : singleParamsStrArray) {
                    String[] keyValueArray = singleParamsStr.split("=", 2);
                    if (keyValueArray.length >= 2) {
                        params.put(keyValueArray[0].trim(), keyValueArray[1].trim());
                    }
                }
            }

            String replacement = generateSQL(subKey, context, params, invokeTreeNode);
            result = result.replace(regExpMatchArray[0], replacement);
            regExpMatchArray = matchSubQuery(result);
        }
        return result;
    }

    /**
     * 匹配子查询
     *
     * @param str 待匹配的字符串
     * @return 匹配到的子查询信息列表，每个元素包含完整匹配内容、子SQL名称以及参数列表
     */
    private static String[] matchSubQuery(String str) {
        Pattern pattern = Pattern.compile("@([\\u4e00-\\u9fa5_a-zA-Z0-9]+)\\((.*?)\\)");
        Matcher matcher = pattern.matcher(str);
        if (matcher.find()) {
            String fullMatch = matcher.group();
            String subKey = matcher.group(1);
            String paramsStr = matcher.group(2);
            return new String[] {fullMatch, subKey, paramsStr};
        }
        return null;
    }

    /**
     * 获取节点
     * 
     * @param node
     * @return
     */
    private static String getSql(JsonNode node) {
        return node.has(FIELD_SQL) ? node.get(FIELD_SQL).asText() : node.asText();
    }

    /**
     * 获取参数
     * 
     * @param node
     * @return
     */
    private static Map<String, String> getParams(JsonNode node) {
        Map<String, String> params = new HashMap<>();
        JsonNode paramsNode = node.get(FIELD_PARAMS);
        if (paramsNode != null) {
            paramsNode.fields().forEachRemaining(entry -> params.put(entry.getKey(), entry.getValue().asText()));
        }
        return params;
    }
}
