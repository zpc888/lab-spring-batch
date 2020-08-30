package io.spring.batch.xml

import org.springframework.batch.item.ExecutionContext
import org.springframework.batch.item.xml.builder.StaxEventItemReaderBuilder
import org.springframework.core.io.FileSystemResource
import org.springframework.oxm.jaxb.Jaxb2Marshaller
import java.text.SimpleDateFormat
import java.util.*
import javax.xml.bind.annotation.XmlElement
import javax.xml.bind.annotation.XmlElementWrapper
import javax.xml.bind.annotation.XmlRootElement
import javax.xml.bind.annotation.XmlType
import javax.xml.bind.annotation.adapters.XmlAdapter
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter

class DateTimeAdapter(): XmlAdapter<String, Date>() {
    val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")

    override fun marshal(v: Date?): String {
        return dateFormat.format(v!!)
    }

    override fun unmarshal(v: String?): Date {
        return dateFormat.parse(v!!)
    }
}

@XmlType(name="transaction")
data class Transaction(var accountNumber: String? = null,
                       var amount: Double? = null) {
    var transactionDate: Date? = null
    @XmlJavaTypeAdapter(DateTimeAdapter::class)
    set(value) {field = value}
}

@XmlRootElement
data class Customer(var firstName: String? = null, var middleInitial: String? = null
                    , var lastName: String? = null, var address: String? = null
                    , var city: String? = null, var state: String? = null
                    , var zipCode: String? = null) {
    var transactions: List<Transaction>? = null
        @XmlElementWrapper(name="transactions")
        @XmlElement(name="transaction")
    set(value) { field = value}

    override fun toString() = "$firstName $middleInitial. $lastName has ${if (transactions == null) 0 else transactions!!.size } transactions."
}

fun main(args: Array<String>) {
    val marshaller = Jaxb2Marshaller()
     marshaller.setClassesToBeBound(Customer::class.java, Transaction::class.java)

    val resource = FileSystemResource("customer.xml")
    println("resource = $resource")
    val reader = StaxEventItemReaderBuilder<Customer>().name("t1")
            .resource(resource)
            .addFragmentRootElements("customer")
            .unmarshaller(marshaller)
            .build()
    reader.open(ExecutionContext())
    var customer1 = reader.read()
    println(customer1)
    var customer2 = reader.read()
    println(customer2)
    var customer3 = reader.read()
    println(customer3)
    reader.close()
}