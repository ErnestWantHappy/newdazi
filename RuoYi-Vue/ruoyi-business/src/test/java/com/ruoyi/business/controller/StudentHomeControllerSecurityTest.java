package com.ruoyi.business.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;
import org.springframework.security.access.prepost.PreAuthorize;

class StudentHomeControllerSecurityTest
{
    @Test
    void allStudentHomeEndpointsRequireStudentIdentity()
    {
        PreAuthorize authorization = StudentHomeController.class.getAnnotation(PreAuthorize.class);

        assertEquals("@studentSs.isStudent()", authorization.value());
    }
}
