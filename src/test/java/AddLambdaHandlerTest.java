//import com.amazonaws.services.lambda.runtime.Context;
//import com.amazonaws.services.lambda.runtime.LambdaLogger;
//
//import java.io.IOException;
//import java.io.InputStream;
//import java.io.OutputStream;
//import java.util.concurrent.CompletableFuture;
//
//import model.LambdaRequest;
//import org.junit.Before;
//import org.junit.Test;
//import org.junit.jupiter.api.Disabled;
//import org.mockito.junit.MockitoJUnitRunner;
//import org.mockito.Mock;
//import org.junit.runner.RunWith;
//import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
//import software.amazon.awssdk.core.async.AsyncRequestBody;
//import software.amazon.awssdk.regions.Region;
//import software.amazon.awssdk.services.s3.S3AsyncClient;
//import software.amazon.awssdk.services.s3.model.PutObjectRequest;
//import software.amazon.awssdk.services.s3.model.PutObjectResponse;
//
//import static org.mockito.Mockito.when;
//import static org.mockito.Mockito.doAnswer;
//import static org.mockito.ArgumentMatchers.anyString;
//
//@RunWith(MockitoJUnitRunner.class)
//public class AddLambdaHandlerTest {
//
//    private AddLambdaHandler handler;
//
////    @Mock
////    Context context;
////
////    @Mock
////    LambdaLogger loggerMock;
////
////    @Before
////    public void setUp() throws Exception {
////        when(context.getLogger()).thenReturn(loggerMock);
////
////        doAnswer(call -> {
////            System.out.println((String)call.getArgument(0));//print to the console
////            return null;
////        }).when(loggerMock).log(anyString());
////
////        handler = new AddLambdaHandler();
////    }
//
//    @Test
//    public void shouldUploadToS3() {
////        //given
////        InputStream inputStream = null;
////        OutputStream outputStream = null;
////
////        try {
////            handler.handleRequest(inputStream, outputStream, context);
////        } catch (IOException e) {
////            e.printStackTrace();
////        }
//
////
////        S3AsyncClient s3AsyncClient = S3AsyncClient.builder()
////                .region(Region.EU_WEST_1)
////                .credentialsProvider(DefaultCredentialsProvider.create())
////                .build();
////
////        LambdaRequest lambdaRequest = LambdaRequest.builder()
////                .body(hexStringToByteArray("12312341232132"))
////                .build();
////
////        CompletableFuture<PutObjectResponse> future =
////                s3AsyncClient.putObject(PutObjectRequest.builder()
////                                .bucket("images-s3")
////                                .key("USER1FOLDER/imgNAME1")
////                           //     .contentType(contentType)
////                                .build(),
////                        AsyncRequestBody.fromBytes(lambdaRequest.getBody()));
//
//    }
//
//    public static byte[] hexStringToByteArray(String s) {
//        int len = s.length();
//        byte[] data = new byte[len / 2];
//        for (int i = 0; i < len; i += 2) {
//            data[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4)
//                    + Character.digit(s.charAt(i+1), 16));
//        }
//        return data;
//    }
//}
