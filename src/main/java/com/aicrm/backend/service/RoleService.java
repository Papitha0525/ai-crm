package com.aicrm.backend.service;

import org.springframework.stereotype.Service;

@Service
public class RoleService {

    public String getRole(String userId) {

        if ("1".equals(userId))
            return "ADMIN";

        if ("2".equals(userId))
            return "SALESMAN";

        return "USER";
    }
}