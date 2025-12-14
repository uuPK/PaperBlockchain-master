package cn.junf.JNA;

import com.sun.jna.Memory;
import com.sun.jna.Native;
import com.sun.jna.Pointer;

import java.nio.charset.StandardCharsets;

public class AuthServer {
    public static String hello(String name){
        //申请jna的内存空间
        Pointer p=new Memory(name.getBytes(StandardCharsets.UTF_8).length+1);
        //设置传入参数值
        p.setString(0,name);
        Pointer ptr = null;
        try{
            ptr=AuthLibrary.INSTANCE.hello(p);
            return ptr.getString(0,"utf8");
        }finally {
            //释放传入jna的指针对应的内存空间
            Native.free(Pointer.nativeValue(p));
            //解决多次调用崩溃的问题
            Pointer.nativeValue(p,0);
            if (ptr!=null){
                //释放go中申请的C内存
                AuthLibrary.INSTANCE.freePoint(ptr);
            }
        }
    }
    private static void freeJna(Pointer pointer){
        //释放传入jna的指针对应的内存空间
        Native.free(Pointer.nativeValue(pointer));
        //解决多次调用崩溃的问题
        Pointer.nativeValue(pointer,0);
    }
}
