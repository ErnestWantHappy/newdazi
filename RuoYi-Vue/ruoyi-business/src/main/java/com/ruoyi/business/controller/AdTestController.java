package com.ruoyi.business.controller;

import org.springframework.context.annotation.Bean;
import org.springframework.ldap.core.LdapTemplate;
import org.springframework.ldap.core.support.LdapContextSource;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/test")
public class AdTestController {

    // --- 请在这里填入你从网络管理员那里获取的真实信息 ---
    // AD服务器的地址和端口
    private static final String AD_URL = "ldap://10.52.1.1:389";

    // Base DN (搜索的根路径)
    private static final String BASE_DN = "DC=jky,DC=local"; // 例如: "DC=example,DC=com"

    // 服务账号的完整DN (Distinguished Name)
    private static final String AD_USER_DN = "CN=ServiceUser,OU=Users,DC=jky,DC=local"; // 例如: "CN=admin,DC=example,DC=com"

    // 服务账号的密码
    private static final String AD_PASSWORD = "your_service_account_password";
    // ----------------------------------------------------

    @Bean
    public LdapTemplate ldapTemplate() {
        LdapContextSource contextSource = new LdapContextSource();
        contextSource.setUrl(AD_URL);
        contextSource.setBase(BASE_DN);
        contextSource.setUserDn(AD_USER_DN);
        contextSource.setPassword(AD_PASSWORD);
        contextSource.afterPropertiesSet(); // 使配置生效
        return new LdapTemplate(contextSource);
    }

    @GetMapping("/ad")
    public String testAdConnection() {
        try {
            // 尝试在 "ou=Teachers" 这个组织单位下，查找一个存在的用户
            // LdapQueryBuilder.query().where("objectclass").is("user").and("sAMAccountName").is("zhangsan")
            // 这里我们用一个更简单的方式，直接看能否搜索出结果
            ldapTemplate().search("ou=Teachers", "(objectclass=user)", (javax.naming.directory.Attributes attrs) -> {
                // 只要能进到这里，说明搜索成功了
                return attrs.get("cn").get();
            });

            return "成功连接到AD域，并且能够执行搜索查询！项目最大技术风险已解除！";

        } catch (Exception e) {
            // 将详细的错误信息返回，便于排查
            e.printStackTrace(); // 在后台打印详细错误
            return "连接或查询AD域失败！请检查后台日志获取详细错误信息。错误: " + e.getMessage();
        }
    }
}