package com.github.fenrir.xcommon.message.annotations;

import java.lang.annotation.*;

@Documented
@Inherited
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface Stream {
    String name();
    String topicName();
    String description() default "";
}
