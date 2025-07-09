package com.ureka.team3.utong_backend.datatrade.utils;

public class RedisKeyUtil {

    private static final String ORDER_QUEUE_PREFIX = "order_queue";
    private static final String ORDER_BOOK_PREFIX = "order_book";

    public static String buildSellListKey(String dataCode, long price) {
        return ORDER_QUEUE_PREFIX + ":sell:" + dataCode + ":" + price;
    }

    public static String buildBuyListKey(String dataCode, long price) {
        return ORDER_QUEUE_PREFIX + ":buy:" + dataCode + ":" + price;
    }

    public static String buildSellZSetKey(String dataCode) {
        return ORDER_BOOK_PREFIX + ":sell:" + dataCode;
    }

    public static String buildBuyZSetKey(String dataCode) {
        return ORDER_BOOK_PREFIX + ":buy:" + dataCode;
    }

    public static String buildOrderKey(String type, Long orderId) {
        return "order:" + type + ":" + orderId;
    }
}
