package com.prot.study.spring.batch.itemprocessor.validate

import org.springframework.batch.core.Job
import org.springframework.batch.core.Step
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory
import org.springframework.batch.core.configuration.annotation.StepScope
import org.springframework.batch.item.ItemWriter
import org.springframework.batch.item.data.MongoItemWriter
import org.springframework.batch.item.data.builder.MongoItemWriterBuilder
import org.springframework.batch.item.database.HibernateItemWriter
import org.springframework.batch.item.database.JpaItemWriter
import org.springframework.batch.item.database.builder.HibernateItemWriterBuilder
import org.springframework.batch.item.file.builder.FlatFileItemReaderBuilder
import org.springframework.batch.item.support.ScriptItemProcessor
import org.springframework.batch.item.validator.BeanValidatingItemProcessor
import org.springframework.batch.item.validator.ValidatingItemProcessor
import org.springframework.batch.item.xml.StaxEventItemWriter
import org.springframework.batch.item.xml.builder.StaxEventItemWriterBuilder
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.io.Resource
import org.springframework.data.mongodb.core.MongoOperations
import org.springframework.oxm.xstream.XStreamMarshaller

@Configuration
class ValidationAndStaxWriterJob {
    @Autowired
    var jobBuilderFactory: JobBuilderFactory? = null

    @Autowired
    var stepBuilderFactory: StepBuilderFactory? = null

    @Bean
    @StepScope
    fun customerItemReder(@Value("#{jobParameters['customerFile']}") inputFile: Resource?) =
            FlatFileItemReaderBuilder<Customer>().name("customerItemReader").delimited()
                    .names("firstName", "middleInitial", "lastName"
                            , "address", "city", "state", "zip")
                    .targetType(Customer::class.java)
                    .resource(inputFile!!)
                    .build()

    @Bean
    fun validator(): UniqueLastNameValidator {
        val ret = UniqueLastNameValidator()
        ret.setName("customValidator")
        return ret
    }

    @Bean
    @StepScope
    fun itemProcessor(@Value("#{jobParameters['script']}") script: Resource?):ScriptItemProcessor<Customer, Customer> {
        val ret = ScriptItemProcessor<Customer, Customer>()
        ret.setScript(script!!)
        return ret
    }

    @Bean
//    fun customerValidatingItemProcessor() = BeanValidatingItemProcessor<Customer>()
    fun customerValidatingItemProcessor() = ValidatingItemProcessor(validator())

//    @Bean
//    fun copyFileStep(): Step = stepBuilderFactory!!.get("copyFileStep")
//            .chunk<Customer, Customer>(5)
//            .reader(customerItemReder(null))
//            .processor(itemProcessor(null))
////            .processor(customerValidatingItemProcessor())
//            .writer(mongoItemWriter(null))
//            .stream(validator())
//            .build()

    @Bean
    @StepScope
    fun xmlCustomerWriter( @Value("#{jobParameters['outputFile']}") outputFile: Resource?): StaxEventItemWriter<Customer> {
        val aliases = mapOf("customer" to Customer::class.java)
        val marshaller = XStreamMarshaller()
        marshaller.setAliases(aliases)
        marshaller.afterPropertiesSet()
        return StaxEventItemWriterBuilder<Customer>().name("customerItemWriter")
                .resource(outputFile!!)
                .marshaller(marshaller)
                .rootTagName("customers")
                .build();
    }

    @Bean
    fun xmlFormatStep(): Step =
        stepBuilderFactory!!.get("xmlFormatStep")
                .chunk<Customer, Customer>(10)
                .reader(customerItemReder(null))
                .writer(xmlCustomerWriter(null))
                .build()

    @Bean
    fun xmlFormatJob(): Job = jobBuilderFactory!!.get("xmlFormatJob").start(xmlFormatStep()).build()

    @Bean
    fun mongoItemWriter(mongoTemplate: MongoOperations?): MongoItemWriter<Customer> {
        return MongoItemWriterBuilder<Customer>()
                .collection("customers")
                .template(mongoTemplate!!)
                .build();
    }

    @Bean
    fun mongoFormatStep(): Step = stepBuilderFactory!!.get("mongoFormatStep")
            .chunk<Customer, Customer>(10)
            .reader(customerItemReder(null))
            .writer(mongoItemWriter(null))
            .build()

    @Bean
    fun mongoFormatJob(): Job = jobBuilderFactory!!.get("mongoFormatJob")
            .start(mongoFormatStep()).build()
}