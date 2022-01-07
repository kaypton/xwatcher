package com.github.fenrir.xhttpclient;

public class XHttpResponse<T> {
    private T body;
    private int statusCode;

    public static class Builder<T> {

        private final XHttpResponse<T> response;

        public Builder(){
            response = new XHttpResponse<>();
        }

        public Builder<T> statusCode(int code){
            this.response.setStatusCode(code);
            return this;
        }

        public Builder<T> body(T body){
            this.response.setBody(body);
            return this;
        }

        public XHttpResponse<T> build(){
            return this.response;
        }

        public static <T> Builder<T> newBuilder(Class<T> clazz){
            return new Builder<>();
        }
    }

    public T body(){
        return this.body;
    }

    public int statusCode(){
        return this.statusCode;
    }

    private void setBody(T body){
        this.body = body;
    }

    private void setStatusCode(int code){
        this.statusCode = code;
    }
}
