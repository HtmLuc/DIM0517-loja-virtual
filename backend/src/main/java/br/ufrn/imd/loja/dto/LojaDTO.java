package br.ufrn.imd.loja.dto;

import java.math.BigDecimal;
import java.util.List;

import br.ufrn.imd.loja.model.Produto;
import br.ufrn.imd.loja.service.exception.DadosInvalidosException;

public final class LojaDTO 
{
  private interface InputDTO<T> 
  {
    T toEntity();
    void validar();
  }

  private interface OutputDTO<T> 
  {
    // OutputDTO<T> fromEntity(T entidade);
  }

  public record ProdutoDTO(int codigo, BigDecimal preco, int estoque) implements OutputDTO<Produto>
  {
    public static ProdutoDTO fromEntity(final Produto entidade) 
    {
      return new ProdutoDTO(entidade.getCodigo(), entidade.getPreco(), entidade.getEstoque());
    }

    @Override 
    public String toString() 
    {
      return "{codigo=" + codigo + ", preco=" + preco + ", estoque=" + estoque + "}";
    }
  }

  public record CriarProdutoDTO(int codigo, BigDecimal preco) implements InputDTO<Produto>
  { 
    public Produto toEntity() 
    {
      return new Produto(codigo, preco);
    }

    public void validar() 
    {
      if (codigo == 0)
      {
        throw new DadosInvalidosException("Código 0 não é permitido.");
      }
      if (preco.intValue() < 0) 
      {
        throw new DadosInvalidosException("Preços negativos não são permitidos.");
      }
    }

    @Override 
    public String toString() 
    {
      return "{codigo=" + codigo + ", preco=" + preco + "}";
    }
  }

  public record ProdutoEstoqueDTO(int codigo, int estoque) implements OutputDTO<Produto>
  {
    public static ProdutoEstoqueDTO fromEntity(final Produto entidade) 
    {
      return new ProdutoEstoqueDTO(entidade.getCodigo(), entidade.getEstoque());
    }
  }

  public record RetirarEstoqueDTO(int quantidade) implements InputDTO<Produto>
  {
    @Override
    public Produto toEntity() 
    {
      // TODO: remover esse contrato depois, criar uma nova interface?
      return new Produto(0, null, quantidade);
    }

    @Override
    public void validar() 
    {
      if (quantidade < 0) 
      {
        throw new DadosInvalidosException("Quantidade deve ser positiva.");
      }
    }
  }
  
  public record ItemVendaDTO(int codigo, int quantidade)
  {
    public void validar() 
    {
      if (codigo == 0)
      {
        throw new DadosInvalidosException("Código 0 não é permitido.");
      }
      if (quantidade <= 0) 
      {
        throw new DadosInvalidosException("Quantidade deve ser positiva.");
      }
    }
  }

  public record VendaDTO(List<ItemVendaDTO> itens)
  {
    public void validar() 
    {
      if (itens == null || itens.isEmpty())
      {
        throw new DadosInvalidosException("A venda deve ter pelo menos um item.");
      }
      for (final ItemVendaDTO item : itens)
      {
        if (item == null)
        {
          throw new DadosInvalidosException("Item da venda inválido.");
        }
        item.validar();
      }
    }
  }

  public record ItemVendidoDTO(int codigo, int quantidade, BigDecimal subtotal, int estoque)
  {
  }

  public record VendaRespostaDTO(BigDecimal total, List<ItemVendidoDTO> itens)
  {
  }
}
