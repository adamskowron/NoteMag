package model;

import com.fasterxml.jackson.annotation.*;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.List;

@ToString
@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown = true)
public class AddLambdaRequest {

//    @JsonProperty
//    private List<String> headers;

    @JsonProperty
    private boolean isBase64Encoded;

    @JsonProperty
    private byte[] body;

//    @JsonIgnore
//    private List<String> requestContext;
}
