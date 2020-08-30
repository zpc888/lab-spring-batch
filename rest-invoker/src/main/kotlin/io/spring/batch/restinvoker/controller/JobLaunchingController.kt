package io.spring.batch.restinvoker.controller

import org.springframework.batch.core.ExitStatus
import org.springframework.batch.core.Job
import org.springframework.batch.core.JobParameters
import org.springframework.batch.core.JobParametersBuilder
import org.springframework.batch.core.explore.JobExplorer
import org.springframework.batch.core.launch.JobLauncher
import org.springframework.context.ApplicationContext
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.ResponseBody
import org.springframework.web.bind.annotation.RestController
import reactor.core.publisher.Mono
import java.util.*

@RestController
class JobLaunchingController(val jobLauncher: JobLauncher, val context: ApplicationContext, val jobExplorer: JobExplorer) {

    @PostMapping(path = ["/run"], consumes = [MediaType.APPLICATION_JSON_VALUE], produces = [MediaType.APPLICATION_STREAM_JSON_VALUE])
    @ResponseBody fun runJob(@RequestBody request: JobLaunchRequest): Mono<ExitStatus> {
        val job: Job = context.getBean(request.name, Job::class.java)
        val jobParameters = if (job.name.startsWith("simple"))
                request.getJobParameters()
            else JobParametersBuilder(request.getJobParameters(), jobExplorer).getNextJobParameters(job).toJobParameters()
        return Mono.just( jobLauncher.run(job, jobParameters).exitStatus )
    }
}

data class JobLaunchRequest(val name: String, var jobParams: Properties) {
    fun getJobParameters(): JobParameters {
        val prop = Properties()
            prop.putAll(jobParams)
        return JobParametersBuilder(prop).toJobParameters()
    }
}
