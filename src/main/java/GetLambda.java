import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.amazonaws.services.dynamodbv2.document.DynamoDB;
import com.amazonaws.services.dynamodbv2.document.Item;
import com.amazonaws.services.dynamodbv2.document.Table;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.LambdaLogger;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import config.Properties;
import software.amazon.awssdk.http.HttpStatusCode;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public class GetLambda implements RequestHandler<APIGatewayProxyRequestEvent, APIGatewayProxyResponseEvent> {

    @Override
    public APIGatewayProxyResponseEvent handleRequest(APIGatewayProxyRequestEvent lambdaRequest, Context context) {

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
            return new APIGatewayProxyResponseEvent()
                    .withStatusCode(HttpStatusCode.BAD_REQUEST)
                    .withBody("Wrong path variables");
        }

        AmazonDynamoDB client = AmazonDynamoDBClientBuilder.standard().build();
        DynamoDB dynamoDB = new DynamoDB(client);

        Table table = dynamoDB.getTable(Properties.tableName);
        Item item = table.getItem("userId", userName, "imageName", fileName);
        if(item == null) {
            logger.log("element with id not found: " + userName + "/" + fileName);
            return new APIGatewayProxyResponseEvent()
                    .withStatusCode(HttpStatusCode.NOT_FOUND)
                    .withBody("Not found element");
        }
        List<String> text = item.getList("translatedLines");
        logger.log("GOT ITEM FROM DYNAMODB: " + item.toJSON());
        return new APIGatewayProxyResponseEvent()
                .withStatusCode(HttpStatusCode.OK)
                .withIsBase64Encoded(false)
                .withBody(text.toString());
    }
}
