package io.spring.batch.restinvoker

import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
@EnableBatchProcessing
class RestInvokerApplication

fun main(args: Array<String>) {
	runApplication<RestInvokerApplication>(*args)
}
