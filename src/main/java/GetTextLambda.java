import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.LambdaLogger;
import com.amazonaws.services.lambda.runtime.RequestStreamHandler;
import com.fasterxml.jackson.databind.ObjectMapper;
import model.AddLambdaRequest;
import model.AddLambdaResponse;
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.core.SdkBytes;
import software.amazon.awssdk.http.HttpStatusCode;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.rekognition.RekognitionClient;
import software.amazon.awssdk.services.rekognition.model.*;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.Charset;
import java.util.List;

public class GetTextLambda implements RequestStreamHandler {
    @Override
    public void handleRequest(InputStream inputStream, OutputStream outputStream, Context context) throws IOException {
        LambdaLogger logger = context.getLogger();

        ObjectMapper mapper = new ObjectMapper();
        //   logger.log(inputStream.toString().);

        try {
            AddLambdaRequest addLambdaRequest = mapper.readValue(inputStream, AddLambdaRequest.class);
            logger.log(addLambdaRequest.toString());
            // logger.log(addLambdaRequest.getBody());
            logger.log("TUTAJ PRZED IFEM");

            if(addLambdaRequest.getBody() != null)
            {
                logger.log("W IFIE, BODY NOT NULL");
                logger.log("JESTESMY W IF TERAZ REQUEST DO REKONA");

                RekognitionClient rekognitionClient = RekognitionClient.builder()
                        .region(Region.EU_WEST_1)
                        .credentialsProvider(DefaultCredentialsProvider.create())
                        .build();

                logger.log("UTWORZONO REKO CLIENT \n" + rekognitionClient.toString());

             //   String textOut = detectTextLabels(rekognitionClient, addLambdaRequest.getBody(), context);

               // logger.log("Po wywolaniu reko: \n" + textOut);

                AddLambdaResponse addLambdaResponse = AddLambdaResponse.builder()
                        .statusCode(HttpStatusCode.OK)
                        //        .headers(Map.of("ResponseHDR", "123"))
                 //       .body(textOut)
                        .build();

                logger.log("I RESPONSE \n" + addLambdaResponse.toString());

                OutputStreamWriter writer = new OutputStreamWriter(outputStream, "UTF-8");
                writer.write(mapper.writer().withDefaultPrettyPrinter().writeValueAsString(addLambdaResponse));
                writer.close();

                //         responseBody.put("image", "PARSED SOME LINES !!!" + textOut);
//
//                responseJson.put("statusCode", 200);
//                responseJson.put("headers", headerJson);
//                responseJson.put("body", responseBody.toJSONString());
//                responseJson.put("isBaseEncoded", "FALSE");
            } else {
                logger.log("WSZEDL NA ELSA !!!");
            }

        } catch (Exception pex) {
//            responseJson.put("statusCode", 400);
//            responseJson.put("exception", pex);

            //    responseBody.put("body", "throw exception" + pex.getMessage());
            logger.log("MAMY EXCEPTIONA:" + pex.getMessage());
            // isEncodedJson.put()

//            responseJson.put("statusCode", 200);
//            responseJson.put("headers", headerJson);
//            responseJson.put("body", responseBody.toJSONString());
//            responseJson.put("isBaseEncoded", "FALSE");
//
//            OutputStreamWriter writer = new OutputStreamWriter(outputStream, "UTF-8");
//            writer.write(responseJson.toString());
//            writer.close();
        }

//        OutputStreamWriter writer = new OutputStreamWriter(outputStream, "UTF-8");
//        writer.write(responseJson.toString());
//        writer.close();

    }


    public static String detectTextLabels(RekognitionClient rekClient, String imageString, Context context) {

        try {
            LambdaLogger logger = context.getLogger();
            logger.log("JEST TRY POCZATEK FUNC");

            //  InputStream sourceStream = new FileInputStream(base64image);
            SdkBytes sourceBytes = SdkBytes.fromString(imageString, Charset.defaultCharset());
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
