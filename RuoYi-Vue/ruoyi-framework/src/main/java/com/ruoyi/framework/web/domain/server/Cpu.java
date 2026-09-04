package com.ruoyi.framework.web.domain.server;

import com.ruoyi.common.utils.Arith;

/**
 * CPU相关信息
 * 
 * @author ruoyi
 */
public class Cpu
{
    /**
     * 核心数
     */
    private int cpuNum;

    /**
     * CPU 型号（如 AMD Ryzen 5 5600 6-Core Processor）
     */
    private String model;

    /**
     * CPU总的使用率
     */
    private double total;

    /**
     * CPU系统使用率
     */
    private double sys;

    /**
     * CPU用户使用率
     */
    private double used;

    /**
     * CPU当前等待率
     */
    private double wait;

    /**
     * CPU当前空闲率
     */
    private double free;

    public int getCpuNum()
    {
        return cpuNum;
    }

    public void setCpuNum(int cpuNum)
    {
        this.cpuNum = cpuNum;
    }

    public String getModel()
    {
        return model;
    }

    public void setModel(String model)
    {
        this.model = model;
    }

    public double getTotal()
    {
        // 字段存的是 tick 原始差值：总使用率 = 忙碌 tick 占比 = (total - idle) / total
        return safePercent(total - free, total);
    }

    public void setTotal(double total)
    {
        this.total = total;
    }

    public double getSys()
    {
        return safePercent(sys, total);
    }

    public void setSys(double sys)
    {
        this.sys = sys;
    }

    public double getUsed()
    {
        return safePercent(used, total);
    }

    public void setUsed(double used)
    {
        this.used = used;
    }

    public double getWait()
    {
        return safePercent(wait, total);
    }

    public void setWait(double wait)
    {
        this.wait = wait;
    }

    public double getFree()
    {
        return safePercent(free, total);
    }

    public void setFree(double free)
    {
        this.free = free;
    }

    /**
     * OSHI 首次采样可能给出 0/NaN，sys/total 会产生 NaN，
     * Arith.mul 解析 “NaN” 字符串会抛 NumberFormatException 炸掉整个诊断接口；统一兜底为 0。
     */
    private double safePercent(double value, double denominator)
    {
        if (Double.isNaN(value) || Double.isInfinite(value)
                || Double.isNaN(denominator) || Double.isInfinite(denominator) || denominator <= 0)
        {
            return 0D;
        }
        double percent = Arith.round(Arith.mul(value / denominator, 100), 2);
        return Double.isNaN(percent) || Double.isInfinite(percent) ? 0D : percent;
    }
}
