import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.LambdaLogger;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import model.S3Event;
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.core.ResponseInputStream;
import software.amazon.awssdk.core.SdkBytes;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.rekognition.RekognitionClient;
import software.amazon.awssdk.services.rekognition.model.*;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.List;

public class GetTextLambda implements RequestHandler<InputStream, String> {

    @Override
    public String handleRequest(InputStream event, Context context) {
        LambdaLogger logger = context.getLogger();

        ObjectMapper mapper = new ObjectMapper();
        mapper.setVisibility(PropertyAccessor.FIELD, JsonAutoDetect.Visibility.ANY);
        mapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false); //TODO: FIX MAPPING AND PASSING TO KOGNITO
        logger.log(event.toString());

        S3Event s3Event = mapper.convertValue(event, S3Event.class);

        logger.log("MAPPED REQUEST: " + s3Event);

        S3Client s3Client = S3Client.builder().region(Region.EU_WEST_1)
                .credentialsProvider(DefaultCredentialsProvider.create())
                .build();

        ResponseInputStream<GetObjectResponse> image = s3Client.getObject(GetObjectRequest.builder()
                .bucket("images-s3")
//                .key(s3Event.stream()
//                        .map(S3Event::getObject)
//                        .collect(Collectors.joining()))
                .key(s3Event.getRecords().get(0).toString())
                .build());

        RekognitionClient rekognitionClient = RekognitionClient.builder()
                .region(Region.EU_WEST_1)
                .credentialsProvider(DefaultCredentialsProvider.create())
                .build();

        try {
            String textLabels = detectTextLabels(rekognitionClient, image.readAllBytes(), context);
            logger.log("W PALE SIE NIE MIESCI: " + textLabels);
        } catch (IOException e) {
            logger.log("EXCEPTION ON READ BYTES FROM S3 IMAGE" + e.getMessage());
        }

        return s3Event.toString();
    }

//    @Override
//    public void handleRequest(InputStream inputStream, OutputStream outputStream, Context context) throws IOException {
//        LambdaLogger logger = context.getLogger();
//
//        logger.log("process image lambda triggered by S3 image upload");
//
//        ObjectMapper mapper = new ObjectMapper();
//        logger.log("RAW INTPUT STREAM: " + inputStream.toString());
//
//        LambdaRequest lambdaRequest = mapper.readValue(inputStream, LambdaRequest.class);
//        logger.log("MAPPED REQUEST: " + lambdaRequest.toString());
//        logger.log("MAPPED REQUEST BODY: " + lambdaRequest.getBody().toString());
//
//        RekognitionClient rekognitionClient = RekognitionClient.builder()
//                .region(Region.EU_WEST_1)
//                .credentialsProvider(DefaultCredentialsProvider.create())
//                .build();
//
//        logger.log("UTWORZONO REKO CLIENT \n" + rekognitionClient.toString());
//
//        String textOut = detectTextLabels(rekognitionClient, lambdaRequest.getBody(), context);
//        logger.log("PARSED TEXT !!!!!!XDD: " + textOut);
//
//        try {
//            LambdaRequest lambdaRequest = mapper.readValue(inputStream, LambdaRequest.class);
//            logger.log(lambdaRequest.toString());
//            // logger.log(addLambdaRequest.getBody());
//            logger.log("TUTAJ PRZED IFEM");
//
//            if(lambdaRequest.getBody() != null)
//            {
//                logger.log("W IFIE, BODY NOT NULL");
//                logger.log("JESTESMY W IF TERAZ REQUEST DO REKONA");
//
//                RekognitionClient rekognitionClient = RekognitionClient.builder()
//                        .region(Region.EU_WEST_1)
//                        .credentialsProvider(DefaultCredentialsProvider.create())
//                        .build();
//
//                logger.log("UTWORZONO REKO CLIENT \n" + rekognitionClient.toString());
//
//             //   String textOut = detectTextLabels(rekognitionClient, addLambdaRequest.getBody(), context);
//
//               // logger.log("Po wywolaniu reko: \n" + textOut);
//
//                LambdaResponse lambdaResponse = LambdaResponse.builder()
//                        .statusCode(HttpStatusCode.OK)
//                        //        .headers(Map.of("ResponseHDR", "123"))
//                 //       .body(textOut)
//                        .build();
//
//                logger.log("I RESPONSE \n" + lambdaResponse.toString());
//
//                OutputStreamWriter writer = new OutputStreamWriter(outputStream, "UTF-8");
//                writer.write(mapper.writer().withDefaultPrettyPrinter().writeValueAsString(lambdaResponse));
//                writer.close();
//
//                //         responseBody.put("image", "PARSED SOME LINES !!!" + textOut);
////
////                responseJson.put("statusCode", 200);
////                responseJson.put("headers", headerJson);
////                responseJson.put("body", responseBody.toJSONString());
////                responseJson.put("isBaseEncoded", "FALSE");
//            } else {
//                logger.log("WSZEDL NA ELSA !!!");
//            }
//
//        } catch (Exception pex) {
////            responseJson.put("statusCode", 400);
////            responseJson.put("exception", pex);
//
//            //    responseBody.put("body", "throw exception" + pex.getMessage());
//            logger.log("MAMY EXCEPTIONA:" + pex.getMessage());
//            // isEncodedJson.put()
//
////            responseJson.put("statusCode", 200);
////            responseJson.put("headers", headerJson);
////            responseJson.put("body", responseBody.toJSONString());
////            responseJson.put("isBaseEncoded", "FALSE");
////
////            OutputStreamWriter writer = new OutputStreamWriter(outputStream, "UTF-8");
////            writer.write(responseJson.toString());
////            writer.close();
//        }
//
////        OutputStreamWriter writer = new OutputStreamWriter(outputStream, "UTF-8");
////        writer.write(responseJson.toString());
////        writer.close();
//
 //   }


    public static String detectTextLabels(RekognitionClient rekClient, byte[] imageBytes, Context context) {

        try {
            LambdaLogger logger = context.getLogger();
            logger.log("JEST TRY POCZATEK FUNC");

            //  InputStream sourceStream = new FileInputStream(base64image);
            SdkBytes sourceBytes = SdkBytes.fromByteArray(imageBytes);
//                    .split("\\.")[1].
//                    replace('-', '+').replace('_', '/')));

            logger.log("W FUNKCJII !!! SDKBYtse:" + sourceBytes.asString(Charset.defaultCharset()));
            //   logger.log(sourceBytes.asString(Charset.defaultCharset()));

            // Create an Image object for the source image
            Image souImage = Image.builder()
                    .bytes(sourceBytes)
                    .build();

            logger.log(souImage.toString());
            logger.log(souImage.bytes().toString());
            logger.log(souImage.sdkFields().toString());

            DetectTextRequest textRequest = DetectTextRequest.builder()
                    .image(souImage)
                    .build();

            DetectTextResponse textResponse = rekClient.detectText(textRequest);

            logger.log("detekt text response: " + textResponse.textModelVersion());
            logger.log("detekt text response: " + textResponse.toString());

            List<TextDetection> textCollection = textResponse.textDetections();

            //    System.out.println("Detected lines and words !!!");
            return textCollection.toString();
//            for (TextDetection text: textCollection) {
//                System.out.println("Detected: " + text.detectedText());
//                System.out.println("Confidence: " + text.confidence().toString());
//                System.out.println("Id : " + text.id());
//                System.out.println("Parent Id: " + text.parentId());
//                System.out.println("Type: " + text.type());
//                System.out.println();

        } catch (RekognitionException e) {
            return e.toString();
        }
    }
}
