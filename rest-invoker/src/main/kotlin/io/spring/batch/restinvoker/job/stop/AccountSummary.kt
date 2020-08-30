package io.spring.batch.restinvoker.job.stop

data class AccountSummary(val id: Int, val accountNumber: String, var currentBalance: Double) {
}