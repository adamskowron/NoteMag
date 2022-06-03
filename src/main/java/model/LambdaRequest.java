package model;

import com.fasterxml.jackson.annotation.*;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.extern.jackson.Jacksonized;

import java.util.*;

@ToString
@Builder
@Getter
@Setter
@Jacksonized
@JsonIgnoreProperties(ignoreUnknown = true)
public class LambdaRequest <T> {

    @JsonProperty
    private Map<String, ArrayList<String>> headers;

    @JsonProperty
    private boolean isBase64Encoded;

    @JsonProperty
    private T body;

//    @JsonProperty
//    private List<String> requestContext;
}
