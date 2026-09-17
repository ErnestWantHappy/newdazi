package com.ruoyi.framework.interceptor;

import java.util.UUID;
import org.slf4j.MDC;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import com.ruoyi.common.core.domain.model.LoginUser;
import com.ruoyi.common.utils.SecurityUtils;

/**
 * 课堂事故“黑匣子”基础：把 traceId / 用户 / 学校(部门) 写入 MDC，
 * 使本请求抛出的所有异常日志都能直接定位到人，无需登录服务器按时间猜。
 * 必须在 afterCompletion 清理 MDC，否则线程池复用会把上一个学生的身份串到下一个请求。
 */
@Component
public class DiagnosticContextInterceptor implements HandlerInterceptor
{
    @Override
    public boolean preHandle(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response, @NonNull Object handler)
    {
        MDC.put("traceId", UUID.randomUUID().toString().replace("-", "").substring(0, 12));
        try
        {
            LoginUser loginUser = SecurityUtils.getLoginUser();
            if (loginUser != null && loginUser.getUser() != null)
            {
                MDC.put("userId", String.valueOf(loginUser.getUser().getUserId()));
                if (loginUser.getUser().getDeptId() != null)
                {
                    MDC.put("deptId", String.valueOf(loginUser.getUser().getDeptId()));
                }
            }
        }
        catch (Exception ignore)
        {
            // 匿名请求（登录、验证码等）没有用户上下文，保留 traceId 即可
        }
        return true;
    }

    @Override
    public void afterCompletion(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response, @NonNull Object handler, Exception ex)
    {
        MDC.clear();
    }
}
