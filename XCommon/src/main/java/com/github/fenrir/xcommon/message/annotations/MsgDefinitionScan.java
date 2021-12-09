package com.github.fenrir.xcommon.message.annotations;

import java.lang.annotation.*;

@Documented
@Inherited
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface MsgDefinitionScan {
    String[] path();
}
