package com.wsg.protocl_7000;

import com.alibaba.fastjson.JSON;
import com.wsg.protocl_7001.Message;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import org.apache.commons.codec.binary.Hex;
import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import org.springframework.stereotype.Component;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

/**
 * Created by logread on 2018/12/19.
 */
@ChannelHandler.Sharable
@Component
public class Protocol7000Handler extends ChannelInboundHandlerAdapter {

    private String key = "7b522e435b6f5a514b71716a24af60de";
    private static final byte MSG_TYPE_KEEP_LIVE = (byte) 0x80;

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        //super.channelRead(ctx, msg);
        Message message = (Message)msg;
        if(MSG_TYPE_KEEP_LIVE == message.getType()){
            System.out.println("保活消息："+JSON.toJSONString(message));
        }
        if(message.getData() != null){
            System.out.println("数据消息："+JSON.toJSONString(message));
            byte[] decrypt = decrypt(message.getData());
            System.out.println(new String(decrypt, "utf-8"));
        }
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        System.out.println(ctx);
        System.out.println("7000的设备连接上来了");
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        System.out.println(ctx);
        System.out.println("7000的设备离线了");
    }

    private byte[] decrypt(byte[] content) throws Exception {
        byte[] keyByte = Hex.decodeHex(key.toCharArray());
        SecretKey secretKey = new SecretKeySpec(keyByte, "AES");
        Cipher cipher = Cipher.getInstance("AES");
        cipher.init(Cipher.DECRYPT_MODE, secretKey);
        return cipher.doFinal(content);
    }
}
