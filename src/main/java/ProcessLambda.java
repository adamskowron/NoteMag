import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.amazonaws.services.dynamodbv2.document.DynamoDB;
import com.amazonaws.services.dynamodbv2.document.Item;
import com.amazonaws.services.dynamodbv2.document.Table;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.LambdaLogger;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.S3Event;
import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import config.Properties;
import lombok.SneakyThrows;
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.core.ResponseInputStream;
import software.amazon.awssdk.core.SdkBytes;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.rekognition.RekognitionClient;
import software.amazon.awssdk.services.rekognition.model.DetectTextRequest;
import software.amazon.awssdk.services.rekognition.model.DetectTextResponse;
import software.amazon.awssdk.services.rekognition.model.Image;
import software.amazon.awssdk.services.rekognition.model.TextDetection;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;

import java.util.List;
import java.util.stream.Collectors;

public class ProcessLambda implements RequestHandler<S3Event, String> {

    @SneakyThrows
    @Override
    public String handleRequest(S3Event event, Context context) {

        LambdaLogger logger = context.getLogger();

        ObjectMapper mapper = new ObjectMapper();
        mapper.setVisibility(PropertyAccessor.FIELD, JsonAutoDetect.Visibility.ANY);
        mapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
        logger.log(event.getRecords().toString());

        logger.log("MAPPED REQUEST: " + event.toString());

        String s3Key = event.getRecords().get(0).getS3().getObject().getKey();
        String[] s3KeyComponents = s3Key.split("/");
        String userName = s3KeyComponents[0];
        String imageName = s3KeyComponents[1];

        S3Client s3Client = S3Client.builder().region(Region.EU_WEST_1)
                .credentialsProvider(DefaultCredentialsProvider.create())
                .build();

        ResponseInputStream<GetObjectResponse> image = s3Client.getObject(GetObjectRequest.builder()
                .bucket(Properties.bucketName)
                .key(event.getRecords().get(0).getS3().getObject().getKey())
                .build());

        logger.log("IMAGE KEY: " + event.getRecords().get(0).getS3().getObject().getKey());


        RekognitionClient rekognitionClient = RekognitionClient.builder()
                .region(Region.EU_WEST_1)
                .credentialsProvider(DefaultCredentialsProvider.create())
                .build();

        logger.log("created rekognition client");

        SdkBytes sourceBytes = SdkBytes.fromInputStream(image);
        Image souImage = Image.builder()
                .bytes(sourceBytes)
                .build();
        DetectTextRequest textRequest = DetectTextRequest.builder()
                .image(souImage).build();
        DetectTextResponse textResponse = rekognitionClient.detectText(textRequest);

        List<String> detectedLines = textResponse.textDetections().stream()
                .map(TextDetection::detectedText)
                .collect(Collectors.toList());

        if(detectedLines.isEmpty()) {
            logger.log("No lines are parsed");
            return "No lines parsed";
        }

        logger.log("Detected lines and words");
        StringBuilder lines = new StringBuilder();
        for (String text: detectedLines) {
            lines.append(text);
        }
        logger.log(lines.toString());

        AmazonDynamoDB client = AmazonDynamoDBClientBuilder.standard().build();
        DynamoDB dynamoDB = new DynamoDB(client);
        Table table = dynamoDB.getTable(Properties.tableName);
        Item item = new Item()
                .withPrimaryKey("userId", userName, "imageName", imageName)
                .withList("translatedLines", detectedLines)
                .withString("S3Key", s3Key);

        logger.log("Saving to DynamoDb object: " + item.toJSON());

        return table.putItem(item).toString();
    }
}
