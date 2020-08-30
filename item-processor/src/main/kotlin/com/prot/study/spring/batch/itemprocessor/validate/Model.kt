package com.prot.study.spring.batch.itemprocessor.validate

import org.springframework.data.annotation.Id
import java.io.Serializable
import javax.validation.constraints.NotNull
import javax.validation.constraints.Pattern
import javax.validation.constraints.Size

data class Customer(
    @get:NotNull(message = "First name is required")
    @get:Pattern(regexp = "[a-zA-Z]+", message = "First name must be alphabetical")
    var firstName: String? = null
    ,
    @get:Size(min=1, max=1)
    @get:Pattern(regexp = "[a-zA-Z]", message = "Middle initial must be alphabetical")
    var middleInitial: String? = null
    ,
    @get:NotNull(message = "Last name is required")
    @get:Pattern(regexp = "[a-zA-Z]+", message = "Last name must be alphabetical")
    var lastName: String? = null
    ,
    @get:NotNull(message = "Address is required")
    @get:Pattern(regexp = "[0-9a-zA-Z\\. ]+", message = "Address must be alphanumeric, dot or space")
    var address: String? = null
    ,
    @get:NotNull(message = "City is required")
    @get:Pattern(regexp = "[a-zA-Z\\. ]+")
    var city: String? = null
    ,
    @get:NotNull(message = "State is required")
    @get:Size(min=2, max=2)
    @get:Pattern(regexp = "[A-Z]{2}")
    var state: String? = null
    ,
    @get:NotNull(message = "Zip is required")
    @get:Size(min=5, max=5)
    @get:Pattern(regexp = "\\d{5}")
    var zip: String? = null
): Serializable {

    @get:Id
    var id: String? = null
}
