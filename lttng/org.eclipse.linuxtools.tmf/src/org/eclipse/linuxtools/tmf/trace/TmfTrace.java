/*******************************************************************************
 * Copyright (c) 2009 Ericsson
 * 
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *   Francois Chouinard - Initial API and implementation
 *******************************************************************************/

package org.eclipse.linuxtools.tmf.trace;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Collections;
import java.util.Vector;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.linuxtools.tmf.event.TmfEvent;
import org.eclipse.linuxtools.tmf.event.TmfTimeRange;
import org.eclipse.linuxtools.tmf.event.TmfTimestamp;
import org.eclipse.linuxtools.tmf.signal.TmfSignalManager;

/**
 * <b><u>TmfEventStream</u></b>
 * <p>
 * TODO: Implement me. Please.
 */
public abstract class TmfTrace implements ITmfTrace {

    // ========================================================================
    // Constants
    // ========================================================================

    // The default number of events to cache
    public static final int DEFAULT_CACHE_SIZE = 1000;

    // ========================================================================
    // Attributes
    // ========================================================================

    // The stream name
    private final String fName;

    // The stream parser
    private final ITmfEventParser fParser;

    // The cache size
    private final int fCacheSize;

    // The set of event stream checkpoints (for random access)
    private Vector<TmfStreamCheckpoint> fCheckpoints = new Vector<TmfStreamCheckpoint>();

    // The number of events collected
    private int fNbEvents = 0;

    // The time span of the event stream
    private TmfTimeRange fTimeRange = new TmfTimeRange(TmfTimestamp.BigBang, TmfTimestamp.BigBang);

    // ========================================================================
    // Constructors
    // ========================================================================

    /**
     * @param filename
     * @param parser
     * @param cacheSize
     * @throws FileNotFoundException
     */
    protected TmfTrace(String filename, ITmfEventParser parser, int cacheSize) throws FileNotFoundException {
    	fName = filename;
        fParser = parser;
        fCacheSize = cacheSize;
    }

    /**
     * @param filename
     * @param parser
     * @throws FileNotFoundException
     */
    protected TmfTrace(String filename, ITmfEventParser parser) throws FileNotFoundException {
    	this(filename, parser, DEFAULT_CACHE_SIZE);
    }

    // ========================================================================
    // Accessors
    // ========================================================================

    /**
     * @return
     */
    public int getCacheSize() {
        return fCacheSize;
    }

    /**
     * @return
     */
    public String getName() {
        return fName;
    }

    /* (non-Javadoc)
     * @see org.eclipse.linuxtools.tmf.stream.ITmfEventStream#getNbEvents()
     */
    public int getNbEvents() {
        return fNbEvents;
    }

    /* (non-Javadoc)
     * @see org.eclipse.linuxtools.tmf.stream.ITmfEventStream#getTimeRange()
     */
    public TmfTimeRange getTimeRange() {
        return fTimeRange;
    }

    /* (non-Javadoc)
     * @see org.eclipse.linuxtools.tmf.stream.ITmfEventStream#getIndex(org.eclipse.linuxtools.tmf.event.TmfTimestamp)
     */
    public int getIndex(TmfTimestamp timestamp) {
    	StreamContext context = seekEvent(timestamp);
    	return context.index;
    }

    /* (non-Javadoc)
     * @see org.eclipse.linuxtools.tmf.stream.ITmfEventStream#getTimestamp(int)
     */
    public TmfTimestamp getTimestamp(int index) {
    	StreamContext context = seekEvent(index);
    	TmfEvent event = peekEvent(context);
    	return event.getTimestamp();
    }

    // ========================================================================
    // Operators
    // ========================================================================

    public StreamContext seekEvent(TmfTimestamp timestamp) {

        // First, find the right checkpoint
    	int index = Collections.binarySearch(fCheckpoints, new TmfStreamCheckpoint(timestamp, 0));

        // In the very likely event that the checkpoint was not found, bsearch
        // returns its negated would-be location (not an offset...). From that
        // index, we can then position the stream and get the event.
        if (index < 0) {
            index = Math.max(0, -(index + 2));
        }

        // Position the stream at the checkpoint
        Object location = (index < fCheckpoints.size()) ? fCheckpoints.elementAt(index).getLocation() : null;
        StreamContext nextEventContext;
        synchronized(this) {
        	nextEventContext = seekLocation(location);
        }
        StreamContext currentEventContext = new StreamContext(nextEventContext.location, index * fCacheSize);

        // And get the event
        TmfEvent event = getNextEvent(nextEventContext);
        while (event != null && event.getTimestamp().compareTo(timestamp, false) < 0) {
        	currentEventContext.location = nextEventContext.location;
        	currentEventContext.index++;
        	event = getNextEvent(nextEventContext);
        }

        return currentEventContext;
    }

    public StreamContext seekEvent(int position) {

        // Position the stream at the previous checkpoint
        int index = position / fCacheSize;
        Object location = (index < fCheckpoints.size()) ? fCheckpoints.elementAt(index).getLocation() : null;
        StreamContext nextEventContext;
        synchronized(this) {
        	nextEventContext = seekLocation(location);
        }
        StreamContext currentEventContext = new StreamContext(nextEventContext);

        // And locate the event (if it exists)
        int current = index * fCacheSize;
        TmfEvent event = getNextEvent(nextEventContext);
        while (event != null && current < position) {
        	currentEventContext.location = nextEventContext.location;
        	event = getNextEvent(nextEventContext);
            current++;
        }

        return currentEventContext;
    }

    public TmfEvent peekEvent(StreamContext context) {
    	StreamContext ctx = new StreamContext(context);
        return getNextEvent(ctx);
    }

    public TmfEvent getEvent(StreamContext context, TmfTimestamp timestamp) {

    	// Position the stream and update the context object
    	StreamContext ctx = seekEvent(timestamp);
    	context.location = ctx.location; 

        return getNextEvent(context);
    }

    public TmfEvent getEvent(StreamContext context, int position) {

    	// Position the stream and update the context object
    	StreamContext ctx = seekEvent(position);
    	context.location = ctx.location; 

        return getNextEvent(context);
    }

    public synchronized TmfEvent getNextEvent(StreamContext context) {
        try {
        	seekLocation(context.location);
        	TmfEvent event = fParser.getNextEvent(this);
        	context.location = getCurrentLocation();
        	return event;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

	private void notifyListeners(TmfTimeRange range) {
		TmfSignalManager.dispatchSignal(new TmfStreamUpdatedSignal(this, this, range));
	}
   
    /* (non-Javadoc)
     * @see org.eclipse.linuxtools.tmf.eventlog.ITmfEventStream#indexStream()
     */
    public void indexStream(boolean waitForCompletion) {
    	IndexingJob job = new IndexingJob(fName);
    	job.schedule();
        if (waitForCompletion) {
            try {
				job.join();
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
        }
    }

    private class IndexingJob extends Job {

		public IndexingJob(String name) {
			super(name);
		}

		/* (non-Javadoc)
		 * @see org.eclipse.core.runtime.jobs.Job#run(org.eclipse.core.runtime.IProgressMonitor)
		 */
		@Override
		protected IStatus run(IProgressMonitor monitor) {

            int nbEvents = 0;
            TmfTimestamp startTime = new TmfTimestamp();
            TmfTimestamp lastTime  = new TmfTimestamp();
            TmfTimestamp rangeStartTime = new TmfTimestamp();

            monitor.beginTask("Indexing " + fName, IProgressMonitor.UNKNOWN);

            try {
                StreamContext nextEventContext;
                synchronized(this) {
                	nextEventContext = seekLocation(null);
                }
                StreamContext currentEventContext = new StreamContext(nextEventContext);
                TmfEvent event = getNextEvent(nextEventContext);
                if (event != null) {
                	startTime = event.getTimestamp();
                	lastTime  = event.getTimestamp();
                }

                rangeStartTime = startTime;
                while (event != null) {
                    lastTime = event.getTimestamp();
                    if ((nbEvents++ % fCacheSize) == 0) {
                        TmfStreamCheckpoint bookmark = new TmfStreamCheckpoint(lastTime, currentEventContext.location);
                        synchronized(this) {
                        	fCheckpoints.add(bookmark);
                        	fNbEvents = nbEvents;
                            fTimeRange = new TmfTimeRange(startTime, lastTime);
                        }
                        notifyListeners(new TmfTimeRange(rangeStartTime, lastTime));
                        monitor.worked(1);
                        // Check monitor *after* fCheckpoints has been updated
                        if (monitor.isCanceled()) {
                        	return Status.CANCEL_STATUS;
                        }
                    }

                	currentEventContext.location = nextEventContext.location;
					event = getNextEvent(nextEventContext);
                }
            }
            finally {
                synchronized(this) {
                	fNbEvents = nbEvents;
                	fTimeRange = new TmfTimeRange(startTime, lastTime);
                }
                notifyListeners(new TmfTimeRange(rangeStartTime, lastTime));
                monitor.done();
            }

			return Status.OK_STATUS;
		}
    }

}
