package com.cfi.lookout.issues;

import com.cfi.lookout.response.ApiResponse;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class GetIssuesResponse extends ApiResponse {


    public List<Issue> getIssues() {
        return issues;
    }

    public void setIssues(List<Issue> issues) {
        this.issues = issues;
    }

    private List<Issue> issues;

}
