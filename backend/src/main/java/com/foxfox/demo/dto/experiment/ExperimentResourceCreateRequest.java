package com.foxfox.demo.dto.experiment;

import com.foxfox.demo.model.ResourceType;
import jakarta.validation.constraints.*;

public class ExperimentResourceCreateRequest {

    @NotBlank
    private String name;

    @NotNull
    private ResourceType type;

    @NotBlank
    private String url;

    @Positive
    private Integer size;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public ResourceType getType() {
        return type;
    }

    public void setType(ResourceType type) {
        this.type = type;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public Integer getSize() {
        return size;
    }

    public void setSize(Integer size) {
        this.size = size;
    }
}