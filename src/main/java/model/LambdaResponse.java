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
public class LambdaResponse {

    @JsonProperty
    int statusCode;

//    @JsonProperty
//    private Map<String,List<String>> headers;

    @JsonProperty
    String body;
}
