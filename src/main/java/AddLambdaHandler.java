import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.LambdaLogger;
import com.amazonaws.services.lambda.runtime.RequestStreamHandler;
import com.fasterxml.jackson.databind.ObjectMapper;
import model.AddLambdaRequest;
import model.AddLambdaResponse;
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
    @Override
    public void handleRequest(InputStream inputStream, OutputStream outputStream, Context context) throws IOException {
        LambdaLogger logger = context.getLogger();

        ObjectMapper mapper = new ObjectMapper();
        logger.log(inputStream.toString());

        try {
            AddLambdaRequest addLambdaRequest = mapper.readValue(inputStream, AddLambdaRequest.class);
            logger.log("MAPPED REQUEST");
            logger.log(addLambdaRequest.toString());
             logger.log(addLambdaRequest.getBody().toString());

            InputStream bodyInputStream = new ByteArrayInputStream(addLambdaRequest.getBody());
            String mimeType = URLConnection.guessContentTypeFromStream(bodyInputStream);
            bodyInputStream.close();
            //...close stream

            if(mimeType.toLowerCase().equals("image/jpeg") || mimeType.toLowerCase().equals("image/png"))
            {
                logger.log("REQUEST VALID");


                logger.log("GOT IMAGE: " + addLambdaRequest.getBody().toString());

                S3Client s3Client = S3Client.builder().region(Region.EU_WEST_1)
                        .credentialsProvider(DefaultCredentialsProvider.create())
                        .build();

                PutObjectResponse sa = s3Client.putObject(PutObjectRequest.builder()
                                .bucket("images-s3")
                                .key("USER1FOLDER/imgNAME1").build(),
                        RequestBody.fromBytes(addLambdaRequest.getBody()));

                AddLambdaResponse addLambdaResponse = AddLambdaResponse.builder()
                        .statusCode(HttpStatusCode.OK)
                        //        .headers(Map.of("ResponseHDR", "123"))
                        .body(sa.toString())
                        .build();

                OutputStreamWriter writer = new OutputStreamWriter(outputStream, "UTF-8");
                writer.write(mapper.writer().withDefaultPrettyPrinter().writeValueAsString(addLambdaResponse));
                writer.close();

            } else {
                logger.log("GOT WRONG FILE FORMAT: " + mimeType);
                OutputStreamWriter writer = new OutputStreamWriter(outputStream, "UTF-8");
                writer.write(mapper.writer().withDefaultPrettyPrinter().writeValueAsString("WRONG FILE FORMAT"));
                writer.close();
            }

        } catch (Exception ex) {
            logger.log("GOT EXCEPTION: " + ex.getMessage());
        }
    }
}

