package com.library.util;

import com.library.common.BusinessException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

/**
 * 文件上传工具类（图书封面）
 */
@Component
public class FileUtil {

    @Value("${library.file.upload-dir}")
    private String uploadDir;

    @Value("${library.file.url-prefix}")
    private String urlPrefix;

    /** 允许的图片类型 */
    private static final List<String> ALLOWED_EXT = Arrays.asList("jpg", "jpeg", "png");

    /** 单张封面最大 2MB */
    private static final long MAX_SIZE = 2 * 1024 * 1024;

    /**
     * 保存封面图片，返回访问 URL
     */
    public String saveCover(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BusinessException("上传文件不能为空");
        }
        // 大小校验
        if (file.getSize() > MAX_SIZE) {
            throw new BusinessException("封面图片大小不能超过 2MB");
        }
        // 后缀校验
        String original = file.getOriginalFilename();
        String ext = "";
        if (original != null && original.contains(".")) {
            ext = original.substring(original.lastIndexOf(".") + 1).toLowerCase();
        }
        if (!ALLOWED_EXT.contains(ext)) {
            throw new BusinessException("只允许上传 jpg / png 格式图片");
        }
        // 生成唯一文件名
        String fileName = UUID.randomUUID().toString().replace("-", "") + "." + ext;
        // 确保目录存在
        File dir = new File(uploadDir);
        if (!dir.exists()) {
            dir.mkdirs();
        }
        File dest = new File(dir, fileName);
        try {
            file.transferTo(dest.getAbsoluteFile());
        } catch (IOException e) {
            throw new BusinessException("文件保存失败：" + e.getMessage());
        }
        return urlPrefix + "/" + fileName;
    }
}
