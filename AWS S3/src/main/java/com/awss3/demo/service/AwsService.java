package com.awss3.demo.service;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@Service
public class AwsService {
    @Autowired
    private AmazonS3 s3Client;

    @Value("${aws.s3.bucket}")
    public String bucketName; //S3 버킷 경로


    /**
     * S3에 이미지 업로드 메소드
     * @param file <- 업로드 할 파일
     * @param bucketKey <- 업로드할 버켓 key로 경로(폴더) '/image'가 들어온다
     * @throws IOException
     */
    public void uploadMultipartFile(MultipartFile file, String bucketKey) throws IOException{

        /**
         * Java용 AWS SDK를 사용하여 객체를 업로드할 때 서버 측 암호화를 사용하여 객체를 암호화할 수 있습니다.
         * 서버 측 암호화를 요청하려면 ObjectMetadata의 PutObjectRequest 속성을 사용하여
         * x-amz-server-side-encryption 요청 헤더를 설정합니다. AmazonS3Client의 putObject() 메서드를 호출할 때
         * Amazon S3에서는 데이터를 암호화해 저장합니다.
         */
        ObjectMetadata omd = new ObjectMetadata();

        omd.setContentType(file.getContentType());
        omd.setContentLength(file.getSize());
        omd.setHeader("filename",file.getOriginalFilename());

        // config에서 만든 Bean을 이용하여 버켓에 Object 업로드
        // 참조 https://docs.toast.com/ko/Storage/Object%20Storage/ko/s3-api-guide/
        s3Client.putObject(new PutObjectRequest(
                bucketName+bucketKey,
                file.getOriginalFilename(),
                file.getInputStream(),omd));

    }
}
