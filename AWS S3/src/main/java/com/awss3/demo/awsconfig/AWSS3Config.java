package com.awss3.demo.awsconfig;

import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AWSS3Config {
    //properties파일에서 s3 access id와 pw 호출
    @Value("${aws.s3.access-id}")
    private String accessKey;

    @Value("${aws.s3.access-pw}")
    private String secretKey;

    @Bean
    public BasicAWSCredentials awsCredentials(){
        BasicAWSCredentials awsCredentials = new BasicAWSCredentials(accessKey,secretKey);
        return awsCredentials;
    }

    @Bean
    public AmazonS3 awsS3Client(){
        AmazonS3 amazonS3 = AmazonS3ClientBuilder.standard()
                .withRegion(Regions.AP_NORTHEAST_2)
                .withCredentials(new AWSStaticCredentialsProvider(this.awsCredentials()))
                .build();
        return amazonS3;
    }
}
