package com.github.fenrir.xtraceprocessor.processors.tracebuilder.utils;

public class URIUtils {
    static public boolean match(String uri, String target){
        String[] uriSplit = uri.split("/");
        String[] targetSplit = target.split("/");

        if(uriSplit.length != targetSplit.length){
            return false;
        }else{
            for(int i = 0; i < uriSplit.length; i++){
                if(!targetSplit[i].equals("*")){
                    if(!targetSplit[i].equals(uriSplit[i]))
                        return false;
                }
            }
        }
        return true;
    }
}
