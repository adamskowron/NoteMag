import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.LambdaLogger;
import com.amazonaws.services.lambda.runtime.RequestStreamHandler;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import model.LambdaRequest;
import model.LambdaResponse;
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.http.HttpStatusCode;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectResponse;

import java.io.*;
import java.net.URLConnection;

public class AddLambdaHandler implements RequestStreamHandler {

    public void handleRequest(InputStream inputStream, OutputStream outputStream, Context context) throws IOException {
        LambdaLogger logger = context.getLogger();
        logger.log(inputStream.toString());

        ObjectMapper mapper = new ObjectMapper();
        mapper.enable(DeserializationFeature.ACCEPT_EMPTY_STRING_AS_NULL_OBJECT);
        mapper.enable(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY);

        LambdaRequest<byte[]> lambdaRequest = mapper.readValue(inputStream, new TypeReference<LambdaRequest<byte[]>>() {}); //readVal ???

        if(lambdaRequest.getHeaders().get("fileName").isEmpty() && lambdaRequest.getHeaders().get("fileName").size() > 1
        && lambdaRequest.getHeaders().get("userName").isEmpty() && lambdaRequest.getHeaders().get("userName").size() > 1) {
            logger.log("MISSING fileName HEADER");
            OutputStreamWriter writer = new OutputStreamWriter(outputStream, "UTF-8");
            writer.write(mapper.writer().withDefaultPrettyPrinter().writeValueAsString(LambdaResponse.builder()
                    .statusCode(HttpStatusCode.BAD_REQUEST)
                    .body("missing fileName or userName header")
                    .build()));
            writer.close();
            return;
        }
        String fileName = lambdaRequest.getHeaders().get("fileName").get(0);
        String userName = lambdaRequest.getHeaders().get("userName").get(0);

        try {

            logger.log("MAPPED REQUEST");
            logger.log(lambdaRequest.toString());
            logger.log("HEADERS: " + lambdaRequest.getHeaders());
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

                PutObjectResponse putObjectResponse = s3Client.putObject(PutObjectRequest.builder()
                                .bucket("images-s3")
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

