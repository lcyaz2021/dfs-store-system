package com.leyou.upload.service;

import com.leyou.common.enums.ExceptionEnums;
import com.leyou.common.exception.LyException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

@Service
@Slf4j
public class UploadService {
    private static final List<String> ALLOW_TYPES = Arrays.asList("image/jpeg","image/png","image/bmp");
    public String uploadImage(MultipartFile file) {
        // 准备目标路径
        try {
            // 校验文件类型
            String contentType = file.getContentType();
            if(!ALLOW_TYPES.contains(contentType)){
                throw new LyException(ExceptionEnums.INVALID_FILE_TYPE);
            }
            // 校验文件内容
            BufferedImage image = ImageIO.read(file.getInputStream());
            if(image == null){
                throw new LyException(ExceptionEnums.INVALID_FILE_TYPE);
            }

            File dest = new File("D:/JavaWork/leyou_static/", file.getOriginalFilename());
            file.transferTo(dest);
            return "http:image.leyou.com/" + file.getOriginalFilename();
        } catch (IOException e) {
//            e.printStackTrace();
            log.error("上传文件失败！",e);
            throw new LyException(ExceptionEnums.UPLOAD_ERROR);
        }
    }
}
