package com.bbz.outsource.uaes.oa.kt.consts;

/**
 * Created by liulaoye on 17-7-19.
 * json的key常量
 */
public class JsonConsts {
    /**
     * 初始金额
     */
    public static final String INIT_BALANCE_KEY = "initBalance";
    /**
     * 初始金额的缺省值
     */
    public static final String DEFAULT_INIT_BALANCE_VALUE = "100000";
    /**
     * QuantTradeContext的相关配置
     */
    public static final String CTX_KEY = "ctx";
    /**
     * IStockDataProvider的相关配置
     */
    public static final String DATA_PROVIDER_KEY = "dataProvider";
    /**
     * 策略模型的类名：所有的策略模型都必须统一如下规则：
     * 1、包前缀为org.bbz.stock.quanttrader.trade.model.impl.
     * 2、直接包名为类名全小写并去掉model
     */
    public static final String MODEL_CLASS_KEY = "modelClass";
    public static final String MODEL_STATUS_KEY = "status";
    public static final String MODEL_DESC_KEY = "desc";
    public static final String MODEL_NAME_KEY = "modelName";


    /**
     * 股票列表
     */
    public static final String STOCKS = "stocks";


    public static final String USER_SALT = "salt";
    public static final String USER_NAME = "username";
    public static final String USER_PASSWORD = "password";
    public static final String USER_PERMISSIONS = "permissions";
    public static final String USER_ROLES = "roles";
    public static final String DB_ID = "_id";



    public static final String TRADE_RECORDS = "tradeRecords";


}
