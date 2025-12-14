package cn.junf.JNA;

import com.sun.jna.Library;
import com.sun.jna.Native;
import com.sun.jna.Pointer;

public interface AuthLibrary extends Library {
    AuthLibrary INSTANCE = Native.load("awesome",AuthLibrary.class);

    int add(int a,int b);

    Pointer hello(Pointer name);

    void freePoint(Pointer pointer);

}
