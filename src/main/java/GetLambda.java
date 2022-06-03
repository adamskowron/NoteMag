import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import model.LambdaRequest;
import model.LambdaResponse;
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.core.ResponseInputStream;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.http.HttpStatusCode;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;

public class GetLambda implements RequestHandler<LambdaRequest<String>, LambdaResponse<byte[]>> {

    @Override
    public LambdaResponse handleRequest(LambdaRequest<String> lambdaRequest, Context context) {

        String pathRegex = "^(\\/\\w+){0,2}";
        String key = lambdaRequest.getBody();
        if(key.isEmpty() && !key.matches(pathRegex)) {
            return LambdaResponse.builder()
                    .statusCode(HttpStatusCode.BAD_REQUEST)
                    .body("Bad key")
                    .build();
        }

        S3Client s3Client = S3Client.builder().region(Region.EU_WEST_1)
                .credentialsProvider(DefaultCredentialsProvider.create())
                .build();

        ResponseInputStream<GetObjectResponse> object = s3Client.getObject(GetObjectRequest.builder()
                .bucket("images-s3")
                .key(key).build());

        return LambdaResponse.builder()
                .statusCode(HttpStatusCode.OK)
                .body(object).build();
    }
}
