package com.leyou.common.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public enum ExceptionEnums {
    CATEGORY_NOT_FOUND(404,"商品分类不存在"),
    ORDER_NOT_FOUND(404,"订单不存在"),
    GOODS_NOT_FOUND(404,"商品不存在"),
    ORDER_STATUS_NOT_FOUND(404,"订单状态不存在"),
    GOODS_ID_CANNOT_BE_NULL(404,"商品ID不存在"),
    GOODS_SKU_NOT_FOUND(404,"商品SKU不存在"),
    GOODS_STOCK_NOT_FOUND(404,"商品库存不存在"),
    BRAND_NOT_FOUND(404,"品牌没查到"),
    CART_NOT_FOUND(404,"购物车没找到"),
    SPEC_GROUP_NOT_FOUND(404,"规格组不存在"),
    ORDER_DETAIL_NOT_FOUND(404,"订单详情不存在"),
    SPEC_PARAM_NOT_FOUND(404,"规格规格参数不存在"),
    SPU_DETAIL_NOT_FOUND(404,"SPU详情不存在"),
    BRAND_SAVE_ERROR(500,"新增品牌失败"),
    GOODS_UPDATE_ERROR(500,"品牌修改失败"),
    GOODS_SAVE_ERROR(500,"新增商品失败"),
    ORDER_STATUS_ERROR(500,"订单状态错误"),
    UPLOAD_ERROR(500,"文件上传失败"),
    WX_PAY_ORDER_FAIL(500,"微信下单失败"),
    INVALID_FILE_TYPE(400,"无效的文件类型"),
    STOCK_NOT_ENOUGH(400,"库存不足"),
    USER_DATA_TYPE_ERROR(400,"请求参数有误"),
    INVALID_VERIFY_CODE(400,"验证码错误"),
    CREATE_ORDER_ERROR(400,"订单创建失败"),
    INVALID_ORDER_PARAM(400,"订单参数错误"),
    UPDATE_ORDER_STATUS_ERROR(400,"更新订单失败"),
    INVALID_USERNAME_PASSWORD(404,"用户名或密码不存在"),
    INVALID_SIGN_ERROR(404,"微信支付签名错误"),
    UN_AUTHORIZE(401,"未认证"),
    ;
    private int code;
    private String msg;
}
