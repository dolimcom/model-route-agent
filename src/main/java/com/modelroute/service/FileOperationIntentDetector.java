package com.modelroute.service;

import java.util.Locale;
import java.util.regex.Pattern;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Component
public class FileOperationIntentDetector {

    private static final Pattern MUTATION_INTENT = Pattern.compile(
            "(?:创建|新建|生成|写入|保存|修改|更新|替换|追加|删除|移除|重命名|改名|"
                    + "create|write|save|update|modify|replace|append|delete|remove|rename)",
            Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE);
    private static final Pattern FILE_CONTEXT = Pattern.compile(
            "(?:文件|目录|文件夹|源码|代码|\\.[a-z0-9]{1,12}(?:\\s|$)|"
                    + "file|folder|directory|workspace|source)",
            Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE);
    private static final Pattern SELECTED_REFERENCE = Pattern.compile(
            "(?:当前|这个|该文件|选中|上面的文件|此文件|"
                    + "current|selected|this file|the file above)",
            Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE);

    public boolean isMutation(String instruction, String selectedPath) {
        if (!StringUtils.hasText(instruction) || !MUTATION_INTENT.matcher(instruction).find()) {
            return false;
        }
        String normalized = instruction.toLowerCase(Locale.ROOT);
        return FILE_CONTEXT.matcher(normalized).find()
                || normalized.contains("路径")
                || (StringUtils.hasText(selectedPath) && SELECTED_REFERENCE.matcher(normalized).find());
    }
}
