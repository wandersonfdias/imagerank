#!/bin/bash
arquivoLog="gera_base_treino.log"
diretorioBase="$PWD/base_completa"
diretorioImagens="imagens"
diretorioDescritores="descritores"
diretorioPares="pares"
diretorioLACDataset="lac_dataset"
arquivoParesOriginal="$PWD/pares_originais/tag-classes-reduced.dat"
totalParesGerar=1000000

java -Xms1536M  -Xmx3096M -Xss8192k -XX:+UseCompressedOops -XX:-UseParallelOldGC -Dfile.encoding=UTF-8 -cp imagerank-1.0.0-SNAPSHOT.jar br.ufmg.dcc.imagerank.main.ImageRankBatchProcessor $diretorioBase $diretorioImagens $diretorioDescritores $diretorioPares $diretorioLACDataset $arquivoParesOriginal $totalParesGerar 2&> $arquivoLog &

echo -e "Acompannhe o log do arquivo '"$arquivoLog"'..."