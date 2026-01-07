package com.factory.monitor.security;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.io.Serializable;

/**
 * 用户主体信息（存储在SecurityContext中）
 */
@Data
@AllArgsConstructor
public class UserPrincipal implements Serializable {
    private static final long serialVersionUID = 1L;

    private Long userId;
    private String username;
    private String role;
}
