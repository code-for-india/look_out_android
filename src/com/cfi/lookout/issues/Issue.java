package com.cfi.lookout.issues;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Issue {

    public Issue()
    {
        issueType = "";
        comment = "";
        state = "";
        source = "";
        gender = "";
        pictureUrl = "";
        userId = "";
    }

    @JsonProperty("id")
    private Integer id;

    @JsonProperty("loo_id")
    private Integer looId;

    @JsonProperty("user_id")
    private String userId;

    @JsonProperty("issue_type")
    private String issueType;

    @JsonProperty("comment")
    private String comment;

    @JsonProperty("state")
    private String state;

    @JsonProperty("source")
    private String source;

    @JsonProperty("gender")
    private String gender;

    @JsonProperty("picture_url")
    private String pictureUrl;


    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getLooId() {
        return looId;
    }

    public void setLooId(Integer looId) {
        this.looId = looId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getIssueType() {
        return issueType;
    }

    public void setIssueType(String issueType) {
        this.issueType = issueType;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public String getPictureUrl() {
        return pictureUrl;
    }

    public void setPictureUrl(String pictureUrl) {
        this.pictureUrl = pictureUrl;
    }
}
