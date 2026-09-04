package com.ruoyi.business.judge;

/** Judge0 只由平台服务端调用，任何实现均不得暴露给浏览器。 */
public interface Judge0Client {
    Judge0Result submit(Judge0Request request);
    Judge0Result poll(String token);
}
