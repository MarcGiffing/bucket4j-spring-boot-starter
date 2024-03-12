package com.giffing.bucket4j.spring.boot.starter.context.properties;

import com.giffing.bucket4j.spring.boot.starter.context.ExecutePredicateDefinition;
import com.giffing.bucket4j.spring.boot.starter.context.constraintvalidations.ValidBandWidthIds;
import io.github.bucket4j.TokensInheritanceStrategy;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@Data
@ValidBandWidthIds
public class RateLimit implements Serializable {

    /**
     * SpEl condition to check if the rate limit should be executed. If null there is no check.
     */
    private String executeCondition;

    /**
     * If you provide a post execution condition. The incoming check only estimates the
     * token consumption. It will not consume a token. This check is based on the response
     * to decide if the token should be consumed or not.
     *
     *
     */
    private String postExecuteCondition;

    @Valid
    private List<ExecutePredicateDefinition> executePredicates = new ArrayList<>();

    /**
     * SpEl condition to check if the rate-limit should apply. If null there is no check.
     */
    private String skipCondition;

    @Valid
    private List<ExecutePredicateDefinition> skipPredicates = new ArrayList<>();

    /**
     * SPEL expression to dynamic evaluate filter key
     */
    @NotBlank
    private String cacheKey = "1";

    /**
     * The number of tokens that should be consumed
     */
    @NotNull
    @Min(1)
    private Integer numTokens = 1;

    @NotEmpty
    @Valid
    private List<BandWidth> bandwidths = new ArrayList<>();

    /**
     * The token inheritance strategy to use when replacing the configuration of a bucket
     */
    @NotNull
    private TokensInheritanceStrategy tokensInheritanceStrategy = TokensInheritanceStrategy.RESET;

    public RateLimit copy() {
        var copy = new RateLimit();
        copy.setExecuteCondition(this.executeCondition);
        copy.setPostExecuteCondition(this.postExecuteCondition);
        copy.setExecutePredicates(this.executePredicates);
        copy.setSkipCondition(this.skipCondition);
        copy.setSkipPredicates(this.skipPredicates);
        copy.setCacheKey(this.cacheKey);
        copy.setNumTokens(this.numTokens);
        copy.setBandwidths(this.bandwidths);
        copy.setTokensInheritanceStrategy(this.tokensInheritanceStrategy);
        return copy;
    }

    public void consumeNotNullValues(RateLimit toConsume) {
        if(toConsume == null) {
            return;
        }

        if (toConsume.getExecuteCondition() != null && !toConsume.getExecuteCondition().isEmpty()) {
            this.setExecuteCondition(toConsume.getExecuteCondition());
        }
        if (toConsume.getPostExecuteCondition() != null && !toConsume.getPostExecuteCondition().isEmpty()) {
            this.setPostExecuteCondition(toConsume.getPostExecuteCondition());
        }
        if (toConsume.getExecutePredicates() != null && !toConsume.getExecutePredicates().isEmpty()) {
            this.setExecutePredicates(toConsume.getExecutePredicates());
        }
        if (toConsume.getSkipCondition() != null && !toConsume.getSkipCondition().isEmpty()) {
            this.setSkipCondition(toConsume.getSkipCondition());
        }
        if (toConsume.getSkipPredicates() != null && !toConsume.getSkipPredicates().isEmpty()) {
            this.setSkipPredicates(toConsume.getSkipPredicates());
        }
        if (toConsume.getCacheKey() != null && !toConsume.getCacheKey().equals("1") && !toConsume.getCacheKey().isEmpty()) {
            this.setCacheKey(toConsume.getCacheKey());
        }
        if(toConsume.getNumTokens() != null && toConsume.getNumTokens() != 1) {
            this.setNumTokens(toConsume.getNumTokens());
        }
        if(toConsume.getBandwidths() != null && !toConsume.getBandwidths().isEmpty()) {
            this.setBandwidths(toConsume.getBandwidths());
        }
        if(toConsume.getTokensInheritanceStrategy() != null) {
            this.setTokensInheritanceStrategy(toConsume.getTokensInheritanceStrategy());
        }
    }

}