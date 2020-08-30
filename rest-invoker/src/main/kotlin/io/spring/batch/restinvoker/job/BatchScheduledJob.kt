package io.spring.batch.restinvoker.job

import org.quartz.JobExecutionContext
import org.springframework.batch.core.Job
import org.springframework.batch.core.JobParametersBuilder
import org.springframework.batch.core.explore.JobExplorer
import org.springframework.batch.core.launch.JobLauncher
import org.springframework.scheduling.quartz.*

class BatchScheduledJob(val complexJob: Job
                        , val jobExplorer: JobExplorer
                        , val jobLauncher: JobLauncher
                        ): QuartzJobBean() {

    override fun executeInternal(context: JobExecutionContext) {
       val jobParameters = JobParametersBuilder(this.jobExplorer)
               .getNextJobParameters(complexJob)
               .toJobParameters()
        try {
            this.jobLauncher.run(complexJob, jobParameters)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}