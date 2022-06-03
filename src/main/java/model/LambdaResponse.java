package model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.ToString;

@Builder
@ToString
public class LambdaResponse <T> {

    @JsonProperty
    private int statusCode;

//    @JsonProperty
//    private Map<String,List<String>> headers;

    @JsonProperty
    private T body;
}
