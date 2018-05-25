package com.giffing.bucket4j.spring.boot.starter.context;

import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Consumer;
import java.util.function.Function;

import io.github.bucket4j.BucketListener;

/**
 * Marker Interface
 *
 */
public class MetricBucketListener implements BucketListener {

	private Consumer<Long> consumedFunction;
	private Consumer<Long> rejectedFunction;
	private AtomicLong consumed = new AtomicLong();
    private AtomicLong rejected = new AtomicLong();
    private AtomicLong parkedNanos = new AtomicLong();
    private AtomicLong delayedNanos = new AtomicLong();
    private AtomicLong interrupted = new AtomicLong();
	
    private String name;

    public MetricBucketListener(String name) {
		this.setName(name);

	}
    
    @Override
    public void onConsumed(long tokens) {
        consumed.addAndGet(tokens);
        consumedFunction.accept(tokens);
    }

    @Override
    public void onRejected(long tokens) {
        rejected.addAndGet(tokens);
        getRejectedFunction().accept(tokens);
    }

    @Override
    public void onParked(long nanos) {
        parkedNanos.addAndGet(nanos);
    }

    public long getConsumed() {
        return consumed.get();
    }

    public long getRejected() {
        return rejected.get();
    }

    public long getParkedNanos() {
        return parkedNanos.get();
    }

    public long getInterrupted() {
        return interrupted.get();
    }

	@Override
	public void onInterrupted(InterruptedException e) {
		interrupted.incrementAndGet();
	}

	@Override
	public void onDelayed(long nanos) {
		delayedNanos.addAndGet(nanos);
		
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Consumer<Long> getConsumedFunction() {
		return consumedFunction;
	}

	public void setConsumedFunction(Consumer<Long> consumedFunction) {
		this.consumedFunction = consumedFunction;
	}

	public Consumer<Long> getRejectedFunction() {
		return rejectedFunction;
	}

	public void setRejectedFunction(Consumer<Long> rejectedFunction) {
		this.rejectedFunction = rejectedFunction;
	}

}
