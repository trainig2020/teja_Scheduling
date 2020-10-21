
package com.pac;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import javax.sql.DataSource;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.item.database.BeanPropertyItemSqlParameterSourceProvider;
import org.springframework.batch.item.database.JdbcBatchItemWriter;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.MultiResourceItemReader;
import org.springframework.batch.item.file.mapping.BeanWrapperFieldSetMapper;
import org.springframework.batch.item.file.mapping.DefaultLineMapper;
import org.springframework.batch.item.file.transform.DelimitedLineTokenizer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;




@Configuration

@EnableBatchProcessing
public class Config {

	@Autowired
	public JobBuilderFactory jobBuilderFactory;

	@Autowired
	public StepBuilderFactory stepBuilderFactory;

	@Autowired
	public DataSource dataSource;

	
	/*
	 * @Value("classpath:*.csv") private Resource[] inputResources;
	 */

	@Bean
	public MultiResourceItemReader<Employee> multiResourceItemReader() throws Exception {
		MultiResourceItemReader<Employee> resourceItemReader = new MultiResourceItemReader<Employee>();

		 List<FileSystemResource> fileSystemResources = new ArrayList<>();
//      
       try {
           Stream<Path> stream = Files.list(Paths.get("C:\\Users\\Public\\Documents\\CSV\\"));
           stream.forEach(x -> {
               fileSystemResources.add(new FileSystemResource(x.toFile()));
           });
           Resource[] resources = {};
           resources = fileSystemResources.toArray(resources);
           
           resourceItemReader.setResources(resources);
   		resourceItemReader.setDelegate(reader());

         
       } catch (IOException e) {
           e.printStackTrace();
       }
      		
return resourceItemReader;
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })

	@Bean
	public FlatFileItemReader<Employee> reader() throws Exception {

		// Create reader instance
		FlatFileItemReader<Employee> reader = new FlatFileItemReader<Employee>();
		reader.setLinesToSkip(1);

		// Configure how each line will be parsed and mapped to different values
		reader.setLineMapper(new DefaultLineMapper() {
			{ // 3 columns in each row
				setLineTokenizer(new DelimitedLineTokenizer() {
					{
						setNames(new String[] { "empId", "empName", "mobile" });
					}
				}); // Set values in Employee class
				setFieldSetMapper(new BeanWrapperFieldSetMapper<Employee>() {
					{
						setTargetType(Employee.class);
					}
				});
			}
		});

		return reader;
	}

	@Bean
	public EmployeeProcessor processor() {
		return new EmployeeProcessor();

	}

	@Bean
	public JdbcBatchItemWriter<Employee> writer() {
		JdbcBatchItemWriter<Employee> writer = new JdbcBatchItemWriter<Employee>();
		writer.setItemSqlParameterSourceProvider(new BeanPropertyItemSqlParameterSourceProvider<>());
		writer.setSql("INSERT INTO employee  " + "VALUES (:empId,:empName,:mobile)");
		writer.setDataSource(dataSource);

		return writer;
	}

	@Bean
	public Step step1() throws Exception {
		return stepBuilderFactory.get("step1").<Employee, Employee>chunk(2).reader(multiResourceItemReader())
				.processor(processor()).writer(writer()).build();
	}

	@Bean
	public Job exportUserJob() throws Exception {
		return jobBuilderFactory.get("exportUserJob").incrementer(new RunIdIncrementer()).flow(step1()).end().build();
	}

	@Bean
	public ConsoleItemWriter<Employee> customWriter() {
		return new ConsoleItemWriter<Employee>();
	}

}
