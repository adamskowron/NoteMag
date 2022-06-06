import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.LambdaLogger;
import com.amazonaws.services.lambda.runtime.RequestStreamHandler;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import config.Properties;
import model.LambdaRequest;
import model.LambdaResponse;
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.http.HttpStatusCode;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.io.*;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Map;
import java.util.Optional;

public class UploadLambda implements RequestStreamHandler {

    public void handleRequest(InputStream inputStream, OutputStream outputStream, Context context) throws IOException {
        LambdaLogger logger = context.getLogger();
        logger.log(inputStream.toString());

        ObjectMapper mapper = new ObjectMapper();
        mapper.enable(DeserializationFeature.ACCEPT_EMPTY_STRING_AS_NULL_OBJECT);
        mapper.enable(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY);

        LambdaRequest<byte[]> lambdaRequest = mapper.readValue(inputStream, new TypeReference<LambdaRequest<byte[]>>() {}); //readVal ???
        Map<String, ArrayList<String>> pathParams = lambdaRequest.getPathParams();


        String fileName = Optional.ofNullable(pathParams.get("fileName"))
                .map(list -> list.get(0))
                .orElse(null);

        String userName = Optional.ofNullable(pathParams.get("userName"))
                .map(list -> list.get(0))
                .orElse(null);

        if(fileName == null || userName == null) {
            logger.log("MISSING fileName path variable");
            OutputStreamWriter writer = new OutputStreamWriter(outputStream, "UTF-8");
            writer.write(mapper.writer().withDefaultPrettyPrinter().writeValueAsString(
                    LambdaResponse.builder()
                    .statusCode(HttpStatusCode.BAD_REQUEST)
                    .body("missing fileName or userName path variable")
                    .build()));
            writer.close();
            return;
        }

        double imageSizeKB = lambdaRequest.getBody().length / 1024;
        if(imageSizeKB > 2000.0d) {
            logger.log("Uploaded image exceeds 2 MB");
            OutputStreamWriter writer = new OutputStreamWriter(outputStream, "UTF-8");
            writer.write(mapper.writer().withDefaultPrettyPrinter().writeValueAsString(
                    LambdaResponse.builder()
                            .statusCode(HttpStatusCode.BAD_REQUEST)
                            .body("Uploaded image exceeds 2 MB")
                            .build()));
            writer.close();
            return;
        }

        try {
            logger.log("MAPPED REQUEST");
            logger.log(lambdaRequest.toString());
            logger.log("PATH PARAMS: " + lambdaRequest.getPathParams());
            logger.log(lambdaRequest.getBody().toString());

            InputStream bodyInputStream = new ByteArrayInputStream(lambdaRequest.getBody());
            String mimeType = URLConnection.guessContentTypeFromStream(bodyInputStream);
            logger.log("PARSED FILE Extension: " + mimeType);
            bodyInputStream.close();

            if(mimeType.toLowerCase().equals("image/jpeg") || mimeType.toLowerCase().equals("image/png"))
            {
                logger.log("REQUEST VALID");
                logger.log("GOT IMAGE: " + lambdaRequest.getBody().toString());

                S3Client s3Client = S3Client.builder().region(Region.EU_WEST_1)
                        .credentialsProvider(DefaultCredentialsProvider.create())
                        .build();

                s3Client.putObject(PutObjectRequest.builder()
                                .bucket(Properties.bucketName)
                                .contentType(mimeType.toLowerCase())
                                .key(userName + "/" + fileName).build(),
                        RequestBody.fromBytes(lambdaRequest.getBody()));

                LambdaResponse lambdaResponse = LambdaResponse.builder()
                        .statusCode(HttpStatusCode.CREATED)
                        .body("UPLOADED FILE")
                        .build();

                OutputStreamWriter writer = new OutputStreamWriter(outputStream, "UTF-8");
                writer.write(mapper.writer().withDefaultPrettyPrinter().writeValueAsString(lambdaResponse));
                writer.close();

            } else {
                logger.log("GOT WRONG FILE FORMAT: " + mimeType);
                OutputStreamWriter writer = new OutputStreamWriter(outputStream, "UTF-8");
                writer.write(mapper.writer().withDefaultPrettyPrinter().writeValueAsString(LambdaResponse.builder()
                                .statusCode(HttpStatusCode.BAD_REQUEST)
                                .body("WRONG FILE FORMAT: " + mimeType)
                                .build()));
                writer.close();
            }
        } catch (Exception ex) {
            logger.log("GOT EXCEPTION: " + ex.getMessage());
        }
    }
}

