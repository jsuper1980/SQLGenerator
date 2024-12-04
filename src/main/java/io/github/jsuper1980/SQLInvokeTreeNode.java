package io.github.jsuper1980;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class SQLInvokeTreeNode {
    private String title;
    private String sql;
    private Map<String, String> params;
    private List<SQLInvokeTreeNode> children;
    private String resultSQL;

    public SQLInvokeTreeNode(String title, String sql, Map<String, String> params) {
        this.title = title;
        this.sql = sql;
        this.params = params;
        this.children = new ArrayList<>();
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getSql() {
        return sql;
    }

    public void setSql(String sql) {
        this.sql = sql;
    }

    public Map<String, String> getParams() {
        return params;
    }

    public void setParams(Map<String, String> params) {
        this.params = params;
    }

    public List<SQLInvokeTreeNode> getChildren() {
        return children;
    }

    public void setChildren(List<SQLInvokeTreeNode> children) {
        this.children = children;
    }

    public String getResultSQL() {
        return resultSQL;
    }

    public void setResultSQL(String resultSQL) {
        this.resultSQL = resultSQL;
    }
}
