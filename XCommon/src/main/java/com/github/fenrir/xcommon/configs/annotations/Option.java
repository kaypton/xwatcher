package com.github.fenrir.xcommon.configs.annotations;

import java.lang.annotation.*;

@Documented
@Inherited
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Option {
    String name();
    String usage();
    boolean need();
}
