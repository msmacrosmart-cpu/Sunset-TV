# Sunset TV

Cliente Android Xtream para conteúdo autorizado pelo provedor do usuário. Não inclui canais, contas ou listas.

## Recursos implementados

- Login URL / Usuário / Senha / Entrar; validação da conta ativa, codificação das credenciais, mensagens de falha sem exibir senhas.
- Categorias Xtream, TV ao vivo com EPG em telas largas, filmes e séries em grade adaptativa, busca, temporadas e episódios.
- Navegação inferior em larguras menores que 600 dp; lateral em telas maiores. TV ao vivo em lista no compacto e três colunas proporcionais a partir de 840 dp. Orientação livre no celular.
- Player Media3: play/pausa, busca temporal quando permitida pelo conteúdo, áudio/qualidade/velocidade no menu nativo, legendas disponíveis, buffer, ajuste/preenchimento, nova tentativa e anterior/próximo para canais.
- Pausa ao enviar o app para segundo plano e liberação do player ao sair. Foco para navegação com controle remoto.
- Configurações persistentes para buffer e preenchimento; sair da sessão. Credenciais somente na memória.

O player não cria faixas, qualidades ou legendas que o provedor não oferece. Avançar/retroceder exige conteúdo com suporte a busca; transmissões ao vivo podem não permitir. Compatibilidade de codecs depende do aparelho.

## Compilar

JDK 17, Android SDK 34 e Gradle 8.7:

```sh
cd SunsetTV
gradle :app:assembleDebug :app:lintDebug :app:testDebugUnitTest --no-daemon
```

Saída: `SunsetTV/app/build/outputs/apk/debug/app-debug.apk`.
O workflow raiz `.github/workflows/build-apk.yml` executa esses comandos e publica `SunsetTV-debug-apk` e relatórios em Artifacts em pushes para `arena/**`, `main` e `master`, PRs e execução manual. O APK debug é assinado automaticamente para testes; distribuição em produção requer chave de release própria. O workflow dentro de `SunsetTV/.github` não é executado pelo GitHub.

## Estado de validação desta entrega

**Não há APK compilado ou teste de runtime comprovado nesta sessão.** O ambiente local não contém Java/Android SDK; duas tentativas de push para a branch da sessão foram rejeitadas pelo remoto com `fatal error in commit_refs`, impedindo iniciar CI com estas alterações. Os testes adicionados cobrem encoding e respostas de autenticação com servidor HTTP local, mas ainda precisam ser executados.

O APK MakroTV de referência não pôde ser baixado devido ao bloqueio de rede no host de assets do GitHub. Não foi decompilado nem copiado; os recursos aqui são uma implementação própria usando Xtream e Media3. Paridade visual/funcional completa com MakroTV **não foi verificada**.

## Checklist antes de distribuir

- Executar o workflow e verificar lint, testes e instalação do APK gerado.
- Usar servidor Xtream autorizado para testar conta ativa, expirada, senha com caracteres especiais, catálogo completo, erros e reconexão.
- Testar vídeo ao vivo HLS, filmes, episódios, faixas de áudio, legendas, velocidade e limites de conexão do provedor.
- Testar celular em retrato/paisagem e teclado aberto, Android TV e Fire TV Stick com controle físico (foco, OK, mídia e Voltar).
- Verificar EPG do provedor, rede lenta, app em segundo plano e ausência de conteúdo.
- Favoritos dos detalhes são persistidos, mas ainda não há uma seção dedicada de favoritos; histórico de sessão não é uma retomada persistente por posição.

Servidores HTTP são aceitos para compatibilidade com Xtream legado; prefira HTTPS porque HTTP expõe credenciais e conteúdo na rede.
