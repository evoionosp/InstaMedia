
package com.developer.devshubhpatel.instamedia.models;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class IMedia {

    @SerializedName("graphql")
    @Expose
    private Graphql graphql;

    public Graphql getGraphql() {
        return graphql;
    }

    public void setGraphql(Graphql graphql) {
        this.graphql = graphql;
    }

}
