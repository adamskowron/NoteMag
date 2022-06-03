package model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

@Builder
@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class LambdaResponse <T> {

    @JsonProperty
    private int statusCode;

    @JsonProperty
    private T body;
}
