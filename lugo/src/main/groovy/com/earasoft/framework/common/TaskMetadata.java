package com.earasoft.framework.common;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE) //on class level
public @interface TaskMetadata {
 
    public enum Priority {
       LOW, MEDIUM, HIGH
    }
 
    Priority priority() default Priority.MEDIUM;
    int version() default 1;
}