package com.rbardini.carteiro.util;

import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;
import android.content.Context;
import android.content.Intent;
import com.rbardini.carteiro.R;
import com.rbardini.carteiro.model.PostalItem;

public final class PostalUtils {
  public static final class Category {
    public static final int RETURNED    = 0x1;
    public static final int UNKNOWN     = 0x2;
    public static final int IRREGULAR   = 0x4;
    public static final int ALL         = 0x8;
    public static final int FAVORITES   = 0x10;
    public static final int AVAILABLE   = 0x20;
    public static final int DELIVERED   = 0x40;
    public static final int UNDELIVERED = 0x80;

    private static final Map<Integer, String[]> StatusesMap = buildStatusesMap();
    private static final Map<Integer, Integer> TitleMap = buildTitleMap();
    private static final Map<Integer, Integer> IconMap = buildIconMap();

    private static TreeMap<Integer, String[]> buildStatusesMap() {
      TreeMap<Integer, String[]> map = new TreeMap<Integer, String[]>();

      map.put(RETURNED, new String[] {
          Status.EM_DEVOLUCAO,
          Status.ESTORNADO,
          Status.DISTRIBUIDO_AO_REMETENTE,
          Status.DEVOLVIDO_AO_REMETENTE
      });
      map.put(UNKNOWN, new String[] {
          Status.INDETERMINADO,
          Status.NAO_ENCONTRADO,
          Status.NAO_LOCALIZADO_FLUXO_POSTAL
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
          Status.IMPORTACAO_NAO_AUTORIZADA
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

      return map;
    }

    private static TreeMap<Integer, Integer> buildIconMap() {
      TreeMap<Integer, Integer> map = new TreeMap<Integer, Integer>();

      map.put(ALL, R.drawable.ic_action_all);
      map.put(FAVORITES, R.drawable.ic_action_star);
      map.put(AVAILABLE, R.drawable.ic_action_time);
      map.put(DELIVERED, R.drawable.ic_action_accept);
      map.put(IRREGULAR, R.drawable.ic_action_warning);
      map.put(UNKNOWN, R.drawable.ic_action_help);
      map.put(RETURNED, R.drawable.ic_action_undo);

      return map;
    }

    private static TreeMap<Integer, Integer> buildTitleMap() {
      TreeMap<Integer, Integer> map = new TreeMap<Integer, Integer>();

      map.put(ALL, R.string.category_all);
      map.put(FAVORITES, R.string.category_favorites);
      map.put(AVAILABLE, R.string.category_available);
      map.put(DELIVERED, R.string.category_delivered);
      map.put(IRREGULAR, R.string.category_irregular);
      map.put(UNKNOWN, R.string.category_unknown);
      map.put(RETURNED, R.string.category_returned);

      return map;
    }

    public static String[] getStatuses(int category) {
      return StatusesMap.get(category);
    }

    public static int getTitle(int category) {
      return TitleMap.get(category);
    }

    public static int getIcon(int category) {
      return IconMap.get(category);
    }
  }

  public static final class Status {
    // Returned
    public static final String EM_DEVOLUCAO = "Em devolução";
    public static final String ESTORNADO = "Estornado";
    public static final String DISTRIBUIDO_AO_REMETENTE = "Distribuido ao Remetente";
    public static final String DEVOLVIDO_AO_REMETENTE = "Devolvido ao remetente";

    // Unknown
    public static final String INDETERMINADO = "Indeterminado";
    public static final String NAO_ENCONTRADO = "Não encontrado";
    public static final String NAO_LOCALIZADO_FLUXO_POSTAL = "Não localizado no fluxo postal";

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

    // Other
    public static final String POSTADO = "Postado";
    public static final String POSTAGEM_DH = "Postagem - DH";
    public static final String POSTADO_DEPOIS_HORARIO_LIMITE = "Postado depois do horário limite da agência";
    public static final String ENCAMINHADO = "Encaminhado";
    public static final String MAL_ENCAMINHADO = "Mal encaminhado";
    public static final String RECEBIDO = "Recebido na unidade de distribuição";
    public static final String CONFERIDO = "Conferido";
    public static final String DESTINATARIO_AUSENTE = "Destinatário ausente";
    public static final String ENTREGA_PROGRAMADA = "Entrega programada";
    public static final String DISTRIBUICAO_ESPECIAL_AGENDADA = "Distribuição especial agendada";
    public static final String AGUARDANDO_PARTE_LOTE = "Aguardando parte do lote";
    public static final String ENTREGA_CONDICIONADA_COMPOSICAO_LOTE = "Entrega condicionada à composição do lote";
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

    private static final Map<String, Integer> CategoryMap = buildCategoryMap();
    private static final Map<String, Integer> IconMap = buildIconMap();

    private static TreeMap<String, Integer> buildCategoryMap() {
      TreeMap<String, Integer> map = new TreeMap<String, Integer>(String.CASE_INSENSITIVE_ORDER);

      // Returned
      map.put(EM_DEVOLUCAO, Category.RETURNED);
      map.put(ESTORNADO, Category.RETURNED);
      map.put(DISTRIBUIDO_AO_REMETENTE, Category.RETURNED);
      map.put(DEVOLVIDO_AO_REMETENTE, Category.RETURNED);

      // Unknown
      map.put(INDETERMINADO, Category.UNKNOWN);
      map.put(NAO_ENCONTRADO, Category.UNKNOWN);
      map.put(NAO_LOCALIZADO_FLUXO_POSTAL, Category.UNKNOWN);

      // Irregular
      map.put(DESTINATARIO_DESCONHECIDO_ENDERECO, Category.IRREGULAR);
      map.put(DESTINATARIO_DESCONHECIDO, Category.IRREGULAR);
      map.put(DESTINATARIO_MUDOU_SE, Category.IRREGULAR);
      map.put(DESTINATARIO_AUSENTE_3_TENTATIVAS, Category.IRREGULAR);
      map.put(DESTINATARIO_RECUSOU_SE, Category.IRREGULAR);
      map.put(DESTINATARIO_NAO_APRESENTOU_SE, Category.IRREGULAR);
      map.put(ENDERECO_INSUFICIENTE, Category.IRREGULAR);
      map.put(MAL_ENDERECADO, Category.IRREGULAR);
      map.put(ENDERECO_INCOMPLETO, Category.IRREGULAR);
      map.put(ENDERECO_INCORRETO, Category.IRREGULAR);
      map.put(ENDERECO_SEM_DISTRIBUICAO, Category.IRREGULAR);
      map.put(DISTRIBUICAO_NAO_AUTORIZADA, Category.IRREGULAR);
      map.put(LOGRADOURO_IRREGULAR, Category.IRREGULAR);
      map.put(ENDERECO_IRREGULAR, Category.IRREGULAR);
      map.put(NUMERO_INEXISTENTE, Category.IRREGULAR);
      map.put(EMPRESA_SEM_EXPEDIENTE, Category.IRREGULAR);
      map.put(MERCADORIA_AVARIADA, Category.IRREGULAR);
      map.put(DOCUMENTACAO_NAO_FORNECIDA, Category.IRREGULAR);
      map.put(DESTINATARIO_NAO_APRESENTOU_DOCUMENTACAO, Category.IRREGULAR);
      map.put(OBJETO_FORA_PADRAO, Category.IRREGULAR);
      map.put(DIMENSOES_IMPOSSIBILITAM_ENTREGA, Category.IRREGULAR);
      map.put(PEDIDO_NAO_SOLICITADO, Category.IRREGULAR);
      map.put(RECUSADO, Category.IRREGULAR);
      map.put(NAO_PROCURADO, Category.IRREGULAR);
      map.put(OBJETO_PERDIDO_ASSALTO_CARTEIRO, Category.IRREGULAR);
      map.put(OBJETO_PERDIDO_ASSALTO_VEICULO, Category.IRREGULAR);
      map.put(OBJETO_PERDIDO_ASSALTO_UNIDADE, Category.IRREGULAR);
      map.put(OBJETO_RETIDO, Category.IRREGULAR);
      map.put(IMPORTACAO_NAO_AUTORIZADA, Category.IRREGULAR);

      // Available
      map.put(AGUARDANDO_RETIRADA, Category.AVAILABLE);
      map.put(SAIU_PARA_ENTREGA, Category.AVAILABLE);
      map.put(SAIU_PARA_ENTREGA_DESTINATARIO, Category.AVAILABLE);
      map.put(DISPONIVEL_EM_CAIXA_POSTAL, Category.AVAILABLE);
      map.put(DISPONIVEL_NA_CAIXA_POSTAL, Category.AVAILABLE);
      map.put(DISPONIVEL_PARA_RETIRADA_CAIXA_POSTAL, Category.AVAILABLE);

      // Delivered
      map.put(ENTREGUE, Category.DELIVERED);
      map.put(ENTREGA_EFETUADA, Category.DELIVERED);

      return map;
    }

    private static TreeMap<String, Integer> buildIconMap() {
      TreeMap<String, Integer> map = new TreeMap<String, Integer>(String.CASE_INSENSITIVE_ORDER);

      // Returned
      map.put(EM_DEVOLUCAO, R.drawable.ic_postal_em_devolucao);
      map.put(ESTORNADO, R.drawable.ic_postal_em_devolucao);
      map.put(DISTRIBUIDO_AO_REMETENTE, R.drawable.ic_postal_distribuido_ao_remetente);
      map.put(DEVOLVIDO_AO_REMETENTE, R.drawable.ic_postal_em_devolucao);

      // Unknown
      map.put(INDETERMINADO, R.drawable.ic_postal_indeterminado);
      map.put(NAO_ENCONTRADO, R.drawable.ic_postal_nao_encontrado);
      map.put(NAO_LOCALIZADO_FLUXO_POSTAL, R.drawable.ic_postal_nao_encontrado);

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

      // Other
      map.put(POSTADO, R.drawable.ic_postal_postado);
      map.put(POSTAGEM_DH, R.drawable.ic_postal_postado);
      map.put(POSTADO_DEPOIS_HORARIO_LIMITE, R.drawable.ic_postal_postado);
      map.put(RECEBIDO, R.drawable.ic_postal_recebido);
      map.put(CONFERIDO, R.drawable.ic_postal_conferido);
      map.put(ENCAMINHADO, R.drawable.ic_postal_encaminhado);
      map.put(MAL_ENCAMINHADO, R.drawable.ic_postal_mal_encaminhado);
      map.put(DESTINATARIO_AUSENTE, R.drawable.ic_postal_destinatario_ausente);
      map.put(AGUARDANDO_PARTE_LOTE, R.drawable.ic_postal_aguardando_parte_lote);
      map.put(ENTREGA_CONDICIONADA_COMPOSICAO_LOTE, R.drawable.ic_postal_aguardando_parte_lote);
      map.put(ATRASADO, R.drawable.ic_postal_atrasado);
      map.put(LOG_REVERSA_SIMULTANEA, R.drawable.ic_postal_log_reversa_simultanea);
      map.put(LOGISTICA_REVERSA_SIMULTANEA, R.drawable.ic_postal_log_reversa_simultanea);
      map.put(REINTEGRADO, R.drawable.ic_postal_reintegrado);
      map.put(DEVOLVIDO_AOS_CORREIOS, R.drawable.ic_postal_reintegrado);
      map.put(SAIDA_CANCELADA, R.drawable.ic_postal_saida_cancelada);
      map.put(SAIDA_NAO_EFETUADA, R.drawable.ic_postal_saida_cancelada);
      map.put(ENTREGA_PROGRAMADA, R.drawable.ic_postal_entrega_programada);
      map.put(DISTRIBUICAO_ESPECIAL_AGENDADA, R.drawable.ic_postal_entrega_programada);
      map.put(ENTREGA_NAO_EFETUADA, R.drawable.ic_postal_entrega_nao_efetuada);
      map.put(ENTREGA_NAO_EFETUADA_MOTIVOS_OPERACIONAIS, R.drawable.ic_postal_entrega_nao_efetuada);
      map.put(EMPRESA_SEM_EXPEDIENTE, R.drawable.ic_postal_empresa_sem_expediente);
      map.put(AGUARDANDO_DOCUMENTACAO_FISCAL, R.drawable.ic_postal_aguardando_documentacao_fiscal);
      map.put(ENTREGA_CONDICIONADA_COMPLEMENTO_DOCUMENTACAO, R.drawable.ic_postal_aguardando_documentacao_fiscal);
      map.put(CANCELADA_ULTIMA_INFORMACAO, R.drawable.ic_postal_cancelada_ultima_informacao);

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
      TreeMap<String, String> map = new TreeMap<String, String>(String.CASE_INSENSITIVE_ORDER);

      map.put("AL", "AGENTES DE LEITURA");
      map.put("AR", "AVISO DE RECEBIMENTO");
      map.put("AS", "ENCOMENDA PAC – AÇÃO SOCIAL");
      map.put("CA", "OBJETO INTERNACIONAL");
      map.put("CB", "OBJETO INTERNACIONAL");
      map.put("CC", "COLIS POSTAUX");
      map.put("CD", "OBJETO INTERNACIONAL");
      map.put("CE", "OBJETO INTERNACIONAL");
      map.put("CF", "OBJETO INTERNACIONAL");
      map.put("CG", "OBJETO INTERNACIONAL");
      map.put("CH", "OBJETO INTERNACIONAL");
      map.put("CI", "OBJETO INTERNACIONAL");
      map.put("CJ", "REGISTRADO INTERNACIONAL");
      map.put("CK", "OBJETO INTERNACIONAL");
      map.put("CL", "OBJETO INTERNACIONAL");
      map.put("CM", "OBJETO INTERNACIONAL");
      map.put("CN", "OBJETO INTERNACIONAL");
      map.put("CO", "OBJETO INTERNACIONAL");
      map.put("CP", "COLIS POSTAUX");
      map.put("CQ", "OBJETO INTERNACIONAL");
      map.put("CR", "CARTA REGISTRADA SEM VALOR DECLARADO");
      map.put("CS", "OBJETO INTERNACIONAL");
      map.put("CT", "OBJETO INTERNACIONAL");
      map.put("CU", "OBJETO INTERNACIONAL");
      map.put("CV", "REGISTRADO INTERNACIONAL");
      map.put("CW", "OBJETO INTERNACIONAL");
      map.put("CX", "OBJETO INTERNACIONAL");
      map.put("CY", "OBJETO INTERNACIONAL");
      map.put("CZ", "OBJETO INTERNACIONAL");
      map.put("DA", "REM EXPRES COM AR DIGITAL");
      map.put("DB", "REM EXPRES COM AR DIGITAL BRADESCO");
      map.put("DC", "REM EXPRESSA CRLV/CRV/CNH e NOTIFICAÇÃO");
      map.put("DD", "DEVOLUÇÃO DE DOCUMENTOS");
      map.put("DE", "REMESSA EXPRESSA TALÃO E CARTÃO C/ AR");
      map.put("DF", "E-SEDEX (LÓGICO)");
      map.put("DG", "E-SEDEX (LÓGICO)");
      map.put("DI", "REM EXPRES COM AR DIGITAL ITAU");
      map.put("DL", "ENCOMENDA SEDEX (LÓGICO)");
      map.put("DP", "REM EXPRES COM AR DIGITAL PRF");
      map.put("DS", "REM EXPRES COM AR DIGITAL SANTANDER");
      map.put("DT", "REMESSA ECON.SEG.TRANSITO C/AR DIGITAL");
      map.put("DX", "ENCOMENDA SEDEX 10 (LÓGICO)");
      map.put("EA", "OBJETO INTERNACIONAL");
      map.put("EB", "OBJETO INTERNACIONAL");
      map.put("EC", "ENCOMENDA PAC");
      map.put("ED", "OBJETO INTERNACIONAL");
      map.put("EE", "SEDEX INTERNACIONAL");
      map.put("EF", "OBJETO INTERNACIONAL");
      map.put("EG", "OBJETO INTERNACIONAL");
      map.put("EH", "ENCOMENDA NORMAL COM AR DIGITAL");
      map.put("EI", "OBJETO INTERNACIONAL");
      map.put("EJ", "ENCOMENDA INTERNACIONAL");
      map.put("EK", "OBJETO INTERNACIONAL");
      map.put("EL", "OBJETO INTERNACIONAL");
      map.put("EM", "OBJETO INTERNACIONAL");
      map.put("EN", "ENCOMENDA NORMAL NACIONAL");
      map.put("EO", "OBJETO INTERNACIONAL");
      map.put("EP", "OBJETO INTERNACIONAL");
      map.put("EQ", "ENCOMENDA SERVIÇO NÃO EXPRESSA ECT");
      map.put("ER", "REGISTRADO");
      map.put("ES", "e-SEDEX");
      map.put("ET", "OBJETO INTERNACIONAL");
      map.put("EU", "OBJETO INTERNACIONAL");
      map.put("EV", "OBJETO INTERNACIONAL");
      map.put("EW", "OBJETO INTERNACIONAL");
      map.put("EX", "OBJETO INTERNACIONAL");
      map.put("EY", "OBJETO INTERNACIONAL");
      map.put("EZ", "OBJETO INTERNACIONAL");
      map.put("FA", "FAC REGISTRATO (LÓGICO)");
      map.put("FE", "ENCOMENDA FNDE");
      map.put("FF", "REGISTRADO DETRAN");
      map.put("FH", "REGISTRADO FAC COM AR DIGITAL");
      map.put("FM", "REGISTRADO - FAC MONITORADO");
      map.put("FR", "REGISTRADO FAC");
      map.put("IA", "INTEGRADA AVULSA");
      map.put("IC", "INTEGRADA A COBRAR");
      map.put("ID", "INTEGRADA DEVOLUCAO DE DOCUMENTO");
      map.put("IE", "INTEGRADA ESPECIAL");
      map.put("IF", "CPF");
      map.put("II", "INTEGRADA INTERNO");
      map.put("IK", "INTEGRADA COM COLETA SIMULTANEA");
      map.put("IM", "INTEGRADA MEDICAMENTOS");
      map.put("IN", "OBJ DE CORRESP E EMS REC EXTERIOR");
      map.put("IP", "INTEGRADA PROGRAMADA");
      map.put("IR", "IMPRESSO REGISTRADO");
      map.put("IS", "INTEGRADA STANDARD");
      map.put("IT", "INTEGRADO TERMOLÁBIL");
      map.put("IU", "INTEGRADA URGENTE");
      map.put("JA", "REMESSA ECONOMICA C/AR DIGITAL");
      map.put("JB", "REMESSA ECONOMICA C/AR DIGITAL");
      map.put("JC", "REMESSA ECONOMICA C/AR DIGITAL");
      map.put("JD", "REMESSA ECONÔMICA S/ AR DIGITAL");
      map.put("JE", "REMESSA ECONÔMICA C/ AR DIGITAL");
      map.put("JG", "REGISTRATO AGÊNCIA (FÍSICO)");
      map.put("JJ", "REGISTRADO JUSTIÇA");
      map.put("JL", "OBJETO REGISTRADO (LÓGICO)");
      map.put("JM", "MALA DIRETA POSTAL ESPECIAL (LÓGICO)");
      map.put("LA", "LOGÍSTICA REVERSA SIMULTÂNEA - ENCOMENDA SEDEX (AGÊNCIA)");
      map.put("LB", "LOGÍSTICA REVERSA SIMULTÂNEA - ENCOMENDA e-SEDEX (AGÊNCIA)");
      map.put("LC", "CARTA EXPRESSA");
      map.put("LE", "LOGÍSTICA REVERSA ECONOMICA");
      map.put("LP", "LOGÍSTICA REVERSA SIMULTÂNEA - ENCOMENDA PAC (AGÊNCIA)");
      map.put("LS", "LOGISTICA REVERSA SEDEX");
      map.put("LV", "LOGISTICA REVERSA EXPRESSA");
      map.put("LX", "CARTA EXPRESSA");
      map.put("LY", "CARTA EXPRESSA");
      map.put("MA", "SERVIÇOS ADICIONAIS");
      map.put("MB", "TELEGRAMA DE BALCÃO");
      map.put("MC", "MALOTE CORPORATIVO");
      map.put("ME", "TELEGRAMA");
      map.put("MF", "TELEGRAMA FONADO");
      map.put("MK", "TELEGRAMA CORPORATIVO");
      map.put("MM", "TELEGRAMA GRANDES CLIENTES");
      map.put("MP", "TELEGRAMA PRÉ-PAGO");
      map.put("MS", "ENCOMENDA SAUDE");
      map.put("MT", "TELEGRAMA VIA TELEMAIL");
      map.put("MY", "TELEGRAMA INTERNACIONAL ENTRANTE");
      map.put("MZ", "TELEGRAMA VIA CORREIOS ON LINE");
      map.put("NE", "TELE SENA RESGATADA");
      map.put("PA", "PASSAPORTE");
      map.put("PB", "ENCOMENDA PAC - NÃO URGENTE");
      map.put("PC", "ENCOMENDA PAC A COBRAR");
      map.put("PD", "ENCOMENDA PAC - NÃO URGENTE");
      map.put("PF", "PASSAPORTE");
      map.put("PG", "ENCOMENDA PAC (ETIQUETA FÍSICA)");
      map.put("PH", "ENCOMENDA PAC (ETIQUETA LÓGICA)");
      map.put("PR", "REEMBOLSO POSTAL - CLIENTE AVULSO");
      map.put("RA", "REGISTRADO PRIORITÁRIO");
      map.put("RB", "CARTA REGISTRADA");
      map.put("RC", "CARTA REGISTRADA COM VALOR DECLARADO");
      map.put("RD", "REMESSA ECONOMICA DETRAN");
      map.put("RE", "REGISTRADO ECONÔMICO");
      map.put("RF", "OBJETO DA RECEITA FEDERAL");
      map.put("RG", "REGISTRADO DO SISTEMA SARA");
      map.put("RH", "REGISTRADO COM AR DIGITAL");
      map.put("RI", "REGISTRADO");
      map.put("RJ", "REGISTRADO AGÊNCIA");
      map.put("RK", "REGISTRADO AGÊNCIA");
      map.put("RL", "REGISTRADO LÓGICO");
      map.put("RM", "REGISTRADO AGÊNCIA");
      map.put("RN", "REGISTRADO AGÊNCIA");
      map.put("RO", "REGISTRADO AGÊNCIA");
      map.put("RP", "REEMBOLSO POSTAL - CLIENTE INSCRITO");
      map.put("RQ", "REGISTRADO AGÊNCIA");
      map.put("RR", "CARTA REGISTRADA SEM VALOR DECLARADO");
      map.put("RS", "REGISTRADO LÓGICO");
      map.put("RT", "REM ECON TALAO/CARTAO SEM AR DIGITAL");
      map.put("RU", "REGISTRADO SERVIÇO ECT");
      map.put("RV", "REM ECON CRLV/CRV/CNH COM AR DIGITAL");
      map.put("RY", "REM ECON TALAO/CARTAO COM AR DIGITAL");
      map.put("RZ", "REGISTRADO");
      map.put("SA", "SEDEX ANOREG");
      map.put("SB", "SEDEX 10 AGÊNCIA (FÍSICO)");
      map.put("SC", "SEDEX A COBRAR");
      map.put("SD", "REMESSA EXPRESSA DETRAN");
      map.put("SE", "ENCOMENDA SEDEX");
      map.put("SF", "SEDEX AGÊNCIA");
      map.put("SG", "SEDEX DO SISTEMA SARA");
      map.put("SI", "SEDEX AGÊNCIA");
      map.put("SJ", "SEDEX HOJE");
      map.put("SK", "SEDEX AGÊNCIA");
      map.put("SL", "SEDEX LÓGICO");
      map.put("SM", "SEDEX MESMO DIA");
      map.put("SN", "SEDEX COM VALOR DECLARADO");
      map.put("SO", "SEDEX AGÊNCIA");
      map.put("SP", "SEDEX PRÉ-FRANQUEADO");
      map.put("SQ", "SEDEX");
      map.put("SR", "SEDEX");
      map.put("SS", "SEDEX FÍSICO");
      map.put("ST", "REM EXPRES TALAO/CARTAO SEM AR DIGITAL");
      map.put("SU", "ENCOMENDA SERVIÇO EXPRESSA ECT");
      map.put("SV", "REM EXPRES CRLV/CRV/CNH COM AR DIGITAL");
      map.put("SW", "e-SEDEX");
      map.put("SX", "SEDEX 10");
      map.put("SY", "REM EXPRES TALAO/CARTAO COM AR DIGITAL");
      map.put("SZ", "SEDEX AGÊNCIA");
      map.put("TE", "TESTE (OBJETO PARA TREINAMENTO)");
      map.put("TS", "TESTE (OBJETO PARA TREINAMENTO)");
      map.put("VA", "ENCOMENDAS COM VALOR DECLARADO");
      map.put("VC", "ENCOMENDAS");
      map.put("VD", "ENCOMENDAS COM VALOR DECLARADO");
      map.put("VE", "ENCOMENDAS");
      map.put("VF", "ENCOMENDAS COM VALOR DECLARADO");
      map.put("XM", "SEDEX MUNDI");
      map.put("XR", "ENCOMENDA SUR POSTAL EXPRESSO");
      map.put("XX", "ENCOMENDA SUR POSTAL 24 HORAS");;

      return map;
    }

    public static String getService(String cod) {
      String service = ServicesMap.get(cod.substring(0, 2));
      if (service == null) service = "SERVIÇO DESCONHECIDO";

      return service;
    }
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
      for (int i=0; i<list.size(); i++) {
        PostalItem pi = list.get(i);
        text += String.format(context.getString(pi.isFav() ? R.string.text_send_list_line_1_fav : R.string.text_send_list_line_1, pi.getCod()));
        if (pi.getDesc() != null) { text += String.format(context.getString(R.string.text_send_list_line_2, pi.getDesc())); }
        text += String.format(context.getString(R.string.text_send_list_line_3, pi.getStatus(), UIUtils.getRelativeTime(pi.getDate())));
      }

      shareIntent = new Intent(Intent.ACTION_SEND);
      shareIntent.setType("text/plain");
      shareIntent.putExtra(Intent.EXTRA_SUBJECT, context.getString(R.string.subject_send_list));
      shareIntent.putExtra(Intent.EXTRA_TEXT, text);
    }

    return shareIntent;
  }
}
