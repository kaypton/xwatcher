package com.github.fenrir.xmessaging.rpc.exceptions;

public class NoSuchServerException extends Exception{
    public NoSuchServerException(){
        super("no such subserver in this RPC server");
    }
}
