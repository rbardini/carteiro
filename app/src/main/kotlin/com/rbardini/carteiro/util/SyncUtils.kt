package com.rbardini.carteiro.util

import android.content.Context
import android.preference.PreferenceManager
import com.rbardini.carteiro.CarteiroApplication
import com.rbardini.carteiro.R
import com.rbardini.carteiro.model.Shipment

object SyncUtils {
  @JvmStatic
  fun getShipmentsForSync(app: CarteiroApplication): List<Shipment> =
    app.databaseHelper.getShallowShipmentsForSync(getSyncFlags(app))

  private fun getSyncFlags(context: Context): Int {
    val prefs = PreferenceManager.getDefaultSharedPreferences(context)
    var flags = 0

    if (prefs.getBoolean(context.getString(R.string.pref_key_sync_favorites_only), false)) {
      flags = flags or PostalUtils.Category.FAVORITES
    }

    if (prefs.getBoolean(context.getString(R.string.pref_key_dont_sync_delivered_items), false)) {
      flags = flags or PostalUtils.Category.UNDELIVERED
    }

    return flags
  }
}
