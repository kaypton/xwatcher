package com.github.fenrir.xlocalmonitor.annotations;

import java.lang.annotation.*;

@Documented
@Inherited
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface Monitor {
    String name();
    String[] streams() default {};
    String[] events() default {};
    String[] inspectors() default {};
}
