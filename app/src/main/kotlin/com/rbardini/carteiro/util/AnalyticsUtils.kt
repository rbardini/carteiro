package com.rbardini.carteiro.util

import android.app.Activity
import android.content.Context
import android.os.Bundle
import com.google.firebase.analytics.FirebaseAnalytics
import com.rbardini.carteiro.model.Shipment
import kotlin.math.min

object AnalyticsUtils {
  object Event {
    const val ADD_ITEM = "add_item"
    const val SEARCH = FirebaseAnalytics.Event.SEARCH
    const val SHARE = FirebaseAnalytics.Event.SHARE
    const val VIEW_ITEM = FirebaseAnalytics.Event.VIEW_ITEM
  }

  object Param {
    const val CONTENT_TYPE = FirebaseAnalytics.Param.CONTENT_TYPE
    const val ITEM_CATEGORY = FirebaseAnalytics.Param.ITEM_CATEGORY
    const val ITEM_ID = FirebaseAnalytics.Param.ITEM_ID
    const val ITEM_NAME = FirebaseAnalytics.Param.ITEM_NAME
    const val SEARCH_TERM = FirebaseAnalytics.Param.SEARCH_TERM
    const val SIZE = "size"
  }

  object Value {
    const val CONTENT_TYPE_SHIPMENT = "shipment"
    const val CONTENT_TYPE_SHIPMENT_LIST = "shipment_list"
    const val ITEM_CATEGORY_SHIPMENT = CONTENT_TYPE_SHIPMENT
  }

  private fun getAnalyticsInstance(context: Context) = FirebaseAnalytics.getInstance(context)

  private fun buildShipmentBundle(shipment: Shipment): Bundle {
    return Bundle().apply {
      putString(Param.ITEM_ID, shipment.number)
      putString(Param.ITEM_NAME, shipment.name)
    }
  }

  private fun buildShipmentsBundle(shipments: List<Shipment>): Bundle {
    // Event parameters can be up to 100 characters long, so enforce
    // a maximum number list size to not exceed the limit.
    val size = shipments.size
    val max = min(size, 6)
    val numbers = mutableListOf<String>()
    for (i in 0 until max) numbers.add(shipments[i].number)
    if (size > max) numbers.add("...")

    return Bundle().apply {
      putString(Param.ITEM_ID, numbers.toString())
      putInt(Param.SIZE, size)
    }
  }

  private fun logEvent(context: Context, name: String, bundle: Bundle) {
    getAnalyticsInstance(context).logEvent(name, bundle)
  }

  @JvmStatic
  fun recordScreenView(activity: Activity, screenName: String) {
    getAnalyticsInstance(activity).setCurrentScreen(activity, screenName, null)
  }

  @JvmStatic
  fun recordShipmentAdd(context: Context, shipment: Shipment) {
    val bundle = buildShipmentBundle(shipment)
    bundle.putString(Param.ITEM_CATEGORY, Value.ITEM_CATEGORY_SHIPMENT)

    getAnalyticsInstance(context).logEvent(Event.ADD_ITEM, bundle)
  }

  @JvmStatic
  fun recordShipmentView(context: Context, shipment: Shipment) {
    val bundle = buildShipmentBundle(shipment)
    bundle.putString(Param.ITEM_CATEGORY, Value.ITEM_CATEGORY_SHIPMENT)

    getAnalyticsInstance(context).logEvent(Event.VIEW_ITEM, bundle)
  }

  @JvmStatic
  fun recordSearch(context: Context, searchTerm: String) {
    val bundle = Bundle()
    bundle.putString(Param.SEARCH_TERM, searchTerm)

    logEvent(context, Event.SEARCH, bundle)
  }

  @JvmStatic
  fun recordShare(context: Context, shipment: Shipment) {
    val bundle = buildShipmentBundle(shipment)
    bundle.putString(Param.CONTENT_TYPE,  Value.CONTENT_TYPE_SHIPMENT)

    logEvent(context, Event.SHARE, bundle)
  }

  @JvmStatic
  fun recordShare(context: Context, shipments: List<Shipment>) {
    val bundle = buildShipmentsBundle(shipments)
    bundle.putString(Param.CONTENT_TYPE,  Value.CONTENT_TYPE_SHIPMENT_LIST)

    logEvent(context, Event.SHARE, bundle)
  }
}
