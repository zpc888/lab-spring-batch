package io.spring.batch.restinvoker.job.stop

import org.springframework.batch.item.ItemProcessor
import org.springframework.jdbc.core.JdbcTemplate
import javax.sql.DataSource

interface TransactionDao {
    fun getTransactionsByAccountNumber(accountNumber: String): List<Transaction>
}

class TransactionDaoSupport(
        var ds: DataSource): JdbcTemplate(ds), TransactionDao {
    override fun getTransactionsByAccountNumber(accountNumber: String): List<Transaction> {
        val sql = """
            select t.id, t.timestamp, t.amount
            from transaction t inner join account_summary a on 
            a.id = t.account_summary_id
            where a.account_number = ?
        """
        return query(sql, arrayOf(accountNumber)) { rs, _ ->
            Transaction( accountNumber, rs.getTimestamp("timestamp"), rs.getDouble("amount"))
        }
    }
}

class TransactionApplierProcessor(val transactionDao: TransactionDao): ItemProcessor<AccountSummary, AccountSummary> {
    override fun process(summary: AccountSummary): AccountSummary? {
        val transactions = transactionDao.getTransactionsByAccountNumber(summary.accountNumber)
        transactions.forEach {
            summary.currentBalance += it.amount
        }
        return summary
    }
}


