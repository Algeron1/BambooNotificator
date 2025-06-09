package com.rgs.bamboonotifier.DTO;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

public class DeployResults {

    @JsonProperty("results")
    private List<DeployResult> deployResults;

    public List<DeployResult> getResults() {
        return deployResults;
    }
}
