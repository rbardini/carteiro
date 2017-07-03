package com.rbardini.carteiro.model

import java.io.Serializable
import java.util.*

data class ShipmentRecord(val date: Date, val status: String, val local: String?, val info: String?) : Serializable {
  companion object {
    private const val serialVersionUID: Long = 1L
  }

  constructor(date: Date, status: String) : this(date, status, null, null)

  fun getDescription() = status + if (info != null) ". " + info else ""
}
