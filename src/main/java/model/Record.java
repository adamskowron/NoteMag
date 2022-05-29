package model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;

@JsonIgnoreProperties(ignoreUnknown = true)
@RequiredArgsConstructor
public class Record {

    @JsonProperty
    private String eventTime;

    @JsonProperty
    private String userIdentity;

    @JsonProperty
    private String object;

    @JsonProperty
    private String size;

}
