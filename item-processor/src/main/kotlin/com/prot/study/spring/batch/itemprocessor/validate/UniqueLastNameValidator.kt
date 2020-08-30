package com.prot.study.spring.batch.itemprocessor.validate

import org.springframework.batch.item.ExecutionContext
import org.springframework.batch.item.ItemStreamSupport
import org.springframework.batch.item.validator.ValidationException
import org.springframework.batch.item.validator.Validator

class UniqueLastNameValidator(): ItemStreamSupport(), Validator<Customer> {
    private var lastNames = mutableSetOf<String>()
    @Throws(ValidationException::class)
    override fun validate(value: Customer) {
        if (lastNames.contains(value.lastName)) {
            throw ValidationException("Duplicate last name [${value.lastName}] was found")
        }
        this.lastNames.add(value.lastName!!)
    }

    override fun update(executionContext: ExecutionContext) {
        executionContext.put(getExecutionContextKey("lastNames"), lastNames)
    }

    override fun open(executionContext: ExecutionContext) {
        val lastNamesKey = getExecutionContextKey("lastNames")
        if (executionContext.containsKey(lastNamesKey)) {
            this.lastNames = executionContext.get(lastNamesKey) as MutableSet<String>
        }
    }
}