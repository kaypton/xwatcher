package com.github.fenrir.xlocalmonitor.annotations;

import java.lang.annotation.*;

@Documented
@Inherited
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface InspectorScan {
    String[] path();
}
