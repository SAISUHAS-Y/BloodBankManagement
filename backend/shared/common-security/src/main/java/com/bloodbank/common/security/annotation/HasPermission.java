package com.bloodbank.common.security.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Custom annotation for fine-grained, permission-based authorization.
 * Can be applied to controllers or service methods.
 * 
 * Example:
 * <pre>
 * &#064;GetMapping
 * &#064;HasPermission("DONOR_READ")
 * public ResponseEntity&lt;ApiResponse&lt;List&lt;DonorDTO&gt;&gt;&gt; getAllDonors() { ... }
 * </pre>
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Inherited
@Documented
public @interface HasPermission {
    String value();
}
