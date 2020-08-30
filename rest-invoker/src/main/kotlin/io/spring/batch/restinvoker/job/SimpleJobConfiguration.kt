package io.spring.batch.restinvoker.job

import org.springframework.batch.core.Job
import org.springframework.batch.core.Step
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory
import org.springframework.batch.core.launch.support.RunIdIncrementer
import org.springframework.batch.repeat.RepeatStatus
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class SimpleJobConfiguration {
    @Autowired
    lateinit var jobBuilderFactory: JobBuilderFactory

    @Autowired
    lateinit var stepBuilderFactory: StepBuilderFactory

    @Bean
    fun simpleJob(): Job = this.jobBuilderFactory.get("simple-job").start(step()).build()

    @Bean
    fun complexJob(): Job = this.jobBuilderFactory.get("complex-job").incrementer(RunIdIncrementer()).start(step()).build()

    @Bean
    fun step(): Step = this.stepBuilderFactory.get("step01").tasklet { _, _ ->
        println("Step01 ran today!")
        RepeatStatus.FINISHED
    }.build();
}