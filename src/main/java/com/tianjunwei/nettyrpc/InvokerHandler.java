package com.tianjunwei.nettyrpc;

import java.lang.reflect.Method;
import java.util.concurrent.ConcurrentHashMap;

import com.tianjunwei.nettyrpc.entity.ClassInfo;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

public class InvokerHandler extends ChannelInboundHandlerAdapter {
    //把classmap当做容器,存储服务对象
    public static ConcurrentHashMap<String, Object> classMap = new ConcurrentHashMap<String, Object>();

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        ClassInfo classInfo = (ClassInfo) msg;
        Object claszz = null;
        //用于记录反射获得类，这样可以提高性能
        if (!classMap.containsKey(classInfo.getClassName())) {
            try {
                //动态加载类，也就是生成对象
                claszz = Class.forName(classInfo.getClassName()).newInstance();
                classMap.put(classInfo.getClassName(), claszz);
            } catch (InstantiationException | IllegalAccessException | ClassNotFoundException e) {
                e.printStackTrace();
            }
        } else {
            claszz = classMap.get(classInfo.getClassName());
        }
        Method method = claszz.getClass().getMethod(classInfo.getMethodName(), classInfo.getTypes());
        Object result = method.invoke(claszz, classInfo.getObjects());
        ctx.write(result);
        ctx.flush();
        ctx.close();
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        cause.printStackTrace();
        ctx.close();
    }

}
