package io.spring.batch.restinvoker.job.stop

import org.springframework.batch.core.ExitStatus
import org.springframework.batch.core.StepExecution
import org.springframework.batch.core.annotation.AfterStep
import org.springframework.batch.core.annotation.BeforeStep
import org.springframework.batch.item.ExecutionContext
import org.springframework.batch.item.ItemStreamReader
import org.springframework.batch.item.file.transform.FieldSet
import java.util.*

data class Transaction(val accountNumber: String, val timestamp: Date, val amount: Double) {
}

open class TransactionReader(val fieldSetReader: ItemStreamReader<FieldSet>): ItemStreamReader<Transaction> {
    var recordCount = 0
    var expectedRecordCount = 0

    @AfterStep
    fun afterStep(execution: StepExecution): ExitStatus
            = if (recordCount == expectedRecordCount) execution.exitStatus else ExitStatus.STOPPED

    override fun update(executionContext: ExecutionContext) {
        this.fieldSetReader.update(executionContext)
    }

    override fun open(executionContext: ExecutionContext) {
        fieldSetReader.open(executionContext)
    }

    override fun close() {
        this.fieldSetReader.close()
    }

    override fun read(): Transaction? {
        return process(fieldSetReader.read())
    }

    private fun process(fieldSet: FieldSet?): Transaction? {
        var result: Transaction? = null
        if (fieldSet != null) {
            if (fieldSet.fieldCount > 1) {
                result = Transaction(
                        fieldSet.readString(0)
                        , fieldSet.readDate(1, "yyyy-MM-DD HH:mm:ss")
                        , fieldSet.readDouble(2)
                )
                recordCount ++
                println("********************** actual record count = ${recordCount} ---- PROCESSING")
            } else {
                expectedRecordCount = fieldSet.readInt(0)
                println("********************** actual record count = ${recordCount}; expected = ${expectedRecordCount}")
            }
        }
        return result
    }
}

open class TransactionReaderWithTerminator(val fieldSetReader: ItemStreamReader<FieldSet>): ItemStreamReader<Transaction> {
    var stepExecution: StepExecution? = null
    var recordCount = 0

    override fun update(executionContext: ExecutionContext) {
        this.fieldSetReader.update(executionContext)
    }

    override fun open(executionContext: ExecutionContext) {
        fieldSetReader.open(executionContext)
    }

    override fun close() {
        this.fieldSetReader.close()
    }

    override fun read(): Transaction? {
        return process(fieldSetReader.read())
    }

    @BeforeStep
    fun beforeStep(execution: StepExecution) {
        this.stepExecution = execution
    }

    private fun process(fieldSet: FieldSet?): Transaction? {
        var result: Transaction? = null
        if (fieldSet != null) {
            if (fieldSet.fieldCount > 1) {
                result = Transaction(
                        fieldSet.readString(0)
                        , fieldSet.readDate(1, "yyyy-MM-DD HH:mm:ss")
                        , fieldSet.readDouble(2)
                )
                recordCount ++;
            } else {
                val expectedRecordCount = fieldSet.readInt(0)

                if (expectedRecordCount != this.recordCount) {
                    this.stepExecution!!.setTerminateOnly()
                }
            }
        }
        return result
    }
}
