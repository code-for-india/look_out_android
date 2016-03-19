package com.cfi.lookout.loos;

import com.cfi.lookout.response.ApiResponse;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class GetLoosResponse extends ApiResponse {


    public List<Loo> getLoos() {
        return loos;
    }

    public void setLoos(List<Loo> loos) {
        this.loos = loos;
    }

    private List<Loo> loos;

}
