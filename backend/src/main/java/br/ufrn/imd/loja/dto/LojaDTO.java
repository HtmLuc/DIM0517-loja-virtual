package br.ufrn.imd.loja.dto;

import java.math.BigDecimal;

import br.ufrn.imd.loja.model.Produto;

public final class LojaDTO 
{

  private interface InputDTO<T> 
  {
    T toEntity();
  }

  private interface OutputDTO<T> 
  {
    // OutputDTO<T> fromEntity(T entidade);
  }

  public record ProdutoDTO(int codigo, BigDecimal preco, int estoque) implements OutputDTO<Produto>
  {
    public static ProdutoDTO fromEntity(Produto entidade) 
    {
      return new ProdutoDTO(entidade.getCodigo(), entidade.getPreco(), entidade.getEstoque());
    }
  }

  public record CriarProdutoDTO(int codigo, BigDecimal preco) implements InputDTO<Produto>
  { 
    public Produto toEntity() 
    {
      return new Produto(codigo, preco);
    }
  }

}
