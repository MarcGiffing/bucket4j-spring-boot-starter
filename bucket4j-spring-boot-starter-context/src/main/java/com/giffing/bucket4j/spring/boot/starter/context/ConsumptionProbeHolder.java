package com.giffing.bucket4j.spring.boot.starter.context;

import java.util.concurrent.CompletableFuture;

import io.github.bucket4j.ConsumptionProbe;

public class ConsumptionProbeHolder {

	private ConsumptionProbe consumptionProbe;
	
	private CompletableFuture<ConsumptionProbe> consumptionProbeCompletableFuture;
	
	public ConsumptionProbeHolder(ConsumptionProbe consumptionProbe) {
		this.consumptionProbe = consumptionProbe;
	}
	
	public ConsumptionProbeHolder(CompletableFuture<ConsumptionProbe> consumptionProbeCompletableFuture) {
		this.consumptionProbeCompletableFuture = consumptionProbeCompletableFuture;
	}

	public ConsumptionProbe getConsumptionProbe() {
		return consumptionProbe;
	}

	public void setConsumptionProbe(ConsumptionProbe consumptionProbe) {
		this.consumptionProbe = consumptionProbe;
	}

	public CompletableFuture<ConsumptionProbe> getConsumptionProbeCompletableFuture() {
		return consumptionProbeCompletableFuture;
	}

	public void setConsumptionProbeCompletableFuture(
			CompletableFuture<ConsumptionProbe> consumptionProbeCompletableFuture) {
		this.consumptionProbeCompletableFuture = consumptionProbeCompletableFuture;
	}
	
	
	
}
