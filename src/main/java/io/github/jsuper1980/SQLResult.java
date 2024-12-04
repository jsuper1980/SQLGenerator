package io.github.jsuper1980;

public class SQLResult {
    private String resultSQL;
    private SQLInvokeTreeNode invokeTree;

    public SQLResult(String resultSQL, SQLInvokeTreeNode invokeTree) {
        this.resultSQL = resultSQL;
        this.invokeTree = invokeTree;
    }

    public String getResultSQL() {
        return resultSQL;
    }

    public void setResultSQL(String resultSQL) {
        this.resultSQL = resultSQL;
    }

    public SQLInvokeTreeNode getInvokeTree() {
        return invokeTree;
    }

    public void setInvokeTree(SQLInvokeTreeNode invokeTree) {
        this.invokeTree = invokeTree;
    }

}
