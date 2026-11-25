package br.ufrn.imd.loja.api;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import br.ufrn.imd.loja.dto.LojaDTO;
import br.ufrn.imd.loja.service.LojaService;

@RestController
@RequestMapping("/api/v1/loja")
public class LojaController {

  private final LojaService lojaService;

  public LojaController(LojaService lojaService) {
    this.lojaService = lojaService;
  }

  @PostMapping("/produto")
  public LojaDTO.ProdutoDTO cadastrarProduto(@RequestBody LojaDTO.CriarProdutoDTO criarProdutoDTO) 
  {
    return lojaService.cadastrarProduto(criarProdutoDTO);
  }
   
  @GetMapping("/produto/{codigo}/estoque")
  public LojaDTO.ProdutoEstoqueDTO consultarEstoque(@PathVariable("codigo") final int codigo) 
  {
    return lojaService.consultarEstoque(codigo);
  }

  @PutMapping("/produto/{codigo}/saida")
  public LojaDTO.ProdutoEstoqueDTO retirarEstoque(
    @PathVariable("codigo") final int codigo, @RequestBody final LojaDTO.RetirarEstoqueDTO retirarEstoqueDTO) 
  {
    return lojaService.retirarDoEstoque(codigo, retirarEstoqueDTO);
  }

  @PutMapping("/produto/{codigo}/entrada")
  public LojaDTO.ProdutoEstoqueDTO adicionarEstoque(
    @PathVariable("codigo") final int codigo, @RequestBody final LojaDTO.RetirarEstoqueDTO entradaEstoqueDTO)
  {
    return lojaService.adicionarAoEstoque(codigo, entradaEstoqueDTO);
  }
 
  @PostMapping("/venda")
  @ResponseStatus(HttpStatus.CREATED)
  public LojaDTO.VendaRespostaDTO realizarVenda(@RequestBody final LojaDTO.VendaDTO vendaDTO) 
  {
    return lojaService.realizarVenda(vendaDTO);
  }

}
