package com.github.fenrir.xmessaging.rpc.exceptions;

public class NotRpcReturnMessageException extends Exception {
    public NotRpcReturnMessageException(){
        super("this message is not RPC return message");
    }
}
