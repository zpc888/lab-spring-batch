package io.spring.batch.restinvoker.job

import org.quartz.JobBuilder
import org.quartz.SimpleScheduleBuilder
import org.quartz.Trigger
import org.quartz.TriggerBuilder
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class QuartzConfiguration {
    @Bean
    fun quartzJobDetail() = JobBuilder.newJob(BatchScheduledJob::class.java).storeDurably().build();

    @Bean
    fun jobTrigger(): Trigger {
        val scheduleBuilder = SimpleScheduleBuilder.simpleSchedule()
                .withIntervalInSeconds(600).withRepeatCount(4)
        return TriggerBuilder.newTrigger().forJob(quartzJobDetail())
                .withSchedule(scheduleBuilder).build()
    }
}