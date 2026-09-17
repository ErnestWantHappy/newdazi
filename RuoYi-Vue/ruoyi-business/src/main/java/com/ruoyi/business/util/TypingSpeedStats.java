package com.ruoyi.business.util;

/**
 * 打字题统计口径（服务端权威实现）。
 *
 * <p>前端上报的打字速度/正确率/完成率不可采信：机房学生可用控制台直接改写提交负载，
 * 伪造数万字/分的极端值入库并污染学情画像。所有落库统计量必须经本工具由正文比重算得出。</p>
 */
public final class TypingSpeedStats
{
    /** 打字速度上限（字符/分钟）。小学基准 20、初高中 40，500 已远超人类持续极限，仅拦截伪造。 */
    public static final int MAX_TYPING_SPEED = 500;

    private final int typingSpeed;
    private final double accuracyRate;
    private final double completionRate;

    private TypingSpeedStats(int typingSpeed, double accuracyRate, double completionRate)
    {
        this.typingSpeed = typingSpeed;
        this.accuracyRate = accuracyRate;
        this.completionRate = completionRate;
    }

    /**
     * 按正文比重算统计量并封顶。
     *
     * @param correctCount    与原文逐字比对正确的字数
     * @param completedCount  学生输入总字数
     * @param originalLength  原文字数
     * @param timeSpentSeconds 实际答题用时（秒），非法时按 60 秒兜底
     */
    public static TypingSpeedStats resolve(int correctCount, int completedCount, int originalLength, Integer timeSpentSeconds)
    {
        int safeCorrect = Math.max(0, correctCount);
        int safeCompleted = Math.max(0, completedCount);
        int safeOriginal = Math.max(0, originalLength);
        int safeTime = timeSpentSeconds == null || timeSpentSeconds <= 0 ? 60 : timeSpentSeconds.intValue();
        int speed = (int) Math.round(safeCorrect * 60.0 / safeTime);
        speed = Math.max(0, Math.min(MAX_TYPING_SPEED, speed));
        double accuracy = safeCompleted > 0 ? safeCorrect * 100.0 / safeCompleted : 0.0;
        double completion = safeOriginal > 0 ? safeCorrect * 100.0 / safeOriginal : 0.0;
        return new TypingSpeedStats(speed, roundOne(clampPercent(accuracy)), roundOne(clampPercent(completion)));
    }

    /** 逐字比对前缀正确字数（与课程/抽测两处计分循环语义一致）。 */
    public static int countCorrectPrefix(String original, String studentAnswer)
    {
        if (original == null || studentAnswer == null) {
            return 0;
        }
        int correct = 0;
        int compareLength = Math.min(original.length(), studentAnswer.length());
        for (int i = 0; i < compareLength; i++) {
            if (original.charAt(i) == studentAnswer.charAt(i)) {
                correct++;
            }
        }
        return correct;
    }

    /** 客户端自报速度仅做上限钳制（用于无法重算的兼容路径），正常链路一律用 {@link #resolve} 重算。 */
    public static int clampSpeed(Integer reported)
    {
        if (reported == null) {
            return 0;
        }
        return Math.max(0, Math.min(MAX_TYPING_SPEED, reported.intValue()));
    }

    public static double clampPercent(Double value)
    {
        if (value == null || value.isNaN() || value.isInfinite()) {
            return 0.0;
        }
        return Math.max(0.0, Math.min(100.0, value.doubleValue()));
    }

    private static double roundOne(double value)
    {
        return Math.round(value * 10.0) / 10.0;
    }

    /** 打字速度（字符/分钟，已封顶）。 */
    public int getTypingSpeed() { return typingSpeed; }

    /** 正确率（%，0～100，保留一位小数）。 */
    public double getAccuracyRate() { return accuracyRate; }

    /** 完成率（%，0～100，保留一位小数）。 */
    public double getCompletionRate() { return completionRate; }
}
