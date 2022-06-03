package model;

import com.fasterxml.jackson.annotation.*;
import lombok.*;
import lombok.extern.jackson.Jacksonized;

import java.util.*;

@ToString
@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Jacksonized
@JsonIgnoreProperties(ignoreUnknown = true)
public class LambdaRequest <T> {

    @JsonProperty
    private Map<String, ArrayList<String>> headers;

    @JsonProperty
    private boolean isBase64Encoded;

    @JsonProperty("queryStringParameters")
    private Map<String, ArrayList<String>> pathParams;

    @JsonProperty
    private T body;
}
