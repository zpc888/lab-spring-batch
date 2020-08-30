package com.prot.study.spring.batch.itemprocessor

import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication


@SpringBootApplication
@EnableBatchProcessing
class ItemProcessorApplication

fun main(args: Array<String>) {
	runApplication<ItemProcessorApplication>(*args)
}
