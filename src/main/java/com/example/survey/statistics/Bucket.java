package com.example.survey.statistics;

/**
 * 分布中的一个桶。
 *
 * @param key   稳定标识（选项 key；打分题为分值的字符串形式）
 * @param label 展示文案
 * @param count 命中次数
 * @param ratio 占答题人数的比例（0~1，分母为 0 时由 ZeroGuard 兜底为 0）
 */
public record Bucket(String key, String label, int count, double ratio) {
}
