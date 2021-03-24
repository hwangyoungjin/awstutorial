package com.awss3.demo.controller;

import com.awss3.demo.service.AwsService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@Slf4j
@RestController
@RequestMapping("/api")
public class FileApiController {

    @Autowired
    AwsService awsService;

    /**
     * 이미지 1개 업로드하기
     * "file" 이름으로 이미지 받는다
     */
    @PostMapping("/upload")
    public String uploadImages(@RequestParam("file") MultipartFile file) throws Exception{
        log.debug("[ Call /obj/img-put - POST ]");

        //s3Path : 버켓의 /images 경로(시작경로)를 의미
        String s3Path = "/images";

        //파라미터로 받은 file을 "/image" 폴더 안에 저장
        awsService.uploadMultipartFile(file,s3Path);

        return "success";
    }
}
