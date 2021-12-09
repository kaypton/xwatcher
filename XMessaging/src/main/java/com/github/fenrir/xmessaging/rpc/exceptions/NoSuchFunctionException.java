package com.github.fenrir.xmessaging.rpc.exceptions;

public class NoSuchFunctionException extends Exception{
    public NoSuchFunctionException(){
        super("No such function in this RPC server");
    }
}
