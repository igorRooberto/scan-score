<p align="center">
  <img src="./assets/Logo.png" alt="ScanScore Logo" width="250"/>
</p>

# ScanScore

> **SCAN. GRADE. DELIVER RESULTS.**

### 📝 Sobre o Projeto

O **ScanScore** é uma aplicação baseada em visão computacional projetada para automatizar a correção de provas de múltipla escolha (gabaritos), reduzindo drasticamente o tempo de trabalho manual de educadores.

### 🎯 Motivação e Impacto Social

Professores, especialmente na rede pública de ensino, perdem dezenas de horas valiosas todos os meses corrigindo provas à mão. O ScanScore nasceu para devolver esse tempo aos educadores, utilizando o Reconhecimento Óptico de Marcas (OMR) para escanear e tabular notas em segundos, permitindo que o foco principal volte a ser o ensino e o desenvolvimento dos alunos.

### 🛠️ Tecnologias Utilizadas

- **Linguagem:** Java
- **Framework Principal:** Spring Boot (Spring Web, Spring Boot DevTools)
- **Visão Computacional:** OpenCV
- **Utilitários:** Project Lombok

## 🧠 Como Funciona o Meu Motor de Visão Computacional (OMR)
Quando comecei a desenhar o núcleo de processamento de imagens do ScanScore, eu optei por não utilizar modelos pesados ou complexos de Inteligência Artificial. Em vez disso, preferi focar em Análise Geométrica e Heurísticas puras com a ajuda do OpenCV. Essa escolha manteve o sistema extremamente rápido, previsível, leve e capaz de rodar sem precisar de infraestruturas caras ou lentas.

Abaixo, eu explico passo a passo como estruturei a lógica para detectar as marcações:

### 1. Pré-processamento (A Binarização)
Primeiro, eu converto a foto original da prova para tons de cinza. Para conseguir lidar com as sombras e variações de iluminação comuns de fotos tiradas por celular, eu decidi aplicar o Threshold Adaptativo (`adaptiveThreshold`) combinado com uma operação de Morfologia (`Closing`). 

Eu escolhi essa abordagem porque o algoritmo analisa o contraste em pequenos blocos da imagem. Isso garante que mesmo as linhas mais finas de caneta não sumam se aquela parte do papel estiver na sombra. O efeito da morfologia serve para fechar os pequenos "buracos" que ficam dentro das letras impressas (A, B, C...). No fim das contas, eu transformo o papel branco em um fundo preto absoluto, onde apenas as formas sólidas brilham em branco para facilitar a matemática que vem a seguir.

### 2. A Busca pelas Formas (findContours)
Com a imagem limpa em preto e branco, eu utilizo a função `findContours` do OpenCV. Ela varre a imagem mapeando qualquer "ilha" contínua de pixels brancos. O grande desafio aqui é que a biblioteca me entrega absolutamente tudo: as letras do cabeçalho, os riscos tortos, as margens da folha e as bolinhas do gabarito.

### 3. A Minha de Validação (Filtro Heurístico)
Para ensinar o sistema a separar as bolinhas reais de todo o resto do lixo visual, eu traço um retângulo imaginário (`boundingRect`) ao redor de cada forma encontrada e aplico quatro regras de validação geométrica:
* 📐 **A Regra da Proporção (Aspect Ratio):** Eu divido a largura da forma pela altura e exijo que o resultado fique entre 0.85 e 1.15. Como um círculo perfeito cabe perfeitamente dentro de um quadrado, se a forma for esticada (como uma linha ou uma letra oval), eu a descarto na hora.
* 🔎 **Filtro Anti-Poeira:** Eu defini que a mancha precisa ter pelo menos 15x15 pixels. Assim, eu evito que o programa confunda um pingo acidental de tinta ou poeira com uma marcação.
* 🛑 **Filtro Anti-Gigantes:** Limitei o tamanho máximo em 80x80 pixels. Se passar disso, eu sei que o sistema esbarrou em alguma caixa preta do cabeçalho, borda da mesa ou QR Code.
* 🎯 **Taxa de Preenchimento (Extent):** Em vez de confiar apenas na linha de contorno externa (que pode falhar na binarização), eu calculo a porcentagem de área preenchida dentro do retângulo imaginário. Se a mancha ocupar entre 60% e 88% do quadrado delimitador, o sistema me confirma que é um círculo real.

Se a mancha passar nesses quatro testes ao mesmo tempo, eu a classifico oficialmente como uma bolinha válida e guardo suas coordenadas (X, Y).

### 4. Estruturação do Gabarito (Organização em Grid)
O OpenCV me entrega as bolinhas validadas em uma lista totalmente caótica. Para transformar ascoordenadas em uma estrutura de gabarito que faça sentido, eu desenvolvi um algoritmo de ordenação espacial:
* ⬇️ **Ordenação Vertical (Eixo Y):** Eu ordeno todas as formas de cima para baixo para garantir que a leitura comece sempre pela primeira questão da folha.
* 📏 **Agrupamento por Linha (Tolerância Y):** Como a foto do papel nunca fica 100% reta, eu defini uma tolerância de segurança de 15 pixels. Se a altura (Y) da próxima bolinha for muito próxima à da anterior, eu coloco-as na mesma linha (mesma questão). Se a diferença for grande, o sistema entende que mudou de linha.
* 🛡️ **Tolerância a Falhas (Filtro Mágico):** Para evitar que textos isolados do papel criem questões falsas, eu criei uma regra onde a linha precisa ter pelo menos 4 bolinhas (`>= 4`) para ser considerada uma questão válida. Eu optei por aceitar a partir de 4 em vez de exigir exatamente 5 para proteger o aluno, caso o flash da câmera apague completamente os contornos de uma alternativa vazia.
* ➡️ **Ordenação Horizontal (Eixo X):** Com as bolinhas agrupadas em uma questão válida, eu as ordeno da esquerda para a direita. Desse jeito, eu garanto matematicamente que o índice 0 será sempre a alternativa 'A', o 1 será a 'B', e assim por diante.

O resultado final desse passo é um mapa estruturado pronto para o teste de preenchimento.

### 5. Detecção de Tinta (Análise de Preenchimento)
Com o grid estruturado, eu preciso descobrir quais bolinhas foram pintadas pelo aluno. Para manter a performance lá no alto, eu uso matemática direta de imagem:
* ✂️ **Recorte (Region of Interest):** Para cada bolinha do grid, eu uso as coordenadas para extrair um recorte exato (`Rect`) na imagem binarizada.
* 🧮 **Contagem de Pixels:** Como a imagem está invertida (tinta = pixels brancos), eu uso a função `Core.countNonZero` para contar a quantidade exata de tinta ali dentro.
* ⚖️ **O Limiar de Aceitação:** Eu divido a quantidade de pixels brancos pela área total do retângulo. Eu defini que se a bolinha tiver **70% ou mais de preenchimento**, ela é considerada marcada.
* 👨‍🏫 **Regras de Negócio:** Aplico três regras finais para classificar o estado da questão:
  * **Em Branco (-):** Se nenhuma alternativa bateu os 70%.
  * **Anulada/Rasurada (*):** Se duas ou mais alternativas passaram do limiar (o aluno tentou marcar mais de uma ou errou e tentou rasurar).
  * **Resposta Válida (A, B, C...):** Quando apenas uma única alternativa passou no teste de preenchimento.

Depois disso, eu descarto toda a complexidade do OpenCV e entrego um dicionário de dados limpo (ex: `{1='A', 2='C', 3='-', 4='*'}`) direto para o motor de correção.

### 6. Motor de Correção (A Avaliação Final)
Aqui o processamento de imagem acaba e eu entro em lógica de negócio pura. Eu cruzo as respostas extraídas com o Gabarito Oficial enviado na requisição para gerar o boletim:
* 🛡️ **Iteração Segura:** Eu fiz o loop percorrer as chaves do **Gabarito Oficial**, e não as da leitura da imagem. Isso me garante que, se uma questão não for lida na foto por algum corte, o aluno cai em um fallback automático como "Em Branco (-)" e não é avaliado por uma prova mais curta.
* 🧮 **Cálculo da Nota:** Uso um switch expression moderno para computar acertos, erros e converter o resultado em uma nota precisa de 0.00 a 10.00, entregando tudo formatado em um DTO limpo (`ExamResultDto`).

---
