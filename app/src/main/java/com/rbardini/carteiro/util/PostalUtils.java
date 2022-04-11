package com.rbardini.carteiro.util;

import android.content.Context;
import android.content.Intent;

import com.rbardini.carteiro.R;
import com.rbardini.carteiro.model.Shipment;
import com.rbardini.carteiro.model.ShipmentRecord;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

public final class PostalUtils {
  public static final String HEALTH_URL = "https://downdetector.com.br/fora-do-ar/correios/";

  public static final class Category {
    public static final int ALL         = 0x1;
    public static final int FAVORITES   = 0x2;
    public static final int ARCHIVED    = 0x4;
    public static final int POSTED      = 0x8;
    public static final int UNDELIVERED = 0x10;
    public static final int AVAILABLE   = 0x20;
    public static final int DELIVERED   = 0x40;
    public static final int IRREGULAR   = 0x80;
    public static final int UNKNOWN     = 0x100;
    public static final int RETURNED    = 0x200;
    public static final int IN_TRANSIT  = 0x400;

    private static final Map<Integer, String[]> StatusesMap = buildStatusesMap();
    private static final Map<Integer, Integer> IdMap = buildIdMap();
    private static final Map<Integer, Integer> TitleMap = buildTitleMap();
    private static final Map<Integer, Integer> ColorMap = buildColorMap();
    private static final Map<Integer, Integer> CategoryMap = buildCategoryMap();

    private static TreeMap<Integer, String[]> buildStatusesMap() {
      TreeMap<Integer, String[]> map = new TreeMap<>();

      map.put(POSTED, new String[] {
          Status.COLETADO,
          Status.POSTADO,
          Status.POSTAGEM_DH,
          Status.POSTADO_DEPOIS_HORARIO_LIMITE,
          Status.POSTADO_APOS_HORARIO_LIMITE_AGENCIA,
          Status.POSTADO_APOS_HORARIO_LIMITE_UNIDADE
      });
      map.put(AVAILABLE, new String[] {
          Status.AGUARDANDO_RETIRADA,
          Status.AGUARDANDO_RETIRADA_ENDERECO,
          Status.SAIU_PARA_ENTREGA,
          Status.SAIU_PARA_ENTREGA_DESTINATARIO,
          Status.SAIU_PARA_ENTREGA_REMETENTE,
          Status.DISPONIVEL_EM_CAIXA_POSTAL,
          Status.DISPONIVEL_NA_CAIXA_POSTAL,
          Status.DISPONIVEL_PARA_RETIRADA_NA_CAIXA_POSTAL,
          Status.DISPONIVEL_PARA_RETIRADA_EM_CAIXA_POSTAL
      });
      map.put(DELIVERED, new String[] {
          Status.ENTREGUE,
          Status.ENTREGUE_DESTINATARIO,
          Status.ENTREGA_EFETUADA,
          Status.ENTREGUE_CAIXA_INTELIGENTE
      });
      map.put(IRREGULAR, new String[] {
          Status.DESTINATARIO_DESCONHECIDO_ENDERECO,
          Status.DESTINATARIO_DESCONHECIDO,
          Status.CLIENTE_DESCONHECIDO,
          Status.DESTINATARIO_MUDOU_SE,
          Status.CLIENTE_MUDOU_SE,
          Status.DESTINATARIO_AUSENTE_3_TENTATIVAS,
          Status.DESTINATARIO_RECUSOU_SE,
          Status.CLIENTE_RECUSOU_SE,
          Status.DESTINATARIO_NAO_APRESENTOU_SE,
          Status.DESTINATARIO_NAO_RETIROU,
          Status.REMETENTE_NAO_RETIROU,
          Status.NAO_PROCURADO,
          Status.ENDERECO_INSUFICIENTE,
          Status.MAL_ENDERECADO,
          Status.ENDERECO_INCOMPLETO,
          Status.ENDERECO_INCORRETO,
          Status.ENDERECO_SEM_DISTRIBUICAO,
          Status.DISTRIBUICAO_NAO_AUTORIZADA,
          Status.RETIRADA_NAO_AUTORIZADA,
          Status.LOGRADOURO_IRREGULAR,
          Status.ENDERECO_IRREGULAR,
          Status.NUMERO_INEXISTENTE,
          Status.EMPRESA_SEM_EXPEDIENTE,
          Status.EMPRESA_FALIDA,
          Status.MERCADORIA_AVARIADA,
          Status.AVARIADO,
          Status.OBJETO_AVARIADO,
          Status.AVARIADO_ACIDENTE_VEICULO,
          Status.DOCUMENTACAO_NAO_FORNECIDA,
          Status.DESTINATARIO_NAO_APRESENTOU_DOCUMENTACAO,
          Status.DESTINATARIO_NAO_APRESENTOU_DOCUMENTO,
          Status.OBJETO_FORA_PADRAO,
          Status.DIMENSOES_IMPOSSIBILITAM_ENTREGA,
          Status.DIMENSOES_IMPOSSIBILITAM_TRATAMENTO,
          Status.PEDIDO_NAO_SOLICITADO,
          Status.RECUSADO,
          Status.EM_DEVOLUCAO,
          Status.DEVOLVIDO_SOLICITACAO_REMETENTE,
          Status.ROUBADO,
          Status.OBJETO_PERDIDO_ASSALTO_CARTEIRO,
          Status.OBJETO_PERDIDO_ASSALTO_VEICULO,
          Status.OBJETO_PERDIDO_ASSALTO_UNIDADE,
          Status.RECEBIMENTO_PAGAMENTO_ICMS,
          Status.OBJETO_RETIDO,
          Status.APREENDIDO_POR_ORGAO,
          Status.IMPORTACAO_NAO_AUTORIZADA_RECEITA,
          Status.IMPORTACAO_NAO_AUTORIZADA_FISCALIZADORES,
          Status.DECLARAO_ADUANEIRA_AUSENTE,
          Status.MAL_ENCAMINHADO,
          Status.DESTINATARIO_AUSENTE,
          Status.CARTEIRO_NAO_ATENDIDO,
          Status.DISTRIBUICAO_SUJEITA_PRAZO_DIFERENCIADO,
          Status.AINDA_NAO_CHEGOU,
          Status.ATRASADO,
          Status.ATRASO_ENTREGA,
          Status.LOG_REVERSA_SIMULTANEA,
          Status.LOGISTICA_REVERSA_SIMULTANEA,
          Status.REINTEGRADO,
          Status.DEVOLVIDO_AOS_CORREIOS,
          Status.SAIDA_CANCELADA,
          Status.SAIDA_NAO_EFETUADA,
          Status.SAIDA_ENTREGA_CANCELADA,
          Status.REIMPRESSO,
          Status.ENTREGA_BLOQUEADA,
          Status.ENTREGA_NAO_EFETUADA,
          Status.ENTREGA_NAO_PODE_SER_EFETUADA,
          Status.ENTREGA_NAO_EFETUADA_MOTIVOS_OPERACIONAIS,
          Status.TENTATIVA_ENTREGA_NAO_EFETUADA,
          Status.COLETA_ENTREGA_NAO_EFETUADA,
          Status.AGUARDANDO_DOCUMENTACAO_FISCAL,
          Status.ENTREGA_CONDICIONADA_COMPLEMENTO_DOCUMENTACAO,
          Status.LOTE_INCOMPLETO,
          Status.ESTORNADO,
          Status.CANCELADA_ULTIMA_INFORMACAO,
          Status.DESCONSIDERAR_INFORMACAO_ANTERIOR,
          Status.EM_REVISAO_DE_TRIBUTO_U,
          Status.SOLICITADA_REVISAO_TRIBUTO,
          Status.TRIBUTO_ALTERADO_U,
          Status.TRIBUTO_ALTERADO,
          Status.TRIBUTO_MANTIDO
      });
      map.put(UNKNOWN, new String[] {
          Status.INDETERMINADO,
          Status.NAO_ENCONTRADO,
          Status.NAO_LOCALIZADO,
          Status.NAO_LOCALIZADO_FLUXO_POSTAL
      });
      map.put(RETURNED, new String[] {
          Status.DISTRIBUIDO_AO_REMETENTE,
          Status.DEVOLVIDO_AO_REMETENTE
      });

      return map;
    }

    private static TreeMap<Integer, Integer> buildIdMap() {
      TreeMap<Integer, Integer> map = new TreeMap<>();

      map.put(ALL, R.id.category_all);
      map.put(FAVORITES, R.id.category_favorites);
      map.put(AVAILABLE, R.id.category_available);
      map.put(DELIVERED, R.id.category_delivered);
      map.put(IN_TRANSIT, R.id.category_in_transit);
      map.put(IRREGULAR, R.id.category_irregular);
      map.put(UNKNOWN, R.id.category_unknown);
      map.put(RETURNED, R.id.category_returned);
      map.put(ARCHIVED, R.id.category_archived);

      return map;
    }

    private static LinkedHashMap<Integer, Integer> buildTitleMap() {
      // Use LinkedHashMap to keep insertion order
      LinkedHashMap<Integer, Integer> map = new LinkedHashMap<>();

      map.put(ALL, R.string.category_all);
      map.put(FAVORITES, R.string.category_favorites);
      map.put(AVAILABLE, R.string.category_available);
      map.put(DELIVERED, R.string.category_delivered);
      map.put(IN_TRANSIT, R.string.category_in_transit);
      map.put(IRREGULAR, R.string.category_irregular);
      map.put(UNKNOWN, R.string.category_unknown);
      map.put(RETURNED, R.string.category_returned);
      map.put(ARCHIVED, R.string.category_archived);

      return map;
    }

    private static TreeMap<Integer, Integer> buildColorMap() {
      TreeMap<Integer, Integer> map = new TreeMap<>();

      map.put(ALL, R.color.postal_status_all);
      map.put(POSTED, R.color.postal_status_posted);
      map.put(AVAILABLE, R.color.postal_status_available);
      map.put(DELIVERED, R.color.postal_status_delivered);
      map.put(IN_TRANSIT, R.color.postal_status_in_transit);
      map.put(IRREGULAR, R.color.postal_status_irregular);
      map.put(UNKNOWN, R.color.postal_status_unknown);
      map.put(RETURNED, R.color.postal_status_returned);

      return map;
    }

    private static TreeMap<Integer, Integer> buildCategoryMap() {
      TreeMap<Integer, Integer> map = new TreeMap<>();

      for (Entry<Integer, Integer> entry : Category.IdMap.entrySet()) {
        Integer id = entry.getValue();
        Integer category = entry.getKey();
        map.put(id, category);
      }

      return map;
    }

    public static String[] getStatuses(int category) {
      return StatusesMap.get(category);
    }

    public static int getId(int category) {
      return IdMap.get(category);
    }

    public static int getTitle(int category) {
      return TitleMap.get(category);
    }

    public static Map<Integer, Integer> getTitleMap() {
      return TitleMap;
    }

    public static int getColor(int category) {
      return ColorMap.get(category);
    }

    public static int getCategoryById(int id) {
      return CategoryMap.get(id);
    }
  }

  public static final class Status {
    // Posted
    public static final String COLETADO = "Coletado"; // New
    public static final String POSTADO = "Postado"; // New + old
    public static final String POSTAGEM_DH = "Postagem - DH";
    public static final String POSTADO_DEPOIS_HORARIO_LIMITE = "Postado depois do horário limite da agência";
    public static final String POSTADO_APOS_HORARIO_LIMITE_AGENCIA = "Postado após o horário limite da agência"; // New
    public static final String POSTADO_APOS_HORARIO_LIMITE_UNIDADE = "Postado após o horário limite da unidade"; // New

    // Available
    public static final String AGUARDANDO_RETIRADA = "Aguardando retirada";
    public static final String AGUARDANDO_RETIRADA_ENDERECO = "Aguardando retirada no endereço indicado"; // New
    public static final String SAIU_PARA_ENTREGA = "Saiu para entrega";
    public static final String SAIU_PARA_ENTREGA_DESTINATARIO = "Saiu para entrega ao destinatário"; // New + old
    public static final String SAIU_PARA_ENTREGA_REMETENTE = "Saiu para entrega ao remetente"; // New
    public static final String DISPONIVEL_EM_CAIXA_POSTAL = "Disponível em caixa postal";
    public static final String DISPONIVEL_NA_CAIXA_POSTAL = "Disponível na caixa postal";
    public static final String DISPONIVEL_PARA_RETIRADA_NA_CAIXA_POSTAL = "Disponível para retirada na caixa postal";
    public static final String DISPONIVEL_PARA_RETIRADA_EM_CAIXA_POSTAL = "Disponível para retirada em Caixa Postal"; // New

    // Delivered
    public static final String ENTREGUE = "Entregue";
    public static final String ENTREGUE_DESTINATARIO = "Entregue ao destinatário"; // New
    public static final String ENTREGA_EFETUADA = "Entrega Efetuada";
    public static final String ENTREGUE_CAIXA_INTELIGENTE = "Entregue na Caixa de Correios Inteligente";

    // Irregular
    public static final String DESTINATARIO_DESCONHECIDO_ENDERECO = "Destinatário desconhecido no endereço";
    public static final String DESTINATARIO_DESCONHECIDO = "Destinatário desconhecido";
    public static final String CLIENTE_DESCONHECIDO = "Cliente desconhecido no local"; // New
    public static final String DESTINATARIO_MUDOU_SE = "Destinatário mudou-se";
    public static final String CLIENTE_MUDOU_SE = "Cliente mudou-se"; // New
    public static final String DESTINATARIO_AUSENTE_3_TENTATIVAS = "Destinatário ausente em 3 tentativas de entrega";
    public static final String DESTINATARIO_RECUSOU_SE = "Destinatário recusou-se a receber";
    public static final String CLIENTE_RECUSOU_SE = "Cliente recusou-se a receber"; // New
    public static final String DESTINATARIO_NAO_APRESENTOU_SE = "Destinatário não apresentou-se para receber";
    public static final String DESTINATARIO_NAO_RETIROU = "Destinatário não retirou objeto na Unidade dos Correios"; // New
    public static final String REMETENTE_NAO_RETIROU = "Remetente não retirou objeto na Unidade dos Correios"; // New
    public static final String NAO_PROCURADO = "Não procurado";
    public static final String ENDERECO_INSUFICIENTE = "Endereço insuficiente para entrega";
    public static final String MAL_ENDERECADO = "Mal endereçado";
    public static final String ENDERECO_INCOMPLETO = "Endereço incompleto - em pesquisa";
    public static final String ENDERECO_INCORRETO = "Endereço incorreto"; // New + old
    public static final String ENDERECO_SEM_DISTRIBUICAO = "Endereço sem distribuição domiciliária - Entrega interna não autorizada";
    public static final String DISTRIBUICAO_NAO_AUTORIZADA = "Distribuição não autorizada";
    public static final String RETIRADA_NAO_AUTORIZADA = "Retirada em Unidade dos Correios não autorizada pelo remetente"; // New
    public static final String LOGRADOURO_IRREGULAR = "Logradouro com numeração irregular"; // New + old
    public static final String ENDERECO_IRREGULAR = "Endereço com numeração irregular - Em pesquisa";
    public static final String NUMERO_INEXISTENTE = "Não existe o número indicado";
    public static final String EMPRESA_SEM_EXPEDIENTE = "Empresa sem expediente"; // New + old
    public static final String EMPRESA_FALIDA = "Endereçado à empresa falida"; // New
    public static final String MERCADORIA_AVARIADA = "Mercadoria avariada";
    public static final String AVARIADO = "Avariado"; // New
    public static final String OBJETO_AVARIADO = "Objeto avariado";
    public static final String AVARIADO_ACIDENTE_VEICULO = "Avariado por acidente com veículo"; // New
    public static final String DOCUMENTACAO_NAO_FORNECIDA = "Documentação não fornecida pelo destinatário";
    public static final String DESTINATARIO_NAO_APRESENTOU_DOCUMENTACAO = "Destinatário não apresentou a documentação";
    public static final String DESTINATARIO_NAO_APRESENTOU_DOCUMENTO = "Destinatário não apresentou documento exigido"; // New
    public static final String OBJETO_FORA_PADRAO = "Objeto fora do padrão - Limites de dimensão";
    public static final String DIMENSOES_IMPOSSIBILITAM_ENTREGA = "Dimensões impossibilitam a entrega";
    public static final String DIMENSOES_IMPOSSIBILITAM_TRATAMENTO = "As dimensões do objeto impossibilitam o tratamento e a entrega"; // New
    public static final String PEDIDO_NAO_SOLICITADO = "Pedido não solicitado";
    public static final String RECUSADO = "Recusado";
    public static final String EM_DEVOLUCAO = "Em devolução";
    public static final String DEVOLVIDO_SOLICITACAO_REMETENTE = "Será devolvido por solicitação do remetente"; // New
    public static final String ROUBADO = "Roubado"; // New
    public static final String OBJETO_PERDIDO_ASSALTO_CARTEIRO = "Objeto perdido em assalto ao carteiro";
    public static final String OBJETO_PERDIDO_ASSALTO_VEICULO = "Objeto perdido em assalto a veículo dos correios";
    public static final String OBJETO_PERDIDO_ASSALTO_UNIDADE = "Objeto perdido em assalto a unidade dos correios";
    public static final String RECEBIMENTO_PAGAMENTO_ICMS = "Para recebimento do objeto é necessário o pagamento do ICMS Importação"; // New
    public static final String OBJETO_RETIDO = "Objeto retido pelo órgão de fiscalização";
    public static final String APREENDIDO_POR_ORGAO = "Apreendido por órgão de fiscalização ou outro órgão anuente"; // New
    public static final String IMPORTACAO_NAO_AUTORIZADA_RECEITA = "Importação não autorizada por órgão da receita";
    public static final String IMPORTACAO_NAO_AUTORIZADA_FISCALIZADORES = "A importação do objeto não foi autorizada pelos órgãos fiscalizadores"; // New
    public static final String DECLARAO_ADUANEIRA_AUSENTE = "Declaração aduaneira ausente ou incorreta"; // New
    public static final String MAL_ENCAMINHADO = "Mal encaminhado"; // New + old
    public static final String DESTINATARIO_AUSENTE = "Destinatário ausente";
    public static final String CARTEIRO_NAO_ATENDIDO = "Carteiro não atendido"; // New
    public static final String DISTRIBUICAO_SUJEITA_PRAZO_DIFERENCIADO = "Área com distribuição sujeita a prazo diferenciado"; // New + old
    public static final String AINDA_NAO_CHEGOU = "Ainda não chegou na unidade"; // New
    public static final String ATRASADO = "Atrasado";
    public static final String ATRASO_ENTREGA = "Com atraso na entrega"; // New
    public static final String LOG_REVERSA_SIMULTANEA = "Log. reversa simultânea";
    public static final String LOGISTICA_REVERSA_SIMULTANEA = "Logística reversa simultânea";
    public static final String REINTEGRADO = "Reintegrado";
    public static final String DEVOLVIDO_AOS_CORREIOS = "Devolvido aos Correios"; // New
    public static final String OBJETO_DEVOLVIDO_AOS_CORREIOS = "Objeto devolvido aos correios";
    public static final String SAIDA_CANCELADA = "Saída cancelada";
    public static final String SAIDA_NAO_EFETUADA = "Saída não efetuada";
    public static final String SAIDA_ENTREGA_CANCELADA = "Saída para entrega cancelada"; // New
    public static final String REIMPRESSO = "Reimpresso e reenviado"; // New
    public static final String ENTREGA_BLOQUEADA = "Entrega de objeto bloqueada a pedido do remetente"; // New
    public static final String ENTREGA_NAO_EFETUADA = "Entrega não efetuada";
    public static final String ENTREGA_NAO_PODE_SER_EFETUADA = "A entrega não pode ser efetuada"; // New
    public static final String ENTREGA_NAO_EFETUADA_MOTIVOS_OPERACIONAIS = "Entrega não efetuada por motivos operacionais";
    public static final String TENTATIVA_ENTREGA_NAO_EFETUADA = "Tentativa de entrega não efetuada"; // New
    public static final String COLETA_ENTREGA_NAO_EFETUADA = "Coleta ou entrega de objeto não efetuada"; // New
    public static final String AGUARDANDO_DOCUMENTACAO_FISCAL = "Aguardando documentação fiscal";
    public static final String ENTREGA_CONDICIONADA_COMPLEMENTO_DOCUMENTACAO = "Entrega condicionada ao complemento da documentação";
    public static final String LOTE_INCOMPLETO = "Lote de objetos incompleto"; // New
    public static final String ESTORNADO = "Estornado";
    public static final String CANCELADA_ULTIMA_INFORMACAO = "Cancelada a última informação";
    public static final String DESCONSIDERAR_INFORMACAO_ANTERIOR = "Favor desconsiderar a informação anterior"; // New
    public static final String EM_REVISAO_DE_TRIBUTO_U = "EM REVISÃO DE TRIBUTO";
    public static final String SOLICITADA_REVISAO_TRIBUTO = "Solicitada revisão do tributo estabelecido"; // New
    public static final String TRIBUTO_ALTERADO_U = "TRIBUTO ALTERADO";
    public static final String TRIBUTO_ALTERADO = "Tributo alterado"; // New
    public static final String TRIBUTO_MANTIDO = "Tributo mantido"; // New

    // Unknown
    public static final String INDETERMINADO = "Indeterminado";
    public static final String NAO_ENCONTRADO = "Não encontrado";
    public static final String NAO_LOCALIZADO = "Não localizado"; // New
    public static final String NAO_LOCALIZADO_FLUXO_POSTAL = "Não localizado no fluxo postal";

    // Returned
    public static final String DISTRIBUIDO_AO_REMETENTE = "Distribuido ao Remetente";
    public static final String DEVOLVIDO_AO_REMETENTE = "Devolvido ao remetente"; // New + old

    // Other
    public static final String ENCAMINHADO = "Encaminhado";
    public static final String ENCAMINHADO_PARA = "Encaminhado para"; // New
    public static final String EM_TRANSITO = "Em trânsito"; // New
    public static final String SAIU_UNIDADE_INTERNACIONAL = "Saiu da Unidade Internacional"; // New
    public static final String RECEBIDO_EM = "Recebido em"; // New
    public static final String RECEBIDO_UNIDADE_DISTRIBUICAO = "Recebido na unidade de distribuição"; // New + old
    public static final String RECEBIDO_UNIDADE_EXPORTACAO = "Recebido na unidade de exportação"; // New
    public static final String RECEBIDO_UNIDADE_CORREIOS = "Recebido na Unidade dos Correios"; // New
    public static final String RECEBIDO_BRASIL = "Recebido no Brasil"; // New
    public static final String RECEBIDO_CORREIOS_BRASIL = "Recebido pelos Correios do Brasil"; // New
    public static final String CONFERIDO = "Conferido";
    public static final String LIBERADO = "Liberado"; // New
    public static final String LIBERADO_ALFANDEGA = "Liberado pela alfândega"; // New
    public static final String ENTREGA_PROGRAMADA = "Entrega programada";
    public static final String ENTREGA_AGENDADA = "Com data de entrega agendada"; // New
    public static final String DISTRIBUICAO_ESPECIAL_AGENDADA = "Distribuição especial agendada";
    public static final String AGUARDANDO_PARTE_LOTE = "Aguardando parte do lote";
    public static final String ENTREGA_CONDICIONADA_COMPOSICAO_LOTE = "Entrega condicionada à composição do lote";
    public static final String ENTREGA_OBJETO_CONDICIONADA_COMPOSICAO_LOTE = "A entrega do objeto está condicionada à composição do lote"; // New

    private static final Map<String, Integer> CategoryMap = buildCategoryMap();
    private static final Map<String, Integer> IconMap = buildIconMap();

    private static TreeMap<String, Integer> buildCategoryMap() {
      TreeMap<String, Integer> map = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);

      for (Entry<Integer, String[]> entry : Category.StatusesMap.entrySet()) {
        String[] statuses = entry.getValue();

        for (String status : statuses) {
          Integer category = entry.getKey();
          map.put(status, category);
        }
      }

      return map;
    }

    private static TreeMap<String, Integer> buildIconMap() {
      TreeMap<String, Integer> map = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);

      // Posted
      map.put(COLETADO, R.drawable.ic_postal_distribuido_ao_remetente);
      map.put(POSTADO, R.drawable.ic_postal_postado);
      map.put(POSTAGEM_DH, R.drawable.ic_postal_postado);
      map.put(POSTADO_DEPOIS_HORARIO_LIMITE, R.drawable.ic_postal_postado);
      map.put(POSTADO_APOS_HORARIO_LIMITE_AGENCIA, R.drawable.ic_postal_postado);
      map.put(POSTADO_APOS_HORARIO_LIMITE_UNIDADE, R.drawable.ic_postal_postado);

      // Available
      map.put(AGUARDANDO_RETIRADA, R.drawable.ic_postal_aguardando_retirada);
      map.put(AGUARDANDO_RETIRADA_ENDERECO, R.drawable.ic_postal_aguardando_retirada);
      map.put(SAIU_PARA_ENTREGA, R.drawable.ic_postal_saiu_entrega);
      map.put(SAIU_PARA_ENTREGA_DESTINATARIO, R.drawable.ic_postal_saiu_entrega);
      map.put(SAIU_PARA_ENTREGA_REMETENTE, R.drawable.ic_postal_saiu_entrega);
      map.put(DISPONIVEL_EM_CAIXA_POSTAL, R.drawable.ic_postal_disponivel_caixa_postal);
      map.put(DISPONIVEL_NA_CAIXA_POSTAL, R.drawable.ic_postal_disponivel_caixa_postal);
      map.put(DISPONIVEL_PARA_RETIRADA_NA_CAIXA_POSTAL, R.drawable.ic_postal_disponivel_caixa_postal);
      map.put(DISPONIVEL_PARA_RETIRADA_EM_CAIXA_POSTAL, R.drawable.ic_postal_disponivel_caixa_postal);

      // Delivered
      map.put(ENTREGUE, R.drawable.ic_postal_entregue);
      map.put(ENTREGUE_DESTINATARIO, R.drawable.ic_postal_entregue);
      map.put(ENTREGA_EFETUADA, R.drawable.ic_postal_entregue);
      map.put(ENTREGUE_CAIXA_INTELIGENTE, R.drawable.ic_postal_entregue);

      // Irregular
      map.put(DESTINATARIO_DESCONHECIDO_ENDERECO, R.drawable.ic_postal_destinatario_desconhecido);
      map.put(DESTINATARIO_DESCONHECIDO, R.drawable.ic_postal_destinatario_desconhecido);
      map.put(CLIENTE_DESCONHECIDO, R.drawable.ic_postal_destinatario_desconhecido);
      map.put(DESTINATARIO_MUDOU_SE, R.drawable.ic_postal_destinatario_mudou);
      map.put(CLIENTE_MUDOU_SE, R.drawable.ic_postal_destinatario_mudou);
      map.put(DESTINATARIO_AUSENTE_3_TENTATIVAS, R.drawable.ic_postal_destinatario_ausente);
      map.put(DESTINATARIO_RECUSOU_SE, R.drawable.ic_postal_recusado);
      map.put(CLIENTE_RECUSOU_SE, R.drawable.ic_postal_recusado);
      map.put(DESTINATARIO_NAO_APRESENTOU_SE, R.drawable.ic_postal_nao_procurado);
      map.put(DESTINATARIO_NAO_RETIROU, R.drawable.ic_postal_nao_procurado);
      map.put(REMETENTE_NAO_RETIROU, R.drawable.ic_postal_nao_procurado);
      map.put(NAO_PROCURADO, R.drawable.ic_postal_nao_procurado);
      map.put(ENDERECO_INSUFICIENTE, R.drawable.ic_postal_endereco_insuficiente);
      map.put(MAL_ENDERECADO, R.drawable.ic_postal_endereco_insuficiente);
      map.put(ENDERECO_INCOMPLETO, R.drawable.ic_postal_endereco_insuficiente);
      map.put(ENDERECO_INCORRETO, R.drawable.ic_postal_endereco_incorreto);
      map.put(ENDERECO_SEM_DISTRIBUICAO, R.drawable.ic_postal_endereco_sem_distribuicao);
      map.put(DISTRIBUICAO_NAO_AUTORIZADA, R.drawable.ic_postal_saida_cancelada);
      map.put(RETIRADA_NAO_AUTORIZADA, R.drawable.ic_postal_retirada_nao_autorizada);
      map.put(LOGRADOURO_IRREGULAR, R.drawable.ic_postal_endereco_insuficiente);
      map.put(ENDERECO_IRREGULAR, R.drawable.ic_postal_endereco_insuficiente);
      map.put(NUMERO_INEXISTENTE, R.drawable.ic_postal_endereco_incorreto);
      map.put(EMPRESA_SEM_EXPEDIENTE, R.drawable.ic_postal_empresa_sem_expediente);
      map.put(EMPRESA_FALIDA, R.drawable.ic_postal_empresa_falida);
      map.put(AVARIADO_ACIDENTE_VEICULO, R.drawable.ic_postal_objeto_perdido_assalto_veiculo);
      map.put(DOCUMENTACAO_NAO_FORNECIDA, R.drawable.ic_postal_documentacao_nao_fornecida);
      map.put(DESTINATARIO_NAO_APRESENTOU_DOCUMENTACAO, R.drawable.ic_postal_documentacao_nao_fornecida);
      map.put(DESTINATARIO_NAO_APRESENTOU_DOCUMENTO, R.drawable.ic_postal_documentacao_nao_fornecida);
      map.put(OBJETO_FORA_PADRAO, R.drawable.ic_postal_objeto_fora_padrao);
      map.put(DIMENSOES_IMPOSSIBILITAM_ENTREGA, R.drawable.ic_postal_objeto_fora_padrao);
      map.put(DIMENSOES_IMPOSSIBILITAM_TRATAMENTO, R.drawable.ic_postal_objeto_fora_padrao);
      map.put(PEDIDO_NAO_SOLICITADO, R.drawable.ic_postal_pedido_nao_solicitado);
      map.put(RECUSADO, R.drawable.ic_postal_recusado);
      map.put(EM_DEVOLUCAO, R.drawable.ic_postal_em_devolucao);
      map.put(DEVOLVIDO_SOLICITACAO_REMETENTE, R.drawable.ic_postal_em_devolucao);
      map.put(ROUBADO, R.drawable.ic_postal_objeto_perdido_assalto_carteiro);
      map.put(OBJETO_PERDIDO_ASSALTO_CARTEIRO, R.drawable.ic_postal_objeto_perdido_assalto_carteiro);
      map.put(OBJETO_PERDIDO_ASSALTO_VEICULO, R.drawable.ic_postal_objeto_perdido_assalto_veiculo);
      map.put(OBJETO_PERDIDO_ASSALTO_UNIDADE, R.drawable.ic_postal_objeto_perdido_assalto_unidade);
      map.put(RECEBIMENTO_PAGAMENTO_ICMS, R.drawable.ic_postal_aguardando_documentacao_fiscal);
      map.put(OBJETO_RETIDO, R.drawable.ic_postal_objeto_retido);
      map.put(APREENDIDO_POR_ORGAO, R.drawable.ic_postal_objeto_retido);
      map.put(IMPORTACAO_NAO_AUTORIZADA_RECEITA, R.drawable.ic_postal_objeto_retido);
      map.put(IMPORTACAO_NAO_AUTORIZADA_FISCALIZADORES, R.drawable.ic_postal_objeto_retido);
      map.put(DECLARAO_ADUANEIRA_AUSENTE, R.drawable.ic_postal_documentacao_nao_fornecida);
      map.put(MAL_ENCAMINHADO, R.drawable.ic_postal_mal_encaminhado);
      map.put(DESTINATARIO_AUSENTE, R.drawable.ic_postal_destinatario_ausente);
      map.put(CARTEIRO_NAO_ATENDIDO, R.drawable.ic_postal_destinatario_ausente);
      map.put(DISTRIBUICAO_SUJEITA_PRAZO_DIFERENCIADO, R.drawable.ic_postal_entrega_programada);
      map.put(AINDA_NAO_CHEGOU, R.drawable.ic_postal_atrasado);
      map.put(ATRASADO, R.drawable.ic_postal_atrasado);
      map.put(ATRASO_ENTREGA, R.drawable.ic_postal_atrasado);
      map.put(LOG_REVERSA_SIMULTANEA, R.drawable.ic_postal_log_reversa_simultanea);
      map.put(LOGISTICA_REVERSA_SIMULTANEA, R.drawable.ic_postal_log_reversa_simultanea);
      map.put(REINTEGRADO, R.drawable.ic_postal_distribuido_ao_remetente);
      map.put(OBJETO_DEVOLVIDO_AOS_CORREIOS, R.drawable.ic_postal_distribuido_ao_remetente);
      map.put(SAIDA_CANCELADA, R.drawable.ic_postal_saida_cancelada);
      map.put(SAIDA_NAO_EFETUADA, R.drawable.ic_postal_saida_cancelada);
      map.put(SAIDA_ENTREGA_CANCELADA, R.drawable.ic_postal_saida_cancelada);
      map.put(REIMPRESSO, R.drawable.ic_postal_reintegrado);
      map.put(ENTREGA_BLOQUEADA, R.drawable.ic_postal_saida_cancelada);
      map.put(ENTREGA_NAO_EFETUADA, R.drawable.ic_postal_entrega_nao_efetuada);
      map.put(ENTREGA_NAO_PODE_SER_EFETUADA, R.drawable.ic_postal_entrega_nao_efetuada);
      map.put(ENTREGA_NAO_EFETUADA_MOTIVOS_OPERACIONAIS, R.drawable.ic_postal_entrega_nao_efetuada);
      map.put(TENTATIVA_ENTREGA_NAO_EFETUADA, R.drawable.ic_postal_entrega_nao_efetuada);
      map.put(COLETA_ENTREGA_NAO_EFETUADA, R.drawable.ic_postal_entrega_nao_efetuada);
      map.put(AGUARDANDO_DOCUMENTACAO_FISCAL, R.drawable.ic_postal_aguardando_documentacao_fiscal);
      map.put(ENTREGA_CONDICIONADA_COMPLEMENTO_DOCUMENTACAO, R.drawable.ic_postal_aguardando_documentacao_fiscal);
      map.put(LOTE_INCOMPLETO, R.drawable.ic_postal_lote_incompleto);
      map.put(ESTORNADO, R.drawable.ic_postal_cancelada_ultima_informacao);
      map.put(CANCELADA_ULTIMA_INFORMACAO, R.drawable.ic_postal_cancelada_ultima_informacao);
      map.put(DESCONSIDERAR_INFORMACAO_ANTERIOR, R.drawable.ic_postal_cancelada_ultima_informacao);
      map.put(EM_REVISAO_DE_TRIBUTO_U, R.drawable.ic_postal_em_revisao_de_tributo);
      map.put(SOLICITADA_REVISAO_TRIBUTO, R.drawable.ic_postal_em_revisao_de_tributo);
      map.put(TRIBUTO_ALTERADO_U, R.drawable.ic_postal_tributo_alterado);
      map.put(TRIBUTO_ALTERADO, R.drawable.ic_postal_tributo_alterado);
      map.put(TRIBUTO_MANTIDO, R.drawable.ic_postal_tributo_alterado);

      // Unknown
      map.put(INDETERMINADO, R.drawable.ic_postal_indeterminado);
      map.put(NAO_ENCONTRADO, R.drawable.ic_postal_nao_encontrado);
      map.put(NAO_LOCALIZADO, R.drawable.ic_postal_nao_encontrado);
      map.put(NAO_LOCALIZADO_FLUXO_POSTAL, R.drawable.ic_postal_nao_encontrado);

      // Returned
      map.put(DISTRIBUIDO_AO_REMETENTE, R.drawable.ic_postal_entregue);
      map.put(DEVOLVIDO_AO_REMETENTE, R.drawable.ic_postal_entregue);

      // Other
      map.put(ENCAMINHADO, R.drawable.ic_postal_encaminhado);
      map.put(ENCAMINHADO_PARA, R.drawable.ic_postal_encaminhado);
      map.put(EM_TRANSITO, R.drawable.ic_postal_encaminhado);
      map.put(SAIU_UNIDADE_INTERNACIONAL, R.drawable.ic_postal_encaminhado);
      map.put(RECEBIDO_EM, R.drawable.ic_postal_recebido);
      map.put(RECEBIDO_UNIDADE_DISTRIBUICAO, R.drawable.ic_postal_recebido);
      map.put(RECEBIDO_UNIDADE_EXPORTACAO, R.drawable.ic_postal_recebido);
      map.put(RECEBIDO_UNIDADE_CORREIOS, R.drawable.ic_postal_recebido);
      map.put(RECEBIDO_BRASIL, R.drawable.ic_postal_recebido);
      map.put(RECEBIDO_CORREIOS_BRASIL, R.drawable.ic_postal_recebido);
      map.put(CONFERIDO, R.drawable.ic_postal_conferido);
      map.put(LIBERADO, R.drawable.ic_postal_conferido);
      map.put(LIBERADO_ALFANDEGA, R.drawable.ic_postal_conferido);
      map.put(ENTREGA_PROGRAMADA, R.drawable.ic_postal_entrega_programada);
      map.put(ENTREGA_AGENDADA, R.drawable.ic_postal_entrega_programada);
      map.put(DISTRIBUICAO_ESPECIAL_AGENDADA, R.drawable.ic_postal_entrega_programada);
      map.put(AGUARDANDO_PARTE_LOTE, R.drawable.ic_postal_aguardando_parte_lote);
      map.put(ENTREGA_CONDICIONADA_COMPOSICAO_LOTE, R.drawable.ic_postal_aguardando_parte_lote);
      map.put(ENTREGA_OBJETO_CONDICIONADA_COMPOSICAO_LOTE, R.drawable.ic_postal_aguardando_parte_lote);

      return map;
    }

    public static int getCategory(String status) {
      Integer flag = CategoryMap.get(status);
      return flag != null ? flag : 0;
    }

    public static Map<String, Integer> getCategoryMap() {
      return CategoryMap;
    }

    public static int getIcon(String status) {
      Integer icon = IconMap.get(status);
      return icon != null ? icon : R.drawable.ic_postal_outros;
    }
  }

  public static final class Service {
    private static final Map<String, String> ServicesMap = buildServicesMap();

    private static TreeMap<String, String> buildServicesMap() {
      TreeMap<String, String> map = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);

      map.put("AA", "ETIQUETA LÓGICA SEDEX");
      map.put("AB", "ETIQUETA LÓGICA SEDEX");
      map.put("AL", "AGENTES DE LEITURA");
      map.put("AR", "AVISO DE RECEBIMENTO");
      map.put("AS", "ENCOMENDA PAC - AÇÃO SOCIAL");
      map.put("BE", "REMESSA ECONÔMICA SEM AR DIGITAL");
      map.put("BF", "REMESSA EXPRESSA SEM AR DIGITAL");
      map.put("BG", "ETIQUETA LÓGICA REMESSA ECONÔMICA COM AR BG");
      map.put("BH", "MENSAGEM FÍSICO-DIGITAL");
      map.put("BI", "ETIQUETA LÓGICA REGISTRADO URGENTE");
      map.put("BM", "REMESSA EXPRESSA COM AR DIGITAL");
      map.put("BQ", "REMESSA EXPRESSA COM AR DIGITAL E FOTO FACHADA");
      map.put("BS", "REMESSA EXPRESSA COM AR ELETRÔNICO");
      map.put("BX", "REMESSA EXPRESSA COM AR ELETRÔNICO E FOTO FACHADA");
      map.put("CA", "OBJETO INTERNACIONAL COLIS");
      map.put("CB", "OBJETO INTERNACIONAL COLIS");
      map.put("CC", "OBJETO INTERNACIONAL COLIS");
      map.put("CD", "OBJETO INTERNACIONAL COLIS");
      map.put("CE", "OBJETO INTERNACIONAL COLIS");
      map.put("CF", "OBJETO INTERNACIONAL COLIS");
      map.put("CG", "OBJETO INTERNACIONAL COLIS");
      map.put("CH", "OBJETO INTERNACIONAL COLIS");
      map.put("CI", "OBJETO INTERNACIONAL COLIS");
      map.put("CJ", "OBJETO INTERNACIONAL COLIS");
      map.put("CK", "OBJETO INTERNACIONAL COLIS");
      map.put("CL", "OBJETO INTERNACIONAL COLIS");
      map.put("CM", "OBJETO INTERNACIONAL COLIS");
      map.put("CN", "OBJETO INTERNACIONAL COLIS");
      map.put("CO", "OBJETO INTERNACIONAL COLIS");
      map.put("CP", "OBJETO INTERNACIONAL COLIS");
      map.put("CQ", "OBJETO INTERNACIONAL COLIS");
      map.put("CR", "OBJETO INTERNACIONAL COLIS");
      map.put("CS", "OBJETO INTERNACIONAL COLIS");
      map.put("CT", "OBJETO INTERNACIONAL COLIS");
      map.put("CU", "OBJETO INTERNACIONAL COLIS");
      map.put("CV", "OBJETO INTERNACIONAL COLIS");
      map.put("CW", "OBJETO INTERNACIONAL COLIS");
      map.put("CX", "OBJETO INTERNACIONAL COLIS");
      map.put("CY", "OBJETO INTERNACIONAL COLIS");
      map.put("CZ", "OBJETO INTERNACIONAL COLIS");
      map.put("DA", "ENCOMENDA SEDEX COM AR DIGITAL");
      map.put("DB", "REMESSA EXPRESSA COM AR DIGITAL BRADESCO");
      map.put("DC", "REMESSA EXPRESSA (ÓRGÃO TRÂNSITO)");
      map.put("DD", "DEVOLUÇÃO DE DOCUMENTOS");
      map.put("DE", "REMESSA EXPRESSA COM AR DIGITAL");
      map.put("DF", "ENCOMENDA SEDEX (ETIQUETA LÓGICA)");
      map.put("DG", "ENCOMENDA SEDEX (ETIQUETA LÓGICA)");
      map.put("DI", "REMESSA EXPRESSA COM AR DIGITAL ITAÚ");
      map.put("DJ", "ENCOMENDA SEDEX");
      map.put("DK", "SEDEX EXTRA GRANDE");
      map.put("DL", "SEDEX LÓGICO");
      map.put("DM", "ENCOMENDA SEDEX");
      map.put("DN", "ENCOMENDA SEDEX");
      map.put("DO", "REMESSA EXPRESSA COM AR DIGITAL ITAÚ UNIBANCO");
      map.put("DP", "SEDEX PAGAMENTO ENTREGA");
      map.put("DQ", "REMESSA EXPRESSA COM AR DIGITAL BRADESCO");
      map.put("DR", "REMESSA EXPRESSA COM AR DIGITAL SANTANDER");
      map.put("DS", "REMESSA EXPRESSA COM AR DIGITAL SANTANDER");
      map.put("DT", "REMESSA ECONÔMICA SEG. TRÂNSITO COM AR DIGITAL");
      map.put("DU", "ENCOMENDA SEDEX");
      map.put("DV", "SEDEX COM AR DIGITAL");
      map.put("DW", "ENCOMENDA SEDEX (ETIQUETA LÓGICA)");
      map.put("DX", "SEDEX 10 LÓGICO");
      map.put("DY", "ENCOMENDA SEDEX (ETIQUETA FÍSICA)");
      map.put("DZ", "ENCOMENDA SEDEX (ETIQUETA LÓGICA)");
      map.put("EA", "OBJETO INTERNACIONAL EMS");
      map.put("EB", "OBJETO INTERNACIONAL EMS");
      map.put("EC", "ENCOMENDA PAC");
      map.put("EE", "OBJETO INTERNACIONAL EMS");
      map.put("EF", "OBJETO INTERNACIONAL EMS");
      map.put("EG", "OBJETO INTERNACIONAL EMS");
      map.put("EH", "OBJETO INTERNACIONAL EMS");
      map.put("EI", "OBJETO INTERNACIONAL EMS");
      map.put("EJ", "OBJETO INTERNACIONAL EMS");
      map.put("EK", "OBJETO INTERNACIONAL EMS");
      map.put("EL", "OBJETO INTERNACIONAL EMS");
      map.put("EM", "SEDEX MUNDI");
      map.put("EN", "OBJETO INTERNACIONAL EMS");
      map.put("EO", "OBJETO INTERNACIONAL EMS");
      map.put("EP", "OBJETO INTERNACIONAL EMS");
      map.put("EQ", "ENCOMENDA SERVIÇO NÃO EXPRESSA ECT");
      map.put("ER", "REGISTRADO");
      map.put("ES", "OBJETO INTERNACIONAL EMS");
      map.put("ET", "OBJETO INTERNACIONAL EMS");
      map.put("EU", "OBJETO INTERNACIONAL EMS");
      map.put("EV", "OBJETO INTERNACIONAL EMS");
      map.put("EW", "OBJETO INTERNACIONAL EMS");
      map.put("EX", "OBJETO INTERNACIONAL EMS");
      map.put("EY", "OBJETO INTERNACIONAL EMS");
      map.put("EZ", "OBJETO INTERNACIONAL EMS");
      map.put("FA", "FAC REGISTRADO");
      map.put("FB", "FAC REGISTRADO");
      map.put("FC", "FAC REGISTRADO (5 DIAS)");
      map.put("FD", "FAC REGISTRADO (10 DIAS)");
      map.put("FE", "ENCOMENDA FNDE");
      map.put("FF", "REGISTRADO DETRAN");
      map.put("FH", "FAC REGISTRADO COM AR DIGITAL");
      map.put("FJ", "REMESSA ECONÔMICA COM AR DIGITAL");
      map.put("FM", "FAC REGISTRADO (MONITORADO)");
      map.put("FR", "FAC REGISTRADO");
      map.put("IA", "INTEGRADA AVULSA");
      map.put("IC", "INTEGRADA A COBRAR");
      map.put("ID", "INTEGRADA DEVOLUÇÃO DE DOCUMENTO");
      map.put("IE", "INTEGRADA ESPECIAL");
      map.put("IF", "CPF");
      map.put("II", "INTEGRADA INTERNO");
      map.put("IK", "INTEGRADA COM COLETA SIMULTÂNEA");
      map.put("IM", "INTEGRADA MEDICAMENTOS");
      map.put("IN", "OBJETO DE CORRESPONDÊNCIA E EMS REC EXTERIOR");
      map.put("IP", "INTEGRADA PROGRAMADA");
      map.put("IR", "IMPRESSO REGISTRADO");
      map.put("IS", "INTEGRADA STANDARD");
      map.put("IT", "INTEGRADA TERMOLÁBIL");
      map.put("IU", "INTEGRADA URGENTE");
      map.put("IX", "CORREIOS PACKET EXPRESS");
      map.put("JA", "REMESSA ECONÔMICA COM AR DIGITAL");
      map.put("JB", "REMESSA ECONÔMICA COM AR DIGITAL");
      map.put("JC", "REMESSA ECONÔMICA COM AR DIGITAL");
      map.put("JD", "REMESSA ECONÔMICA SEM AR DIGITAL");
      map.put("JE", "REMESSA ECONÔMICA COM AR DIGITAL");
      map.put("JF", "REMESSA ECONÔMICA COM AR DIGITAL");
      map.put("JG", "REGISTRADO PRIORITÁRIO");
      map.put("JH", "REGISTRADO PRIORITÁRIO");
      map.put("JI", "REMESSA ECONÔMICA SEM AR DIGITAL");
      map.put("JJ", "REGISTRADO JUSTIÇA");
      map.put("JK", "REMESSA ECONÔMICA SEM AR DIGITAL");
      map.put("JL", "REGISTRADO LÓGICO");
      map.put("JM", "MALA DIRETA POSTAL ESPECIAL");
      map.put("JN", "MALA DIRETA POSTAL ESPECIAL");
      map.put("JO", "REGISTRADO PRIORITÁRIO");
      map.put("JP", "OBJETO RECEITA FEDERAL (EXCLUSIVO)");
      map.put("JQ", "REMESSA ECONÔMICA COM AR DIGITAL");
      map.put("JR", "REGISTRADO PRIORITÁRIO");
      map.put("JS", "REGISTRADO LÓGICO");
      map.put("JT", "REGISTRADO URGENTE");
      map.put("JU", "ETIQUETA FÍSICA REGISTRADO URGENTE");
      map.put("JV", "REMESSA ECONÔMICA COM AR DIGITAL");
      map.put("JW", "CARTA COMERCIAL A FATURAR (5 DIAS)");
      map.put("JX", "CARTA COMERCIAL A FATURAR (10 DIAS)");
      map.put("JY", "REMESSA ECONÔMICA (5 DIAS)");
      map.put("JZ", "REMESSA ECONÔMICA (10 DIAS)");
      map.put("LA", "LOGÍSTICA REVERSA SIMULTÂNEA SEDEX");
      map.put("LB", "LOGÍSTICA REVERSA SIMULTÂNEA SEDEX");
      map.put("LC", "OBJETO INTERNACIONAL PRIME");
      map.put("LD", "OBJETO INTERNACIONAL PRIME");
      map.put("LE", "LOGÍSTICA REVERSA ECONÔMICA");
      map.put("LF", "OBJETO INTERNACIONAL PRIME");
      map.put("LG", "OBJETO INTERNACIONAL PRIME");
      map.put("LH", "OBJETO INTERNACIONAL PRIME");
      map.put("LI", "OBJETO INTERNACIONAL PRIME");
      map.put("LJ", "OBJETO INTERNACIONAL PRIME");
      map.put("LK", "OBJETO INTERNACIONAL PRIME");
      map.put("LL", "OBJETO INTERNACIONAL PRIME");
      map.put("LM", "OBJETO INTERNACIONAL PRIME");
      map.put("LN", "OBJETO INTERNACIONAL PRIME");
      map.put("LP", "LOGÍSTICA REVERSA SIMULTÂNEA PAC");
      map.put("LQ", "OBJETO INTERNACIONAL PRIME");
      map.put("LS", "LOGÍSTICA REVERSA SEDEX");
      map.put("LV", "LOGÍSTICA REVERSA EXPRESSA");
      map.put("LW", "OBJETO INTERNACIONAL PRIME");
      map.put("LY", "OBJETO INTERNACIONAL PRIME");
      map.put("LZ", "OBJETO INTERNACIONAL PRIME");
      map.put("MA", "TELEGRAMA - SERVIÇOS ADICIONAIS");
      map.put("MB", "TELEGRAMA DE BALCÃO");
      map.put("MC", "TELEGRAMA FONADO");
      map.put("MD", "MÁQUINA DE FRANQUEAR (LÓGICA)");
      map.put("ME", "TELEGRAMA");
      map.put("MF", "TELEGRAMA FONADO");
      map.put("MH", "CARTA VIA INTERNET");
      map.put("MK", "TELEGRAMA CORPORATIVO");
      map.put("MM", "TELEGRAMA GRANDES CLIENTES");
      map.put("MP", "TELEGRAMA PRÉ-PAGO");
      map.put("MS", "ENCOMENDA SAÚDE");
      map.put("MT", "TELEGRAMA VIA TELEMAIL");
      map.put("MY", "TELEGRAMA INTERNACIONAL ENTRANTE");
      map.put("MZ", "TELEGRAMA VIA CORREIOS ONLINE");
      map.put("NE", "TELE SENA RESGATADA");
      map.put("NX", "CORREIOS PACKET STANDARD");
      map.put("OA", "ENCOMENDA SEDEX (ETIQUETA LÓGICA)");
      map.put("OB", "ENCOMENDA SEDEX (ETIQUETA LÓGICA)");
      map.put("OC", "ENCOMENDA SEDEX (ETIQUETA LÓGICA)");
      map.put("OD", "ENCOMENDA SEDEX (ETIQUETA FÍSICA)");
      map.put("OF", "ETIQUETA LÓGICA SEDEX");
      map.put("OG", "ETIQUETA LÓGICA SEDEX");
      map.put("OH", "ETIQUETA LÓGICA SEDEX");
      map.put("OI", "ETIQUETA LÓGICA SEDEX");
      map.put("PA", "PASSAPORTE");
      map.put("PB", "ENCOMENDA PAC - NÃO URGENTE");
      map.put("PC", "ENCOMENDA PAC A COBRAR");
      map.put("PD", "ENCOMENDA PAC");
      map.put("PE", "ENCOMENDA PAC (ETIQUETA FÍSICA)");
      map.put("PF", "PASSAPORTE");
      map.put("PG", "ENCOMENDA PAC (ETIQUETA FÍSICA)");
      map.put("PH", "ENCOMENDA PAC (ETIQUETA LÓGICA)");
      map.put("PI", "ENCOMENDA PAC");
      map.put("PJ", "ENCOMENDA PAC");
      map.put("PK", "PAC EXTRA GRANDE");
      map.put("PL", "ENCOMENDA PAC");
      map.put("PM", "ENCOMENDA PAC (ETIQUETA FÍSICA)");
      map.put("PN", "ENCOMENDA PAC (ETIQUETA LÓGICA)");
      map.put("PO", "ENCOMENDA PAC (ETIQUETA LÓGICA)");
      map.put("PP", "ETIQUETA LÓGICA PAC");
      map.put("PQ", "ETIQUETA LÓGICA PAC MINI");
      map.put("PR", "REEMBOLSO POSTAL - CLIENTE AVULSO");
      map.put("PS", "ETIQUETA LÓGICA PAC");
      map.put("PT", "ETIQUETA LÓGICA PAC");
      map.put("PU", "ETIQUETA LÓGICA PAC");
      map.put("PV", "ETIQUETA LÓGICA PAC ADMINISTRATIVO");
      map.put("PW", "ETIQUETA LÓGICA PAC");
      map.put("PX", "ETIQUETA LÓGICA PAC");
      map.put("RA", "REGISTRADO PRIORITÁRIO");
      map.put("RB", "CARTA REGISTRADA");
      map.put("RC", "CARTA REGISTRADA COM VALOR DECLARADO");
      map.put("RD", "REMESSA ECONÔMICA DETRAN");
      map.put("RE", "MALA DIRETA POSTAL ESPECIAL");
      map.put("RF", "OBJETO DA RECEITA FEDERAL");
      map.put("RG", "REGISTRADO DO SISTEMA SARA");
      map.put("RH", "REGISTRADO COM AR DIGITAL");
      map.put("RI", "REGISTRADO PRIORITÁRIO INTERNACIONAL");
      map.put("RJ", "REGISTRADO AGÊNCIA");
      map.put("RK", "REGISTRADO AGÊNCIA");
      map.put("RL", "REGISTRADO LÓGICO");
      map.put("RM", "REGISTRADO AGÊNCIA");
      map.put("RN", "REGISTRADO AGÊNCIA");
      map.put("RO", "REGISTRADO AGÊNCIA");
      map.put("RP", "REEMBOLSO POSTAL - CLIENTE INSCRITO");
      map.put("RQ", "REGISTRADO AGÊNCIA");
      map.put("RR", "REGISTRADO INTERNACIONAL");
      map.put("RS", "REMESSA ECONÔMICA ÓRGÃO TRÂNSITO COM OU SEM AR");
      map.put("RT", "REMESSA ECONÔMICA TALÃO/CARTÃO SEM AR DIGITAL");
      map.put("RU", "REGISTRADO SERVIÇO ECT");
      map.put("RV", "REMESSA ECONÔMICA CRLV/CRV/CNH COM AR DIGITAL");
      map.put("RW", "REGISTRADO INTERNACIONAL");
      map.put("RX", "REGISTRADO INTERNACIONAL");
      map.put("RY", "REMESSA ECONÔMICA TALÃO/CARTÃO COM AR DIGITAL");
      map.put("RZ", "REGISTRADO");
      map.put("SA", "ETIQUETA SEDEX AGÊNCIA");
      map.put("SB", "SEDEX 10");
      map.put("SC", "SEDEX A COBRAR");
      map.put("SD", "REMESSA EXPRESSA DETRAN");
      map.put("SE", "ENCOMENDA SEDEX");
      map.put("SF", "SEDEX AGÊNCIA");
      map.put("SG", "SEDEX DO SISTEMA SARA");
      map.put("SH", "SEDEX COM AR DIGITAL");
      map.put("SI", "SEDEX AGÊNCIA");
      map.put("SJ", "SEDEX HOJE");
      map.put("SK", "SEDEX AGÊNCIA");
      map.put("SL", "SEDEX LÓGICO");
      map.put("SM", "SEDEX 12");
      map.put("SN", "SEDEX AGÊNCIA");
      map.put("SO", "SEDEX AGÊNCIA");
      map.put("SP", "SEDEX PRÉ-FRANQUEADO");
      map.put("SQ", "SEDEX");
      map.put("SR", "SEDEX");
      map.put("SS", "SEDEX FÍSICO");
      map.put("ST", "REMESSA EXPRESSA TALÃO/CARTÃO SEM AR DIGITAL");
      map.put("SU", "ENCOMENDA SERVIÇO EXPRESSA ECT");
      map.put("SV", "REMESSA EXPRESSA CRLV/CRV/CNH COM AR DIGITAL");
      map.put("SW", "ENCOMENDA SEDEX");
      map.put("SX", "SEDEX 10");
      map.put("SY", "REMESSA EXPRESSA TALÃO/CARTÃO COM AR DIGITAL");
      map.put("SZ", "SEDEX AGÊNCIA");
      map.put("TC", "TESTE (OBJETO PARA TREINAMENTO)");
      map.put("TE", "TESTE (OBJETO PARA TREINAMENTO)");
      map.put("TR", "OBJETO TREINAMENTO - NÃO GERA PRÉ-ALERTA");
      map.put("TS", "TESTE (OBJETO PARA TREINAMENTO)");
      map.put("VA", "OBJETO INTERNACIONAL COM VALOR DECLARADO");
      map.put("VC", "OBJETO INTERNACIONAL COM VALOR DECLARADO");
      map.put("VD", "OBJETO INTERNACIONAL COM VALOR DECLARADO");
      map.put("VE", "OBJETO INTERNACIONAL COM VALOR DECLARADO");
      map.put("VF", "OBJETO INTERNACIONAL COM VALOR DECLARADO");
      map.put("VV", "OBJETO INTERNACIONAL COM VALOR DECLARADO");
      map.put("XA", "AVISO CHEGADA OBJETO INTERNACIONAL TRIBUTADO");
      map.put("XM", "SEDEX MUNDI");
      map.put("XP", "CORREIOS PACKET MINI");
      map.put("XR", "OBJETO INTERNACIONAL (PPS TRIBUTADO)");
      map.put("XX", "OBJETO INTERNACIONAL (PPS TRIBUTADO)");

      return map;
    }

    public static String getService(String cod) {
      String service = ServicesMap.get(cod.substring(0, 2));
      if (service == null) service = "Objeto postal";

      return service;
    }
  }

  public static final class Error {
    public static final String NET_ERROR = "Could not complete the request to Correios server";
  }

  public static String getLocation(ShipmentRecord record, boolean uri) {
    String info = record.getInfo();
    String loc = record.getLocal();

    if (info != null && info.startsWith("Endereço")) {
      loc = info.substring(10)+loc.substring(loc.lastIndexOf(" - "));
    }

    if (loc.charAt(loc.length()-3) == '/') {
      loc += " BRASIL";
    }

    if (uri) {
      loc = "geo:0,0?q="+loc;
    }

    return loc;
  }

  public static Intent getShareIntent(Context context, Shipment shipment) {
    String number = shipment.getNumber();
    String name = shipment.getName();
    ShipmentRecord record = shipment.getLastRecord();

    return new Intent(Intent.ACTION_SEND)
        .setType("text/plain")
        .putExtra(Intent.EXTRA_SUBJECT, context.getString(R.string.share_status_subject))
        .putExtra(Intent.EXTRA_TEXT, String.format(context.getString(R.string.share_status_text),
            name == null ? number : name + " (" + number + ")",
            record.getStatus().toLowerCase(Locale.getDefault()),
            UIUtils.getRelativeTime(record.getDate()))
        );
  }

  public static Intent getShareIntent(Context context, List<Shipment> shipments) {
    Intent shareIntent = null;

    if (shipments.size() > 0) {
      String text = "";
      for (Shipment shipment : shipments) {
        text += context.getString(shipment.isFavorite() ? R.string.text_send_list_line_1_fav : R.string.text_send_list_line_1, shipment.getNumber());
        if (shipment.getName() != null) {
          text += context.getString(R.string.text_send_list_line_2, shipment.getName());
        }
        ShipmentRecord lastRecord = shipment.getLastRecord();
        text += context.getString(R.string.text_send_list_line_3, lastRecord.getStatus(), UIUtils.getRelativeTime(lastRecord.getDate()));
      }

      shareIntent = new Intent(Intent.ACTION_SEND);
      shareIntent.setType("text/plain");
      shareIntent.putExtra(Intent.EXTRA_SUBJECT, context.getString(R.string.subject_send_list));
      shareIntent.putExtra(Intent.EXTRA_TEXT, text);
    }

    return shareIntent;
  }
}
