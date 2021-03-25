# awstutorial
AWS
---
1. # AWS S3 활용해서 Image 업로드하기
    0. ## Contents Delivery Network -> AWS S3
    1. ## [AWS S3](https://s3.console.aws.amazon.com/s3/hom) 에서 버킷만들기
    ```java
    * 버킷 이름 : caucampustcontact
    * 모든 public access 차단
    ```
    
    2. AccessKey 만들기
    ```java
    1. 루트사용자에 AWS Management 콘솔 로 로그인

    2. 오른쪽 상단의 탐색 모음에서 계정 이름 또는 번호를 선택한 다음 My Security Credentials(내 보안 자격 증명)를 선택

    3. 액세스 키(액세스 키 ID 및 보안 액세스 키) 섹션을 확장

    4. Create New Access Key(새 액세스 키 생성).을 선택하십시오. 이미 두 개의 액세스 키가 있는 경우 이 버튼이 비활성화

    5. 메시지가 표시되면 액세스 키 표시 또는 키 파일 다운로드를 선택합니다. 이 경우에만 보안 액세스 키를 저장할 수 있다.

    6. 보안 위치에 보안 액세스 키를 저장한 후 닫기를 선택
    ```
    
    2. ## Project setting
        1. ### 환경
        ```java
        * jdk 11
        * Gradle
        * SpringBoot 2.4.3
        * Dependency
        - Web
        - Lombok
        - Spring Boot Jpa
        - Spring Security
        - Devtools
        - H2DB
        ```

        2. ### application.properties
        ```properties
        #H2DB
        spring.jpa.hibernate.ddl-auto=create
        spring.jpa.show-sql=true
        spring.jpa.properties.hibernate.format_sql=true
        logging.level.org.hibernate.type.descriptor.sql=trace
        ```

    3. ## Gradle에 AWS SDK 추가
    ```gradle
    implementation platform('com.amazonaws:aws-java-sdk-bom:1.11.228')
    implementation 'com.amazonaws:aws-java-sdk-s3'
    ``` 

    4. ## AWS AccessKey Properties에 추가
    ```properties
    #AWS
    aws.s3.access-id= "다운받은 key 참조"
    aws.s3.access-pw= "다운받은 key 참조"
    aws.s3.bucket=caucampuscontact
    ```

    5. ## AWS config 클래스 생성
    ### [AWS S3 호환 API 가이드](https://docs.toast.com/ko/Storage/Object%20Storage/ko/s3-api-guide/)
    ```java
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
                    .withCredentials(new AWSStaticCredentialsProvider())
                    .build();
            return amazonS3;
        }
    }
    ```

    6. ## AWS 비즈니스 클래스 생성
    ```java
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
    ```

    7. ## Controller 구현
    ```java
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
    ```
    8. ## Postman Test
    - <img src="https://user-images.githubusercontent.com/60174144/112311329-f07da700-8ce8-11eb-9a03-bdbd20e706e2.png" width="80%" height="80%">

    9. ## image 경로
    ```http
    https://bucketname.s3.amazonaws.com/foldername/image

    적용-->

    https://caucampuscontact.s3.amazonaws.com/images/imageProfileIcon.png
    ```

    10. ## Browser Test
    ```java
    [access denied 에러]

    [해경 방법]
    1. AWS 로그인 후 S3 접속
    2. 해당 버킷의 버킷 정책 클릭
    3. 안에 json 코드 입력하여 
    모든 곳에서 Access 가능하도록 설정
    ```

    - ### [Bucket policy examples 참고](https://docs.aws.amazon.com/AmazonS3/latest/userguide/example-bucket-policies.html)

    ```json
    * 추가한 json
    * Read Only
    {
        "Version": "2012-10-17",
        "Statement": [
            {
                "Sid": "PublicRead",
                "Effect": "Allow",
                "Principal": "*",
                "Action": [
                    "s3:GetObject",
                    "s3:GetObjectVersion"
                ],
                "Resource": "arn:aws:s3:::caucampuscontact/images/*"
            }
        ]
    }
    ```
    ```java
    [결과]
    * Brower에서
    https://caucampuscontact.s3.amazonaws.com/images/imageProfileIcon.png
    입력하여
    접근성공
    ```