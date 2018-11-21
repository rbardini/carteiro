package com.rbardini.carteiro.model

import android.content.Context
import com.rbardini.carteiro.db.DatabaseHelper
import com.rbardini.carteiro.util.MobileTracker
import com.rbardini.carteiro.util.PostalUtils
import java.io.Serializable
import java.util.Locale

data class Shipment @JvmOverloads constructor(
  val number: String,
  var name: String? = null,
  var isFavorite: Boolean = false,
  var isArchived: Boolean = false,
  var isUnread: Boolean = false
) : Serializable {
  companion object {
    private const val serialVersionUID: Long = 1L
  }

  val records = mutableListOf<ShipmentRecord>()

  fun toggleFavorite() { isFavorite = !isFavorite }
  fun toggleArchived() { isArchived = !isArchived }
  fun toggleUnread() { isUnread = !isUnread }

  fun getDescription() = if (name != null) name else number

  fun saveTo(dh: DatabaseHelper) {
    dh.beginTransaction()
    if (dh.insertShipment(this)) dh.setTransactionSuccessful()
    dh.endTransaction()
  }

  fun fetchRecords(context: Context) = MobileTracker.deepTrack(this, context)

  fun loadRecords(dh: DatabaseHelper) = replaceRecords(dh.getPostalRecords(number))

  fun addRecord(newRecord: ShipmentRecord) = records.add(newRecord)

  fun addRecords(newRecords: List<ShipmentRecord>) = records.addAll(newRecords)

  fun replaceRecords(newRecords: List<ShipmentRecord>) {
    records.clear()
    records.addAll(newRecords)
  }

  fun getRecord(index: Int) = records[index]

  fun getFirstRecord() = records.firstOrNull()

  fun getLastRecord() = records.lastOrNull()

  fun size() = records.size

  fun isEmpty() = records.isEmpty()

  fun isNotEmpty() = records.isNotEmpty()

  fun clear() = records.clear()

  fun getService() = PostalUtils.Service.getService(number)

  fun getFlag(context: Context): Int {
    val countryCode = number.substring(11, 13).toLowerCase(Locale.getDefault())
    return context.resources.getIdentifier("flag_$countryCode", "drawable", context.applicationInfo.packageName)
  }
}
