import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.LambdaLogger;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import config.Properties;
import lombok.SneakyThrows;
import model.LambdaResponse;
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.core.ResponseInputStream;
import software.amazon.awssdk.http.HttpStatusCode;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;

import java.util.Map;
import java.util.Optional;

public class GetImageLambda implements RequestHandler<APIGatewayProxyRequestEvent, LambdaResponse<byte[]>> {

    @SneakyThrows
    @Override
    public LambdaResponse handleRequest(APIGatewayProxyRequestEvent lambdaRequest, Context context) {

        LambdaLogger logger = context.getLogger();

        logger.log("GOT REQUEST :" + lambdaRequest.toString());

        Map<String, String> pathParams = lambdaRequest.getQueryStringParameters();

        String fileName = Optional.ofNullable(
                pathParams.get("fileName"))
                .orElse(null);

        String userName = Optional.ofNullable(
                pathParams.get("userName"))
                .orElse(null);

        if(fileName == null || userName == null) {
            logger.log("MISSING fileName or userName path variable");
            return LambdaResponse.builder()
                    .statusCode(HttpStatusCode.BAD_REQUEST)
                    .body("missing fileName or userName path variable")
                    .build();
        }

        S3Client s3Client = S3Client.builder().region(Region.EU_WEST_1)
                .credentialsProvider(DefaultCredentialsProvider.create())
                .build();

        ResponseInputStream<GetObjectResponse> object = s3Client.getObject(GetObjectRequest.builder()
                .bucket(Properties.bucketName)
                .key(userName + "/" + fileName)
                .build());
        byte[] imageBytes = object.readAllBytes();

        logger.log("GOT ITEM FROM S3 Bucket: " + object.toString());
        return LambdaResponse.builder()
                .statusCode(HttpStatusCode.OK)
                .body(imageBytes)
                .build();
    }
}
