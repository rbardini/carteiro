package com.rbardini.carteiro.util;

import java.util.Map;
import java.util.TreeMap;

import com.rbardini.carteiro.PostalItem;
import com.rbardini.carteiro.R;

public final class PostalUtils {
  public static final class Category {
    public static final int RETURNED = 1;
    public static final int UNKNOWN = 2;
    public static final int IRREGULAR = 4;
    public static final int ALL = 8;
    public static final int FAVORITES = 16;
    public static final int AVAILABLE = 32;
    public static final int DELIVERED = 64;
    public static final int UNDELIVERED = 128;

    private static final Map<Integer, String[]> StatusesMap = buildStatusesMap();

    private static TreeMap<Integer, String[]> buildStatusesMap() {
      TreeMap<Integer, String[]> map = new TreeMap<Integer, String[]>();

      map.put(RETURNED, new String[] {
          Status.EM_DEVOLUCAO,
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
          Status.ENDERECO_INCOMPLETO,
          Status.ENDERECO_INCORRETO,
          Status.ENDERECO_SEM_DISTRIBUICAO,
          Status.LOGRADOURO_IRREGULAR,
          Status.ENDERECO_IRREGULAR,
          Status.NUMERO_INEXISTENTE,
          Status.EMPRESA_SEM_EXPEDIENTE,
          Status.MERCADORIA_AVARIADA,
          Status.DOCUMENTACAO_NAO_FORNECIDA,
          Status.OBJETO_FORA_PADRAO,
          Status.PEDIDO_NAO_SOLICITADO,
          Status.RECUSADO,
          Status.NAO_PROCURADO,
          Status.OBJETO_PERDIDO_ASSALTO_CARTEIRO,
          Status.OBJETO_PERDIDO_ASSALTO_VEICULO,
          Status.OBJETO_RETIDO,
          Status.IMPORTACAO_NAO_AUTORIZADA
      });
      map.put(AVAILABLE, new String[] {
          Status.AGUARDANDO_RETIRADA,
          Status.SAIU_PARA_ENTREGA,
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

    public static String[] getStatuses(int category) {
      return StatusesMap.get(category);
    }
  }

  public static final class Status {
    // Returned
    public static final String EM_DEVOLUCAO = "Em devolu��o";
    public static final String DISTRIBUIDO_AO_REMETENTE = "Distribuido ao Remetente";
    public static final String DEVOLVIDO_AO_REMETENTE = "Devolvido ao remetente";

    // Unknown
    public static final String INDETERMINADO = "Indeterminado";
    public static final String NAO_ENCONTRADO = "N�o encontrado";
    public static final String NAO_LOCALIZADO_FLUXO_POSTAL = "N�o localizado no fluxo postal";

    // Irregular
    public static final String DESTINATARIO_DESCONHECIDO_ENDERECO = "Destinat�rio desconhecido no endere�o";
    public static final String DESTINATARIO_DESCONHECIDO = "Destinat�rio desconhecido";
    public static final String DESTINATARIO_MUDOU_SE = "Destinat�rio mudou-se";
    public static final String DESTINATARIO_AUSENTE_3_TENTATIVAS = "Destinat�rio ausente em 3 tentativas de entrega";
    public static final String DESTINATARIO_RECUSOU_SE = "Destinat�rio recusou-se a receber";
    public static final String DESTINATARIO_NAO_APRESENTOU_SE = "Destinat�rio n�o apresentou-se para receber";
    public static final String ENDERECO_INSUFICIENTE = "Endere�o insuficiente para entrega";
    public static final String ENDERECO_INCOMPLETO = "Endere�o incompleto - em pesquisa";
    public static final String ENDERECO_INCORRETO = "Endere�o incorreto";
    public static final String ENDERECO_SEM_DISTRIBUICAO = "Endere�o sem distribui��o domicili�ria - Entrega interna n�o autorizada";
    public static final String LOGRADOURO_IRREGULAR = "Logradouro com numera��o irregular";
    public static final String ENDERECO_IRREGULAR = "Endere�o com numera��o irregular - Em pesquisa";
    public static final String NUMERO_INEXISTENTE = "N�o existe o n�mero indicado";
    public static final String EMPRESA_SEM_EXPEDIENTE = "Empresa sem expediente";
    public static final String MERCADORIA_AVARIADA = "Mercadoria avariada";
    public static final String DOCUMENTACAO_NAO_FORNECIDA = "Documenta��o n�o fornecida pelo destinat�rio";
    public static final String OBJETO_FORA_PADRAO = "Objeto fora do padr�o - Limites de dimens�o";
    public static final String PEDIDO_NAO_SOLICITADO = "Pedido n�o solicitado";
    public static final String RECUSADO = "Recusado";
    public static final String NAO_PROCURADO = "N�o procurado";
    public static final String OBJETO_PERDIDO_ASSALTO_CARTEIRO = "Objeto perdido em assalto ao carteiro";
    public static final String OBJETO_PERDIDO_ASSALTO_VEICULO = "Objeto perdido em assalto a ve�culo dos correios";
    public static final String OBJETO_RETIDO = "Objeto retido pelo �rg�o de fiscaliza��o";
    public static final String IMPORTACAO_NAO_AUTORIZADA = "Importa��o n�o autorizada por �rg�o da receita";

    // Available
    public static final String AGUARDANDO_RETIRADA = "Aguardando retirada";
    public static final String SAIU_PARA_ENTREGA = "Saiu para entrega";
    public static final String DISPONIVEL_EM_CAIXA_POSTAL = "Dispon�vel em caixa postal";
    public static final String DISPONIVEL_NA_CAIXA_POSTAL = "Dispon�vel na caixa postal";
    public static final String DISPONIVEL_PARA_RETIRADA_CAIXA_POSTAL = "Dispon�vel para retirada na caixa postal";

    // Delivered
    public static final String ENTREGUE = "Entregue";
    public static final String ENTREGA_EFETUADA = "Entrega Efetuada";

    // Other
    public static final String POSTADO = "Postado";
    public static final String POSTAGEM_DH = "Postagem - DH";
    public static final String ENCAMINHADO = "Encaminhado";
    public static final String MAL_ENCAMINHADO = "Mal encaminhado";
    public static final String RECEBIDO = "Recebido na unidade de distribui��o";
    public static final String CONFERIDO = "Conferido";
    public static final String DESTINATARIO_AUSENTE = "Destinat�rio ausente";
    public static final String ENTREGA_PROGRAMADA = "Entrega programada";
    public static final String DISTRIBUICAO_ESPECIAL_AGENDADA = "Distribui��o especial agendada";
    public static final String AGUARDANDO_PARTE_LOTE = "Aguardando parte do lote";
    public static final String ATRASADO = "Atrasado";
    public static final String LOG_REVERSA_SIMULTANEA = "Log. reversa simult�nea";
    public static final String REINTEGRADO = "Reintegrado";
    public static final String DEVOLVIDO_AOS_CORREIOS = "Objeto devolvido aos correios";
    public static final String SAIDA_CANCELADA = "Sa�da cancelada";
    public static final String SAIDA_NAO_EFETUADA = "Sa�da n�o efetuada";
    public static final String ENTREGA_NAO_EFETUADA = "Entrega n�o efetuada";
    public static final String ENTREGA_NAO_EFETUADA_MOTIVOS_OPERACIONAIS = "Entrega n�o efetuada por motivos operacionais";
    public static final String AGUARDANDO_DOCUMENTACAO_FISCAL = "Aguardando documenta��o fiscal";
    public static final String ENTREGA_CONDICIONADA_COMPLEMENTO_DOCUMENTACAO = "Entrega condicionada ao complemento da documenta��o";
    public static final String CANCELADA_ULTIMA_INFORMACAO = "Cancelada a �ltima informa��o";

    private static final Map<String, Integer> CategoryMap = buildCategoryMap();
    private static final Map<String, Integer> IconMap = buildIconMap();

    private static TreeMap<String, Integer> buildCategoryMap() {
      TreeMap<String, Integer> map = new TreeMap<String, Integer>(String.CASE_INSENSITIVE_ORDER);

      // Returned
      map.put(EM_DEVOLUCAO, Category.RETURNED);
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
      map.put(ENDERECO_INCOMPLETO, Category.IRREGULAR);
      map.put(ENDERECO_INCORRETO, Category.IRREGULAR);
      map.put(ENDERECO_SEM_DISTRIBUICAO , Category.IRREGULAR);
      map.put(LOGRADOURO_IRREGULAR, Category.IRREGULAR);
      map.put(ENDERECO_IRREGULAR, Category.IRREGULAR);
      map.put(NUMERO_INEXISTENTE, Category.IRREGULAR);
      map.put(EMPRESA_SEM_EXPEDIENTE, Category.IRREGULAR);
      map.put(MERCADORIA_AVARIADA, Category.IRREGULAR);
      map.put(DOCUMENTACAO_NAO_FORNECIDA, Category.IRREGULAR);
      map.put(OBJETO_FORA_PADRAO, Category.IRREGULAR);
      map.put(PEDIDO_NAO_SOLICITADO, Category.IRREGULAR);
      map.put(RECUSADO, Category.IRREGULAR);
      map.put(NAO_PROCURADO, Category.IRREGULAR);
      map.put(OBJETO_PERDIDO_ASSALTO_CARTEIRO, Category.IRREGULAR);
      map.put(OBJETO_PERDIDO_ASSALTO_VEICULO, Category.IRREGULAR);
      map.put(OBJETO_RETIDO, Category.IRREGULAR);
      map.put(IMPORTACAO_NAO_AUTORIZADA, Category.IRREGULAR);

      // Available
      map.put(AGUARDANDO_RETIRADA, Category.AVAILABLE);
      map.put(SAIU_PARA_ENTREGA, Category.AVAILABLE);
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
      map.put(ENDERECO_INCOMPLETO, R.drawable.ic_postal_endereco_insuficiente);
      map.put(ENDERECO_INCORRETO, R.drawable.ic_postal_endereco_incorreto);
      map.put(ENDERECO_SEM_DISTRIBUICAO , R.drawable.ic_postal_endereco_sem_distribuicao);
      map.put(LOGRADOURO_IRREGULAR, R.drawable.ic_postal_endereco_insuficiente);
      map.put(ENDERECO_IRREGULAR, R.drawable.ic_postal_endereco_insuficiente);
      map.put(NUMERO_INEXISTENTE, R.drawable.ic_postal_endereco_incorreto);
      map.put(EMPRESA_SEM_EXPEDIENTE, R.drawable.ic_postal_empresa_sem_expediente);
      map.put(DOCUMENTACAO_NAO_FORNECIDA, R.drawable.ic_postal_documentacao_nao_fornecida);
      map.put(OBJETO_FORA_PADRAO, R.drawable.ic_postal_objeto_fora_padrao);
      map.put(PEDIDO_NAO_SOLICITADO, R.drawable.ic_postal_pedido_nao_solicitado);
      map.put(RECUSADO, R.drawable.ic_postal_recusado);
      map.put(NAO_PROCURADO, R.drawable.ic_postal_nao_procurado);
      map.put(OBJETO_PERDIDO_ASSALTO_CARTEIRO, R.drawable.ic_postal_objeto_perdido_assalto_carteiro);
      map.put(OBJETO_PERDIDO_ASSALTO_VEICULO, R.drawable.ic_postal_objeto_perdido_assalto_veiculo);
      map.put(OBJETO_RETIDO, R.drawable.ic_postal_objeto_retido);
      map.put(IMPORTACAO_NAO_AUTORIZADA, R.drawable.ic_postal_objeto_retido);

      // Available
      map.put(AGUARDANDO_RETIRADA, R.drawable.ic_postal_aguardando_retirada);
      map.put(SAIU_PARA_ENTREGA, R.drawable.ic_postal_saiu_entrega);
      map.put(DISPONIVEL_EM_CAIXA_POSTAL, R.drawable.ic_postal_disponivel_caixa_postal);
      map.put(DISPONIVEL_NA_CAIXA_POSTAL, R.drawable.ic_postal_disponivel_caixa_postal);
      map.put(DISPONIVEL_PARA_RETIRADA_CAIXA_POSTAL, R.drawable.ic_postal_disponivel_caixa_postal);

      // Delivered
      map.put(ENTREGUE, R.drawable.ic_postal_entregue);
      map.put(ENTREGA_EFETUADA, R.drawable.ic_postal_entregue);

      // Other
      map.put(POSTADO, R.drawable.ic_postal_postado);
      map.put(POSTAGEM_DH, R.drawable.ic_postal_postado);
      map.put(RECEBIDO, R.drawable.ic_postal_recebido);
      map.put(CONFERIDO, R.drawable.ic_postal_conferido);
      map.put(ENCAMINHADO, R.drawable.ic_postal_encaminhado);
      map.put(MAL_ENCAMINHADO, R.drawable.ic_postal_mal_encaminhado);
      map.put(DESTINATARIO_AUSENTE, R.drawable.ic_postal_destinatario_ausente);
      map.put(AGUARDANDO_PARTE_LOTE, R.drawable.ic_postal_aguardando_parte_lote);
      map.put(ATRASADO, R.drawable.ic_postal_atrasado);
      map.put(LOG_REVERSA_SIMULTANEA, R.drawable.ic_postal_log_reversa_simultanea);
      map.put(REINTEGRADO, R.drawable.ic_postal_reintegrado);
      map.put(DEVOLVIDO_AOS_CORREIOS, R.drawable.ic_postal_reintegrado);
      map.put(SAIDA_CANCELADA, R.drawable.ic_postal_saida_cancelada);
      map.put(SAIDA_NAO_EFETUADA, R.drawable.ic_postal_saida_cancelada);
      map.put(ENTREGA_PROGRAMADA, R.drawable.ic_postal_entrega_programada);
      map.put(DISTRIBUICAO_ESPECIAL_AGENDADA, R.drawable.ic_postal_entrega_programada);
      map.put(ENTREGA_NAO_EFETUADA, R.drawable.ic_postal_entrega_nao_efetuada);
      map.put(ENTREGA_NAO_EFETUADA_MOTIVOS_OPERACIONAIS, R.drawable.ic_postal_entrega_nao_efetuada);
      map.put(EMPRESA_SEM_EXPEDIENTE, R.drawable.ic_postal_empresa_sem_expediente);
      map.put(DOCUMENTACAO_NAO_FORNECIDA, R.drawable.ic_postal_documentacao_nao_fornecida);
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

  public static String getLocation(PostalItem pi, boolean uri) {
    String info = pi.getInfo(), loc = pi.getLoc();

    if (info != null && info.startsWith("Endere�o")) {
      loc = info.substring(11)+loc.substring(loc.lastIndexOf(" - "));
    }
    if (loc.charAt(loc.length()-3) == '/') {
      loc += " BRASIL";
    }
    if (uri) {
      loc = "geo:0,0?q="+loc;
    }

    return loc;
  }
}
