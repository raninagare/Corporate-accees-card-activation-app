    package com.csuf.cs.accessid.accessidserver.controller;

    import com.amazonaws.AmazonClientException;
    import com.amazonaws.AmazonServiceException;
    import com.amazonaws.auth.AWSCredentials;
    import com.amazonaws.auth.AWSStaticCredentialsProvider;
    import com.amazonaws.auth.profile.ProfileCredentialsProvider;
    import com.amazonaws.regions.Regions;
    import com.amazonaws.services.rekognition.AmazonRekognition;
    import com.amazonaws.services.rekognition.AmazonRekognitionClientBuilder;
    import com.amazonaws.services.rekognition.model.*;
    import com.amazonaws.services.s3.AmazonS3;

    import com.amazonaws.services.s3.AmazonS3ClientBuilder;
    import com.amazonaws.services.s3.model.ObjectMetadata;
    import com.amazonaws.services.s3.model.PutObjectRequest;
    import com.amazonaws.services.s3.model.S3Object;
    import org.springframework.http.MediaType;
    import org.springframework.web.bind.annotation.RequestMapping;
    import org.springframework.web.bind.annotation.RequestMethod;
    import org.springframework.web.bind.annotation.RequestParam;
    import org.springframework.web.bind.annotation.RestController;
    import org.springframework.web.multipart.MultipartFile;

    import java.io.IOException;
    import java.io.InputStream;
    import java.util.List;

    @RestController
    @RequestMapping("/api/facerecognition")
    public class AWSController {
        private static String bucketName = "facerecograni1";

            //@RequestMapping(value = "/upload", method = RequestMethod.POST, consumes = MediaType.MULTIPART_FORM_DATA)
            // Upload user image while registration
            @RequestMapping(value = "/upload", method = RequestMethod.POST, consumes = MediaType.ALL_VALUE)
            public String uploadFile(@RequestParam("userName") String userName,
                                         @RequestParam("file") MultipartFile file) throws IOException {

                String status= uploadFiletos3( file );

                createCollection(userName+"Collection");
                //Add file to collection for indexing
                addToCollection(file.getOriginalFilename(),userName+"Collection");
                return status;

            }

        /**
         * This method will allow userto login by face id, first it will store the captured image to s3 and then compare that image with existing collection
         * @param userName
         * @param file
         * @return
         */
        @RequestMapping(value = "/loginwithface", method = RequestMethod.POST, consumes = MediaType.ALL_VALUE)
        public boolean validateUserByFace(@RequestParam("userName") String userName,
                                          @RequestParam("file") MultipartFile file){

            try {
                String status= uploadFiletos3( file );
            } catch (IOException e) {
                e.printStackTrace();
            }
            AmazonRekognition rekognitionClient = getAmazonRekognition();

            // Get an image object from S3 bucket.
            Image image=new Image()
                    .withS3Object(new com.amazonaws.services.rekognition.model.S3Object()
                            .withBucket(bucketName)
                            .withName(file.getOriginalFilename()));

            // Search collection for faces similar to the largest face in the image.
            SearchFacesByImageRequest searchFacesByImageRequest = new SearchFacesByImageRequest()
                    .withCollectionId(userName+"Collection")
                    .withImage(image)
                    .withFaceMatchThreshold(70F)
                    .withMaxFaces(2);

            SearchFacesByImageResult searchFacesByImageResult =
                    rekognitionClient.searchFacesByImage(searchFacesByImageRequest);

            System.out.println("Faces matching largest face in image from" + file.getOriginalFilename());
            List<FaceMatch> faceImageMatches = searchFacesByImageResult.getFaceMatches();
            for (FaceMatch face: faceImageMatches) {
                if(face.getSimilarity()>90){
                    System.out.println( "User is Authenticated :" );
                    return true;
                }
            }
            return false;
        }

        /**
         * Connect to S3 bucket and add file to s3
         * @param file
         * @return
         * @throws IOException
         */
        private String uploadFiletos3( MultipartFile file) throws IOException {

            InputStream is = file.getInputStream();
            String keyName = file.getOriginalFilename();
            String result="";

            AWSCredentials credentials;
            try {
                credentials = new ProfileCredentialsProvider().getCredentials();
            } catch (Exception e) {
                throw new AmazonClientException("Cannot load the credentials from the credential profiles file. "
                        + "Please make sure that your credentials file is at the correct "
                        + "location (/Users/userid/.aws/credentials), and is in valid format.", e);
            }

            AmazonS3 s3client = AmazonS3ClientBuilder
                    .standard()
                    .withCredentials(new AWSStaticCredentialsProvider(credentials))
                    .withRegion( Regions.US_WEST_1)
                    .build();


            try {
                System.out.println("Uploading a new object to S3 from a file\n");
                S3Object s3Object = new S3Object();
                ObjectMetadata omd = new ObjectMetadata();
                omd.setContentType(file.getContentType());
                omd.setContentLength(file.getSize());
                omd.setHeader("filename", file.getName());


                s3Object.setObjectContent(is);
                s3client.putObject(new PutObjectRequest(bucketName, keyName, is, omd));
                s3Object.close();
                //URL url = s3client.generatePresignedUrl(bucketName, keyName, Date.from( Instant.now().plus(5, ChronoUnit.MINUTES)));
                result= "File Uploaded SuccessFully";

            } catch (AmazonServiceException ase) {
                System.out.println("Caught an AmazonServiceException, which " +
                        "means your request made it " +
                        "to Amazon S3, but was rejected with an error response" +
                        " for some reason.");
                System.out.println("Error Message:    " + ase.getMessage());
                System.out.println("HTTP Status Code: " + ase.getStatusCode());
                System.out.println("AWS Error Code:   " + ase.getErrorCode());
                System.out.println("Error Type:       " + ase.getErrorType());
                System.out.println("Request ID:       " + ase.getRequestId());
            } catch (AmazonClientException ace) {
                System.out.println("Caught an AmazonClientException, which " +
                        "means the client encountered " +
                        "an internal error while trying to " +
                        "communicate with S3, " +
                        "such as not being able to access the network.");
                System.out.println("Error Message: " + ace.getMessage());
                result=ace.getMessage();
            }

            return result;
        }

        /**
         * Add File to collection for indexing and easy searaching
         * @param imageName
         */
        public void addToCollection( String imageName,String collectionName){
            AmazonRekognition rekognitionClient = getAmazonRekognition();


            Image image = new Image()
                    .withS3Object(new com.amazonaws.services.rekognition.model.S3Object()
                            .withBucket(bucketName)
                            .withName(imageName));

            IndexFacesRequest indexFacesRequest = new IndexFacesRequest()
                    .withImage(image)
                    .withQualityFilter( QualityFilter.AUTO)
                    .withMaxFaces(1)
                    .withCollectionId(collectionName)
                    .withExternalImageId(imageName)
                    .withDetectionAttributes("DEFAULT");

            IndexFacesResult indexFacesResult = rekognitionClient.indexFaces(indexFacesRequest);

            System.out.println("Results for " + imageName);
            System.out.println("Faces indexed:");
            List<FaceRecord> faceRecords = indexFacesResult.getFaceRecords();
            for (FaceRecord faceRecord : faceRecords) {
                System.out.println("  Face ID: " + faceRecord.getFace().getFaceId());
                System.out.println("  Location:" + faceRecord.getFaceDetail().getBoundingBox().toString());
            }
        }


        /**
         * Login to s3 bucket
         * @return
         */
        private AmazonRekognition getAmazonRekognition() {
            AWSCredentials credentials;
            try {
                credentials = new ProfileCredentialsProvider().getCredentials();
            } catch (Exception e) {
                throw new AmazonClientException( "Cannot load the credentials from the credential profiles file. "
                        + "Please make sure that your credentials file is at the correct "
                        + "location (/Users/userid/.aws/credentials), and is in valid format.", e );
            }
            return AmazonRekognitionClientBuilder
                    .standard()
                    .withRegion( Regions.US_WEST_1 )
                    .withCredentials( new AWSStaticCredentialsProvider( credentials ) )
                    .build();
        }

        public void createCollection(String collectionId){

            AmazonRekognition rekognitionClient = getAmazonRekognition();
            System.out.println("Creating collection: " +
                    collectionId );

            CreateCollectionRequest request = new CreateCollectionRequest()
                    .withCollectionId(collectionId);

            CreateCollectionResult createCollectionResult = rekognitionClient.createCollection(request);
            System.out.println("CollectionArn : " +
                    createCollectionResult.getCollectionArn());
            System.out.println("Status code : " +
                    createCollectionResult.getStatusCode().toString());

        }


    }
