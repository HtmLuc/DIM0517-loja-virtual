package br.ufrn.imd.loja.service.exception;

public class EstoqueInsuficienteException extends RuntimeException 
{
  public EstoqueInsuficienteException(final String mensagem)
  {
    super(mensagem);
  }
  
}
