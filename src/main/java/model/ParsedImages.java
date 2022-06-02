package model;

import lombok.Builder;

import java.util.List;

@Builder
public class ParsedImages {
    private String userId;
    private String imageName;
    private List<String> lines;
    private String S3Key;
}
