/**
 * 
 */
package com.twinsoft.convertigo.engine;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletionService;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.ws.commons.schema.XmlSchema;

import com.twinsoft.convertigo.engine.SchemaManager.Option;

public class XmlSchemaBuilderExecutor {
	
	private Set<XmlSchemaBuilder> builders = new HashSet<XmlSchemaBuilder>();
	XmlSchemaBuilder mainBuilder = null;
	
	protected XmlSchemaBuilderExecutor() {
		
	}
	
	protected XmlSchemaBuilder getMainBuilder() {
		return this.mainBuilder;
	}
	
	protected XmlSchema buildSchema(String projectName, Option... options) throws EngineException {
		if (projectName == null || projectName.isEmpty()) {
			throw new EngineException("Incorrect project: "+ projectName +" is null or empty");
		}
		if (!Engine.theApp.databaseObjectsManager.existsProject(projectName)) {
			throw new EngineException("Incorrect project: "+ projectName +" does not exist");
		}
		
		try {
			long timeStart = System.currentTimeMillis();
			
			builders.clear();
			
			boolean forFull = Option.fullSchema.is(options);
			
			List<XmlSchemaBuilder> list = new ArrayList<XmlSchemaBuilder>();
			List<String> refs = new ArrayList<String>();
			SchemaManager.getProjectReferences(refs, projectName);
			for (String ref: refs) {
				if (projectName.equals(ref)) {
					list.add(0, createBuilder(ref, forFull));
					list.add(1, createBuilder(ref, !forFull));
				} else {
					list.add(createBuilder(ref, false));
				}
			}
			
			for (XmlSchemaBuilder builder: list) {
				if (mainBuilder == null) {
					mainBuilder = builder;
				}
				if (!builders.add(builder)) {
					Engine.logEngine.warn("For project \""+ projectName +"\" could not add builder "+ builder.toString());
				}
			}
			
			int size = builders.size();
		    ExecutorService executor = Executors.newFixedThreadPool(size);
		    CompletionService<String> completion = new ExecutorCompletionService<String>(executor);
		    for (final XmlSchemaBuilder builder: builders) {
		    	completion.submit(new Callable<String>() {
					@Override
					public String call() throws Exception {
						builder.beginBuildSchema(XmlSchemaBuilderExecutor.this);
						return builder.toString();
					}
				});
		    }
		    for (int i = 0; i < size; i++) {
		    	try {
		    		completion.take().get();
		    	} catch (Exception e) {
		    		throw e;
		    	} finally {
		    		executor.shutdown();
		    	}
		    }
		    executor.shutdown();
		    
			for (XmlSchemaBuilder builder: builders) {
				builder.postBuildSchema();
			}
			
			for (XmlSchemaBuilder builder: builders) {
				builder.endBuildSchema();
				
//				try {
//					if (builder.equals(mainBuilder) || (!builder.equals(mainBuilder) && !builder.getProjectName().equals(projectName))) {
//						System.out.println(XMLUtils.prettyPrintDOM(builder.getXmlSchema().getSchemaDocument()));
//					}
//				} catch (Exception e) {
//					e.printStackTrace();
//				}
			}
			
			long timeStop = System.currentTimeMillis();
			System.out.println((mainBuilder.isFull ? "Full ": "") +
					"Schema for project \"" + projectName + "\" | Times >> total : " + (timeStop - timeStart) + " ms");
		
			return mainBuilder.getXmlSchema();
			
		} catch (Exception e) {
			throw new EngineException("Error occured while building schema", e);
		} finally {
			builders.clear();
		}
	}
	
	protected XmlSchemaBuilder createBuilder(String projectName, boolean forFull) {
		return forFull ? new XmlSchemaBuilder("").new XmlSchemaFullBuilder(projectName) : new XmlSchemaBuilder(projectName);
	}
	
	protected XmlSchemaBuilder getBuilder(String projectName, boolean forFull) {
		synchronized (builders) {
			for (XmlSchemaBuilder builder: builders) {
				if (builder.getProjectName().equals(projectName) && builder.isFull == forFull) {
					return builder;
				}
			}
			return null;
		}
	}
	
	protected XmlSchemaBuilder getBuilderByTargetNamespace(String targetNamespace) {
		synchronized (builders) {
			for (XmlSchemaBuilder builder: builders) {
				if (builder.getTargetNamespace().equals(targetNamespace)) {
					return builder;
				}
			}
			return null;
		}
	}
}