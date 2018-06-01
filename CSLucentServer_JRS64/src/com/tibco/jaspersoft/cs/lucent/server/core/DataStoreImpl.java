package com.tibco.jaspersoft.cs.lucent.server.core;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.tibco.jaspersoft.cs.lucent.server.api.LogAggregate;
import com.tibco.jaspersoft.cs.lucent.server.api.LogEntry;
import com.tibco.jaspersoft.cs.lucent.server.api.LucentGlobalContext;
import com.tibco.jaspersoft.cs.lucent.server.logging.BasicLogAggImpl;

import java.util.concurrent.ConcurrentLinkedDeque;

/*
 * $Id: DataStoreImpl.java 282 2018-05-23 03:13:55Z jwhang $
 */
public class DataStoreImpl implements DataStore {

	//total list of aggregates are keyed by the test Id, then by a list of log aggregates.
	private Map<String, Map<String, LogAggregate>> recordEntries = new ConcurrentHashMap<String, Map<String, LogAggregate>>();
	
	private long maxLifeSpan = 60 * 60 * 1000; //1 hour
	private final int maxInterval = 100;
	private int intervalCounter = 0; //used to reduce frequency of checks as writing log entries may be a high volume set of operations.
	
	public void writeLogEntry(LogEntry entry) {
		//TODO:
		//at some interval, check to see if any stale entries exist and automatically remove them.  //TODO: scaffold code, implement long term solution.
		
		//check if any other entries of this type exist, if not create a new map and add an entry to the creation timestamp list.
		String testId = String.valueOf(LucentGlobalContext.getInstance().getFlowContext().getTestId());
		Map<String, LogAggregate> entries = recordEntries.get(testId);
		//if an entry list does exist for this test ID, add an additional log sample into the mix.
		if (entries==null){ //create a new entry.
			entries = new ConcurrentHashMap<String,LogAggregate>();
		} 
		recordEntries.put(testId, entries);
		String catLabel = entry.getEntryCategory().getLabel();
		LogAggregate catEntry = entries.get(catLabel);
		if (catEntry==null){
			catEntry = new BasicLogAggImpl(catLabel, entry.getStartTimeMs(), entry.getElapsedTimeNs());
		} else {
			catEntry.increment(entry.getElapsedTimeNs());
		}
		entries.put(catLabel, catEntry);
	}

	public Map<String, LogAggregate> readLogEntries(String testId) {
		Map<String, LogAggregate> entries = recordEntries.get(testId);
		if (entries!=null){
			return entries;
		} else { //this should never happen; log an exception.
			System.err.println("Missing log entry collection.");
		}
		return null;
	}
	
	//TODO: may need to remove this as all returned results should be aggregated values, not individual.
	public String summarizeLogEntries(String testId){
		return null;
	}

	//tests should retrieve results in relatively short order to minimize heap usage
	//and delete the information on the server.
	public void deleteLogEntries(String testId) {
		
	}
	
}



