package com.shawn.aiagent.support.time;

import java.time.Duration;
import java.util.regex.Pattern;

/**
 * 统一的 Duration 解析工具：支持纯数字毫秒或 ISO-8601 Duration。
 */
public final class DurationParser {

    private static final Pattern DIGITS = Pattern.compile("^\\d+$");

    private DurationParser() {
    }

    /**
     * Intent: 将字符串解析为 Duration，支持纯数字毫秒或 ISO-8601 Duration。
     * Input: raw - 待解析的字符串，不能为空；允许包含首尾空白。
     * Output: Duration 对象；纯数字按毫秒解释，其它按 Duration.parse 解析。
     * SideEffects: 无。
     * Failure: raw 为 null 时抛 IllegalArgumentException；非数字且非合法 ISO-8601 格式时抛 DateTimeParseException。
     * Idempotency: 对同一输入返回一致的 Duration。
     */
    public static Duration parseMsOrIso(String raw) {
        if (raw == null) {
            throw new IllegalArgumentException("duration string cannot be null");
        }
        String trimmed = raw.trim();
        if (DIGITS.matcher(trimmed).matches()) {
            return Duration.ofMillis(Long.parseLong(trimmed));
        }
        return Duration.parse(trimmed);
    }
}

