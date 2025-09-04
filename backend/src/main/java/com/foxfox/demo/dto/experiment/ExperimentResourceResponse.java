package com.foxfox.demo.dto.experiment;

import com.foxfox.demo.model.ExperimentResource;
import java.time.LocalDateTime;

public class ExperimentResourceResponse {
    private Integer id;
    private String name;
    private String type; // 枚举名
    private String url;
    private Integer size;
    private LocalDateTime uploadedAt;

    public static ExperimentResourceResponse from(ExperimentResource r){
        if(r==null) return null;
        ExperimentResourceResponse resp = new ExperimentResourceResponse();
        resp.id = r.getId();
        resp.name = r.getName();
        resp.type = r.getType()==null? null : r.getType().name();
        resp.url = r.getUrl();
        resp.size = r.getSize();
        resp.uploadedAt = r.getUploadedAt();
        return resp;
    }

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
    public String getUrl() { return url; }
    public void setUrl(String url) { this.url = url; }
    public Integer getSize() { return size; }
    public void setSize(Integer size) { this.size = size; }
    public LocalDateTime getUploadedAt() { return uploadedAt; }
    public void setUploadedAt(LocalDateTime uploadedAt) { this.uploadedAt = uploadedAt; }
}
