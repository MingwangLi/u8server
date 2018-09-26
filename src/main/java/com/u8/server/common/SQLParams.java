package com.u8.server.common;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * SQL 参数组合对象
 * Created by ant on 2016/8/5.
 */
public class SQLParams {

    private List<SQLParamPair> params = new ArrayList<SQLParamPair>();


    public SQLParams(){
    }

    public void EQ(String key, Object value){

        addParam(key, value, SQLCondition.EQ);

    }

    // 大于
    public void GT(String key, Object value){
        addParam(key, value, SQLCondition.GT);
    }

    //大于等于
    public void GE(String key, Object value){
        addParam(key, value, SQLCondition.GE);
    }

    //小于
    public void LT(String key, Object value){
        addParam(key, value, SQLCondition.LT);
    }

    //小于等于
    public void LE(String key, Object value){
        addParam(key, value, SQLCondition.LE);
    }

    //不等于
    public void NE(String key, Object value){
        addParam(key, value, SQLCondition.NE);
    }

    //Between
    public void Between(String key, Object value, Object value2){
        addParam(key, new Object[]{value, value2}, SQLCondition.BETWEEN);
    }

    //IN
    public void IN(String key, Object[] values){
        addParam(key, values, SQLCondition.IN);
    }

    //Like
    public void Like(String key, Object value){
        addParam(key, "%" + value + "%", SQLCondition.LIKE);
    }


    /**
     * 获取where条件sql语句
     * @return
     */
    public String getWhereSQL(){

        if(params.size() <= 0){
            return "";
        }


        StringBuilder sb = new StringBuilder();
        sb.append(" where ");
        for(int i=0; i<params.size(); i++){

            sb.append(params.get(i).toString());

            if(i < params.size() - 1){
                sb.append(" and ");
            }
        }

        return sb.toString();
    }

    /**
     * 获取where条件语句中的值数组
     * @return
     */
    public Object[] getWhereValues(){
        List<Object> objs = new ArrayList<Object>();
        for(SQLParamPair p : params){
            if(p.condition != SQLCondition.IN){
                objs.addAll(Arrays.asList(p.values));
            }
        }
        return objs.toArray();
    }


    private void addParam(String key, Object value, SQLCondition condition){
        addParam(key, new Object[]{value}, condition);
    }

    private void addParam(String key, Object values[], SQLCondition condition){
        addParam(new SQLParamPair(key, condition, values));
    }

    private void addParam(SQLParamPair p){
        if(p == null) return;
        params.add(p);
    }


    /**
     * SQL 一个条件对象
     */
    class SQLParamPair{

        public SQLParamPair(){

        }

        public SQLParamPair(String key, SQLCondition condition, Object[] values){
            this.key = key;
            this.condition = condition;
            this.values = values;
        }

        public String key;
        public SQLCondition condition;
        public Object[] values;

        private String getInValues(){
            StringBuilder sb = new StringBuilder();
            if(values != null){
                for(int i=0; i<values.length;i++){
                    sb.append(values[i]);
                    if(i < values.length-1){
                        sb.append(",");
                    }
                }
            }
            return sb.toString();
        }

        public String toString(){
            StringBuilder sb = new StringBuilder();
            sb.append(key);

            switch(condition){
                case EQ:
                    sb.append("=?");
                    break;
                case GT:
                    sb.append(">?");
                    break;
                case GE:
                    sb.append(">=?");
                    break;
                case LT:
                    sb.append("<?");
                    break;
                case LE:
                    sb.append("<=?");
                    break;
                case BETWEEN:
                    sb.append(" BETWEEN ? AND ?");
                    break;
                case LIKE:
                    sb.append(" LIKE ?");
                    break;
                case IN:
                    sb.append(" IN (").append(getInValues()).append(")");
            }

            return sb.toString();
        }
    }

    /**
     * SQL 条件类型
     */
    enum SQLCondition{
        EQ, //=
        GT, //>
        GE, //>=
        LT, //<
        LE, //<=
        NE, //<>
        BETWEEN, //between
        LIKE,    //like
        IN       //in
    }
}
