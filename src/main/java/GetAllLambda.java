import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.amazonaws.services.dynamodbv2.document.DynamoDB;
import com.amazonaws.services.dynamodbv2.document.ItemCollection;
import com.amazonaws.services.dynamodbv2.document.ScanOutcome;
import com.amazonaws.services.dynamodbv2.document.Table;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.LambdaLogger;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import config.Properties;
import software.amazon.awssdk.http.HttpStatusCode;

import java.util.Map;
import java.util.Optional;

public class GetAllLambda implements RequestHandler<APIGatewayProxyRequestEvent, APIGatewayProxyResponseEvent> {

    @Override
    public APIGatewayProxyResponseEvent handleRequest(APIGatewayProxyRequestEvent lambdaRequest, Context context) {

        LambdaLogger logger = context.getLogger();

        logger.log("GOT REQUEST :" + lambdaRequest.toString());

        Map<String, String> pathParams = lambdaRequest.getQueryStringParameters();

        String userName = Optional.ofNullable(
                pathParams.get("userName"))
                .orElse(null);

        if( userName == null) {
            logger.log("NO INFORMATION ABOUT USER");
            return new APIGatewayProxyResponseEvent()
                    .withStatusCode(HttpStatusCode.BAD_REQUEST)
                    .withBody("Wrong path variables");
        }

        AmazonDynamoDB client = AmazonDynamoDBClientBuilder.standard().build();
        DynamoDB dynamoDB = new DynamoDB(client);

        Table table = dynamoDB.getTable(Properties.tableName);
        ItemCollection<ScanOutcome> scan = table.scan();
        if(scan.getAccumulatedItemCount() == 0) {
            logger.log("Scan is empty");
            return new APIGatewayProxyResponseEvent()
                    .withStatusCode(HttpStatusCode.NOT_FOUND)
                    .withBody("No elements");
        }

        logger.log("GOT SCAN FROM DYNAMODB: " + scan.toString());
        return new APIGatewayProxyResponseEvent()
                .withStatusCode(HttpStatusCode.OK)
                .withIsBase64Encoded(false)
                .withBody(scan.toString());
    }
}
