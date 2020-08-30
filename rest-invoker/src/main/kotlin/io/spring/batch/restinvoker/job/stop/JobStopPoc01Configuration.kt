package io.spring.batch.restinvoker.job.stop

import org.springframework.batch.core.Job
import org.springframework.batch.core.Step
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory
import org.springframework.batch.core.configuration.annotation.StepScope
import org.springframework.batch.item.database.BeanPropertyItemSqlParameterSourceProvider
import org.springframework.batch.item.database.JdbcCursorItemReader
import org.springframework.batch.item.database.builder.JdbcBatchItemWriterBuilder
import org.springframework.batch.item.database.builder.JdbcCursorItemReaderBuilder
import org.springframework.batch.item.file.FlatFileItemWriter
import org.springframework.batch.item.file.builder.FlatFileItemReaderBuilder
import org.springframework.batch.item.file.builder.FlatFileItemWriterBuilder
import org.springframework.batch.item.file.mapping.PassThroughFieldSetMapper
import org.springframework.batch.item.file.transform.BeanWrapperFieldExtractor
import org.springframework.batch.item.file.transform.DelimitedLineAggregator
import org.springframework.batch.item.file.transform.DelimitedLineTokenizer
import org.springframework.batch.item.file.transform.FieldSet
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.io.FileSystemResourceLoader
import javax.sql.DataSource

@Configuration
class JobStopPoc01Configuration {
    @Autowired
    lateinit var jobBuilderFactory: JobBuilderFactory

    @Autowired
    lateinit var stepBuilderFactory: StepBuilderFactory

    @Bean
    @StepScope
    fun fileItemReader(@Value("#{jobParameters['transactionFile']}") inputFileName: String?)
        = FlatFileItemReaderBuilder<FieldSet>()
            .name("transactionFileItemReader")
            .resource(FileSystemResourceLoader().getResource(inputFileName!!))
            .lineTokenizer(DelimitedLineTokenizer())
            .fieldSetMapper(PassThroughFieldSetMapper())
            .build();

    @Bean
    @StepScope
    fun transactionReader(): TransactionReader = TransactionReader(fileItemReader(null))

    @Bean
    @StepScope
    fun transactionReaderWithTerminate(): TransactionReaderWithTerminator = TransactionReaderWithTerminator(fileItemReader(null))

    @Bean
    fun transactionWriter(dataSource: DataSource?)
        = JdbcBatchItemWriterBuilder<Transaction>()
            .itemSqlParameterSourceProvider( BeanPropertyItemSqlParameterSourceProvider() )
            .sql("""
 INSERT INTO TRANSACTION ( ACCOUNT_SUMMARY_ID, TIMESTAMP, AMOUNT) 
 VALUES ((SELECT ID FROM ACCOUNT_SUMMARY WHERE ACCOUNT_NUMBER = :accountNumber), :timestamp, :amount)
            """).dataSource(dataSource!!)
            .build()

    @Bean
    fun importTransactionFileStep(): Step
        = this.stepBuilderFactory.get("importTransactionFileStep")
            .chunk<Transaction, Transaction>(100)
            .reader(transactionReader())
            .writer(transactionWriter(null))
            .allowStartIfComplete(true)
            .listener(transactionReader())
            .build()

    @Bean
    fun importTransactionFileStepWithTerminate(): Step
            = this.stepBuilderFactory.get("importTransactionFileStep")
            .chunk<Transaction, Transaction>(100)
            .reader(transactionReaderWithTerminate())
            .writer(transactionWriter(null))
            .allowStartIfComplete(true)
            .listener(transactionReaderWithTerminate())
            .build()

    @Bean
    @StepScope
    fun accountSummaryReader(dataSource: DataSource?): JdbcCursorItemReader<AccountSummary>
        = JdbcCursorItemReaderBuilder<AccountSummary>()
            .name("accountSummaryReader")
            .dataSource(dataSource!!)
            .sql("""
SELECT ID, ACCOUNT_NUMBER, CURRENT_BALANCE FROM ACCOUNT_SUMMARY A 
WHERE A.ID IN (SELECT DISTINCT T.ACCOUNT_SUMMARY_ID FROM TRANSACTION T) 
ORDER BY A.ACCOUNT_NUMBER
            """).rowMapper { rs, _ ->
                AccountSummary( rs.getInt("ID"), rs.getString("ACCOUNT_NUMBER"), rs.getDouble("CURRENT_BALANCE"))
            }.build()

    @Bean
    fun transactionDao(dataSource: DataSource?) = TransactionDaoSupport(dataSource!!)

    @Bean
    fun transactionApplierProcessor(): TransactionApplierProcessor = TransactionApplierProcessor(transactionDao(null))

    @Bean
    fun accountSummaryWriter(dataSource: DataSource?)
        = JdbcBatchItemWriterBuilder<AccountSummary>()
            .dataSource(dataSource!!)
            .itemSqlParameterSourceProvider( BeanPropertyItemSqlParameterSourceProvider() )
            .sql("UPDATE ACCOUNT_SUMMARY SET CURRENT_BALANCE = :currentBalance WHERE ACCOUNT_NUMBER = :accountNumber")
            .build()

    @Bean
    fun applyTransactionsStep(): Step
        = this.stepBuilderFactory.get("applyTransactionsStep")
            .chunk<AccountSummary, AccountSummary>(100)
            .reader(accountSummaryReader(null))
            .processor(transactionApplierProcessor())
            .writer(accountSummaryWriter(null))
            .build()

    @Bean
    @StepScope
    fun accountSummaryFileWriter(@Value("#{jobParameters['summaryFile']}") summaryFile: String?): FlatFileItemWriter<AccountSummary> {
        val lineAggregator = DelimitedLineAggregator<AccountSummary>()
        val fieldExtractor = BeanWrapperFieldExtractor<AccountSummary>()
        fieldExtractor.setNames(arrayOf("accountNumber", "currentBalance"))
        fieldExtractor.afterPropertiesSet()
        lineAggregator.setFieldExtractor(fieldExtractor)
        return FlatFileItemWriterBuilder<AccountSummary>()
                .name("accountSummaryFileWriter")
                .resource( FileSystemResourceLoader().getResource(summaryFile!!))
                .lineAggregator(lineAggregator)
                .build()
    }

    @Bean
    fun generateAccountSummaryStep(): Step
            = this.stepBuilderFactory.get("generateAccountSummaryStep")
            .chunk<AccountSummary, AccountSummary>(100)
            .reader(accountSummaryReader(null))
            .writer(accountSummaryFileWriter(null))
            .build()

//    @Bean
//    fun transactionJob(): Job
//            = this.jobBuilderFactory.get("transactionJob")
//            .start(importTransactionFileStep())
//            .on("STOPPED").stopAndRestart(importTransactionFileStep())
//            .from(importTransactionFileStep()).on("*").to(applyTransactionsStep())
//            .from(applyTransactionsStep()).next(generateAccountSummaryStep())
//            .end()
//            .build()

    @Bean
    fun transactionJob(): Job
            = this.jobBuilderFactory.get("transactionJob")
            .start(importTransactionFileStepWithTerminate())
            .next(applyTransactionsStep())
            .next(generateAccountSummaryStep())
            .build()
}

