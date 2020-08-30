package io.spring.batch.helloworld;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@EnableBatchProcessing
@SpringBootApplication
public class HelloWorldApplication {
	@Autowired
	private JobBuilderFactory jobBuilderFactory;
	@Autowired
	private StepBuilderFactory stepBuilderFactory;
	@Autowired
	private AccountService accountService;

	public static void main(String[] args) {
		System.out.println(Thread.currentThread().getName() + " >> main");
		SpringApplication.run(HelloWorldApplication.class, args);
		System.out.println(Thread.currentThread().getName() + " << main");
	}

	/*
	@Bean
	public Step step() {
		System.out.println(Thread.currentThread().getName() + " >> step");
		Step step = this.stepBuilderFactory.get("step-1")
				.tasklet(new Tasklet() {
					@Override
					public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) throws Exception {
//						System.out.println("Hello World!!!");
						Object name = chunkContext.getStepContext().getJobParameters().get("name");
						System.out.printf("Hello %s!!!%n", name);
						System.out.println(Thread.currentThread().getName() + " >> execute step");
						accountService.transfer();
						System.out.println(Thread.currentThread().getName() + " << execute step");
						return RepeatStatus.FINISHED;
					}
				}).build();
		System.out.println(Thread.currentThread().getName() + " << step");
		return step;
	}
	 */

	@Bean
	public Step step1() {
		return this.stepBuilderFactory.get("step1").tasklet( helloWorldTasklet(null) ).build();
	}

	@StepScope
	@Bean
	public Tasklet helloWorldTasklet(@Value("#{jobParameters['name']}") String name) {
		return (contribution, chunkContext) -> {
			System.out.printf("HELLO, ..... %s!!!%n", name);
			return RepeatStatus.FINISHED;
		};
	}

	@Bean
	public Job job() {
		return this.jobBuilderFactory.get("job-4").start(step1()).build();
	}

}
