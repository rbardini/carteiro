package com.rbardini.carteiro.util;

import android.content.Context;
import android.content.Intent;

import com.rbardini.carteiro.R;
import com.rbardini.carteiro.model.PostalItem;

import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

public final class PostalUtils {
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

    private static final Map<Integer, String[]> StatusesMap = buildStatusesMap();
    private static final Map<Integer, Integer> TitleMap = buildTitleMap();
    private static final Map<Integer, Integer> ColorMap = buildColorMap();

    private static TreeMap<Integer, String[]> buildStatusesMap() {
      TreeMap<Integer, String[]> map = new TreeMap<>();

      map.put(POSTED, new String[] {
          Status.POSTADO,
          Status.POSTADO_DEPOIS_HORARIO_LIMITE,
          Status.POSTAGEM_DH
      });
      map.put(AVAILABLE, new String[] {
          Status.AGUARDANDO_RETIRADA,
          Status.SAIU_PARA_ENTREGA,
          Status.SAIU_PARA_ENTREGA_DESTINATARIO,
          Status.DISPONIVEL_EM_CAIXA_POSTAL,
          Status.DISPONIVEL_NA_CAIXA_POSTAL,
          Status.DISPONIVEL_PARA_RETIRADA_CAIXA_POSTAL
      });
      map.put(DELIVERED, new String[] {
          Status.ENTREGUE,
          Status.ENTREGA_EFETUADA
      });
      map.put(IRREGULAR, new String[] {
          Status.DESTINATARIO_DESCONHECIDO_ENDERECO,
          Status.DESTINATARIO_DESCONHECIDO,
          Status.DESTINATARIO_MUDOU_SE,
          Status.DESTINATARIO_AUSENTE_3_TENTATIVAS,
          Status.DESTINATARIO_RECUSOU_SE,
          Status.DESTINATARIO_NAO_APRESENTOU_SE,
          Status.ENDERECO_INSUFICIENTE,
          Status.MAL_ENDERECADO,
          Status.ENDERECO_INCOMPLETO,
          Status.ENDERECO_INCORRETO,
          Status.ENDERECO_SEM_DISTRIBUICAO,
          Status.DISTRIBUICAO_NAO_AUTORIZADA,
          Status.LOGRADOURO_IRREGULAR,
          Status.ENDERECO_IRREGULAR,
          Status.NUMERO_INEXISTENTE,
          Status.EMPRESA_SEM_EXPEDIENTE,
          Status.MERCADORIA_AVARIADA,
          Status.OBJETO_AVARIADO,
          Status.DOCUMENTACAO_NAO_FORNECIDA,
          Status.DESTINATARIO_NAO_APRESENTOU_DOCUMENTACAO,
          Status.OBJETO_FORA_PADRAO,
          Status.DIMENSOES_IMPOSSIBILITAM_ENTREGA,
          Status.PEDIDO_NAO_SOLICITADO,
          Status.RECUSADO,
          Status.NAO_PROCURADO,
          Status.OBJETO_PERDIDO_ASSALTO_CARTEIRO,
          Status.OBJETO_PERDIDO_ASSALTO_VEICULO,
          Status.OBJETO_PERDIDO_ASSALTO_UNIDADE,
          Status.OBJETO_RETIDO,
          Status.IMPORTACAO_NAO_AUTORIZADA,
          Status.MAL_ENCAMINHADO,
          Status.DESTINATARIO_AUSENTE,
          Status.DISTRIBUICAO_SUJEITA_PRAZO_DIFERENCIADO,
          Status.ATRASADO,
          Status.LOG_REVERSA_SIMULTANEA,
          Status.LOGISTICA_REVERSA_SIMULTANEA,
          Status.REINTEGRADO,
          Status.DEVOLVIDO_AOS_CORREIOS,
          Status.SAIDA_CANCELADA,
          Status.SAIDA_NAO_EFETUADA,
          Status.ENTREGA_NAO_EFETUADA,
          Status.ENTREGA_NAO_EFETUADA_MOTIVOS_OPERACIONAIS,
          Status.AGUARDANDO_DOCUMENTACAO_FISCAL,
          Status.ENTREGA_CONDICIONADA_COMPLEMENTO_DOCUMENTACAO,
          Status.CANCELADA_ULTIMA_INFORMACAO,
          Status.EM_REVISAO_DE_TRIBUTO,
          Status.TRIBUTO_ALTERADO
      });
      map.put(UNKNOWN, new String[] {
          Status.INDETERMINADO,
          Status.NAO_ENCONTRADO,
          Status.NAO_LOCALIZADO_FLUXO_POSTAL
      });
      map.put(RETURNED, new String[] {
          Status.EM_DEVOLUCAO,
          Status.ESTORNADO,
          Status.DISTRIBUIDO_AO_REMETENTE,
          Status.DEVOLVIDO_AO_REMETENTE
      });

      return map;
    }

    private static TreeMap<Integer, Integer> buildTitleMap() {
      TreeMap<Integer, Integer> map = new TreeMap<>();

      map.put(ALL, R.string.category_all);
      map.put(FAVORITES, R.string.category_favorites);
      map.put(AVAILABLE, R.string.category_available);
      map.put(DELIVERED, R.string.category_delivered);
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
      map.put(IRREGULAR, R.color.postal_status_irregular);
      map.put(UNKNOWN, R.color.postal_status_unknown);
      map.put(RETURNED, R.color.postal_status_returned);

      return map;
    }

    public static String[] getStatuses(int category) {
      return StatusesMap.get(category);
    }

    public static int getTitle(int category) {
      return TitleMap.get(category);
    }

    public static int getColor(int category) {
      return ColorMap.get(category);
    }
  }

  public static final class Status {
    // Posted
    public static final String POSTADO = "Postado";
    public static final String POSTAGEM_DH = "Postagem - DH";
    public static final String POSTADO_DEPOIS_HORARIO_LIMITE = "Postado depois do horário limite da agência";

    // Available
    public static final String AGUARDANDO_RETIRADA = "Aguardando retirada";
    public static final String SAIU_PARA_ENTREGA = "Saiu para entrega";
    public static final String SAIU_PARA_ENTREGA_DESTINATARIO = "Saiu para entrega ao destinatário";
    public static final String DISPONIVEL_EM_CAIXA_POSTAL = "Disponível em caixa postal";
    public static final String DISPONIVEL_NA_CAIXA_POSTAL = "Disponível na caixa postal";
    public static final String DISPONIVEL_PARA_RETIRADA_CAIXA_POSTAL = "Disponível para retirada na caixa postal";

    // Delivered
    public static final String ENTREGUE = "Entregue";
    public static final String ENTREGA_EFETUADA = "Entrega Efetuada";

    // Irregular
    public static final String DESTINATARIO_DESCONHECIDO_ENDERECO = "Destinatário desconhecido no endereço";
    public static final String DESTINATARIO_DESCONHECIDO = "Destinatário desconhecido";
    public static final String DESTINATARIO_MUDOU_SE = "Destinatário mudou-se";
    public static final String DESTINATARIO_AUSENTE_3_TENTATIVAS = "Destinatário ausente em 3 tentativas de entrega";
    public static final String DESTINATARIO_RECUSOU_SE = "Destinatário recusou-se a receber";
    public static final String DESTINATARIO_NAO_APRESENTOU_SE = "Destinatário não apresentou-se para receber";
    public static final String ENDERECO_INSUFICIENTE = "Endereço insuficiente para entrega";
    public static final String MAL_ENDERECADO = "Mal endereçado";
    public static final String ENDERECO_INCOMPLETO = "Endereço incompleto - em pesquisa";
    public static final String ENDERECO_INCORRETO = "Endereço incorreto";
    public static final String ENDERECO_SEM_DISTRIBUICAO = "Endereço sem distribuição domiciliária - Entrega interna não autorizada";
    public static final String DISTRIBUICAO_NAO_AUTORIZADA = "Distribuição não autorizada";
    public static final String LOGRADOURO_IRREGULAR = "Logradouro com numeração irregular";
    public static final String ENDERECO_IRREGULAR = "Endereço com numeração irregular - Em pesquisa";
    public static final String NUMERO_INEXISTENTE = "Não existe o número indicado";
    public static final String EMPRESA_SEM_EXPEDIENTE = "Empresa sem expediente";
    public static final String MERCADORIA_AVARIADA = "Mercadoria avariada";
    public static final String OBJETO_AVARIADO = "Objeto avariado";
    public static final String DOCUMENTACAO_NAO_FORNECIDA = "Documentação não fornecida pelo destinatário";
    public static final String DESTINATARIO_NAO_APRESENTOU_DOCUMENTACAO = "Destinatário não apresentou a documentação";
    public static final String OBJETO_FORA_PADRAO = "Objeto fora do padrão - Limites de dimensão";
    public static final String DIMENSOES_IMPOSSIBILITAM_ENTREGA = "Dimensões impossibilitam a entrega";
    public static final String PEDIDO_NAO_SOLICITADO = "Pedido não solicitado";
    public static final String RECUSADO = "Recusado";
    public static final String NAO_PROCURADO = "Não procurado";
    public static final String OBJETO_PERDIDO_ASSALTO_CARTEIRO = "Objeto perdido em assalto ao carteiro";
    public static final String OBJETO_PERDIDO_ASSALTO_VEICULO = "Objeto perdido em assalto a veículo dos correios";
    public static final String OBJETO_PERDIDO_ASSALTO_UNIDADE = "Objeto perdido em assalto a unidade dos correios";
    public static final String OBJETO_RETIDO = "Objeto retido pelo órgão de fiscalização";
    public static final String IMPORTACAO_NAO_AUTORIZADA = "Importação não autorizada por órgão da receita";
    public static final String MAL_ENCAMINHADO = "Mal encaminhado";
    public static final String DESTINATARIO_AUSENTE = "Destinatário ausente";
    public static final String DISTRIBUICAO_SUJEITA_PRAZO_DIFERENCIADO = "Área com distribuição sujeita a prazo diferenciado";
    public static final String ATRASADO = "Atrasado";
    public static final String LOG_REVERSA_SIMULTANEA = "Log. reversa simultânea";
    public static final String LOGISTICA_REVERSA_SIMULTANEA = "Logística reversa simultânea";
    public static final String REINTEGRADO = "Reintegrado";
    public static final String DEVOLVIDO_AOS_CORREIOS = "Objeto devolvido aos correios";
    public static final String SAIDA_CANCELADA = "Saída cancelada";
    public static final String SAIDA_NAO_EFETUADA = "Saída não efetuada";
    public static final String ENTREGA_NAO_EFETUADA = "Entrega não efetuada";
    public static final String ENTREGA_NAO_EFETUADA_MOTIVOS_OPERACIONAIS = "Entrega não efetuada por motivos operacionais";
    public static final String AGUARDANDO_DOCUMENTACAO_FISCAL = "Aguardando documentação fiscal";
    public static final String ENTREGA_CONDICIONADA_COMPLEMENTO_DOCUMENTACAO = "Entrega condicionada ao complemento da documentação";
    public static final String CANCELADA_ULTIMA_INFORMACAO = "Cancelada a última informação";
    public static final String EM_REVISAO_DE_TRIBUTO = "EM REVISÃO DE TRIBUTO";
    public static final String TRIBUTO_ALTERADO = "TRIBUTO ALTERADO";

    // Unknown
    public static final String INDETERMINADO = "Indeterminado";
    public static final String NAO_ENCONTRADO = "Não encontrado";
    public static final String NAO_LOCALIZADO_FLUXO_POSTAL = "Não localizado no fluxo postal";

    // Returned
    public static final String EM_DEVOLUCAO = "Em devolução";
    public static final String ESTORNADO = "Estornado";
    public static final String DISTRIBUIDO_AO_REMETENTE = "Distribuido ao Remetente";
    public static final String DEVOLVIDO_AO_REMETENTE = "Devolvido ao remetente";

    // Other
    public static final String ENCAMINHADO = "Encaminhado";
    public static final String RECEBIDO = "Recebido na unidade de distribuição";
    public static final String CONFERIDO = "Conferido";
    public static final String ENTREGA_PROGRAMADA = "Entrega programada";
    public static final String DISTRIBUICAO_ESPECIAL_AGENDADA = "Distribuição especial agendada";
    public static final String AGUARDANDO_PARTE_LOTE = "Aguardando parte do lote";
    public static final String ENTREGA_CONDICIONADA_COMPOSICAO_LOTE = "Entrega condicionada à composição do lote";

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
      map.put(POSTADO, R.drawable.ic_postal_postado);
      map.put(POSTAGEM_DH, R.drawable.ic_postal_postado);
      map.put(POSTADO_DEPOIS_HORARIO_LIMITE, R.drawable.ic_postal_postado);

      // Available
      map.put(AGUARDANDO_RETIRADA, R.drawable.ic_postal_aguardando_retirada);
      map.put(SAIU_PARA_ENTREGA, R.drawable.ic_postal_saiu_entrega);
      map.put(SAIU_PARA_ENTREGA_DESTINATARIO, R.drawable.ic_postal_saiu_entrega);
      map.put(DISPONIVEL_EM_CAIXA_POSTAL, R.drawable.ic_postal_disponivel_caixa_postal);
      map.put(DISPONIVEL_NA_CAIXA_POSTAL, R.drawable.ic_postal_disponivel_caixa_postal);
      map.put(DISPONIVEL_PARA_RETIRADA_CAIXA_POSTAL, R.drawable.ic_postal_disponivel_caixa_postal);

      // Delivered
      map.put(ENTREGUE, R.drawable.ic_postal_entregue);
      map.put(ENTREGA_EFETUADA, R.drawable.ic_postal_entregue);

      // Irregular
      map.put(DESTINATARIO_DESCONHECIDO_ENDERECO, R.drawable.ic_postal_destinatario_desconhecido);
      map.put(DESTINATARIO_DESCONHECIDO, R.drawable.ic_postal_destinatario_desconhecido);
      map.put(DESTINATARIO_MUDOU_SE, R.drawable.ic_postal_destinatario_mudou);
      map.put(DESTINATARIO_AUSENTE_3_TENTATIVAS, R.drawable.ic_postal_destinatario_ausente);
      map.put(DESTINATARIO_RECUSOU_SE, R.drawable.ic_postal_recusado);
      map.put(DESTINATARIO_NAO_APRESENTOU_SE, R.drawable.ic_postal_nao_procurado);
      map.put(ENDERECO_INSUFICIENTE, R.drawable.ic_postal_endereco_insuficiente);
      map.put(MAL_ENDERECADO, R.drawable.ic_postal_endereco_insuficiente);
      map.put(ENDERECO_INCOMPLETO, R.drawable.ic_postal_endereco_insuficiente);
      map.put(ENDERECO_INCORRETO, R.drawable.ic_postal_endereco_incorreto);
      map.put(ENDERECO_SEM_DISTRIBUICAO, R.drawable.ic_postal_endereco_sem_distribuicao);
      map.put(DISTRIBUICAO_NAO_AUTORIZADA, R.drawable.ic_postal_endereco_sem_distribuicao);
      map.put(LOGRADOURO_IRREGULAR, R.drawable.ic_postal_endereco_insuficiente);
      map.put(ENDERECO_IRREGULAR, R.drawable.ic_postal_endereco_insuficiente);
      map.put(NUMERO_INEXISTENTE, R.drawable.ic_postal_endereco_incorreto);
      map.put(EMPRESA_SEM_EXPEDIENTE, R.drawable.ic_postal_empresa_sem_expediente);
      map.put(DOCUMENTACAO_NAO_FORNECIDA, R.drawable.ic_postal_documentacao_nao_fornecida);
      map.put(DESTINATARIO_NAO_APRESENTOU_DOCUMENTACAO, R.drawable.ic_postal_documentacao_nao_fornecida);
      map.put(OBJETO_FORA_PADRAO, R.drawable.ic_postal_objeto_fora_padrao);
      map.put(DIMENSOES_IMPOSSIBILITAM_ENTREGA, R.drawable.ic_postal_objeto_fora_padrao);
      map.put(PEDIDO_NAO_SOLICITADO, R.drawable.ic_postal_pedido_nao_solicitado);
      map.put(RECUSADO, R.drawable.ic_postal_recusado);
      map.put(NAO_PROCURADO, R.drawable.ic_postal_nao_procurado);
      map.put(OBJETO_PERDIDO_ASSALTO_CARTEIRO, R.drawable.ic_postal_objeto_perdido_assalto_carteiro);
      map.put(OBJETO_PERDIDO_ASSALTO_VEICULO, R.drawable.ic_postal_objeto_perdido_assalto_veiculo);
      map.put(OBJETO_PERDIDO_ASSALTO_UNIDADE, R.drawable.ic_postal_objeto_perdido_assalto_unidade);
      map.put(OBJETO_RETIDO, R.drawable.ic_postal_objeto_retido);
      map.put(IMPORTACAO_NAO_AUTORIZADA, R.drawable.ic_postal_objeto_retido);
      map.put(MAL_ENCAMINHADO, R.drawable.ic_postal_mal_encaminhado);
      map.put(DESTINATARIO_AUSENTE, R.drawable.ic_postal_destinatario_ausente);
      map.put(DISTRIBUICAO_SUJEITA_PRAZO_DIFERENCIADO, R.drawable.ic_postal_entrega_programada);
      map.put(ATRASADO, R.drawable.ic_postal_atrasado);
      map.put(LOG_REVERSA_SIMULTANEA, R.drawable.ic_postal_log_reversa_simultanea);
      map.put(LOGISTICA_REVERSA_SIMULTANEA, R.drawable.ic_postal_log_reversa_simultanea);
      map.put(REINTEGRADO, R.drawable.ic_postal_reintegrado);
      map.put(DEVOLVIDO_AOS_CORREIOS, R.drawable.ic_postal_reintegrado);
      map.put(SAIDA_CANCELADA, R.drawable.ic_postal_saida_cancelada);
      map.put(SAIDA_NAO_EFETUADA, R.drawable.ic_postal_saida_cancelada);
      map.put(ENTREGA_NAO_EFETUADA, R.drawable.ic_postal_entrega_nao_efetuada);
      map.put(ENTREGA_NAO_EFETUADA_MOTIVOS_OPERACIONAIS, R.drawable.ic_postal_entrega_nao_efetuada);
      map.put(AGUARDANDO_DOCUMENTACAO_FISCAL, R.drawable.ic_postal_aguardando_documentacao_fiscal);
      map.put(ENTREGA_CONDICIONADA_COMPLEMENTO_DOCUMENTACAO, R.drawable.ic_postal_aguardando_documentacao_fiscal);
      map.put(CANCELADA_ULTIMA_INFORMACAO, R.drawable.ic_postal_cancelada_ultima_informacao);
      map.put(EM_REVISAO_DE_TRIBUTO, R.drawable.ic_postal_em_revisao_de_tributo);
      map.put(TRIBUTO_ALTERADO, R.drawable.ic_postal_tributo_alterado);

      // Unknown
      map.put(INDETERMINADO, R.drawable.ic_postal_indeterminado);
      map.put(NAO_ENCONTRADO, R.drawable.ic_postal_nao_encontrado);
      map.put(NAO_LOCALIZADO_FLUXO_POSTAL, R.drawable.ic_postal_nao_encontrado);

      // Returned
      map.put(EM_DEVOLUCAO, R.drawable.ic_postal_em_devolucao);
      map.put(ESTORNADO, R.drawable.ic_postal_em_devolucao);
      map.put(DISTRIBUIDO_AO_REMETENTE, R.drawable.ic_postal_distribuido_ao_remetente);
      map.put(DEVOLVIDO_AO_REMETENTE, R.drawable.ic_postal_em_devolucao);

      // Other
      map.put(RECEBIDO, R.drawable.ic_postal_recebido);
      map.put(CONFERIDO, R.drawable.ic_postal_conferido);
      map.put(ENCAMINHADO, R.drawable.ic_postal_encaminhado);
      map.put(AGUARDANDO_PARTE_LOTE, R.drawable.ic_postal_aguardando_parte_lote);
      map.put(ENTREGA_CONDICIONADA_COMPOSICAO_LOTE, R.drawable.ic_postal_aguardando_parte_lote);
      map.put(ENTREGA_PROGRAMADA, R.drawable.ic_postal_entrega_programada);
      map.put(DISTRIBUICAO_ESPECIAL_AGENDADA, R.drawable.ic_postal_entrega_programada);

      return map;
    }

    public static int getCategory(String status) {
      Integer flag = CategoryMap.get(status);
      return flag != null ? flag : 0;
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

      map.put("AL", "Agentes de leitura");
      map.put("AR", "Avisos de recebimento");
      map.put("AS", "PAC - Ação Social");
      map.put("CA", "Encomenda Internacional - Colis");
      map.put("CB", "Encomenda Internacional - Colis");
      map.put("CC", "Encomenda Internacional - Colis");
      map.put("CD", "Encomenda Internacional - Colis");
      map.put("CE", "Encomenda Internacional - Colis");
      map.put("CF", "Encomenda Internacional - Colis");
      map.put("CG", "Encomenda Internacional - Colis");
      map.put("CH", "Encomenda Internacional - Colis");
      map.put("CI", "Encomenda Internacional - Colis");
      map.put("CJ", "Encomenda Internacional - Colis");
      map.put("CK", "Encomenda Internacional - Colis");
      map.put("CL", "Encomenda Internacional - Colis");
      map.put("CM", "Encomenda Internacional - Colis");
      map.put("CN", "Encomenda Internacional - Colis");
      map.put("CO", "Encomenda Internacional - Colis");
      map.put("CP", "Encomenda Internacional - Colis");
      map.put("CQ", "Encomenda Internacional - Colis");
      map.put("CR", "Carta registrada sem Valor Declarado");
      map.put("CS", "Encomenda Internacional - Colis");
      map.put("CT", "Encomenda Internacional - Colis");
      map.put("CU", "Encomenda internacional - Colis");
      map.put("CV", "Encomenda Internacional - Colis");
      map.put("CW", "Encomenda Internacional - Colis");
      map.put("CX", "Encomenda internacional - Colis ou Selo Lacre para Caixetas");
      map.put("CY", "Encomenda Internacional - Colis");
      map.put("CZ", "Encomenda Internacional - Colis");
      map.put("DA", "SEDEX ou Remessa Expressa com AR Digital");
      map.put("DB", "SEDEX ou Remessa Expressa com AR Digital (Bradesco)");
      map.put("DC", "Remessa Expressa CRLV/CRV/CNH e Notificações");
      map.put("DD", "Devolução de documentos");
      map.put("DE", "Remessa Expressa Talão/Cartão com AR");
      map.put("DF", "e-SEDEX ");
      map.put("DG", "SEDEX");
      map.put("DI", "SEDEX ou Remessa Expressa com AR Digital (Itaú)");
      map.put("DJ", "SEDEX");
      map.put("DK", "PAC Extra Grande");
      map.put("DL", "SEDEX");
      map.put("DM", "e-SEDEX");
      map.put("DN", "SEDEX");
      map.put("DO", "SEDEX ou Remessa Expressa com AR Digital (Itaú)");
      map.put("DP", "SEDEX Pagamento na Entrega");
      map.put("DQ", "SEDEX ou Remessa Expressa com AR Digital (Santander)");
      map.put("DR", "Remessa Expressa com AR Digital (Santander)");
      map.put("DS", "SEDEX ou Remessa Expressa com AR Digital (Santander)");
      map.put("DT", "Remessa econômica com AR Digital (DETRAN)");
      map.put("DX", "SEDEX 10");
      map.put("EA", "Encomenda Internacional - EMS ");
      map.put("EB", "Encomenda Internacional - EMS ");
      map.put("EC", "PAC");
      map.put("ED", "Packet Express");
      map.put("EE", "Encomenda Internacional - EMS ");
      map.put("EF", "Encomenda Internacional - EMS ");
      map.put("EG", "Encomenda Internacional - EMS ");
      map.put("EH", "Encomenda Internacional - EMS ou Encomenda com AR Digital");
      map.put("EI", "Encomenda Internacional - EMS ");
      map.put("EJ", "Encomenda Internacional - EMS ");
      map.put("EK", "Encomenda Internacional - EMS ");
      map.put("EL", "Encomenda Internacional - EMS ");
      map.put("EM", "SEDEX Mundi ou EMS ");
      map.put("EN", "Encomenda Internacional - EMS ");
      map.put("EP", "Encomenda Internacional - EMS ");
      map.put("EQ", "Encomenda de serviço não expressa (ECT)");
      map.put("ER", "Objeto registrado");
      map.put("ES", "e-SEDEX ou EMS ");
      map.put("EU", "Encomenda Internacional - EMS ");
      map.put("EV", "Encomenda Internacional - EMS ");
      map.put("EX", "Encomenda Internacional - EMS ");
      map.put("EY", "Encomenda Internacional - EMS ");
      map.put("EZ", "Encomenda Internacional - EMS ");
      map.put("FA", "FAC registrado");
      map.put("FE", "Encomenda FNDE");
      map.put("FF", "Objeto registrado (DETRAN)");
      map.put("FH", "FAC registrado com AR Digital");
      map.put("FM", "FAC monitorado");
      map.put("FR", "FAC registrado");
      map.put("IA", "Logística Integrada (agendado / avulso)");
      map.put("IC", "Logística Integrada (a cobrar) ");
      map.put("ID", "Logística Integrada (devolução de documento)");
      map.put("IE", "Logística Integrada (Especial)");
      map.put("IF", "CPF");
      map.put("II", "Logística Integrada (ECT)");
      map.put("IK", "Logística Integraca com Coleta Simultânea");
      map.put("IM", "Logística Integrada (Medicamentos)");
      map.put("IN", "Correspondência e EMS recebido do Exterior");
      map.put("IP", "Logística Integrada (Programada)");
      map.put("IR", "Impresso Registrado");
      map.put("IS", "Logística integrada standard (medicamentos)");
      map.put("IT", "Remessa Expressa Medicamentos / Logística Integrada Termolábil");
      map.put("IU", "Logística Integrada (urgente)");
      map.put("IX", "EDEI Expresso");
      map.put("JA", "Remessa econômica com AR Digital");
      map.put("JB", "Remessa econômica com AR Digital");
      map.put("JC", "Remessa econômica com AR Digital");
      map.put("JD", "Remessa econômica Talão/Cartão");
      map.put("JE", "Remessa econômica com AR Digital");
      map.put("JF", "Remessa econômica com AR Digital");
      map.put("JG", "Objeto registrado urgente/prioritário");
      map.put("JH", "Objeto registrado urgente / prioritário");
      map.put("JI", "Remessa econômica Talão/Cartão");
      map.put("JJ", "Objeto registrado (Justiça)");
      map.put("JK", "Remessa econômica Talão/Cartão");
      map.put("JL", "Objeto registrado");
      map.put("JM", "Mala Direta Postal Especial");
      map.put("JN", "Objeto registrado econômico");
      map.put("JO", "Objeto registrado urgente");
      map.put("JP", "Receita Federal");
      map.put("JQ", "Remessa econômica com AR Digital");
      map.put("JR", "Objeto registrado urgente / prioritário");
      map.put("JS", "Objeto registrado");
      map.put("JV", "Remessa Econômica (c/ AR DIGITAL)");
      map.put("LA", "SEDEX com Logística Reversa Simultânea em Agência");
      map.put("LB", "e-SEDEX com Logística Reversa Simultânea em Agência");
      map.put("LC", "Objeto Internacional (Prime)");
      map.put("LE", "Logística Reversa Econômica");
      map.put("LF", "Objeto Internacional (Prime)");
      map.put("LI", "Objeto Internacional (Prime)");
      map.put("LJ", "Objeto Internacional (Prime)");
      map.put("LK", "Objeto Internacional (Prime)");
      map.put("LM", "Objeto Internacional (Prime)");
      map.put("LN", "Objeto Internacional (Prime)");
      map.put("LP", "PAC com Logística Reversa Simultânea em Agência");
      map.put("LS", "SEDEX Logística Reversa");
      map.put("LV", "Logística Reversa Expressa");
      map.put("LX", "Packet Standard / Econômica");
      map.put("LZ", "Objeto Internacional (Prime)");
      map.put("MA", "Serviços adicionais do Telegrama");
      map.put("MB", "Telegrama (balcão)");
      map.put("MC", "Telegrama (Fonado)");
      map.put("MD", "SEDEX Mundi (Documento interno)");
      map.put("ME", "Telegrama");
      map.put("MF", "Telegrama (Fonado)");
      map.put("MK", "Telegrama (corporativo)");
      map.put("ML", "Fecha Malas (Rabicho)");
      map.put("MM", "Telegrama (Grandes clientes)");
      map.put("MP", "Telegrama (Pré-pago)");
      map.put("MR", "AR digital");
      map.put("MS", "Encomenda Saúde");
      map.put("MT", "Telegrama (Telemail)");
      map.put("MY", "Telegrama internacional (entrante)");
      map.put("MZ", "Telegrama (Correios Online)");
      map.put("NE", "Tele Sena resgatada");
      map.put("NX", "EDEI Econômico (não urgente)");
      map.put("PA", "Passaporte");
      map.put("PB", "PAC");
      map.put("PC", "PAC a Cobrar ");
      map.put("PD", "PAC");
      map.put("PE", "PAC");
      map.put("PF", "Passaporte");
      map.put("PG", "PAC");
      map.put("PH", "PAC");
      map.put("PI", "PAC");
      map.put("PJ", "PAC");
      map.put("PK", "PAC Extra Grande");
      map.put("PR", "Reembolso Postal");
      map.put("QQ", "Objeto de teste (SIGEP Web)");
      map.put("RA", "Objeto registrado / prioritário");
      map.put("RB", "Carta registrada");
      map.put("RC", "Carta registrada com Valor Declarado");
      map.put("RD", "Remessa econômica ou objeto registrado (DETRAN) ");
      map.put("RE", "Objeto registrado econômico");
      map.put("RF", "Receita Federal");
      map.put("RG", "Objeto registrado");
      map.put("RH", "Objeto registrado com AR Digital");
      map.put("RI", "Objeto registrado internacional prioritário");
      map.put("RJ", "Objeto registrado");
      map.put("RK", "Objeto registrado");
      map.put("RL", "Objeto registrado");
      map.put("RM", "Objeto registrado urgente");
      map.put("RN", "Objeto registrado (SIGEPWEB ou Agência)");
      map.put("RO", "Objeto registrado");
      map.put("RP", "Reembolso Postal");
      map.put("RQ", "Objeto registrado");
      map.put("RR", "Objeto registrado");
      map.put("RS", "Objeto registrado");
      map.put("RT", "Remessa econômica Talão/Cartão");
      map.put("RU", "Objeto registrado (ECT)");
      map.put("RV", "Remessa econômica CRLV/CRV/CNH e Notificações com AR Digital");
      map.put("RW", "Objeto internacional");
      map.put("RX", "Objeto internacional");
      map.put("RY", "Remessa econômica Talão/Cartão com AR Digital");
      map.put("RZ", "Objeto registrado");
      map.put("SA", "SEDEX");
      map.put("SB", "SEDEX 10");
      map.put("SC", "SEDEX a cobrar");
      map.put("SD", "SEDEX ou Remessa Expressa (DETRAN)");
      map.put("SE", "SEDEX");
      map.put("SF", "SEDEX");
      map.put("SG", "SEDEX");
      map.put("SH", "SEDEX com AR Digital / SEDEX ou AR Digital");
      map.put("SI", "SEDEX");
      map.put("SJ", "SEDEX Hoje");
      map.put("SK", "SEDEX");
      map.put("SL", "SEDEX ");
      map.put("SM", "SEDEX 12");
      map.put("SN", "SEDEX ");
      map.put("SO", "SEDEX");
      map.put("SP", "SEDEX Pré-franqueado");
      map.put("SQ", "SEDEX");
      map.put("SR", "SEDEX");
      map.put("SS", "SEDEX");
      map.put("ST", "Remessa Expressa Talão/Cartão");
      map.put("SU", "Encomenda de serviço expressa (ECT)");
      map.put("SV", "Remessa Expressa CRLV/CRV/CNH e Notificações com AR Digital");
      map.put("SW", "e-SEDEX");
      map.put("SX", "SEDEX 10");
      map.put("SY", "Remessa Expressa Talão/Cartão com AR Digital");
      map.put("SZ", "SEDEX");
      map.put("TC", "Objeto para treinamento");
      map.put("TE", "Objeto para treinamento");
      map.put("TS", "Objeto para treinamento");
      map.put("VA", "Encomendas com valor declarado");
      map.put("VC", "Encomendas");
      map.put("VD", "Encomendas com valor declarado");
      map.put("VE", "Encomendas");
      map.put("VF", "Encomendas com valor declarado");
      map.put("VV", "Objeto internacional");
      map.put("XA", "Aviso de chegada (internacional)");
      map.put("XM", "SEDEX Mundi");
      map.put("XR", "Encomenda SUR Postal Expresso");
      map.put("XX", "Encomenda SUR Postal 24 horas");

      return map;
    }

    public static String getService(String cod) {
      String service = ServicesMap.get(cod.substring(0, 2));
      if (service == null) service = "Serviço desconhecido";

      return service;
    }
  }

  public static final class Error {
    public static final String NOT_FOUND = "O sistema dos Correios não possui dados sobre o objeto informado";
    public static final String NET_ERROR = "Não foi possível obter contato com o site";
  }

  public static String getLocation(PostalItem pi, boolean uri) {
    String info = pi.getInfo(), loc = pi.getLoc();

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

  public static Intent getShareIntent(Context context, PostalItem pi) {
    return new Intent(Intent.ACTION_SEND)
        .setType("text/plain")
        .putExtra(Intent.EXTRA_SUBJECT, context.getString(R.string.share_status_subject))
        .putExtra(Intent.EXTRA_TEXT, String.format(context.getString(R.string.share_status_text),
            pi.getFullDesc(), pi.getStatus().toLowerCase(Locale.getDefault()), UIUtils.getRelativeTime(pi.getDate())));
  }

  public static Intent getShareIntent(Context context, List<PostalItem> list) {
    Intent shareIntent = null;

    if (list.size() > 0) {
      String text = "";
      for (PostalItem pi : list) {
        text += context.getString(pi.isFav() ? R.string.text_send_list_line_1_fav : R.string.text_send_list_line_1, pi.getCod());
        if (pi.getDesc() != null) {
          text += context.getString(R.string.text_send_list_line_2, pi.getDesc());
        }
        text += context.getString(R.string.text_send_list_line_3, pi.getStatus(), UIUtils.getRelativeTime(pi.getDate()));
      }

      shareIntent = new Intent(Intent.ACTION_SEND);
      shareIntent.setType("text/plain");
      shareIntent.putExtra(Intent.EXTRA_SUBJECT, context.getString(R.string.subject_send_list));
      shareIntent.putExtra(Intent.EXTRA_TEXT, text);
    }

    return shareIntent;
  }
}
