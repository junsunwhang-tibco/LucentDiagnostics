package com.tibco.jaspersoft.cs.lucent.server.core;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

import com.tibco.jaspersoft.cs.lucent.server.api.EntryCategory;
import com.tibco.jaspersoft.cs.lucent.server.api.LucentGlobalContext;


import net.sf.jasperreports.engine.JRDataSource;

/*
 * $Id: JRDataSourceProxy.java 274 2018-05-04 06:33:50Z jwhang $
 */
public class JRDataSourceProxy implements InvocationHandler {

	JRDataSource jrd;
	
	public JRDataSourceProxy(JRDataSource jrd){
		this.jrd = jrd;
	}

	public Object invoke(Object obj, Method method, Object[] args) throws Throwable {
		try{
			if (method.getName().equalsIgnoreCase("getFieldValue")){
				long startTime = System.currentTimeMillis();
				long startTimeNs = System.nanoTime();
				Object retVal = method.invoke(jrd, args);
				long elapsedTime = System.nanoTime() - startTimeNs;
				LucentGlobalContext.getInstance().addEntry(EntryCategory.EC_ReportFill_RetrieveField, startTime, elapsedTime);
				return retVal;
			}
			
			if (method.getName().equalsIgnoreCase("next")){
				long startTime = System.currentTimeMillis();
				long startTimeNs = System.nanoTime();
				Object retVal = method.invoke(jrd, args);
				long elapsedTime = System.nanoTime() - startTimeNs;
				LucentGlobalContext.getInstance().addEntry(EntryCategory.EC_ReportFill_NextRow, startTime, elapsedTime);
				return retVal;
			}
			
		} catch (Exception e){
			e.printStackTrace();
		}
		//if no special action, invoke the nested class.
		return method.invoke(jrd, args);
	}
	
}