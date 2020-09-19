package com.rbardini.carteiro.util;

import android.content.Context;

import com.rbardini.carteiro.db.DatabaseHelper;
import com.rbardini.carteiro.model.Shipment;
import com.rbardini.carteiro.model.ShipmentRecord;

import java.util.Arrays;
import java.util.Date;

import static com.rbardini.carteiro.db.DatabaseHelper.POSTAL_ITEM_TABLE;
import static java.util.concurrent.TimeUnit.DAYS;
import static java.util.concurrent.TimeUnit.HOURS;
import static java.util.concurrent.TimeUnit.MINUTES;

public final class Developer {
  public static void populate(DatabaseHelper dh) {
    long now = System.currentTimeMillis();
    Shipment shipment;

    dh.deleteAll(POSTAL_ITEM_TABLE);

    shipment = new Shipment("SS123456789BR");
    shipment.setName("Câmera Digital");
    shipment.addRecords(
      Arrays.asList(
        new ShipmentRecord(new Date(now - HOURS.toMillis(89) - MINUTES.toMillis(rand(10, 50))), "Postado", "AC MOCOCA - Mococa/SP", null),
        new ShipmentRecord(new Date(now - HOURS.toMillis(53) - MINUTES.toMillis(rand(10, 50))), "Encaminhado", "AC MOCOCA - Mococa/SP", "Em trânsito para CTE CAMPINAS - Campinas/SP"),
        new ShipmentRecord(new Date(now - HOURS.toMillis(47) - MINUTES.toMillis(rand(10, 50))), "Encaminhado", "CTE CAMPINAS - Campinas/SP", "Em trânsito para CEE CAMPINAS - Campinas/SP"),
        new ShipmentRecord(new Date(now - HOURS.toMillis(29) - MINUTES.toMillis(rand(10, 50))), "Saiu para entrega ao destinatário", "CEE CAMPINAS - Campinas/SP", null),
        new ShipmentRecord(new Date(now - HOURS.toMillis(23) - MINUTES.toMillis(rand(10, 50))), "Destinatário ausente", "CEE CAMPINAS - Campinas/SP", "Será realizada nova tentativa de entrega"),
        new ShipmentRecord(new Date(now - HOURS.toMillis(5) - MINUTES.toMillis(rand(10, 50))), "Saiu para entrega ao destinatário", "CEE CAMPINAS - Campinas/SP", null),
        new ShipmentRecord(new Date(now - HOURS.toMillis(2) - MINUTES.toMillis(rand(10, 50))), "Entrega efetuada", "CEE CAMPINAS - Campinas/SP", null)
      )
    );
    shipment.saveTo(dh);

    shipment = new Shipment("SS234567890BR");
    shipment.setName("Óculos de Sol");
    shipment.setFavorite(true);
    shipment.addRecord(new ShipmentRecord(new Date(now - DAYS.toMillis(1)), "Aguardando retirada", "AC SÃO CARLOS - São Carlos/SP", null));
    shipment.saveTo(dh);

    shipment = new Shipment("SS345678901BR");
    shipment.setName("Roteador Wireless");
    shipment.addRecord(new ShipmentRecord(new Date(now - DAYS.toMillis(3)), "Conferido", "UNIDADE INTERNACIONAL CURITIBA - Curitiba/PR", null));
    shipment.saveTo(dh);

    shipment = new Shipment("SS456789012BR");
    shipment.setName("PlayStation 5");
    shipment.setFavorite(true);
    shipment.addRecord(new ShipmentRecord(new Date(now - DAYS.toMillis(5)), "Devolvido ao remetente", "CDD MOCOCA - Mococa/SP", null));
    shipment.saveTo(dh);

    shipment = new Shipment("SS567890123BR");
    shipment.setName("Tênis Esportivo");
    shipment.addRecord(new ShipmentRecord(new Date(now - DAYS.toMillis(15)), "Destinatário mudou-se", "CEE LIMEIRA - Limeira/SP", null));
    shipment.saveTo(dh);

    shipment = new Shipment("SS678901234BR");
    shipment.setName("HD Externo");
    shipment.addRecord(new ShipmentRecord(new Date(now - DAYS.toMillis(30)), "Encaminhado", "CDD LORENA - Lorena/SP", null));
    shipment.saveTo(dh);

    shipment = new Shipment("SS789012345BR");
    shipment.setName("Bicicleta Dobrável");
    shipment.addRecord(new ShipmentRecord(new Date(now - DAYS.toMillis(60)), "Não encontrado"));
    shipment.saveTo(dh);
  }

  public static void sendNotification(Context context) {
    long now = System.currentTimeMillis();
    Shipment shipment = new Shipment("SS123456789BR");

    shipment.setName("Câmera Digital");
    shipment.addRecord(new ShipmentRecord(new Date(now - HOURS.toMillis(2) - MINUTES.toMillis(rand(10, 50))), "Entrega efetuada", "CEE CAMPINAS - Campinas/SP", null));

    NotificationUtils.notifyShipmentUpdatesIfAllowed(context, Arrays.asList(shipment));
  }

  private static int rand(int min, int max) {
    int range = (max - min) + 1;
    return (int) (Math.random() * range) + min;
  }
}
