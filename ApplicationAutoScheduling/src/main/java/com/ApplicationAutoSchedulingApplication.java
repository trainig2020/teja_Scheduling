package com;
import static java.nio.file.StandardWatchEventKinds.ENTRY_CREATE;
import static java.nio.file.StandardWatchEventKinds.ENTRY_DELETE;
import static java.nio.file.StandardWatchEventKinds.ENTRY_MODIFY;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;

import javax.sql.DataSource;

import org.springframework.batch.item.database.BeanPropertyItemSqlParameterSourceProvider;
import org.springframework.batch.item.database.JdbcBatchItemWriter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;


@SpringBootApplication
public class ApplicationAutoSchedulingApplication  {
	
	

	public static void main(String[] args) throws InterruptedException {
		SpringApplication.run(ApplicationAutoSchedulingApplication.class, args);
		try {
	        WatchService watcher = FileSystems.getDefault().newWatchService();
	        Path dir = Paths.get("C:\\Users\\Public\\Documents\\CSV\\");
	        dir.register(watcher, ENTRY_CREATE, ENTRY_DELETE, ENTRY_MODIFY);             
	        System.out.println("Watch Service registered for dir: " + dir.getFileName());
	        WatchKey key;
	        while ((key = watcher.take())!=null) 
	        {
	            for (WatchEvent<?> event : key.pollEvents()) {
	                 
	            	WatchEvent.Kind<?> kind = event.kind();
	                
	                @SuppressWarnings("unchecked")
	                WatchEvent<Path> ev = (WatchEvent<Path>) event;
	                Path fileName = ev.context();
	                
	                if(kind==ENTRY_CREATE)
	                {
	                	System.out.println("New File Added, file Name " + fileName);
	                	
	                	
	                }
					
	            }
	             
	            boolean valid = key.reset();
	            if (!valid) {
	                break;
	            }
	        }
	         
	    } catch (IOException ex) {
	        System.err.println(ex);
	    }

		
	}

}
	


