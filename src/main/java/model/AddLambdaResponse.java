package model;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSetter;
import lombok.Builder;
import lombok.ToString;
import software.amazon.awssdk.http.HttpStatusCode;

import java.util.List;
import java.util.Map;

@Builder
@ToString
public class AddLambdaResponse {

    @JsonProperty
    int statusCode;

//    @JsonProperty
//    Map<String, String> headers;

    @JsonProperty
    String body;
}
